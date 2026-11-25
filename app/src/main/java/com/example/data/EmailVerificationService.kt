package com.example.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class EmailVerificationBackendResult {
    data class CustomEmailSent(
        val message: String,
        val provider: String = "emailjs"
    ) : EmailVerificationBackendResult()

    data class FirebaseFallback(
        val provider: String = "firebase",
        val backendMessage: String? = null
    ) : EmailVerificationBackendResult()

    data class DeliveryUncertain(
        val message: String
    ) : EmailVerificationBackendResult()

    data class AlreadyVerified(
        val message: String
    ) : EmailVerificationBackendResult()

    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val isNetworkError: Boolean = false,
        val isAuthError: Boolean = false
    ) : EmailVerificationBackendResult()
}

class EmailVerificationService(
    private val endpointUrl: String = DEFAULT_VERIFY_EMAIL_URL,
    private val httpClient: OkHttpClient = defaultHttpClient,
    private val userTokenProvider: suspend (FirebaseUser) -> Result<String> = { user ->
        fetchFreshIdToken(user)
    }
) {
    companion object {
        private const val TAG = "EmailVerificationSvc"

        const val DEFAULT_VERIFY_EMAIL_URL = "https://kcet-password-reset.vercel.app/api/send-verification-email"
        const val MSG_CUSTOM_VERIFICATION_SENT =
            "Verification email sent successfully. Please check your inbox."
        const val MSG_DELIVERY_UNCERTAIN =
            "We could not confirm email delivery. Please check your inbox and spam folder before trying again."
        const val MSG_ALREADY_VERIFIED =
            "Your email is already verified."

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val BEARER_REGEX = Regex("Bearer\\s+[A-Za-z0-9._-]+", RegexOption.IGNORE_CASE)
        private val JWT_REGEX = Regex("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]*")
        private val SENSITIVE_PARAM_REGEX = Regex(
            "(oobCode|apiKey|password|pass|token|idToken|secret|private_key|privateKey)\\s*[:=]\\s*[^&\\s,;\"'}]+",
            RegexOption.IGNORE_CASE
        )

        // Prioritize IPv4 addresses first to avoid Android emulator / mobile IPv6 connect stalls,
        // while keeping IPv6 addresses as fallback.
        private val ipv4FirstDns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val addresses = Dns.SYSTEM.lookup(hostname)
                val ipv4 = addresses.filterIsInstance<Inet4Address>()
                val other = addresses.filterNot { it is Inet4Address }
                return ipv4 + other
            }
        }

        // Dedicated HTTP client with no HTTP body/header logging so Firebase ID tokens are never logged,
        // and generous timeouts for serverless cold starts + Firebase Admin + EmailJS dispatch.
        private val defaultHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .dns(ipv4FirstDns)
                .connectTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }

        val instance: EmailVerificationService by lazy { EmailVerificationService() }

        internal fun sanitizeDiagnosticText(raw: String?, maxLength: Int = 180): String {
            if (raw.isNullOrBlank()) return ""
            var cleaned = raw.trim()
            cleaned = BEARER_REGEX.replace(cleaned, "Bearer [REDACTED]")
            cleaned = JWT_REGEX.replace(cleaned, "[REDACTED_TOKEN]")
            cleaned = SENSITIVE_PARAM_REGEX.replace(cleaned) { match ->
                val key = match.groupValues[1]
                "$key=[REDACTED]"
            }
            cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
            return if (cleaned.length > maxLength) cleaned.take(maxLength) + "..." else cleaned
        }

        private fun safeLogD(msg: String) {
            try {
                Log.d(TAG, msg)
            } catch (_: Throwable) {}
        }

        private fun safeLogW(msg: String) {
            try {
                Log.w(TAG, msg)
            } catch (_: Throwable) {}
        }

        private fun safeLogE(msg: String, tr: Throwable? = null) {
            try {
                if (tr != null) Log.e(TAG, msg, tr) else Log.e(TAG, msg)
            } catch (_: Throwable) {}
        }

        suspend fun fetchFreshIdToken(user: FirebaseUser): Result<String> {
            return try {
                safeLogD("Fetching fresh Firebase ID token for uid=${user.uid}, emailVerified=${user.isEmailVerified}")
                val tokenResult = user.getIdToken(true).awaitTask()
                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    safeLogW("Firebase ID token retrieval returned blank token for uid=${user.uid}")
                    Result.failure(IllegalStateException("Missing Firebase ID token"))
                } else {
                    safeLogD("Firebase ID token retrieval succeeded for uid=${user.uid}")
                    Result.success(token)
                }
            } catch (e: Exception) {
                safeLogE(
                    "Firebase ID token retrieval failed for uid=${user.uid}: ${e.javaClass.simpleName}: ${sanitizeDiagnosticText(e.message)}"
                )
                Result.failure(e)
            }
        }

        private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
            addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result)
            }
            addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
            addOnCanceledListener {
                if (cont.isActive) cont.cancel()
            }
        }

        fun parseBackendResponse(httpCode: Int, rawBody: String): EmailVerificationBackendResult {
            val trimmedBody = rawBody.trim()
            var jsonParseError: String? = null
            val json = try {
                if (trimmedBody.isNotBlank()) JSONObject(trimmedBody) else null
            } catch (e: Exception) {
                jsonParseError = sanitizeDiagnosticText(e.message)
                null
            }

            if (json != null) {
                val success = json.optBoolean("success", false)
                val provider = json.optNullableString("provider")
                val alreadyVerified = json.optBoolean("alreadyVerified", false)
                val fallbackToFirebase = json.optBoolean("fallbackToFirebase", false)
                val deliveryUncertain = json.optBoolean("deliveryUncertain", false)
                val rawBackendMessage = json.optNullableString("message")
                    ?: json.optNullableString("error")
                val backendMessage = rawBackendMessage?.let { sanitizeDiagnosticText(it) }?.ifBlank { null }

                // Scenario: Already verified — do not send duplicate email
                if (alreadyVerified && (success || httpCode in 200..299)) {
                    return EmailVerificationBackendResult.AlreadyVerified(
                        message = MSG_ALREADY_VERIFIED
                    )
                }

                // Scenario: Uncertain delivery — MUST NOT call Firebase sendEmailVerification() fallback
                if (deliveryUncertain) {
                    return EmailVerificationBackendResult.DeliveryUncertain(
                        message = MSG_DELIVERY_UNCERTAIN
                    )
                }

                // Scenario: Explicit fallback to Firebase sendEmailVerification()
                if (fallbackToFirebase) {
                    return EmailVerificationBackendResult.FirebaseFallback(
                        provider = provider ?: "firebase",
                        backendMessage = backendMessage
                    )
                }

                // Scenario: Custom EmailJS verification email sent successfully
                if (httpCode in 200..299 && success) {
                    return EmailVerificationBackendResult.CustomEmailSent(
                        message = MSG_CUSTOM_VERIFICATION_SENT,
                        provider = provider ?: "emailjs"
                    )
                }

                if (httpCode == 401) {
                    return EmailVerificationBackendResult.Error(
                        message = backendMessage?.let {
                            "Verification session expired or invalid ($it). Please sign in again."
                        } ?: "Verification session expired or invalid (HTTP 401). Please sign in again.",
                        httpCode = 401,
                        isAuthError = true
                    )
                }

                if (httpCode == 403) {
                    return EmailVerificationBackendResult.Error(
                        message = backendMessage?.let {
                            "$it (HTTP 403)"
                        } ?: "Not authorized to send verification email for this account (HTTP 403).",
                        httpCode = 403,
                        isAuthError = true
                    )
                }

                if (!backendMessage.isNullOrBlank()) {
                    val diagnosticSuffix = if (httpCode !in 200..299) " (HTTP $httpCode)" else ""
                    return EmailVerificationBackendResult.Error(
                        message = "$backendMessage$diagnosticSuffix",
                        httpCode = httpCode
                    )
                }

                if (httpCode in 200..299) {
                    return EmailVerificationBackendResult.Error(
                        message = "Unexpected verification server response (HTTP $httpCode). Please check your inbox before retrying.",
                        httpCode = httpCode
                    )
                }
            }

            if (httpCode in 200..299) {
                val parseHint = jsonParseError?.takeIf { it.isNotBlank() } ?: "empty or invalid JSON response"
                return EmailVerificationBackendResult.Error(
                    message = "Invalid response from verification server (HTTP $httpCode: $parseHint). Please check your inbox before retrying.",
                    httpCode = httpCode
                )
            }

            return when (httpCode) {
                401 -> EmailVerificationBackendResult.Error(
                    message = "Verification session expired or invalid (HTTP 401). Please sign in again.",
                    httpCode = 401,
                    isAuthError = true
                )
                403 -> EmailVerificationBackendResult.Error(
                    message = "Not authorized to send verification email for this account (HTTP 403).",
                    httpCode = 403,
                    isAuthError = true
                )
                429 -> EmailVerificationBackendResult.Error(
                    message = "Too many verification email requests (HTTP 429). Please wait a few minutes and try again.",
                    httpCode = 429
                )
                else -> EmailVerificationBackendResult.Error(
                    message = "Unable to send verification email right now (HTTP $httpCode). Please try again later.",
                    httpCode = httpCode
                )
            }
        }

        private fun JSONObject.optNullableString(key: String): String? {
            if (!has(key) || isNull(key)) return null
            val raw = optString(key, "").trim()
            return if (raw.isBlank() || raw.equals("null", ignoreCase = true)) null else raw
        }
    }

    suspend fun sendVerificationEmail(user: FirebaseUser): EmailVerificationBackendResult {
        safeLogD("Starting verification dispatch for currentUser uid=${user.uid}, emailVerified=${user.isEmailVerified}")
        if (user.isEmailVerified) {
            safeLogD("User already verified locally for uid=${user.uid}; skipping backend call")
            return EmailVerificationBackendResult.AlreadyVerified(MSG_ALREADY_VERIFIED)
        }

        val tokenResult = userTokenProvider(user)
        val idToken = tokenResult.getOrNull()
        if (tokenResult.isFailure || idToken.isNullOrBlank()) {
            val tokenErr = sanitizeDiagnosticText(tokenResult.exceptionOrNull()?.message)
                .ifBlank { "missing or expired ID token" }
            safeLogW("Aborting verification API request: ID token unavailable ($tokenErr)")
            return EmailVerificationBackendResult.Error(
                message = "Authentication session expired or invalid ($tokenErr). Please sign in again to verify your email.",
                httpCode = 401,
                isAuthError = true
            )
        }

        return sendVerificationEmailWithToken(idToken = idToken, isAlreadyVerified = user.isEmailVerified)
    }

    suspend fun sendVerificationEmailWithToken(
        idToken: String,
        isAlreadyVerified: Boolean = false
    ): EmailVerificationBackendResult = withContext(Dispatchers.IO) {
        if (isAlreadyVerified) {
            safeLogD("Skipping API request: email already verified")
            return@withContext EmailVerificationBackendResult.AlreadyVerified(MSG_ALREADY_VERIFIED)
        }
        if (idToken.isBlank()) {
            safeLogW("Skipping API request: blank Firebase ID token")
            return@withContext EmailVerificationBackendResult.Error(
                message = "Authentication token is missing or invalid. Please sign in again.",
                httpCode = 401,
                isAuthError = true
            )
        }

        val request = Request.Builder()
            .url(endpointUrl)
            .post("{}".toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", "Bearer $idToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        safeLogD("Sending verification API request: POST $endpointUrl")

        try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                val parsed = parseBackendResponse(response.code, rawBody)
                when (parsed) {
                    is EmailVerificationBackendResult.Error -> {
                        safeLogW(
                            "Verification API error response: httpCode=${response.code}, detail=${sanitizeDiagnosticText(parsed.message)}"
                        )
                    }
                    else -> {
                        safeLogD(
                            "Verification API response: httpCode=${response.code}, outcome=${parsed.javaClass.simpleName}"
                        )
                    }
                }
                parsed
            }
        } catch (e: IOException) {
            val sanitizedCause = sanitizeDiagnosticText(e.message).ifBlank { e.javaClass.simpleName }
            safeLogW("Verification API IOException (${e.javaClass.simpleName}): $sanitizedCause")
            val diagnosticMessage = when (e) {
                is SocketTimeoutException ->
                    "Unable to connect to the server: request timed out ($sanitizedCause). Please check your inbox and spam folder before trying again."
                is UnknownHostException ->
                    "Unable to connect to the server: DNS lookup failed ($sanitizedCause). Please check your internet connection."
                is SSLHandshakeException, is SSLException ->
                    "Unable to connect to the server: secure SSL/TLS connection failed ($sanitizedCause)."
                is ConnectException ->
                    "Unable to connect to the server: connection refused or unreachable ($sanitizedCause)."
                else ->
                    "Unable to connect to the server (${e.javaClass.simpleName}: $sanitizedCause). Please check your internet connection and try again."
            }
            EmailVerificationBackendResult.Error(
                message = diagnosticMessage,
                isNetworkError = true
            )
        } catch (e: Exception) {
            val sanitizedCause = sanitizeDiagnosticText(e.message).ifBlank { e.javaClass.simpleName }
            safeLogE("Verification API unexpected failure (${e.javaClass.simpleName}): $sanitizedCause")
            EmailVerificationBackendResult.Error(
                message = "Unable to send verification email (${e.javaClass.simpleName}: $sanitizedCause). Please try again."
            )
        }
    }
}
