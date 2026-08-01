package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirebaseService {
    private const val TAG = "FirebaseService"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
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
    suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(Result.success(result))
            }
            addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
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
                    "authEmail" to (docSnap.getString("authEmail") ?: docSnap.getString("email") ?: "")
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
        name: String
    ): Boolean {
        val cleanEmail = email?.lowercase()?.trim() ?: ""
        val cleanPhone = phone?.trim()?.replace(" ", "")?.replace("-", "") ?: ""

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
            "registrationDate" to "2026"
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

    suspend fun updateUserPasswordInFirestore(identifier: String, newPass: String) {
        try {
            val cleanId = normalizeIdentifier(identifier)
            val updateMap = mapOf("password" to newPass.trim())

            // Fetch record to get all linked identifiers
            val record = getUserAccountRecord(cleanId)

            firestore.collection("user_accounts").document(cleanId).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()

            if (record != null) {
                val email = record["email"] ?: ""
                val phone = record["phone"] ?: ""
                if (email.isNotBlank()) {
                    firestore.collection("user_accounts").document(email.lowercase().trim()).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                }
                if (phone.isNotBlank()) {
                    val normPhone = normalizeIndianPhoneNumber(phone)
                    firestore.collection("user_accounts").document(normPhone).set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
                    firestore.collection("user_accounts").document("+91$normPhone").set(updateMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
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

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = profile.uid.ifBlank { auth.currentUser?.uid ?: "guest" }
        val userMap = mapOf(
            "uid" to uid,
            "name" to profile.name,
            "email" to profile.email,
            "photoUrl" to (profile.photoUrl ?: ""),
            "isPremium" to profile.isPremium,
            "kcetTargetRank" to profile.kcetTargetRank,
            "targetStream" to profile.targetStream,
            "registrationDate" to profile.registrationDate
        )
        val res = firestore.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
        return res.isSuccess
    }

    suspend fun getUserProfileOnce(uid: String): UserProfile? {
        if (uid.isBlank()) return null
        return try {
            val res = firestore.collection("users").document(uid).get().awaitResult()
            val doc = res.getOrNull()
            if (doc != null && doc.exists()) {
                UserProfile(
                    uid = doc.getString("uid") ?: uid,
                    name = doc.getString("name") ?: "Karthik Gowda",
                    email = doc.getString("email") ?: "",
                    photoUrl = doc.getString("photoUrl"),
                    isPremium = doc.getBoolean("isPremium") ?: false,
                    kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000",
                    targetStream = doc.getString("targetStream") ?: "Engineering (B.E / B.Tech)",
                    registrationDate = doc.getString("registrationDate") ?: "2026"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user profile: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = UserProfile(
                    uid = snapshot.getString("uid") ?: uid,
                    name = snapshot.getString("name") ?: "Karthik Gowda",
                    email = snapshot.getString("email") ?: "karthik.kcet2026@gmail.com",
                    photoUrl = snapshot.getString("photoUrl"),
                    isPremium = snapshot.getBoolean("isPremium") ?: false,
                    kcetTargetRank = snapshot.getString("kcetTargetRank") ?: "Under 1000",
                    targetStream = snapshot.getString("targetStream") ?: "Engineering (B.E / B.Tech)",
                    registrationDate = snapshot.getString("registrationDate") ?: "2026"
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
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
    suspend fun saveTestResult(result: TestResultEntity, userId: String = "") {
        val uid = userId.ifBlank { auth.currentUser?.uid ?: "guest" }
        val docMap = mapOf(
            "userId" to uid,
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
        firestore.collection("test_results").add(docMap).awaitResult()
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
                TestResultEntity(
                    id = idx + 1,
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

    // -------------------------------------------------------------
    // 4. LEADERBOARD COLLECTION
    // -------------------------------------------------------------
    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val query = firestore.collection("leaderboard").orderBy("points", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                val defaultList = listOf(
                    LeaderboardEntry(1, "Ananya Rao", "Under 100", 1450, 95.2f, 18, "AR"),
                    LeaderboardEntry(2, "Rohan Kulkarni", "Under 250", 1320, 91.0f, 15, "RK"),
                    LeaderboardEntry(3, "Deepika Bhat", "Under 350", 1180, 88.5f, 14, "DB"),
                    LeaderboardEntry(4, "Preetham Shetty", "Under 500", 1020, 84.0f, 12, "PS"),
                    LeaderboardEntry(5, "Karthik Gowda", "Under 500", 450, 75.0f, 1, "KG", isCurrentUser = true)
                )
                trySend(defaultList)
                return@addSnapshotListener
            }

            val currentUid = auth.currentUser?.uid ?: ""
            val list = snapshot.documents.mapIndexed { index, doc ->
                LeaderboardEntry(
                    rank = index + 1,
                    name = doc.getString("name") ?: "Student",
                    kcetTargetRank = doc.getString("kcetTargetRank") ?: "Under 1000",
                    points = (doc.getLong("points") ?: 0L).toInt(),
                    accuracy = (doc.getDouble("accuracy") ?: 75.0).toFloat(),
                    testsCount = (doc.getLong("testsCount") ?: 1L).toInt(),
                    avatarInitials = doc.getString("avatarInitials") ?: "ST",
                    isCurrentUser = doc.getString("uid") == currentUid
                )
            }
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateLeaderboardEntry(profile: UserProfile, points: Int, accuracy: Float, testsCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val initials = profile.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        val docMap = mapOf(
            "uid" to uid,
            "name" to profile.name,
            "kcetTargetRank" to profile.kcetTargetRank,
            "points" to points,
            "accuracy" to accuracy,
            "testsCount" to testsCount,
            "avatarInitials" to if (initials.isNotBlank()) initials else "ST"
        )
        firestore.collection("leaderboard").document(uid).set(docMap).awaitResult()
    }

    // -------------------------------------------------------------
    // 5. BADGES COLLECTION
    // -------------------------------------------------------------
    fun observeBadges(): Flow<List<BadgeItem>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: "guest"
        val query = firestore.collection("badges").document(uid).collection("user_badges")
        val listener = query.addSnapshotListener { snapshot, error ->
            val defaultBadges = listOf(
                BadgeItem("b1", "KCET Pioneer", "Complete your first CBT mock test", "🎯", isUnlocked = true, unlockedDate = "2026", progress = 1f),
                BadgeItem("b2", "Top Scorer", "Score 80%+ on any mock exam", "🏆", isUnlocked = false, progress = 0.75f),
                BadgeItem("b3", "Perfect Score", "Achieve 100% accuracy on a test", "💯", isUnlocked = false, progress = 0.4f),
                BadgeItem("b4", "Speed Demon", "Finish exam with < 30s per question", "⚡", isUnlocked = false, progress = 0.5f),
                BadgeItem("b5", "Streak Master", "Attempt 3 or more mock tests", "🔥", isUnlocked = false, progress = 0.33f),
                BadgeItem("b6", "Physics Wizard", "Score 80%+ in Physics section", "⚛️", isUnlocked = false, progress = 0.7f),
                BadgeItem("b7", "Chemistry Ace", "Score 80%+ in Chemistry section", "🧪", isUnlocked = false, progress = 0.85f),
                BadgeItem("b8", "Maths Genius", "Score 80%+ in Mathematics section", "📐", isUnlocked = false, progress = 0.65f)
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

    suspend fun saveBadge(badge: BadgeItem) {
        val uid = auth.currentUser?.uid ?: return
        val docMap = mapOf(
            "id" to badge.id,
            "title" to badge.title,
            "description" to badge.description,
            "iconEmoji" to badge.iconEmoji,
            "isUnlocked" to badge.isUnlocked,
            "unlockedDate" to (badge.unlockedDate ?: ""),
            "progress" to badge.progress,
            "category" to badge.category
        )
        firestore.collection("badges").document(uid)
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
            "isImportant" to announcement.isImportant
        )
        firestore.collection("announcements").document(announcement.id).set(docMap).awaitResult()
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
                        mentorName = doc.getString("mentorName") ?: "Senior KCET Mentor (@ULLAS_U_SUVARNA)",
                        instagramHandle = doc.getString("instagramHandle") ?: "@ULLAS_U_SUVARNA",
                        supportEmail = doc.getString("supportEmail") ?: "kcetpracticepro9@gmail.com",
                        amountPaid = (doc.getLong("amountPaid") ?: 50L).toInt(),
                        bookedAtTimestamp = doc.getLong("bookedAtTimestamp") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "Waiting for Senior Confirmation",
                        adminMessage = doc.getString("adminMessage") ?: ""
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
            "instagramHandle" to booking.instagramHandle,
            "supportEmail" to booking.supportEmail,
            "amountPaid" to booking.amountPaid,
            "bookedAtTimestamp" to booking.bookedAtTimestamp,
            "status" to booking.status,
            "adminMessage" to booking.adminMessage
        )
        firestore.collection("senior_chat_bookings").document(booking.bookingId).set(docMap).awaitResult()
    }

    suspend fun updateSeniorChatBookingStatus(bookingId: String, status: String, adminMessage: String) {
        val updateMap = mapOf(
            "status" to status,
            "adminMessage" to adminMessage
        )
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
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding resources/announcements/important dates/rank vs marks/helplines: ${e.message}")
        }
    }
}

