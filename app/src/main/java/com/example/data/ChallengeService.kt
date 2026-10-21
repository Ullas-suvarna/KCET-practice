package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object ChallengeService {
    private const val TAG = "ChallengeService"
    private val firestore: FirebaseFirestore by lazy { FirebaseService.firestore }
    
    private val _xpUpdateEvents = MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 20)
    val xpUpdateEvents: SharedFlow<Pair<String, Int>> = _xpUpdateEvents.asSharedFlow()
    
    // Configuration
    private var cachedPrepDurationMs = 3 * 3600 * 1000L

    // In-memory fallback challenge store if Firestore network is delayed
    private val localChallenges = java.util.concurrent.ConcurrentHashMap<String, ChallengeData>()
    private val localQuestionSets = java.util.concurrent.ConcurrentHashMap<String, ChallengeQuestionSet>()

    init {
        // Pre-seed 5 sample question sets into local memory
        SampleChallengeQuestionSets.initialSets.forEach { set ->
            localQuestionSets[set.setId] = set
        }
        loadPrepDuration()
    }

    private fun loadPrepDuration() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = firestore.collection("settings").document("challenge").get().await()
                if (doc.exists()) {
                    cachedPrepDurationMs = doc.getLong("prepDurationMs") ?: (3 * 3600 * 1000L)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading prep duration: ${e.message}")
            }
        }
    }

    suspend fun updatePrepDuration(hours: Long, minutes: Long): Result<Boolean> {
        return try {
            val ms = (hours * 3600 * 1000L) + (minutes * 60 * 1000L)
            firestore.collection("settings").document("challenge")
                .set(mapOf("prepDurationMs" to ms), com.google.firebase.firestore.SetOptions.merge()).await()
            cachedPrepDurationMs = ms
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPrepDuration(): Long = cachedPrepDurationMs

    // =========================================================================
    // 1. REALTIME OBSERVERS
    // =========================================================================

    fun observeStudentChallenges(userId: String): Flow<List<ChallengeData>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Initialize question sets in Firestore if missing
        CoroutineScope(Dispatchers.IO).launch {
            ensureInitialQuestionSetsInFirestore()
        }

        val listener = firestore.collection("challenges")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Observe student challenges error: ${error.message}")
                    val fallback = localChallenges.values.filter {
                        it.challengerId == userId || it.challengedId == userId
                    }.sortedByDescending { it.createdAt }
                    trySend(fallback)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChallengeData::class.java)
                    }.filter {
                        val matches = it.challengerId == userId || it.challengedId == userId
                        if (!matches) {
                            Log.d(TAG, "Challenge ${it.challengeId} does not match userId $userId (Challenger: ${it.challengerId}, Challenged: ${it.challengedId})")
                        }
                        matches
                    }

                    // Cache locally & check auto-transitions
                    val updatedList = list.map { ch ->
                        localChallenges[ch.challengeId] = ch
                        checkAndAutoTransitionChallenge(ch)
                    }.sortedByDescending { it.createdAt }
                    
                    Log.d(TAG, "Observe student challenges: Found ${updatedList.size} challenges for userId $userId")

                    trySend(updatedList)
                } else {
                    trySend(emptyList())
                }
            }

        // Active 1-second background ticker to immediately trigger prep expiry, join deadlines, and 10s start countdowns
        val tickerJob = launch {
            while (isActive) {
                delay(1000L)
                val myChallenges = localChallenges.values.filter {
                    it.challengerId == userId || it.challengedId == userId
                }
                var hasChanges = false
                val transitioned = myChallenges.map { ch ->
                    val after = checkAndAutoTransitionChallenge(ch)
                    if (after.status != ch.status || after.challengerJoined != ch.challengerJoined || after.challengedJoined != ch.challengedJoined) {
                        hasChanges = true
                    }
                    after
                }
                if (hasChanges) {
                    trySend(transitioned.sortedByDescending { it.createdAt })
                }
            }
        }

        awaitClose {
            listener.remove()
            tickerJob.cancel()
        }
    }

    fun observeAllChallengesForAdmin(): Flow<List<ChallengeData>> = callbackFlow {
        val listener = firestore.collection("challenges")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Observe admin challenges error: ${error.message}")
                    trySend(localChallenges.values.sortedByDescending { it.createdAt })
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChallengeData::class.java)
                    }.map { ch ->
                        localChallenges[ch.challengeId] = ch
                        checkAndAutoTransitionChallenge(ch)
                    }.sortedByDescending { it.createdAt }

                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    fun observeQuestionSets(): Flow<List<ChallengeQuestionSet>> = callbackFlow {
        val listener = firestore.collection("challengeQuestionSets")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(localQuestionSets.values.sortedBy { it.setId })
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChallengeQuestionSet::class.java)
                }
                list.forEach { localQuestionSets[it.setId] = it }
                trySend(if (list.isNotEmpty()) list.sortedBy { it.setId } else localQuestionSets.values.sortedBy { it.setId })
            }

        awaitClose { listener.remove() }
    }

    // =========================================================================
    // 2. AUTO TIMEOUT & STATE TRANSITIONS
    // =========================================================================

    private fun checkAndAutoTransitionChallenge(ch: ChallengeData): ChallengeData {
        if (ch.paused) return ch
        val now = System.currentTimeMillis()

        // 1. 24-hr acceptance timeout
        if (ch.status == "PENDING" && now >= ch.acceptDeadline) {
            val expiredCh = ch.copy(status = "EXPIRED")
            localChallenges[ch.challengeId] = expiredCh
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("challenges").document(ch.challengeId)
                        .update("status", "EXPIRED")
                    sendNotification(ch.challengerId, "⏰ Challenge Expired", "Your 1v1 challenge to ${ch.challengedName} expired (no response in 24 hours).")
                    sendNotification(ch.challengedId, "⏰ Challenge Expired", "Challenge from ${ch.challengerName} expired because 24 hours elapsed.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting EXPIRED: ${e.message}")
                }
            }
            return expiredCh
        }

        // 2. PREPARING -> JOINING
        if (ch.status == "PREPARING" && ch.prepStartTime != null && now >= ch.prepStartTime + ch.prepDurationMs) {
            val joiningCh = ch.copy(status = "JOINING", joinStartTime = now)
            localChallenges[ch.challengeId] = joiningCh
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("challenges").document(ch.challengeId)
                        .update(mapOf("status" to "JOINING", "joinStartTime" to now))
                    sendNotification(ch.challengerId, "⚠️ IMPORTANT: Preparation Ended - Test Starting Soon!", "Preparation time has ended! Both students must join the challenge now.")
                    sendNotification(ch.challengedId, "⚠️ IMPORTANT: Preparation Ended - Test Starting Soon!", "Preparation time has ended! Both students must join the challenge now.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error transitioning to JOINING: ${e.message}")
                }
            }
            return joiningCh
        }

        // 3. JOINING deadline check (after join window passes, e.g. 5 minutes)
        if (ch.status == "JOINING" && ch.joinStartTime != null && now >= ch.joinStartTime + (5 * 60 * 1000L)) {
            if (!(ch.challengerJoined && ch.challengedJoined)) {
                val resolvedCh = if (ch.challengerJoined && !ch.challengedJoined) {
                    // Challenger joined, Challenged did not -> Challenger wins by default walkover
                    ch.copy(
                        status = "COMPLETED",
                        winnerId = ch.challengerId,
                        loserId = ch.challengedId,
                        terminationReason = "${ch.challengedName} did not join the challenge in time. ${ch.challengerName} wins by walkover!",
                        xpTransactionStatus = if (ch.isXpChallenge) "REWARD_PAID" else "NONE"
                    )
                } else if (!ch.challengerJoined && ch.challengedJoined) {
                    // Challenged joined, Challenger did not -> Challenged wins by default walkover
                    ch.copy(
                        status = "COMPLETED",
                        winnerId = ch.challengedId,
                        loserId = ch.challengerId,
                        terminationReason = "${ch.challengerName} did not join the challenge in time. ${ch.challengedName} wins by walkover!",
                        xpTransactionStatus = if (ch.isXpChallenge) "REWARD_PAID" else "NONE"
                    )
                } else {
                    // Neither joined -> TERMINATED
                    ch.copy(
                        status = "TERMINATED",
                        terminationReason = "Neither student joined the challenge in time.",
                        xpTransactionStatus = if (ch.isXpChallenge) "REFUNDED" else "NONE"
                    )
                }

                localChallenges[ch.challengeId] = resolvedCh
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("challenges").document(ch.challengeId).set(resolvedCh).await()
                        if (resolvedCh.winnerId != null) {
                            sendNotification(resolvedCh.winnerId!!, "🏆 You Won the Challenge!", "Opponent failed to join in time. You won by default walkover!")
                            if (ch.isXpChallenge && resolvedCh.loserId != null) {
                                payoutWinnerAndDeductLoserXp(resolvedCh, resolvedCh.winnerId!!, resolvedCh.loserId!!)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating expired JOINING challenge: ${e.message}")
                    }
                }
                return resolvedCh
            }
        }
        
        // 4. READY_TO_START -> IN_PROGRESS (10s countdown)
        if (ch.status == "READY_TO_START" && ch.readyStartTime != null && now >= ch.readyStartTime + (10 * 1000L)) {
            val startingCh = ch.copy(status = "IN_PROGRESS", quizStartTime = now)
            localChallenges[ch.challengeId] = startingCh
            CoroutineScope(Dispatchers.IO).launch {
                firestore.collection("challenges").document(ch.challengeId)
                    .update(mapOf("status" to "IN_PROGRESS", "quizStartTime" to now))
            }
            return startingCh
        }

        return ch
    }

    // =========================================================================
    // 3. STUDENT ACTIONS
    // =========================================================================

    suspend fun createChallenge(
        challenger: UserProfile,
        challengerRank: Int,
        challengerBadge: String,
        challenged: LeaderboardEntry,
        challengeType: ChallengeType,
        challengerCurrentXp: Int
    ): Result<ChallengeData> {
        return try {
            val isXp = challengeType == ChallengeType.XP
            if (isXp && challengerCurrentXp < 200) {
                return Result.failure(Exception("You need at least 200 XP to send an XP Challenge."))
            }

            val now = System.currentTimeMillis()
            val challengeId = "CHL-" + (100000..999999).random()

            // Select an active question set
            val activeSets = localQuestionSets.values.filter { it.isActive && it.questions.size == 20 }
            val selectedSet = if (activeSets.isNotEmpty()) activeSets.random() else SampleChallengeQuestionSets.initialSets.first()

            val challenge = ChallengeData(
                challengeId = challengeId,
                challengerId = if (challenger.uid.isNotBlank()) challenger.uid else "user_" + challenger.email.replace(".", "_"),
                challengerName = challenger.name,
                challengerPhoto = challenger.photoUrl,
                challengerRank = challengerRank,
                challengerBadge = challengerBadge,

                challengedId = if (challenged.uid.isNotBlank()) challenged.uid else "user_" + challenged.email.replace(".", "_"),
                challengedName = challenged.name,
                challengedPhoto = null,
                challengedRank = challenged.rank,
                challengedBadge = com.example.ui.components.getLeaderboardBadgeInfo(challenged.rank)?.title ?: "🏅 Student",

                challengeType = if (isXp) "XP" else "FRIENDLY",
                entryXP = if (isXp) 200 else 0,
                prizeXP = if (isXp) 400 else 0,

                status = "PENDING",
                questionSetId = selectedSet.setId,
                questionIds = selectedSet.questions.map { it.id },

                createdAt = now,
                acceptDeadline = now + (24 * 3600 * 1000L),
                prepDurationMs = getPrepDuration(),
                pauseType = "ACCEPTANCE"
            )
            Log.d(TAG, "Creating challenge ${challenge.challengeId}: Challenger: ${challenge.challengerId}, Challenged: ${challenge.challengedId}")
            localChallenges[challengeId] = challenge

            firestore.collection("challenges").document(challengeId)
                .set(challenge).await()

            sendNotification(
                userId = challenge.challengedId,
                title = "⚠️ IMPORTANT: New 1v1 Challenge Received!",
                message = "${challenger.name} challenged you to a ${if (isXp) "200 XP" else "Friendly"} 1v1 Challenge! Action required."
            )

            Result.success(challenge)
        } catch (e: Exception) {
            Log.e(TAG, "Create challenge error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun acceptChallenge(challengeId: String, currentUserId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
            if (ch == null) return Result.failure(Exception("Challenge not found."))

            if (ch.status != "PENDING") {
                return Result.failure(Exception("Challenge is no longer pending."))
            }

            val now = System.currentTimeMillis()

            val updated = ch.copy(
                status = "PREPARING",
                prepStartTime = now,
                pauseType = "QUIZ_START",
                xpTransactionStatus = if (ch.isXpChallenge) "PENDING" else "NONE"
            )

            localChallenges[challengeId] = updated
            docRef.set(updated).await()

            sendNotification(
                userId = ch.challengerId,
                title = "✅ Challenge Accepted!",
                message = "${ch.challengedName} accepted your challenge! Preparation timer started."
            )

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Accept challenge error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun rejectChallenge(challengeId: String, currentUserId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
            if (ch == null) return Result.failure(Exception("Challenge not found."))

            val updated = ch.copy(status = "REJECTED")
            localChallenges[challengeId] = updated
            docRef.update("status", "REJECTED").await()

            sendNotification(
                userId = ch.challengerId,
                title = "❌ Challenge Rejected",
                message = "${ch.challengedName} declined your 1v1 challenge."
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinChallenge(challengeId: String, userId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            
            firestore.runTransaction { transaction ->
                val docSnap = transaction.get(docRef)
                val ch = docSnap.toObject(ChallengeData::class.java)
                    ?: throw Exception("Challenge not found.")

                if (ch.status == "PREPARING" || ch.status == "JOINING") {
                    val now = System.currentTimeMillis()
                    val currentStatus = "JOINING"
                    val currentJoinStartTime = ch.joinStartTime ?: now

                    val isChallenger = (userId == ch.challengerId)
                    val updated = if (isChallenger) {
                        ch.copy(status = currentStatus, joinStartTime = currentJoinStartTime, challengerJoined = true)
                    } else {
                        ch.copy(status = currentStatus, joinStartTime = currentJoinStartTime, challengedJoined = true)
                    }

                    // If both joined, transition to READY_TO_START (10s countdown)
                    val finalCh = if (updated.challengerJoined && updated.challengedJoined) {
                        updated.copy(status = "READY_TO_START", readyStartTime = now)
                    } else {
                        updated
                    }
                    
                    transaction.set(docRef, finalCh)
                    null
                } else {
                    throw Exception("Challenge status is ${ch.status}")
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitQuizAnswers(
        challengeId: String,
        userId: String,
        selectedAnswersMap: Map<Int, Option>,
        timeTakenSec: Long
    ): Result<ChallengeData> {
        return submitOrQuitQuiz(challengeId, userId, selectedAnswersMap, timeTakenSec, isQuit = false)
    }

    suspend fun quitChallengeQuiz(
        challengeId: String,
        userId: String,
        selectedAnswersMap: Map<Int, Option>,
        timeTakenSec: Long
    ): Result<ChallengeData> {
        return submitOrQuitQuiz(challengeId, userId, selectedAnswersMap, timeTakenSec, isQuit = true)
    }

    private suspend fun submitOrQuitQuiz(
        challengeId: String,
        userId: String,
        selectedAnswersMap: Map<Int, Option>,
        timeTakenSec: Long,
        isQuit: Boolean
    ): Result<ChallengeData> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val answerRef = docRef.collection("answers").document(userId)

            val finalCh = firestore.runTransaction { transaction ->
                val docSnap = transaction.get(docRef)
                val ch = docSnap.toObject(ChallengeData::class.java)
                    ?: throw Exception("Challenge not found.")

                val qSet = getQuestionSetById(ch.questionSetId)
                val questions = qSet?.questions ?: emptyList()
                
                var correct = 0
                var wrong = 0
                var unanswered = 0
                questions.forEach { q ->
                    val ans = selectedAnswersMap[q.id]
                    if (ans == null) unanswered++
                    else if (ans == q.correctOption) correct++
                    else wrong++
                }
                val score = correct
                val accuracy = if (questions.isNotEmpty()) (correct.toFloat() / questions.size) * 100f else 0f
                val newStatus = if (isQuit) "QUIT" else "SUBMITTED"
                val now = System.currentTimeMillis()

                val answersMapString = selectedAnswersMap.mapKeys { it.key.toString() }.mapValues { it.value.name }
                val isChallenger = (userId == ch.challengerId)
                val updated = if (isChallenger) {
                    ch.copy(
                        challengerScore = score,
                        challengerTimeTakenSec = timeTakenSec,
                        challengerStatus = newStatus,
                        challengerCorrectCount = correct,
                        challengerWrongCount = wrong,
                        challengerUnansweredCount = unanswered,
                        challengerAccuracy = accuracy,
                        challengerSubmittedAt = now,
                        challengerAnswers = answersMapString
                    )
                } else {
                    ch.copy(
                        challengedScore = score,
                        challengedTimeTakenSec = timeTakenSec,
                        challengedStatus = newStatus,
                        challengedCorrectCount = correct,
                        challengedWrongCount = wrong,
                        challengedUnansweredCount = unanswered,
                        challengedAccuracy = accuracy,
                        challengedSubmittedAt = now,
                        challengedAnswers = answersMapString
                    )
                }

                // Save individual answer document
                val answerDoc = mapOf(
                    "userId" to userId,
                    "score" to score,
                    "correctCount" to correct,
                    "wrongCount" to wrong,
                    "unansweredCount" to unanswered,
                    "accuracy" to accuracy,
                    "timeTakenSeconds" to timeTakenSec,
                    "testStatus" to newStatus,
                    "answers" to answersMapString,
                    "submittedAt" to now
                )
                transaction.set(answerRef, answerDoc)

                // Check if both players finished
                val challengerDone = updated.challengerStatus == "SUBMITTED" || updated.challengerStatus == "QUIT"
                val challengedDone = updated.challengedStatus == "SUBMITTED" || updated.challengedStatus == "QUIT"

                val final = if (challengerDone && challengedDone) {
                    val score1 = updated.challengerScore ?: 0
                    val score2 = updated.challengedScore ?: 0
                    val status1 = updated.challengerStatus
                    val status2 = updated.challengedStatus

                    val winner = when {
                        status1 == "QUIT" && status2 == "SUBMITTED" -> updated.challengedId
                        status2 == "QUIT" && status1 == "SUBMITTED" -> updated.challengerId
                        score1 > score2 -> updated.challengerId
                        score2 > score1 -> updated.challengedId
                        else -> null
                    }
                    val isDraw = (winner == null && score1 == score2)
                    
                    updated.copy(
                        status = if (isDraw) "DRAW" else "COMPLETED",
                        winnerId = winner,
                        loserId = if (winner == updated.challengerId) updated.challengedId else if (winner == updated.challengedId) updated.challengerId else null,
                        isDraw = isDraw,
                        completedAt = now,
                        xpTransactionStatus = if (winner != null && updated.isXpChallenge) "REWARD_PAID" else if (isDraw && updated.isXpChallenge) "REFUNDED" else "NONE"
                    )
                } else {
                    updated
                }

                transaction.set(docRef, final)
                final
            }.await()

            // Side effects after transaction
            if (finalCh.status == "COMPLETED" && finalCh.winnerId != null && finalCh.loserId != null) {
                payoutWinnerAndDeductLoserXp(finalCh, finalCh.winnerId!!, finalCh.loserId!!)
            } else if (finalCh.status == "DRAW") {
                refundDrawXp(finalCh)
            }

            Result.success(finalCh)
        } catch (e: Exception) {
            Log.e(TAG, "Submit or quit quiz error: ${e.message}")
            Result.failure(e)
        }
    }

    // =========================================================================
    // 4. ATOMIC XP TRANSACTIONS
    // =========================================================================

    suspend fun payoutWinnerAndDeductLoserXp(ch: ChallengeData, winnerId: String, loserId: String) {
        if (!ch.isXpChallenge) return
        try {
            val txRef = firestore.collection("xpTransactions").document(ch.challengeId)
            val txSnap = txRef.get().await()
            val currentStatus = if (txSnap.exists()) txSnap.getString("status") else null
            if (currentStatus == "REWARD_PAID") return // Prevent duplicate payout

            val winnerReward = 100
            val loserDeduction = 100

            val record = mapOf(
                "transactionId" to ch.challengeId,
                "challengeId" to ch.challengeId,
                "winnerId" to winnerId,
                "loserId" to loserId,
                "winnerRewardXp" to winnerReward,
                "loserDeductedXp" to loserDeduction,
                "status" to "REWARD_PAID",
                "completedAt" to System.currentTimeMillis()
            )
            txRef.set(record, com.google.firebase.firestore.SetOptions.merge()).await()

            // 1. Winner receives winning XP (+200 XP)
            adjustUserXp(winnerId, winnerReward)
            // 2. Loser loses 100 XP (-100 XP)
            adjustUserXp(loserId, -loserDeduction)

            sendNotification(winnerId, "🏆 1v1 Victory!", "You won the 1v1 challenge! +$winnerReward XP added to your profile!")
            sendNotification(loserId, "⚔️ 1v1 Challenge Defeat", "You lost the 1v1 challenge. -$loserDeduction XP deducted from your profile.")
        } catch (e: Exception) {
            Log.e(TAG, "Payout winner and deduct loser XP error: ${e.message}")
        }
    }

    private suspend fun refundDrawXp(ch: ChallengeData) {
        if (!ch.isXpChallenge) return
        try {
            val txRef = firestore.collection("xpTransactions").document(ch.challengeId)
            val txSnap = txRef.get().await()
            val currentStatus = if (txSnap.exists()) txSnap.getString("status") else null
            if (currentStatus == "REFUNDED") return

            txRef.set(mapOf(
                "status" to "REFUNDED",
                "completedAt" to System.currentTimeMillis()
            ), com.google.firebase.firestore.SetOptions.merge()).await()

            sendNotification(ch.challengerId, "🤝 1v1 Challenge Draw!", "The challenge ended in a tie. No XP deducted.")
            sendNotification(ch.challengedId, "🤝 1v1 Challenge Draw!", "The challenge ended in a tie. No XP deducted.")
        } catch (e: Exception) {
            Log.e(TAG, "Refund draw XP error: ${e.message}")
        }
    }

    suspend fun adjustUserXp(userId: String, xpDelta: Int): Int {
        try {
            if (userId.isBlank()) return 0
            val cleanId = userId.trim()
            var currentPoints = 0
            var found = false

            val userRef = firestore.collection("users").document(cleanId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                currentPoints = (userSnap.getLong("points") ?: userSnap.getLong("xp") ?: 0L).toInt()
                found = true
            }

            val lbRef = firestore.collection("leaderboard").document(cleanId)
            if (!found) {
                val lbSnap = lbRef.get().await()
                if (lbSnap.exists()) {
                    currentPoints = (lbSnap.getLong("points") ?: lbSnap.getLong("xp") ?: 0L).toInt()
                    found = true
                }
            }

            val newPoints = (currentPoints + xpDelta).coerceAtLeast(0)

            val updateData = mapOf(
                "points" to newPoints,
                "xp" to newPoints,
                "lastUpdatedMillis" to System.currentTimeMillis()
            )
            userRef.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
            lbRef.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()

            try {
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance()
                rtdb.getReference("liveXP").child(cleanId).updateChildren(mapOf(
                    "xp" to newPoints,
                    "points" to newPoints,
                    "updatedAt" to System.currentTimeMillis()
                ))
                rtdb.getReference("leaderboard").child(cleanId).updateChildren(mapOf(
                    "xp" to newPoints,
                    "points" to newPoints
                ))
            } catch (e: Exception) {
                Log.w(TAG, "Error updating RTDB XP for $cleanId: ${e.message}")
            }

            _xpUpdateEvents.tryEmit(Pair(cleanId, newPoints))
            return newPoints
        } catch (e: Exception) {
            Log.w(TAG, "Error adjustUserXp for $userId: ${e.message}")
            return 0
        }
    }

    suspend fun triggerChallengeAutoTransition(challengeId: String): ChallengeData? {
        val ch = localChallenges[challengeId] ?: firestore.collection("challenges").document(challengeId).get().await().toObject(ChallengeData::class.java)
        return if (ch != null) {
            checkAndAutoTransitionChallenge(ch)
        } else null
    }

    // =========================================================================
    // 5. ADMIN CONTROLS
    // =========================================================================

    suspend fun adminPauseTimer(challengeId: String, adminReason: String = "Paused by Admin"): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
                ?: return Result.failure(Exception("Challenge not found."))

            if (ch.paused) return Result.success(true)

            val now = System.currentTimeMillis()
            val currentDeadline = if (ch.status == "PENDING") ch.acceptDeadline else (ch.quizStartTime ?: (now + 6 * 3600 * 1000L))
            val remainingMs = (currentDeadline - now).coerceAtLeast(0L)

            val updated = ch.copy(
                paused = true,
                pausedAt = now,
                remainingTimeAtPause = remainingMs,
                terminationReason = adminReason
            )

            localChallenges[challengeId] = updated
            docRef.set(updated).await()

            sendNotification(ch.challengerId, "⏸ Challenge Paused by Admin", adminReason)
            sendNotification(ch.challengedId, "⏸ Challenge Paused by Admin", adminReason)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminResumeTimer(challengeId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
                ?: return Result.failure(Exception("Challenge not found."))

            if (!ch.paused) return Result.success(true)

            val now = System.currentTimeMillis()
            val remaining = ch.remainingTimeAtPause ?: (6 * 3600 * 1000L)
            val newDeadline = now + remaining

            val updated = if (ch.status == "PENDING") {
                ch.copy(
                    paused = false,
                    acceptDeadline = newDeadline,
                    resumedAt = now,
                    remainingTimeAtPause = null
                )
            } else {
                ch.copy(
                    paused = false,
                    quizStartTime = newDeadline,
                    resumedAt = now,
                    remainingTimeAtPause = null
                )
            }

            localChallenges[challengeId] = updated
            docRef.set(updated).await()

            sendNotification(ch.challengerId, "▶ Challenge Resumed", "The challenge timer has been resumed by Admin.")
            sendNotification(ch.challengedId, "▶ Challenge Resumed", "The challenge timer has been resumed by Admin.")

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminTerminateChallenge(challengeId: String, reason: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
                ?: return Result.failure(Exception("Challenge not found."))

            val updated = ch.copy(
                status = "TERMINATED",
                terminationReason = reason,
                completedAt = System.currentTimeMillis()
            )

            // Refund XP if entry was deducted and challenge terminated before completion
            if (ch.isXpChallenge && ch.xpTransactionStatus == "ENTRY_DEDUCTED") {
                refundDrawXp(ch)
            }

            localChallenges[challengeId] = updated
            docRef.set(updated).await()

            sendNotification(ch.challengerId, "🚫 Challenge Terminated", "Challenge terminated by Admin: $reason")
            sendNotification(ch.challengedId, "🚫 Challenge Terminated", "Challenge terminated by Admin: $reason")

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminForceExpire(challengeId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
                ?: return Result.failure(Exception("Challenge not found."))

            val updated = ch.copy(status = "EXPIRED")
            localChallenges[challengeId] = updated
            docRef.update("status", "EXPIRED").await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 6. ADMIN QUESTION SET MANAGEMENT
    // =========================================================================

    suspend fun saveQuestionSet(questionSet: ChallengeQuestionSet): Result<Boolean> {
        return try {
            if (questionSet.questions.size != 20) {
                return Result.failure(Exception("A Question Set must contain exactly 20 questions."))
            }

            localQuestionSets[questionSet.setId] = questionSet
            firestore.collection("challengeQuestionSets").document(questionSet.setId)
                .set(questionSet).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuestionSet(setId: String): Result<Boolean> {
        return try {
            // Check if assigned to active challenge
            val activeAssigned = localChallenges.values.any {
                it.questionSetId == setId && (it.status == "PENDING" || it.status == "ACCEPTED" || it.status == "IN_PROGRESS")
            }
            if (activeAssigned) {
                return Result.failure(Exception("Cannot delete a question set currently assigned to an active challenge."))
            }

            localQuestionSets.remove(setId)
            firestore.collection("challengeQuestionSets").document(setId).delete().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleQuestionSetStatus(setId: String, isActive: Boolean): Result<Boolean> {
        return try {
            val set = localQuestionSets[setId]
            if (set != null) {
                val updated = set.copy(isActive = isActive)
                localQuestionSets[setId] = updated
                firestore.collection("challengeQuestionSets").document(setId).update("isActive", isActive).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getQuestionSetById(setId: String): ChallengeQuestionSet? {
        return localQuestionSets[setId] ?: SampleChallengeQuestionSets.initialSets.find { it.setId == setId }
    }

    // =========================================================================
    // 7. UTILITIES & NOTIFICATIONS
    // =========================================================================

    private suspend fun ensureInitialQuestionSetsInFirestore() {
        try {
            val snap = firestore.collection("challengeQuestionSets").limit(1).get().await()
            if (snap.isEmpty) {
                SampleChallengeQuestionSets.initialSets.forEach { set ->
                    firestore.collection("challengeQuestionSets").document(set.setId).set(set).await()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Ensure initial question sets error: ${e.message}")
        }
    }

    suspend fun deleteChallengeHistory(challengeId: String, userId: String): Result<Boolean> {
        return try {
            val docRef = firestore.collection("challenges").document(challengeId)
            val docSnap = docRef.get().await()
            val ch = docSnap.toObject(ChallengeData::class.java) ?: localChallenges[challengeId]
            if (ch != null) {
                val updatedMap = ch.historyDeletedBy.toMutableMap()
                updatedMap[userId] = true
                val updated = ch.copy(historyDeletedBy = updatedMap)
                localChallenges[challengeId] = updated
                docRef.set(updated).await()
                Result.success(true)
            } else {
                Result.failure(Exception("Challenge not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sendNotification(userId: String, title: String, message: String) {
        if (userId.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notifId = "notif_chl_${System.currentTimeMillis()}_${(0..999).random()}"
                val notifDoc = mapOf(
                    "id" to notifId,
                    "title" to title,
                    "message" to message,
                    "timestamp" to "Just now",
                    "isRead" to false,
                    "type" to "CHALLENGE",
                    "targetUserId" to userId,
                    "targetUserEmail" to "",
                    "createdAtMillis" to System.currentTimeMillis()
                )
                firestore.collection("notifications").document(notifId).set(notifDoc).await()
            } catch (e: Exception) {
                Log.w(TAG, "Send notification error: ${e.message}")
            }
        }
    }
}
