package com.example.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class EmailDeliverySettings(
    val activeProvider: String,
    val emailjsStatus: String,
    val customEmailEnabled: Boolean,
    val customVerificationEmailEnabled: Boolean = false,
    val lastFailureReason: String?,
    val lastUpdatedTime: String,
    val rawLastUpdatedTime: String? = null,
    val updatedBy: String? = null
)

data class FirebaseAdminSessionStatus(
    val isSignedIn: Boolean,
    val email: String?,
    val isEmailVerified: Boolean
)

sealed class EmailSettingsApiResult<out T> {
    data class Success<T>(val data: T, val message: String? = null) : EmailSettingsApiResult<T>()
    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val isNetworkError: Boolean = false,
        val isAuthError: Boolean = false
    ) : EmailSettingsApiResult<Nothing>()
}

class EmailSettingsService(
    private val endpointUrl: String = DEFAULT_ENDPOINT_URL,
    private val httpClient: OkHttpClient = defaultHttpClient,
    private val tokenProvider: suspend () -> EmailSettingsApiResult<String> = { fetchFirebaseAdminIdToken() }
) {
    companion object {
        const val DEFAULT_ENDPOINT_URL = "https://kcet-password-reset.vercel.app/api/admin/email-settings"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        // Dedicated HTTP client with no logging interceptor so Firebase ID tokens are never logged.
        private val defaultHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        }

        val instance: EmailSettingsService by lazy { EmailSettingsService() }

        fun getFirebaseAdminSessionStatus(): FirebaseAdminSessionStatus {
            val user = try {
                FirebaseAuth.getInstance().currentUser
            } catch (_: Exception) {
                null
            }
            return FirebaseAdminSessionStatus(
                isSignedIn = user != null,
                email = user?.email,
                isEmailVerified = user?.isEmailVerified == true
            )
        }

        suspend fun fetchFirebaseAdminIdToken(): EmailSettingsApiResult<String> {
            val auth = try {
                FirebaseAuth.getInstance()
            } catch (_: Exception) {
                return EmailSettingsApiResult.Error(
                    message = "Administrator authentication is missing or invalid.",
                    httpCode = 401,
                    isAuthError = true
                )
            }

            val user = auth.currentUser
                ?: return EmailSettingsApiResult.Error(
                    message = "Administrator authentication is missing or invalid. Please sign in with Firebase Authentication.",
                    httpCode = 401,
                    isAuthError = true
                )

            if (!user.isEmailVerified) {
                try {
                    user.reload().awaitTask()
                } catch (_: Exception) {
                    // Proceed with current state check if reload cannot complete
                }
            }

            if (!user.isEmailVerified) {
                return EmailSettingsApiResult.Error(
                    message = "Signed-in Firebase email is not verified (email_verified = true required).",
                    httpCode = 403,
                    isAuthError = true
                )
            }

            return try {
                val tokenResult = user.getIdToken(false).awaitTask()
                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    EmailSettingsApiResult.Error(
                        message = "Administrator authentication is missing or invalid.",
                        httpCode = 401,
                        isAuthError = true
                    )
                } else {
                    EmailSettingsApiResult.Success(token)
                }
            } catch (_: Exception) {
                EmailSettingsApiResult.Error(
                    message = "Administrator authentication is missing or invalid.",
                    httpCode = 401,
                    isAuthError = true
                )
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

        fun parseEmailSettingsJson(rawJson: String): EmailSettingsApiResult<EmailDeliverySettings> {
            if (rawJson.isBlank()) {
                return EmailSettingsApiResult.Error("Received an empty response from the server.")
            }

            return try {
                val root = JSONObject(rawJson)

                if (root.has("ok") && !root.optBoolean("ok", true)) {
                    val serverMsg = extractServerErrorMessage(root)
                        ?: "The server rejected the email settings request."
                    return EmailSettingsApiResult.Error(serverMsg)
                }
                if (root.has("success") && !root.optBoolean("success", true)) {
                    val serverMsg = extractServerErrorMessage(root)
                        ?: "The server rejected the email settings request."
                    return EmailSettingsApiResult.Error(serverMsg)
                }

                val payload = when {
                    root.optJSONObject("settings") != null -> root.getJSONObject("settings")
                    root.optJSONObject("data") != null -> root.getJSONObject("data")
                    else -> root
                }

                val customEmailEnabled = when {
                    payload.has("customEmailEnabled") && !payload.isNull("customEmailEnabled") ->
                        payload.optBoolean("customEmailEnabled", false)
                    root.has("customEmailEnabled") && !root.isNull("customEmailEnabled") ->
                        root.optBoolean("customEmailEnabled", false)
                    else -> {
                        val providerHint = payload.optString("activeProvider", root.optString("activeProvider", ""))
                        providerHint.equals("emailjs", ignoreCase = true)
                    }
                }

                val customVerificationEmailEnabled = when {
                    payload.has("customVerificationEmailEnabled") && !payload.isNull("customVerificationEmailEnabled") ->
                        payload.optBoolean("customVerificationEmailEnabled", false)
                    root.has("customVerificationEmailEnabled") && !root.isNull("customVerificationEmailEnabled") ->
                        root.optBoolean("customVerificationEmailEnabled", false)
                    else -> false
                }

                val activeProviderRaw = firstNonBlankString(
                    payload.optNullableString("activeProvider"),
                    payload.optNullableString("provider"),
                    root.optNullableString("activeProvider"),
                    root.optNullableString("provider")
                )
                val activeProvider = activeProviderRaw ?: if (customEmailEnabled) "emailjs" else "firebase"

                val statusRaw = firstNonBlankString(
                    payload.optNullableString("emailjsStatus"),
                    payload.optNullableString("emailJsStatus"),
                    payload.optNullableString("status"),
                    root.optNullableString("emailjsStatus"),
                    root.optNullableString("emailJsStatus")
                )
                val emailjsStatus = when {
                    statusRaw != null && (statusRaw.equals("ENABLED", ignoreCase = true) || statusRaw.equals("ACTIVE", ignoreCase = true)) -> "ENABLED"
                    statusRaw != null && (statusRaw.equals("DISABLED", ignoreCase = true) || statusRaw.equals("INACTIVE", ignoreCase = true)) -> "DISABLED"
                    statusRaw != null -> statusRaw.uppercase(Locale.US)
                    else -> if (customEmailEnabled) "ENABLED" else "DISABLED"
                }

                val lastFailureReason = firstNonBlankString(
                    payload.optNullableString("lastFailureReason"),
                    payload.optNullableString("failureReason"),
                    root.optNullableString("lastFailureReason")
                )

                val rawUpdated = extractRawTimestamp(payload, root)
                val formattedUpdated = formatUpdatedTimestamp(rawUpdated)

                val updatedBy = firstNonBlankString(
                    payload.optNullableString("updatedBy"),
                    root.optNullableString("updatedBy")
                )

                val message = firstNonBlankString(
                    root.optNullableString("message"),
                    payload.optNullableString("message")
                )

                EmailSettingsApiResult.Success(
                    data = EmailDeliverySettings(
                        activeProvider = activeProvider,
                        emailjsStatus = emailjsStatus,
                        customEmailEnabled = customEmailEnabled,
                        customVerificationEmailEnabled = customVerificationEmailEnabled,
                        lastFailureReason = lastFailureReason,
                        lastUpdatedTime = formattedUpdated,
                        rawLastUpdatedTime = rawUpdated,
                        updatedBy = updatedBy
                    ),
                    message = message
                )
            } catch (e: Exception) {
                EmailSettingsApiResult.Error("Unable to parse server response: ${e.message ?: "Malformed JSON"}")
            }
        }

        fun mapHttpError(code: Int, rawBody: String?): EmailSettingsApiResult.Error {
            val serverMessage = try {
                if (!rawBody.isNullOrBlank()) {
                    extractServerErrorMessage(JSONObject(rawBody))
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            return when (code) {
                401 -> EmailSettingsApiResult.Error(
                    message = if (serverMessage.isNullOrBlank()) {
                        "Administrator authentication is missing or invalid."
                    } else {
                        "Administrator authentication is missing or invalid. ($serverMessage)"
                    },
                    httpCode = 401,
                    isAuthError = true
                )
                403 -> EmailSettingsApiResult.Error(
                    message = if (serverMessage.isNullOrBlank()) {
                        "The signed-in account is not authorized."
                    } else {
                        "The signed-in account is not authorized. ($serverMessage)"
                    },
                    httpCode = 403,
                    isAuthError = true
                )
                else -> {
                    val detail = serverMessage ?: "Unexpected server response."
                    EmailSettingsApiResult.Error(
                        message = "Request failed (HTTP $code): $detail",
                        httpCode = code
                    )
                }
            }
        }

        private fun extractServerErrorMessage(json: JSONObject): String? {
            return firstNonBlankString(
                json.optNullableString("error"),
                json.optNullableString("message"),
                json.optNullableString("detail"),
                json.optNullableString("reason")
            )
        }

        private fun extractRawTimestamp(payload: JSONObject, root: JSONObject): String? {
            val keys = listOf("lastUpdatedTime", "updatedAt", "lastUpdated", "updated_at")
            for (obj in listOf(payload, root)) {
                for (key in keys) {
                    if (!obj.has(key) || obj.isNull(key)) continue
                    val value = obj.opt(key) ?: continue
                    when (value) {
                        is String -> if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) return value.trim()
                        is Number -> {
                            val num = value.toLong()
                            if (num > 0L) return num.toString()
                        }
                        is JSONObject -> {
                            val sec = when {
                                value.has("_seconds") -> value.optLong("_seconds", -1L)
                                value.has("seconds") -> value.optLong("seconds", -1L)
                                else -> -1L
                            }
                            if (sec > 0L) return (sec * 1000L).toString()
                        }
                    }
                }
            }
            return null
        }

        fun formatUpdatedTimestamp(raw: String?): String {
            if (raw.isNullOrBlank() || raw.equals("null", ignoreCase = true)) {
                return "Never updated"
            }

            val outFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a z", Locale.getDefault())

            // Try numeric epoch (millis or seconds)
            val epoch = raw.toLongOrNull()
            if (epoch != null && epoch > 0L) {
                val millis = if (epoch < 10_000_000_000L) epoch * 1000L else epoch
                return outFormatter.format(Date(millis))
            }

            // Try ISO-8601 via java.time.Instant (available on minSdk 26+)
            return try {
                val instant = Instant.parse(raw)
                outFormatter.format(Date.from(instant))
            } catch (_: Exception) {
                val isoFormats = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssXXX"
                )
                for (pattern in isoFormats) {
                    try {
                        val parser = SimpleDateFormat(pattern, Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val date = parser.parse(raw)
                        if (date != null) {
                            return outFormatter.format(date)
                        }
                    } catch (_: Exception) {
                    }
                }
                raw
            }
        }

        private fun JSONObject.optNullableString(key: String): String? {
            if (!has(key) || isNull(key)) return null
            val raw = optString(key, "").trim()
            return if (raw.isBlank() || raw.equals("null", ignoreCase = true)) null else raw
        }

        private fun firstNonBlankString(vararg values: String?): String? {
            for (v in values) {
                if (!v.isNullOrBlank()) return v
            }
            return null
        }
    }

    suspend fun fetchEmailSettings(): EmailSettingsApiResult<EmailDeliverySettings> =
        withContext(Dispatchers.IO) {
            val tokenResult = tokenProvider()
            val idToken = when (tokenResult) {
                is EmailSettingsApiResult.Success -> tokenResult.data
                is EmailSettingsApiResult.Error -> return@withContext tokenResult
            }

            val request = Request.Builder()
                .url(endpointUrl)
                .get()
                .header("Authorization", "Bearer $idToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            executeRequest(request)
        }

    suspend fun updateEmailSettings(customEmailEnabled: Boolean): EmailSettingsApiResult<EmailDeliverySettings> =
        postSettingsPayload(JSONObject().apply {
            put("customEmailEnabled", customEmailEnabled)
        })

    suspend fun updateCustomVerificationEmailSetting(
        customVerificationEmailEnabled: Boolean
    ): EmailSettingsApiResult<EmailDeliverySettings> =
        postSettingsPayload(JSONObject().apply {
            put("customVerificationEmailEnabled", customVerificationEmailEnabled)
        })

    private suspend fun postSettingsPayload(bodyJsonObj: JSONObject): EmailSettingsApiResult<EmailDeliverySettings> =
        withContext(Dispatchers.IO) {
            val tokenResult = tokenProvider()
            val idToken = when (tokenResult) {
                is EmailSettingsApiResult.Success -> tokenResult.data
                is EmailSettingsApiResult.Error -> return@withContext tokenResult
            }

            val bodyJson = bodyJsonObj.toString()

            val request = Request.Builder()
                .url(endpointUrl)
                .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
                .header("Authorization", "Bearer $idToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            executeRequest(request)
        }

    private fun executeRequest(request: Request): EmailSettingsApiResult<EmailDeliverySettings> {
        return try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return mapHttpError(response.code, rawBody)
                }
                parseEmailSettingsJson(rawBody)
            }
        } catch (_: IOException) {
            EmailSettingsApiResult.Error(
                message = "Unable to connect to the server. Try again.",
                isNetworkError = true
            )
        } catch (e: Exception) {
            EmailSettingsApiResult.Error(
                message = e.message?.takeIf { it.isNotBlank() } ?: "Unable to connect to the server. Try again."
            )
        }
    }
}
