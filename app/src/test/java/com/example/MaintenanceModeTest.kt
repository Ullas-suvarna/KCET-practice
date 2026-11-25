package com.example

import com.example.data.MaintenanceConfig
import com.example.data.MaintenanceControlConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class MaintenanceModeTest {

    @Test
    fun `validateDurationStrings rejects zero negative and non-numeric durations`() {
        val zero = MaintenanceControlConfig.validateDurationStrings("0", "0")
        assertFalse(zero.isValid)
        assertNotNull(zero.errorMessage)

        val empty = MaintenanceControlConfig.validateDurationStrings("", "")
        assertFalse(empty.isValid)

        val negativeHours = MaintenanceControlConfig.validateDurationStrings("-1", "30")
        assertFalse(negativeHours.isValid)

        val negativeMinutes = MaintenanceControlConfig.validateDurationStrings("0", "-5")
        assertFalse(negativeMinutes.isValid)

        val invalidText = MaintenanceControlConfig.validateDurationStrings("abc", "10")
        assertFalse(invalidText.isValid)

        val tooLong = MaintenanceControlConfig.validateDurationStrings("999", "0")
        assertFalse(tooLong.isValid)
    }

    @Test
    fun `validateDurationStrings accepts valid durations and computes exact millis`() {
        val thirtyMins = MaintenanceControlConfig.validateDurationStrings("", "30")
        assertTrue(thirtyMins.isValid)
        assertEquals(30L * 60_000L, thirtyMins.totalDurationMillis)

        val oneHourFifteen = MaintenanceControlConfig.validateDurationStrings("1", "15")
        assertTrue(oneHourFifteen.isValid)
        assertEquals(75L * 60_000L, oneHourFifteen.totalDurationMillis)
    }

    @Test
    fun `formatCountdownParts handles standard and over-24-hour durations accurately`() {
        val zero = MaintenanceControlConfig.formatCountdownParts(0L)
        assertEquals("00", zero.hoursFormatted)
        assertEquals("00", zero.minutesFormatted)
        assertEquals("00", zero.secondsFormatted)

        // 1 second remaining
        val oneSec = MaintenanceControlConfig.formatCountdownParts(1L)
        assertEquals("00", oneSec.hoursFormatted)
        assertEquals("00", oneSec.minutesFormatted)
        assertEquals("01", oneSec.secondsFormatted)

        // Exact 10 minutes
        val tenMin = MaintenanceControlConfig.formatCountdownParts(10L * 60_000L)
        assertEquals("00", tenMin.hoursFormatted)
        assertEquals("10", tenMin.minutesFormatted)
        assertEquals("00", tenMin.secondsFormatted)

        // 26 hours 5 minutes 9 seconds (must NOT wrap modulo 24)
        val over24HoursMs = (26L * 3600L + 5L * 60L + 9L) * 1000L
        val over24 = MaintenanceControlConfig.formatCountdownParts(over24HoursMs)
        assertEquals("26", over24.hoursFormatted)
        assertEquals("05", over24.minutesFormatted)
        assertEquals("09", over24.secondsFormatted)
    }

    @Test
    fun `maintenance config active and auto-expiry checks respond immediately to endTime`() {
        val config = MaintenanceConfig(
            maintenanceEnabled = true,
            maintenanceStartTime = 1_000_000L,
            maintenanceEndTime = 2_800_000L,
            updatedAt = 1_000_000L,
            updatedBy = "ullassuvarna65@gmail.com"
        )

        assertTrue(config.isEffectivelyActive(1_500_000L))
        assertFalse(config.isExpired(1_500_000L))
        assertEquals(1_300_000L, config.remainingMillis(1_500_000L))

        // At exact end time or after, maintenance automatically expires locally even if backend wasn't updated yet
        assertFalse(config.isEffectivelyActive(2_800_000L))
        assertTrue(config.isExpired(2_800_000L))
        assertEquals(0L, config.remainingMillis(2_800_000L))
        assertFalse(config.isEffectivelyActive(3_000_000L))
    }

    @Test
    fun `resolveAuthoritativeConfig chooses latest updatedAt across Firestore and RTDB`() {
        val olderActive = MaintenanceConfig(
            maintenanceEnabled = true,
            maintenanceStartTime = 1000L,
            maintenanceEndTime = 5000L,
            updatedAt = 1000L,
            updatedBy = "ullassuvarna65@gmail.com"
        )
        val newerDisabled = MaintenanceConfig(
            maintenanceEnabled = false,
            maintenanceStartTime = 1000L,
            maintenanceEndTime = 0L,
            updatedAt = 2500L,
            updatedBy = "ullassuvarna65@gmail.com"
        )

        val merged1 = MaintenanceControlConfig.resolveAuthoritativeConfig(olderActive, newerDisabled)
        assertEquals(newerDisabled, merged1)

        val newerExtended = olderActive.copy(maintenanceEndTime = 9000L, updatedAt = 3000L)
        val merged2 = MaintenanceControlConfig.resolveAuthoritativeConfig(newerExtended, newerDisabled)
        assertEquals(newerExtended, merged2)
    }

    @Test
    fun `formatExpectedBackText formats local timezone clearly for same day and next day`() {
        val tz = TimeZone.getTimeZone("Asia/Kolkata")
        // 2026-11-25 14:00:00 UTC == 19:30 IST; +1 hour == 20:30 IST (8:30 PM)
        val nowIstMs = 1795615200_000L // fixed epoch
        val oneHourLaterMs = nowIstMs + 3600_000L
        val sameDay = MaintenanceControlConfig.formatExpectedBackText(
            endTimeMillis = oneHourLaterMs,
            nowMillis = nowIstMs,
            timeZone = tz,
            locale = Locale.US
        )
        assertTrue("Expected prefix, got: $sameDay", sameDay.startsWith("Expected back at "))

        val twoDaysLaterMs = nowIstMs + 48L * 3600_000L
        val futureDay = MaintenanceControlConfig.formatExpectedBackText(
            endTimeMillis = twoDaysLaterMs,
            nowMillis = nowIstMs,
            timeZone = tz,
            locale = Locale.US
        )
        assertTrue("Expected date included for future day, got: $futureDay", futureDay.contains(","))
    }

    @Test
    fun `authorized admin check enforces exact admin email`() {
        assertTrue(MaintenanceControlConfig.isAuthorizedAdminEmail("ullassuvarna65@gmail.com"))
        assertTrue(MaintenanceControlConfig.isAuthorizedAdminEmail("  UllasSuvarna65@gmail.com "))
        assertFalse(MaintenanceControlConfig.isAuthorizedAdminEmail("student@gmail.com"))
        assertFalse(MaintenanceControlConfig.isAuthorizedAdminEmail(null))
        assertFalse(MaintenanceControlConfig.isAuthorizedAdminEmail(""))
    }

    @Test
    fun `maintenance screen and admin section classes load and respect JVM param limits`() {
        val screenCls = Class.forName("com.example.ui.screens.MaintenanceScreenKt")
        val adminSectionCls = Class.forName("com.example.ui.screens.MaintenanceAdminSectionKt")
        val adminPanelCls = Class.forName("com.example.ui.screens.AdminPanelScreenKt")

        assertNotNull(screenCls)
        assertNotNull(adminSectionCls)
        assertNotNull(adminPanelCls)

        for (m in adminPanelCls.declaredMethods + adminSectionCls.declaredMethods + screenCls.declaredMethods) {
            assertTrue(
                "Method ${m.name} has ${m.parameterCount} params, must be <= 255",
                m.parameterCount <= 255
            )
        }
    }

    @Test
    fun `startup classes load cleanly and MainViewModel initializes StateFlows before collectors`() {
        val startupClassNames = listOf(
            "com.example.MainActivity",
            "com.example.MainActivityKt",
            "com.example.viewmodel.MainViewModel",
            "com.example.data.FirebaseService",
            "com.example.data.FirebaseRealtimeService"
        )
        for (className in startupClassNames) {
            val cls = Class.forName(className)
            assertNotNull("Expected $className to load", cls)
            for (m in cls.declaredMethods) {
                assertTrue(
                    "Method $className.${m.name} has ${m.parameterCount} params, must be <= 255",
                    m.parameterCount <= 255
                )
            }
        }

        val vmFile = java.io.File("src/main/java/com/example/viewmodel/MainViewModel.kt").let {
            if (it.exists()) it else java.io.File("app/src/main/java/com/example/viewmodel/MainViewModel.kt")
        }
        assertTrue("Expected MainViewModel.kt to exist for init-order verification", vmFile.exists())
        val content = vmFile.readText()
        val initContextIdx = content.indexOf("FirebaseService.initContext")
        val dbInitIdx = content.indexOf("AppDatabase.getInstance")
        assertTrue("FirebaseService.initContext must be called before AppDatabase.getInstance", initContextIdx in 0 until dbInitIdx)

        val currentScreenDeclIdx = content.indexOf("private val _currentScreen")
        val maintenanceCollectIdx = content.indexOf("_maintenanceConfig")
        val maintenanceCollectorLaunchIdx = content.indexOf("MaintenanceControlConfig.resolveAuthoritativeConfig")
        assertTrue("_currentScreen must be declared before startup maintenance collector", currentScreenDeclIdx in 0 until maintenanceCollectorLaunchIdx)
        assertTrue("_maintenanceConfig must be declared", maintenanceCollectIdx >= 0)
    }
}
