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
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(Result.success(result))
            }
            addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
        }

    // -------------------------------------------------------------
    // 1. USERS COLLECTION
    // -------------------------------------------------------------
    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: profile.uid.ifBlank { "guest" }
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
        val res = firestore.collection("users").document(uid).set(userMap).awaitResult()
        return res.isSuccess
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
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
                        explanation = explanation
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
            "explanation" to question.explanation
        )
        firestore.collection("questions").document(question.id.toString()).set(docMap).awaitResult()
    }

    suspend fun deleteQuestion(questionId: Int) {
        firestore.collection("questions").document(questionId.toString()).delete().awaitResult()
    }

    suspend fun seedSampleQuestionsIfEmpty() {
        try {
            val query = firestore.collection("questions").get().awaitResult()
            if (query.isSuccess && (query.getOrNull()?.isEmpty == true)) {
                SampleData.sampleQuestions.forEach { q ->
                    saveQuestion(q)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding sample questions: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // 3. TEST_RESULTS COLLECTION
    // -------------------------------------------------------------
    suspend fun saveTestResult(result: TestResultEntity) {
        val uid = auth.currentUser?.uid ?: "guest"
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

    fun observeUserTestResults(): Flow<List<TestResultEntity>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: "guest"
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
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding resources/announcements: ${e.message}")
        }
    }
}
