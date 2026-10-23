package com.example.data

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseService {
    private const val TAG = "FirebaseService"

    // Static override registry with persistent SharedPreferences backing for student premium statuses
    private val premiumOverrides = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    private var appContext: android.content.Context? = null

    fun initContext(context: android.content.Context) {
        appContext = context.applicationContext
    }

    private fun getPremiumPrefs(): android.content.SharedPreferences? {
        return appContext?.getSharedPreferences("kcet_premium_overrides", android.content.Context.MODE_PRIVATE)
    }

    fun setPremiumOverride(identifier: String, isPremium: Boolean) {
        if (identifier.isBlank()) return
        val clean = normalizeIdentifier(identifier)
        val emailDocId = clean.replace(".", "_").replace("@", "_")
        premiumOverrides[clean] = isPremium
        premiumOverrides[identifier] = isPremium
        premiumOverrides[emailDocId] = isPremium
        if (clean.contains("@")) {
            premiumOverrides[clean.lowercase().trim()] = isPremium
        }
        val safeRaw = identifier.replace(".", "_").replace("@", "_")
        premiumOverrides[safeRaw] = isPremium

        try {
            getPremiumPrefs()?.edit()?.apply {
                putBoolean("prem_$clean", isPremium)
                putBoolean("prem_$identifier", isPremium)
                putBoolean("prem_$emailDocId", isPremium)
                putBoolean("prem_$safeRaw", isPremium)
                if (clean.contains("@")) {
                    putBoolean("prem_${clean.lowercase().trim()}", isPremium)
                }
                apply()
            }
        } catch (_: Exception) {}
    }

    fun getPremiumOverride(identifier: String): Boolean? {
        if (identifier.isBlank()) return null
        val clean = normalizeIdentifier(identifier)
        val emailDocId = clean.replace(".", "_").replace("@", "_")
        val safeRaw = identifier.replace(".", "_").replace("@", "_")

        val inMem = premiumOverrides[clean] ?: premiumOverrides[identifier] ?: premiumOverrides[emailDocId] ?: premiumOverrides[safeRaw] ?: if (clean.contains("@")) premiumOverrides[clean.lowercase().trim()] else null
        if (inMem != null) return inMem

        try {
            val prefs = getPremiumPrefs()
            if (prefs != null) {
                val keys = listOf("prem_$clean", "prem_$identifier", "prem_$emailDocId", "prem_$safeRaw")
                for (k in keys) {
                    if (prefs.contains(k)) {
                        val p = prefs.getBoolean(k, false)
                        premiumOverrides[clean] = p
                        premiumOverrides[identifier] = p
                        return p
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun extractBoolean(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): Boolean? {
        for (field in fieldNames) {
            val raw = doc.get(field) ?: continue
            val bool = when (raw) {
                is Boolean -> raw
                is Number -> raw.toInt() == 1
                is String -> when (raw.trim().lowercase()) {
                    "true", "1", "yes", "premium", "active" -> true
                    "false", "0", "no", "free", "inactive" -> false
                    else -> null
                }
                else -> null
            }
            if (bool != null) return bool
        }
        return null
    }

    fun extractBooleanFromRtdb(snapshot: com.google.firebase.database.DataSnapshot): Boolean? {
        val raw = snapshot.value ?: return null
        return when (raw) {
            is Boolean -> raw
            is Number -> raw.toInt() == 1
            is String -> when (raw.trim().lowercase()) {
                "true", "1", "yes", "premium", "active" -> true
                "false", "0", "no", "free", "inactive" -> false
                else -> null
            }
            is Map<*, *> -> {
                val p = raw["isPremium"] ?: raw["premium"] ?: raw["status"]
                when (p) {
                    is Boolean -> p
                    is Number -> p.toInt() == 1
                    is String -> p.trim().lowercase() in listOf("true", "1", "yes", "premium", "active")
                    else -> null
                }
            }
            else -> null
        }
    }

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val realtimeDb: com.google.firebase.database.FirebaseDatabase by lazy {
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance(FirebaseRealtimeService.RTDB_URL)
        } catch (e: Exception) {
            com.google.firebase.database.FirebaseDatabase.getInstance()
        }
    }
    val firestore: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            instance.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w(TAG, "Firestore settings cache config info: ${e.message}")
        }
        instance
    }

    // Helper for async Firebase Tasks
    suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { continuation ->
            try {
                if (isComplete) {
                    if (isSuccessful) {
                        continuation.resume(result)
                    } else {
                        continuation.resumeWithException(exception ?: Exception("Unknown Firebase Task exception"))
                    }
                    return@suspendCancellableCoroutine
                }
                
                addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
                addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
        }

    suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> =
        try {
            Result.success(this.awaitTask())
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getUserAccountRecord(identifier: String): Map<String, String>? {
        return try {
            val cleanId = normalizeIdentifier(identifier)
            val docSnap = firestore.collection("user_accounts").document(cleanId).get().awaitResult().getOrNull()
            if (docSnap != null && docSnap.exists()) {
                mapOf(
                    "password" to (docSnap.getString("password") ?: ""),
                    "name" to (docSnap.getString("name") ?: ""),
                    "email" to (docSnap.getString("email") ?: ""),
                    "phone" to (docSnap.getString("phone") ?: ""),
                    "authEmail" to (docSnap.getString("authEmail") ?: docSnap.getString("email") ?: ""),
                    "uid" to (docSnap.getString("uid") ?: ""),
                    "emailVerified" to (docSnap.getBoolean("emailVerified")?.toString() ?: "false")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isValidIndianPhoneNumber(phone: String): Boolean {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        val digitsOnly = when {
            clean.startsWith("+91") -> clean.substring(3)
            clean.startsWith("91") && clean.length == 12 -> clean.substring(2)
            clean.startsWith("0") && clean.length == 11 -> clean.substring(1)
            else -> clean
        }
        return digitsOnly.length == 10 && digitsOnly.all { it.isDigit() } &&
                (digitsOnly.startsWith("6") || digitsOnly.startsWith("7") || digitsOnly.startsWith("8") || digitsOnly.startsWith("9"))
    }

    fun normalizeIndianPhoneNumber(phone: String): String {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        val digitsOnly = when {
            clean.startsWith("+91") -> clean.substring(3)
            clean.startsWith("91") && clean.length == 12 -> clean.substring(2)
            clean.startsWith("0") && clean.length == 11 -> clean.substring(1)
            else -> clean
        }
        return if (digitsOnly.length == 10) digitsOnly else clean
    }

    /**
     * Sends SMS verification code to registered mobile phone using Firebase Authentication SDK.
     */
    fun sendFirebaseSmsVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String, resendingToken: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (credential: PhoneAuthCredential) -> Unit,
        onVerificationFailed: (e: FirebaseException) -> Unit
    ) {
        val cleanDigits = normalizeIndianPhoneNumber(phoneNumber)
        val formattedPhone = if (cleanDigits.startsWith("+")) cleanDigits else "+91$cleanDigits"

        Log.d(TAG, "Initiating Firebase Auth SMS verification for: $formattedPhone")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "Firebase Auth SMS auto-verification completed for $formattedPhone")
                    onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "Firebase Auth SMS verification failed: ${e.message}", e)
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "Firebase Auth SMS code sent successfully! VerificationID: $verificationId")
                    onCodeSent(verificationId, token)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun getPhoneAuthCredential(verificationId: String, userCode: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, userCode)
    }

    fun normalizeIdentifier(identifier: String): String {
        val trimmed = identifier.lowercase().trim()
        if (trimmed.contains("@")) return trimmed
        return normalizeIndianPhoneNumber(trimmed)
    }

    // -------------------------------------------------------------
    // 1. USERS COLLECTION
    // -------------------------------------------------------------
    suspend fun saveUserRecord(
        uid: String,
        email: String?,
        phone: String?,
        password: String,
        name: String,
        signInMethod: String = "Email & Password",
        emailVerified: Boolean = false
    ): Boolean {
        val cleanEmail = email?.lowercase()?.trim() ?: ""
        val cleanPhone = phone?.trim()?.replace(" ", "")?.replace("-", "") ?: ""

        // Automatically clear deleted student marker when saving or re-registering account
        try {
            if (cleanEmail.isNotBlank()) clearDeletedStudent(cleanEmail, uid)
            if (cleanPhone.isNotBlank()) clearDeletedStudent(cleanPhone, uid)
            if (uid.isNotBlank()) clearDeletedStudent("", uid)
        } catch (_: Exception) {}

        val derivedAuthEmail = if (cleanEmail.contains("@")) {
            cleanEmail
        } else if (cleanPhone.isNotBlank()) {
            "phone_${cleanPhone.replace("+", "")}@kcetapp.com"
        } else {
            "user_${System.currentTimeMillis()}@kcetapp.com"
        }

        val userMap = mutableMapOf<String, Any>(
            "uid" to uid,
            "name" to name,
            "email" to cleanEmail,
            "phone" to cleanPhone,
            "authEmail" to derivedAuthEmail,
            "password" to password.trim(),
            "isPremium" to false,
            "kcetTargetRank" to "Under 1000",
            "targetStream" to "Engineering (B.E / B.Tech)",
            "registrationDate" to "2026",
            "signInMethod" to signInMethod,
            "emailVerified" to emailVerified
        )

        val res = firestore.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

        // Index by email if provided
        if (cleanEmail.isNotBlank()) {
            firestore.collection("user_accounts").document(cleanEmail).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
        }

        // Index by phone if provided
        if (cleanPhone.isNotBlank()) {
            firestore.collection("user_accounts").document(cleanPhone).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            // If phone has 10 digits without +91, also index with +91 and vice versa for easy login
            if (cleanPhone.length == 10) {
                firestore.collection("user_accounts").document("+91$cleanPhone").set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            } else if (cleanPhone.startsWith("+91") && cleanPhone.length == 13) {
                firestore.collection("user_accounts").document(cleanPhone.substring(3)).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
        }

        return res.isSuccess
    }

    suspend fun updateUserEmailVerified(uid: String, identifier: String) {
        try {
            val cleanId = normalizeIdentifier(identifier)
            val updateMap = mapOf("emailVerified" to true)
            if (uid.isNotBlank()) {
                firestore.collection("users").document(uid).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            if (cleanId.isNotBlank()) {
                firestore.collection("user_accounts").document(cleanId).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating emailVerified in firestore: ${e.message}")
        }
    }

    suspend fun sendUserVerificationEmail(user: com.google.firebase.auth.FirebaseUser): Result<Unit> =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            user.sendEmailVerification()
                .addOnSuccessListener {
                    Log.d(TAG, "sendEmailVerification successfully dispatched for ${user.email}")
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exc ->
                    Log.e(TAG, "sendEmailVerification failure for ${user.email}: ${exc.message}", exc)
                    if (continuation.isActive) continuation.resume(Result.failure(exc))
                }
        }

    suspend fun reloadAndCheckEmailVerified(user: com.google.firebase.auth.FirebaseUser): Boolean =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            user.reload()
                .addOnCompleteListener { _ ->
                    val refreshedUser = auth.currentUser ?: user
                    val isVerified = refreshedUser.isEmailVerified
                    Log.d(TAG, "reloadAndCheckEmailVerified for ${refreshedUser.email}: isVerified=$isVerified")
                    if (continuation.isActive) {
                        continuation.resume(isVerified)
                    }
                }
        }

    suspend fun resendEmailVerification(email: String, password: String): Result<Unit> {
        return try {
            val cleanEmail = email.lowercase().trim()
            val signInRes = auth.signInWithEmailAndPassword(cleanEmail, password.trim()).awaitResult()
            if (signInRes.isFailure) {
                return Result.failure(signInRes.exceptionOrNull() ?: Exception("Unable to authenticate with provided credentials"))
            }
            val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val sendRes = sendUserVerificationEmail(currentUser)
            // Sign out to maintain security until verified
            auth.signOut()
            sendRes
        } catch (e: Exception) {
            try { auth.signOut() } catch (_: Exception) {}
            Result.failure(e)
        }
    }

    suspend fun sendVerificationEmailToAddress(email: String, explicitPassword: String? = null): Result<String> {
        return try {
            val cleanEmail = email.lowercase().trim()
            val current = auth.currentUser
            if (current != null && current.email?.equals(cleanEmail, ignoreCase = true) == true) {
                val sendRes = sendUserVerificationEmail(current)
                return if (sendRes.isSuccess) {
                    Result.success("Verification email successfully sent to $cleanEmail!")
                } else {
                    Result.failure(sendRes.exceptionOrNull() ?: Exception("Failed to send verification email to $cleanEmail"))
                }
            }

            // Look up existing credentials or password
            val record = getUserAccountRecord(cleanEmail)
            val storedPassword = record?.get("password")?.ifBlank { null }
            val candidatePasswords = listOfNotNull(
                explicitPassword?.ifBlank { null },
                storedPassword
            ).distinct()

            var signedIn = false
            for (pwd in candidatePasswords) {
                val res = auth.signInWithEmailAndPassword(cleanEmail, pwd).awaitResult()
                if (res.isSuccess) {
                    signedIn = true
                    break
                }
            }

            if (!signedIn && !explicitPassword.isNullOrBlank()) {
                // Try creating user in Firebase Auth so we can send verification email
                val createRes = auth.createUserWithEmailAndPassword(cleanEmail, explicitPassword.trim()).awaitResult()
                if (createRes.isSuccess) {
                    signedIn = true
                    val newUid = createRes.getOrNull()?.user?.uid ?: "std_${System.currentTimeMillis()}"
                    val name = record?.get("name")?.ifBlank { null } ?: "KCET Student"
                    saveUserRecord(
                        uid = newUid,
                        email = cleanEmail,
                        phone = record?.get("phone") ?: "",
                        password = explicitPassword.trim(),
                        name = name,
                        signInMethod = "Email & Password",
                        emailVerified = false
                    )
                }
            }

            val user = auth.currentUser
            if (user != null) {
                val sendRes = sendUserVerificationEmail(user)
                auth.signOut()
                if (sendRes.isSuccess) {
                    Result.success("Verification email successfully sent to $cleanEmail! Please check your inbox and spam folder.")
                } else {
                    Result.failure(sendRes.exceptionOrNull() ?: Exception("Failed to send verification email"))
                }
            } else {
                Result.failure(Exception("Unable to authenticate $cleanEmail to send verification link."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendVerificationEmailToAddress error for $email: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserPasswordInFirestore(identifier: String, newPass: String) {
        try {
            val cleanId = normalizeIdentifier(identifier)
            val updateMap = mapOf(
                "password" to newPass.trim(),
                "passwordUpdatedAt" to System.currentTimeMillis()
            )

            // Fetch record to get all linked identifiers
            val record = getUserAccountRecord(cleanId)

            firestore.collection("user_accounts").document(cleanId).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

            if (record != null) {
                val email = record["email"] ?: ""
                val phone = record["phone"] ?: ""
                val uid = record["uid"] ?: ""
                if (email.isNotBlank()) {
                    firestore.collection("user_accounts").document(email.lowercase().trim()).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    firestore.collection("users").document(email.lowercase().trim()).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                }
                if (phone.isNotBlank()) {
                    val normPhone = normalizeIndianPhoneNumber(phone)
                    firestore.collection("user_accounts").document(normPhone).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    firestore.collection("user_accounts").document("+91$normPhone").set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                }
                if (uid.isNotBlank()) {
                    firestore.collection("users").document(uid).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                }
            }

            val userUid = auth.currentUser?.uid
            if (!userUid.isNullOrBlank()) {
                firestore.collection("users").document(userUid).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password in firestore: ${e.message}")
        }
    }

    private fun extractSignInMethod(doc: com.google.firebase.firestore.DocumentSnapshot): String {
        val saved = doc.getString("signInMethod")
        if (!saved.isNullOrBlank()) return saved
        val photoUrl = doc.getString("photoUrl") ?: ""
        val email = doc.getString("email") ?: ""
        val phone = doc.getString("phone") ?: ""
        val uid = doc.getString("uid") ?: doc.id
        return when {
            photoUrl.contains("googleusercontent") || uid.startsWith("google_") -> "Google Sign-In"
            phone.isNotBlank() || email.startsWith("phone_") -> "Phone Number"
            email.contains("@") -> "Email & Password"
            else -> "Email & Password"
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = profile.uid.ifBlank { auth.currentUser?.uid ?: "guest" }
        setPremiumOverride(uid, profile.isPremium)
        if (profile.email.isNotBlank()) {
            setPremiumOverride(profile.email, profile.isPremium)
        }
        val cleanEmail = profile.email.lowercase().trim()
        try {
            if (cleanEmail.isNotBlank()) clearDeletedStudent(cleanEmail, uid)
            if (uid.isNotBlank()) clearDeletedStudent("", uid)
        } catch (_: Exception) {}
        val formattedId = profile.formattedStudentId
        val now = if (profile.lastUpdatedMillis > 0L) profile.lastUpdatedMillis else System.currentTimeMillis()
        val userMap = mapOf(
            "uid" to uid,
            "name" to profile.name,
            "email" to cleanEmail,
            "photoUrl" to (profile.photoUrl ?: ""),
            "isPremium" to profile.isPremium,
            "kcetTargetRank" to profile.kcetTargetRank,
            "targetStream" to profile.targetStream,
            "registrationDate" to profile.registrationDate,
            "isForceLoggedOut" to profile.isForceLoggedOut,
            "isBlocked" to profile.isBlocked,
            "blockReason" to profile.blockReason,
            "signInMethod" to profile.signInMethod,
            "studentId" to formattedId,
            "lastUpdatedMillis" to now
        )
        val res = firestore.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

        if (cleanEmail.isNotBlank()) {
            val emailDocId = cleanEmail.replace(".", "_").replace("@", "_")
            if (emailDocId != uid) {
                firestore.collection("users").document(emailDocId).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            firestore.collection("user_accounts").document(cleanEmail).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
        }
        return res.isSuccess
    }

    suspend fun syncStudentProfileAcrossCollections(profile: UserProfile) {
        try {
            val uid = profile.uid.trim()
            val email = profile.email.lowercase().trim()
            if (uid.isBlank() && email.isBlank()) return

            // 1. Update test_results
            val testResultsQuery = firestore.collection("test_results").get().awaitResult()
            if (testResultsQuery.isSuccess) {
                val docs = testResultsQuery.getOrNull()?.documents ?: emptyList()
                for (doc in docs) {
                    val dUserId = doc.getString("userId") ?: ""
                    val dEmail = (doc.getString("userEmail") ?: "").lowercase().trim()
                    if ((uid.isNotBlank() && dUserId == uid) || (email.isNotBlank() && dEmail == email)) {
                        doc.reference.update(
                            mapOf(
                                "userName" to profile.name,
                                "studentId" to profile.formattedStudentId
                            )
                        ).awaitResult()
                    }
                }
            }

            // 2. Update senior_chat_bookings
            val bookingsQuery = firestore.collection("senior_chat_bookings").get().awaitResult()
            if (bookingsQuery.isSuccess) {
                val docs = bookingsQuery.getOrNull()?.documents ?: emptyList()
                for (doc in docs) {
                    val dUserId = doc.getString("userId") ?: ""
                    val dEmail = (doc.getString("userEmail") ?: "").lowercase().trim()
                    if ((uid.isNotBlank() && dUserId == uid) || (email.isNotBlank() && dEmail == email)) {
                        doc.reference.update("userName", profile.name).awaitResult()
                    }
                }
            }

            // 3. Update Realtime DB Leaderboard and Presence
            FirebaseRealtimeService.updateStudentProfile(profile)
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing student profile across collections: ${e.message}")
        }
    }

    suspend fun getUserProfileOnce(identifier: String): UserProfile? {
        if (identifier.isBlank()) return null
        return try {
            val cleanId = normalizeIdentifier(identifier)
            val emailDocId = cleanId.replace(".", "_").replace("@", "_")

            val docs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()
            val doc1 = firestore.collection("users").document(identifier).get().awaitResult().getOrNull()
            if (doc1 != null && doc1.exists()) docs.add(doc1)

            if (emailDocId != identifier) {
                val doc2 = firestore.collection("users").document(emailDocId).get().awaitResult().getOrNull()
                if (doc2 != null && doc2.exists()) docs.add(doc2)
            }

            if (cleanId.contains("@")) {
                val querySnap = firestore.collection("users").whereEqualTo("email", cleanId).get().awaitResult().getOrNull()
                querySnap?.documents?.filter { it.exists() }?.let { docs.addAll(it) }

                val accDoc = firestore.collection("user_accounts").document(cleanId).get().awaitResult().getOrNull()
                if (accDoc != null && accDoc.exists()) docs.add(accDoc)
            }

            if (docs.isNotEmpty()) {
                val doc = docs.maxByOrNull { it.getLong("lastUpdatedMillis") ?: 0L } ?: docs.first()
                val anyDocPremium = docs.any { extractBoolean(it, "isPremium", "premium", "hasPremium") == true }
                val override = getPremiumOverride(identifier)
                    ?: (if (cleanId.isNotBlank()) getPremiumOverride(cleanId) else null)
                    ?: (if (emailDocId.isNotBlank()) getPremiumOverride(emailDocId) else null)
                    ?: docs.firstNotNullOfOrNull { getPremiumOverride(it.id) }
                val isAnyPremium = when {
                    override != null -> override
                    anyDocPremium -> true
                    else -> false
                }

                if (isAnyPremium) {
                    setPremiumOverride(identifier, true)
                    if (cleanId.isNotBlank()) setPremiumOverride(cleanId, true)
                    if (emailDocId.isNotBlank()) setPremiumOverride(emailDocId, true)
                }

                val isAnyBlocked = docs.any { it.getBoolean("isBlocked") == true }
                val blockReason = docs.firstOrNull { !(it.getString("blockReason").isNullOrBlank()) }?.getString("blockReason") ?: ""
                val foundStudentId = docs.firstOrNull { !it.getString("studentId").isNullOrBlank() }?.getString("studentId") ?: ""

                UserProfile(
                    uid = doc.getString("uid") ?: identifier,
                    name = doc.getString("name") ?: "Student",
                    email = doc.getString("email") ?: "",
                    photoUrl = doc.getString("photoUrl"),
                    isPremium = isAnyPremium,
                    kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000",
                    targetStream = doc.getString("targetStream") ?: "Engineering (B.E / B.Tech)",
                    registrationDate = doc.getString("registrationDate") ?: "2026",
                    isForceLoggedOut = docs.any { it.getBoolean("isForceLoggedOut") == true },
                    isBlocked = isAnyBlocked,
                    blockReason = blockReason,
                    signInMethod = extractSignInMethod(doc),
                    studentId = foundStudentId,
                    lastUpdatedMillis = doc.getLong("lastUpdatedMillis") ?: System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserProfile(uid: String, email: String = ""): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank() && email.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val cleanEmail = email.lowercase().trim()
        val emailDocId = cleanEmail.replace(".", "_").replace("@", "_")
        val cleanUid = uid.trim()

        var currentProfile: UserProfile? = null

        fun emitUpdatedProfile(incoming: UserProfile?) {
            if (incoming != null) {
                currentProfile = incoming
                setPremiumOverride(incoming.uid, incoming.isPremium)
                if (incoming.email.isNotBlank()) setPremiumOverride(incoming.email, incoming.isPremium)
                trySend(incoming)
            }
        }

        fun parseDoc(snapshot: com.google.firebase.firestore.DocumentSnapshot, fallbackId: String): UserProfile {
            val sUid = snapshot.getString("uid")?.takeIf { it.isNotBlank() } ?: fallbackId
            val sEmail = (snapshot.getString("email") ?: cleanEmail).lowercase().trim()
            val parsedPrem = extractBoolean(snapshot, "isPremium", "premium", "hasPremium")
            val override = getPremiumOverride(sUid)
                ?: (if (sEmail.isNotBlank()) getPremiumOverride(sEmail) else null)
                ?: (if (emailDocId.isNotBlank()) getPremiumOverride(emailDocId) else null)
                ?: (if (cleanUid.isNotBlank()) getPremiumOverride(cleanUid) else null)
            val isPrem = when {
                override != null -> override
                parsedPrem == true -> true
                else -> false
            }

            if (isPrem) {
                setPremiumOverride(sUid, true)
                if (sEmail.isNotBlank()) setPremiumOverride(sEmail, true)
            }

            return UserProfile(
                uid = sUid,
                name = snapshot.getString("name") ?: "Karthik Gowda",
                email = sEmail,
                photoUrl = snapshot.getString("photoUrl"),
                isPremium = isPrem,
                kcetTargetRank = snapshot.getString("kcetTargetRank") ?: "Under 1000",
                targetStream = snapshot.getString("targetStream") ?: "Engineering (B.E / B.Tech)",
                registrationDate = snapshot.getString("registrationDate") ?: "2026",
                isForceLoggedOut = snapshot.getBoolean("isForceLoggedOut") ?: false,
                isBlocked = snapshot.getBoolean("isBlocked") ?: false,
                blockReason = snapshot.getString("blockReason") ?: "",
                signInMethod = extractSignInMethod(snapshot),
                studentId = snapshot.getString("studentId") ?: "",
                lastUpdatedMillis = snapshot.getLong("lastUpdatedMillis") ?: System.currentTimeMillis()
            )
        }

        val listenerUid = if (cleanUid.isNotBlank()) {
            firestore.collection("users").document(cleanUid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val profile = parseDoc(snapshot, cleanUid)
                        emitUpdatedProfile(profile)
                    }
                }
        } else null

        val listenerEmail = if (emailDocId.isNotBlank() && emailDocId != cleanUid) {
            firestore.collection("users").document(emailDocId)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val profile = parseDoc(snapshot, cleanUid.ifBlank { emailDocId })
                        emitUpdatedProfile(profile)
                    }
                }
        } else null

        val listenerAccount = if (cleanEmail.isNotBlank()) {
            firestore.collection("user_accounts").document(cleanEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val profile = parseDoc(snapshot, cleanUid.ifBlank { cleanEmail })
                        emitUpdatedProfile(profile)
                    }
                }
        } else null

        val listenerQueryEmail = if (cleanEmail.isNotBlank()) {
            firestore.collection("users").whereEqualTo("email", cleanEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val doc = snapshot.documents.maxByOrNull { it.getLong("lastUpdatedMillis") ?: 0L } ?: snapshot.documents.first()
                        val profile = parseDoc(doc, cleanUid.ifBlank { cleanEmail })
                        emitUpdatedProfile(profile)
                    }
                }
        } else null

        // Realtime Database listeners for sub-50ms instant premium push!
        val rtdbListeners = mutableListOf<Pair<com.google.firebase.database.DatabaseReference, com.google.firebase.database.ValueEventListener>>()
        val rtdbKeys = mutableSetOf<String>()
        if (cleanUid.isNotBlank()) rtdbKeys.add(cleanUid.replace(".", "_").replace("@", "_"))
        if (cleanEmail.isNotBlank()) {
            rtdbKeys.add(cleanEmail.replace(".", "_").replace("@", "_"))
            rtdbKeys.add(emailDocId)
        }

        rtdbKeys.forEach { key ->
            val ref = realtimeDb.getReference("user_premium").child(key)
            val listener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val isPrem = extractBooleanFromRtdb(snapshot)
                    if (isPrem != null) {
                        setPremiumOverride(key, isPrem)
                        if (cleanUid.isNotBlank()) setPremiumOverride(cleanUid, isPrem)
                        if (cleanEmail.isNotBlank()) setPremiumOverride(cleanEmail, isPrem)
                        if (currentProfile != null) {
                            emitUpdatedProfile(currentProfile?.copy(isPremium = isPrem))
                        }
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            rtdbListeners.add(Pair(ref, listener))
        }

        awaitClose {
            listenerUid?.remove()
            listenerEmail?.remove()
            listenerAccount?.remove()
            listenerQueryEmail?.remove()
            rtdbListeners.forEach { (ref, l) -> ref.removeEventListener(l) }
        }
    }

    fun areSameStudent(a: UserProfile, b: UserProfile): Boolean {
        val aEmail = a.email.trim().lowercase()
        val bEmail = b.email.trim().lowercase()
        if (aEmail.isNotBlank() && bEmail.isNotBlank() && aEmail.contains("@") && bEmail.contains("@") && aEmail == bEmail) {
            return true
        }

        val aUid = a.uid.trim()
        val bUid = b.uid.trim()
        val aIsSyntheticUid = aUid.isBlank() || aUid.startsWith("test_") || aUid.startsWith("receipt_") || aUid == "guest" || aUid == "student" || aUid == "null"
        val bIsSyntheticUid = bUid.isBlank() || bUid.startsWith("test_") || bUid.startsWith("receipt_") || bUid == "guest" || bUid == "student" || bUid == "null"
        if (!aIsSyntheticUid && !bIsSyntheticUid && aUid.equals(bUid, ignoreCase = true)) {
            return true
        }

        val aStudentId = a.formattedStudentId.trim().lowercase()
        val bStudentId = b.formattedStudentId.trim().lowercase()
        if (aStudentId.isNotBlank() && bStudentId.isNotBlank() && aStudentId == bStudentId) {
            return true
        }

        val aName = a.name.trim().lowercase()
        val bName = b.name.trim().lowercase()
        val isGenericA = aName.isBlank() || aName == "student" || aName == "guest" || aName == "guest student" || aName == "kcet aspirant" || aName == "user"
        val isGenericB = bName.isBlank() || bName == "student" || bName == "guest" || bName == "guest student" || bName == "kcet aspirant" || bName == "user"
        if (!isGenericA && !isGenericB && aName == bName) {
            return true
        }

        // Sanitized email matching UID (e.g. UID="karthik_kcet2026_gmail_com" and Email="karthik.kcet2026@gmail.com")
        val aSanitizedEmail = aEmail.replace(".", "_").replace("@", "_")
        val bSanitizedEmail = bEmail.replace(".", "_").replace("@", "_")
        if (aEmail.isNotBlank() && aSanitizedEmail.isNotBlank() && aSanitizedEmail.equals(bUid, ignoreCase = true)) return true
        if (bEmail.isNotBlank() && bSanitizedEmail.isNotBlank() && bSanitizedEmail.equals(aUid, ignoreCase = true)) return true

        return false
    }

    fun deduplicateStudentProfiles(students: List<UserProfile>): List<UserProfile> {
        val clusters = mutableListOf<MutableList<UserProfile>>()

        for (student in students) {
            val matchingCluster = clusters.firstOrNull { cluster ->
                cluster.any { existing -> areSameStudent(existing, student) }
            }

            if (matchingCluster != null) {
                matchingCluster.add(student)
            } else {
                clusters.add(mutableListOf(student))
            }
        }

        return clusters.map { cluster ->
            val bestProfile = cluster.maxWithOrNull(
                compareBy<UserProfile> { it.email.isNotBlank() && it.email.contains("@") }
                    .thenBy { it.uid.isNotBlank() && !it.uid.startsWith("test_") && !it.uid.startsWith("receipt_") && !it.uid.startsWith("user_") }
                    .thenBy { it.studentId.isNotBlank() || it.formattedStudentId.isNotBlank() }
                    .thenBy { it.lastUpdatedMillis }
            ) ?: cluster.first()

            val canonicalName = cluster.firstOrNull { it.name.isNotBlank() && !it.name.equals("Student", ignoreCase = true) && !it.name.equals("Guest", ignoreCase = true) }?.name ?: bestProfile.name
            val canonicalEmail = cluster.firstOrNull { it.email.isNotBlank() && it.email.contains("@") }?.email ?: bestProfile.email
            val canonicalStudentId = cluster.firstOrNull { it.studentId.isNotBlank() }?.studentId
                ?: cluster.firstOrNull { it.formattedStudentId.isNotBlank() }?.formattedStudentId
                ?: bestProfile.studentId
            val canonicalPhoto = cluster.firstOrNull { !it.photoUrl.isNullOrBlank() }?.photoUrl ?: bestProfile.photoUrl
            val canonicalStream = cluster.firstOrNull { it.targetStream.isNotBlank() && it.targetStream != "Engineering (B.E / B.Tech)" }?.targetStream ?: bestProfile.targetStream
            val canonicalRank = cluster.firstOrNull { it.kcetTargetRank.isNotBlank() && it.kcetTargetRank != "Under 1000" }?.kcetTargetRank ?: bestProfile.kcetTargetRank

            val explicitOverride = cluster.firstNotNullOfOrNull { s ->
                getPremiumOverride(s.uid)
                    ?: (if (s.email.isNotBlank()) getPremiumOverride(s.email) else null)
                    ?: (if (s.formattedStudentId.isNotBlank()) getPremiumOverride(s.formattedStudentId) else null)
            }

            val isPremium = when (explicitOverride) {
                true -> true
                false -> false
                null -> cluster.any { it.isPremium }
            }

            val isBlocked = cluster.any { it.isBlocked }
            val isForceLoggedOut = cluster.any { it.isForceLoggedOut }
            val blockReason = cluster.firstOrNull { it.blockReason.isNotBlank() }?.blockReason ?: bestProfile.blockReason
            val maxLastUpdated = cluster.maxOfOrNull { it.lastUpdatedMillis } ?: bestProfile.lastUpdatedMillis

            val canonicalPassword = cluster.firstOrNull { it.password.isNotBlank() }?.password ?: bestProfile.password

            bestProfile.copy(
                name = canonicalName,
                email = canonicalEmail,
                studentId = canonicalStudentId,
                photoUrl = canonicalPhoto,
                targetStream = canonicalStream,
                kcetTargetRank = canonicalRank,
                isPremium = isPremium,
                isBlocked = isBlocked,
                isForceLoggedOut = isForceLoggedOut,
                blockReason = blockReason,
                lastUpdatedMillis = maxLastUpdated,
                password = canonicalPassword
            )
        }
    }

    fun getStudentDeduplicationKey(student: UserProfile): String {
        val cleanEmail = student.email.lowercase().trim()
        if (cleanEmail.isNotBlank() && cleanEmail.contains("@")) {
            return "email:$cleanEmail"
        }
        val cleanName = student.name.lowercase().trim()
        if (cleanName.isNotBlank() && cleanName != "student" && cleanName != "guest" && cleanName != "guest student" && cleanName != "kcet aspirant") {
            return "name:$cleanName"
        }
        val phoneDigits = student.email.filter { it.isDigit() }
        if (phoneDigits.length >= 10) {
            return "phone:${phoneDigits.takeLast(10)}"
        }
        val sId = student.studentId.trim()
        if (sId.isNotBlank() && sId.startsWith("KCET-")) {
            return "student_id:${sId.lowercase()}"
        }
        val uid = student.uid.trim()
        if (uid.isNotBlank() && uid != "guest" && uid != "student" && uid != "null") {
            return "uid:${uid.lowercase()}"
        }
        return "composite:${student.name.lowercase().trim()}_${student.formattedStudentId}"
    }

    private fun docToUserProfile(doc: com.google.firebase.firestore.DocumentSnapshot): UserProfile? {
        try {
            val uid = doc.getString("uid")?.takeIf { it.isNotBlank() } ?: doc.id
            val phone = doc.getString("phone") ?: doc.getString("phoneNumber") ?: ""
            var email = doc.getString("email")?.takeIf { it.isNotBlank() }
                ?: doc.getString("userEmail")?.takeIf { it.isNotBlank() }
                ?: doc.getString("authEmail")?.takeIf { it.isNotBlank() }
                ?: ""
            if (email.isBlank() && phone.isNotBlank()) {
                email = phone
            }
            val name = doc.getString("name")?.takeIf { it.isNotBlank() }
                ?: doc.getString("studentName")?.takeIf { it.isNotBlank() }
                ?: doc.getString("userName")?.takeIf { it.isNotBlank() }
                ?: doc.getString("displayName")?.takeIf { it.isNotBlank() }
                ?: if (phone.isNotBlank()) "Student ($phone)" else if (email.isNotBlank() && email.contains("@")) email.substringBefore("@").replace(".", " ") else "Student"
            val photoUrl = doc.getString("photoUrl")
            val parsedPrem = extractBoolean(doc, "isPremium", "premium", "hasPremium")
            val override = getPremiumOverride(uid)
                ?: (if (email.isNotBlank()) getPremiumOverride(email) else null)
                ?: getPremiumOverride(doc.id)
            val isPremium = when {
                override != null -> override
                parsedPrem == true -> true
                else -> false
            }
            if (isPremium) {
                setPremiumOverride(uid, true)
                if (email.isNotBlank()) setPremiumOverride(email, true)
            }
            val kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000"
            val targetStream = doc.getString("targetStream") ?: "Engineering (B.E / B.Tech)"
            val registrationDate = doc.getString("registrationDate") ?: "2026"
            val isForceLoggedOut = doc.getBoolean("isForceLoggedOut") ?: false
            val isBlocked = doc.getBoolean("isBlocked") ?: false
            val blockReason = doc.getString("blockReason") ?: ""
            val signInMethod = extractSignInMethod(doc)
            val studentId = doc.getString("studentId") ?: doc.getString("formattedStudentId") ?: ""
            val lastUpdatedMillis = doc.getLong("lastUpdatedMillis") ?: 0L
            val password = doc.getString("password") ?: doc.getString("userPassword") ?: ""

            return UserProfile(
                uid = uid,
                name = name,
                email = email,
                photoUrl = photoUrl,
                isPremium = isPremium,
                kcetTargetRank = kcetTargetRank,
                targetStream = targetStream,
                registrationDate = registrationDate,
                isForceLoggedOut = isForceLoggedOut,
                isBlocked = isBlocked,
                blockReason = blockReason,
                signInMethod = signInMethod,
                studentId = studentId,
                lastUpdatedMillis = lastUpdatedMillis,
                password = password
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun observeAllUserProfiles(): Flow<List<UserProfile>> = callbackFlow {
        var usersList = emptyList<UserProfile>()
        var userAccountsList = emptyList<UserProfile>()
        var premiumSet = setOf<String>()
        var deletedSet = setOf<String>()

        fun emitCombined() {
            val allRaw = mutableListOf<UserProfile>()
            allRaw.addAll(usersList)
            allRaw.addAll(userAccountsList)

            // Always ensure all benchmark default students are included if not present
            SampleData.defaultStudents.forEach { defaultStd ->
                val exists = allRaw.any { s ->
                    (s.email.isNotBlank() && defaultStd.email.isNotBlank() && s.email.equals(defaultStd.email, ignoreCase = true)) ||
                    (s.uid.isNotBlank() && defaultStd.uid.isNotBlank() && s.uid.equals(defaultStd.uid, ignoreCase = true))
                }
                if (!exists) {
                    val override = getPremiumOverride(defaultStd.uid)
                        ?: (if (defaultStd.email.isNotBlank()) getPremiumOverride(defaultStd.email) else null)
                        ?: (if (defaultStd.formattedStudentId.isNotBlank()) getPremiumOverride(defaultStd.formattedStudentId) else null)
                    val isPrem = when {
                        override != null -> override
                        premiumSet.contains(defaultStd.uid) -> true
                        defaultStd.email.isNotBlank() && premiumSet.contains(defaultStd.email.lowercase().trim()) -> true
                        defaultStd.isPremium -> true
                        else -> false
                    }
                    allRaw.add(defaultStd.copy(isPremium = isPrem))
                }
            }

            // Group and deduplicate safely so no student is lost
            val distinctStudents = allRaw
                .filter { prof ->
                    val uidClean = prof.uid.trim()
                    
                    // A student is hidden if their specific UID is in the deletedSet
                    val isDeletedByUid = deletedSet.contains(uidClean)
                    
                    !isDeletedByUid || uidClean == auth.currentUser?.uid
                }
                .groupBy { getStudentDeduplicationKey(it) }
                .map { (_, list) ->
                    val primary = list.maxByOrNull { it.lastUpdatedMillis } ?: list.first()
                    val override = list.firstNotNullOfOrNull { getPremiumOverride(it.uid) }
                        ?: list.firstNotNullOfOrNull { if (it.email.isNotBlank()) getPremiumOverride(it.email) else null }
                        ?: list.firstNotNullOfOrNull { if (it.formattedStudentId.isNotBlank()) getPremiumOverride(it.formattedStudentId) else null }
                        ?: getPremiumOverride(primary.uid)
                        ?: (if (primary.email.isNotBlank()) getPremiumOverride(primary.email) else null)

                    val isAnyDocPremium = list.any { it.isPremium }
                    val isAnyPremium = when {
                        override != null -> override
                        isAnyDocPremium -> true
                        premiumSet.contains(primary.uid) -> true
                        primary.email.isNotBlank() && premiumSet.contains(primary.email.lowercase().trim()) -> true
                        primary.formattedStudentId.isNotBlank() && premiumSet.contains(primary.formattedStudentId) -> true
                        list.any { premiumSet.contains(it.uid) || (it.email.isNotBlank() && premiumSet.contains(it.email.lowercase().trim())) } -> true
                        else -> false
                    }

                    val isAnyBlocked = list.any { it.isBlocked }
                    val isAnyLoggedOut = list.any { it.isForceLoggedOut }
                    val blockReason = list.firstOrNull { it.blockReason.isNotBlank() }?.blockReason ?: primary.blockReason

                    primary.copy(
                        isPremium = isAnyPremium,
                        isBlocked = isAnyBlocked,
                        isForceLoggedOut = isAnyLoggedOut,
                        blockReason = if (isAnyBlocked) blockReason else primary.blockReason
                    )
                }

            trySend(distinctStudents)
        }

        val deletedListener = firestore.collection("deleted_students").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val newSet = mutableSetOf<String>()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid") ?: doc.id
                    val email = (doc.getString("email") ?: "").trim().lowercase()
                    val name = (doc.getString("name") ?: "").trim().lowercase()
                    if (uid.isNotBlank()) newSet.add(uid.trim())
                    if (email.isNotBlank()) newSet.add(email)
                }
                deletedSet = newSet
            }
            emitCombined()
        }

        val usersListener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                usersList = snapshot.documents.mapNotNull { docToUserProfile(it) }
            }
            emitCombined()
        }

        val accountsListener = firestore.collection("user_accounts").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                userAccountsList = snapshot.documents.mapNotNull { docToUserProfile(it) }
            }
            emitCombined()
        }

        val premiumListener = firestore.collection("premium_students").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val newPremSet = mutableSetOf<String>()
                for (doc in snapshot.documents) {
                    val isPrem = extractBoolean(doc, "isPremium", "premium") ?: true
                    if (isPrem) {
                        newPremSet.add(doc.id)
                        doc.getString("uid")?.let { if (it.isNotBlank()) newPremSet.add(it) }
                        doc.getString("email")?.let { if (it.isNotBlank()) newPremSet.add(it.lowercase().trim()) }
                        doc.getString("studentId")?.let { if (it.isNotBlank()) newPremSet.add(it) }
                    }
                }
                premiumSet = newPremSet
                newPremSet.forEach { setPremiumOverride(it, true) }
            }
            emitCombined()
        }

        // Initial emission with defaults
        emitCombined()

        awaitClose {
            deletedListener.remove()
            usersListener.remove()
            accountsListener.remove()
            premiumListener.remove()
        }
    }

    suspend fun checkIfUserIsBlocked(identifierOrUid: String): Pair<Boolean, String> {
        if (identifierOrUid.isBlank()) return Pair(false, "")
        val clean = normalizeIdentifier(identifierOrUid)
        if (clean == "ullassuvarna65@gmail.com" || clean == "ullassuvarna85@gmail.com" || clean == "admin_ullas" || identifierOrUid.trim().lowercase() == "ullassuvarna65@gmail.com") {
            return Pair(false, "")
        }
        try {
            // 1. Direct UID check in users collection
            val directDoc = firestore.collection("users").document(identifierOrUid).get().awaitResult().getOrNull()
            if (directDoc != null && directDoc.exists()) {
                val isBlocked = directDoc.getBoolean("isBlocked") ?: false
                val reason = directDoc.getString("blockReason") ?: ""
                if (isBlocked) return Pair(true, reason)
            }

            // 2. Normalize and check user_accounts collection
            val cleanId = normalizeIdentifier(identifierOrUid)
            val accDoc = firestore.collection("user_accounts").document(cleanId).get().awaitResult().getOrNull()
            if (accDoc != null && accDoc.exists()) {
                val uid = accDoc.getString("uid")
                if (!uid.isNullOrBlank()) {
                    val userDoc = firestore.collection("users").document(uid).get().awaitResult().getOrNull()
                    if (userDoc != null && userDoc.exists()) {
                        val isBlocked = userDoc.getBoolean("isBlocked") ?: false
                        val reason = userDoc.getString("blockReason") ?: ""
                        if (isBlocked) return Pair(true, reason)
                    }
                }
            }

            // 3. Check users collection by email query
            if (identifierOrUid.contains("@")) {
                val queryRes = firestore.collection("users").whereEqualTo("email", identifierOrUid.lowercase().trim()).limit(1).get().awaitResult().getOrNull()
                if (queryRes != null && !queryRes.isEmpty) {
                    val doc = queryRes.documents.first()
                    val isBlocked = doc.getBoolean("isBlocked") ?: false
                    val reason = doc.getString("blockReason") ?: ""
                    if (isBlocked) return Pair(true, reason)
                }
            }

            // 4. Check sample default students in memory
            val sampleStudent = SampleData.defaultStudents.find { it.uid == identifierOrUid || it.email.equals(identifierOrUid, ignoreCase = true) }
            if (sampleStudent?.isBlocked == true) {
                return Pair(true, sampleStudent.blockReason)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user block status: ${e.message}")
        }
        return Pair(false, "")
    }

    suspend fun forceLogoutStudent(identifier: String): Boolean {
        if (identifier.isBlank()) return false
        val cleanCheck = normalizeIdentifier(identifier)
        if (cleanCheck == "ullassuvarna65@gmail.com" || cleanCheck == "admin_ullas" || cleanCheck == "ullassuvarna85@gmail.com") {
            return false
        }
        return try {
            val cleanId = normalizeIdentifier(identifier)
            val emailDocId = cleanId.replace(".", "_").replace("@", "_")

            firestore.collection("users").document(identifier).update("isForceLoggedOut", true).awaitResult()
            if (emailDocId != identifier) {
                firestore.collection("users").document(emailDocId).update("isForceLoggedOut", true).awaitResult()
            }
            if (cleanId.contains("@")) {
                val querySnap = firestore.collection("users").whereEqualTo("email", cleanId).get().awaitResult().getOrNull()
                querySnap?.documents?.forEach { doc ->
                    firestore.collection("users").document(doc.id).update("isForceLoggedOut", true).awaitResult()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateStudentPremiumStatus(
        identifier: String,
        isPremium: Boolean,
        email: String = "",
        studentId: String = ""
    ): Boolean {
        if (identifier.isBlank() && email.isBlank()) return false
        val now = System.currentTimeMillis()
        val updateData = mapOf(
            "isPremium" to isPremium,
            "premium" to isPremium,
            "lastUpdatedMillis" to now
        )

        val keysToOverride = mutableSetOf<String>()
        if (identifier.isNotBlank()) {
            keysToOverride.add(identifier)
            val cleanId = normalizeIdentifier(identifier)
            keysToOverride.add(cleanId)
            keysToOverride.add(cleanId.replace(".", "_").replace("@", "_"))
        }
        if (email.isNotBlank()) {
            val cleanEmail = email.lowercase().trim()
            keysToOverride.add(cleanEmail)
            keysToOverride.add(cleanEmail.replace(".", "_").replace("@", "_"))
        }
        if (studentId.isNotBlank()) {
            keysToOverride.add(studentId)
            keysToOverride.add(studentId.lowercase().trim())
        }

        // 1. In-memory overrides
        keysToOverride.forEach { k ->
            setPremiumOverride(k, isPremium)
        }

        // 2. Realtime Database instant push across ALL keys
        try {
            val rtdbRef = realtimeDb.getReference("user_premium")
            keysToOverride.forEach { k ->
                val safeKey = k.replace(".", "_").replace("@", "_")
                rtdbRef.child(safeKey).setValue(isPremium)
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB user_premium update error: ${e.message}")
        }

        // 3. Firestore users collection
        val cleanEmail = (if (email.isNotBlank()) email else if (identifier.contains("@")) identifier else "").lowercase().trim()
        val emailDocId = cleanEmail.replace(".", "_").replace("@", "_")

        return try {
            if (identifier.isNotBlank()) {
                firestore.collection("users").document(identifier).set(updateData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            if (emailDocId.isNotBlank() && emailDocId != identifier) {
                firestore.collection("users").document(emailDocId).set(updateData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("user_accounts").document(cleanEmail).set(updateData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                val qSnap = firestore.collection("users").whereEqualTo("email", cleanEmail).get().awaitResult().getOrNull()
                qSnap?.documents?.forEach { doc ->
                    try {
                        doc.reference.set(updateData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    } catch (_: Exception) {}
                }
            }
            if (identifier.isNotBlank()) {
                val qSnapUid = firestore.collection("users").whereEqualTo("uid", identifier).get().awaitResult().getOrNull()
                qSnapUid?.documents?.forEach { doc ->
                    try {
                        doc.reference.set(updateData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    } catch (_: Exception) {}
                }
            }

            // 4. Update dedicated premium_students collection
            val premDocData = mapOf(
                "isPremium" to isPremium,
                "uid" to identifier,
                "email" to cleanEmail,
                "studentId" to studentId,
                "lastUpdatedMillis" to now
            )
            if (identifier.isNotBlank()) {
                firestore.collection("premium_students").document(identifier).set(premDocData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            if (emailDocId.isNotBlank()) {
                firestore.collection("premium_students").document(emailDocId).set(premDocData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("premium_students").document(cleanEmail).set(premDocData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating Firestore premium status: ${e.message}")
            false
        }
    }

    suspend fun resetUserMilestonesAndReceipts(userId: String) {
        if (userId.isBlank()) return
        
        val initialBadges = listOf(
            BadgeItem("b1", "KCET Pioneer", "Complete your first CBT mock test", "🎯", isUnlocked = false, progress = 0f),
            BadgeItem("b2", "Top Scorer", "Score 80%+ on any mock exam", "🏆", isUnlocked = false, progress = 0f),
            BadgeItem("b3", "Perfect Score", "Achieve 100% accuracy on a test", "💯", isUnlocked = false, progress = 0f),
            BadgeItem("b4", "Speed Demon", "Finish exam with < 30s per question", "⚡", isUnlocked = false, progress = 0f),
            BadgeItem("b5", "Streak Master", "Attempt 3 or more mock tests", "🔥", isUnlocked = false, progress = 0f),
            BadgeItem("b6", "Physics Wizard", "Score 80%+ in Physics section", "⚛️", isUnlocked = false, progress = 0f),
            BadgeItem("b7", "Chemistry Ace", "Score 80%+ in Chemistry section", "🧪", isUnlocked = false, progress = 0f),
            BadgeItem("b8", "Maths Genius", "Score 80%+ in Mathematics section", "📐", isUnlocked = false, progress = 0f),
            BadgeItem("b9", "Daily Warrior", "Complete 5 Daily AI Practice / Mock tests", "📅", isUnlocked = false, progress = 0f),
            BadgeItem("b10", "Bookmark Scholar", "Save & practice 10+ bookmarked questions", "🔖", isUnlocked = false, progress = 0f),
            BadgeItem("b11", "Century Club", "Attempt 100+ questions across all mock tests", "📊", isUnlocked = false, progress = 0f),
            BadgeItem("b12", "KCET Marathoner", "Attempt a full 180-question PCM mock exam", "🏃", isUnlocked = false, progress = 0f),
            BadgeItem("b13", "Accuracy Elite", "Score 90%+ with under 40s per question", "🎯", isUnlocked = false, progress = 0f),
            BadgeItem("b14", "KCET Centurion Legend", "Score 160+/180 on a full 180-Question Mock Test with >88% accuracy", "👑", isUnlocked = false, progress = 0f, category = "Elite Challenge", isVeryDifficult = true),
            BadgeItem("b15", "State Rank #1 Grandmaster", "Accumulate 2,500+ XP points and complete 10+ full CBT mock exams", "🌟", isUnlocked = false, progress = 0f, category = "Elite Challenge", isVeryDifficult = true)
        )
        
        for (badge in initialBadges) {
            try {
                saveBadge(userId, badge)
            } catch (_: Exception) {}
        }
        
        try {
            val rejectedData = mapOf(
                "status" to "REJECTED",
                "rejectionReason" to "Premium access revoked by Administrator. Please pay and submit receipt to unlock again."
            )
            val q1 = firestore.collection("payment_receipts")
                .whereEqualTo("userId", userId)
                .get()
                .awaitResult()
                .getOrNull()
                
            q1?.documents?.forEach { doc ->
                try {
                    firestore.collection("payment_receipts").document(doc.id).set(rejectedData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                } catch (_: Exception) {}
            }

            if (userId.contains("@")) {
                val q2 = firestore.collection("payment_receipts")
                    .whereEqualTo("userEmail", userId)
                    .get()
                    .awaitResult()
                    .getOrNull()
                q2?.documents?.forEach { doc ->
                    try {
                        firestore.collection("payment_receipts").document(doc.id).set(rejectedData, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun forceLogoutAllStudents(): Boolean {
        return try {
            val snapshot = firestore.collection("users").get().awaitResult().getOrNull()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val email = doc.getString("email")?.lowercase()?.trim() ?: ""
                    if (doc.id == "admin_ullas" || email == "ullassuvarna65@gmail.com" || email == "ullassuvarna85@gmail.com" || doc.id == "mentor_ullas_85") {
                        continue
                    }
                    firestore.collection("users").document(doc.id).update("isForceLoggedOut", true).awaitResult()
                }
            }
            firestore.collection("system_settings").document("global_session").set(
                mapOf("lastForceLogoutAllTimestamp" to System.currentTimeMillis()),
                com.google.firebase.firestore.SetOptions.merge()
            ).awaitResult()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearForceLogoutFlag(uid: String): Boolean {
        if (uid.isBlank()) return false
        val res = firestore.collection("users").document(uid).update("isForceLoggedOut", false).awaitResult()
        return res.isSuccess
    }

    suspend fun clearDeletedStudent(email: String = "", uid: String = "") {
        try {
            val emailLower = email.trim().lowercase()
            val uidTrim = uid.trim()

            // 1. Delete by document ID (all possible patterns)
            val keysToDelete = mutableSetOf<String>()
            if (emailLower.isNotBlank()) {
                keysToDelete.add(emailLower)
                keysToDelete.add(normalizeIdentifier(emailLower))
            }
            if (uidTrim.isNotBlank()) {
                keysToDelete.add(uidTrim)
            }

            keysToDelete.forEach { key ->
                if (key.isNotBlank()) {
                    firestore.collection("deleted_students").document(key).delete().awaitResult()
                }
            }

            // 2. Aggressive field-based deletion
            if (emailLower.isNotBlank()) {
                val q1 = firestore.collection("deleted_students").whereEqualTo("email", emailLower).get().awaitResult().getOrNull()
                q1?.documents?.forEach { it.reference.delete().awaitResult() }
                
                val q2 = firestore.collection("deleted_students").whereEqualTo("cleanEmail", normalizeIdentifier(emailLower)).get().awaitResult().getOrNull()
                q2?.documents?.forEach { it.reference.delete().awaitResult() }
            }
            if (uidTrim.isNotBlank()) {
                val q3 = firestore.collection("deleted_students").whereEqualTo("uid", uidTrim).get().awaitResult().getOrNull()
                q3?.documents?.forEach { it.reference.delete().awaitResult() }
            }
        } catch (_: Exception) {}
    }

    suspend fun checkIfStudentIsDeleted(identifier: String): Boolean {
        if (identifier.isBlank()) return false
        val clean = normalizeIdentifier(identifier)
        if (clean == "ullassuvarna65@gmail.com" || clean == "ullassuvarna85@gmail.com" || clean == "admin_ullas" || identifier.trim().lowercase() == "ullassuvarna65@gmail.com") {
            return false
        }
        return try {

            // If an active account record or user profile document actually exists in users or user_accounts,
            // then the student has registered/re-registered! Clear the deleted marker and return false!
            val accountRecord = getUserAccountRecord(clean)
            val userProfile = getUserProfileOnce(clean)
            if (accountRecord != null || userProfile != null) {
                clearDeletedStudent(clean, userProfile?.uid ?: "")
                return false
            }

            val doc1 = firestore.collection("deleted_students").document(clean).get().awaitResult().getOrNull()
            if (doc1 != null && doc1.exists()) return true
            val doc2 = firestore.collection("deleted_students").document(identifier.trim().lowercase()).get().awaitResult().getOrNull()
            if (doc2 != null && doc2.exists()) return true
            val doc3 = firestore.collection("deleted_students").document(identifier.trim()).get().awaitResult().getOrNull()
            if (doc3 != null && doc3.exists()) return true
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteStudentAccountPermanentlyByAdmin(student: UserProfile): Boolean {
        val uid = student.uid.trim()
        val email = student.email.trim().lowercase()
        val name = student.name.trim()
        val studentId = student.studentId.trim()
        val formattedId = student.formattedStudentId.trim()
        val cleanEmail = if (email.isNotBlank()) normalizeIdentifier(email) else ""

        return try {
            // 1. Store record in deleted_students collection in Firestore
            val deletedInfo = mapOf(
                "uid" to uid,
                "email" to email,
                "name" to name,
                "studentId" to formattedId,
                "deletedAtTimestamp" to System.currentTimeMillis()
            )
            if (uid.isNotBlank()) {
                firestore.collection("deleted_students").document(uid).set(deletedInfo).awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("deleted_students").document(cleanEmail).set(deletedInfo).awaitResult()
            }
            if (email.isNotBlank() && email != cleanEmail) {
                firestore.collection("deleted_students").document(email).set(deletedInfo).awaitResult()
            }

            // 2. Delete from users collection
            if (uid.isNotBlank()) {
                firestore.collection("users").document(uid).delete().awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("users").document(cleanEmail).delete().awaitResult()
            }
            if (email.isNotBlank() && email != cleanEmail) {
                firestore.collection("users").document(email).delete().awaitResult()
            }
            if (email.isNotBlank()) {
                val userByEmailQuery = firestore.collection("users").whereEqualTo("email", email).get().awaitResult().getOrNull()
                userByEmailQuery?.documents?.forEach { doc ->
                    doc.reference.delete().awaitResult()
                }
            }

            // 3. Delete from user_accounts collection (Authentication & Credentials)
            if (uid.isNotBlank()) {
                firestore.collection("user_accounts").document(uid).delete().awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("user_accounts").document(cleanEmail).delete().awaitResult()
            }
            if (email.isNotBlank() && email != cleanEmail) {
                firestore.collection("user_accounts").document(email).delete().awaitResult()
            }
            if (uid.isNotBlank()) {
                val accUidDocs = firestore.collection("user_accounts").whereEqualTo("uid", uid).get().awaitResult().getOrNull()?.documents
                accUidDocs?.forEach { it.reference.delete().awaitResult() }
            }
            if (email.isNotBlank()) {
                val accEmailDocs = firestore.collection("user_accounts").whereEqualTo("email", email).get().awaitResult().getOrNull()?.documents
                accEmailDocs?.forEach { it.reference.delete().awaitResult() }
                val accAuthEmailDocs = firestore.collection("user_accounts").whereEqualTo("authEmail", email).get().awaitResult().getOrNull()?.documents
                accAuthEmailDocs?.forEach { it.reference.delete().awaitResult() }
            }

            // 4. Delete from Firestore leaderboard collection
            if (uid.isNotBlank()) {
                firestore.collection("leaderboard").document(uid).delete().awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                firestore.collection("leaderboard").document(cleanEmail).delete().awaitResult()
            }
            if (email.isNotBlank() && email != cleanEmail) {
                firestore.collection("leaderboard").document(email).delete().awaitResult()
            }
            if (uid.isNotBlank()) {
                val lbUidDocs = firestore.collection("leaderboard").whereEqualTo("uid", uid).get().awaitResult().getOrNull()?.documents
                lbUidDocs?.forEach { it.reference.delete().awaitResult() }
            }
            if (email.isNotBlank()) {
                val lbEmailDocs = firestore.collection("leaderboard").whereEqualTo("email", email).get().awaitResult().getOrNull()?.documents
                lbEmailDocs?.forEach { it.reference.delete().awaitResult() }
            }
            if (name.isNotBlank() && !name.equals("Student", ignoreCase = true) && !name.contains("Guest", ignoreCase = true)) {
                val lbNameDocs = firestore.collection("leaderboard").whereEqualTo("name", name).get().awaitResult().getOrNull()?.documents
                lbNameDocs?.forEach { it.reference.delete().awaitResult() }
            }

            // 5. Delete from Realtime Database
            FirebaseRealtimeService.deleteStudentData(uid, email, name)

            // 6. Delete test results
            val testResultsQuery = firestore.collection("test_results").get().awaitResult().getOrNull()
            testResultsQuery?.documents?.forEach { doc ->
                val docUid = doc.getString("userId") ?: ""
                val docEmail = (doc.getString("userEmail") ?: "").trim().lowercase()
                val docStudentId = doc.getString("studentId") ?: ""
                val docName = (doc.getString("userName") ?: "").trim()
                val matches = (uid.isNotBlank() && docUid == uid) ||
                        (email.isNotBlank() && docEmail == email) ||
                        (formattedId.isNotBlank() && docStudentId == formattedId) ||
                        (studentId.isNotBlank() && docStudentId == studentId) ||
                        (name.isNotBlank() && !name.equals("Student", ignoreCase = true) && docName.equals(name, ignoreCase = true))
                if (matches) {
                    doc.reference.delete().awaitResult()
                }
            }

            // 7. Delete bookmarks
            if (uid.isNotBlank()) {
                val bmDocs = firestore.collection("user_bookmarks").whereEqualTo("userId", uid).get().awaitResult().getOrNull()?.documents
                bmDocs?.forEach { it.reference.delete().awaitResult() }
            }
            if (cleanEmail.isNotBlank()) {
                val bmEmailDocs = firestore.collection("user_bookmarks").whereEqualTo("userId", cleanEmail).get().awaitResult().getOrNull()?.documents
                bmEmailDocs?.forEach { it.reference.delete().awaitResult() }
            }

            // 8. Delete user badges
            if (uid.isNotBlank()) {
                val badgeDocs = firestore.collection("badges").document(uid).collection("user_badges").get().awaitResult().getOrNull()?.documents
                badgeDocs?.forEach { it.reference.delete().awaitResult() }
                firestore.collection("badges").document(uid).delete().awaitResult()
            }
            if (cleanEmail.isNotBlank()) {
                val badgeEmailDocs = firestore.collection("badges").document(cleanEmail).collection("user_badges").get().awaitResult().getOrNull()?.documents
                badgeEmailDocs?.forEach { it.reference.delete().awaitResult() }
                firestore.collection("badges").document(cleanEmail).delete().awaitResult()
            }

            // 9. Delete payment receipts
            val receiptsQuery = firestore.collection("payment_receipts").get().awaitResult().getOrNull()
            receiptsQuery?.documents?.forEach { doc ->
                val rUid = doc.getString("userId") ?: ""
                val rEmail = (doc.getString("userEmail") ?: "").trim().lowercase()
                if ((uid.isNotBlank() && rUid == uid) || (email.isNotBlank() && rEmail == email)) {
                    doc.reference.delete().awaitResult()
                }
            }

            // 10. Delete senior chat bookings
            val bookingsQuery = firestore.collection("senior_chat_bookings").get().awaitResult().getOrNull()
            bookingsQuery?.documents?.forEach { doc ->
                val bUid = doc.getString("userId") ?: ""
                val bEmail = (doc.getString("userEmail") ?: "").trim().lowercase()
                if ((uid.isNotBlank() && bUid == uid) || (email.isNotBlank() && bEmail == email)) {
                    doc.reference.delete().awaitResult()
                }
            }

            // 11. Delete Firebase Auth User if currently logged in
            val currentFbUser = auth.currentUser
            if (currentFbUser != null && (currentFbUser.uid == uid || (email.isNotBlank() && currentFbUser.email?.equals(email, ignoreCase = true) == true))) {
                try { currentFbUser.delete().awaitResult() } catch (_: Exception) {}
                try { auth.signOut() } catch (_: Exception) {}
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteStudentAccountPermanentlyByAdmin: ${e.message}", e)
            false
        }
    }

    suspend fun deleteStudentAccountByAdmin(uid: String): Boolean {
        if (uid.isBlank()) return false
        val prof = getUserProfileOnce(uid) ?: UserProfile(uid = uid, name = "Student", email = if (uid.contains("@")) uid else "")
        return deleteStudentAccountPermanentlyByAdmin(prof)
    }

    // -------------------------------------------------------------
    // 2. QUESTIONS COLLECTION
    // -------------------------------------------------------------
    fun observeQuestions(): Flow<List<Question>> = callbackFlow {
        val query = firestore.collection("questions")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleQuestions)
                return@addSnapshotListener
            }
            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                    val subjectStr = doc.getString("subject") ?: "PHYSICS"
                    val subject = try { Subject.valueOf(subjectStr) } catch (_: Exception) { Subject.PHYSICS }
                    val questionText = doc.getString("questionText") ?: ""
                    val imageUrl = doc.getString("imageUrl")
                    val optionA = doc.getString("optionA") ?: ""
                    val optionB = doc.getString("optionB") ?: ""
                    val optionC = doc.getString("optionC") ?: ""
                    val optionD = doc.getString("optionD") ?: ""
                    val correctOptStr = doc.getString("correctOption") ?: "A"
                    val correctOption = try { Option.valueOf(correctOptStr) } catch (_: Exception) { Option.A }
                    val explanation = doc.getString("explanation") ?: ""
                    val testSetId = doc.getString("testSetId") ?: "default"
                    val isPremium = doc.getBoolean("isPremium") ?: false

                    Question(
                        id = id,
                        subject = subject,
                        questionText = questionText,
                        imageUrl = imageUrl,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOption = correctOption,
                        explanation = explanation,
                        testSetId = testSetId,
                        isPremium = isPremium
                    )
                } catch (e: Exception) {
                    null
                }
            }
            trySend(if (questions.isNotEmpty()) questions else SampleData.sampleQuestions)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveQuestion(question: Question) {
        val docMap = mapOf(
            "id" to question.id,
            "subject" to question.subject.name,
            "questionText" to question.questionText,
            "imageUrl" to (question.imageUrl ?: ""),
            "optionA" to question.optionA,
            "optionB" to question.optionB,
            "optionC" to question.optionC,
            "optionD" to question.optionD,
            "correctOption" to question.correctOption.name,
            "explanation" to question.explanation,
            "testSetId" to question.testSetId,
            "isPremium" to question.isPremium
        )
        firestore.collection("questions").document(question.id.toString()).set(docMap).awaitResult()
    }

    suspend fun saveQuestionBatch(questions: List<Question>) {
        val chunks = questions.chunked(400)
        chunks.forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { q ->
                val docRef = firestore.collection("questions").document(q.id.toString())
                val docMap = mapOf(
                    "id" to q.id,
                    "subject" to q.subject.name,
                    "questionText" to q.questionText,
                    "imageUrl" to (q.imageUrl ?: ""),
                    "optionA" to q.optionA,
                    "optionB" to q.optionB,
                    "optionC" to q.optionC,
                    "optionD" to q.optionD,
                    "correctOption" to q.correctOption.name,
                    "explanation" to q.explanation,
                    "testSetId" to q.testSetId,
                    "isPremium" to q.isPremium
                )
                batch.set(docRef, docMap)
            }
            batch.commit().awaitResult()
        }
    }

    suspend fun deleteQuestion(questionId: Int) {
        firestore.collection("questions").document(questionId.toString()).delete().awaitResult()
    }

    // -------------------------------------------------------------
    // USER BOOKMARKS COLLECTION (Stored in Firebase)
    // -------------------------------------------------------------
    fun observeUserBookmarks(userId: String = "kcet_std_001"): Flow<List<BookmarkEntity>> = callbackFlow {
        val query = firestore.collection("user_bookmarks").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    BookmarkEntity(
                        questionId = (doc.getLong("questionId") ?: doc.id.toLongOrNull() ?: 0L).toInt(),
                        subject = doc.getString("subject") ?: "PHYSICS",
                        questionText = doc.getString("questionText") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        optionA = doc.getString("optionA") ?: "",
                        optionB = doc.getString("optionB") ?: "",
                        optionC = doc.getString("optionC") ?: "",
                        optionD = doc.getString("optionD") ?: "",
                        correctOption = doc.getString("correctOption") ?: "A",
                        explanation = doc.getString("explanation") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserBookmark(userId: String = "kcet_std_001", bookmark: BookmarkEntity) {
        val docMap = mapOf(
            "userId" to userId,
            "questionId" to bookmark.questionId,
            "subject" to bookmark.subject,
            "questionText" to bookmark.questionText,
            "imageUrl" to (bookmark.imageUrl ?: ""),
            "optionA" to bookmark.optionA,
            "optionB" to bookmark.optionB,
            "optionC" to bookmark.optionC,
            "optionD" to bookmark.optionD,
            "correctOption" to bookmark.correctOption,
            "explanation" to bookmark.explanation,
            "timestamp" to bookmark.timestamp
        )
        firestore.collection("user_bookmarks").document("${userId}_${bookmark.questionId}").set(docMap).awaitResult()
    }

    suspend fun deleteUserBookmark(userId: String = "kcet_std_001", questionId: Int) {
        firestore.collection("user_bookmarks").document("${userId}_${questionId}").delete().awaitResult()
    }

    // -------------------------------------------------------------
    // TEST SERIES & QUESTION SETS COLLECTION
    // -------------------------------------------------------------
    fun observeTestSeriesSets(): Flow<List<TestSeriesSet>> = callbackFlow {
        val query = firestore.collection("test_series_sets")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleTestSeriesSets)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                TestSeriesSet(
                    id = doc.getString("id") ?: doc.id,
                    title = doc.getString("title") ?: "",
                    category = doc.getString("category") ?: "Full Mock Tests",
                    questionCount = (doc.getLong("questionCount") ?: 20L).toInt(),
                    durationMins = (doc.getLong("durationMins") ?: 20L).toInt(),
                    isPremium = doc.getBoolean("isPremium") ?: true,
                    description = doc.getString("description") ?: "",
                    createdDate = doc.getString("createdDate") ?: "2026"
                )
            }
            trySend(if (items.isNotEmpty()) items else SampleData.sampleTestSeriesSets)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveTestSeriesSet(item: TestSeriesSet) {
        val docMap = mapOf(
            "id" to item.id,
            "title" to item.title,
            "category" to item.category,
            "questionCount" to item.questionCount,
            "durationMins" to item.durationMins,
            "isPremium" to item.isPremium,
            "description" to item.description,
            "createdDate" to item.createdDate
        )
        firestore.collection("test_series_sets").document(item.id).set(docMap).awaitResult()
    }

    suspend fun deleteTestSeriesSet(id: String) {
        firestore.collection("test_series_sets").document(id).delete().awaitResult()
    }

    suspend fun seedSampleQuestionsIfEmpty() {
        try {
            val query = firestore.collection("questions").get().awaitResult()
            if (query.isSuccess && (query.getOrNull()?.isEmpty == true)) {
                SampleData.sampleQuestions.forEach { q ->
                    saveQuestion(q)
                }
            }

            val tsQuery = firestore.collection("test_series_sets").get().awaitResult()
            if (tsQuery.isSuccess && (tsQuery.getOrNull()?.isEmpty == true)) {
                SampleData.sampleTestSeriesSets.forEach { ts ->
                    saveTestSeriesSet(ts)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding sample questions or test series sets: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // 3. TEST_RESULTS COLLECTION
    // -------------------------------------------------------------
    suspend fun saveTestResult(
        result: TestResultEntity,
        userId: String = "",
        userName: String = "",
        userEmail: String = "",
        studentId: String = ""
    ) {
        val uid = userId.ifBlank { auth.currentUser?.uid ?: "guest" }
        val docId = if (result.attemptId.isNotBlank()) {
            result.attemptId
        } else {
            "att_${result.timestamp}_${result.score}_${result.totalQuestions}"
        }
        val docMap = mapOf(
            "attemptId" to docId,
            "userId" to uid,
            "userName" to userName,
            "userEmail" to userEmail,
            "studentId" to studentId,
            "testName" to result.testName,
            "timestamp" to result.timestamp,
            "totalQuestions" to result.totalQuestions,
            "score" to result.score,
            "correctCount" to result.correctCount,
            "wrongCount" to result.wrongCount,
            "unansweredCount" to result.unansweredCount,
            "timeTakenSeconds" to result.timeTakenSeconds,
            "percentage" to result.percentage
        )
        firestore.collection("test_results").document(docId).set(docMap).awaitResult()
    }

    suspend fun saveTestResult(result: TestResultEntity, userProfile: UserProfile) {
        saveTestResult(
            result = result,
            userId = userProfile.uid,
            userName = userProfile.name,
            userEmail = userProfile.email,
            studentId = userProfile.formattedStudentId
        )
    }

    fun observeUserTestResults(userId: String = ""): Flow<List<TestResultEntity>> = callbackFlow {
        val uid = userId.ifBlank { auth.currentUser?.uid ?: "guest" }
        val query = firestore.collection("test_results")
            .whereEqualTo("userId", uid)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val results = snapshot.documents.mapIndexed { idx, doc ->
                val attId = doc.getString("attemptId") ?: doc.id
                TestResultEntity(
                    id = idx + 1,
                    attemptId = attId,
                    testName = doc.getString("testName") ?: "CBT Mock Test",
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    totalQuestions = (doc.getLong("totalQuestions") ?: 20L).toInt(),
                    score = (doc.getLong("score") ?: 0L).toInt(),
                    correctCount = (doc.getLong("correctCount") ?: 0L).toInt(),
                    wrongCount = (doc.getLong("wrongCount") ?: 0L).toInt(),
                    unansweredCount = (doc.getLong("unansweredCount") ?: 0L).toInt(),
                    timeTakenSeconds = doc.getLong("timeTakenSeconds") ?: 0L,
                    percentage = (doc.getDouble("percentage") ?: 0.0).toFloat()
                )
            }
            trySend(results)
        }
        awaitClose { listener.remove() }
    }

    fun observeAllUserTestResults(): Flow<List<AdminTestResultItem>> = callbackFlow {
        val query = firestore.collection("test_results")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.defaultStudentTestResults)
                return@addSnapshotListener
            }
            val results = snapshot.documents.mapNotNull { doc ->
                try {
                    AdminTestResultItem(
                        docId = doc.id,
                        userId = doc.getString("userId") ?: "guest",
                        userName = doc.getString("userName") ?: doc.getString("userId") ?: "Student",
                        userEmail = doc.getString("userEmail") ?: "",
                        studentId = doc.getString("studentId") ?: "",
                        testName = doc.getString("testName") ?: "CBT Mock Test",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        totalQuestions = (doc.getLong("totalQuestions") ?: 20L).toInt(),
                        score = (doc.getLong("score") ?: 0L).toInt(),
                        correctCount = (doc.getLong("correctCount") ?: 0L).toInt(),
                        wrongCount = (doc.getLong("wrongCount") ?: 0L).toInt(),
                        unansweredCount = (doc.getLong("unansweredCount") ?: 0L).toInt(),
                        timeTakenSeconds = doc.getLong("timeTakenSeconds") ?: 0L,
                        percentage = (doc.getDouble("percentage") ?: 0.0).toFloat()
                    )
                } catch (e: Exception) { null }
            }
            if (results.isEmpty()) {
                trySend(SampleData.defaultStudentTestResults)
            } else {
                val existingIds = results.map { it.docId }.toSet()
                val merged = results + SampleData.defaultStudentTestResults.filter { it.docId !in existingIds }
                trySend(merged)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun deleteTestResultByAdmin(docId: String): Boolean {
        if (docId.isBlank()) return false
        val res = firestore.collection("test_results").document(docId).delete().awaitResult()
        return res.isSuccess
    }

    suspend fun updateTestResultByAdmin(docId: String, score: Int, correctCount: Int, wrongCount: Int, percentage: Float): Boolean {
        if (docId.isBlank()) return false
        val updates = mapOf(
            "score" to score,
            "correctCount" to correctCount,
            "wrongCount" to wrongCount,
            "percentage" to percentage
        )
        val res = firestore.collection("test_results").document(docId).update(updates).awaitResult()
        return res.isSuccess
    }

    // -------------------------------------------------------------
    // 4. LEADERBOARD COLLECTION
    // -------------------------------------------------------------
    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val currentUid = auth.currentUser?.uid ?: ""
        val currentEmail = auth.currentUser?.email?.trim()?.lowercase() ?: ""
        val leaderboardMap = mutableMapOf<String, LeaderboardEntry>()
        val userEntries = mutableMapOf<String, LeaderboardEntry>()

        val mentorEmails = mutableSetOf<String>()
        val mentorNames = mutableSetOf<String>()

        // Seed with defaults
        val deletedStudentKeys = mutableSetOf<String>()

        defaultVerifiedSeniorMentors.forEach {
            if (it.name.isNotBlank()) mentorNames.add(it.name.trim().lowercase())
            if (it.email.isNotBlank()) mentorEmails.add(it.email.trim().lowercase())
        }

        fun emitCombined() {
            val mergedList = mutableListOf<LeaderboardEntry>()

            // Gather all candidate sources in strict priority order:
            // 1. Seed defaults
            // 2. User profile records
            // 3. Dedicated leaderboard records (contain latest synced XP)
            val allCandidates = mutableListOf<LeaderboardEntry>()
            allCandidates.addAll(userEntries.values)
            allCandidates.addAll(leaderboardMap.values)

            for (candidate in allCandidates) {
                val emailLower = candidate.email.trim().lowercase()
                val nameClean = candidate.name.trim()
                val nameLower = nameClean.lowercase()
                val uidClean = candidate.uid.trim()

                // Filter out mentors and admins from statewide student leaderboard
                if (nameLower.contains("mentor") || emailLower.contains("mentor") ||
                    nameLower.contains("admin") || emailLower.contains("admin") ||
                    emailLower == "ullassuvarna65@gmail.com" || emailLower == "ullassuvarna85@gmail.com" ||
                    uidClean == "admin_ullas" || uidClean == "mentor_ullas_85") {
                    continue
                }
                if (mentorNames.contains(nameLower) || (emailLower.isNotBlank() && mentorEmails.contains(emailLower))) {
                    continue
                }
                if (deletedStudentKeys.contains(uidClean) ||
                    (emailLower.isNotBlank() && deletedStudentKeys.contains(emailLower))) {
                    continue
                }
                if (nameClean.isBlank()) continue

                val isGenericStudent = nameLower == "student" || nameLower == "kcet aspirant"
                if (isGenericStudent && candidate.points <= 0 && emailLower.isBlank()) {
                    continue
                }

                // Check if candidate already exists in mergedList (by email, uid, cross-match, or exact name)
                val existingIndex = mergedList.indexOfFirst { existing ->
                    val exEmail = existing.email.trim().lowercase()
                    val exUid = existing.uid.trim()
                    val exName = existing.name.trim().lowercase()

                    val emailMatch = emailLower.isNotBlank() && exEmail.isNotBlank() && emailLower == exEmail
                    val uidMatch = uidClean.isNotBlank() && exUid.isNotBlank() && uidClean == exUid
                    val crossEmailMatch = (uidClean.contains("@") && exEmail.isNotBlank() && uidClean.lowercase() == exEmail) ||
                                          (exUid.contains("@") && emailLower.isNotBlank() && exUid.lowercase() == emailLower)

                    val isGenericEx = exName == "student" || exName == "kcet aspirant" || exName.isBlank()
                    val nameMatch = !isGenericStudent && !isGenericEx && nameLower == exName

                    emailMatch || uidMatch || crossEmailMatch || nameMatch
                }

                val isCurrent = (currentUid.isNotBlank() && (uidClean == currentUid || emailLower == currentUid.lowercase())) ||
                                (currentEmail.isNotBlank() && emailLower == currentEmail) ||
                                candidate.isCurrentUser

                val processedCandidate = candidate.copy(isCurrentUser = isCurrent)

                if (existingIndex >= 0) {
                    val existing = mergedList[existingIndex]
                    val bestName = when {
                        existing.name.isNotBlank() && !existing.name.equals("Student", ignoreCase = true) && !existing.name.equals("KCET Aspirant", ignoreCase = true) -> existing.name
                        processedCandidate.name.isNotBlank() && !processedCandidate.name.equals("Student", ignoreCase = true) && !processedCandidate.name.equals("KCET Aspirant", ignoreCase = true) -> processedCandidate.name
                        existing.name.isNotBlank() -> existing.name
                        else -> processedCandidate.name.ifBlank { "KCET Aspirant" }
                    }
                    val bestEmail = if (existing.email.isNotBlank() && existing.email.contains("@")) existing.email else processedCandidate.email
                    val bestUid = if (existing.uid.isNotBlank() && !existing.uid.startsWith("mock_") && !existing.uid.contains("@")) existing.uid else processedCandidate.uid.ifBlank { existing.uid }
                    val bestInitials = if (existing.avatarInitials.isNotBlank() && existing.avatarInitials != "ST" && existing.avatarInitials != "KC") existing.avatarInitials else processedCandidate.avatarInitials
                    val bestTargetRank = if (existing.kcetTargetRank.isNotBlank() && existing.kcetTargetRank != "Under 1000") existing.kcetTargetRank else processedCandidate.kcetTargetRank

                    val bestAccuracy = if (processedCandidate.accuracy > 0f && existing.accuracy > 0f) {
                        maxOf(existing.accuracy, processedCandidate.accuracy)
                    } else if (processedCandidate.accuracy > 0f) {
                        processedCandidate.accuracy
                    } else {
                        existing.accuracy
                    }

                    mergedList[existingIndex] = existing.copy(
                        name = bestName,
                        email = bestEmail,
                        uid = bestUid,
                        kcetTargetRank = bestTargetRank,
                        points = maxOf(existing.points, processedCandidate.points),
                        accuracy = bestAccuracy,
                        testsCount = maxOf(existing.testsCount, processedCandidate.testsCount),
                        avatarInitials = bestInitials,
                        isCurrentUser = existing.isCurrentUser || processedCandidate.isCurrentUser
                    )
                } else {
                    mergedList.add(processedCandidate)
                }
            }

            val list = mergedList.sortedWith(
                compareByDescending<LeaderboardEntry> { it.points }
                    .thenByDescending { it.accuracy }
                    .thenByDescending { it.testsCount }
            ).mapIndexed { index, entry ->
                entry.copy(
                    rank = index + 1,
                    isCurrentUser = entry.isCurrentUser || 
                        (currentUid.isNotBlank() && entry.uid == currentUid) || 
                        (currentEmail.isNotBlank() && entry.email.trim().lowercase() == currentEmail)
                )
            }
            trySend(list)
        }

        val lbListener = firestore.collection("leaderboard").addSnapshotListener { snapshot, _ ->
            leaderboardMap.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val uid = (doc.getString("uid") ?: doc.id).trim()
                    val email = (doc.getString("email") ?: if (doc.id.contains("@")) doc.id else "").trim().lowercase()
                    val name = (doc.getString("name") ?: "Student").trim()
                    val docKey = uid.ifBlank { email.ifBlank { doc.id } }
                    val initials = doc.getString("avatarInitials") ?: if (name.length >= 2) name.substring(0, 2).uppercase() else "ST"
                    val entry = LeaderboardEntry(
                        rank = 0,
                        name = name,
                        kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000",
                        points = (doc.getLong("points") ?: 0L).toInt(),
                        accuracy = (doc.getDouble("accuracy") ?: 75.0).toFloat(),
                        testsCount = (doc.getLong("testsCount") ?: 1L).toInt(),
                        avatarInitials = initials,
                        isCurrentUser = uid == currentUid || doc.getString("uid") == currentUid || (currentEmail.isNotBlank() && email == currentEmail),
                        uid = uid,
                        email = email
                    )
                    leaderboardMap[docKey] = entry
                }
            }
            emitCombined()
        }

        val userListener = firestore.collection("users").addSnapshotListener { snapshot, _ ->
            userEntries.clear()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val uid = (doc.getString("uid") ?: doc.id).trim()
                    val email = (doc.getString("email") ?: if (doc.id.contains("@")) doc.id else "").trim().lowercase()
                    val name = (doc.getString("name") ?: "Student").trim()
                    val pts = (doc.getLong("points") ?: 0L).toInt()
                    val acc = (doc.getDouble("accuracy") ?: 75.0).toFloat()
                    val tests = (doc.getLong("testsCount") ?: 1L).toInt()

                    // Skip orphaned update documents that only have doc.id = email and no password/registration
                    if (doc.id.contains("@") && doc.getString("password") == null && doc.getString("registrationDate") == null && pts == 0) {
                        continue
                    }
                    // Skip alias docs ending in _gmail_com or similar
                    if (doc.id.endsWith("_gmail_com") || doc.id.endsWith("_yahoo_com") || doc.id.endsWith("_kcetapp_com")) {
                        continue
                    }

                    val docKey = uid.ifBlank { email.ifBlank { doc.id } }
                    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                    val entry = LeaderboardEntry(
                        rank = 0,
                        name = name,
                        kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000",
                        points = pts,
                        accuracy = acc,
                        testsCount = tests,
                        avatarInitials = if (initials.isNotBlank()) initials else "ST",
                        isCurrentUser = uid == currentUid || doc.id == currentUid || (currentEmail.isNotBlank() && email == currentEmail),
                        uid = uid,
                        email = email
                    )
                    userEntries[docKey] = entry
                }
            }
            emitCombined()
        }

        // Snapshot listeners for all mentor collections to filter them out dynamically
        val m1 = firestore.collection("mentors").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val mName = doc.getString("name") ?: doc.getString("mentorName") ?: ""
                    val mEmail = doc.getString("email") ?: doc.getString("mentorEmail") ?: ""
                    if (mName.isNotBlank()) mentorNames.add(mName.trim().lowercase())
                    if (mEmail.isNotBlank()) mentorEmails.add(mEmail.trim().lowercase())
                }
                emitCombined()
            }
        }

        val m2 = firestore.collection("registered_mentors").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val mName = doc.getString("name") ?: doc.getString("mentorName") ?: ""
                    val mEmail = doc.getString("email") ?: doc.getString("mentorEmail") ?: ""
                    if (mName.isNotBlank()) mentorNames.add(mName.trim().lowercase())
                    if (mEmail.isNotBlank()) mentorEmails.add(mEmail.trim().lowercase())
                }
                emitCombined()
            }
        }

        val m3 = firestore.collection("registeredMentors").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val mName = doc.getString("name") ?: doc.getString("mentorName") ?: ""
                    val mEmail = doc.getString("email") ?: doc.getString("mentorEmail") ?: ""
                    if (mName.isNotBlank()) mentorNames.add(mName.trim().lowercase())
                    if (mEmail.isNotBlank()) mentorEmails.add(mEmail.trim().lowercase())
                }
                emitCombined()
            }
        }

        val m4 = firestore.collection("senior_mentors").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val mName = doc.getString("name") ?: doc.getString("mentorName") ?: ""
                    val mEmail = doc.getString("email") ?: doc.getString("mentorEmail") ?: ""
                    if (mName.isNotBlank()) mentorNames.add(mName.trim().lowercase())
                    if (mEmail.isNotBlank()) mentorEmails.add(mEmail.trim().lowercase())
                }
                emitCombined()
            }
        }

        val delStudentListener = firestore.collection("deleted_students").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                deletedStudentKeys.clear()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid") ?: doc.id
                    val email = (doc.getString("email") ?: "").trim().lowercase()
                    val name = (doc.getString("name") ?: "").trim().lowercase()
                    if (uid.isNotBlank()) deletedStudentKeys.add(uid.trim())
                    if (email.isNotBlank()) deletedStudentKeys.add(email)
                }
                emitCombined()
            }
        }

        awaitClose {
            delStudentListener.remove()
            lbListener.remove()
            userListener.remove()
            m1.remove()
            m2.remove()
            m3.remove()
            m4.remove()
        }
    }

    suspend fun getUserPointsOnce(uid: String, email: String = ""): Int {
        val cleanUid = uid.trim()
        val cleanEmail = email.trim().lowercase()
        var maxPoints = 0

        try {
            if (cleanUid.isNotBlank()) {
                val lbDoc = firestore.collection("leaderboard").document(cleanUid).get().awaitResult().getOrNull()
                val lbPoints = (lbDoc?.getLong("points") ?: 0L).toInt()
                maxPoints = maxOf(maxPoints, lbPoints)

                val userDoc = firestore.collection("users").document(cleanUid).get().awaitResult().getOrNull()
                val uPoints = (userDoc?.getLong("points") ?: 0L).toInt()
                maxPoints = maxOf(maxPoints, uPoints)
            }
            if (cleanEmail.isNotBlank()) {
                val lbDocEmail = firestore.collection("leaderboard").document(cleanEmail).get().awaitResult().getOrNull()
                val lbEmailPoints = (lbDocEmail?.getLong("points") ?: 0L).toInt()
                maxPoints = maxOf(maxPoints, lbEmailPoints)

                val userDocEmail = firestore.collection("users").document(cleanEmail).get().awaitResult().getOrNull()
                val uEmailPoints = (userDocEmail?.getLong("points") ?: 0L).toInt()
                maxPoints = maxOf(maxPoints, uEmailPoints)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user points once: ${e.message}")
        }

        return maxPoints
    }

    suspend fun updateLeaderboardEntry(profile: UserProfile, points: Int, accuracy: Float, testsCount: Int) {
        val uid = profile.uid.ifBlank { auth.currentUser?.uid ?: "" }
        val email = profile.email.trim().lowercase()
        if (uid.isBlank() && email.isBlank()) return

        // Filter out mentors and admins from leaderboard updates
        val nameLower = profile.name.lowercase().trim()
        if (nameLower.contains("mentor") || email.contains("mentor") ||
            nameLower.contains("admin") || email.contains("admin") ||
            email == "ullassuvarna65@gmail.com" || email == "ullassuvarna85@gmail.com") {
            return
        }

        val canonicalDocId = uid.ifBlank { email }
        val initials = profile.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        val avatar = if (initials.isNotBlank()) initials else "ST"
        val now = System.currentTimeMillis()
        val docMap = mapOf(
            "uid" to uid,
            "name" to profile.name,
            "kcetTargetRank" to profile.kcetTargetRank,
            "points" to points,
            "accuracy" to accuracy,
            "testsCount" to testsCount,
            "avatarInitials" to avatar,
            "email" to profile.email,
            "lastUpdatedMillis" to now
        )

        // Write ONLY to single canonical document in leaderboard collection
        firestore.collection("leaderboard").document(canonicalDocId).set(docMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

        // Clean up legacy duplicate document if email was used as docId and differs from canonicalDocId
        if (email.isNotBlank() && email != canonicalDocId) {
            try {
                firestore.collection("leaderboard").document(email).delete()
            } catch (_: Exception) {}
        }

        // Also update users collection for the canonical UID with complete info
        val userUpdate = mapOf(
            "uid" to uid,
            "name" to profile.name,
            "email" to profile.email,
            "points" to points,
            "accuracy" to accuracy,
            "testsCount" to testsCount,
            "kcetTargetRank" to profile.kcetTargetRank,
            "lastUpdatedMillis" to now
        )
        firestore.collection("users").document(canonicalDocId).set(userUpdate, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

        // Clean up legacy empty doc at users.document(email) if email differs
        if (email.isNotBlank() && email != canonicalDocId) {
            try {
                val legacyDoc = firestore.collection("users").document(email).get().awaitResult().getOrNull()
                if (legacyDoc != null && legacyDoc.exists() && legacyDoc.getString("password") == null) {
                    firestore.collection("users").document(email).delete()
                }
            } catch (_: Exception) {}
        }

        // Also sync Realtime Database LiveXP
        try {
            val rtdbUid = canonicalDocId.replace(".", "_").replace("@", "_")
            FirebaseRealtimeService.updateUserLiveXP(
                uid = rtdbUid,
                userName = profile.name,
                xp = points,
                rank = 0,
                targetRank = profile.kcetTargetRank
            )
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 5. BADGES COLLECTION
    // -------------------------------------------------------------
    fun observeBadges(uid: String): Flow<List<BadgeItem>> = callbackFlow {
        val finalUid = uid.ifBlank { auth.currentUser?.uid ?: "guest" }
        val query = firestore.collection("badges").document(finalUid).collection("user_badges")
        val listener = query.addSnapshotListener { snapshot, error ->
            val defaultBadges = listOf(
                BadgeItem("b1", "KCET Pioneer", "Complete your first CBT mock test", "🎯", isUnlocked = false, progress = 0f),
                BadgeItem("b2", "Top Scorer", "Score 80%+ on any mock exam", "🏆", isUnlocked = false, progress = 0f),
                BadgeItem("b3", "Perfect Score", "Achieve 100% accuracy on a test", "💯", isUnlocked = false, progress = 0f),
                BadgeItem("b4", "Speed Demon", "Finish exam with < 30s per question", "⚡", isUnlocked = false, progress = 0f),
                BadgeItem("b5", "Streak Master", "Attempt 3 or more mock tests", "🔥", isUnlocked = false, progress = 0f),
                BadgeItem("b6", "Physics Wizard", "Score 80%+ in Physics section", "⚛️", isUnlocked = false, progress = 0f),
                BadgeItem("b7", "Chemistry Ace", "Score 80%+ in Chemistry section", "🧪", isUnlocked = false, progress = 0f),
                BadgeItem("b8", "Maths Genius", "Score 80%+ in Mathematics section", "📐", isUnlocked = false, progress = 0f),
                BadgeItem("b9", "Daily Warrior", "Complete 5 Daily AI Practice / Mock tests", "📅", isUnlocked = false, progress = 0f),
                BadgeItem("b10", "Bookmark Scholar", "Save & practice 10+ bookmarked questions", "🔖", isUnlocked = false, progress = 0f),
                BadgeItem("b11", "Century Club", "Attempt 100+ questions across all mock tests", "📊", isUnlocked = false, progress = 0f),
                BadgeItem("b12", "KCET Marathoner", "Attempt a full 180-question PCM mock exam", "🏃", isUnlocked = false, progress = 0f),
                BadgeItem("b13", "Accuracy Elite", "Score 90%+ with under 40s per question", "🎯", isUnlocked = false, progress = 0f),
                // 2 VERY DIFFICULT / ULTRA-RARE ELITE BADGES
                BadgeItem("b14", "KCET Centurion Legend", "Score 160+/180 on a full 180-Question Mock Test with >88% accuracy", "👑", isUnlocked = false, progress = 0f, category = "Elite Challenge", isVeryDifficult = true),
                BadgeItem("b15", "State Rank #1 Grandmaster", "Accumulate 2,500+ XP points and complete 10+ full CBT mock exams", "🌟", isUnlocked = false, progress = 0f, category = "Elite Challenge", isVeryDifficult = true)
            )

            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(defaultBadges)
                return@addSnapshotListener
            }

            val savedBadgesMap = snapshot.documents.associateBy { it.id }
            val mergedBadges = defaultBadges.map { badge ->
                val doc = savedBadgesMap[badge.id]
                if (doc != null) {
                    badge.copy(
                        isUnlocked = doc.getBoolean("isUnlocked") ?: badge.isUnlocked,
                        unlockedDate = doc.getString("unlockedDate") ?: badge.unlockedDate,
                        progress = (doc.getDouble("progress") ?: badge.progress.toDouble()).toFloat()
                    )
                } else {
                    badge
                }
            }
            trySend(mergedBadges)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveBadge(uid: String, badge: BadgeItem) {
        val finalUid = uid.ifBlank { auth.currentUser?.uid ?: "" }
        if (finalUid.isBlank()) return
        val docMap = mapOf(
            "id" to badge.id,
            "title" to badge.title,
            "description" to badge.description,
            "iconEmoji" to badge.iconEmoji,
            "isUnlocked" to badge.isUnlocked,
            "unlockedDate" to (badge.unlockedDate ?: ""),
            "progress" to badge.progress,
            "category" to badge.category,
            "isVeryDifficult" to badge.isVeryDifficult
        )
        firestore.collection("badges").document(finalUid)
            .collection("user_badges").document(badge.id).set(docMap).awaitResult()
    }

    // -------------------------------------------------------------
    // 6. RESOURCES & ANNOUNCEMENTS COLLECTIONS
    // -------------------------------------------------------------
    fun observeResources(): Flow<List<ResourceItem>> = callbackFlow {
        val query = firestore.collection("resources")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleResources)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                ResourceItem(
                    id = doc.getString("id") ?: doc.id,
                    title = doc.getString("title") ?: "",
                    category = doc.getString("category") ?: "Notes",
                    fileFormat = doc.getString("fileFormat") ?: "PDF",
                    fileSize = doc.getString("fileSize") ?: "1.5 MB",
                    yearOrSubject = doc.getString("yearOrSubject") ?: "KCET 2024",
                    downloadUrl = doc.getString("downloadUrl") ?: "",
                    description = doc.getString("description") ?: ""
                )
            }
            trySend(if (items.isNotEmpty()) items else SampleData.sampleResources)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveResource(resource: ResourceItem) {
        val docMap = mapOf(
            "id" to resource.id,
            "title" to resource.title,
            "category" to resource.category,
            "fileFormat" to resource.fileFormat,
            "fileSize" to resource.fileSize,
            "yearOrSubject" to resource.yearOrSubject,
            "downloadUrl" to resource.downloadUrl,
            "description" to resource.description
        )
        firestore.collection("resources").document(resource.id).set(docMap).awaitResult()
    }

    suspend fun deleteResource(id: String) {
        firestore.collection("resources").document(id).delete().awaitResult()
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val query = firestore.collection("announcements")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleAnnouncements)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                Announcement(
                    id = doc.getString("id") ?: doc.id,
                    title = doc.getString("title") ?: "",
                    date = doc.getString("date") ?: "",
                    message = doc.getString("message") ?: "",
                    isImportant = doc.getBoolean("isImportant") ?: false
                )
            }
            trySend(if (items.isNotEmpty()) items else SampleData.sampleAnnouncements)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveAnnouncement(announcement: Announcement) {
        val docMap = mapOf(
            "id" to announcement.id,
            "title" to announcement.title,
            "date" to announcement.date,
            "message" to announcement.message,
            "isImportant" to announcement.isImportant,
            "createdAtMillis" to System.currentTimeMillis()
        )
        firestore.collection("announcements").document(announcement.id).set(docMap).awaitResult()
    }

    suspend fun deleteAnnouncement(id: String) {
        firestore.collection("announcements").document(id).delete().awaitResult()
        // Also remove linked notification if any
        firestore.collection("notifications").document("notif_$id").delete().awaitResult()
    }

    // -------------------------------------------------------------
    // 6.1 NOTIFICATIONS COLLECTION (Real-time synced across all students)
    // -------------------------------------------------------------
    fun observeNotifications(): Flow<List<StudentNotification>> = callbackFlow {
        val query = firestore.collection("notifications")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.defaultNotifications)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    StudentNotification(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getString("timestamp") ?: "Just now",
                        type = doc.getString("type") ?: "GENERAL",
                        isRead = doc.getBoolean("isRead") ?: false,
                        actionType = doc.getString("actionType"),
                        targetUserId = doc.getString("targetUserId"),
                        targetUserEmail = doc.getString("targetUserEmail"),
                        createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.createdAtMillis }
            trySend(if (items.isNotEmpty()) items else SampleData.defaultNotifications)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveNotification(notification: StudentNotification) {
        val id = notification.id.ifBlank { "notif_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "id" to id,
            "title" to notification.title,
            "message" to notification.message,
            "timestamp" to notification.timestamp,
            "type" to notification.type,
            "isRead" to notification.isRead,
            "actionType" to (notification.actionType ?: ""),
            "targetUserId" to (notification.targetUserId ?: "ALL"),
            "targetUserEmail" to (notification.targetUserEmail ?: ""),
            "createdAtMillis" to notification.createdAtMillis
        )
        firestore.collection("notifications").document(id).set(docMap).awaitResult()
    }

    suspend fun deleteNotification(id: String) {
        deleteNotificationPermanentlyByAdmin(id)
    }

    suspend fun deleteNotificationPermanentlyByAdmin(id: String) {
        try {
            // 1. Delete from Firestore notifications collection
            firestore.collection("notifications").document(id).delete().awaitResult()
            // 2. Mark in globally_deleted_notifications collection in Firestore
            val docMap = mapOf(
                "id" to id,
                "deletedBy" to "admin",
                "deletedAtMillis" to System.currentTimeMillis()
            )
            firestore.collection("globally_deleted_notifications").document(id).set(docMap).awaitResult()
            // 3. Remove in RTDB and record globally deleted node
            FirebaseRealtimeService.deleteNotificationPermanently(id)
            Log.d(TAG, "Notification $id permanently deleted by admin across all student portals")
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteNotificationPermanentlyByAdmin: ${e.message}")
        }
    }

    fun observeGloballyDeletedNotificationIds(): Flow<Set<String>> = callbackFlow {
        val query = firestore.collection("globally_deleted_notifications")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptySet())
                return@addSnapshotListener
            }
            val ids = snapshot.documents.map { it.id }.toSet()
            trySend(ids)
        }
        awaitClose { listener.remove() }
    }

    suspend fun cleanupScheduleSpamNotifications() {
        try {
            val query = firestore.collection("notifications").get().awaitResult()
            if (query.isSuccess) {
                val docs = query.getOrNull()?.documents ?: emptyList()
                for (doc in docs) {
                    val title = doc.getString("title") ?: ""
                    val msg = doc.getString("message") ?: ""
                    if (title.contains("Official KEA Schedule", ignoreCase = true) ||
                        title.contains("Official KCET Schedule", ignoreCase = true) ||
                        title.contains("Schedule Updated", ignoreCase = true) ||
                        (msg.contains("cetonline.karnataka.gov.in", ignoreCase = true) && msg.contains("schedule", ignoreCase = true))
                    ) {
                        doc.reference.delete().awaitResult()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 7. IMPORTANT DATES & SCHEDULE COLLECTION
    // -------------------------------------------------------------
    fun observeImportantDates(): Flow<List<ImportantDateItem>> = callbackFlow {
        val query = firestore.collection("important_dates")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleImportantDates)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                ImportantDateItem(
                    id = doc.getString("id") ?: doc.id,
                    eventTitle = doc.getString("eventTitle") ?: "",
                    dateOrDeadline = doc.getString("dateOrDeadline") ?: "",
                    status = doc.getString("status") ?: "Upcoming",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "Exam",
                    isHighlighted = doc.getBoolean("isHighlighted") ?: false
                )
            }
            trySend(if (items.isNotEmpty()) items else SampleData.sampleImportantDates)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveImportantDate(item: ImportantDateItem) {
        val docMap = mapOf(
            "id" to item.id,
            "eventTitle" to item.eventTitle,
            "dateOrDeadline" to item.dateOrDeadline,
            "status" to item.status,
            "description" to item.description,
            "category" to item.category,
            "isHighlighted" to item.isHighlighted
        )
        firestore.collection("important_dates").document(item.id).set(docMap).awaitResult()
    }

    suspend fun replaceAllImportantDates(newItems: List<ImportantDateItem>) {
        try {
            val snapshot = firestore.collection("important_dates").get().awaitResult()
            snapshot.getOrNull()?.documents?.forEach { doc ->
                firestore.collection("important_dates").document(doc.id).delete().awaitResult()
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error clearing old important dates", e)
        }
        newItems.forEach { item ->
            saveImportantDate(item)
        }
    }

    suspend fun deleteImportantDate(id: String) {
        firestore.collection("important_dates").document(id).delete().awaitResult()
    }

    // -------------------------------------------------------------
    // 8. RANK VS MARKS COLLECTION (Stored in Firebase)
    // -------------------------------------------------------------
    fun observeRankVsMarks(): Flow<List<RankVsMarksItem>> = callbackFlow {
        val query = firestore.collection("rank_vs_marks").orderBy("minMarks", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(SampleData.sampleRankVsMarks)
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                RankVsMarksItem(
                    id = doc.getString("id") ?: doc.id,
                    marksRange = doc.getString("marksRange") ?: "",
                    expectedRankRange = doc.getString("expectedRankRange") ?: "",
                    year = doc.getString("year") ?: "2025",
                    description = doc.getString("description") ?: "",
                    minMarks = (doc.getLong("minMarks") ?: 0L).toInt(),
                    maxMarks = (doc.getLong("maxMarks") ?: 180L).toInt()
                )
            }
            trySend(if (items.isNotEmpty()) items else SampleData.sampleRankVsMarks)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveRankVsMarks(item: RankVsMarksItem) {
        val docMap = mapOf(
            "id" to item.id,
            "marksRange" to item.marksRange,
            "expectedRankRange" to item.expectedRankRange,
            "year" to item.year,
            "description" to item.description,
            "minMarks" to item.minMarks,
            "maxMarks" to item.maxMarks
        )
        firestore.collection("rank_vs_marks").document(item.id).set(docMap).awaitResult()
    }

    suspend fun deleteRankVsMarks(id: String) {
        firestore.collection("rank_vs_marks").document(id).delete().awaitResult()
    }

    // -------------------------------------------------------------
    // 9. SENIOR CHAT BOOKINGS COLLECTION (Stored in Firebase)
    // -------------------------------------------------------------
    fun observeSeniorChatBookings(): Flow<List<SeniorChatBooking>> = callbackFlow {
        val query = firestore.collection("senior_chat_bookings").orderBy("bookedAtTimestamp", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    SeniorChatBooking(
                        bookingId = doc.getString("bookingId") ?: doc.id,
                        userId = doc.getString("userId") ?: "kcet_std_001",
                        userName = doc.getString("userName") ?: "Karthik Gowda",
                        userEmail = doc.getString("userEmail") ?: "student@gmail.com",
                        selectedDate = doc.getString("selectedDate") ?: "",
                        selectedTime = doc.getString("selectedTime") ?: "",
                        mentorName = doc.getString("mentorName") ?: "",
                        assignedMentorEmail = doc.getString("assignedMentorEmail") ?: "",
                        instagramHandle = doc.getString("instagramHandle") ?: "",
                        supportEmail = doc.getString("supportEmail") ?: "kcetpracticepro9@gmail.com",
                        amountPaid = (doc.getLong("amountPaid") ?: 50L).toInt(),
                        paymentRefNumber = doc.getString("paymentRefNumber") ?: "",
                        receiptImageUri = doc.getString("receiptImageUri") ?: doc.getString("receiptUrl") ?: "",
                        receiptUrl = doc.getString("receiptUrl") ?: doc.getString("receiptImageUri") ?: "",
                        bookedAtTimestamp = doc.getLong("bookedAtTimestamp") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "Under Verification",
                        adminMessage = doc.getString("adminMessage") ?: "",
                        rejectionReason = doc.getString("rejectionReason") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveSeniorChatBooking(booking: SeniorChatBooking) {
        val docMap = mapOf(
            "bookingId" to booking.bookingId,
            "userId" to booking.userId,
            "userName" to booking.userName,
            "userEmail" to booking.userEmail,
            "selectedDate" to booking.selectedDate,
            "selectedTime" to booking.selectedTime,
            "mentorName" to booking.mentorName,
            "assignedMentorEmail" to booking.assignedMentorEmail,
            "instagramHandle" to booking.instagramHandle,
            "supportEmail" to booking.supportEmail,
            "amountPaid" to booking.amountPaid,
            "paymentRefNumber" to booking.paymentRefNumber,
            "receiptImageUri" to booking.receiptImageUri,
            "receiptUrl" to booking.receiptUrl,
            "bookedAtTimestamp" to booking.bookedAtTimestamp,
            "status" to booking.status,
            "adminMessage" to booking.adminMessage,
            "rejectionReason" to booking.rejectionReason
        )
        firestore.collection("senior_chat_bookings").document(booking.bookingId).set(docMap).awaitResult()
    }

    suspend fun updateSeniorChatBookingStatus(
        bookingId: String,
        status: String,
        adminMessage: String,
        mentorName: String = "",
        assignedMentorEmail: String = "",
        rejectionReason: String = ""
    ) {
        val updateMap = mutableMapOf<String, Any>(
            "status" to status,
            "adminMessage" to adminMessage
        )
        if (mentorName.isNotBlank()) {
            updateMap["mentorName"] = mentorName
        }
        if (assignedMentorEmail.isNotBlank()) {
            updateMap["assignedMentorEmail"] = assignedMentorEmail
        }
        if (rejectionReason.isNotBlank()) {
            updateMap["rejectionReason"] = rejectionReason
        }
        firestore.collection("senior_chat_bookings").document(bookingId).update(updateMap).awaitResult()
    }

    suspend fun rescheduleSeniorChatBooking(bookingId: String, newDate: String, newTime: String) {
        val updateMap = mapOf(
            "selectedDate" to newDate,
            "selectedTime" to newTime,
            "status" to "Waiting for Senior Confirmation",
            "adminMessage" to "Slot re-selected by student ($newDate $newTime)"
        )
        firestore.collection("senior_chat_bookings").document(bookingId).update(updateMap).awaitResult()
    }

    suspend fun deleteSeniorChatBooking(bookingId: String) {
        firestore.collection("senior_chat_bookings").document(bookingId).delete().awaitResult()
    }

    // ------------------ HELPLINES FIRESTORE ------------------
    fun observeHelplines(): Flow<List<HelplineItem>> = callbackFlow {
        val listener = firestore.collection("helplines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(SampleData.defaultHelplines)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        HelplineItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            contactValue = doc.getString("contactValue") ?: "",
                            type = doc.getString("type") ?: "EMAIL",
                            description = doc.getString("description") ?: "",
                            actionUrl = doc.getString("actionUrl") ?: "",
                            isHighlighted = doc.getBoolean("isHighlighted") ?: true
                        )
                    }
                    trySend(list)
                } else {
                    trySend(SampleData.defaultHelplines)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveHelpline(item: HelplineItem) {
        val id = item.id.ifBlank { "h_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "id" to id,
            "title" to item.title,
            "contactValue" to item.contactValue,
            "type" to item.type,
            "description" to item.description,
            "actionUrl" to item.actionUrl,
            "isHighlighted" to item.isHighlighted
        )
        firestore.collection("helplines").document(id).set(docMap).awaitResult()
    }

    suspend fun deleteHelpline(id: String) {
        firestore.collection("helplines").document(id).delete().awaitResult()
    }

    suspend fun seedResourcesAndAnnouncementsIfEmpty() {
        try {
            val resQuery = firestore.collection("resources").get().awaitResult()
            if (resQuery.isSuccess && (resQuery.getOrNull()?.isEmpty == true)) {
                SampleData.sampleResources.forEach { saveResource(it) }
            }

            val annQuery = firestore.collection("announcements").get().awaitResult()
            if (annQuery.isSuccess && (annQuery.getOrNull()?.isEmpty == true)) {
                SampleData.sampleAnnouncements.forEach { saveAnnouncement(it) }
            }

            val datesQuery = firestore.collection("important_dates").get().awaitResult()
            if (datesQuery.isSuccess && (datesQuery.getOrNull()?.isEmpty == true)) {
                SampleData.sampleImportantDates.forEach { saveImportantDate(it) }
            }

            val rankQuery = firestore.collection("rank_vs_marks").get().awaitResult()
            if (rankQuery.isSuccess && (rankQuery.getOrNull()?.isEmpty == true)) {
                SampleData.sampleRankVsMarks.forEach { saveRankVsMarks(it) }
            }

            val helpQuery = firestore.collection("helplines").get().awaitResult()
            if (helpQuery.isSuccess && (helpQuery.getOrNull()?.isEmpty == true)) {
                SampleData.defaultHelplines.forEach { saveHelpline(it) }
            }

            // Do not seed static notifications to Firestore so notifications stay clean and dynamic
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding resources/announcements/important dates/rank vs marks/helplines: ${e.message}")
        }
    }

    // ------------------ PAYMENT RECEIPTS & PREMIUM VERIFICATION ------------------
    fun observePaymentReceipts(): Flow<List<PaymentReceipt>> = callbackFlow {
        val query = firestore.collection("payment_receipts").orderBy("submittedAtTimestamp", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    PaymentReceipt(
                        receiptId = doc.getString("receiptId") ?: doc.id,
                        userId = doc.getString("userId") ?: "kcet_std_001",
                        userName = doc.getString("userName") ?: "Karthik Gowda",
                        userEmail = doc.getString("userEmail") ?: "student@gmail.com",
                        userPhone = doc.getString("userPhone") ?: "",
                        amountPaid = (doc.getLong("amountPaid") ?: 299L).toInt(),
                        transactionRef = doc.getString("transactionRef") ?: "",
                        receiptImageUri = doc.getString("receiptImageUri") ?: "",
                        submittedAtTimestamp = doc.getLong("submittedAtTimestamp") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "UNDER_VERIFICATION",
                        rejectionReason = doc.getString("rejectionReason") ?: "",
                        verifiedAtTimestamp = doc.getLong("verifiedAtTimestamp")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun observeUserPaymentReceipt(userId: String): Flow<PaymentReceipt?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val query = firestore.collection("payment_receipts").whereEqualTo("userId", userId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                trySend(null)
                return@addSnapshotListener
            }
            // Get latest receipt
            val doc = snapshot.documents.maxByOrNull { it.getLong("submittedAtTimestamp") ?: 0L }
            if (doc != null) {
                val receipt = PaymentReceipt(
                    receiptId = doc.getString("receiptId") ?: doc.id,
                    userId = doc.getString("userId") ?: userId,
                    userName = doc.getString("userName") ?: "Karthik Gowda",
                    userEmail = doc.getString("userEmail") ?: "",
                    userPhone = doc.getString("userPhone") ?: "",
                    amountPaid = (doc.getLong("amountPaid") ?: 299L).toInt(),
                    transactionRef = doc.getString("transactionRef") ?: "",
                    receiptImageUri = doc.getString("receiptImageUri") ?: "",
                    submittedAtTimestamp = doc.getLong("submittedAtTimestamp") ?: System.currentTimeMillis(),
                    status = doc.getString("status") ?: "UNDER_VERIFICATION",
                    rejectionReason = doc.getString("rejectionReason") ?: "",
                    verifiedAtTimestamp = doc.getLong("verifiedAtTimestamp")
                )
                trySend(receipt)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun submitPaymentReceipt(receipt: PaymentReceipt): Boolean {
        val id = receipt.receiptId.ifBlank { "REC_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "receiptId" to id,
            "userId" to receipt.userId,
            "userName" to receipt.userName,
            "userEmail" to receipt.userEmail,
            "userPhone" to receipt.userPhone,
            "amountPaid" to receipt.amountPaid,
            "transactionRef" to receipt.transactionRef,
            "receiptImageUri" to receipt.receiptImageUri,
            "submittedAtTimestamp" to receipt.submittedAtTimestamp,
            "status" to "UNDER_VERIFICATION",
            "rejectionReason" to "",
            "verifiedAtTimestamp" to null
        )
        val res = firestore.collection("payment_receipts").document(id).set(docMap).awaitResult()
        return res.isSuccess
    }

    suspend fun updatePaymentReceiptStatus(
        receiptId: String,
        userId: String,
        status: String, // VERIFIED or REJECTED
        rejectionReason: String = ""
    ) {
        val isVerified = status == "VERIFIED"
        val updateMap = mutableMapOf<String, Any>(
            "status" to status,
            "rejectionReason" to rejectionReason
        )
        if (isVerified) {
            updateMap["verifiedAtTimestamp"] = System.currentTimeMillis()
        }
        firestore.collection("payment_receipts").document(receiptId).update(updateMap).awaitResult()

        if (isVerified && userId.isNotBlank()) {
            // Permanently unlock premium status in user profile!
            val userUpdate = mapOf("isPremium" to true)
            firestore.collection("users").document(userId).set(userUpdate, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
        }
    }

    // ------------------ COLLEGES & BRANCH EXPLORER FIRESTORE ------------------
    private fun extractInt(value: Any?): Int? = when (value) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }

    private fun extractLong(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is String -> value.trim().toLongOrNull()
        else -> null
    }

    private fun extractFloat(value: Any?): Float? = when (value) {
        is Number -> value.toFloat()
        is String -> value.trim().toFloatOrNull()
        else -> null
    }

    private fun extractDouble(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }

    private fun getAnyInt(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): Int? {
        for (name in fieldNames) {
            val v = extractInt(doc.get(name))
            if (v != null) return v
        }
        return null
    }

    private fun getAnyString(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): String? {
        for (name in fieldNames) {
            val v = doc.getString(name) ?: doc.get(name)?.toString()
            if (!v.isNullOrBlank()) return v.trim()
        }
        return null
    }

    fun observeColleges(): Flow<List<CollegeInfo>> = callbackFlow {
        var firestoreColleges = listOf<CollegeInfo>()
        var deletedIds = setOf<String>()

        fun emitCombined() {
            val baseList = CollegeSampleData.sampleColleges.filter { it.id !in deletedIds }
            val map = LinkedHashMap<String, CollegeInfo>()

            // Index base sample colleges
            baseList.forEach { col ->
                val key = col.id.ifBlank { col.name.trim().lowercase() }
                map[key] = col
            }

            // Put firestore colleges, replacing matching sample college by ID or Name
            firestoreColleges.filter { it.id !in deletedIds }.forEach { col ->
                val matchingKey = map.keys.find { key ->
                    val existing = map[key] ?: return@find false
                    existing.id.equals(col.id, ignoreCase = true) ||
                    existing.name.trim().equals(col.name.trim(), ignoreCase = true) ||
                    (col.code.isNotBlank() && existing.code.equals(col.code, ignoreCase = true))
                }
                if (matchingKey != null) {
                    map.remove(matchingKey)
                }
                val finalKey = col.id.ifBlank { col.name.trim().lowercase() }
                map[finalKey] = col
            }

            trySend(map.values.toList())
        }

        // Emit initial data immediately so UI never lags or shows empty state
        emitCombined()

        val delListener = firestore.collection("deleted_colleges")
            .addSnapshotListener { snap, _ ->
                deletedIds = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                emitCombined()
            }

        val colListener = firestore.collection("colleges")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    firestoreColleges = snapshot.documents.mapNotNull { doc ->
                        try {
                            CollegeInfo(
                                id = doc.id,
                                name = getAnyString(doc, "name", "collegeName", "college_name") ?: "",
                                code = getAnyString(doc, "code", "collegeCode", "college_code") ?: "",
                                district = getAnyString(doc, "district", "place", "city") ?: "Bengaluru Urban",
                                collegeType = getAnyString(doc, "collegeType", "college_type", "type") ?: "Autonomous",
                                imageResUrl = getAnyString(doc, "imageResUrl", "image_url", "image", "banner") ?: "",
                                logoResUrl = getAnyString(doc, "logoResUrl", "logo_url", "logo") ?: "",
                                naacGrade = getAnyString(doc, "naacGrade", "naac_grade", "naac") ?: "A++",
                                nirfRank = extractInt(doc.get("nirfRank") ?: doc.get("nirf_rank")) ?: 50,
                                tuitionFeePerYear = extractInt(doc.get("tuitionFeePerYear") ?: doc.get("tuition_fee")) ?: 105000,
                                hostelFeePerYear = extractInt(doc.get("hostelFeePerYear") ?: doc.get("hostel_fee")) ?: 85000,
                                highestPackageLpa = extractFloat(doc.get("highestPackageLpa") ?: doc.get("highest_package")) ?: 58.0f,
                                avgPackageLpa = extractFloat(doc.get("avgPackageLpa") ?: doc.get("avg_package")) ?: 14.5f,
                                placementPercentage = extractInt(doc.get("placementPercentage") ?: doc.get("placement_percentage")) ?: 94,
                                topRecruiters = (doc.get("topRecruiters") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Microsoft", "Google", "Amazon"),
                                studentRating = extractFloat(doc.get("studentRating") ?: doc.get("rating")) ?: 4.8f,
                                hasHostel = doc.getBoolean("hasHostel") ?: (doc.get("hasHostel")?.toString()?.toBooleanStrictOrNull()) ?: true,
                                websiteUrl = getAnyString(doc, "websiteUrl", "website_url", "website") ?: "",
                                mapLocationQuery = getAnyString(doc, "mapLocationQuery", "map_location_query", "mapLocation", "location") ?: "",
                                distanceKmFromBlr = extractInt(doc.get("distanceKmFromBlr") ?: doc.get("distance")) ?: 12,
                                brochureUrl = getAnyString(doc, "brochureUrl", "brochure_url", "brochure") ?: "",
                                cutoffRank = getAnyInt(doc, "cutoffRank", "cutoff_rank", "cutoff", "gmCutoff", "gm_cutoff", "gmRank", "rank") ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                emitCombined()
            }

        awaitClose {
            delListener.remove()
            colListener.remove()
        }
    }

    suspend fun saveCollege(college: CollegeInfo) {
        val id = college.id.ifBlank { "col_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "id" to id,
            "name" to college.name,
            "code" to college.code,
            "district" to college.district,
            "collegeType" to college.collegeType,
            "imageResUrl" to college.imageResUrl,
            "logoResUrl" to college.logoResUrl,
            "naacGrade" to college.naacGrade,
            "nirfRank" to college.nirfRank,
            "tuitionFeePerYear" to college.tuitionFeePerYear,
            "hostelFeePerYear" to college.hostelFeePerYear,
            "highestPackageLpa" to college.highestPackageLpa,
            "avgPackageLpa" to college.avgPackageLpa,
            "placementPercentage" to college.placementPercentage,
            "topRecruiters" to college.topRecruiters,
            "studentRating" to college.studentRating,
            "hasHostel" to college.hasHostel,
            "websiteUrl" to college.websiteUrl,
            "mapLocationQuery" to college.mapLocationQuery,
            "distanceKmFromBlr" to college.distanceKmFromBlr,
            "brochureUrl" to college.brochureUrl,
            "cutoffRank" to college.cutoffRank
        )
        firestore.collection("colleges").document(id).set(docMap).awaitResult()
        // If it was previously marked deleted, unmark it
        try {
            firestore.collection("deleted_colleges").document(id).delete().awaitResult()
        } catch (_: Exception) {}
    }

    suspend fun deleteCollege(id: String) {
        firestore.collection("colleges").document(id).delete().awaitResult()
        try {
            firestore.collection("deleted_colleges").document(id).set(mapOf("deletedAt" to System.currentTimeMillis())).awaitResult()
        } catch (_: Exception) {}
    }

    suspend fun fetchCollegesOnce(): List<CollegeInfo> {
        return try {
            val snapshot = firestore.collection("colleges").get().awaitResult().getOrNull()
            if (snapshot != null) {
                snapshot.documents.mapNotNull { doc ->
                    try {
                        CollegeInfo(
                            id = doc.id,
                            name = getAnyString(doc, "name", "collegeName", "college_name") ?: "",
                            code = getAnyString(doc, "code", "collegeCode", "college_code") ?: "",
                            district = getAnyString(doc, "district", "place", "city") ?: "Bengaluru Urban",
                            collegeType = getAnyString(doc, "collegeType", "college_type", "type") ?: "Autonomous",
                            imageResUrl = getAnyString(doc, "imageResUrl", "image_url", "image", "banner") ?: "",
                            logoResUrl = getAnyString(doc, "logoResUrl", "logo_url", "logo") ?: "",
                            naacGrade = getAnyString(doc, "naacGrade", "naac_grade", "naac") ?: "A++",
                            nirfRank = extractInt(doc.get("nirfRank") ?: doc.get("nirf_rank")) ?: 50,
                            tuitionFeePerYear = extractInt(doc.get("tuitionFeePerYear") ?: doc.get("tuition_fee")) ?: 105000,
                            hostelFeePerYear = extractInt(doc.get("hostelFeePerYear") ?: doc.get("hostel_fee")) ?: 85000,
                            highestPackageLpa = extractFloat(doc.get("highestPackageLpa") ?: doc.get("highest_package")) ?: 58.0f,
                            avgPackageLpa = extractFloat(doc.get("avgPackageLpa") ?: doc.get("avg_package")) ?: 14.5f,
                            placementPercentage = extractInt(doc.get("placementPercentage") ?: doc.get("placement_percentage")) ?: 94,
                            topRecruiters = (doc.get("topRecruiters") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Microsoft", "Google", "Amazon"),
                            studentRating = extractFloat(doc.get("studentRating") ?: doc.get("rating")) ?: 4.8f,
                            hasHostel = doc.getBoolean("hasHostel") ?: (doc.get("hasHostel")?.toString()?.toBooleanStrictOrNull()) ?: true,
                            websiteUrl = getAnyString(doc, "websiteUrl", "website_url", "website") ?: "",
                            mapLocationQuery = getAnyString(doc, "mapLocationQuery", "map_location_query", "mapLocation", "location") ?: "",
                            distanceKmFromBlr = extractInt(doc.get("distanceKmFromBlr") ?: doc.get("distance")) ?: 12,
                            brochureUrl = getAnyString(doc, "brochureUrl", "brochure_url", "brochure") ?: "",
                            cutoffRank = getAnyInt(doc, "cutoffRank", "cutoff_rank", "cutoff", "gmCutoff", "gm_cutoff", "gmRank", "rank") ?: 0
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun observeBranches(): Flow<List<EngineeringBranchInfo>> = callbackFlow {
        var firestoreBranches = listOf<EngineeringBranchInfo>()
        var deletedBranchIds = setOf<String>()

        fun emitCombined() {
            val baseList = CollegeSampleData.sampleBranches.filter { it.id !in deletedBranchIds && it.branchCode !in deletedBranchIds }
            val map = LinkedHashMap<String, EngineeringBranchInfo>()

            baseList.forEach { br ->
                map[br.branchCode.uppercase()] = br
            }

            firestoreBranches.filter { it.id !in deletedBranchIds && it.branchCode !in deletedBranchIds }.forEach { br ->
                map[br.branchCode.uppercase()] = br
            }

            trySend(map.values.toList())
        }

        emitCombined()

        val delListener = firestore.collection("deleted_branches")
            .addSnapshotListener { snap, _ ->
                deletedBranchIds = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                emitCombined()
            }

        val brListener = firestore.collection("engineering_branches")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    firestoreBranches = snapshot.documents.mapNotNull { doc ->
                        try {
                            EngineeringBranchInfo(
                                id = doc.id,
                                branchCode = doc.getString("branchCode") ?: "CSE",
                                branchName = doc.getString("branchName") ?: "",
                                iconEmoji = doc.getString("iconEmoji") ?: "💻",
                                category = doc.getString("category") ?: "Tech & AI",
                                overview = doc.getString("overview") ?: "",
                                futureDemandLevel = doc.getString("futureDemandLevel") ?: "Very High Demand 🔥",
                                futureScope = doc.getString("futureScope") ?: "",
                                careerOpportunities = (doc.get("careerOpportunities") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                higherStudiesOptions = (doc.get("higherStudiesOptions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                requiredSkills = (doc.get("requiredSkills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                avgStartingSalaryLpa = (doc.getDouble("avgStartingSalaryLpa") ?: 12.5).toFloat(),
                                topOfferingCollegesCount = (doc.getLong("topOfferingCollegesCount") ?: 180L).toInt(),
                                districtOrPlace = doc.getString("districtOrPlace") ?: "Bengaluru Urban"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                emitCombined()
            }

        awaitClose {
            delListener.remove()
            brListener.remove()
        }
    }

    suspend fun saveBranch(branch: EngineeringBranchInfo) {
        val id = branch.id.ifBlank { "br_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "id" to id,
            "branchCode" to branch.branchCode,
            "branchName" to branch.branchName,
            "iconEmoji" to branch.iconEmoji,
            "category" to branch.category,
            "overview" to branch.overview,
            "futureDemandLevel" to branch.futureDemandLevel,
            "futureScope" to branch.futureScope,
            "careerOpportunities" to branch.careerOpportunities,
            "higherStudiesOptions" to branch.higherStudiesOptions,
            "requiredSkills" to branch.requiredSkills,
            "avgStartingSalaryLpa" to branch.avgStartingSalaryLpa,
            "topOfferingCollegesCount" to branch.topOfferingCollegesCount,
            "districtOrPlace" to branch.districtOrPlace
        )
        firestore.collection("engineering_branches").document(id).set(docMap).awaitResult()
        try {
            firestore.collection("deleted_branches").document(id).delete().awaitResult()
        } catch (_: Exception) {}
    }

    suspend fun deleteBranch(id: String) {
        firestore.collection("engineering_branches").document(id).delete().awaitResult()
        try {
            firestore.collection("deleted_branches").document(id).set(mapOf("deletedAt" to System.currentTimeMillis())).awaitResult()
        } catch (_: Exception) {}
    }

    fun observeBranchCutoffs(): Flow<List<BranchCutoffInfo>> = callbackFlow {
        var firestoreCutoffs = listOf<BranchCutoffInfo>()
        var collegeLevelCutoffs = listOf<BranchCutoffInfo>()

        fun emitCombined() {
            val cutoffMap = LinkedHashMap<String, BranchCutoffInfo>()

            // 1. Index base sample cutoffs for all colleges
            CollegeSampleData.sampleBranchCutoffs.forEach { c ->
                val key = "${c.collegeId.lowercase().trim()}_${c.branchCode.uppercase().trim()}"
                cutoffMap[key] = c
            }

            // Collect all college identifier variants from live Firestore updates
            val liveCollegeKeys = mutableSetOf<String>()
            firestoreCutoffs.forEach { c ->
                val colId = c.collegeId.lowercase().trim()
                if (colId.isNotBlank()) {
                    liveCollegeKeys.add(colId)
                    liveCollegeKeys.add(colId.removePrefix("col_"))
                }
            }
            collegeLevelCutoffs.forEach { c ->
                val colId = c.collegeId.lowercase().trim()
                if (colId.isNotBlank()) {
                    liveCollegeKeys.add(colId)
                    liveCollegeKeys.add(colId.removePrefix("col_"))
                }
            }

            // 2. PURGE static sample cutoffs for any college that has live Firestore data!
            // This prevents old static sample cutoffs from shadowing live updates from Web Admin.
            val staleKeys = cutoffMap.keys.filter { key ->
                val colPrefix = key.substringBeforeLast("_")
                val cleanPrefix = colPrefix.removePrefix("col_")
                colPrefix in liveCollegeKeys || cleanPrefix in liveCollegeKeys
            }
            staleKeys.forEach { cutoffMap.remove(it) }

            // 3. Overlay cutoffs derived from colleges collection (when web admin edits cutoff directly on college document)
            collegeLevelCutoffs.forEach { c ->
                val baseColId = c.collegeId.lowercase().trim()
                val cleanColId = baseColId.removePrefix("col_")

                val cAll = c.copy(branchCode = "ALL")
                cutoffMap["${baseColId}_ALL"] = cAll
                cutoffMap["${cleanColId}_ALL"] = cAll.copy(collegeId = cleanColId)
                cutoffMap["col_${cleanColId}_ALL"] = cAll.copy(collegeId = "col_$cleanColId")

                // Overlay standard engineering branches with this live cutoff if not specifically set
                listOf("CSE", "AIML", "ECE", "ISE", "ME", "CE").forEach { branch ->
                    val bKey = "${baseColId}_$branch"
                    val branchCutoff = c.copy(branchCode = branch)
                    cutoffMap[bKey] = branchCutoff
                    cutoffMap["${cleanColId}_$branch"] = branchCutoff.copy(collegeId = cleanColId)
                    cutoffMap["col_${cleanColId}_$branch"] = branchCutoff.copy(collegeId = "col_$cleanColId")
                }
            }

            // 4. Overlay specific Firestore branch_cutoffs documents (highest priority)
            firestoreCutoffs.forEach { c ->
                val baseColId = c.collegeId.lowercase().trim()
                val cleanColId = baseColId.removePrefix("col_")
                val branch = c.branchCode.uppercase().trim()

                cutoffMap["${baseColId}_$branch"] = c
                cutoffMap["${cleanColId}_$branch"] = c.copy(collegeId = cleanColId)
                cutoffMap["col_${cleanColId}_$branch"] = c.copy(collegeId = "col_$cleanColId")

                if (branch == "ALL") {
                    listOf("CSE", "AIML", "ECE", "ISE", "ME", "CE").forEach { b ->
                        val bKey = "${baseColId}_$b"
                        if (!cutoffMap.containsKey(bKey) || cutoffMap[bKey]?.branchCode == "ALL") {
                            val bCutoff = c.copy(branchCode = b)
                            cutoffMap[bKey] = bCutoff
                            cutoffMap["${cleanColId}_$b"] = bCutoff.copy(collegeId = cleanColId)
                            cutoffMap["col_${cleanColId}_$b"] = bCutoff.copy(collegeId = "col_$cleanColId")
                        }
                    }
                }
            }

            trySend(cutoffMap.values.distinctBy { "${it.collegeId.lowercase().trim()}_${it.branchCode.uppercase().trim()}" })
        }

        emitCombined()

        // Listener 1: branch_cutoffs collection
        val branchCutoffListener = firestore.collection("branch_cutoffs")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    firestoreCutoffs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val rawColId = getAnyString(doc, "collegeId", "college_id", "college", "code", "collegeCode", "college_code", "id")
                                ?: if (doc.id.contains("_")) doc.id.substringBeforeLast("_") else doc.id
                            val collegeId = rawColId.trim()

                            val rawBranchCode = getAnyString(doc, "branchCode", "branch_code", "branch", "course")
                                ?: if (doc.id.contains("_")) doc.id.substringAfterLast("_") else "ALL"
                            val branchCode = rawBranchCode.uppercase().trim()

                            val branchName = getAnyString(doc, "branchName", "branch_name")
                                ?: when (branchCode) {
                                    "CSE" -> "Computer Science & Engineering"
                                    "AIML" -> "AI & Machine Learning"
                                    "AIDS" -> "AI & Data Science"
                                    "ISE" -> "Information Science & Engineering"
                                    "ECE" -> "Electronics & Communication Engineering"
                                    "ME" -> "Mechanical Engineering"
                                    "CE" -> "Civil Engineering"
                                    "ALL" -> "All Core Engineering Branches"
                                    else -> branchCode
                                }

                            val catMap = mutableMapOf<String, Int>()

                            // Check map fields
                            val rawMap = (doc.get("categoryCutoffs")
                                ?: doc.get("category_cutoffs")
                                ?: doc.get("cutoffs")
                                ?: doc.get("ranks")) as? Map<*, *>

                            rawMap?.forEach { (k, v) ->
                                val keyStr = k?.toString()?.uppercase()?.trim()
                                val num = extractInt(v)
                                if (!keyStr.isNullOrBlank() && num != null && num > 0) {
                                    catMap[keyStr] = num
                                }
                            }

                            // Check direct root fields
                            val gmVal = getAnyInt(doc, "GM", "gm", "cutoffRank", "cutoff_rank", "cutoff", "gmCutoff", "gm_cutoff", "gmRank", "rank")
                            if (gmVal != null && gmVal > 0 && !catMap.containsKey("GM")) {
                                catMap["GM"] = gmVal
                            }
                            getAnyInt(doc, "2A", "2a")?.let { if (it > 0 && !catMap.containsKey("2A")) catMap["2A"] = it }
                            getAnyInt(doc, "2B", "2b")?.let { if (it > 0 && !catMap.containsKey("2B")) catMap["2B"] = it }
                            getAnyInt(doc, "3A", "3a")?.let { if (it > 0 && !catMap.containsKey("3A")) catMap["3A"] = it }
                            getAnyInt(doc, "3B", "3b")?.let { if (it > 0 && !catMap.containsKey("3B")) catMap["3B"] = it }
                            getAnyInt(doc, "SC", "sc")?.let { if (it > 0 && !catMap.containsKey("SC")) catMap["SC"] = it }
                            getAnyInt(doc, "ST", "st")?.let { if (it > 0 && !catMap.containsKey("ST")) catMap["ST"] = it }

                            val finalGm = catMap["GM"] ?: gmVal ?: 5000
                            if (!catMap.containsKey("GM")) catMap["GM"] = finalGm
                            if (!catMap.containsKey("2A")) catMap["2A"] = (finalGm * 1.5).toInt()
                            if (!catMap.containsKey("2B")) catMap["2B"] = (finalGm * 1.7).toInt()
                            if (!catMap.containsKey("3A")) catMap["3A"] = (finalGm * 1.2).toInt()
                            if (!catMap.containsKey("3B")) catMap["3B"] = (finalGm * 1.25).toInt()
                            if (!catMap.containsKey("SC")) catMap["SC"] = (finalGm * 4.5).toInt().coerceAtLeast(10000)
                            if (!catMap.containsKey("ST")) catMap["ST"] = (finalGm * 5.0).toInt().coerceAtLeast(15000)

                            BranchCutoffInfo(
                                collegeId = collegeId,
                                branchCode = branchCode,
                                branchName = branchName,
                                categoryCutoffs = catMap
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                emitCombined()
            }

        // Listener 2: colleges collection (when Web App saves cutoffs directly on the college document)
        val collegeCutoffsListener = firestore.collection("colleges")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    collegeLevelCutoffs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val colId = doc.id
                            val colCode = getAnyString(doc, "code", "collegeCode", "college_code") ?: ""
                            val gmVal = getAnyInt(doc, "cutoffRank", "cutoff_rank", "cutoff", "gmCutoff", "gm_cutoff", "gmRank", "rank")

                            val catMap = mutableMapOf<String, Int>()
                            val rawMap = (doc.get("categoryCutoffs")
                                ?: doc.get("category_cutoffs")
                                ?: doc.get("cutoffs")
                                ?: doc.get("ranks")) as? Map<*, *>

                            rawMap?.forEach { (k, v) ->
                                val keyStr = k?.toString()?.uppercase()?.trim()
                                val num = extractInt(v)
                                if (!keyStr.isNullOrBlank() && num != null && num > 0) {
                                    catMap[keyStr] = num
                                }
                            }

                            if (gmVal != null && gmVal > 0 && !catMap.containsKey("GM")) {
                                catMap["GM"] = gmVal
                            }

                            if (catMap.isNotEmpty() || (gmVal != null && gmVal > 0)) {
                                val finalGm = catMap["GM"] ?: gmVal ?: 5000
                                if (!catMap.containsKey("GM")) catMap["GM"] = finalGm
                                if (!catMap.containsKey("2A")) catMap["2A"] = (finalGm * 1.5).toInt()
                                if (!catMap.containsKey("2B")) catMap["2B"] = (finalGm * 1.7).toInt()
                                if (!catMap.containsKey("3A")) catMap["3A"] = (finalGm * 1.2).toInt()
                                if (!catMap.containsKey("3B")) catMap["3B"] = (finalGm * 1.25).toInt()
                                if (!catMap.containsKey("SC")) catMap["SC"] = (finalGm * 4.5).toInt().coerceAtLeast(10000)
                                if (!catMap.containsKey("ST")) catMap["ST"] = (finalGm * 5.0).toInt().coerceAtLeast(15000)

                                BranchCutoffInfo(
                                    collegeId = colId,
                                    branchCode = "ALL",
                                    branchName = "Computer Science & All Core Branches",
                                    categoryCutoffs = catMap
                                )
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                emitCombined()
            }

        awaitClose {
            branchCutoffListener.remove()
            collegeCutoffsListener.remove()
        }
    }

    suspend fun saveBranchCutoff(cutoff: BranchCutoffInfo) {
        val cleanBranch = cutoff.branchCode.uppercase().trim()
        val docId = "${cutoff.collegeId}_${cleanBranch}".ifBlank { "cutoff_${System.currentTimeMillis()}" }
        val docMap = mapOf(
            "collegeId" to cutoff.collegeId,
            "branchCode" to cleanBranch,
            "branchName" to cutoff.branchName,
            "categoryCutoffs" to cutoff.categoryCutoffs,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection("branch_cutoffs").document(docId).set(docMap).awaitResult()

        // Also update colleges document so web or mobile clients reading colleges collection are updated!
        val gm = cutoff.categoryCutoffs["GM"]
        if (gm != null && gm > 0 && cutoff.collegeId.isNotBlank()) {
            try {
                firestore.collection("colleges").document(cutoff.collegeId)
                    .update(mapOf(
                        "cutoffRank" to gm,
                        "categoryCutoffs" to cutoff.categoryCutoffs
                    ))
                    .awaitResult()
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteBranchCutoff(collegeId: String, branchCode: String) {
        val cleanBranch = branchCode.uppercase().trim()
        val docId = "${collegeId}_${cleanBranch}"
        firestore.collection("branch_cutoffs").document(docId).delete().awaitResult()
        try {
            firestore.collection("branch_cutoffs").document("${collegeId.removePrefix("col_")}_${cleanBranch}").delete().awaitResult()
        } catch (_: Exception) {}
    }

    // ==========================================
    // ==========================================
    // SENIOR MENTORS FIRESTORE BACKUP & SYNC
    // ==========================================
    val defaultVerifiedSeniorMentors = listOf(
        SeniorMentorProfile(
            name = "Ullas Suvarna (Senior Mentor)",
            bio = "Official Senior Mentor & KCET Strategy Guide",
            specialty = "KCET Physics, Chemistry & Math Strategy",
            qualification = "Senior Mentor Guide",
            experience = "Senior Strategy Coach",
            telegramOrInstagram = "@ullas_mentor",
            email = "ullassuvarna85@gmail.com",
            phone = "",
            status = "Active"
        ),
        SeniorMentorProfile(
            name = "Senior Mentor",
            bio = "Main Admin Mentor",
            specialty = "Engineering & Medical Guidance",
            qualification = "Senior Mentor Guide",
            experience = "1+ Years Exam Strategy coaching",
            telegramOrInstagram = "@kcet_mentor",
            email = "senior.mentor@kcet.com",
            phone = "",
            status = "Active"
        )
    )

    private fun parseMentorDoc(doc: com.google.firebase.firestore.DocumentSnapshot): SeniorMentorProfile? {
        try {
            if (!doc.exists()) return null
            val data = doc.data ?: emptyMap()

            fun getStr(vararg keys: String): String {
                for (k in keys) {
                    val v = data[k] ?: try { doc.get(k) } catch (_: Exception) { null }
                    if (v != null) {
                        val s = v.toString().trim()
                        if (s.isNotBlank() && s != "null") return s
                    }
                }
                return ""
            }

            val name = getStr("name", "mentorName", "fullName", "mentor_name", "username", "title").ifBlank {
                if (doc.id.isNotBlank() && !doc.id.startsWith("mentor_")) doc.id else ""
            }
            if (name.isBlank()) return null

            val specialty = getStr("specialty", "specialization", "subject", "domain", "category", "expertise")
            val qualification = getStr("qualification", "degree", "education", "qual")
            val experience = getStr("experience", "exp", "experienceYears", "yearsOfExperience")
            val bio = getStr("bio", "description", "about", "aboutMe", "profile")
            val socials = getStr("telegramOrInstagram", "socials", "instagram", "telegram", "handle", "socialMedia", "social", "contactSocial")
            val email = getStr("email", "mail", "userEmail", "contactEmail")
            val phone = getStr("phone", "contact", "mobile", "phoneNumber", "contactPhone")
            val status = getStr("status", "state").ifBlank { "Active" }
            val password = getStr("password", "pwd")

            return SeniorMentorProfile(
                name = name,
                bio = bio,
                specialty = specialty,
                telegramOrInstagram = socials,
                password = password,
                email = email,
                phone = phone,
                qualification = qualification,
                experience = experience,
                status = status
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing mentor doc ${doc.id}: ${e.message}")
            return null
        }
    }

    fun seedDefaultVerifiedMentors() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                defaultVerifiedSeniorMentors.forEach { mentor ->
                    saveSeniorMentorToFirestore(mentor)
                }
            } catch (_: Exception) {}
        }
    }

    fun observeAllSeniorMentorsFromFirestore(): Flow<List<SeniorMentorProfile>> = callbackFlow {
        var mentorsList = emptyList<SeniorMentorProfile>()
        var regMentorsList = emptyList<SeniorMentorProfile>()
        var regMentorsCamelList = emptyList<SeniorMentorProfile>()
        var seniorMentorsList = emptyList<SeniorMentorProfile>()
        var usersMentorsList = emptyList<SeniorMentorProfile>()

        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener {}
        }

        seedDefaultVerifiedMentors()

        fun emitMerged() {
            val map = LinkedHashMap<String, SeniorMentorProfile>()

            // 1. Seed with default verified mentors
            defaultVerifiedSeniorMentors.forEach { prof ->
                if (!FirebaseRealtimeService.isMentorDeleted(prof.name)) {
                    val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                    map[key] = prof
                }
            }

            // 2. Overlay Firestore documents
            listOf(mentorsList, regMentorsList, regMentorsCamelList, seniorMentorsList, usersMentorsList).forEach { list ->
                list.forEach { prof ->
                    if (prof.name.isNotBlank() && !FirebaseRealtimeService.isMentorDeleted(prof.name)) {
                        val key = prof.email.lowercase().trim().ifBlank { prof.name.lowercase().trim() }
                        map[key] = prof
                    }
                }
            }

            val finalMentors = map.values.toList()
            trySend(finalMentors)
        }

        emitMerged()

        val listener1 = firestore.collection("mentors").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                mentorsList = snapshot.documents.mapNotNull { parseMentorDoc(it) }
            } else {
                mentorsList = emptyList()
            }
            emitMerged()
        }

        val listener2 = firestore.collection("registered_mentors").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                regMentorsList = snapshot.documents.mapNotNull { parseMentorDoc(it) }
            } else {
                regMentorsList = emptyList()
            }
            emitMerged()
        }

        val listener3 = firestore.collection("registeredMentors").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                regMentorsCamelList = snapshot.documents.mapNotNull { parseMentorDoc(it) }
            } else {
                regMentorsCamelList = emptyList()
            }
            emitMerged()
        }

        val listener4 = firestore.collection("senior_mentors").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                seniorMentorsList = snapshot.documents.mapNotNull { parseMentorDoc(it) }
            } else {
                seniorMentorsList = emptyList()
            }
            emitMerged()
        }

        val listener5 = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val list = mutableListOf<SeniorMentorProfile>()
                for (doc in snapshot.documents) {
                    val email = doc.getString("email") ?: ""
                    val name = doc.getString("name") ?: ""
                    if (email.lowercase().contains("mentor") || name.lowercase().contains("mentor") ||
                        email.lowercase().contains("senior") || name.lowercase().contains("senior")) {
                        list.add(SeniorMentorProfile(
                            name = name,
                            email = email,
                            bio = doc.getString("kcetTargetRank") ?: "Senior Mentor Guide",
                            specialty = doc.getString("targetStream") ?: "KCET Guidance",
                            qualification = "Verified Mentor",
                            experience = "Strategy Coach",
                            status = "Active"
                        ))
                    }
                }
                usersMentorsList = list
            } else {
                usersMentorsList = emptyList()
            }
            emitMerged()
        }

        awaitClose {
            listener1.remove()
            listener2.remove()
            listener3.remove()
            listener4.remove()
            listener5.remove()
        }
    }

    suspend fun saveSeniorMentorToFirestore(profile: SeniorMentorProfile) {
        val mentorId = profile.name.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" + System.currentTimeMillis() }
        val docMap = mapOf(
            "name" to profile.name,
            "mentorName" to profile.name,
            "fullName" to profile.name,
            "bio" to profile.bio,
            "description" to profile.bio,
            "specialty" to profile.specialty,
            "specialization" to profile.specialty,
            "qualification" to profile.qualification,
            "experience" to profile.experience,
            "telegramOrInstagram" to profile.telegramOrInstagram,
            "socials" to profile.telegramOrInstagram,
            "email" to profile.email,
            "phone" to profile.phone,
            "status" to profile.status,
            "password" to profile.password,
            "createdAt" to profile.createdAt,
            "lastUpdated" to System.currentTimeMillis()
        )
        val collections = listOf("mentors", "registered_mentors", "registeredMentors", "senior_mentors")
        for (col in collections) {
            try {
                firestore.collection(col).document(mentorId).set(docMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
            } catch (e: Exception) {
                Log.w(TAG, "Save mentor to $col error: ${e.message}")
            }
        }
    }

    suspend fun deleteSeniorMentorFromFirestore(mentorName: String) {
        val mentorId = mentorName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" }
        FirebaseRealtimeService.markMentorDeletedLocally(mentorName)
        val collections = listOf("mentors", "registered_mentors", "registeredMentors", "senior_mentors")
        for (col in collections) {
            try {
                firestore.collection(col).document(mentorId).delete().awaitResult()
            } catch (e: Exception) {
                Log.w(TAG, "Delete mentor from $col error: ${e.message}")
            }
        }
    }

    suspend fun updateSeniorMentorStatusInFirestore(mentorName: String, newStatus: String) {
        val mentorId = mentorName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").ifBlank { "mentor_" }
        val collections = listOf("mentors", "registered_mentors", "registeredMentors", "senior_mentors")
        for (col in collections) {
            try {
                firestore.collection(col).document(mentorId).update("status", newStatus).awaitResult()
            } catch (e: Exception) {
                Log.w(TAG, "Update mentor in $col error: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // ADMIN SECURITY PIN CONFIGURATION
    // -------------------------------------------------------------
    const val DEFAULT_ADMIN_PIN = "17082006"

    fun observeAdminPin(): Flow<String> = callbackFlow {
        val docRef = firestore.collection("admin_config").document("security")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                trySend(DEFAULT_ADMIN_PIN)
                return@addSnapshotListener
            }
            val remotePin = snapshot.getString("pin")
                ?: snapshot.get("pin")?.toString()
                ?: snapshot.getString("admin_pin")
                ?: snapshot.get("admin_pin")?.toString()
                ?: snapshot.getString("password")
            val effectivePin = if (!remotePin.isNullOrBlank()) remotePin.trim() else DEFAULT_ADMIN_PIN
            trySend(effectivePin)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAdminPin(): String {
        return try {
            val snapshot = firestore.collection("admin_config").document("security").get().awaitResult().getOrNull()
            if (snapshot != null && snapshot.exists()) {
                val remotePin = snapshot.getString("pin")
                    ?: snapshot.get("pin")?.toString()
                    ?: snapshot.getString("admin_pin")
                    ?: snapshot.get("admin_pin")?.toString()
                    ?: snapshot.getString("password")
                if (!remotePin.isNullOrBlank()) {
                    return remotePin.trim()
                }
            }
            DEFAULT_ADMIN_PIN
        } catch (e: Exception) {
            DEFAULT_ADMIN_PIN
        }
    }

    // -------------------------------------------------------------
    // ADMIN PRICING CONFIGURATION (Live Chat & Subscription Pass)
    // -------------------------------------------------------------
    data class AdminPricingConfig(
        val liveChatPrice: Int = 50,
        val subscriptionPrice: Int = 299
    )

    fun observeAdminPricing(): Flow<AdminPricingConfig> = callbackFlow {
        val docRef = firestore.collection("admin_config").document("pricing")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                trySend(AdminPricingConfig(50, 299))
                return@addSnapshotListener
            }
            val chatPrice = extractInt(snapshot.get("liveChatPrice") ?: snapshot.get("live_chat_price") ?: snapshot.get("chatPrice")) ?: 50
            val subPrice = extractInt(snapshot.get("subscriptionPrice") ?: snapshot.get("subscription_price") ?: snapshot.get("subPrice")) ?: 299
            trySend(AdminPricingConfig(chatPrice, subPrice))
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveAdminPricing(liveChatPrice: Int, subscriptionPrice: Int) {
        try {
            val docMap = mapOf(
                "liveChatPrice" to liveChatPrice,
                "subscriptionPrice" to subscriptionPrice,
                "updatedAtMillis" to System.currentTimeMillis()
            )
            firestore.collection("admin_config").document("pricing").set(docMap).awaitResult()
            FirebaseRealtimeService.saveAdminPricingToRTDB(liveChatPrice, subscriptionPrice)
            Log.d(TAG, "Admin pricing successfully saved to Firestore & RTDB: Chat=₹$liveChatPrice, Sub=₹$subscriptionPrice")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving admin pricing: ${e.message}")
        }
    }
}

