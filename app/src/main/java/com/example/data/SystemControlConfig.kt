package com.example.data

object SystemControlConfig {
    const val DEFAULT_SYSTEM_SECURITY_KEY = "17082006"

    data class RemoteSecurityKeySnapshot(
        val securityKey: String? = null,
        val pin: String? = null,
        val updatedAtMillis: Long = 0L
    )

    data class SystemShutdownStatus(
        val isShutdown: Boolean = false,
        val updatedAtMillis: Long = 0L,
        val updatedBy: String = ""
    )

    /**
     * Resolves the single authoritative security key from Firebase Firestore and Realtime Database.
     *
     * Rules:
     * - Initial default key is "17082006" only when neither Firestore nor RTDB has a stored key.
     * - If a key is updated in Firebase (either Firestore `admin_config/security` or RTDB `admin_config`),
     *   ONLY the updated key is returned (never falling back to "17082006" if Firebase holds a newer/custom key).
     */
    fun resolveEffectiveSecurityKey(
        firestoreSnapshot: RemoteSecurityKeySnapshot?,
        rtdbSecurityKey: String?,
        rtdbPin: String? = null,
        rtdbUpdatedAtMillis: Long = 0L
    ): String {
        val fsSecKey = firestoreSnapshot?.securityKey?.trim()?.takeIf { it.isNotEmpty() }
        val fsPin = firestoreSnapshot?.pin?.trim()?.takeIf { it.isNotEmpty() }
        val fsResolved = when {
            fsSecKey != null && fsSecKey != DEFAULT_SYSTEM_SECURITY_KEY -> fsSecKey
            fsPin != null && fsPin != DEFAULT_SYSTEM_SECURITY_KEY -> fsPin
            else -> fsSecKey ?: fsPin
        }

        val rtdbSec = rtdbSecurityKey?.trim()?.takeIf { it.isNotEmpty() }
        val rtdbP = rtdbPin?.trim()?.takeIf { it.isNotEmpty() }
        val rtdbResolved = when {
            rtdbSec != null && rtdbSec != DEFAULT_SYSTEM_SECURITY_KEY -> rtdbSec
            rtdbP != null && rtdbP != DEFAULT_SYSTEM_SECURITY_KEY -> rtdbP
            else -> rtdbSec ?: rtdbP
        }

        val fsTs = firestoreSnapshot?.updatedAtMillis ?: 0L

        return when {
            fsResolved != null && rtdbResolved != null -> {
                if (fsResolved == rtdbResolved) {
                    fsResolved
                } else if (fsTs > 0L && rtdbUpdatedAtMillis > 0L) {
                    if (rtdbUpdatedAtMillis > fsTs) rtdbResolved else fsResolved
                } else if (rtdbResolved != DEFAULT_SYSTEM_SECURITY_KEY && fsResolved == DEFAULT_SYSTEM_SECURITY_KEY) {
                    rtdbResolved
                } else if (fsResolved != DEFAULT_SYSTEM_SECURITY_KEY && rtdbResolved == DEFAULT_SYSTEM_SECURITY_KEY) {
                    fsResolved
                } else {
                    fsResolved
                }
            }
            fsResolved != null -> fsResolved
            rtdbResolved != null -> rtdbResolved
            else -> DEFAULT_SYSTEM_SECURITY_KEY
        }
    }

    /**
     * Strictly validates the entered security key against the resolved Firebase key.
     * Rejects empty strings and rejects the default key whenever Firebase holds an updated key.
     */
    fun verifySecurityKey(enteredKey: String, effectiveFirebaseKey: String): Boolean {
        val cleanEntered = enteredKey.trim()
        val cleanTarget = effectiveFirebaseKey.trim().ifBlank { DEFAULT_SYSTEM_SECURITY_KEY }
        return cleanEntered.isNotEmpty() && cleanEntered == cleanTarget
    }

    fun resolveSystemShutdownStatus(
        firestoreStatus: SystemShutdownStatus?,
        rtdbStatus: SystemShutdownStatus?
    ): Boolean {
        return when {
            firestoreStatus != null && rtdbStatus != null -> {
                if (rtdbStatus.updatedAtMillis > firestoreStatus.updatedAtMillis) {
                    rtdbStatus.isShutdown
                } else {
                    firestoreStatus.isShutdown
                }
            }
            firestoreStatus != null -> firestoreStatus.isShutdown
            rtdbStatus != null -> rtdbStatus.isShutdown
            else -> false
        }
    }

    const val LEADERBOARD_CYCLE_DAYS = 30L
    const val LEADERBOARD_CYCLE_DURATION_MS = LEADERBOARD_CYCLE_DAYS * 24L * 60L * 60L * 1000L

    data class LeaderboardCycleStatus(
        val lastResetAtMillis: Long = 0L,
        val cycleStartAtMillis: Long = 0L,
        val nextResetAtMillis: Long = 0L
    ) {
        val effectiveCycleStartAtMillis: Long
            get() = when {
                cycleStartAtMillis > 0L -> cycleStartAtMillis
                lastResetAtMillis > 0L -> lastResetAtMillis
                nextResetAtMillis > LEADERBOARD_CYCLE_DURATION_MS -> nextResetAtMillis - LEADERBOARD_CYCLE_DURATION_MS
                else -> 0L
            }

        val effectiveNextResetAtMillis: Long
            get() = when {
                nextResetAtMillis > 0L -> nextResetAtMillis
                effectiveCycleStartAtMillis > 0L -> effectiveCycleStartAtMillis + LEADERBOARD_CYCLE_DURATION_MS
                else -> 0L
            }

        fun remainingMillis(nowMillis: Long): Long {
            val target = effectiveNextResetAtMillis
            if (target <= 0L) return LEADERBOARD_CYCLE_DURATION_MS
            return (target - nowMillis).coerceAtLeast(0L)
        }

        fun progressFraction(nowMillis: Long): Float {
            val start = effectiveCycleStartAtMillis
            val end = effectiveNextResetAtMillis
            if (start <= 0L || end <= start) return 0f
            val total = (end - start).toFloat()
            val elapsed = (nowMillis - start).coerceIn(0L, end - start).toFloat()
            return (elapsed / total).coerceIn(0f, 1f)
        }

        fun isDueForReset(nowMillis: Long): Boolean {
            val target = effectiveNextResetAtMillis
            return target > 0L && nowMillis >= target
        }
    }

    data class LeaderboardCountdownParts(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long
    ) {
        val daysFormatted: String get() = days.toString().padStart(2, '0')
        val hoursFormatted: String get() = hours.toString().padStart(2, '0')
        val minutesFormatted: String get() = minutes.toString().padStart(2, '0')
        val secondsFormatted: String get() = seconds.toString().padStart(2, '0')
    }

    fun formatLeaderboardCountdownParts(remainingMillis: Long): LeaderboardCountdownParts {
        val safeMs = remainingMillis.coerceAtLeast(0L)
        val totalSeconds = if (safeMs == 0L) 0L else (safeMs + 999L) / 1000L
        val days = totalSeconds / 86_400L
        val hours = (totalSeconds % 86_400L) / 3_600L
        val minutes = (totalSeconds % 3_600L) / 60L
        val seconds = totalSeconds % 60L
        return LeaderboardCountdownParts(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds
        )
    }

    fun formatLeaderboardResetDateText(
        nextResetAtMillis: Long,
        timeZone: java.util.TimeZone = java.util.TimeZone.getDefault(),
        locale: java.util.Locale = java.util.Locale.getDefault()
    ): String {
        if (nextResetAtMillis <= 0L) return "30-day cycle"
        val fmt = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", locale).apply {
            this.timeZone = timeZone
        }
        return "Resets ${fmt.format(java.util.Date(nextResetAtMillis))}"
    }

    fun createLeaderboardCycleFromReset(resetAtMillis: Long): LeaderboardCycleStatus {
        return LeaderboardCycleStatus(
            lastResetAtMillis = resetAtMillis,
            cycleStartAtMillis = resetAtMillis,
            nextResetAtMillis = resetAtMillis + LEADERBOARD_CYCLE_DURATION_MS
        )
    }

    fun resolveLeaderboardCycleStatus(
        firestoreStatus: LeaderboardCycleStatus?,
        rtdbStatus: LeaderboardCycleStatus?
    ): LeaderboardCycleStatus? {
        if (firestoreStatus == null && rtdbStatus == null) return null
        if (firestoreStatus == null) return rtdbStatus
        if (rtdbStatus == null) return firestoreStatus

        val fsRankTs = maxOf(firestoreStatus.lastResetAtMillis, firestoreStatus.effectiveCycleStartAtMillis)
        val rtdbRankTs = maxOf(rtdbStatus.lastResetAtMillis, rtdbStatus.effectiveCycleStartAtMillis)
        return when {
            rtdbRankTs > fsRankTs -> rtdbStatus
            fsRankTs > rtdbRankTs -> firestoreStatus
            rtdbStatus.effectiveNextResetAtMillis > firestoreStatus.effectiveNextResetAtMillis -> rtdbStatus
            else -> firestoreStatus
        }
    }
}
