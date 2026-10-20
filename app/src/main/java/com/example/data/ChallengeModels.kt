package com.example.data

enum class ChallengeType {
    XP, FRIENDLY
}

enum class ChallengeStatus {
    PENDING,
    PREPARING,
    JOINING,
    READY_TO_START,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    EXPIRED,
    TERMINATED,
    DRAW
}

data class ChallengeData(
    val challengeId: String = "",
    val challengerId: String = "",
    val challengerName: String = "",
    val challengerPhoto: String? = null,
    val challengerRank: Int = 1,
    val challengerBadge: String = "👑 GrandMaster",

    val challengedId: String = "",
    val challengedName: String = "",
    val challengedPhoto: String? = null,
    val challengedRank: Int = 2,
    val challengedBadge: String = "🏆 Master",

    val challengeType: String = "XP", // "XP" or "FRIENDLY"
    val entryXP: Int = 200,
    val prizeXP: Int = 400,

    val status: String = "PENDING", // PENDING, PREPARING, JOINING, READY_TO_START, IN_PROGRESS, COMPLETED, REJECTED, EXPIRED, TERMINATED, DRAW
    val questionSetId: String = "set_01",
    val questionIds: List<Int> = emptyList(),

    val createdAt: Long = System.currentTimeMillis(),
    val acceptDeadline: Long = System.currentTimeMillis() + (24 * 3600 * 1000L), // 24 Hours
    val prepDurationMs: Long = 3 * 3600 * 1000L, // 3 Hours (Admin-configurable)
    val prepStartTime: Long? = null,
    val joinStartTime: Long? = null,
    val readyStartTime: Long? = null,
    val quizStartTime: Long? = null,
    val quizEndTime: Long? = null,
    val completedAt: Long? = null,

    // Admin Pause controls
    val paused: Boolean = false,
    val pauseType: String = "ACCEPTANCE", // "ACCEPTANCE" or "QUIZ_START"
    val pausedAt: Long? = null,
    val remainingTimeAtPause: Long? = null,
    val resumedAt: Long? = null,

    // Quiz scores
    val challengerScore: Int? = null,
    val challengedScore: Int? = null,
    val challengerTimeTakenSec: Long? = null,
    val challengedTimeTakenSec: Long? = null,

    val challengerStatus: String = "IN_PROGRESS", // IN_PROGRESS, SUBMITTED, QUIT
    val challengedStatus: String = "IN_PROGRESS", // IN_PROGRESS, SUBMITTED, QUIT
    val challengerCorrectCount: Int = 0,
    val challengerWrongCount: Int = 0,
    val challengerUnansweredCount: Int = 0,
    val challengerAccuracy: Float = 0f,
    val challengerSubmittedAt: Long? = null,

    val challengedCorrectCount: Int = 0,
    val challengedWrongCount: Int = 0,
    val challengedUnansweredCount: Int = 0,
    val challengedAccuracy: Float = 0f,
    val challengedSubmittedAt: Long? = null,

    val winnerId: String? = null,
    val loserId: String? = null,
    val isDraw: Boolean = false,
    
    val challengerJoined: Boolean = false,
    val challengedJoined: Boolean = false,

    val terminationReason: String? = null,
    val xpTransactionStatus: String = "NONE", // NONE, ENTRY_DEDUCTED, REWARD_PAID, REFUNDED, FAILED
    val historyDeletedBy: Map<String, Boolean> = emptyMap(),
    val challengerAnswers: Map<String, String> = emptyMap(),
    val challengedAnswers: Map<String, String> = emptyMap()
) {
    val isXpChallenge: Boolean get() = challengeType.equals("XP", ignoreCase = true)

    fun getEffectiveRemainingMs(now: Long = System.currentTimeMillis()): Long {
        if (paused) {
            return remainingTimeAtPause ?: 0L
        }
        val targetDeadline = when (status) {
            "PENDING" -> acceptDeadline
            "PREPARING" -> (prepStartTime ?: 0L) + prepDurationMs
            "JOINING" -> (joinStartTime ?: 0L) + (5 * 60 * 1000L)
            "READY_TO_START" -> (readyStartTime ?: 0L) + (10 * 1000L)
            else -> 0L
        }
        val remaining = targetDeadline - now
        return if (remaining > 0L) remaining else 0L
    }
}

data class ChallengeQuestionSet(
    val setId: String = "",
    val name: String = "",
    val description: String = "",
    val questions: List<Question> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChallengeAnswer(
    val challengeId: String = "",
    val userId: String = "",
    val selectedOptions: Map<String, String> = emptyMap(), // questionId -> "A"/"B"/"C"/"D"
    val score: Int = 0,
    val timeTakenSeconds: Long = 0L,
    val completedAt: Long = System.currentTimeMillis()
)

data class XpTransactionRecord(
    val transactionId: String = "",
    val challengeId: String = "",
    val player1Id: String = "",
    val player2Id: String = "",
    val entryAmount: Int = 200,
    val rewardAmount: Int = 400,
    val status: String = "ENTRY_DEDUCTED", // ENTRY_DEDUCTED, REWARD_PAID, REFUNDED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class ChallengeSummaryStats(
    val totalChallenges: Int = 0,
    val pendingCount: Int = 0,
    val acceptedCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val rejectedCount: Int = 0,
    val expiredCount: Int = 0,
    val terminatedCount: Int = 0,
    val drawCount: Int = 0,
    val xpChallengeCount: Int = 0,
    val friendlyChallengeCount: Int = 0,
    val totalXpWagered: Int = 0,
    val totalXpRewarded: Int = 0,
    val totalXpRefunded: Int = 0
)
