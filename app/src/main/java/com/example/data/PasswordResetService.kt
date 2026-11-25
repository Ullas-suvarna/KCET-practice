package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class EmailValidationResult {
    data class Valid(val normalizedEmail: String) : EmailValidationResult()
    data class Invalid(val errorMessage: String) : EmailValidationResult()
}

sealed class PasswordResetBackendResult {
    data class EmailJsSent(
        val message: String,
        val provider: String = "emailjs"
    ) : PasswordResetBackendResult()

    data class FirebaseFallback(
        val provider: String = "firebase",
        val backendMessage: String? = null
    ) : PasswordResetBackendResult()

    data class DeliveryUncertain(
        val message: String
    ) : PasswordResetBackendResult()

    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val isNetworkError: Boolean = false
    ) : PasswordResetBackendResult()
}

class PasswordResetService(
    private val endpointUrl: String = DEFAULT_RESET_EMAIL_URL,
    private val httpClient: OkHttpClient = defaultHttpClient
) {
    companion object {
        const val DEFAULT_RESET_EMAIL_URL = "https://kcet-password-reset.vercel.app/api/send-reset-email"
        const val MSG_EMAILJS_SUCCESS =
            "✅ Password reset email sent successfully! Please check your inbox and spam folder."
        const val MSG_DELIVERY_UNCERTAIN =
            "Your request is being processed. Please wait a few minutes before requesting another reset email."

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val EMAIL_REGEX: Pattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

        private val defaultHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        }

        val instance: PasswordResetService by lazy { PasswordResetService() }

        fun validateEmail(rawEmail: String): EmailValidationResult {
            val clean = rawEmail.trim().lowercase(Locale.US)
            if (clean.isEmpty()) {
                return EmailValidationResult.Invalid("Please enter your registered email address.")
            }
            if (!EMAIL_REGEX.matcher(clean).matches()) {
                return EmailValidationResult.Invalid(
                    "Please enter a valid email address (e.g. student@example.com)."
                )
            }
            return EmailValidationResult.Valid(clean)
        }

        fun parseBackendResponse(httpCode: Int, rawBody: String): PasswordResetBackendResult {
            val json = try {
                if (rawBody.isNotBlank()) JSONObject(rawBody) else null
            } catch (_: Exception) {
                null
            }

            if (json != null) {
                val success = json.optBoolean("success", false)
                val provider = json.optNullableString("provider")
                val fallbackToFirebase = json.optBoolean("fallbackToFirebase", false)
                val deliveryUncertain = json.optBoolean("deliveryUncertain", false)
                val backendMessage = json.optNullableString("message")
                    ?: json.optNullableString("error")

                // Scenario C: Uncertain email delivery — MUST NOT call Firebase fallback
                if (deliveryUncertain) {
                    return PasswordResetBackendResult.DeliveryUncertain(
                        message = MSG_DELIVERY_UNCERTAIN
                    )
                }

                // Scenario B: Firebase fallback — caller invokes FirebaseAuth sendPasswordResetEmail()
                if (!success && fallbackToFirebase) {
                    return PasswordResetBackendResult.FirebaseFallback(
                        provider = provider ?: "firebase",
                        backendMessage = backendMessage
                    )
                }

                // Scenario A: Successful backend / EmailJS delivery
                if (httpCode in 200..299 && success) {
                    val resolvedProvider = provider ?: "emailjs"
                    return PasswordResetBackendResult.EmailJsSent(
                        message = MSG_EMAILJS_SUCCESS,
                        provider = resolvedProvider
                    )
                }

                // Other backend error response
                if (!backendMessage.isNullOrBlank()) {
                    return PasswordResetBackendResult.Error(
                        message = backendMessage,
                        httpCode = httpCode
                    )
                }
            }

            if (httpCode == 429) {
                return PasswordResetBackendResult.Error(
                    message = "Too many password reset requests. Please wait a few minutes and try again.",
                    httpCode = 429
                )
            }

            return PasswordResetBackendResult.Error(
                message = "Unable to process your password reset request right now (HTTP $httpCode). Please try again later.",
                httpCode = httpCode
            )
        }

        private fun JSONObject.optNullableString(key: String): String? {
            if (!has(key) || isNull(key)) return null
            val raw = optString(key, "").trim()
            return if (raw.isBlank() || raw.equals("null", ignoreCase = true)) null else raw
        }
    }

    suspend fun sendResetEmail(rawEmail: String): PasswordResetBackendResult =
        withContext(Dispatchers.IO) {
            val cleanEmail = when (val validation = validateEmail(rawEmail)) {
                is EmailValidationResult.Valid -> validation.normalizedEmail
                is EmailValidationResult.Invalid -> {
                    return@withContext PasswordResetBackendResult.Error(validation.errorMessage)
                }
            }

            val payload = JSONObject().apply {
                put("email", cleanEmail)
            }.toString()

            val request = Request.Builder()
                .url(endpointUrl)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    parseBackendResponse(response.code, rawBody)
                }
            } catch (_: IOException) {
                PasswordResetBackendResult.Error(
                    message = "Unable to connect to the server. Please check your internet connection and try again.",
                    isNetworkError = true
                )
            } catch (e: Exception) {
                PasswordResetBackendResult.Error(
                    message = e.message?.takeIf { it.isNotBlank() }
                        ?: "Unable to process your password reset request right now. Please try again."
                )
            }
        }
}
