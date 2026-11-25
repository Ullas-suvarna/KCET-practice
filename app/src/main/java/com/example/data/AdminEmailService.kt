package com.example.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AdminRegisteredStudent(
    val uid: String,
    val name: String,
    val email: String,
    val studentId: String = ""
)

data class AdminStudentsPage(
    val students: List<AdminRegisteredStudent>,
    val nextPageToken: String? = null
)

data class AdminSendEmailResult(
    val recipientEmail: String,
    val message: String
)

enum class AdminEmailDeliveryStatus(val label: String) {
    SUBMITTED("Submitted"),
    UNCERTAIN("Uncertain"),
    FAILED("Failed")
}

data class AdminEmailLogItem(
    val id: String,
    val recipientName: String,
    val recipientEmail: String,
    val subject: String,
    val status: AdminEmailDeliveryStatus,
    val rawStatus: String,
    val timestampFormatted: String,
    val rawTimestamp: String? = null,
    val errorDetail: String? = null
)

sealed class AdminEmailApiResult<out T> {
    data class Success<T>(val data: T, val message: String? = null) : AdminEmailApiResult<T>()
    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val isNetworkError: Boolean = false,
        val isAuthError: Boolean = false,
        val isTimeoutUncertain: Boolean = false
    ) : AdminEmailApiResult<Nothing>()
}

class AdminEmailService(
    private val studentsUrl: String = DEFAULT_STUDENTS_URL,
    private val sendEmailUrl: String = DEFAULT_SEND_EMAIL_URL,
    private val emailLogsUrl: String = DEFAULT_EMAIL_LOGS_URL,
    private val httpClient: OkHttpClient = defaultHttpClient,
    private val tokenProvider: suspend () -> AdminEmailApiResult<String> = { fetchFreshFirebaseAdminIdToken() }
) {
    companion object {
        const val DEFAULT_STUDENTS_URL = "https://kcet-password-reset.vercel.app/api/admin/students"
        const val DEFAULT_SEND_EMAIL_URL = "https://kcet-password-reset.vercel.app/api/admin/send-student-email"
        const val DEFAULT_EMAIL_LOGS_URL = "https://kcet-password-reset.vercel.app/api/admin/email-logs"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val BEARER_REGEX = Regex("Bearer\\s+[A-Za-z0-9._-]+", RegexOption.IGNORE_CASE)
        private val JWT_REGEX = Regex("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]*")

        // Prioritize IPv4 addresses first to avoid emulator/mobile IPv6 stalls while preserving IPv6 fallback.
        private val ipv4FirstDns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val addresses = Dns.SYSTEM.lookup(hostname)
                val ipv4 = addresses.filterIsInstance<Inet4Address>()
                val other = addresses.filterNot { it is Inet4Address }
                return ipv4 + other
            }
        }

        // Dedicated HTTP client with no HTTP body/header logging so Firebase ID tokens are never exposed.
        private val defaultHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .dns(ipv4FirstDns)
                .connectTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .build()
        }

        val instance: AdminEmailService by lazy { AdminEmailService() }

        fun getFirebaseAdminSessionStatus(): FirebaseAdminSessionStatus {
            return EmailSettingsService.getFirebaseAdminSessionStatus()
        }

        suspend fun fetchFreshFirebaseAdminIdToken(): AdminEmailApiResult<String> {
            val auth = try {
                FirebaseAuth.getInstance()
            } catch (_: Exception) {
                return AdminEmailApiResult.Error(
                    message = "Administrator authentication is missing or invalid. Please sign in with Firebase Authentication.",
                    httpCode = 401,
                    isAuthError = true
                )
            }

            val user = auth.currentUser
                ?: return AdminEmailApiResult.Error(
                    message = "Administrator authentication is missing or invalid. Please sign in with a verified Firebase Admin account.",
                    httpCode = 401,
                    isAuthError = true
                )

            if (!user.isEmailVerified) {
                try {
                    user.reload().awaitTask()
                } catch (_: Exception) {
                    // Proceed with current state if reload fails offline
                }
            }

            if (!user.isEmailVerified) {
                return AdminEmailApiResult.Error(
                    message = "Signed-in Firebase admin email is not verified (email_verified = true required).",
                    httpCode = 403,
                    isAuthError = true
                )
            }

            return try {
                // Force-refresh ID token so backend Firebase Admin SDK never sees a stale session token.
                val tokenResult = user.getIdToken(true).awaitTask()
                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    AdminEmailApiResult.Error(
                        message = "Unable to obtain a valid Firebase ID token. Please sign in again.",
                        httpCode = 401,
                        isAuthError = true
                    )
                } else {
                    AdminEmailApiResult.Success(token)
                }
            } catch (_: Exception) {
                AdminEmailApiResult.Error(
                    message = "Failed to refresh Firebase admin ID token. Please check your connection or sign in again.",
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

        fun filterStudents(
            students: List<AdminRegisteredStudent>,
            query: String
        ): List<AdminRegisteredStudent> {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return students
            val needle = trimmed.lowercase(Locale.US)
            return students.filter { student ->
                student.name.lowercase(Locale.US).contains(needle) ||
                    student.email.lowercase(Locale.US).contains(needle) ||
                    student.studentId.lowercase(Locale.US).contains(needle)
            }
        }

        fun parseStudentsPageJson(rawJson: String): AdminEmailApiResult<AdminStudentsPage> {
            if (rawJson.isBlank()) {
                return AdminEmailApiResult.Error("Received an empty response while loading students.")
            }

            return try {
                val studentsArray: JSONArray?
                var nextPageToken: String? = null

                val trimmed = rawJson.trim()
                if (trimmed.startsWith("[")) {
                    studentsArray = JSONArray(trimmed)
                } else {
                    val root = JSONObject(trimmed)
                    if (isExplicitFailure(root)) {
                        return AdminEmailApiResult.Error(
                            extractServerErrorMessage(root) ?: "Unable to fetch registered students."
                        )
                    }

                    val dataObj = root.optJSONObject("data")
                    studentsArray = firstNonNullArray(
                        root.optJSONArray("students"),
                        root.optJSONArray("users"),
                        root.optJSONArray("items"),
                        dataObj?.optJSONArray("students"),
                        dataObj?.optJSONArray("users"),
                        dataObj?.optJSONArray("items")
                    )

                    nextPageToken = firstNonBlankString(
                        root.optNullableString("nextPageToken"),
                        root.optNullableString("pageToken"),
                        dataObj?.optNullableString("nextPageToken"),
                        dataObj?.optNullableString("pageToken")
                    )
                }

                if (studentsArray == null) {
                    return AdminEmailApiResult.Error("Unexpected student list format returned by server.")
                }

                val parsed = mutableListOf<AdminRegisteredStudent>()
                for (i in 0 until studentsArray.length()) {
                    val obj = studentsArray.optJSONObject(i) ?: continue
                    val uid = firstNonBlankString(
                        obj.optNullableString("uid"),
                        obj.optNullableString("id"),
                        obj.optNullableString("studentUid")
                    ) ?: continue
                    val email = firstNonBlankString(
                        obj.optNullableString("email"),
                        obj.optNullableString("registeredEmail")
                    ) ?: ""
                    val name = firstNonBlankString(
                        obj.optNullableString("name"),
                        obj.optNullableString("displayName"),
                        obj.optNullableString("fullName")
                    ) ?: email.substringBefore("@").ifBlank { "Student" }
                    val studentId = firstNonBlankString(
                        obj.optNullableString("studentId"),
                        obj.optNullableString("formattedStudentId"),
                        obj.optNullableString("rollNo")
                    ) ?: ""

                    parsed.add(
                        AdminRegisteredStudent(
                            uid = uid,
                            name = name,
                            email = email,
                            studentId = studentId
                        )
                    )
                }

                AdminEmailApiResult.Success(
                    AdminStudentsPage(
                        students = parsed.distinctBy { it.uid },
                        nextPageToken = nextPageToken
                    )
                )
            } catch (e: Exception) {
                AdminEmailApiResult.Error("Unable to parse students response: ${redactSensitive(e.message ?: "Malformed JSON")}")
            }
        }

        fun parseSendEmailJson(
            rawJson: String,
            fallbackRecipientEmail: String = ""
        ): AdminEmailApiResult<AdminSendEmailResult> {
            if (rawJson.isBlank()) {
                return AdminEmailApiResult.Error("Received an empty response from the send-email server.")
            }

            return try {
                val root = JSONObject(rawJson.trim())
                if (isExplicitFailure(root)) {
                    return AdminEmailApiResult.Error(
                        extractServerErrorMessage(root) ?: "Failed to send email to the selected student."
                    )
                }

                val payload = root.optJSONObject("data") ?: root
                val recipientEmail = firstNonBlankString(
                    payload.optNullableString("recipientEmail"),
                    root.optNullableString("recipientEmail"),
                    payload.optNullableString("email"),
                    root.optNullableString("to")
                ) ?: fallbackRecipientEmail

                val serverMessage = firstNonBlankString(
                    root.optNullableString("message"),
                    payload.optNullableString("message")
                ) ?: if (recipientEmail.isNotBlank()) {
                    "Email submitted successfully to $recipientEmail."
                } else {
                    "Email submitted successfully."
                }

                AdminEmailApiResult.Success(
                    data = AdminSendEmailResult(
                        recipientEmail = recipientEmail,
                        message = serverMessage
                    ),
                    message = serverMessage
                )
            } catch (e: Exception) {
                AdminEmailApiResult.Error("Unable to parse send-email response: ${redactSensitive(e.message ?: "Malformed JSON")}")
            }
        }

        fun parseEmailLogsJson(rawJson: String): AdminEmailApiResult<List<AdminEmailLogItem>> {
            if (rawJson.isBlank()) {
                return AdminEmailApiResult.Error("Received an empty response while loading email history.")
            }

            return try {
                val logsArray: JSONArray?
                val trimmed = rawJson.trim()
                if (trimmed.startsWith("[")) {
                    logsArray = JSONArray(trimmed)
                } else {
                    val root = JSONObject(trimmed)
                    if (isExplicitFailure(root)) {
                        return AdminEmailApiResult.Error(
                            extractServerErrorMessage(root) ?: "Unable to fetch email history."
                        )
                    }
                    val dataObj = root.optJSONObject("data")
                    logsArray = firstNonNullArray(
                        root.optJSONArray("logs"),
                        root.optJSONArray("emails"),
                        root.optJSONArray("history"),
                        root.optJSONArray("items"),
                        dataObj?.optJSONArray("logs"),
                        dataObj?.optJSONArray("emails"),
                        dataObj?.optJSONArray("history"),
                        dataObj?.optJSONArray("items")
                    )
                }

                if (logsArray == null) {
                    return AdminEmailApiResult.Error("Unexpected email history format returned by server.")
                }

                val items = mutableListOf<AdminEmailLogItem>()
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.optJSONObject(i) ?: continue
                    val id = firstNonBlankString(
                        obj.optNullableString("id"),
                        obj.optNullableString("logId")
                    ) ?: "log_$i"
                    val recipientName = firstNonBlankString(
                        obj.optNullableString("recipientName"),
                        obj.optNullableString("studentName"),
                        obj.optNullableString("name")
                    ) ?: "Student"
                    val recipientEmail = firstNonBlankString(
                        obj.optNullableString("recipientEmail"),
                        obj.optNullableString("email"),
                        obj.optNullableString("to")
                    ) ?: ""
                    val subject = firstNonBlankString(
                        obj.optNullableString("subject"),
                        obj.optNullableString("title")
                    ) ?: "(No Subject)"
                    val rawStatus = firstNonBlankString(
                        obj.optNullableString("status"),
                        obj.optNullableString("deliveryStatus"),
                        obj.optNullableString("state")
                    ) ?: "Uncertain"
                    val status = normalizeStatus(rawStatus)
                    val rawTimestamp = extractRawTimestamp(obj)
                    val formattedTimestamp = formatTimestamp(rawTimestamp)
                    val errorDetail = firstNonBlankString(
                        obj.optNullableString("errorMessage"),
                        obj.optNullableString("error"),
                        obj.optNullableString("failureReason"),
                        obj.optNullableString("reason")
                    )

                    items.add(
                        AdminEmailLogItem(
                            id = id,
                            recipientName = recipientName,
                            recipientEmail = recipientEmail,
                            subject = subject,
                            status = status,
                            rawStatus = rawStatus,
                            timestampFormatted = formattedTimestamp,
                            rawTimestamp = rawTimestamp,
                            errorDetail = errorDetail
                        )
                    )
                }

                AdminEmailApiResult.Success(items)
            } catch (e: Exception) {
                AdminEmailApiResult.Error("Unable to parse email history: ${redactSensitive(e.message ?: "Malformed JSON")}")
            }
        }

        fun normalizeStatus(raw: String): AdminEmailDeliveryStatus {
            return when (raw.trim().lowercase(Locale.US)) {
                "submitted", "sent", "delivered", "success", "ok" -> AdminEmailDeliveryStatus.SUBMITTED
                "failed", "error", "rejected", "bounced" -> AdminEmailDeliveryStatus.FAILED
                else -> AdminEmailDeliveryStatus.UNCERTAIN
            }
        }

        fun mapHttpError(code: Int, rawBody: String?): AdminEmailApiResult.Error {
            val serverMessage = try {
                if (!rawBody.isNullOrBlank()) {
                    extractServerErrorMessage(JSONObject(rawBody))
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }?.let { redactSensitive(it) }

            return when (code) {
                401 -> AdminEmailApiResult.Error(
                    message = if (serverMessage.isNullOrBlank()) {
                        "Administrator authentication is missing or expired. Please sign in again."
                    } else {
                        "Administrator authentication is missing or expired. ($serverMessage)"
                    },
                    httpCode = 401,
                    isAuthError = true
                )
                403 -> AdminEmailApiResult.Error(
                    message = if (serverMessage.isNullOrBlank()) {
                        "The signed-in account is not authorized for Admin Email access."
                    } else {
                        "The signed-in account is not authorized. ($serverMessage)"
                    },
                    httpCode = 403,
                    isAuthError = true
                )
                404 -> AdminEmailApiResult.Error(
                    message = serverMessage ?: "Selected student was not found in Firebase Auth.",
                    httpCode = 404
                )
                else -> {
                    val detail = serverMessage ?: "Unexpected server response."
                    AdminEmailApiResult.Error(
                        message = "Request failed (HTTP $code): $detail",
                        httpCode = code
                    )
                }
            }
        }

        private fun isExplicitFailure(root: JSONObject): Boolean {
            if (root.has("ok") && !root.isNull("ok") && !root.optBoolean("ok", true)) return true
            if (root.has("success") && !root.isNull("success") && !root.optBoolean("success", true)) return true
            return false
        }

        private fun extractServerErrorMessage(json: JSONObject): String? {
            return firstNonBlankString(
                json.optNullableString("error"),
                json.optNullableString("message"),
                json.optNullableString("detail"),
                json.optNullableString("reason")
            )
        }

        private fun extractRawTimestamp(obj: JSONObject): String? {
            val keys = listOf("timestamp", "sentAt", "createdAt", "updatedAt", "created_at", "date")
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
            return null
        }

        fun formatTimestamp(raw: String?): String {
            if (raw.isNullOrBlank() || raw.equals("null", ignoreCase = true)) {
                return "Time unavailable"
            }

            val outFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

            val epoch = raw.toLongOrNull()
            if (epoch != null && epoch > 0L) {
                val millis = if (epoch < 10_000_000_000L) epoch * 1000L else epoch
                return outFormatter.format(Date(millis))
            }

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
                        if (date != null) return outFormatter.format(date)
                    } catch (_: Exception) {
                    }
                }
                raw
            }
        }

        private fun redactSensitive(input: String): String {
            return input
                .replace(BEARER_REGEX, "Bearer [REDACTED]")
                .replace(JWT_REGEX, "[REDACTED_JWT]")
        }

        private fun firstNonNullArray(vararg arrays: JSONArray?): JSONArray? {
            for (arr in arrays) {
                if (arr != null) return arr
            }
            return null
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

    suspend fun fetchStudentsPage(pageToken: String? = null): AdminEmailApiResult<AdminStudentsPage> =
        withContext(Dispatchers.IO) {
            val tokenResult = tokenProvider()
            val idToken = when (tokenResult) {
                is AdminEmailApiResult.Success -> tokenResult.data
                is AdminEmailApiResult.Error -> return@withContext tokenResult
            }

            val parsedUrl = studentsUrl.toHttpUrlOrNull()
                ?: return@withContext AdminEmailApiResult.Error("Invalid students API URL configuration.")

            val targetUrl = if (!pageToken.isNullOrBlank()) {
                parsedUrl.newBuilder()
                    .addQueryParameter("pageToken", pageToken.trim())
                    .build()
            } else {
                parsedUrl
            }

            val request = Request.Builder()
                .url(targetUrl)
                .get()
                .header("Authorization", "Bearer $idToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            executeGet(request) { body -> parseStudentsPageJson(body) }
        }

    suspend fun fetchAllStudents(maxPages: Int = 20): AdminEmailApiResult<List<AdminRegisteredStudent>> =
        withContext(Dispatchers.IO) {
            val collected = linkedMapOf<String, AdminRegisteredStudent>()
            val seenTokens = mutableSetOf<String>()
            var currentPageToken: String? = null
            var pagesFetched = 0

            while (pagesFetched < maxPages) {
                pagesFetched++
                when (val pageResult = fetchStudentsPage(currentPageToken)) {
                    is AdminEmailApiResult.Error -> return@withContext pageResult
                    is AdminEmailApiResult.Success -> {
                        pageResult.data.students.forEach { student ->
                            collected[student.uid] = student
                        }
                        val nextToken = pageResult.data.nextPageToken?.trim()
                        if (nextToken.isNullOrBlank() || !seenTokens.add(nextToken)) {
                            break
                        }
                        currentPageToken = nextToken
                    }
                }
            }

            AdminEmailApiResult.Success(collected.values.toList())
        }

    suspend fun sendStudentEmail(
        studentUid: String,
        subject: String,
        message: String
    ): AdminEmailApiResult<AdminSendEmailResult> = withContext(Dispatchers.IO) {
        val cleanUid = studentUid.trim()
        val cleanSubject = subject.trim()
        val cleanMessage = message.trim()

        if (cleanUid.isBlank()) {
            return@withContext AdminEmailApiResult.Error("Please select a registered student first.")
        }
        if (cleanSubject.isBlank()) {
            return@withContext AdminEmailApiResult.Error("Please enter an email subject.")
        }
        if (cleanMessage.isBlank()) {
            return@withContext AdminEmailApiResult.Error("Please enter an email message.")
        }

        val tokenResult = tokenProvider()
        val idToken = when (tokenResult) {
            is AdminEmailApiResult.Success -> tokenResult.data
            is AdminEmailApiResult.Error -> return@withContext tokenResult
        }

        // Send ONLY studentUid, subject, message; backend resolves recipient email from Firebase Auth.
        val payloadJson = JSONObject().apply {
            put("studentUid", cleanUid)
            put("subject", cleanSubject)
            put("message", cleanMessage)
        }.toString()

        val request = Request.Builder()
            .url(sendEmailUrl)
            .post(payloadJson.toRequestBody(JSON_MEDIA_TYPE))
            .header("Authorization", "Bearer $idToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext mapHttpError(response.code, rawBody)
                }
                parseSendEmailJson(rawBody)
            }
        } catch (_: SocketTimeoutException) {
            AdminEmailApiResult.Error(
                message = "The request timed out before confirmation. Please check Email History before resending to avoid duplicate emails.",
                isNetworkError = true,
                isTimeoutUncertain = true
            )
        } catch (_: UnknownHostException) {
            AdminEmailApiResult.Error(
                message = "Unable to connect to the server. Please check your internet connection.",
                isNetworkError = true
            )
        } catch (_: ConnectException) {
            AdminEmailApiResult.Error(
                message = "Unable to connect to the server. Please try again shortly.",
                isNetworkError = true
            )
        } catch (_: SSLHandshakeException) {
            AdminEmailApiResult.Error(
                message = "Secure connection failed while contacting the email server.",
                isNetworkError = true
            )
        } catch (_: SSLException) {
            AdminEmailApiResult.Error(
                message = "Secure connection interrupted. Please check Email History before resending.",
                isNetworkError = true,
                isTimeoutUncertain = true
            )
        } catch (_: IOException) {
            AdminEmailApiResult.Error(
                message = "Network error occurred while sending email. Please check Email History before resending.",
                isNetworkError = true,
                isTimeoutUncertain = true
            )
        } catch (e: Exception) {
            AdminEmailApiResult.Error(
                message = redactSensitive(e.message?.takeIf { it.isNotBlank() } ?: "Unexpected error while sending email.")
            )
        }
    }

    suspend fun fetchEmailLogs(): AdminEmailApiResult<List<AdminEmailLogItem>> =
        withContext(Dispatchers.IO) {
            val tokenResult = tokenProvider()
            val idToken = when (tokenResult) {
                is AdminEmailApiResult.Success -> tokenResult.data
                is AdminEmailApiResult.Error -> return@withContext tokenResult
            }

            val request = Request.Builder()
                .url(emailLogsUrl)
                .get()
                .header("Authorization", "Bearer $idToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            executeGet(request) { body -> parseEmailLogsJson(body) }
        }

    private fun <T> executeGet(
        request: Request,
        parser: (String) -> AdminEmailApiResult<T>
    ): AdminEmailApiResult<T> {
        return try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return mapHttpError(response.code, rawBody)
                }
                parser(rawBody)
            }
        } catch (_: SocketTimeoutException) {
            AdminEmailApiResult.Error(
                message = "Server request timed out. Please try again.",
                isNetworkError = true
            )
        } catch (_: UnknownHostException) {
            AdminEmailApiResult.Error(
                message = "Unable to connect to the server. Please check your internet connection.",
                isNetworkError = true
            )
        } catch (_: ConnectException) {
            AdminEmailApiResult.Error(
                message = "Unable to connect to the server. Please try again.",
                isNetworkError = true
            )
        } catch (_: SSLException) {
            AdminEmailApiResult.Error(
                message = "Secure connection failed while contacting the server.",
                isNetworkError = true
            )
        } catch (_: IOException) {
            AdminEmailApiResult.Error(
                message = "Unable to connect to the server. Please try again.",
                isNetworkError = true
            )
        } catch (e: Exception) {
            AdminEmailApiResult.Error(
                message = redactSensitive(e.message?.takeIf { it.isNotBlank() } ?: "Unable to connect to the server.")
            )
        }
    }
}
