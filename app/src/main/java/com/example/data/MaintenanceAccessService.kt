package com.example.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class MaintenanceVerificationResult {
    data object Success : MaintenanceVerificationResult()
    data class InvalidKey(val message: String = "Invalid security key.") : MaintenanceVerificationResult()
    data class AccessDisabled(val message: String = "Developer access is disabled.") : MaintenanceVerificationResult()
    data class RateLimited(val message: String = "Too many failed attempts. Please try again later.") : MaintenanceVerificationResult()
    data class NetworkError(val message: String = "Unable to verify key. Please try again.") : MaintenanceVerificationResult()
}

/**
 * Service for securely verifying maintenance-mode developer access keys via Firebase Cloud Function / backend HTTPS,
 * with live server-verified Firestore resolution if Cloud Functions have not yet been deployed (HTTP 404).
 *
 * Security architecture:
 * 1. The security key and its hash are NEVER hardcoded in the Android app.
 * 2. Verification always requires a live server response (never offline cache or local fallback).
 * 3. Primary path calls the deployed HTTPS backend (`verifyMaintenanceAccessKey` / Vercel endpoint).
 * 4. Once `firestore.rules` locks `app_config/maintenance_access` (`allow read, write: if false;`),
 *    PERMISSION_DENIED ensures direct client reads are rejected and only the Cloud Function can verify.
 */
object MaintenanceAccessService {
    private const val TAG = "MaintenanceAccess"

    // Primary Firebase Cloud Function endpoints (Regional & US) + Companion Vercel backend
    private val VERIFICATION_ENDPOINTS = listOf(
        "https://asia-south1-kcet-app-c0457.cloudfunctions.net/verifyMaintenanceAccessKey",
        "https://us-central1-kcet-app-c0457.cloudfunctions.net/verifyMaintenanceAccessKey",
        "https://kcet-password-reset.vercel.app/api/admin/verify-maintenance-key"
    )

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    // Client-side rate limiting / anti-spam safeguard
    private var consecutiveFailures = 0
    private var localLockoutUntilMs = 0L

    fun getLocalLockoutRemainingSeconds(): Int {
        val now = System.currentTimeMillis()
        return if (localLockoutUntilMs > now) {
            (((localLockoutUntilMs - now) + 999L) / 1000L).toInt()
        } else {
            0
        }
    }

    suspend fun verifyMaintenanceKey(candidateKey: String): MaintenanceVerificationResult =
        withContext(Dispatchers.IO) {
            val trimmedKey = candidateKey.trim()
            if (trimmedKey.isEmpty()) {
                return@withContext MaintenanceVerificationResult.InvalidKey("Please enter the security key.")
            }

            // Client-side anti-hammering check
            val now = System.currentTimeMillis()
            if (localLockoutUntilMs > now) {
                val remainingSec = (((localLockoutUntilMs - now) + 999L) / 1000L).toInt()
                return@withContext MaintenanceVerificationResult.RateLimited(
                    "Too many failed attempts. Please try again in $remainingSec seconds."
                )
            }

            val jsonBody = JSONObject().apply {
                put("securityKey", trimmedKey)
            }.toString()

            var lastResult: MaintenanceVerificationResult? = null
            var hadUndeployed404 = false

            for (endpointUrl in VERIFICATION_ENDPOINTS) {
                try {
                    val request = Request.Builder()
                        .url(endpointUrl)
                        .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                        .header("Accept", "application/json")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        val code = response.code
                        val responseBodyString = response.body?.string().orEmpty()

                        val json = try {
                            JSONObject(responseBodyString)
                        } catch (_: Exception) {
                            JSONObject()
                        }

                        val isSuccess = json.optBoolean("success", false)
                        val reason = json.optString("reason", "")
                        val message = json.optString("message", "")

                        when {
                            code == 200 && isSuccess -> {
                                resetFailures()
                                return@withContext MaintenanceVerificationResult.Success
                            }
                            code == 401 || reason.equals("INVALID_KEY", ignoreCase = true) -> {
                                recordFailure()
                                return@withContext MaintenanceVerificationResult.InvalidKey(
                                    if (message.isNotBlank()) message else "Invalid security key."
                                )
                            }
                            code == 403 || (code == 404 && reason.equals("DISABLED", ignoreCase = true)) || reason.equals("DISABLED", ignoreCase = true) -> {
                                return@withContext MaintenanceVerificationResult.AccessDisabled(
                                    if (message.isNotBlank()) message else "Developer access is disabled."
                                )
                            }
                            code == 429 || reason.equals("RATE_LIMITED", ignoreCase = true) -> {
                                recordFailure()
                                return@withContext MaintenanceVerificationResult.RateLimited(
                                    if (message.isNotBlank()) message else "Too many failed attempts. Please try again later."
                                )
                            }
                            code == 404 -> {
                                // Endpoint route has not been deployed on this host yet; try next endpoint
                                hadUndeployed404 = true
                                lastResult = MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
                            }
                            else -> {
                                lastResult = MaintenanceVerificationResult.NetworkError(
                                    if (message.isNotBlank()) message else "Unable to verify key. Please try again."
                                )
                            }
                        }
                    }
                } catch (_: IOException) {
                    lastResult = MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
                } catch (_: Exception) {
                    lastResult = MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
                }
            }

            // Only if the device is online and the Cloud Function / serverless routes returned 404 (not yet deployed),
            // verify directly against the live Firebase Firestore server (Source.SERVER — never offline cache or hardcoded keys).
            if (hadUndeployed404) {
                return@withContext verifyAgainstLiveFirestoreServer(trimmedKey)
            }

            return@withContext lastResult ?: MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
        }

    private suspend fun verifyAgainstLiveFirestoreServer(trimmedKey: String): MaintenanceVerificationResult {
        val candidateHash = sha256Hex(trimmedKey)

        // 1. Check dedicated app_config/maintenance_access on live Firestore server (if rules allow & doc exists)
        try {
            val accessSnap = getDocumentFromServer("app_config", "maintenance_access")
            if (accessSnap.exists()) {
                val isEnabled = parseEnabledFlag(accessSnap, "enabled", defaultIfMissing = false)
                if (!isEnabled) {
                    return MaintenanceVerificationResult.AccessDisabled("Developer access is disabled.")
                }

                val storedHash = accessSnap.getString("keyHash")?.trim().orEmpty()
                val storedKey = accessSnap.getString("key")?.trim().orEmpty()
                val effectiveHash = when {
                    storedHash.isNotEmpty() -> storedHash
                    storedKey.isNotEmpty() -> sha256Hex(storedKey)
                    else -> ""
                }

                if (effectiveHash.isEmpty()) {
                    return MaintenanceVerificationResult.AccessDisabled("Developer access is disabled.")
                }

                return if (constantTimeEquals(candidateHash, effectiveHash)) {
                    resetFailures()
                    MaintenanceVerificationResult.Success
                } else {
                    recordFailure()
                    MaintenanceVerificationResult.InvalidKey("Invalid security key.")
                }
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                // Strict firestore.rules are deployed, so verification requires the Cloud Function to be deployed.
                return MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
            }
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE || e.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED) {
                return MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
            }
        } catch (_: Exception) {
            return MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
        }

        // 2. Fallback to existing live admin_config/security on Firestore server when app_config/maintenance_access is not yet created
        return try {
            val securitySnap = getDocumentFromServer("admin_config", "security")
            if (!securitySnap.exists()) {
                MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
            } else {
                val enabledOverride = when {
                    securitySnap.contains("developerAccessEnabled") -> parseEnabledFlag(securitySnap, "developerAccessEnabled", defaultIfMissing = true)
                    securitySnap.contains("enabled") -> parseEnabledFlag(securitySnap, "enabled", defaultIfMissing = true)
                    else -> true
                }
                if (!enabledOverride) {
                    MaintenanceVerificationResult.AccessDisabled("Developer access is disabled.")
                } else {
                    val storedHash = extractNonBlankString(securitySnap, "keyHash", "maintenanceKeyHash")
                    val storedPlain = extractNonBlankString(securitySnap, "securityKey", "security_key", "pin", "admin_pin")
                    val effectiveHash = when {
                        storedHash.isNotEmpty() -> storedHash
                        storedPlain.isNotEmpty() -> sha256Hex(storedPlain)
                        else -> ""
                    }

                    if (effectiveHash.isEmpty()) {
                        MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
                    } else if (constantTimeEquals(candidateHash, effectiveHash)) {
                        resetFailures()
                        MaintenanceVerificationResult.Success
                    } else {
                        recordFailure()
                        MaintenanceVerificationResult.InvalidKey("Invalid security key.")
                    }
                }
            }
        } catch (_: Exception) {
            MaintenanceVerificationResult.NetworkError("Unable to verify key. Please try again.")
        }
    }

    private suspend fun getDocumentFromServer(
        collection: String,
        document: String
    ): DocumentSnapshot = withTimeout(10_000L) {
        suspendCancellableCoroutine { cont ->
            FirebaseFirestore.getInstance()
                .collection(collection)
                .document(document)
                .get(Source.SERVER)
                .addOnSuccessListener { snap ->
                    if (cont.isActive) cont.resume(snap)
                }
                .addOnFailureListener { exc ->
                    if (cont.isActive) cont.resumeWithException(exc)
                }
        }
    }

    private fun extractNonBlankString(snapshot: DocumentSnapshot, vararg fields: String): String {
        for (field in fields) {
            val value = try {
                snapshot.get(field)?.toString()?.trim().orEmpty()
            } catch (_: Exception) {
                ""
            }
            if (value.isNotEmpty() && !value.equals("null", ignoreCase = true)) {
                return value
            }
        }
        return ""
    }

    private fun parseEnabledFlag(snapshot: DocumentSnapshot, fieldName: String, defaultIfMissing: Boolean): Boolean {
        val raw = try { snapshot.get(fieldName) } catch (_: Exception) { null } ?: return defaultIfMissing
        return when (raw) {
            is Boolean -> raw
            is Number -> raw.toInt() != 0
            is String -> raw.trim().equals("true", ignoreCase = true) || raw.trim() == "1"
            else -> defaultIfMissing
        }
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val bytesA = a.trim().lowercase().toByteArray(Charsets.UTF_8)
        val bytesB = b.trim().lowercase().toByteArray(Charsets.UTF_8)
        return bytesA.isNotEmpty() && MessageDigest.isEqual(bytesA, bytesB)
    }

    private fun resetFailures() {
        consecutiveFailures = 0
        localLockoutUntilMs = 0L
    }

    private fun recordFailure() {
        consecutiveFailures++
        if (consecutiveFailures >= 5) {
            // Apply 60-second client-side cooldown after 5 consecutive failures
            localLockoutUntilMs = System.currentTimeMillis() + 60_000L
            consecutiveFailures = 0
        }
    }
}
