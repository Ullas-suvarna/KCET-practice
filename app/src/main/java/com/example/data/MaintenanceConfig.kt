package com.example.data

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Keep
@IgnoreExtraProperties
data class MaintenanceConfig(
    val maintenanceEnabled: Boolean = false,
    val maintenanceStartTime: Long = 0L,
    val maintenanceEndTime: Long = 0L,
    val updatedAt: Long = 0L,
    val updatedBy: String = ""
) {
    fun isEffectivelyActive(nowMillis: Long): Boolean {
        return maintenanceEnabled && maintenanceEndTime > 0L && nowMillis < maintenanceEndTime
    }

    fun remainingMillis(nowMillis: Long): Long {
        if (!maintenanceEnabled || maintenanceEndTime <= 0L) return 0L
        return (maintenanceEndTime - nowMillis).coerceAtLeast(0L)
    }

    fun isExpired(nowMillis: Long): Boolean {
        return maintenanceEnabled && maintenanceEndTime > 0L && nowMillis >= maintenanceEndTime
    }
}

data class MaintenanceCountdownParts(
    val hours: Long,
    val minutes: Int,
    val seconds: Int
) {
    val hoursFormatted: String get() = hours.toString().padStart(2, '0')
    val minutesFormatted: String get() = minutes.toString().padStart(2, '0')
    val secondsFormatted: String get() = seconds.toString().padStart(2, '0')
}

data class MaintenanceDurationValidation(
    val isValid: Boolean,
    val totalDurationMillis: Long = 0L,
    val hours: Int = 0,
    val minutes: Int = 0,
    val errorMessage: String? = null
)

data class MaintenancePreset(
    val label: String,
    val hours: Int,
    val minutes: Int
)

object MaintenanceControlConfig {
    const val AUTHORIZED_ADMIN_EMAIL = "ullassuvarna65@gmail.com"
    const val MAX_MAINTENANCE_HOURS = 720 // 30 days max safety bound

    val START_PRESETS: List<MaintenancePreset> = listOf(
        MaintenancePreset("10 min", 0, 10),
        MaintenancePreset("30 min", 0, 30),
        MaintenancePreset("1 hr", 1, 0),
        MaintenancePreset("2 hrs", 2, 0),
        MaintenancePreset("6 hrs", 6, 0),
        MaintenancePreset("24 hrs", 24, 0)
    )

    val EXTEND_PRESETS: List<MaintenancePreset> = listOf(
        MaintenancePreset("+10 min", 0, 10),
        MaintenancePreset("+30 min", 0, 30),
        MaintenancePreset("+1 hr", 1, 0),
        MaintenancePreset("+2 hrs", 2, 0)
    )

    fun isAuthorizedAdminEmail(email: String?): Boolean {
        return email?.trim()?.equals(AUTHORIZED_ADMIN_EMAIL, ignoreCase = true) == true
    }

    fun validateDurationStrings(hoursText: String, minutesText: String): MaintenanceDurationValidation {
        val cleanHoursStr = hoursText.trim()
        val cleanMinutesStr = minutesText.trim()

        val parsedHours = if (cleanHoursStr.isEmpty()) 0 else cleanHoursStr.toIntOrNull()
        val parsedMinutes = if (cleanMinutesStr.isEmpty()) 0 else cleanMinutesStr.toIntOrNull()

        if (parsedHours == null || parsedMinutes == null) {
            return MaintenanceDurationValidation(
                isValid = false,
                errorMessage = "Please enter valid whole numbers for hours and minutes."
            )
        }

        return validateDurationValues(parsedHours, parsedMinutes)
    }

    fun validateDurationValues(hours: Int, minutes: Int): MaintenanceDurationValidation {
        if (hours < 0 || minutes < 0) {
            return MaintenanceDurationValidation(
                isValid = false,
                errorMessage = "Duration cannot contain negative values."
            )
        }
        val totalMinutes = hours.toLong() * 60L + minutes.toLong()
        if (totalMinutes <= 0L) {
            return MaintenanceDurationValidation(
                isValid = false,
                errorMessage = "Duration must be greater than 0 minutes."
            )
        }
        if (totalMinutes > MAX_MAINTENANCE_HOURS.toLong() * 60L) {
            return MaintenanceDurationValidation(
                isValid = false,
                errorMessage = "Maximum allowed duration is $MAX_MAINTENANCE_HOURS hours."
            )
        }
        val durationMillis = totalMinutes * 60_000L
        return MaintenanceDurationValidation(
            isValid = true,
            totalDurationMillis = durationMillis,
            hours = hours,
            minutes = minutes,
            errorMessage = null
        )
    }

    fun resolveAuthoritativeConfig(
        firestoreConfig: MaintenanceConfig?,
        rtdbConfig: MaintenanceConfig?
    ): MaintenanceConfig? {
        return when {
            firestoreConfig != null && rtdbConfig != null -> {
                when {
                    rtdbConfig.updatedAt > firestoreConfig.updatedAt -> rtdbConfig
                    firestoreConfig.updatedAt > rtdbConfig.updatedAt -> firestoreConfig
                    !firestoreConfig.maintenanceEnabled && rtdbConfig.maintenanceEnabled -> rtdbConfig
                    firestoreConfig.maintenanceEnabled && !rtdbConfig.maintenanceEnabled -> firestoreConfig
                    rtdbConfig.maintenanceEndTime > firestoreConfig.maintenanceEndTime -> rtdbConfig
                    else -> firestoreConfig
                }
            }
            firestoreConfig != null -> firestoreConfig
            rtdbConfig != null -> rtdbConfig
            else -> null
        }
    }

    fun formatCountdownParts(remainingMillis: Long): MaintenanceCountdownParts {
        if (remainingMillis <= 0L) {
            return MaintenanceCountdownParts(hours = 0L, minutes = 0, seconds = 0)
        }
        // Ceiling to nearest second avoids showing 09:59 immediately at t=10:00 and hits 00:00:00 at <=0ms
        val totalSeconds = (remainingMillis + 999L) / 1000L
        val hours = totalSeconds / 3600L
        val minutes = ((totalSeconds % 3600L) / 60L).toInt()
        val seconds = (totalSeconds % 60L).toInt()
        return MaintenanceCountdownParts(hours = hours, minutes = minutes, seconds = seconds)
    }

    fun formatShortTime(
        timeMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        if (timeMillis <= 0L) return "--"
        val endCal = Calendar.getInstance(timeZone, locale).apply { timeInMillis = timeMillis }
        val nowCal = Calendar.getInstance(timeZone, locale).apply { timeInMillis = nowMillis }

        val isSameDay = endCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            endCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        val pattern = if (isSameDay) "h:mm a" else "h:mm a, MMM d"
        val sdf = SimpleDateFormat(pattern, locale).apply {
            this.timeZone = timeZone
        }
        return sdf.format(Date(timeMillis))
    }

    fun formatExpectedBackText(
        endTimeMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        if (endTimeMillis <= 0L) return "Expected back shortly"
        return "Expected back at ${formatShortTime(endTimeMillis, nowMillis, timeZone, locale)}"
    }

    fun formatDurationSummary(durationMillis: Long): String {
        val totalMins = (durationMillis / 60_000L).coerceAtLeast(0L)
        val hrs = totalMins / 60L
        val mins = totalMins % 60L
        return when {
            hrs > 0L && mins > 0L -> "$hrs hr${if (hrs > 1) "s" else ""} $mins min${if (mins > 1) "s" else ""}"
            hrs > 0L -> "$hrs hr${if (hrs > 1) "s" else ""}"
            else -> "$mins min${if (mins != 1L) "s" else ""}"
        }
    }
}
