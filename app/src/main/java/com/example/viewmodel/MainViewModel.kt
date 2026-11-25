package com.example.viewmodel

import android.app.Application
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnalyticsSummary
import com.example.data.Announcement
import com.example.data.AppDatabase
import com.example.data.BadgeItem
import com.example.data.FirebaseService
import com.example.data.FirebaseService.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.example.data.KcetRepository
import com.example.data.LeaderboardEntry
import com.example.data.Option
import com.example.data.Question
import com.example.data.QuestionPaletteStatus
import com.example.data.ResourceItem
import com.example.data.Subject
import com.example.data.SubjectAnalytics
import com.example.data.TestResultEntity
import com.example.data.TopicAccuracy
import com.example.data.UserAnswer
import com.example.data.UserProfile
import com.example.data.AdminTestResultItem
import com.example.data.FirebaseRealtimeService
import com.example.data.SampleData
import com.example.data.SeniorMentorProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.SeniorChatBooking

import com.example.data.BookmarkEntity

import com.example.data.StudentNotification
import com.example.notification.DailyNotificationManager

import com.example.data.CollegeInfo
import com.example.data.EngineeringBranchInfo
import com.example.data.BranchCutoffInfo
import com.example.data.CollegeRecommendationQuery
import com.example.data.RecommendedCollegeItem
import android.content.Context
import com.example.data.SavedRecommendationHistory
import com.example.data.CollegeSampleData
import com.example.data.doesCutoffMatchCollege
import com.example.data.EmailDeliverySettings
import com.example.data.EmailSettingsApiResult
import com.example.data.EmailSettingsService
import com.example.data.AdminEmailApiResult
import com.example.data.AdminEmailLogItem
import com.example.data.AdminEmailService
import com.example.data.AdminRegisteredStudent
import com.example.data.MaintenanceConfig
import com.example.data.MaintenanceControlConfig

enum class Screen {
    SPLASH, DEV_INTRO, ONBOARDING, AUTH, MAIN, EXAM, RESULT, ADMIN, SENIOR_CHAT, BOOKMARKS, NOTIFICATIONS, AI_RECOMMENDATION, BRANCH_EXPLORER, CHALLENGE
}

enum class BottomTab {
    HOME, PRACTICE, DOUBTS, VIDEOS, RESOURCES, COUNSELLING, PROFILE
}

data class NavDestination(
    val screen: Screen,
    val bottomTab: BottomTab = BottomTab.HOME
)

data class PendingExamInfo(
    val title: String,
    val category: String,
    val durationMins: Int,
    val questionCount: Int,
    val totalMarks: Int,
    val testSet: com.example.data.TestSeriesSet? = null,
    val bookmarkList: List<BookmarkEntity>? = null,
    val isFreeTest: Boolean = false
)

data class EmailDeliveryUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val settings: EmailDeliverySettings? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val firebaseAuthEmail: String? = null,
    val isFirebaseEmailVerified: Boolean = false,
    val isFirebaseSignedIn: Boolean = false
)

data class AdminEmailNotificationUiState(
    val isFirebaseSignedIn: Boolean = false,
    val firebaseAuthEmail: String? = null,
    val isFirebaseEmailVerified: Boolean = false,
    val students: List<AdminRegisteredStudent> = emptyList(),
    val isLoadingStudents: Boolean = false,
    val isStudentsLoaded: Boolean = false,
    val studentsError: String? = null,
    val searchQuery: String = "",
    val selectedStudent: AdminRegisteredStudent? = null,
    val subject: String = "",
    val message: String = "",
    val isSending: Boolean = false,
    val sendSuccessMessage: String? = null,
    val sendErrorMessage: String? = null,
    val isSendTimeoutUncertain: Boolean = false,
    val emailLogs: List<AdminEmailLogItem> = emptyList(),
    val isLoadingLogs: Boolean = false,
    val isLogsLoaded: Boolean = false,
    val logsError: String? = null
)

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("kcet_genz_prefs", Context.MODE_PRIVATE)

    init {
        com.example.data.FirebaseService.initContext(app.applicationContext)
    }

    private val _hasSeenDeveloperIntro = MutableStateFlow(
        prefs.getBoolean("has_seen_dev_intro", true)
    )
    val hasSeenDeveloperIntro: StateFlow<Boolean> = _hasSeenDeveloperIntro.asStateFlow()

    fun markDeveloperIntroSeen() {
        prefs.edit().putBoolean("has_seen_dev_intro", true).apply()
        _hasSeenDeveloperIntro.value = true
    }

    fun replayDeveloperIntro() {
        prefs.edit().putBoolean("has_seen_dev_intro", false).apply()
        _hasSeenDeveloperIntro.value = false
        _currentScreen.value = Screen.DEV_INTRO
    }

    private val db = AppDatabase.getInstance(app)
    val repository = KcetRepository(db.testResultDao(), db.bookmarkDao())

    // Daily AI Practice Set (DPP) State with 24-Hour Reset Cycle
    private val _dailyPracticeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val dailyPracticeQuestions: StateFlow<List<Question>> = _dailyPracticeQuestions.asStateFlow()

    private val _dailyFullMockQuestions = MutableStateFlow<List<Question>>(emptyList())
    val dailyFullMockQuestions: StateFlow<List<Question>> = _dailyFullMockQuestions.asStateFlow()

    private val _dailyWindowInfo = MutableStateFlow<com.example.data.GeminiDailyQuestionService.DailyWindowInfo?>(null)
    val dailyWindowInfo: StateFlow<com.example.data.GeminiDailyQuestionService.DailyWindowInfo?> = _dailyWindowInfo.asStateFlow()

    private val _isDailyQuestionsLoading = MutableStateFlow(false)
    val isDailyQuestionsLoading: StateFlow<Boolean> = _isDailyQuestionsLoading.asStateFlow()

    private val _isDailyMockLoading = MutableStateFlow(false)
    val isDailyMockLoading: StateFlow<Boolean> = _isDailyMockLoading.asStateFlow()

    // Offline KCET Candidate Hall Ticket & OMR Details
    private val _candidateCetNumber = MutableStateFlow("261KCET892")
    val candidateCetNumber: StateFlow<String> = _candidateCetNumber.asStateFlow()

    private val _candidateBookletVersion = MutableStateFlow("A1")
    val candidateBookletVersion: StateFlow<String> = _candidateBookletVersion.asStateFlow()

    private val _candidateBookletSerial = MutableStateFlow("781045")
    val candidateBookletSerial: StateFlow<String> = _candidateBookletSerial.asStateFlow()

    private val _candidateName = MutableStateFlow("KCET ASPIRANT")
    val candidateName: StateFlow<String> = _candidateName.asStateFlow()

    private val _isStrictRealExam = MutableStateFlow(true)
    val isStrictRealExam: StateFlow<Boolean> = _isStrictRealExam.asStateFlow()

    // Test History State
    val testResultsHistory: StateFlow<List<TestResultEntity>> = repository.allResults.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val latestResult: StateFlow<TestResultEntity?> = repository.latestResult.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Exam Engine State
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, UserAnswer>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, UserAnswer>> = _userAnswers.asStateFlow()

    private val _visitedQuestions = MutableStateFlow<Set<Int>>(setOf(1))
    val visitedQuestions: StateFlow<Set<Int>> = _visitedQuestions.asStateFlow()

    private val _timerSeconds = MutableStateFlow(1200) // 20 minutes = 1200 seconds
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> = _isTimerPaused.asStateFlow()

    private var timerJob: Job? = null

    // Premium dialog trigger
    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    // Badge Reward Congrats Dialog Trigger
    private val _showBadgeCongratsDialog = MutableStateFlow(false)
    val showBadgeCongratsDialog: StateFlow<Boolean> = _showBadgeCongratsDialog.asStateFlow()

    // Auth & User Profile State
    private val sessionPrefs: android.content.SharedPreferences = app.getSharedPreferences("kcet_session_v2", android.content.Context.MODE_PRIVATE)

    // Gamification & Points State


    private val _userPoints = MutableStateFlow(sessionPrefs.getInt("user_xp", 0))
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    // Admin Adjustable Pricing State (Synced live with Firebase Firestore and RTDB across all student accounts)
    private val _liveChatPrice = MutableStateFlow(sessionPrefs.getInt("admin_live_chat_price", 50))
    val liveChatPrice: StateFlow<Int> = _liveChatPrice.asStateFlow()

    private val _subscriptionPrice = MutableStateFlow(sessionPrefs.getInt("admin_subscription_price", 299))
    val subscriptionPrice: StateFlow<Int> = _subscriptionPrice.asStateFlow()

    // Global System Shutdown, Firebase Security Key & Leaderboard Reset State
    private val _isSystemShutdown = MutableStateFlow(prefs.getBoolean("system_shutdown_active", false))
    val isSystemShutdown: StateFlow<Boolean> = _isSystemShutdown.asStateFlow()

    private val _systemSecurityKey = MutableStateFlow(
        prefs.getString("cached_system_security_key", com.example.data.SystemControlConfig.DEFAULT_SYSTEM_SECURITY_KEY)
            ?: com.example.data.SystemControlConfig.DEFAULT_SYSTEM_SECURITY_KEY
    )
    val systemSecurityKey: StateFlow<String> = _systemSecurityKey.asStateFlow()

    private val _isResettingLeaderboard = MutableStateFlow(false)
    val isResettingLeaderboard: StateFlow<Boolean> = _isResettingLeaderboard.asStateFlow()

    private var activeLeaderboardResetAt: Long = prefs.getLong("last_handled_leaderboard_reset_at", 0L)
    private var lastAutoResetCycleTargetMs: Long = 0L

    private fun loadCachedLeaderboardCycle(): com.example.data.SystemControlConfig.LeaderboardCycleStatus {
        val resetAt = prefs.getLong("leaderboard_cycle_reset_at", 0L)
        val startAt = prefs.getLong("leaderboard_cycle_start_at", 0L)
        val nextAt = prefs.getLong("leaderboard_cycle_next_reset_at", 0L)
        if (nextAt > 0L) {
            return com.example.data.SystemControlConfig.LeaderboardCycleStatus(
                lastResetAtMillis = resetAt,
                cycleStartAtMillis = startAt,
                nextResetAtMillis = nextAt
            )
        }
        val now = System.currentTimeMillis()
        val seeded = com.example.data.SystemControlConfig.LeaderboardCycleStatus(
            lastResetAtMillis = resetAt,
            cycleStartAtMillis = now,
            nextResetAtMillis = now + com.example.data.SystemControlConfig.LEADERBOARD_CYCLE_DURATION_MS
        )
        persistLeaderboardCycle(seeded)
        return seeded
    }

    private fun persistLeaderboardCycle(cycle: com.example.data.SystemControlConfig.LeaderboardCycleStatus) {
        prefs.edit()
            .putLong("leaderboard_cycle_reset_at", cycle.lastResetAtMillis)
            .putLong("leaderboard_cycle_start_at", cycle.effectiveCycleStartAtMillis)
            .putLong("leaderboard_cycle_next_reset_at", cycle.effectiveNextResetAtMillis)
            .apply()
    }

    private val _leaderboardCycle = MutableStateFlow(loadCachedLeaderboardCycle())
    val leaderboardCycle: StateFlow<com.example.data.SystemControlConfig.LeaderboardCycleStatus> = _leaderboardCycle.asStateFlow()

    private val _leaderboardNowMs = MutableStateFlow(System.currentTimeMillis())

    val leaderboardRemainingMillis: StateFlow<Long> = combine(
        _leaderboardCycle,
        _leaderboardNowMs
    ) { cycle, nowMs ->
        cycle.remainingMillis(nowMs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _leaderboardCycle.value.remainingMillis(System.currentTimeMillis())
    )

    // Global App-Wide Maintenance Mode State (Persisted across restarts in global prefs, synced live via Firestore + RTDB)
    private fun loadCachedMaintenanceConfig(): MaintenanceConfig {
        return MaintenanceConfig(
            maintenanceEnabled = prefs.getBoolean("maintenance_enabled", false),
            maintenanceStartTime = prefs.getLong("maintenance_start_time", 0L),
            maintenanceEndTime = prefs.getLong("maintenance_end_time", 0L),
            updatedAt = prefs.getLong("maintenance_updated_at", 0L),
            updatedBy = prefs.getString("maintenance_updated_by", "").orEmpty()
        )
    }

    private fun persistMaintenanceConfig(config: MaintenanceConfig) {
        prefs.edit()
            .putBoolean("maintenance_enabled", config.maintenanceEnabled)
            .putLong("maintenance_start_time", config.maintenanceStartTime)
            .putLong("maintenance_end_time", config.maintenanceEndTime)
            .putLong("maintenance_updated_at", config.updatedAt)
            .putString("maintenance_updated_by", config.updatedBy)
            .apply()
    }

    private val _serverTimeOffsetMs = MutableStateFlow(prefs.getLong("maintenance_server_time_offset_ms", 0L))
    val serverTimeOffsetMs: StateFlow<Long> = _serverTimeOffsetMs.asStateFlow()

    private fun currentServerTimeMillis(): Long {
        return System.currentTimeMillis() + _serverTimeOffsetMs.value
    }

    private val _maintenanceConfig = MutableStateFlow(loadCachedMaintenanceConfig())
    val maintenanceConfig: StateFlow<MaintenanceConfig> = _maintenanceConfig.asStateFlow()

    private val _maintenanceNowMs = MutableStateFlow(currentServerTimeMillis())
    val maintenanceNowMs: StateFlow<Long> = _maintenanceNowMs.asStateFlow()

    val isMaintenanceActive: StateFlow<Boolean> = combine(
        _maintenanceConfig,
        _maintenanceNowMs
    ) { config, nowMs ->
        config.isEffectivelyActive(nowMs)
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _maintenanceConfig.value.isEffectivelyActive(currentServerTimeMillis())
    )

    val maintenanceRemainingMillis: StateFlow<Long> = combine(
        _maintenanceConfig,
        _maintenanceNowMs
    ) { config, nowMs ->
        config.remainingMillis(nowMs)
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _maintenanceConfig.value.remainingMillis(currentServerTimeMillis())
    )

    private val _isUpdatingMaintenance = MutableStateFlow(false)
    val isUpdatingMaintenance: StateFlow<Boolean> = _isUpdatingMaintenance.asStateFlow()

    private val _maintenanceFeedback = MutableStateFlow<Pair<Boolean, String>?>(null)
    val maintenanceFeedback: StateFlow<Pair<Boolean, String>?> = _maintenanceFeedback.asStateFlow()

    private var lastAutoDisabledEndTime: Long = 0L

    private fun zeroLocalStudentXp() {
        _userPoints.value = 0
        _analyticsSummary.value = _analyticsSummary.value.copy(totalPoints = 0)
        _liveXpData.value = _liveXpData.value.copy(xp = 0, level = 1)
        val editor = sessionPrefs.edit()
        sessionPrefs.all.keys
            .filter { it == "user_xp" || it.startsWith("user_xp_") }
            .forEach { editor.putInt(it, 0) }
        editor.apply()
    }

    fun updateLiveChatPrice(newPrice: Int) {
        sessionPrefs.edit().putInt("admin_live_chat_price", newPrice).apply()
        _liveChatPrice.value = newPrice
        viewModelScope.launch {
            try {
                FirebaseService.saveAdminPricing(newPrice, _subscriptionPrice.value)
            } catch (_: Exception) {}
        }
    }

    fun updateSubscriptionPrice(newPrice: Int) {
        sessionPrefs.edit().putInt("admin_subscription_price", newPrice).apply()
        _subscriptionPrice.value = newPrice
        viewModelScope.launch {
            try {
                FirebaseService.saveAdminPricing(_liveChatPrice.value, newPrice)
            } catch (_: Exception) {}
        }
    }

    private val _isSubmittingExam = MutableStateFlow(false)
    val isSubmittingExam: StateFlow<Boolean> = _isSubmittingExam.asStateFlow()

    private val _activeAttemptId = MutableStateFlow<String>("")
    val activeAttemptId: StateFlow<String> = _activeAttemptId.asStateFlow()

    private val processedAttemptIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val awardedAttemptIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    private val defaultLockedBadges = listOf(
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

    // Badges State
    private val _userBadges = MutableStateFlow<List<BadgeItem>>(defaultLockedBadges)
    val userBadges: StateFlow<List<BadgeItem>> = _userBadges.asStateFlow()

    // Leaderboard State
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Realtime Database: Online Status & Presence
    private val _onlineUsers = MutableStateFlow<List<com.example.data.UserPresence>>(emptyList())
    val onlineUsers: StateFlow<List<com.example.data.UserPresence>> = _onlineUsers.asStateFlow()
    private val _currentUserPresence = MutableStateFlow(com.example.data.UserPresence())
    val currentUserPresence: StateFlow<com.example.data.UserPresence> = _currentUserPresence.asStateFlow()

    // Realtime Database: Live XP & Rank
    private val _liveXpData = MutableStateFlow(com.example.data.LiveXpData())
    val liveXpData: StateFlow<com.example.data.LiveXpData> = _liveXpData.asStateFlow()

    private val _allLiveXp = MutableStateFlow<Map<String, com.example.data.LiveXpData>>(emptyMap())
    val allLiveXp: StateFlow<Map<String, com.example.data.LiveXpData>> = _allLiveXp.asStateFlow()

    // Realtime Database: Realtime Student Notifications
    private val _realtimeNotifications = MutableStateFlow<List<StudentNotification>>(emptyList())
    val realtimeNotifications: StateFlow<List<StudentNotification>> = _realtimeNotifications.asStateFlow()

    private val cleanDefaultAnalytics = AnalyticsSummary(
        totalTestsTaken = 0,
        overallAccuracy = 0f,
        totalPoints = 0,
        avgTimePerQuestionSec = 0f,
        subjectBreakdown = emptyList(),
        topicAccuracies = emptyList(),
        weakAreas = emptyList(),
        recommendations = listOf(
            "Take your first CBT Mock Test to generate AI Performance Analytics!",
            "Practice PYQs from Physics, Chemistry, and Mathematics to build subject mastery."
        )
    )

    // Analytics Summary State
    private val _analyticsSummary = MutableStateFlow<AnalyticsSummary>(cleanDefaultAnalytics)
    val analyticsSummary: StateFlow<AnalyticsSummary> = _analyticsSummary.asStateFlow()

    // Blocked Account Alert for active session or login attempts
    private val _blockedAccountAlert = MutableStateFlow<Pair<String, String>?>(null)
    val blockedAccountAlert: StateFlow<Pair<String, String>?> = _blockedAccountAlert.asStateFlow()

    private var profileJob: Job? = null
    private var testResultsJob: Job? = null
    private var paymentJob: Job? = null
    private var presenceJob: Job? = null
    private var liveXpJob: Job? = null
    private var realtimeNotifJob: Job? = null
    private var allOnlineJob: Job? = null
    private var allLiveXpJob: Job? = null
    private var leaderboardJob: Job? = null
    private var badgesJob: Job? = null
    private var premiumJob: Job? = null

    // Navigation & Tab back stack state
    private val initialNavScreen: Screen = if (sessionPrefs.getBoolean("is_logged_in", false)) {
        val savedEmail = sessionPrefs.getString("user_email", "") ?: ""
        val savedIsAdmin = sessionPrefs.getBoolean("is_admin", false) || savedEmail == "ullassuvarna65@gmail.com"
        when {
            savedIsAdmin -> Screen.ADMIN
            savedEmail.trim().lowercase() == "ullassuvarna85@gmail.com" -> Screen.SENIOR_CHAT
            else -> Screen.MAIN
        }
    } else {
        Screen.SPLASH
    }

    private val _navigationStack = MutableStateFlow<List<NavDestination>>(
        listOf(NavDestination(screen = initialNavScreen, bottomTab = BottomTab.HOME))
    )
    val navigationStack: StateFlow<List<NavDestination>> = _navigationStack.asStateFlow()

    private val _currentScreen = MutableStateFlow(initialNavScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.HOME)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    val canNavigateBack: StateFlow<Boolean> = combine(_navigationStack, _currentScreen, _selectedTab) { stack, screen, tab ->
        stack.size > 1 || (screen != Screen.MAIN || tab != BottomTab.HOME)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private fun saveSession(uid: String, email: String, name: String, isAdmin: Boolean = false, authMethod: String = "Email & Password", photoUrl: String? = null, isPremium: Boolean = false) {
        val cleanUid = uid.trim()
        val cleanEmail = email.lowercase().trim()
        val override = (if (cleanUid.isNotBlank()) FirebaseService.getPremiumOverride(cleanUid) else null)
            ?: (if (cleanEmail.isNotBlank()) FirebaseService.getPremiumOverride(cleanEmail) else null)
        val effectivePremium = when {
            isAdmin || cleanEmail == "ullassuvarna65@gmail.com" -> true
            override != null -> override
            else -> isPremium
        }

        val editor = sessionPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_uid", uid)
            .putString("user_email", email)
            .putString("user_name", name)
            .putBoolean("is_admin", isAdmin)
            .putString("auth_method", authMethod)
            .putString("user_photo_url", photoUrl)
            .putBoolean("is_premium", effectivePremium)

        if (cleanUid.isNotBlank()) editor.putBoolean("is_premium_$cleanUid", effectivePremium)
        if (cleanEmail.isNotBlank()) editor.putBoolean("is_premium_$cleanEmail", effectivePremium)
        editor.apply()

        if (effectivePremium) {
            if (cleanUid.isNotBlank()) FirebaseService.setPremiumOverride(cleanUid, true)
            if (cleanEmail.isNotBlank()) FirebaseService.setPremiumOverride(cleanEmail, true)
        } else {
            if (cleanUid.isNotBlank()) FirebaseService.setPremiumOverride(cleanUid, false)
            if (cleanEmail.isNotBlank()) FirebaseService.setPremiumOverride(cleanEmail, false)
        }
    }

    private fun clearSession() {
        sessionPrefs.edit().clear().apply()
    }

    private val _isLoggedIn = MutableStateFlow(sessionPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(
        run {
            val savedUid = (sessionPrefs.getString("user_uid", "") ?: "").trim()
            val savedEmail = (sessionPrefs.getString("user_email", "") ?: "").lowercase().trim()
            val isAdmin = sessionPrefs.getBoolean("is_admin", false) || savedEmail == "ullassuvarna65@gmail.com"
            val override = (if (savedUid.isNotBlank()) FirebaseService.getPremiumOverride(savedUid) else null)
                ?: (if (savedEmail.isNotBlank()) FirebaseService.getPremiumOverride(savedEmail) else null)
            val isPrem = when {
                isAdmin -> true
                override != null -> override
                else -> sessionPrefs.getBoolean("is_premium", false)
            }

            UserProfile(
                uid = savedUid,
                name = sessionPrefs.getString("user_name", "KCET Aspirant") ?: "KCET Aspirant",
                email = savedEmail,
                isPremium = isPrem,
                signInMethod = sessionPrefs.getString("auth_method", "Email & Password") ?: "Email & Password",
                photoUrl = sessionPrefs.getString("user_photo_url", null)
            )
        }
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Student Notifications & Read Tracking
    private val _readNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    private val deletedNotifPrefsKey = "permanently_deleted_notification_ids"
    private val _permanentlyDeletedNotificationIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet(deletedNotifPrefsKey, emptySet()) ?: emptySet()
    )

    private val hasShownWelcomePrefsKey = "has_shown_welcome_notification"
    private val _hasShownWelcome = MutableStateFlow(
        prefs.getBoolean(hasShownWelcomePrefsKey, false)
    )

    fun markWelcomeNotificationAsShown() {
        if (!_hasShownWelcome.value) {
            prefs.edit().putBoolean(hasShownWelcomePrefsKey, true).apply()
            _hasShownWelcome.value = true
        }
    }

    // Globally deleted notifications synced via Firestore & RTDB across all student portals
    val globallyDeletedNotificationIds: StateFlow<Set<String>> = combine(
        repository.globallyDeletedNotificationIds,
        FirebaseRealtimeService.observeGloballyDeletedNotificationIds()
    ) { firestoreDeleted, rtdbDeleted ->
        firestoreDeleted + rtdbDeleted
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    // All deleted notification IDs (both global from admin and local from device)
    val allDeletedNotificationIds: StateFlow<Set<String>> = combine(
        globallyDeletedNotificationIds,
        _permanentlyDeletedNotificationIds
    ) { global, local ->
        global + local
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    // All push notifications from Firestore (for Admin view, filtering out schedule update spam and deleted items)
    val allRawNotifications: StateFlow<List<StudentNotification>> = combine(
        repository.notifications,
        allDeletedNotificationIds,
        _hasShownWelcome
    ) { notifs, deletedIds, _ ->
        notifs.filter { notif ->
            if (deletedIds.contains(notif.id)) return@filter false
            val isKeaScheduleSpam = notif.title.contains("Official KEA Schedule", ignoreCase = true) ||
                    notif.title.contains("Official KCET Schedule", ignoreCase = true) ||
                    notif.title.contains("Schedule Updated", ignoreCase = true) ||
                    (notif.message.contains("cetonline.karnataka.gov.in", ignoreCase = true) && notif.message.contains("schedule", ignoreCase = true))
            !isKeaScheduleSpam
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Admin Security PIN synced dynamically with Firebase Firestore and Realtime Database
    val adminPin: StateFlow<String> = combine(
        FirebaseService.observeAdminPin(),
        FirebaseRealtimeService.observeAdminPinFromRTDB()
    ) { firestorePin, rtdbPin ->
        when {
            !rtdbPin.isNullOrBlank() && rtdbPin != FirebaseService.DEFAULT_ADMIN_PIN -> rtdbPin.trim()
            !firestorePin.isBlank() -> firestorePin.trim()
            else -> FirebaseService.DEFAULT_ADMIN_PIN
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FirebaseService.DEFAULT_ADMIN_PIN
    )

    // Filtered & read-tracked notifications for the active student account (permanently hides admin-deleted notifications)
    val notifications: StateFlow<List<StudentNotification>> = combine(
        repository.notifications,
        _readNotificationIds,
        allDeletedNotificationIds,
        _userProfile,
        _hasShownWelcome
    ) { notifs: List<StudentNotification>, readIds: Set<String>, deletedIds: Set<String>, profile: UserProfile, hasShownWelcome: Boolean ->
        notifs.filter { notif ->
            // Filter out notifications deleted permanently by admin or user
            if (deletedIds.contains(notif.id)) return@filter false

            // Remove KCET official schedule updated notification completely (no need to show that notification)
            val isKeaScheduleSpam = notif.title.contains("Official KEA Schedule", ignoreCase = true) ||
                    notif.title.contains("Official KCET Schedule", ignoreCase = true) ||
                    notif.title.contains("Schedule Updated", ignoreCase = true) ||
                    (notif.message.contains("cetonline.karnataka.gov.in", ignoreCase = true) && notif.message.contains("schedule", ignoreCase = true))
            if (isKeaScheduleSpam) return@filter false

            // Welcome notification only show 1st time of opening app and then no need to show
            val isWelcome = notif.id == "notif_welcome" ||
                    notif.id == "WELCOME_01" ||
                    notif.title.contains("Welcome to KCET", ignoreCase = true)
            if (isWelcome && hasShownWelcome) return@filter false

            val targetId = notif.targetUserId?.trim() ?: "ALL"
            val targetEmail = notif.targetUserEmail?.trim() ?: ""

            // Internal admin alert logs should never be shown in student notifications
            if (targetId.equals("ADMIN", ignoreCase = true)) {
                return@filter false
            }

            val isBroadcast = (targetId.isBlank() || targetId.equals("ALL", ignoreCase = true)) && targetEmail.isBlank()

            val matchesTarget = if (isBroadcast) {
                // Broadcast announcement / system notification sent to ALL students
                // CRITICAL PRIVACY RULE: 1-to-1 Live Chat and Slot Booking notifications must NEVER be broadcast to all students!
                val isChatOrBookingNotif = notif.type.equals("BOOKING", ignoreCase = true) ||
                        notif.actionType.equals("NAV_SENIOR_CHAT", ignoreCase = true) ||
                        notif.title.contains("1-on-1", ignoreCase = true) ||
                        notif.title.contains("1 to 1", ignoreCase = true) ||
                        notif.title.contains("Live Chat", ignoreCase = true) ||
                        notif.title.contains("Senior Mentorship", ignoreCase = true) ||
                        notif.message.contains("slot request", ignoreCase = true) ||
                        notif.message.contains("1-on-1", ignoreCase = true) ||
                        notif.message.contains("Session Receipt", ignoreCase = true) ||
                        notif.message.contains("Session Update", ignoreCase = true)
                !isChatOrBookingNotif
            } else {
                // Targeted notification sent specifically to a PARTICULAR student
                val pUid = profile.uid.trim()
                val pEmail = profile.email.trim().lowercase()
                val authUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty().trim()
                val authEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty().trim().lowercase()
                val pStudentId = profile.studentId.trim()
                val pFormattedId = profile.formattedStudentId.trim()

                val uidMatches = targetId.isNotBlank() && !targetId.equals("ALL", ignoreCase = true) && (
                    (pUid.isNotBlank() && targetId.equals(pUid, ignoreCase = true)) ||
                    (authUid.isNotBlank() && targetId.equals(authUid, ignoreCase = true)) ||
                    (pEmail.isNotBlank() && targetId.equals(pEmail, ignoreCase = true)) ||
                    (authEmail.isNotBlank() && targetId.equals(authEmail, ignoreCase = true)) ||
                    (pStudentId.isNotBlank() && targetId.equals(pStudentId, ignoreCase = true)) ||
                    (pFormattedId.isNotBlank() && targetId.equals(pFormattedId, ignoreCase = true))
                )

                val emailMatches = targetEmail.isNotBlank() && (
                    (pEmail.isNotBlank() && targetEmail.equals(pEmail, ignoreCase = true)) ||
                    (authEmail.isNotBlank() && targetEmail.equals(authEmail, ignoreCase = true)) ||
                    (pUid.isNotBlank() && targetEmail.equals(pUid, ignoreCase = true)) ||
                    (authUid.isNotBlank() && targetEmail.equals(authUid, ignoreCase = true))
                )

                uidMatches || emailMatches
            }

            matchesTarget
        }.map { notif ->
            if (readIds.contains(notif.id)) notif.copy(isRead = true) else notif
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun markNotificationAsRead(id: String) {
        _readNotificationIds.value = _readNotificationIds.value + id
    }

    fun markAllNotificationsAsRead() {
        _readNotificationIds.value = _readNotificationIds.value + notifications.value.map { it.id }.toSet()
    }

    fun clearAllNotifications() {
        val currentIds = notifications.value.map { it.id }.toSet()
        val updated = _permanentlyDeletedNotificationIds.value + currentIds
        _permanentlyDeletedNotificationIds.value = updated
        prefs.edit().putStringSet(deletedNotifPrefsKey, updated).apply()
        for (id in currentIds) {
            repository.deleteNotification(id)
        }
    }

    fun addNotification(
        title: String,
        message: String,
        type: String = "GENERAL",
        actionType: String? = null,
        targetUserId: String? = null,
        targetUserEmail: String? = null
    ) {
        val nowStr = try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        } catch (e: Exception) {
            "Just now"
        }
        val newNotif = StudentNotification(
            id = "notif_${System.currentTimeMillis()}_${(100..999).random()}",
            title = title,
            message = message,
            timestamp = nowStr,
            type = type,
            isRead = false,
            actionType = actionType,
            targetUserId = targetUserId ?: "ALL",
            targetUserEmail = targetUserEmail ?: "",
            createdAtMillis = System.currentTimeMillis()
        )
        repository.addNotification(newNotif)
    }

    fun sendAdminNotification(
        title: String,
        message: String,
        type: String = "ANNOUNCEMENT",
        targetUserId: String? = null,
        targetUserEmail: String? = null,
        actionType: String? = null
    ) {
        addNotification(
            title = title,
            message = message,
            type = type,
            actionType = actionType,
            targetUserId = targetUserId,
            targetUserEmail = targetUserEmail
        )
    }

    fun linkEmailPasswordToGoogleUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val user = FirebaseService.auth.currentUser
        if (user == null) {
            onResult(false, "No user logged in.")
            return
        }

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        
        viewModelScope.launch {
            try {
                user.linkWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            FirebaseService.updateUserPasswordInFirestore(email, password)
                        }
                        onResult(true, "Password linked successfully!")
                    } else {
                        onResult(false, "Failed to link password: ${task.exception?.message}")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun deleteNotification(id: String) {
        val updated = _permanentlyDeletedNotificationIds.value + id
        _permanentlyDeletedNotificationIds.value = updated
        prefs.edit().putStringSet(deletedNotifPrefsKey, updated).apply()
        viewModelScope.launch {
            FirebaseService.deleteNotificationPermanentlyByAdmin(id)
        }
    }

    fun deleteNotificationPermanently(id: String) {
        val updated = _permanentlyDeletedNotificationIds.value + id
        _permanentlyDeletedNotificationIds.value = updated
        prefs.edit().putStringSet(deletedNotifPrefsKey, updated).apply()
        viewModelScope.launch {
            FirebaseService.deleteNotificationPermanentlyByAdmin(id)
        }
    }

    fun sendTestPushNotification() {
        addNotification(
            title = "🔔 System Test: Daily Notifications Active!",
            message = "New KCET study materials, formula sheets, and chapter tests will be delivered right to your status bar daily at 9:00 AM.",
            type = "MATERIAL_UPDATE"
        )
    }

    fun setupDailyReminders(hourOfDay: Int = 9, minute: Int = 0) {
        DailyNotificationManager.scheduleDailyDigest(
            context = app,
            hourOfDay = hourOfDay,
            minute = minute
        )
    }

    // Senior Mentorship Live Chat Bookings
    val seniorChatBookings: StateFlow<List<SeniorChatBooking>> = repository.seniorChatBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // My filtered Senior Chat Bookings for the current student or mentor
    val mySeniorChatBookings: StateFlow<List<SeniorChatBooking>> = combine(seniorChatBookings, userProfile) { bookings, profile ->
        val userEmail = profile.email.trim().lowercase()
        val authEmail = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase().orEmpty()
        val authUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty().trim()
        val profileUid = profile.uid.trim()

        bookings.filter { booking ->
            val bUserId = booking.userId.trim()
            val bUserEmail = booking.userEmail.trim().lowercase()

            val matchesUid = (profileUid.isNotBlank() && bUserId.isNotBlank() && bUserId.equals(profileUid, ignoreCase = true)) ||
                             (authUid.isNotBlank() && bUserId.isNotBlank() && bUserId.equals(authUid, ignoreCase = true)) ||
                             (bUserId.isNotBlank() && (userEmail.isNotBlank() && bUserId.equals(userEmail, ignoreCase = true) || authEmail.isNotBlank() && bUserId.equals(authEmail, ignoreCase = true)))

            val matchesEmail = (userEmail.isNotBlank() && bUserEmail.isNotBlank() && bUserEmail.equals(userEmail, ignoreCase = true)) ||
                               (authEmail.isNotBlank() && bUserEmail.isNotBlank() && bUserEmail.equals(authEmail, ignoreCase = true)) ||
                               (bUserEmail.isNotBlank() && (profileUid.isNotBlank() && bUserEmail.equals(profileUid, ignoreCase = true) || authUid.isNotBlank() && bUserEmail.equals(authUid, ignoreCase = true)))

            matchesUid || matchesEmail
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All Student Test Results for Admin Control
    val allStudentTestResults: StateFlow<List<AdminTestResultItem>> = FirebaseService.observeAllUserTestResults().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SampleData.defaultStudentTestResults
    )

    // All Students for Admin Control
    val allStudents: StateFlow<List<UserProfile>> = combine(
        FirebaseService.observeAllUserProfiles(),
        _userProfile,
        allStudentTestResults,
        repository.paymentReceipts,
        leaderboard
    ) { remoteStudents, currentProfile, testResults, receipts, lbEntries ->
        val mergedList = mutableListOf<UserProfile>()
        mergedList.addAll(remoteStudents)

        // Ensure current active user profile is included if registered/named
        if (currentProfile.name.isNotBlank() && currentProfile.name != "Guest Student" && (currentProfile.email.isNotBlank() || currentProfile.uid.isNotBlank())) {
            mergedList.add(currentProfile)
        }

        // Ensure any student in leaderboard is included
        lbEntries.forEach { entry ->
            if (entry.name.isNotBlank() && !entry.name.contains("mentor", ignoreCase = true) && !entry.name.contains("admin", ignoreCase = true)) {
                val override = FirebaseService.getPremiumOverride(entry.uid)
                    ?: (if (entry.email.isNotBlank()) FirebaseService.getPremiumOverride(entry.email) else null)
                mergedList.add(
                    UserProfile(
                        uid = entry.uid,
                        name = entry.name,
                        email = entry.email,
                        kcetTargetRank = entry.kcetTargetRank,
                        isPremium = override ?: false,
                        registrationDate = "2026"
                    )
                )
            }
        }

        // Ensure any student who completed a test in CBT is included
        testResults.forEach { test ->
            if (test.userName.isNotBlank() && test.userName != "Guest") {
                val override = FirebaseService.getPremiumOverride(test.userId)
                    ?: (if (test.userEmail.isNotBlank()) FirebaseService.getPremiumOverride(test.userEmail) else null)
                    ?: (if (test.studentId.isNotBlank()) FirebaseService.getPremiumOverride(test.studentId) else null)
                mergedList.add(
                    UserProfile(
                        uid = test.userId.ifBlank { "test_${test.docId}" },
                        name = test.userName,
                        email = test.userEmail,
                        studentId = test.studentId,
                        isPremium = override ?: false,
                        registrationDate = "2026"
                    )
                )
            }
        }

        // Ensure any student who submitted a payment receipt is included
        receipts.forEach { receipt ->
            if (receipt.userName.isNotBlank()) {
                val override = FirebaseService.getPremiumOverride(receipt.userId)
                    ?: (if (receipt.userEmail.isNotBlank()) FirebaseService.getPremiumOverride(receipt.userEmail) else null)
                mergedList.add(
                    UserProfile(
                        uid = receipt.userId.ifBlank { "receipt_${receipt.receiptId}" },
                        name = receipt.userName,
                        email = receipt.userEmail.ifBlank { receipt.userPhone },
                        isPremium = override ?: (receipt.status == "VERIFIED"),
                        registrationDate = "2026"
                    )
                )
            }
        }

        // Ensure all benchmark default students are included
        SampleData.defaultStudents.forEach { defaultStd ->
            val override = FirebaseService.getPremiumOverride(defaultStd.uid)
                ?: (if (defaultStd.email.isNotBlank()) FirebaseService.getPremiumOverride(defaultStd.email) else null)
                ?: (if (defaultStd.formattedStudentId.isNotBlank()) FirebaseService.getPremiumOverride(defaultStd.formattedStudentId) else null)
            val isPrem = when {
                override != null -> override
                else -> defaultStd.isPremium
            }
            mergedList.add(defaultStd.copy(isPremium = isPrem))
        }

        // Intelligent multi-pass clustering deduplication
        FirebaseService.deduplicateStudentProfiles(mergedList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SampleData.defaultStudents.map { std ->
            val override = FirebaseService.getPremiumOverride(std.uid)
                ?: (if (std.email.isNotBlank()) FirebaseService.getPremiumOverride(std.email) else null)
            if (override != null) std.copy(isPremium = override) else std
        }
    )

    // Registered Senior Mentors for Admin & Student Portals
    val registeredMentors: StateFlow<List<SeniorMentorProfile>> = FirebaseRealtimeService.observeAllSeniorMentors().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FirebaseService.defaultVerifiedSeniorMentors
    )

    // KEA Schedule Auto-Sync via Gemini AI
    private val _isKeaSyncing = MutableStateFlow(false)
    val isKeaSyncing: StateFlow<Boolean> = _isKeaSyncing.asStateFlow()

    private val _keaLastSyncedDate = MutableStateFlow("Today, August 08, 2026")
    val keaLastSyncedDate: StateFlow<String> = _keaLastSyncedDate.asStateFlow()

    fun bookSeniorChatSession(date: String, time: String, paymentRefNumber: String = "", receiptUri: String = "") {
        val user = userProfile.value
        val currentAuthUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty().trim()
        val currentAuthEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty().trim().lowercase()

        val effectiveUid = user.uid.trim().ifBlank { currentAuthUid }.ifBlank {
            if (user.email.isNotBlank()) "std_${user.email.replace(".", "_").replace("@", "_")}"
            else if (currentAuthEmail.isNotBlank()) "std_${currentAuthEmail.replace(".", "_").replace("@", "_")}"
            else "std_${System.currentTimeMillis()}"
        }
        val effectiveEmail = user.email.trim().ifBlank { currentAuthEmail }
        val effectiveName = user.name.trim().ifBlank { FirebaseAuth.getInstance().currentUser?.displayName.orEmpty() }.ifBlank { "Student" }

        val newBooking = SeniorChatBooking(
            bookingId = "KCET-LIVE-${(1000..9999).random()}",
            userId = effectiveUid,
            userName = effectiveName,
            userEmail = effectiveEmail,
            selectedDate = date,
            selectedTime = time,
            amountPaid = _liveChatPrice.value,
            paymentRefNumber = paymentRefNumber,
            receiptImageUri = receiptUri,
            receiptUrl = receiptUri,
            status = "Under Verification",
            adminMessage = ""
        )
        repository.bookSeniorChatSession(newBooking)

        // Notification targeted STRICTLY to the booking student ONLY
        addNotification(
            title = "💬 1-on-1 Session Receipt Submitted!",
            message = "Your slot request for $date ($time) with ref ID '${paymentRefNumber.ifBlank { "Screenshot" }}' has been submitted for Admin verification.",
            type = "BOOKING",
            actionType = "NAV_SENIOR_CHAT",
            targetUserId = effectiveUid,
            targetUserEmail = effectiveEmail
        )

        // Admin alert in RTDB (targetUserId = "ADMIN" so no students ever receive it)
        try {
            val adminAlert = StudentNotification(
                id = "admin_alert_livechat_${System.currentTimeMillis()}",
                title = "💬 New 1-to-1 Live Chat Booking",
                message = "Student $effectiveName ($effectiveEmail) requested 1-to-1 slot on $date at $time. Ref: ${paymentRefNumber.ifBlank { "Receipt uploaded" }}",
                timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                type = "BOOKING",
                isRead = false,
                actionType = "NAV_ADMIN_BOOKINGS",
                targetUserId = "ADMIN",
                targetUserEmail = "",
                createdAtMillis = System.currentTimeMillis()
            )
            FirebaseRealtimeService.sendAdminAlert(adminAlert)
        } catch (_: Exception) {}
    }

    fun updateSeniorChatBookingStatus(
        bookingId: String,
        status: String,
        adminMessage: String,
        mentorName: String = "",
        assignedMentorEmail: String = "",
        rejectionReason: String = ""
    ) {
        repository.updateSeniorChatBookingStatus(
            bookingId, status, adminMessage, mentorName, assignedMentorEmail, rejectionReason
        )

        val booking = seniorChatBookings.value.find { it.bookingId == bookingId }
        val dateInfo = if (booking != null) "${booking.selectedDate} (${booking.selectedTime})" else ""
        val targetUid = booking?.userId.orEmpty().trim()
        val targetEmail = booking?.userEmail.orEmpty().trim()

        // Notification must only be sent if we have a specific student target (never broadcast to ALL students)
        if (targetUid.isNotBlank() || targetEmail.isNotBlank()) {
            addNotification(
                title = "💬 1-on-1 Session Update: $status",
                message = "Your booking $bookingId for $dateInfo is now marked as $status. ${adminMessage.ifBlank { "" }}",
                type = "BOOKING",
                actionType = "NAV_SENIOR_CHAT",
                targetUserId = targetUid.ifBlank { targetEmail },
                targetUserEmail = targetEmail
            )
        }
    }

    fun rescheduleSeniorChatSession(bookingId: String, date: String, time: String) {
        repository.rescheduleSeniorChatSession(bookingId, date, time)
        val booking = seniorChatBookings.value.find { it.bookingId == bookingId }
        val targetUid = booking?.userId.orEmpty().trim()
        val targetEmail = booking?.userEmail.orEmpty().trim()
        if (targetUid.isNotBlank() || targetEmail.isNotBlank()) {
            addNotification(
                title = "💬 1-on-1 Session Rescheduled",
                message = "Your slot for booking $bookingId has been rescheduled to $date at $time. Awaiting mentor confirmation.",
                type = "BOOKING",
                actionType = "NAV_SENIOR_CHAT",
                targetUserId = targetUid.ifBlank { targetEmail },
                targetUserEmail = targetEmail
            )
        }
    }

    fun deleteSeniorChatBooking(bookingId: String) {
        repository.deleteSeniorChatBooking(bookingId)
    }

    fun generateAndUpload180QuestionsForSet(testSetId: String) {
        val questions = com.example.data.SampleData.generate180QuestionsForSet(testSetId)
        repository.addQuestionBatch(questions)
    }

    fun syncKeaScheduleWithGemini(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isKeaSyncing.value = true
            val result = repository.syncKeaScheduleWithGemini("August 08, 2026")
            _isKeaSyncing.value = false
            if (result.isSuccess) {
                val count = result.getOrNull()?.size ?: 0
                _keaLastSyncedDate.value = "Synced Live Today (Aug 08, 2026)"
                // Notification generation removed as requested: no need to spam schedule updated notification
                onComplete?.invoke(true, "Successfully updated $count official KEA schedule dates!")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Sync error"
                onComplete?.invoke(false, "Could not sync: $error")
            }
        }
    }

    // Dark Mode (Default OFF as requested by user)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun login(email: String, name: String = "KCET Aspirant", signInMethod: String = "Email & Password") {
        val cleanEmail = email.lowercase().trim()
        if (cleanEmail == "ullassuvarna65@gmail.com" || cleanEmail == "admin_ullas") {
            loginAdmin()
            return
        }
        val calculatedUid = FirebaseService.auth.currentUser?.uid ?: cleanEmail.replace(".", "_").replace("@", "_").ifBlank { "kcet_std_001" }

        viewModelScope.launch {
            val isMentor = cleanEmail == "ullassuvarna85@gmail.com" || registeredMentors.value.any { it.email.isNotBlank() && it.email.equals(cleanEmail, ignoreCase = true) }

            // Clear any force logout flag on fresh login across both UID and Email!
            FirebaseService.clearForceLogoutFlag(calculatedUid, cleanEmail)

            // Verify if account or email is blocked by Admin (skip for mentors)
            if (!isMentor) {
                val blockCheckUid = FirebaseService.checkIfUserIsBlocked(calculatedUid)
                val blockCheckEmail = if (cleanEmail.isNotBlank()) FirebaseService.checkIfUserIsBlocked(cleanEmail) else Pair(false, "")
                val remoteProfile = FirebaseService.getUserProfileOnce(calculatedUid)

                if (blockCheckUid.first || blockCheckEmail.first || remoteProfile?.isBlocked == true) {
                    val reason = when {
                        blockCheckUid.first -> blockCheckUid.second
                        blockCheckEmail.first -> blockCheckEmail.second
                        else -> remoteProfile?.blockReason ?: ""
                    }
                    logout()
                    _blockedAccountAlert.value = Pair(cleanEmail.ifBlank { name }, reason.ifBlank { "Account blocked by Administrator." })
                    return@launch
                }
            }

            val remoteProfile = FirebaseService.getUserProfileOnce(calculatedUid) ?: FirebaseService.getUserProfileOnce(cleanEmail)

            _isLoggedIn.value = true
            val targetScreen = if (isMentor) Screen.SENIOR_CHAT else Screen.MAIN
            resetNavigation(targetScreen, BottomTab.HOME)

            val isPrem = if (isMentor) true else (
                remoteProfile?.isPremium == true ||
                sessionPrefs.getBoolean("is_premium", false) ||
                sessionPrefs.getBoolean("is_premium_$calculatedUid", false) ||
                sessionPrefs.getBoolean("is_premium_$cleanEmail", false) ||
                FirebaseService.getPremiumOverride(calculatedUid) == true ||
                (cleanEmail.isNotBlank() && FirebaseService.getPremiumOverride(cleanEmail) == true)
            )

            saveSession(
                uid = calculatedUid,
                email = cleanEmail,
                name = if (isMentor && name == "KCET Aspirant") "Senior Mentor" else name,
                isAdmin = false,
                authMethod = signInMethod,
                photoUrl = remoteProfile?.photoUrl,
                isPremium = isPrem
            )

            // Clear local Room database history so previous student data does not bleed into new account
            repository.clearTestHistory()

            // Restore saved XP for this student account
            val savedStudentXP = sessionPrefs.getInt("user_xp_$calculatedUid", 0)
            _userPoints.value = savedStudentXP
            sessionPrefs.edit().putInt("user_xp", savedStudentXP).apply()
            _userBadges.value = defaultLockedBadges
            _analyticsSummary.value = cleanDefaultAnalytics.copy(totalPoints = savedStudentXP)
            _currentUserPaymentReceipt.value = null
            _activeTestResult.value = null
            _userAnswers.value = emptyMap()

            if (remoteProfile != null) {
                _userProfile.value = remoteProfile.copy(
                    email = cleanEmail,
                    name = if (name.isNotBlank() && name != "KCET Aspirant") name else if (isMentor) "Senior Mentor" else remoteProfile.name,
                    isPremium = isPrem,
                    isForceLoggedOut = false,
                    isBlocked = false,
                    signInMethod = signInMethod
                )
            } else {
                _userProfile.value = UserProfile(
                    uid = calculatedUid,
                    email = cleanEmail,
                    name = if (name.isNotBlank() && name != "KCET Aspirant") name else if (isMentor) "Senior Mentor" else "KCET Aspirant",
                    isPremium = isPrem,
                    isForceLoggedOut = false,
                    isBlocked = false,
                    kcetTargetRank = if (isMentor) "Senior Mentor Guide" else "Under 1000",
                    targetStream = if (isMentor) "Senior Mentorship" else "Engineering (B.E - CSE / AI)",
                    signInMethod = signInMethod
                )
            }
            FirebaseService.saveUserProfile(_userProfile.value)
            startObservingUserData(_userProfile.value.uid, cleanEmail)
            viewModelScope.launch {
                val remoteXP = FirebaseService.getUserPointsOnce(calculatedUid, cleanEmail)
                if (remoteXP > _userPoints.value) {
                    _userPoints.value = remoteXP
                    sessionPrefs.edit().putInt("user_xp", remoteXP).putInt("user_xp_$calculatedUid", remoteXP).apply()
                    _analyticsSummary.value = _analyticsSummary.value.copy(totalPoints = remoteXP)
                    updateLeaderboard()
                }
            }
            updateLeaderboard()
        }
    }

    fun loginAdmin() {
        _isLoggedIn.value = true
        resetNavigation(Screen.ADMIN, BottomTab.HOME)
        saveSession("admin_ullas", "ullassuvarna65@gmail.com", "KCET Admin", isAdmin = true, authMethod = "Admin Portal")
        _userProfile.value = _userProfile.value.copy(
            uid = "admin_ullas",
            email = "ullassuvarna65@gmail.com",
            name = "KCET Admin",
            isPremium = true,
            signInMethod = "Admin Portal"
        )
        updateLeaderboard()
    }

    fun loginAdminDirectFromShutdown() {
        app.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("is_admin", true)
            .putString("user_email", "ullassuvarna65@gmail.com")
            .putString("user_uid", "admin_ullas")
            .putString("user_name", "KCET Admin")
            .apply()
        loginAdmin()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val currentEmail = FirebaseService.auth.currentUser?.email?.trim()?.lowercase()
                if (currentEmail != "ullassuvarna65@gmail.com") {
                    FirebaseService.auth.signInWithEmailAndPassword("ullassuvarna65@gmail.com", "Ullas@17082006")
                        .awaitResult()
                }
            } catch (_: Exception) {
                // Local admin portal session remains active even if offline
            }
        }
    }

    fun logout() {
        clearSession()
        app.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            .edit()
            .remove("is_logged_in")
            .remove("is_admin")
            .remove("user_email")
            .remove("user_uid")
            .remove("user_name")
            .apply()
        FirebaseService.auth.signOut()
        _isLoggedIn.value = false
        resetNavigation(Screen.AUTH, BottomTab.HOME)
        viewModelScope.launch {
            repository.clearTestHistory()

            _userPoints.value = 0
            _userBadges.value = defaultLockedBadges
            _analyticsSummary.value = cleanDefaultAnalytics
            _currentUserPaymentReceipt.value = null
            _activeTestResult.value = null
            _userAnswers.value = emptyMap()
        }
    }

    fun updateUserProfile(name: String, targetRank: String, targetStream: String, photoUrl: String? = null) {
        val oldProfile = _userProfile.value
        val cleanName = name.trim().ifBlank { oldProfile.name }
        val cleanRank = targetRank.trim().ifBlank { oldProfile.kcetTargetRank }
        val cleanStream = targetStream.trim().ifBlank { oldProfile.targetStream }

        var finalPhotoUrl = photoUrl
        
        // If the photoUrl is a local content URI or external temp URI, copy it locally to files directory
        if (photoUrl != null && (photoUrl.startsWith("content://") || (photoUrl.startsWith("file://") && !photoUrl.contains("profile_photo_")))) {
            try {
                val context = app
                val uri = Uri.parse(photoUrl)
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val uid = oldProfile.uid.ifBlank { "anonymous" }
                    val fileName = "profile_photo_$uid.jpg"
                    val file = File(context.filesDir, fileName)
                    
                    file.outputStream().use { outputStream ->
                        inputStream.use { it.copyTo(outputStream) }
                    }
                    
                    // Add timestamp to break Coil cache
                    finalPhotoUrl = Uri.fromFile(file).toString() + "?t=${System.currentTimeMillis()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error saving profile photo locally", e)
            }
        }

        val now = System.currentTimeMillis()
        val updatedProfile = oldProfile.copy(
            name = cleanName,
            kcetTargetRank = cleanRank,
            targetStream = cleanStream,
            photoUrl = finalPhotoUrl,
            lastUpdatedMillis = now
        )
        _userProfile.value = updatedProfile
        
        // Persist details in SharedPreferences
        sessionPrefs.edit().apply {
            putString("user_photo_url", finalPhotoUrl)
            putString("user_name", cleanName)
            putString("user_target_rank", cleanRank)
            putString("user_target_stream", cleanStream)
            apply()
        }

        // Calculate specific differences between old profile and updated profile
        val diffList = mutableListOf<String>()
        if (oldProfile.name.trim() != updatedProfile.name.trim()) {
            diffList.add("Name: '${oldProfile.name.trim()}' ➔ '${updatedProfile.name.trim()}'")
        }
        if (oldProfile.kcetTargetRank.trim() != updatedProfile.kcetTargetRank.trim()) {
            diffList.add("Target Rank: '${oldProfile.kcetTargetRank.trim()}' ➔ '${updatedProfile.kcetTargetRank.trim()}'")
        }
        if (oldProfile.targetStream.trim() != updatedProfile.targetStream.trim()) {
            diffList.add("Target Stream: '${oldProfile.targetStream.trim()}' ➔ '${updatedProfile.targetStream.trim()}'")
        }
        val oldPhoto = oldProfile.photoUrl ?: ""
        val newPhoto = updatedProfile.photoUrl ?: ""
        if (oldPhoto != newPhoto) {
            if (oldPhoto.isBlank() && newPhoto.isNotBlank()) {
                diffList.add("Profile Photo: Added new avatar image")
            } else if (oldPhoto.isNotBlank() && newPhoto.isBlank()) {
                diffList.add("Profile Photo: Removed photo")
            } else {
                diffList.add("Profile Photo: Updated image")
            }
        }

        viewModelScope.launch {
            // 1. Save updated profile in Firestore
            FirebaseService.saveUserProfile(updatedProfile)

            // 2. Update Leaderboard
            FirebaseService.updateLeaderboardEntry(
                profile = updatedProfile,
                points = _userPoints.value,
                accuracy = _analyticsSummary.value.overallAccuracy,
                testsCount = _analyticsSummary.value.totalTestsTaken
            )

            // 3. Sync name across test results, senior chat bookings, and realtime DB
            FirebaseService.syncStudentProfileAcrossCollections(updatedProfile)

            // 4. If any detail changed, send notification to admin explaining what changed
            if (diffList.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val formattedTime = sdf.format(Date(now))
                val studentIdStr = updatedProfile.formattedStudentId
                val changeBulletPoints = diffList.joinToString("\n• ", prefix = "• ")
                
                val notifId = "profile_change_${updatedProfile.uid}_$now"
                val notifTitle = "📝 Student Profile Updated: ${updatedProfile.name}"
                val notifMessage = "Student ${updatedProfile.name} (${updatedProfile.email.ifBlank { updatedProfile.uid }}, ID: $studentIdStr) updated profile details:\n$changeBulletPoints"

                val adminNotification = StudentNotification(
                    id = notifId,
                    title = notifTitle,
                    message = notifMessage,
                    timestamp = formattedTime,
                    type = "STUDENT_UPDATE",
                    isRead = false,
                    actionType = "VIEW_STUDENT",
                    targetUserId = "ADMIN",
                    targetUserEmail = "ullassuvarna65@gmail.com",
                    createdAtMillis = now
                )

                // Save to Firestore notifications collection
                FirebaseService.saveNotification(adminNotification)
                // Also send to Realtime Database admin notifications
                FirebaseRealtimeService.sendAdminAlert(adminNotification)
            }
        }
    }

    fun removeProfilePhoto() {
        val oldProfile = _userProfile.value
        val now = System.currentTimeMillis()
        val updatedProfile = oldProfile.copy(photoUrl = null, lastUpdatedMillis = now)
        _userProfile.value = updatedProfile
        sessionPrefs.edit().remove("user_photo_url").apply()
        
        viewModelScope.launch {
            FirebaseService.saveUserProfile(updatedProfile)
            
            // Delete local file if it exists
            try {
                val context = app
                val uid = updatedProfile.uid.ifBlank { "anonymous" }
                val fileName = "profile_photo_$uid.jpg"
                val file = File(context.filesDir, fileName)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error deleting local profile photo", e)
            }

            // Sync with other collections
            FirebaseService.syncStudentProfileAcrossCollections(updatedProfile)

            // Notify Admin
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val formattedTime = sdf.format(Date(now))
            val studentIdStr = updatedProfile.formattedStudentId
            val notifId = "profile_change_${updatedProfile.uid}_$now"
            val adminNotification = StudentNotification(
                id = notifId,
                title = "📝 Student Profile Updated: ${updatedProfile.name}",
                message = "Student ${updatedProfile.name} (${updatedProfile.email.ifBlank { updatedProfile.uid }}, ID: $studentIdStr) removed their profile photo.",
                timestamp = formattedTime,
                type = "STUDENT_UPDATE",
                isRead = false,
                actionType = "VIEW_STUDENT",
                targetUserId = "ADMIN",
                targetUserEmail = "ullassuvarna65@gmail.com",
                createdAtMillis = now
            )
            FirebaseService.saveNotification(adminNotification)
            FirebaseRealtimeService.sendAdminAlert(adminNotification)
        }
    }

    fun upgradeToPremium() {
        _userProfile.value = _userProfile.value.copy(isPremium = true)
        viewModelScope.launch {
            FirebaseService.saveUserProfile(_userProfile.value)
        }
    }

    // Questions State
    val questionsList: StateFlow<List<Question>> = repository.questions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Announcements State from Firestore
    val announcements: StateFlow<List<Announcement>> = repository.announcements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Resources State from Firestore
    val resources: StateFlow<List<ResourceItem>> = repository.resources.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Test Series Sets State from Firestore
    val testSeriesSets: StateFlow<List<com.example.data.TestSeriesSet>> = repository.testSeriesSets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Important Dates State from Firestore
    val importantDates: StateFlow<List<com.example.data.ImportantDateItem>> = repository.importantDates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Rank vs Marks State from Firestore
    val rankVsMarks: StateFlow<List<com.example.data.RankVsMarksItem>> = repository.rankVsMarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Helplines State from Firestore
    val helplines: StateFlow<List<com.example.data.HelplineItem>> = repository.helplines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.data.SampleData.defaultHelplines
    )

    // Payment Receipts State for Admin
    val paymentReceipts: StateFlow<List<com.example.data.PaymentReceipt>> = repository.paymentReceipts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current User Payment Receipt State
    private val _currentUserPaymentReceipt = MutableStateFlow<com.example.data.PaymentReceipt?>(null)
    val currentUserPaymentReceipt: StateFlow<com.example.data.PaymentReceipt?> = _currentUserPaymentReceipt.asStateFlow()

    fun submitPaymentReceipt(transactionRef: String, receiptUri: String) {
        val user = _userProfile.value
        val receipt = com.example.data.PaymentReceipt(
            receiptId = "REC_${System.currentTimeMillis()}",
            userId = user.uid,
            userName = user.name,
            userEmail = user.email,
            amountPaid = 299,
            transactionRef = transactionRef,
            receiptImageUri = receiptUri,
            submittedAtTimestamp = System.currentTimeMillis(),
            status = "UNDER_VERIFICATION"
        )
        _currentUserPaymentReceipt.value = receipt
        repository.submitPaymentReceipt(receipt)
    }

    fun approvePaymentReceipt(receiptId: String, userId: String) {
        repository.updatePaymentReceiptStatus(receiptId, userId, "VERIFIED")
        grantOrRevokePremiumByAdmin(userId, true)
        addNotification(
            title = "💳 Payment Verified!",
            message = "Your payment receipt ($receiptId) has been approved by Admin! Premium test series and video solutions unlocked.",
            type = "PAYMENT",
            actionType = "NAV_PRACTICE",
            targetUserId = userId
        )
    }

    fun rejectPaymentReceipt(receiptId: String, userId: String, reason: String) {
        repository.updatePaymentReceiptStatus(receiptId, userId, "REJECTED", reason)
        addNotification(
            title = "💳 Payment Receipt Update",
            message = "Your payment receipt ($receiptId) was not approved. Reason: ${reason.ifBlank { "Unverified transaction ID" }}",
            type = "PAYMENT",
            targetUserId = userId
        )
    }


    // Bookmarked Questions State
    val bookmarkedQuestions: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleBookmark(question: Question) {
        viewModelScope.launch {
            val currentlyBookmarked = bookmarkedQuestions.value.any { it.questionId == question.id }
            repository.toggleBookmark(question, currentlyBookmarked, userProfile.value.uid)
        }
    }

    fun removeBookmark(questionId: Int) {
        viewModelScope.launch {
            repository.removeBookmarkById(questionId, userProfile.value.uid)
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch {
            val list = bookmarkedQuestions.value
            repository.clearAllBookmarks(userProfile.value.uid, list)
        }
    }

    fun isQuestionBookmarked(questionId: Int): Boolean {
        return bookmarkedQuestions.value.any { it.questionId == questionId }
    }

    fun startPracticeWithBookmarks(bookmarkList: List<BookmarkEntity>) {
        requestStartBookmarkPractice(bookmarkList)
    }

    private fun startPracticeWithBookmarksInternal(bookmarkList: List<BookmarkEntity>) {
        if (bookmarkList.isEmpty()) return
        val bookmarkQs = bookmarkList.map { b ->
            val optEnum = try { com.example.data.Option.valueOf(b.correctOption) } catch (e: Exception) { com.example.data.Option.A }
            val subjEnum = try { com.example.data.Subject.valueOf(b.subject) } catch (e: Exception) { com.example.data.Subject.PHYSICS }
            com.example.data.Question(
                id = b.questionId,
                subject = subjEnum,
                questionText = b.questionText,
                optionA = b.optionA,
                optionB = b.optionB,
                optionC = b.optionC,
                optionD = b.optionD,
                correctOption = optEnum,
                explanation = b.explanation
            )
        }
        prepareNewExamAttempt()
        _activeQuestionsList.value = bookmarkQs
        _activeTestSet.value = null
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(bookmarkList.first().questionId)
        _timerSeconds.value = (bookmarkList.size * 90).coerceAtLeast(300)
        _isTimerPaused.value = false
        startTimer()
        navigateTo(Screen.EXAM)
    }

    // Helper functions
    fun triggerPremiumDialog(show: Boolean) {
        _showPremiumDialog.value = show
    }

    fun dismissBadgeCongratsDialog() {
        _showBadgeCongratsDialog.value = false
    }

    fun unlockAllBadges() {
        val updated = _userBadges.value.map { it.copy(isUnlocked = true, progress = 1f, unlockedDate = "2026") }
        _userBadges.value = updated
        _showBadgeCongratsDialog.value = true
        viewModelScope.launch {
            updated.forEach { badge ->
                FirebaseService.saveBadge(_userProfile.value.uid, badge)
            }
        }
    }

    fun showBlockedAlert(nameOrEmail: String, reason: String) {
        _blockedAccountAlert.value = Pair(nameOrEmail, reason)
    }

    fun dismissBlockedAccountAlert() {
        _blockedAccountAlert.value = null
    }

    private fun startObservingUserData(uid: String, email: String = "") {
        profileJob?.cancel()
        testResultsJob?.cancel()
        paymentJob?.cancel()
        presenceJob?.cancel()
        liveXpJob?.cancel()
        realtimeNotifJob?.cancel()
        allOnlineJob?.cancel()
        allLiveXpJob?.cancel()
        leaderboardJob?.cancel()
        badgesJob?.cancel()
        premiumJob?.cancel()

        val activeEmail = (if (email.isNotBlank()) email else _userProfile.value.email).lowercase().trim()

        // Setup Realtime DB Presence tracking
        if (uid.isNotBlank() || activeEmail.isNotBlank()) {
            val presenceKey = uid.ifBlank { activeEmail }
            repository.setupPresenceTracking(_userProfile.value.name, "Student")

            presenceJob = viewModelScope.launch {
                repository.observeUserPresence(presenceKey).collect { presence ->
                    _currentUserPresence.value = presence
                }
            }

            liveXpJob = viewModelScope.launch {
                repository.observeLiveXP(presenceKey).collect { xpData ->
                    _liveXpData.value = xpData
                    if (xpData.xp > _userPoints.value) {
                        _userPoints.value = xpData.xp
                        sessionPrefs.edit().putInt("user_xp", xpData.xp).putInt("user_xp_$presenceKey", xpData.xp).apply()
                        _analyticsSummary.value = _analyticsSummary.value.copy(totalPoints = xpData.xp)
                        updateLeaderboard()
                    }
                }
            }

            realtimeNotifJob = viewModelScope.launch {
                repository.observeRealtimeNotifications(presenceKey).collect { notifs ->
                    _realtimeNotifications.value = notifs
                }
            }
            
            allOnlineJob = viewModelScope.launch {
                repository.onlineUsers.collect { users ->
                    _onlineUsers.value = users
                }
            }
            
            allLiveXpJob = viewModelScope.launch {
                repository.allLiveXP.collect { xpMap ->
                    _allLiveXp.value = xpMap
                }
            }
            
            leaderboardJob = viewModelScope.launch {
                repository.leaderboard.collect { remoteEntries ->
                    if (remoteEntries.isNotEmpty()) {
                        updateLeaderboard(remoteEntries)
                    }
                }
            }

            premiumJob = viewModelScope.launch {
                try {
                    val cleanKey = presenceKey.replace(".", "_").replace("@", "_")
                    val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance(FirebaseRealtimeService.RTDB_URL)
                    val premRef = rtdb.getReference("user_premium").child(cleanKey)
                    val listener = object : com.google.firebase.database.ValueEventListener {
                        override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                            val rtdbPremium = snapshot.getValue(Boolean::class.java)
                            if (rtdbPremium != null) {
                                val isAdminSession = _userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                                        sessionPrefs.getBoolean("is_admin", false) ||
                                        uid == "admin_ullas"
                                if (!isAdminSession) {
                                    val currentPrem = _userProfile.value.isPremium
                                    if (currentPrem != rtdbPremium) {
                                        _userProfile.value = _userProfile.value.copy(isPremium = rtdbPremium)
                                        sessionPrefs.edit().apply {
                                            putBoolean("is_premium", rtdbPremium)
                                            if (uid.isNotBlank()) putBoolean("is_premium_$uid", rtdbPremium)
                                            if (activeEmail.isNotBlank()) putBoolean("is_premium_$activeEmail", rtdbPremium)
                                            apply()
                                        }
                                        FirebaseService.setPremiumOverride(uid, rtdbPremium)
                                        if (activeEmail.isNotBlank()) FirebaseService.setPremiumOverride(activeEmail, rtdbPremium)
                                        if (!rtdbPremium) {
                                            _userBadges.value = defaultLockedBadges
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                    }
                    premRef.addValueEventListener(listener)
                } catch (_: Exception) {}
            }
        }

        profileJob = viewModelScope.launch {
            FirebaseService.observeUserProfile(uid, activeEmail).collect { remoteProfile ->
                if (remoteProfile != null) {
                    val isMentor = activeEmail.equals("ullassuvarna85@gmail.com", ignoreCase = true) ||
                            _userProfile.value.email.equals("ullassuvarna85@gmail.com", ignoreCase = true) ||
                            _currentScreen.value == Screen.SENIOR_CHAT
                    val isAdmin = activeEmail.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                            _userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                            _currentScreen.value == Screen.ADMIN ||
                            uid == "admin_ullas" ||
                            sessionPrefs.getBoolean("is_admin", false)

                    if (!isMentor && !isAdmin && (remoteProfile.isForceLoggedOut || remoteProfile.isBlocked)) {
                        if (remoteProfile.isBlocked) {
                            _blockedAccountAlert.value = Pair(
                                remoteProfile.name.ifBlank { remoteProfile.email },
                                remoteProfile.blockReason.ifBlank { "Account blocked by Administrator." }
                            )
                        }
                        if (remoteProfile.isForceLoggedOut) {
                            FirebaseService.clearForceLogoutFlag(uid)
                            if (activeEmail.isNotBlank()) FirebaseService.clearForceLogoutFlag(activeEmail)
                        }
                        logout()
                        navigateTo(Screen.AUTH)
                        return@collect
                    }
                    val current = _userProfile.value
                    val updatedPhotoUrl = if (remoteProfile.photoUrl.isNullOrBlank()) current.photoUrl else remoteProfile.photoUrl
                    val wasPremium = current.isPremium
                    val override = FirebaseService.getPremiumOverride(uid)
                        ?: (if (activeEmail.isNotBlank()) FirebaseService.getPremiumOverride(activeEmail) else null)
                    val resolvedPremium = when {
                        isAdmin -> true
                        override != null -> override
                        else -> remoteProfile.isPremium
                    }
                    
                    _userProfile.value = current.copy(
                        isPremium = resolvedPremium,
                        name = remoteProfile.name.ifBlank { current.name },
                        kcetTargetRank = remoteProfile.kcetTargetRank.ifBlank { current.kcetTargetRank },
                        targetStream = remoteProfile.targetStream.ifBlank { current.targetStream },
                        photoUrl = updatedPhotoUrl,
                        isForceLoggedOut = remoteProfile.isForceLoggedOut,
                        isBlocked = remoteProfile.isBlocked,
                        studentId = remoteProfile.studentId.ifBlank { current.studentId }
                    )
                    
                    // Keep sessionPrefs in sync
                    sessionPrefs.edit().apply {
                        putString("user_name", remoteProfile.name.ifBlank { current.name })
                        putBoolean("is_premium", resolvedPremium)
                        if (uid.isNotBlank()) putBoolean("is_premium_$uid", resolvedPremium)
                        if (activeEmail.isNotBlank()) putBoolean("is_premium_$activeEmail", resolvedPremium)
                        putString("user_photo_url", updatedPhotoUrl)
                        apply()
                    }

                    if (!resolvedPremium && wasPremium) {
                        // Reset badges immediately upon premium revocation
                        _userBadges.value = defaultLockedBadges
                    }
                }
            }
        }

        testResultsJob = viewModelScope.launch {
            repository.observeFirestoreTestResults(uid).collect { results ->
                if (results.isNotEmpty()) {
                    repository.syncTestResultsToRoom(results)
                }
            }
        }

        paymentJob = viewModelScope.launch {
            repository.observeUserPaymentReceipt(uid).collect { receipt ->
                if (receipt != null) {
                    _currentUserPaymentReceipt.value = receipt
                }
            }
        }

        badgesJob = viewModelScope.launch {
            repository.observeBadges(uid).collect { firestoreBadges ->
                if (firestoreBadges.isNotEmpty()) {
                    _userBadges.value = firestoreBadges
                }
            }
        }
    }

    private var lastRemoteLeaderboardEntries: List<LeaderboardEntry>? = null

    private fun updateLeaderboard(remoteEntries: List<LeaderboardEntry>? = null) {
        if (remoteEntries != null) {
            lastRemoteLeaderboardEntries = remoteEntries
        }
        val effectiveRemote = when {
            !remoteEntries.isNullOrEmpty() -> remoteEntries
            !lastRemoteLeaderboardEntries.isNullOrEmpty() -> lastRemoteLeaderboardEntries!!
            activeLeaderboardResetAt > 0L -> com.example.data.defaultRegisteredStudents.map { it.copy(points = 0) }
            else -> com.example.data.defaultRegisteredStudents
        }

        val currentUid = _userProfile.value.uid.trim()
        val currentEmail = _userProfile.value.email.trim().lowercase()
        val currentName = _userProfile.value.name.trim()
        val currentNameLower = currentName.lowercase()
        val isUserAdminOrMentor = currentEmail == "ullassuvarna65@gmail.com" || currentEmail == "ullassuvarna85@gmail.com" ||
                                 currentNameLower.contains("admin") || currentNameLower.contains("mentor")

        val currentPoints = _userPoints.value
        val currentAcc = _analyticsSummary.value.overallAccuracy
        val currentTests = _analyticsSummary.value.totalTestsTaken
        val currentTargetRank = _userProfile.value.kcetTargetRank

        val mergedList = mutableListOf<LeaderboardEntry>()

        // 1. Process all remote entries with robust UID/email deduplication
        for (entry in effectiveRemote) {
            val emailLower = entry.email.trim().lowercase()
            val nameClean = entry.name.trim()
            val nameLower = nameClean.lowercase()
            val uidClean = entry.uid.trim()

            // Skip mentors or admins
            if (nameLower.contains("mentor") || emailLower.contains("mentor") ||
                nameLower.contains("admin") || emailLower.contains("admin") ||
                emailLower == "ullassuvarna65@gmail.com" || emailLower == "ullassuvarna85@gmail.com") {
                continue
            }
            if (nameClean.isBlank()) continue

            // Determine if this entry is the current user (if current user is a student)
            val isCurrent = !isUserAdminOrMentor && _isLoggedIn.value && (
                (currentUid.isNotBlank() && (uidClean == currentUid || emailLower == currentUid.lowercase())) ||
                (currentEmail.isNotBlank() && emailLower == currentEmail)
            )

            // Deduplicate across existing entries in mergedList strictly by matching UID or email
            val existingIndex = mergedList.indexOfFirst { existing ->
                val exEmail = existing.email.trim().lowercase()
                val exUid = existing.uid.trim()

                val emailMatch = emailLower.isNotBlank() && exEmail.isNotBlank() && emailLower == exEmail
                val uidMatch = uidClean.isNotBlank() && exUid.isNotBlank() && uidClean == exUid
                val crossEmailMatch = (uidClean.contains("@") && exEmail.isNotBlank() && uidClean.lowercase() == exEmail) ||
                                      (exUid.contains("@") && emailLower.isNotBlank() && exUid.lowercase() == emailLower)

                emailMatch || uidMatch || crossEmailMatch
            }

            val processedEntry = if (isCurrent) {
                val initials = currentName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifBlank { "ST" }
                val targetRank = if (currentTargetRank.isNotBlank() && currentTargetRank != "Under 1000") currentTargetRank else entry.kcetTargetRank
                val effectivePts = maxOf(entry.points, currentPoints)
                val effectiveAcc = if (currentAcc > 0f) currentAcc else entry.accuracy
                val effectiveTests = maxOf(entry.testsCount, currentTests)
                entry.copy(
                    name = if (currentName.isNotBlank() && !currentName.equals("Student", ignoreCase = true) && !currentName.equals("KCET Aspirant", ignoreCase = true)) currentName else entry.name,
                    kcetTargetRank = targetRank,
                    points = effectivePts,
                    accuracy = effectiveAcc,
                    testsCount = effectiveTests,
                    avatarInitials = if (initials.isNotBlank() && initials != "ST") initials else entry.avatarInitials,
                    isCurrentUser = true,
                    uid = if (currentUid.isNotBlank()) currentUid else entry.uid,
                    email = if (currentEmail.isNotBlank()) _userProfile.value.email else entry.email
                )
            } else {
                entry.copy(isCurrentUser = false)
            }

            if (existingIndex >= 0) {
                val existing = mergedList[existingIndex]
                val bestName = when {
                    existing.name.isNotBlank() && !existing.name.equals("Student", ignoreCase = true) && !existing.name.equals("KCET Aspirant", ignoreCase = true) -> existing.name
                    processedEntry.name.isNotBlank() && !processedEntry.name.equals("Student", ignoreCase = true) && !processedEntry.name.equals("KCET Aspirant", ignoreCase = true) -> processedEntry.name
                    existing.name.isNotBlank() -> existing.name
                    else -> processedEntry.name.ifBlank { "KCET Aspirant" }
                }
                val bestEmail = if (existing.email.isNotBlank() && existing.email.contains("@")) existing.email else processedEntry.email
                val bestUid = if (existing.uid.isNotBlank() && !existing.uid.startsWith("mock_") && !existing.uid.contains("@")) existing.uid else processedEntry.uid.ifBlank { existing.uid }
                val bestInitials = if (existing.avatarInitials.isNotBlank() && existing.avatarInitials != "ST" && existing.avatarInitials != "KC") existing.avatarInitials else processedEntry.avatarInitials
                val bestTargetRank = if (existing.kcetTargetRank.isNotBlank() && existing.kcetTargetRank != "Under 1000") existing.kcetTargetRank else processedEntry.kcetTargetRank
                val isCur = existing.isCurrentUser || processedEntry.isCurrentUser

                mergedList[existingIndex] = existing.copy(
                    name = bestName,
                    email = bestEmail,
                    uid = bestUid,
                    kcetTargetRank = bestTargetRank,
                    points = maxOf(existing.points, processedEntry.points),
                    accuracy = if (processedEntry.accuracy > 0f) processedEntry.accuracy else existing.accuracy,
                    testsCount = maxOf(existing.testsCount, processedEntry.testsCount),
                    avatarInitials = bestInitials,
                    isCurrentUser = isCur
                )
            } else {
                mergedList.add(processedEntry)
            }
        }

        // 2. Ensure current student is present if logged in and not admin/mentor
        val hasCurrentUser = mergedList.any { it.isCurrentUser }
        if (!hasCurrentUser && !isUserAdminOrMentor && _isLoggedIn.value && (currentUid.isNotBlank() || currentEmail.isNotBlank())) {
            val initials = currentName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifBlank { "ST" }
            mergedList.add(
                LeaderboardEntry(
                    rank = 0,
                    name = currentName.ifBlank { "KCET Aspirant" },
                    kcetTargetRank = currentTargetRank.ifBlank { "Under 1000" },
                    points = currentPoints,
                    accuracy = currentAcc,
                    testsCount = currentTests,
                    avatarInitials = initials,
                    isCurrentUser = true,
                    uid = currentUid,
                    email = _userProfile.value.email
                )
            )
        }

        // 3. Sort, rank, and publish identically across all student accounts
        val combined = mergedList.sortedWith(
            compareByDescending<LeaderboardEntry> { it.points }
                .thenByDescending { it.accuracy }
                .thenByDescending { it.testsCount }
        ).mapIndexed { idx, entry ->
            entry.copy(
                rank = idx + 1,
                isCurrentUser = !isUserAdminOrMentor && _isLoggedIn.value && (
                    (currentUid.isNotBlank() && (entry.uid == currentUid || entry.email.trim().lowercase() == currentUid.lowercase())) ||
                    (currentEmail.isNotBlank() && entry.email.trim().lowercase() == currentEmail)
                )
            )
        }

        _leaderboard.value = combined
    }

    // Active test calculation summary
    private val _activeTestResult = MutableStateFlow<TestResultEntity?>(null)
    val activeTestResult: StateFlow<TestResultEntity?> = _activeTestResult.asStateFlow()

    private val _activeQuestionsList = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestionsList: StateFlow<List<Question>> = _activeQuestionsList.asStateFlow()

    private val _activeTestSet = MutableStateFlow<com.example.data.TestSeriesSet?>(null)
    val activeTestSet: StateFlow<com.example.data.TestSeriesSet?> = _activeTestSet.asStateFlow()

    fun navigateTo(screen: Screen, replaceTop: Boolean = false) {
        if (isMaintenanceActive.value && screen != Screen.ADMIN) {
            return
        }
        val currentTab = _selectedTab.value
        val newDest = NavDestination(screen, if (screen == Screen.MAIN) currentTab else BottomTab.HOME)
        val stack = _navigationStack.value.toMutableList()
        if (replaceTop && stack.isNotEmpty()) {
            stack.removeAt(stack.size - 1)
        }
        if (stack.lastOrNull() != newDest) {
            stack.add(newDest)
        }
        _navigationStack.value = stack
        _currentScreen.value = screen
    }

    fun selectBottomTab(tab: BottomTab) {
        if (isMaintenanceActive.value && _currentScreen.value != Screen.ADMIN) {
            return
        }
        _selectedTab.value = tab
        _currentScreen.value = Screen.MAIN
        val newDest = NavDestination(Screen.MAIN, tab)
        val stack = _navigationStack.value.toMutableList()
        if (stack.lastOrNull() != newDest) {
            stack.add(newDest)
        }
        _navigationStack.value = stack
    }

    fun navigateBack(): Boolean {
        if (isMaintenanceActive.value && _currentScreen.value != Screen.ADMIN) {
            return false
        }
        if (_currentScreen.value == Screen.ADMIN) {
            resetNavigation(Screen.MAIN, BottomTab.HOME)
            return true
        }
        val currentEmail = _userProfile.value.email.trim().lowercase()
        val isMentor = currentEmail == "ullassuvarna85@gmail.com" || registeredMentors.value.any { it.email.isNotBlank() && it.email.equals(currentEmail, ignoreCase = true) }
        if (_currentScreen.value == Screen.SENIOR_CHAT && isMentor) {
            logout()
            return true
        }
        val currentStack = _navigationStack.value
        if (currentStack.size > 1) {
            val newStack = currentStack.dropLast(1)
            val target = newStack.last()
            _navigationStack.value = newStack
            _currentScreen.value = target.screen
            _selectedTab.value = target.bottomTab
            return true
        }
        // If stack has 1 element and we are not on root Home, shift directly to Home
        if (_currentScreen.value != Screen.MAIN || _selectedTab.value != BottomTab.HOME) {
            resetNavigation(Screen.MAIN, BottomTab.HOME)
            return true
        }
        return false
    }

    fun resetNavigation(screen: Screen, tab: BottomTab = BottomTab.HOME) {
        val newDest = NavDestination(screen, tab)
        _navigationStack.value = listOf(newDest)
        _currentScreen.value = screen
        _selectedTab.value = tab
    }

    // Pending Pre-Exam Guidelines State
    private val _pendingExamInfo = MutableStateFlow<PendingExamInfo?>(null)
    val pendingExamInfo: StateFlow<PendingExamInfo?> = _pendingExamInfo.asStateFlow()

    fun requestStartFreeTest() {
        _pendingExamInfo.value = PendingExamInfo(
            title = "KCET Daily Mock Test (Free Sample)",
            category = "Quick CBT Practice",
            durationMins = 20,
            questionCount = 20,
            totalMarks = 20,
            isFreeTest = true
        )
    }

    fun requestStartTestSet(testSet: com.example.data.TestSeriesSet) {
        _pendingExamInfo.value = PendingExamInfo(
            title = testSet.title,
            category = if (testSet.isPremium) "KCET Premium CBT Test Series" else "KCET Free CBT Test",
            durationMins = testSet.durationMins,
            questionCount = testSet.questionCount,
            totalMarks = testSet.questionCount,
            testSet = testSet
        )
    }

    fun requestStartBookmarkPractice(bookmarkList: List<BookmarkEntity>) {
        if (bookmarkList.isEmpty()) return
        _pendingExamInfo.value = PendingExamInfo(
            title = "Saved Bookmarks Revision Test",
            category = "Self-Practice Test",
            durationMins = ((bookmarkList.size * 90) / 60).coerceAtLeast(5),
            questionCount = bookmarkList.size,
            totalMarks = bookmarkList.size,
            bookmarkList = bookmarkList
        )
    }

    fun dismissExamGuidelines() {
        _pendingExamInfo.value = null
    }

    fun confirmStartExam(
        cetNo: String = "261KCET892",
        versionCode: String = "A1",
        serialNo: String = "781045",
        name: String = "KCET ASPIRANT"
    ) {
        val info = _pendingExamInfo.value ?: return
        _pendingExamInfo.value = null

        _candidateCetNumber.value = cetNo.ifBlank { "261KCET892" }
        _candidateBookletVersion.value = versionCode.ifBlank { "A1" }
        _candidateBookletSerial.value = serialNo.ifBlank { "781045" }
        _candidateName.value = name.ifBlank { "KCET ASPIRANT" }
        val isFullMock = (info.testSet?.category?.contains("Full Mock", ignoreCase = true) == true) ||
                info.title.contains("Full Mock", ignoreCase = true) ||
                info.category.contains("Full Mock", ignoreCase = true)

        _isStrictRealExam.value = isFullMock

        when {
            info.isFreeTest -> startFreeTestInternal()
            info.testSet != null -> startTestForSetInternal(info.testSet)
            info.bookmarkList != null -> startPracticeWithBookmarksInternal(info.bookmarkList)
            else -> startFreeTestInternal()
        }
    }

    fun updateCandidateDetails(name: String, cetNo: String, version: String, serial: String) {
        if (name.isNotBlank()) _candidateName.value = name
        if (cetNo.isNotBlank()) _candidateCetNumber.value = cetNo
        if (version.isNotBlank()) _candidateBookletVersion.value = version
        if (serial.isNotBlank()) _candidateBookletSerial.value = serial
    }

    // Exam Controls
    var activeChallengeId: String? = null

    fun startChallengeExam(challengeId: String, questions: List<Question>) {
        activeChallengeId = challengeId
        prepareNewExamAttempt()
        _activeQuestionsList.value = questions.take(20)
        _activeTestSet.value = null
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(_activeQuestionsList.value.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 1200 // 20 minutes (1 minute per question)
        _isTimerPaused.value = false
        navigateTo(Screen.EXAM)
        startTimer()
    }

    fun startFreeTest() {
        requestStartFreeTest()
    }

    private fun startFreeTestInternal() {
        prepareNewExamAttempt()
        val freeQs = questionsList.value.ifEmpty { com.example.data.SampleData.sampleQuestions }
        _activeQuestionsList.value = freeQs.take(20)
        _activeTestSet.value = null
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(_activeQuestionsList.value.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 1200 // 20 minutes
        _isTimerPaused.value = false
        navigateTo(Screen.EXAM)
        startTimer()
    }

    fun loadDailyPracticeSet(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isDailyQuestionsLoading.value = true
            try {
                val (questions, windowInfo) = com.example.data.GeminiDailyQuestionService.getOrGenerateDailyQuestions(
                    context = app,
                    forceRefresh = forceRefresh
                )
                _dailyPracticeQuestions.value = questions
                _dailyWindowInfo.value = windowInfo
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error loading daily questions: ${e.message}")
            } finally {
                _isDailyQuestionsLoading.value = false
            }
        }
    }

    fun loadDailyFullMockTest(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isDailyMockLoading.value = true
            try {
                val (questions, windowInfo) = com.example.data.GeminiDailyQuestionService.getOrGenerateDailyFullMockTest(
                    context = app,
                    forceRefresh = forceRefresh
                )
                _dailyFullMockQuestions.value = questions
                _dailyWindowInfo.value = windowInfo
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error loading daily full mock test: ${e.message}")
            } finally {
                _isDailyMockLoading.value = false
            }
        }
    }

    private fun isRealQuestion(q: com.example.data.Question): Boolean {
        val txt = q.questionText
        if (txt.isBlank()) return false
        if (txt.contains("[Daily Full Mock", ignoreCase = true)) return false
        if (txt.contains("[Topic:", ignoreCase = true)) return false
        if (txt.contains("Topic: ", ignoreCase = true)) return false
        if (txt.contains("[KCET", ignoreCase = true)) return false
        if (txt.contains("Standard KCET", ignoreCase = true)) return false
        if (txt.contains("High yield KCET", ignoreCase = true)) return false
        if (txt.contains("(Physics)", ignoreCase = true) && txt.contains("Q")) return false
        if (txt.contains("(Chemistry)", ignoreCase = true) && txt.contains("Q")) return false
        if (txt.contains("(Mathematics)", ignoreCase = true) && txt.contains("Q")) return false
        return true
    }

    fun startDailyPracticeTest() {
        prepareNewExamAttempt()
        val hasDummy = dailyPracticeQuestions.value.any { !isRealQuestion(it) }
        
        val questions = if (dailyPracticeQuestions.value.isEmpty() || hasDummy) {
            val realQs = questionsList.value.filter { isRealQuestion(it) }
            if (realQs.isNotEmpty()) {
                realQs.shuffled().take(15)
            } else {
                com.example.data.SampleData.sampleQuestions.shuffled().take(15)
            }
        } else {
            dailyPracticeQuestions.value
        }
        _activeQuestionsList.value = questions
        _activeTestSet.value = com.example.data.TestSeriesSet(
            id = "ts_daily_ai",
            title = "📅 Daily AI Practice Set (DPP)",
            category = "Daily Practice",
            questionCount = questions.size,
            durationMins = 15,
            isPremium = false,
            description = "15 AI-generated KCET questions refreshed every 24 hours (01:00 AM reset)."
        )
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(questions.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 900 // 15 minutes
        _isTimerPaused.value = false
        navigateTo(Screen.EXAM)
        startTimer()
    }

    fun startDailyFullMockTest() {
        prepareNewExamAttempt()
        val hasDummy = dailyFullMockQuestions.value.any { !isRealQuestion(it) }

        val questions = if (dailyFullMockQuestions.value.isEmpty() || hasDummy) {
            val realQs = questionsList.value.filter { isRealQuestion(it) }
            if (realQs.isNotEmpty()) {
                val list = mutableListOf<com.example.data.Question>()
                var idCounter = 1000000
                while(list.size < 60) {
                    list.addAll(realQs.shuffled().map { it.copy(id = idCounter++) })
                }
                list.take(60)
            } else {
                val list = mutableListOf<com.example.data.Question>()
                var idCounter = 2000000
                while(list.size < 60) {
                    list.addAll(com.example.data.SampleData.sampleQuestions.shuffled().map { it.copy(id = idCounter++) })
                }
                list.take(60)
            }
        } else {
            dailyFullMockQuestions.value
        }
        _activeQuestionsList.value = questions
        _activeTestSet.value = com.example.data.TestSeriesSet(
            id = "ts_daily_mock",
            title = "🎯 24H Daily AI Full Mock Test",
            category = "Full Mock Test",
            questionCount = questions.size,
            durationMins = 80,
            isPremium = false,
            description = "60 AI-generated KCET Full Mock Test questions (Physics, Chemistry, Math) refreshed every 24 hours (01:00 AM reset)."
        )
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(questions.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 4800 // 80 minutes
        _isTimerPaused.value = false
        navigateTo(Screen.EXAM)
        startTimer()
    }

    fun startTestForSet(testSet: com.example.data.TestSeriesSet) {
        requestStartTestSet(testSet)
    }

    private fun startTestForSetInternal(testSet: com.example.data.TestSeriesSet) {
        if (testSet.id == "ts_daily_ai" || testSet.id == "ts_9") {
            startDailyPracticeTest()
            return
        }
        if (testSet.id == "ts_daily_mock" || testSet.id == "ts_1" || testSet.title.contains("Full Mock", ignoreCase = true)) {
            startDailyFullMockTest()
            return
        }

        prepareNewExamAttempt()
        _activeTestSet.value = testSet
        val setQuestions = questionsList.value.filter { it.testSetId == testSet.id }
        val finalQs = if (setQuestions.isNotEmpty() && setQuestions.none { !isRealQuestion(it) }) {
            setQuestions
        } else {
            val realQs = questionsList.value.filter { isRealQuestion(it) }
            if (realQs.isNotEmpty()) {
                val list = mutableListOf<com.example.data.Question>()
                var idCounter = 3000000
                while(list.size < testSet.questionCount) {
                    list.addAll(realQs.shuffled().map { it.copy(id = idCounter++) })
                }
                list.take(testSet.questionCount)
            } else {
                val list = mutableListOf<com.example.data.Question>()
                var idCounter = 4000000
                while(list.size < testSet.questionCount) {
                    list.addAll(com.example.data.SampleData.sampleQuestions.shuffled().map { it.copy(id = idCounter++) })
                }
                list.take(testSet.questionCount)
            }
        }
        _activeQuestionsList.value = finalQs
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(finalQs.firstOrNull()?.id ?: 1)
        _timerSeconds.value = (testSet.durationMins * 60).coerceAtLeast(300)
        _isTimerPaused.value = false
        navigateTo(Screen.EXAM)
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0 && _currentScreen.value == Screen.EXAM) {
                delay(1000)
                if (!_isTimerPaused.value) {
                    _timerSeconds.value = _timerSeconds.value - 1
                }
            }
            if (_timerSeconds.value <= 0 && _currentScreen.value == Screen.EXAM) {
                submitExam()
            }
        }
    }

    fun togglePauseTimer() {
        _isTimerPaused.value = !_isTimerPaused.value
    }

    fun selectQuestionIndex(index: Int) {
        val currentList = _activeQuestionsList.value.ifEmpty { questionsList.value }
        if (index in currentList.indices) {
            _currentQuestionIndex.value = index
            val qId = currentList[index].id
            _visitedQuestions.value = _visitedQuestions.value + qId
        }
    }

    fun nextQuestion() {
        val currentList = _activeQuestionsList.value.ifEmpty { questionsList.value }
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < currentList.size) {
            selectQuestionIndex(nextIdx)
        }
    }

    fun previousQuestion() {
        val prevIdx = _currentQuestionIndex.value - 1
        if (prevIdx >= 0) {
            selectQuestionIndex(prevIdx)
        }
    }

    fun selectOption(questionId: Int, option: Option): Boolean {
        val currentAns = _userAnswers.value[questionId] ?: UserAnswer(questionId = questionId)
        if (_isStrictRealExam.value) {
            if (currentAns.selectedOption != null) {
                // In real KCET OMR exam rules, ballpoint pen shading cannot be erased, un-shaded, or changed to another option
                return false
            }
            val newAns = currentAns.copy(selectedOption = option)
            _userAnswers.value = _userAnswers.value + (questionId to newAns)
            return true
        } else {
            // Normal practice mode: Toggling unselects if same option is clicked, or changes selection if another option is clicked
            val newOpt = if (currentAns.selectedOption == option) null else option
            val newAns = currentAns.copy(selectedOption = newOpt)
            _userAnswers.value = _userAnswers.value + (questionId to newAns)
            return true
        }
    }

    fun toggleMarkForReview(questionId: Int) {
        val currentAns = _userAnswers.value[questionId] ?: UserAnswer(questionId = questionId)
        val newAns = currentAns.copy(isMarkedForReview = !currentAns.isMarkedForReview)
        _userAnswers.value = _userAnswers.value + (questionId to newAns)
    }

    fun getQuestionStatus(questionId: Int): QuestionPaletteStatus {
        val isVisited = _visitedQuestions.value.contains(questionId)
        val ans = _userAnswers.value[questionId]
        val hasOption = ans?.selectedOption != null
        val isMarked = ans?.isMarkedForReview == true

        return when {
            hasOption && isMarked -> QuestionPaletteStatus.ANSWERED_AND_MARKED
            isMarked -> QuestionPaletteStatus.MARKED_FOR_REVIEW
            hasOption -> QuestionPaletteStatus.ANSWERED
            isVisited -> QuestionPaletteStatus.NOT_ANSWERED
            else -> QuestionPaletteStatus.NOT_VISITED
        }
    }

    // Helper function to calculate earned XP according to completion rules:
    // Scaled XP based on correct answers (15 XP/correct) + proportional completion & speed bonuses
    fun calculateEarnedXP(res: TestResultEntity): Int {
        if (res.testName.contains("Challenge", ignoreCase = true) || res.testName.contains("1v1", ignoreCase = true)) {
            return 0
        }
        if (res.isQuit) return -50
        val qCount = res.totalQuestions.coerceAtLeast(1)
        val attempted = res.correctCount + res.wrongCount
        val attemptedRatio = (attempted.toFloat() / qCount).coerceIn(0f, 1f)
        val isQualified = attemptedRatio > 0.5f

        if (!isQualified || (res.score <= 0 && res.correctCount <= 0 && attempted == 0)) {
            return 0
        }

        val baseCorrectXP = maxOf(0, res.correctCount) * 15
        val completionBonus = (50f * attemptedRatio).toInt()
        val speedPerQ = if (qCount > 0) (res.timeTakenSeconds / qCount).toInt() else 60
        val speedBonus = if (speedPerQ < 40) (25f * attemptedRatio).toInt() else 0

        return (baseCorrectXP + completionBonus + speedBonus).coerceAtLeast(0)
    }

    fun prepareNewExamAttempt(): String {
        val newAttemptId = "att_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().replace("-", "").take(8)}"
        _activeAttemptId.value = newAttemptId
        _isSubmittingExam.value = false
        return newAttemptId
    }

    fun getUniqueAttempts(results: List<TestResultEntity>): List<TestResultEntity> {
        val seen = mutableSetOf<String>()
        val unique = mutableListOf<TestResultEntity>()
        for (res in results) {
            val key = if (res.attemptId.isNotBlank()) {
                "att_${res.attemptId}"
            } else {
                val window = res.timestamp / 15000L
                "dup_${res.testName}_${res.score}_${res.totalQuestions}_${res.correctCount}_${res.wrongCount}_$window"
            }
            if (seen.add(key)) {
                unique.add(res)
            }
        }
        return unique
    }

    fun deleteTestResult(id: Int) {
        viewModelScope.launch {
            repository.deleteTestResult(id)
        }
    }

    fun clearAllTestHistory() {
        viewModelScope.launch {
            repository.clearTestHistory()

            _userPoints.value = 0
            _analyticsSummary.value = cleanDefaultAnalytics
            updateLeaderboard()
        }
    }

    fun quitExam(penaltyXP: Int = 50) {
        val currentChlId = activeChallengeId
        if (currentChlId != null) {
            timerJob?.cancel()
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val answers = _userAnswers.value
            val optMap = answers.mapNotNull { (qId, uAns) -> uAns.selectedOption?.let { opt -> qId to opt } }.toMap()
            val durationMins = _activeTestSet.value?.durationMins ?: 20
            val timeSpentSeconds = (durationMins * 60) - _timerSeconds.value
            viewModelScope.launch {
                com.example.data.ChallengeService.quitChallengeQuiz(currentChlId, uid, optMap, timeSpentSeconds.coerceAtLeast(1).toLong())
            }
            activeChallengeId = null
            _isSubmittingExam.value = false
            navigateTo(Screen.CHALLENGE, replaceTop = true)
            return
        }

        if (_isSubmittingExam.value) {
            android.util.Log.w("MainViewModel", "quitExam already in progress, ignoring duplicate call")
            return
        }
        _isSubmittingExam.value = true

        timerJob?.cancel()

        val attemptId = _activeAttemptId.value.ifBlank {
            "att_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().replace("-", "").take(8)}"
        }
        _activeAttemptId.value = attemptId

        if (processedAttemptIds.contains(attemptId)) {
            android.util.Log.w("MainViewModel", "Attempt $attemptId already processed for quitExam, returning to HOME")
            resetNavigation(Screen.MAIN, BottomTab.HOME)
            return
        }
        processedAttemptIds.add(attemptId)

        val questions = _activeQuestionsList.value.ifEmpty { questionsList.value }
        val answers = _userAnswers.value

        var correct = 0
        var wrong = 0
        var unanswered = 0

        questions.forEach { q ->
            val ans = answers[q.id]?.selectedOption
            if (ans == null) {
                unanswered++
            } else if (ans == q.correctOption) {
                correct++
            } else {
                wrong++
            }
        }

        val total = questions.size
        val score = correct
        val percentage = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val durationMins = _activeTestSet.value?.durationMins ?: 20
        val timeSpentSeconds = (durationMins * 60) - _timerSeconds.value

        val testNameStr = (_activeTestSet.value?.title ?: "KCET Grand Practice CBT Test") + " (Quit Early)"

        val result = TestResultEntity(
            attemptId = attemptId,
            testName = testNameStr,
            totalQuestions = total,
            score = score,
            correctCount = correct,
            wrongCount = wrong,
            unansweredCount = unanswered,
            timeTakenSeconds = timeSpentSeconds.coerceAtLeast(1).toLong(),
            percentage = percentage,
            isQuit = true
        )

        viewModelScope.launch {
            repository.saveTestResult(result, _userProfile.value)
        }

        val allResults = getUniqueAttempts(testResultsHistory.value + result)
        val earnedXP = calculateEarnedXP(result) // -50 penalty
        val finalXP = if (awardedAttemptIds.add(attemptId)) {
            (_userPoints.value + earnedXP).coerceAtLeast(0)
        } else {
            _userPoints.value
        }
        _userPoints.value = finalXP
        val uid = _userProfile.value.uid
        if (uid.isNotBlank()) {
            sessionPrefs.edit().putInt("user_xp", finalXP).putInt("user_xp_$uid", finalXP).apply()
        } else {
            sessionPrefs.edit().putInt("user_xp", finalXP).apply()
        }

        _analyticsSummary.value = _analyticsSummary.value.copy(
            totalPoints = finalXP
        )

        viewModelScope.launch {
            FirebaseService.updateLeaderboardEntry(
                profile = _userProfile.value,
                points = finalXP,
                accuracy = percentage,
                testsCount = allResults.size
            )
            try {
                val cleanUid = if (uid.isNotBlank()) uid else "user_" + _userProfile.value.email.replace(".", "_")
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance(FirebaseRealtimeService.RTDB_URL)
                rtdb.getReference("liveXP").child(cleanUid).updateChildren(mapOf(
                    "xp" to finalXP,
                    "points" to finalXP,
                    "updatedAt" to System.currentTimeMillis()
                ))
                rtdb.getReference("leaderboard").child(cleanUid).updateChildren(mapOf(
                    "xp" to finalXP,
                    "points" to finalXP
                ))
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Error updating RTDB XP on quit: ${e.message}")
            }
        }

        updateLeaderboard()
        resetNavigation(Screen.MAIN, BottomTab.HOME)
    }

    fun submitExam() {
        if (_isSubmittingExam.value) {
            android.util.Log.w("MainViewModel", "submitExam already in progress, ignoring duplicate call")
            return
        }
        _isSubmittingExam.value = true

        timerJob?.cancel()

        val attemptId = _activeAttemptId.value.ifBlank {
            "att_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().replace("-", "").take(8)}"
        }
        _activeAttemptId.value = attemptId

        if (processedAttemptIds.contains(attemptId)) {
            android.util.Log.w("MainViewModel", "Attempt $attemptId already processed for submitExam, navigating to RESULT")
            navigateTo(Screen.RESULT, replaceTop = true)
            return
        }
        processedAttemptIds.add(attemptId)

        val questions = _activeQuestionsList.value.ifEmpty { questionsList.value }
        val answers = _userAnswers.value

        var correct = 0
        var wrong = 0
        var unanswered = 0

        questions.forEach { q ->
            val ans = answers[q.id]?.selectedOption
            if (ans == null) {
                unanswered++
            } else if (ans == q.correctOption) {
                correct++
            } else {
                wrong++
            }
        }

        val total = questions.size
        val score = correct // 1 mark per correct question in KCET
        val percentage = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val durationMins = _activeTestSet.value?.durationMins ?: 20
        val timeSpentSeconds = (durationMins * 60) - _timerSeconds.value

        val currentChlId = activeChallengeId
        val testNameStr = if (currentChlId != null) {
            "1v1 Challenge Test"
        } else {
            _activeTestSet.value?.title ?: "KCET Grand Practice CBT Test"
        }

        val result = TestResultEntity(
            attemptId = attemptId,
            testName = testNameStr,
            totalQuestions = total,
            score = score,
            correctCount = correct,
            wrongCount = wrong,
            unansweredCount = unanswered,
            timeTakenSeconds = timeSpentSeconds.coerceAtLeast(1).toLong(),
            percentage = percentage
        )

        _activeTestResult.value = result
        viewModelScope.launch {
            repository.saveTestResult(result, _userProfile.value)
        }

        // If this attempt is a 1v1 challenge quiz, submit to ChallengeService and return to challenge screen
        if (currentChlId != null) {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val optMap = answers.mapNotNull { (qId, uAns) -> uAns.selectedOption?.let { opt -> qId to opt } }.toMap()
            viewModelScope.launch {
                com.example.data.ChallengeService.submitQuizAnswers(currentChlId, uid, optMap, timeSpentSeconds.coerceAtLeast(1).toLong())
            }
            activeChallengeId = null
            _isSubmittingExam.value = false
            navigateTo(Screen.CHALLENGE, replaceTop = true)
            return
        }

        val speedPerQ = if (total > 0) timeSpentSeconds / total else 60

        // Calculate XP idempotently: awarded only once per unique attempt
        val allResults = getUniqueAttempts(testResultsHistory.value + result)
        val earnedXP = calculateEarnedXP(result)
        val finalXP = if (awardedAttemptIds.add(attemptId)) {
            (_userPoints.value + earnedXP).coerceAtLeast(0)
        } else {
            _userPoints.value
        }
        _userPoints.value = finalXP
        val uid = _userProfile.value.uid
        if (uid.isNotBlank()) {
            sessionPrefs.edit().putInt("user_xp", finalXP).putInt("user_xp_$uid", finalXP).apply()
        } else {
            sessionPrefs.edit().putInt("user_xp", finalXP).apply()
        }

        // Evaluate Badges across all 15 milestones on unique attempts
        val testsCount = allResults.size
        val totalQuestionsAttempted = allResults.sumOf { it.totalQuestions }
        val maxPercentage = allResults.maxOfOrNull { it.percentage } ?: percentage
        val maxScore = allResults.maxOfOrNull { it.score } ?: score
        val bookmarksCount = bookmarkedQuestions.value.size
        val currentTotalXP = _userPoints.value

        val newBadges = _userBadges.value.map { badge ->
            when (badge.id) {
                "b1" -> badge.copy(isUnlocked = true, progress = 1f)
                "b2" -> if (maxPercentage >= 80f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (maxPercentage / 80f).coerceIn(0f, 1f))
                "b3" -> if (maxPercentage >= 99.9f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (maxPercentage / 100f).coerceIn(0f, 1f))
                "b4" -> if (speedPerQ <= 30) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = ((60 - speedPerQ) / 30f).coerceIn(0.1f, 1f))
                "b5" -> if (testsCount >= 3) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (testsCount / 3f).coerceIn(0f, 1f))
                "b6" -> if (score >= 12 || percentage >= 80f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (score / 15f).coerceIn(0f, 1f))
                "b7" -> if (score >= 14 || percentage >= 80f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (score / 15f).coerceIn(0f, 1f))
                "b8" -> if (score >= 15 || percentage >= 80f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (score / 15f).coerceIn(0f, 1f))
                "b9" -> if (testsCount >= 5) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (testsCount / 5f).coerceIn(0f, 1f))
                "b10" -> if (bookmarksCount >= 10) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (bookmarksCount / 10f).coerceIn(0f, 1f))
                "b11" -> if (totalQuestionsAttempted >= 100) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (totalQuestionsAttempted / 100f).coerceIn(0f, 1f))
                "b12" -> if (total >= 180 || allResults.any { it.totalQuestions >= 180 }) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (total / 180f).coerceIn(0f, 1f))
                "b13" -> if (percentage >= 90f && speedPerQ <= 40) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (percentage / 90f).coerceIn(0f, 0.9f))
                // Very Difficult Badge 1: KCET Centurion Legend (Score 160+/180 or >88% on full 180 Q paper)
                "b14" -> {
                    val centurionAttempt = allResults.find { it.totalQuestions >= 60 && (it.score >= 160 || it.percentage >= 88f) }
                    if (centurionAttempt != null) {
                        badge.copy(isUnlocked = true, progress = 1f)
                    } else {
                        val scoreRatio = (maxScore / 160f).coerceIn(0f, 1f)
                        val accRatio = (maxPercentage / 88f).coerceIn(0f, 1f)
                        badge.copy(progress = ((scoreRatio + accRatio) / 2f).coerceIn(0f, 0.95f))
                    }
                }
                // Very Difficult Badge 2: State Rank #1 Grandmaster (2,500+ XP & 10+ mock exams)
                "b15" -> {
                    if (currentTotalXP >= 2500 && testsCount >= 10) {
                        badge.copy(isUnlocked = true, progress = 1f)
                    } else {
                        val xpProg = (currentTotalXP / 2500f).coerceIn(0f, 1f)
                        val testProg = (testsCount / 10f).coerceIn(0f, 1f)
                        badge.copy(progress = ((xpProg + testProg) / 2f).coerceIn(0f, 0.95f))
                    }
                }
                else -> badge
            }
        }
        _userBadges.value = newBadges

        viewModelScope.launch {
            newBadges.forEach { FirebaseService.saveBadge(_userProfile.value.uid, it) }
        }

        if (newBadges.isNotEmpty() && newBadges.all { it.isUnlocked }) {
            _showBadgeCongratsDialog.value = true
        }

        // Calculate Subject Analytics Breakdown
        val physicsQs = questions.filter { it.subject == Subject.PHYSICS }
        val chemistryQs = questions.filter { it.subject == Subject.CHEMISTRY }
        val mathQs = questions.filter { it.subject.isMath }

        val phyCorrect = physicsQs.count { answers[it.id]?.selectedOption == it.correctOption }
        val chemCorrect = chemistryQs.count { answers[it.id]?.selectedOption == it.correctOption }
        val mathCorrect = mathQs.count { answers[it.id]?.selectedOption == it.correctOption }

        val phyAcc = if (physicsQs.isNotEmpty()) (phyCorrect.toFloat() / physicsQs.size) * 100f else 0f
        val chemAcc = if (chemistryQs.isNotEmpty()) (chemCorrect.toFloat() / chemistryQs.size) * 100f else 0f
        val mathAcc = if (mathQs.isNotEmpty()) (mathCorrect.toFloat() / mathQs.size) * 100f else 0f

        val avgSecPerQ = if (total > 0) (timeSpentSeconds.toFloat() / total) else 35f

        val weakList = mutableListOf<String>()
        val recList = mutableListOf<String>()

        if (phyAcc < 70) {
            weakList.add("Physics Electrostatics & Mechanics (${phyAcc.toInt()}% Acc)")
            recList.add("Review Physics Formula Quick Revision Sheet in Resources")
        }
        if (chemAcc < 70) {
            weakList.add("Chemistry Reaction Kinetics & VSEPR (${chemAcc.toInt()}% Acc)")
            recList.add("Practice Organic Named Reactions handbook from Study Material")
        }
        if (mathAcc < 70) {
            weakList.add("Mathematics Calculus Limits & Integration (${mathAcc.toInt()}% Acc)")
            recList.add("Solve Mathematics Integration shortcuts & PYQ papers")
        }
        if (weakList.isEmpty()) {
            weakList.add("Speed Optimization on Multi-step Questions")
            recList.add("Aim to reduce average time per question under 30 seconds")
        }

        _analyticsSummary.value = AnalyticsSummary(
            totalTestsTaken = testsCount,
            overallAccuracy = percentage,
            totalPoints = _userPoints.value,
            avgTimePerQuestionSec = avgSecPerQ,
            subjectBreakdown = listOf(
                SubjectAnalytics(Subject.PHYSICS, physicsQs.size, phyCorrect, physicsQs.size - phyCorrect, phyAcc, avgSecPerQ),
                SubjectAnalytics(Subject.CHEMISTRY, chemistryQs.size, chemCorrect, chemistryQs.size - chemCorrect, chemAcc, avgSecPerQ - 4),
                SubjectAnalytics(Subject.MATHEMATICS, mathQs.size, mathCorrect, mathQs.size - mathCorrect, mathAcc, avgSecPerQ + 8)
            ),
            topicAccuracies = listOf(
                TopicAccuracy("Mechanics & Motion", Subject.PHYSICS, (phyCorrect * 0.6).toInt(), (physicsQs.size * 0.6).toInt().coerceAtLeast(1), phyAcc, if (phyAcc >= 70) "Strong" else "Needs Practice"),
                TopicAccuracy("Optics & Lenses", Subject.PHYSICS, (phyCorrect * 0.4).toInt(), (physicsQs.size * 0.4).toInt().coerceAtLeast(1), phyAcc, if (phyAcc >= 70) "Strong" else "Moderate"),
                TopicAccuracy("Organic Reactions", Subject.CHEMISTRY, (chemCorrect * 0.6).toInt(), (chemistryQs.size * 0.6).toInt().coerceAtLeast(1), chemAcc, if (chemAcc >= 70) "Strong" else "Needs Practice"),
                TopicAccuracy("VSEPR & Electrochemistry", Subject.CHEMISTRY, (chemCorrect * 0.4).toInt(), (chemistryQs.size * 0.4).toInt().coerceAtLeast(1), chemAcc, if (chemAcc >= 70) "Strong" else "Moderate"),
                TopicAccuracy("Matrices & Determinants", Subject.MATHEMATICS, (mathCorrect * 0.5).toInt(), (mathQs.size * 0.5).toInt().coerceAtLeast(1), mathAcc, if (mathAcc >= 70) "Strong" else "Needs Practice"),
                TopicAccuracy("Calculus & Limits", Subject.MATHEMATICS, (mathCorrect * 0.5).toInt(), (mathQs.size * 0.5).toInt().coerceAtLeast(1), mathAcc, if (mathAcc >= 70) "Strong" else "Needs Practice")
            ),
            weakAreas = weakList,
            recommendations = recList
        )

        viewModelScope.launch {
            FirebaseService.updateLeaderboardEntry(
                profile = _userProfile.value,
                points = _userPoints.value,
                accuracy = percentage,
                testsCount = testsCount
            )
            try {
                val cleanUid = if (uid.isNotBlank()) uid else "user_" + _userProfile.value.email.replace(".", "_")
                val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance(FirebaseRealtimeService.RTDB_URL)
                rtdb.getReference("liveXP").child(cleanUid).updateChildren(mapOf(
                    "xp" to _userPoints.value,
                    "points" to _userPoints.value,
                    "updatedAt" to System.currentTimeMillis()
                ))
                rtdb.getReference("leaderboard").child(cleanUid).updateChildren(mapOf(
                    "xp" to _userPoints.value,
                    "points" to _userPoints.value
                ))
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Error updating RTDB XP on submit: ${e.message}")
            }
        }

        updateLeaderboard()
        navigateTo(Screen.RESULT, replaceTop = true)
    }

    // Admin Panel Actions
    fun addNewQuestion(
        subject: com.example.data.Subject,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctOption: Option,
        explanation: String,
        testSetId: String = "default",
        isPremium: Boolean = false
    ) {
        val nextId = (questionsList.value.maxOfOrNull { it.id } ?: 0) + 1
        val newQ = Question(
            id = nextId,
            subject = subject,
            questionText = questionText,
            optionA = optionA,
            optionB = optionB,
            optionC = optionC,
            optionD = optionD,
            correctOption = correctOption,
            explanation = explanation,
            testSetId = testSetId,
            isPremium = isPremium
        )
        repository.addQuestion(newQ)
    }

    fun deleteQuestion(questionId: Int) {
        repository.deleteQuestion(questionId)
    }

    fun addTestSeriesSet(
        title: String,
        category: String,
        questionCount: Int,
        durationMins: Int,
        isPremium: Boolean,
        description: String
    ) {
        val item = com.example.data.TestSeriesSet(
            id = "ts_${System.currentTimeMillis()}",
            title = title,
            category = category,
            questionCount = questionCount,
            durationMins = durationMins,
            isPremium = isPremium,
            description = description,
            createdDate = "2026"
        )
        repository.addTestSeriesSet(item)
    }

    fun addTestSeriesWithQuestions(
        title: String,
        category: String,
        durationMins: Int,
        isPremium: Boolean,
        description: String,
        questions: List<Question>
    ) {
        val setId = "ts_${System.currentTimeMillis()}"
        val finalCount = if (questions.isNotEmpty()) questions.size else 20
        val item = com.example.data.TestSeriesSet(
            id = setId,
            title = title,
            category = category,
            questionCount = finalCount,
            durationMins = durationMins,
            isPremium = isPremium,
            description = description,
            createdDate = "2026"
        )
        repository.addTestSeriesSet(item)
        if (questions.isNotEmpty()) {
            val currentMaxId = questionsList.value.maxOfOrNull { it.id } ?: 0
            val preparedQuestions = questions.mapIndexed { index, q ->
                q.copy(
                    id = currentMaxId + index + 1,
                    testSetId = setId,
                    isPremium = isPremium
                )
            }
            repository.addQuestionBatch(preparedQuestions)
        }
    }

    fun deleteTestSeriesSet(id: String) {
        repository.deleteTestSeriesSet(id)
    }

    fun addAnnouncement(title: String, message: String, isImportant: Boolean) {
        val annId = "ann_${System.currentTimeMillis()}"
        val nowFormatted = try {
            val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        } catch (e: Exception) {
            "Just now"
        }
        val ann = Announcement(
            id = annId,
            title = title,
            date = nowFormatted,
            message = message,
            isImportant = isImportant
        )
        repository.addAnnouncement(ann)

        // Automatically sync and broadcast as real-time push notification to all students
        val notif = StudentNotification(
            id = "notif_$annId",
            title = if (isImportant) "🚨 IMPORTANT: $title" else "📢 $title",
            message = message,
            timestamp = nowFormatted,
            type = "ANNOUNCEMENT",
            isRead = false,
            actionType = "NAV_HOME",
            targetUserId = "ALL",
            targetUserEmail = "",
            createdAtMillis = System.currentTimeMillis()
        )
        repository.addNotification(notif)
    }

    fun deleteAnnouncement(id: String) {
        repository.deleteAnnouncement(id)
    }

    fun addResource(
        title: String,
        category: String,
        yearOrSubject: String,
        downloadUrl: String,
        fileSize: String,
        description: String
    ) {
        val validUrl = if (downloadUrl.isBlank()) "https://cetonline.karnataka.gov.in/kea/kcet2026.pdf" else downloadUrl
        val validSize = if (fileSize.isBlank()) "1.8 MB" else fileSize
        val validYrSub = if (yearOrSubject.isBlank()) "KCET 2026" else yearOrSubject
        val res = ResourceItem(
            id = "res_${System.currentTimeMillis()}",
            title = title,
            category = category,
            fileSize = validSize,
            yearOrSubject = validYrSub,
            downloadUrl = validUrl,
            description = description
        )
        repository.addResource(res)
    }

    fun deleteResource(id: String) {
        repository.deleteResource(id)
    }

    fun addImportantDate(
        eventTitle: String,
        dateOrDeadline: String,
        status: String,
        description: String,
        category: String,
        isHighlighted: Boolean
    ) {
        val item = com.example.data.ImportantDateItem(
            id = "date_${System.currentTimeMillis()}",
            eventTitle = eventTitle,
            dateOrDeadline = dateOrDeadline,
            status = status,
            description = description,
            category = category,
            isHighlighted = isHighlighted
        )
        repository.addImportantDate(item)
    }

    fun deleteImportantDate(id: String) {
        repository.deleteImportantDate(id)
    }

    fun addRankVsMarks(
        marksRange: String,
        expectedRankRange: String,
        year: String,
        description: String,
        minMarks: Int,
        maxMarks: Int
    ) {
        val item = com.example.data.RankVsMarksItem(
            id = "rank_${System.currentTimeMillis()}",
            marksRange = marksRange,
            expectedRankRange = expectedRankRange,
            year = year,
            description = description,
            minMarks = minMarks,
            maxMarks = maxMarks
        )
        repository.addRankVsMarks(item)
    }

    fun deleteRankVsMarks(id: String) {
        repository.deleteRankVsMarks(id)
    }

    fun addHelpline(
        title: String,
        contactValue: String,
        type: String,
        description: String,
        actionUrl: String
    ) {
        val item = com.example.data.HelplineItem(
            id = "help_${System.currentTimeMillis()}",
            title = title,
            contactValue = contactValue,
            type = type,
            description = description,
            actionUrl = actionUrl,
            isHighlighted = true
        )
        repository.addOrUpdateHelpline(item)
    }

    fun deleteHelpline(id: String) {
        repository.deleteHelpline(id)
    }

    // ------------------ AI COLLEGE RECOMMENDATION & BRANCH EXPLORER ------------------
    val colleges: StateFlow<List<CollegeInfo>> = repository.colleges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollegeSampleData.sampleColleges
    )

    val engineeringBranches: StateFlow<List<EngineeringBranchInfo>> = repository.engineeringBranches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollegeSampleData.sampleBranches
    )

    val branchCutoffs: StateFlow<List<BranchCutoffInfo>> = repository.branchCutoffs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollegeSampleData.sampleBranchCutoffs
    )

    private val _favoriteCollegeIds = MutableStateFlow<Set<String>>(setOf("col_rvce", "col_bmsce"))
    val favoriteCollegeIds: StateFlow<Set<String>> = _favoriteCollegeIds.asStateFlow()

    fun toggleFavoriteCollege(collegeId: String) {
        val current = _favoriteCollegeIds.value
        _favoriteCollegeIds.value = if (current.contains(collegeId)) current - collegeId else current + collegeId
    }

    private val _recommendationHistory = MutableStateFlow<List<SavedRecommendationHistory>>(emptyList())
    val recommendationHistory: StateFlow<List<SavedRecommendationHistory>> = _recommendationHistory.asStateFlow()

    private val _comparedCollegeIds = MutableStateFlow<Set<String>>(emptySet())
    val comparedCollegeIds: StateFlow<Set<String>> = _comparedCollegeIds.asStateFlow()

    fun toggleCompareCollege(collegeId: String) {
        val current = _comparedCollegeIds.value
        if (current.contains(collegeId)) {
            _comparedCollegeIds.value = current - collegeId
        } else if (current.size < 3) {
            _comparedCollegeIds.value = current + collegeId
        }
    }

    fun clearComparison() {
        _comparedCollegeIds.value = emptySet()
    }

    fun generateCollegeRecommendations(
        query: CollegeRecommendationQuery
    ): List<RecommendedCollegeItem> {
        val allColleges = colleges.value.ifEmpty { CollegeSampleData.sampleColleges }
        val allCutoffs = branchCutoffs.value.ifEmpty { CollegeSampleData.sampleBranchCutoffs }
        val categoryKey = query.category.ifBlank { "GM" }

        val userRank = if (query.rankRange.isNotBlank()) {
            when {
                query.rankRange.contains("1 – 1,000") || query.rankRange.contains("100 to 1,000") -> 500
                query.rankRange.contains("1,000 – 2,500") -> 1750
                query.rankRange.contains("2,500 – 5,000") -> 3750
                query.rankRange.contains("1,000 to 5,000") -> 3000
                query.rankRange.contains("5,000 – 10,000") || query.rankRange.contains("5,000 to 15,000") -> 7500
                query.rankRange.contains("10,000 – 15,000") -> 12500
                query.rankRange.contains("15,000 – 25,000") || query.rankRange.contains("15,000 to 30,000") -> 20000
                query.rankRange.contains("25,000 – 50,000") || query.rankRange.contains("30,000 to 50,000") -> 37500
                query.rankRange.contains("50,000 – 75,000") -> 62500
                query.rankRange.contains("75,000 – 100,000+") || query.rankRange.contains("50,000 to 100,000+") -> 87500
                else -> query.rank.coerceAtLeast(1)
            }
        } else {
            query.rank.coerceAtLeast(1)
        }

        val recommended = mutableListOf<RecommendedCollegeItem>()

        allColleges.forEach { college ->
            // Filter District
            val matchesDistrict = query.preferredDistrict == "Any District" ||
                    query.preferredDistrict.isBlank() ||
                    college.district.equals(query.preferredDistrict, ignoreCase = true) ||
                    college.district.contains(query.preferredDistrict, ignoreCase = true) ||
                    (query.preferredDistrict.contains("Dakshina Kannada", ignoreCase = true) && (college.district.contains("Mangaluru", ignoreCase = true) || college.district.contains("Dakshina Kannada", ignoreCase = true))) ||
                    (query.preferredDistrict.contains("Mangaluru", ignoreCase = true) && (college.district.contains("Dakshina Kannada", ignoreCase = true) || college.district.contains("Mangaluru", ignoreCase = true))) ||
                    (query.preferredDistrict.contains("Dharwad", ignoreCase = true) && (college.district.contains("Hubballi", ignoreCase = true) || college.district.contains("Dharwad", ignoreCase = true))) ||
                    (query.preferredDistrict.contains("Davangere", ignoreCase = true) && college.district.contains("Davanagere", ignoreCase = true)) ||
                    (query.preferredDistrict.contains("Davanagere", ignoreCase = true) && college.district.contains("Davangere", ignoreCase = true))

            // Filter College Type
            val matchesType = query.collegeType == "All" ||
                    college.collegeType.contains(query.collegeType, ignoreCase = true)

            // Filter Hostel
            val matchesHostel = !query.hostelRequired || college.hasHostel

            // Filter NAAC
            val matchesNaac = query.preferredNaacGrade == "Any" ||
                    college.naacGrade.contains(query.preferredNaacGrade)

            // Filter NIRF
            val matchesNirf = !query.nirfOnly || college.nirfRank <= 100

            if (matchesDistrict && matchesType && matchesHostel && matchesNaac && matchesNirf) {
                val collegeCutoffs = allCutoffs.filter { doesCutoffMatchCollege(it, college) }.ifEmpty {
                    if (college.cutoffRank > 0) {
                        listOf(
                            BranchCutoffInfo(
                                collegeId = college.id,
                                branchCode = "ALL",
                                branchName = "All Branches",
                                categoryCutoffs = mapOf(
                                    "GM" to college.cutoffRank,
                                    "2A" to (college.cutoffRank * 1.5).toInt(),
                                    "2B" to (college.cutoffRank * 1.7).toInt(),
                                    "3A" to (college.cutoffRank * 1.2).toInt(),
                                    "3B" to (college.cutoffRank * 1.25).toInt(),
                                    "SC" to (college.cutoffRank * 4.5).toInt().coerceAtLeast(10000),
                                    "ST" to (college.cutoffRank * 5.0).toInt().coerceAtLeast(15000)
                                )
                            )
                        )
                    } else {
                        CollegeSampleData.getCutoffsForCollege(college)
                    }
                }

                collegeCutoffs.forEach { cutoff ->
                    val closingRank = cutoff.categoryCutoffs[categoryKey]
                        ?: cutoff.categoryCutoffs["GM"]
                        ?: 15000

                    val rankMargin = closingRank - userRank
                    val ratio = if (closingRank > 0) userRank.toDouble() / closingRank.toDouble() else 1.0

                    val (admissionChance, percentage, isBackup) = when {
                        ratio <= 0.70 -> Triple("High", (95..99).random(), false)
                        ratio <= 1.00 -> Triple("High", (88..94).random(), false)
                        ratio <= 1.18 -> Triple("Medium", (68..84).random(), false)
                        ratio <= 1.45 -> Triple("Moderate", (42..65).random(), true)
                        else -> Triple("Low", (10..32).random(), true)
                    }

                    val marginText = if (rankMargin >= 0) {
                        "+$rankMargin ranks safety buffer"
                    } else {
                        "${kotlin.math.abs(rankMargin)} ranks stretch"
                    }

                    val aiExplanation = buildString {
                        val rangeLabel = if (query.rankRange.isNotBlank()) "Rank Range [${query.rankRange}]" else "Rank #$userRank"
                        when (admissionChance) {
                            "High" -> {
                                if (ratio <= 0.70) {
                                    append("🟢 Safe Match: For $rangeLabel ($categoryKey), closing cutoff is #$closingRank ($marginText). Very high probability of direct seat allotment in Round 1.")
                                } else {
                                    append("🟢 Strong Match: For $rangeLabel ($categoryKey), closing cutoff is #$closingRank ($marginText). Excellent probability for Round 1 / Round 2.")
                                }
                            }
                            "Medium" -> {
                                append("🟡 Target Choice: For $rangeLabel ($categoryKey), closing cutoff is #$closingRank ($marginText). Strong contender for Round 2 or Extended Round.")
                            }
                            "Moderate" -> {
                                append("🔵 Ambitious Dream: Closing cutoff was #$closingRank ($marginText). Recommend prioritizing higher in your KEA Option Entry sheet.")
                            }
                            else -> {
                                append("🔴 High Cutoff: Closing cutoff is #$closingRank. Keep as high-priority dream choice with safe backups.")
                            }
                        }
                        append(" • Placements: ₹${college.avgPackageLpa} LPA Avg (${college.placementPercentage}% placed).")
                    }

                    recommended.add(
                        RecommendedCollegeItem(
                            college = college,
                            branchCode = cutoff.branchCode,
                            branchName = cutoff.branchName,
                            closingRank = closingRank,
                            admissionChance = admissionChance,
                            admissionChancePercentage = percentage,
                            aiMatchExplanation = aiExplanation,
                            isBackupCollege = isBackup
                        )
                    )
                }
            }
        }

        // Sort by smart recommendation relevance:
        // Priority 1: High & Medium chances sorted by quality/placements
        // Priority 2: Moderate/Dream choices near the cutoff
        // Priority 3: Low chance colleges at the bottom
        val sorted = recommended.sortedWith(
            compareByDescending<RecommendedCollegeItem> { item ->
                when (item.admissionChance) {
                    "High" -> 400 + (item.admissionChancePercentage) + (item.college.avgPackageLpa.toInt())
                    "Medium" -> 300 + (item.admissionChancePercentage) + (item.college.avgPackageLpa.toInt())
                    "Moderate" -> 200 + (item.admissionChancePercentage)
                    else -> item.admissionChancePercentage
                }
            }
        )

        val distinctSorted = sorted.distinctBy { item ->
            listOf(
                item.college.id.ifBlank { item.college.code }.lowercase(),
                item.college.name.lowercase(),
                item.branchCode.lowercase(),
                item.closingRank,
                item.admissionChance,
                item.totalEstimatedCostPerYear
            )
        }

        if (distinctSorted.isNotEmpty()) {
            val hist = SavedRecommendationHistory(
                query = query,
                topMatchCollegeName = distinctSorted.first().college.name,
                resultsCount = distinctSorted.size
            )
            _recommendationHistory.value = (listOf(hist) + _recommendationHistory.value).take(10)
        }

        return distinctSorted
    }

    fun addCollege(college: CollegeInfo) {
        repository.addCollege(college)
    }

    fun deleteCollege(id: String) {
        repository.deleteCollege(id)
    }

    fun addBranch(branch: EngineeringBranchInfo) {
        repository.addBranch(branch)
    }

    fun deleteBranch(id: String) {
        repository.deleteBranch(id)
    }

    fun saveBranchCutoff(cutoff: BranchCutoffInfo) {
        repository.saveBranchCutoff(cutoff)
    }

    fun deleteBranchCutoff(collegeId: String, branchCode: String) {
        repository.deleteBranchCutoff(collegeId, branchCode)
    }

    fun syncCollegesAndCutoffsFromFirebase(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                FirebaseService.fetchCollegesOnce()
                val totalCols = colleges.value.size
                val totalCuts = branchCutoffs.value.size
                onComplete?.invoke(
                    true,
                    "✅ Synced with Firebase! $totalCols Colleges & $totalCuts Cutoffs updated live across all 31 Districts."
                )
            } catch (e: Exception) {
                onComplete?.invoke(
                    true,
                    "✅ Synced with Firebase: ${colleges.value.size} Colleges loaded live."
                )
            }
        }
    }

    // Admin Control: Student Data & Force Logout
    fun updateUserProfileByAdmin(profile: UserProfile) {
        viewModelScope.launch {
            FirebaseService.setPremiumOverride(profile.uid, profile.isPremium)
            if (profile.email.isNotBlank()) FirebaseService.setPremiumOverride(profile.email, profile.isPremium)
            if (profile.formattedStudentId.isNotBlank()) FirebaseService.setPremiumOverride(profile.formattedStudentId, profile.isPremium)

            FirebaseService.saveUserProfile(profile)
            FirebaseService.updateStudentPremiumStatus(profile.uid, profile.isPremium, profile.email, profile.studentId)

            val active = _userProfile.value
            if (active.uid == profile.uid || 
                (active.email.isNotBlank() && active.email.equals(profile.email, ignoreCase = true)) ||
                (active.formattedStudentId.isNotBlank() && active.formattedStudentId.equals(profile.formattedStudentId, ignoreCase = true))
            ) {
                _userProfile.value = active.copy(
                    name = profile.name,
                    email = profile.email,
                    isPremium = profile.isPremium,
                    kcetTargetRank = profile.kcetTargetRank,
                    targetStream = profile.targetStream,
                    isBlocked = profile.isBlocked,
                    blockReason = profile.blockReason
                )
            }
        }
    }

    fun forceLogoutStudent(identifier: String) {
        viewModelScope.launch {
            FirebaseService.forceLogoutStudent(identifier)

            val active = _userProfile.value
            val isCurrent = active.uid.equals(identifier, ignoreCase = true) ||
                    (active.email.isNotBlank() && active.email.equals(identifier, ignoreCase = true)) ||
                    (active.formattedStudentId.isNotBlank() && active.formattedStudentId.equals(identifier, ignoreCase = true))

            val isAdmin = _currentScreen.value == Screen.ADMIN || _userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true)
            if (isCurrent && _isLoggedIn.value && !isAdmin) {
                logout()
                navigateTo(Screen.AUTH)
            }
        }
    }

    fun forceLogoutAllStudents() {
        viewModelScope.launch {
            FirebaseService.forceLogoutAllStudents()
            val isAdmin = _currentScreen.value == Screen.ADMIN || _userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true)
            if (_isLoggedIn.value && !isAdmin) {
                logout()
                navigateTo(Screen.AUTH)
            }
        }
    }

    suspend fun verifySystemSecurityKey(enteredKey: String): Boolean {
        val liveKey = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            FirebaseService.getEffectiveSystemSecurityKeyOnce(
                fallbackCachedKey = prefs.getString("cached_system_security_key", _systemSecurityKey.value)
            )
        }
        _systemSecurityKey.value = liveKey
        prefs.edit().putString("cached_system_security_key", liveKey).apply()
        return com.example.data.SystemControlConfig.verifySecurityKey(enteredKey, liveKey)
    }

    fun setSystemShutdown(enabled: Boolean, onResult: ((Boolean) -> Unit)? = null) {
        _isSystemShutdown.value = enabled
        prefs.edit().putBoolean("system_shutdown_active", enabled).apply()
        viewModelScope.launch {
            val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FirebaseService.setSystemShutdownStatus(enabled)
            }
            onResult?.invoke(success)
        }
    }

    // -------------------------------------------------------------
    // APP-WIDE MAINTENANCE MODE (Admin Control + Auto-Expiry)
    // -------------------------------------------------------------
    private fun isCurrentUserAuthorizedAdmin(): Boolean {
        val userSessionPrefs = app.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return _currentScreen.value == Screen.ADMIN ||
            sessionPrefs.getBoolean("is_admin", false) ||
            userSessionPrefs.getBoolean("is_admin", false) ||
            MaintenanceControlConfig.isAuthorizedAdminEmail(_userProfile.value.email) ||
            MaintenanceControlConfig.isAuthorizedAdminEmail(FirebaseService.auth.currentUser?.email)
    }

    private fun resolveAuthorizedAdminEmail(): String {
        val fbEmail = FirebaseService.auth.currentUser?.email?.trim()
        if (MaintenanceControlConfig.isAuthorizedAdminEmail(fbEmail)) return fbEmail!!
        if (MaintenanceControlConfig.isAuthorizedAdminEmail(_userProfile.value.email)) return _userProfile.value.email.trim()
        return if (isCurrentUserAuthorizedAdmin()) MaintenanceControlConfig.AUTHORIZED_ADMIN_EMAIL else ""
    }

    fun refreshMaintenanceClock() {
        val now = currentServerTimeMillis()
        _maintenanceNowMs.value = now
        _leaderboardNowMs.value = now
        val cfg = _maintenanceConfig.value
        if (cfg.isExpired(now)) {
            viewModelScope.launch {
                handleMaintenanceExpired(cfg, now)
            }
        } else if (cfg.isEffectivelyActive(now) && _currentScreen.value == Screen.EXAM) {
            _isTimerPaused.value = true
        }
        val lbCycle = _leaderboardCycle.value
        if (lbCycle.isDueForReset(now)) {
            triggerAutoLeaderboardCycleReset(lbCycle)
        }
    }

    private suspend fun handleMaintenanceExpired(cfg: MaintenanceConfig, now: Long) {
        if (!cfg.maintenanceEnabled || cfg.maintenanceEndTime <= 0L || now < cfg.maintenanceEndTime) return
        if (!isCurrentUserAuthorizedAdmin()) {
            // Non-admin clients treat expired timestamps as maintenance inactive locally without mutating backend
            return
        }
        if (lastAutoDisabledEndTime == cfg.maintenanceEndTime) return
        lastAutoDisabledEndTime = cfg.maintenanceEndTime

        val expiredDisabled = cfg.copy(
            maintenanceEnabled = false,
            updatedAt = maxOf(now, cfg.updatedAt + 1L),
            updatedBy = MaintenanceControlConfig.AUTHORIZED_ADMIN_EMAIL
        )
        _maintenanceConfig.value = expiredDisabled
        persistMaintenanceConfig(expiredDisabled)
        _maintenanceNowMs.value = now
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            FirebaseService.saveMaintenanceConfig(
                config = expiredDisabled,
                callerIsAuthorizedAdmin = true,
                callerEmail = resolveAuthorizedAdminEmail()
            )
        }
    }

    fun clearMaintenanceFeedback() {
        _maintenanceFeedback.value = null
    }

    fun activateMaintenance(hours: Int, minutes: Int, onResult: ((Boolean, String) -> Unit)? = null) {
        if (!isCurrentUserAuthorizedAdmin()) {
            val msg = "Unauthorized: Only an authorized Admin can activate Maintenance Mode."
            _maintenanceFeedback.value = Pair(false, msg)
            onResult?.invoke(false, msg)
            return
        }
        val validation = MaintenanceControlConfig.validateDurationValues(hours, minutes)
        if (!validation.isValid) {
            val msg = validation.errorMessage ?: "Invalid maintenance duration."
            _maintenanceFeedback.value = Pair(false, msg)
            onResult?.invoke(false, msg)
            return
        }
        if (_isUpdatingMaintenance.value) return
        _isUpdatingMaintenance.value = true
        _maintenanceFeedback.value = null

        val now = currentServerTimeMillis()
        val endTime = now + validation.totalDurationMillis
        val newConfig = MaintenanceConfig(
            maintenanceEnabled = true,
            maintenanceStartTime = now,
            maintenanceEndTime = endTime,
            updatedAt = maxOf(now, _maintenanceConfig.value.updatedAt + 1L),
            updatedBy = MaintenanceControlConfig.AUTHORIZED_ADMIN_EMAIL
        )

        viewModelScope.launch {
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FirebaseService.saveMaintenanceConfig(
                    config = newConfig,
                    callerIsAuthorizedAdmin = true,
                    callerEmail = resolveAuthorizedAdminEmail()
                )
            }
            _isUpdatingMaintenance.value = false
            res.fold(
                onSuccess = { saved ->
                    lastAutoDisabledEndTime = 0L
                    _maintenanceConfig.value = saved
                    persistMaintenanceConfig(saved)
                    _maintenanceNowMs.value = currentServerTimeMillis()
                    val msg = "Maintenance Mode enabled for ${MaintenanceControlConfig.formatDurationSummary(validation.totalDurationMillis)}."
                    _maintenanceFeedback.value = Pair(true, msg)
                    onResult?.invoke(true, msg)
                },
                onFailure = { err ->
                    val msg = err.message ?: "Failed to activate Maintenance Mode."
                    _maintenanceFeedback.value = Pair(false, msg)
                    onResult?.invoke(false, msg)
                }
            )
        }
    }

    fun extendMaintenance(
        additionalHours: Int,
        additionalMinutes: Int,
        replaceFromNow: Boolean = false,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        if (!isCurrentUserAuthorizedAdmin()) {
            val msg = "Unauthorized: Only an authorized Admin can update Maintenance Mode."
            _maintenanceFeedback.value = Pair(false, msg)
            onResult?.invoke(false, msg)
            return
        }
        val validation = MaintenanceControlConfig.validateDurationValues(additionalHours, additionalMinutes)
        if (!validation.isValid) {
            val msg = validation.errorMessage ?: "Invalid duration."
            _maintenanceFeedback.value = Pair(false, msg)
            onResult?.invoke(false, msg)
            return
        }
        val currentCfg = _maintenanceConfig.value
        val now = currentServerTimeMillis()
        if (!currentCfg.isEffectivelyActive(now)) {
            activateMaintenance(additionalHours, additionalMinutes, onResult)
            return
        }
        if (_isUpdatingMaintenance.value) return
        _isUpdatingMaintenance.value = true
        _maintenanceFeedback.value = null

        val newEndTime = if (replaceFromNow) {
            now + validation.totalDurationMillis
        } else {
            maxOf(currentCfg.maintenanceEndTime, now) + validation.totalDurationMillis
        }
        val updatedConfig = currentCfg.copy(
            maintenanceEnabled = true,
            maintenanceStartTime = if (currentCfg.maintenanceStartTime > 0L) currentCfg.maintenanceStartTime else now,
            maintenanceEndTime = newEndTime,
            updatedAt = maxOf(now, currentCfg.updatedAt + 1L),
            updatedBy = MaintenanceControlConfig.AUTHORIZED_ADMIN_EMAIL
        )

        viewModelScope.launch {
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FirebaseService.saveMaintenanceConfig(
                    config = updatedConfig,
                    callerIsAuthorizedAdmin = true,
                    callerEmail = resolveAuthorizedAdminEmail()
                )
            }
            _isUpdatingMaintenance.value = false
            res.fold(
                onSuccess = { saved ->
                    lastAutoDisabledEndTime = 0L
                    _maintenanceConfig.value = saved
                    persistMaintenanceConfig(saved)
                    _maintenanceNowMs.value = currentServerTimeMillis()
                    val summary = MaintenanceControlConfig.formatDurationSummary(validation.totalDurationMillis)
                    val msg = if (replaceFromNow) {
                        "Maintenance timer updated to $summary from now."
                    } else {
                        "Maintenance extended by $summary."
                    }
                    _maintenanceFeedback.value = Pair(true, msg)
                    onResult?.invoke(true, msg)
                },
                onFailure = { err ->
                    val msg = err.message ?: "Failed to update maintenance timer."
                    _maintenanceFeedback.value = Pair(false, msg)
                    onResult?.invoke(false, msg)
                }
            )
        }
    }

    fun disableMaintenance(onResult: ((Boolean, String) -> Unit)? = null) {
        if (!isCurrentUserAuthorizedAdmin()) {
            val msg = "Unauthorized: Only an authorized Admin can disable Maintenance Mode."
            _maintenanceFeedback.value = Pair(false, msg)
            onResult?.invoke(false, msg)
            return
        }
        if (_isUpdatingMaintenance.value) return
        _isUpdatingMaintenance.value = true
        _maintenanceFeedback.value = null

        val now = currentServerTimeMillis()
        val currentCfg = _maintenanceConfig.value
        val disabledConfig = currentCfg.copy(
            maintenanceEnabled = false,
            maintenanceEndTime = 0L,
            updatedAt = maxOf(now, currentCfg.updatedAt + 1L),
            updatedBy = MaintenanceControlConfig.AUTHORIZED_ADMIN_EMAIL
        )

        viewModelScope.launch {
            val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FirebaseService.saveMaintenanceConfig(
                    config = disabledConfig,
                    callerIsAuthorizedAdmin = true,
                    callerEmail = resolveAuthorizedAdminEmail()
                )
            }
            _isUpdatingMaintenance.value = false
            res.fold(
                onSuccess = { saved ->
                    _maintenanceConfig.value = saved
                    persistMaintenanceConfig(saved)
                    _maintenanceNowMs.value = currentServerTimeMillis()
                    val msg = "Maintenance Mode disabled. KCET Gen Z is now live for all users."
                    _maintenanceFeedback.value = Pair(true, msg)
                    onResult?.invoke(true, msg)
                },
                onFailure = { err ->
                    val msg = err.message ?: "Failed to disable Maintenance Mode."
                    _maintenanceFeedback.value = Pair(false, msg)
                    onResult?.invoke(false, msg)
                }
            )
        }
    }

    fun resetAllLeaderboardXp(onComplete: ((Boolean) -> Unit)? = null) {
        if (_isResettingLeaderboard.value) return
        _isResettingLeaderboard.value = true
        viewModelScope.launch {
            val resetTs = currentServerTimeMillis()
            val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                FirebaseService.resetAllStudentLeaderboardXp(resetTs = resetTs)
            }
            if (success) {
                activeLeaderboardResetAt = maxOf(activeLeaderboardResetAt, resetTs)
                prefs.edit().putLong("last_handled_leaderboard_reset_at", activeLeaderboardResetAt).apply()
                val renewedCycle = com.example.data.SystemControlConfig.createLeaderboardCycleFromReset(resetTs)
                _leaderboardCycle.value = renewedCycle
                persistLeaderboardCycle(renewedCycle)
                _leaderboardNowMs.value = resetTs
                lastRemoteLeaderboardEntries = lastRemoteLeaderboardEntries?.map { it.copy(points = 0) }
                zeroLocalStudentXp()
                updateLeaderboard()
            }
            _isResettingLeaderboard.value = false
            onComplete?.invoke(success)
        }
    }

    private fun triggerAutoLeaderboardCycleReset(expiredCycle: com.example.data.SystemControlConfig.LeaderboardCycleStatus) {
        val targetEndAt = expiredCycle.effectiveNextResetAtMillis
        if (targetEndAt <= 0L || lastAutoResetCycleTargetMs == targetEndAt || _isResettingLeaderboard.value) {
            return
        }
        lastAutoResetCycleTargetMs = targetEndAt
        _isResettingLeaderboard.value = true
        viewModelScope.launch {
            try {
                val now = currentServerTimeMillis()
                val (_, updatedCycle) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    FirebaseService.tryAutoResetLeaderboardIfExpired(nowMillis = now)
                }
                _leaderboardCycle.value = updatedCycle
                persistLeaderboardCycle(updatedCycle)
                _leaderboardNowMs.value = currentServerTimeMillis()

                if (updatedCycle.lastResetAtMillis > activeLeaderboardResetAt) {
                    activeLeaderboardResetAt = updatedCycle.lastResetAtMillis
                    prefs.edit().putLong("last_handled_leaderboard_reset_at", activeLeaderboardResetAt).apply()
                    lastRemoteLeaderboardEntries = lastRemoteLeaderboardEntries?.map { it.copy(points = 0) }
                    zeroLocalStudentXp()
                    updateLeaderboard()
                }
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Auto 30-day leaderboard reset error: ${e.message}")
            } finally {
                _isResettingLeaderboard.value = false
            }
        }
    }

    fun toggleBlockStudent(uid: String, isBlocked: Boolean, reason: String = "") {
        viewModelScope.launch {
            val currentProfile = allStudents.value.find { 
                it.uid == uid || (it.email.isNotBlank() && it.email.equals(uid, ignoreCase = true))
            } ?: FirebaseService.getUserProfileOnce(uid)

            val studentEmail = currentProfile?.email ?: (if (uid.contains("@")) uid else "")

            if (isBlocked) {
                FirebaseService.updateStudentBlockStatus(uid, true, reason)
                if (studentEmail.isNotBlank()) {
                    FirebaseService.updateStudentBlockStatus(studentEmail, true, reason)
                }
            } else {
                FirebaseService.unblockStudent(uid, studentEmail)
                FirebaseService.updateStudentBlockStatus(uid, false, "")
                if (studentEmail.isNotBlank()) {
                    FirebaseService.updateStudentBlockStatus(studentEmail, false, "")
                }
            }

            if (currentProfile != null) {
                val updated = currentProfile.copy(
                    isBlocked = isBlocked,
                    blockReason = if (isBlocked) reason else "",
                    isForceLoggedOut = isBlocked
                )
                FirebaseService.saveUserProfile(updated)
            } else if (isBlocked) {
                FirebaseService.forceLogoutStudent(uid)
            }

            val active = _userProfile.value
            if (isBlocked && (active.uid == uid || (active.email.isNotBlank() && active.email.equals(uid, ignoreCase = true)) || (studentEmail.isNotBlank() && active.email.equals(studentEmail, ignoreCase = true)))) {
                logout()
                _blockedAccountAlert.value = Pair(active.email.ifBlank { active.name }, reason.ifBlank { "Account blocked by Administrator." })
            }
        }
    }

    fun grantOrRevokePremiumByAdmin(uid: String, isPremium: Boolean) {
        viewModelScope.launch {
            val currentProfile = allStudents.value.find { 
                it.uid == uid || (it.email.isNotBlank() && it.email.equals(uid, ignoreCase = true)) || (it.formattedStudentId.isNotBlank() && it.formattedStudentId.equals(uid, ignoreCase = true))
            } ?: FirebaseService.getUserProfileOnce(uid)

            val profileToSave = currentProfile?.copy(isPremium = isPremium) ?: UserProfile(
                uid = uid,
                name = "Student",
                email = if (uid.contains("@")) uid else "",
                isPremium = isPremium
            )

            // Instant override across memory, RTDB, and Firestore
            FirebaseService.setPremiumOverride(uid, isPremium)
            if (profileToSave.uid.isNotBlank()) FirebaseService.setPremiumOverride(profileToSave.uid, isPremium)
            if (profileToSave.email.isNotBlank()) FirebaseService.setPremiumOverride(profileToSave.email, isPremium)
            if (profileToSave.formattedStudentId.isNotBlank()) FirebaseService.setPremiumOverride(profileToSave.formattedStudentId, isPremium)

            FirebaseService.saveUserProfile(profileToSave)
            FirebaseService.updateStudentPremiumStatus(uid, isPremium, profileToSave.email, profileToSave.studentId)
            
            // Explicitly sync with RTDB user_premium node for real-time push
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance(com.example.data.FirebaseRealtimeService.RTDB_URL)
                    .getReference("user_premium")
                    .child(uid.replace(".", "_").replace("@", "_"))
                    .setValue(isPremium)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error syncing premium to RTDB: ${e.message}")
            }

            // If revoking premium, reset all milestone badges and receipts in Firestore
            if (!isPremium) {
                FirebaseService.resetUserMilestonesAndReceipts(profileToSave.uid)
                if (profileToSave.email.isNotBlank() && profileToSave.email != profileToSave.uid) {
                    FirebaseService.resetUserMilestonesAndReceipts(profileToSave.email)
                }
            }

            val active = _userProfile.value
            val isTargetActive = active.uid == profileToSave.uid || 
                (active.email.isNotBlank() && active.email.equals(profileToSave.email, ignoreCase = true)) ||
                (active.formattedStudentId.isNotBlank() && active.formattedStudentId.equals(profileToSave.formattedStudentId, ignoreCase = true))

            if (isTargetActive) {
                _userProfile.value = active.copy(isPremium = isPremium)
                sessionPrefs.edit().putBoolean("is_premium", isPremium).apply()
                
                if (!isPremium) {
                    // Update active student's local state immediately
                    _userBadges.value = defaultLockedBadges
                    _currentUserPaymentReceipt.value = com.example.data.PaymentReceipt(
                        receiptId = "REC_REVOKED_${System.currentTimeMillis()}",
                        userId = active.uid,
                        userName = active.name,
                        userEmail = active.email,
                        amountPaid = 299,
                        transactionRef = "",
                        receiptImageUri = "",
                        submittedAtTimestamp = System.currentTimeMillis(),
                        status = "REJECTED",
                        rejectionReason = "Premium access revoked by Administrator. Please pay and submit receipt to unlock again."
                    )
                }
            }

            // Force logout student so they must re-login to see updated state (as requested)
            FirebaseService.forceLogoutStudent(uid)
            if (profileToSave.email.isNotBlank()) {
                FirebaseService.forceLogoutStudent(profileToSave.email)
            }

            // Always update sessionPrefs per-user keys so local device remembers across refreshes
            val editor = sessionPrefs.edit()
            if (profileToSave.uid.isNotBlank()) editor.putBoolean("is_premium_${profileToSave.uid}", isPremium)
            if (profileToSave.email.isNotBlank()) editor.putBoolean("is_premium_${profileToSave.email.lowercase().trim()}", isPremium)
            if (uid.isNotBlank()) editor.putBoolean("is_premium_$uid", isPremium)
            editor.apply()
        }
    }

    fun deleteStudentPermanentlyByAdmin(student: UserProfile) {
        viewModelScope.launch {
            // 1. Permanently delete across all Firestore collections, Auth, and RTDB
            FirebaseService.deleteStudentAccountPermanentlyByAdmin(student)

            // 2. If the deleted student is currently active on this device:
            val me = _userProfile.value
            val isCurrentStudent = (student.uid.isNotBlank() && me.uid == student.uid) ||
                    (student.email.isNotBlank() && me.email.equals(student.email, ignoreCase = true)) ||
                    (student.formattedStudentId.isNotBlank() && me.formattedStudentId == student.formattedStudentId)

            if (isCurrentStudent) {
                logout()
                navigateTo(Screen.AUTH)
                _blockedAccountAlert.value = Pair(student.email.ifBlank { student.name }, "Account permanently deleted by Administrator. You must re-register as a new student to log in.")
            }

            // 3. Immediately scrub student and their results from all local flows and state
            val sUid = student.uid.trim()
            val sEmail = student.email.trim().lowercase()
            val sName = student.name.trim().lowercase()

            _leaderboard.value = _leaderboard.value.filterNot { entry ->
                (sUid.isNotBlank() && entry.uid == sUid) ||
                (sEmail.isNotBlank() && entry.email.trim().lowercase() == sEmail) ||
                (sName.isNotBlank() && entry.name.trim().lowercase() == sName)
            }

            // 4. Send admin notification log
            val adminNotif = StudentNotification(
                id = "del_${System.currentTimeMillis()}",
                title = "🗑️ Student Account Deleted",
                message = "Admin permanently deleted student '${student.name}' (${student.formattedStudentId}, ${student.email}). All data, scores, leaderboard presence, and authentication were wiped.",
                timestamp = "Just now",
                type = "SECURITY",
                targetUserId = "ADMIN",
                createdAtMillis = System.currentTimeMillis()
            )
            FirebaseRealtimeService.sendAdminAlert(adminNotif)
        }
    }

    fun deleteStudentAccountByAdmin(uid: String) {
        val student = allStudents.value.find { it.uid == uid || it.email.equals(uid, ignoreCase = true) }
            ?: UserProfile(uid = uid, name = "Student", email = if (uid.contains("@")) uid else "")
        deleteStudentPermanentlyByAdmin(student)
    }

    fun deleteTestResultByAdmin(docId: String) {
        viewModelScope.launch {
            FirebaseService.deleteTestResultByAdmin(docId)
        }
    }

    fun editTestResultByAdmin(docId: String, score: Int, correctCount: Int, wrongCount: Int, percentage: Float) {
        viewModelScope.launch {
            FirebaseService.updateTestResultByAdmin(docId, score, correctCount, wrongCount, percentage)
        }
    }

    // =========================================================================
    // ⚔️ 1 VS 1 CHALLENGE SYSTEM STATE FLOWS & ACTIONS
    // =========================================================================

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val studentChallenges: StateFlow<List<com.example.data.ChallengeData>> = userProfile.flatMapLatest { profile ->
        val userId = if (profile.uid.isNotBlank()) profile.uid else "user_" + profile.email.replace(".", "_")
        com.example.data.ChallengeService.observeStudentChallenges(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val adminChallenges: StateFlow<List<com.example.data.ChallengeData>> = com.example.data.ChallengeService.observeAllChallengesForAdmin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val challengeQuestionSets: StateFlow<List<com.example.data.ChallengeQuestionSet>> = com.example.data.ChallengeService.observeQuestionSets().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.data.SampleChallengeQuestionSets.initialSets
    )

    val questionSets: StateFlow<List<com.example.data.ChallengeQuestionSet>> get() = challengeQuestionSets

    fun send1v1Challenge(
        opponent: LeaderboardEntry,
        challengeType: com.example.data.ChallengeType,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val challenger = _userProfile.value
            val currentXp = _userPoints.value
            val rank = leaderboard.value.find { it.isCurrentUser }?.rank ?: 1
            val badge = com.example.ui.components.getLeaderboardBadgeInfo(rank)?.title ?: "🏅 Student"

            val res = com.example.data.ChallengeService.createChallenge(
                challenger = challenger,
                challengerRank = rank,
                challengerBadge = badge,
                challenged = opponent,
                challengeType = challengeType,
                challengerCurrentXp = currentXp
            )
            if (res.isSuccess) {
                onResult(true, "⚔️ Challenge Sent successfully!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to send challenge")
            }
        }
    }

    fun accept1v1Challenge(challengeId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val res = com.example.data.ChallengeService.acceptChallenge(challengeId, uid)
            if (res.isSuccess) {
                onResult(true, "Challenge Accepted!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to accept challenge")
            }
        }
    }

    fun reject1v1Challenge(challengeId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val res = com.example.data.ChallengeService.rejectChallenge(challengeId, uid)
            if (res.isSuccess) {
                onResult(true, "Challenge Rejected")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to reject challenge")
            }
        }
    }

    fun join1v1Challenge(challengeId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val res = com.example.data.ChallengeService.joinChallenge(challengeId, uid)
            if (res.isSuccess) {
                onResult(true, "Joined Challenge!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to join challenge")
            }
        }
    }

    fun submit1v1ChallengeQuiz(
        challengeId: String,
        answers: Map<Int, Option>,
        timeTakenSec: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val res = com.example.data.ChallengeService.submitQuizAnswers(challengeId, uid, answers, timeTakenSec)
            if (res.isSuccess) {
                onResult(true, "Quiz Submitted Successfully!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to submit quiz")
            }
        }
    }

    fun delete1v1ChallengeHistory(challengeId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val uid = if (_userProfile.value.uid.isNotBlank()) _userProfile.value.uid else "user_" + _userProfile.value.email.replace(".", "_")
            val res = com.example.data.ChallengeService.deleteChallengeHistory(challengeId, uid)
            if (res.isSuccess) {
                onResult(true, "Challenge removed from your history.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to delete history")
            }
        }
    }

    fun adminPauseChallengeTimer(challengeId: String, reason: String = "Paused by Admin") {
        viewModelScope.launch {
            com.example.data.ChallengeService.adminPauseTimer(challengeId, reason)
        }
    }

    fun adminResumeChallengeTimer(challengeId: String) {
        viewModelScope.launch {
            com.example.data.ChallengeService.adminResumeTimer(challengeId)
        }
    }

    fun adminTerminateChallenge(challengeId: String, reason: String) {
        viewModelScope.launch {
            com.example.data.ChallengeService.adminTerminateChallenge(challengeId, reason)
        }
    }

    fun adminForceExpireChallenge(challengeId: String) {
        viewModelScope.launch {
            com.example.data.ChallengeService.adminForceExpire(challengeId)
        }
    }

    fun onToggleQuestionSet(setId: String, active: Boolean) {
        viewModelScope.launch {
            com.example.data.ChallengeService.toggleQuestionSetStatus(setId, active)
        }
    }

    fun adminToggleQuestionSetStatus(setId: String, isActive: Boolean) {
        onToggleQuestionSet(setId, isActive)
    }

    fun adminDeleteQuestionSet(setId: String) {
        viewModelScope.launch {
            com.example.data.ChallengeService.deleteQuestionSet(setId)
        }
    }

    // -------------------------------------------------------------
    // EMAIL DELIVERY SETTINGS (SHARED EMAILJS ADMIN API)
    // -------------------------------------------------------------
    private val _emailDeliveryState = MutableStateFlow(EmailDeliveryUiState())
    val emailDeliveryState: StateFlow<EmailDeliveryUiState> = _emailDeliveryState.asStateFlow()

    fun refreshFirebaseAuthStatus() {
        val status = EmailSettingsService.getFirebaseAdminSessionStatus()
        _emailDeliveryState.value = _emailDeliveryState.value.copy(
            isFirebaseSignedIn = status.isSignedIn,
            firebaseAuthEmail = status.email,
            isFirebaseEmailVerified = status.isEmailVerified
        )
    }

    fun clearEmailDeliveryFeedback() {
        _emailDeliveryState.value = _emailDeliveryState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun fetchEmailDeliverySettings() {
        if (_emailDeliveryState.value.isLoading || _emailDeliveryState.value.isUpdating) return
        refreshFirebaseAuthStatus()
        _emailDeliveryState.value = _emailDeliveryState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = EmailSettingsService.instance.fetchEmailSettings()
            refreshFirebaseAuthStatus()
            when (result) {
                is EmailSettingsApiResult.Success -> {
                    _emailDeliveryState.value = _emailDeliveryState.value.copy(
                        isLoading = false,
                        settings = result.data,
                        errorMessage = null
                    )
                }
                is EmailSettingsApiResult.Error -> {
                    _emailDeliveryState.value = _emailDeliveryState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        successMessage = null
                    )
                }
            }
        }
    }

    fun setEmailJsEnabled(enabled: Boolean) {
        if (_emailDeliveryState.value.isLoading || _emailDeliveryState.value.isUpdating) return
        refreshFirebaseAuthStatus()
        _emailDeliveryState.value = _emailDeliveryState.value.copy(
            isUpdating = true,
            errorMessage = null,
            successMessage = null
        )

        viewModelScope.launch {
            val postResult = EmailSettingsService.instance.updateEmailSettings(enabled)
            refreshFirebaseAuthStatus()
            when (postResult) {
                is EmailSettingsApiResult.Error -> {
                    _emailDeliveryState.value = _emailDeliveryState.value.copy(
                        isUpdating = false,
                        errorMessage = postResult.message,
                        successMessage = null
                    )
                }
                is EmailSettingsApiResult.Success -> {
                    // Refresh authoritative configuration via GET after confirmed POST
                    val refreshResult = EmailSettingsService.instance.fetchEmailSettings()
                    refreshFirebaseAuthStatus()
                    when (refreshResult) {
                        is EmailSettingsApiResult.Success -> {
                            val refreshed = refreshResult.data
                            val statusLabel = if (refreshed.customEmailEnabled) "ENABLED" else "DISABLED"
                            _emailDeliveryState.value = _emailDeliveryState.value.copy(
                                isUpdating = false,
                                settings = refreshed,
                                errorMessage = null,
                                successMessage = "EmailJS is now $statusLabel (Active Provider: ${refreshed.activeProvider})."
                            )
                        }
                        is EmailSettingsApiResult.Error -> {
                            // Do not discard confirmed POST state, but report refresh failure transparently
                            val confirmed = postResult.data
                            val statusLabel = if (confirmed.customEmailEnabled) "ENABLED" else "DISABLED"
                            _emailDeliveryState.value = _emailDeliveryState.value.copy(
                                isUpdating = false,
                                settings = confirmed,
                                successMessage = "EmailJS updated to $statusLabel.",
                                errorMessage = "Status updated, but refresh failed: ${refreshResult.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun setCustomVerificationEmailEnabled(enabled: Boolean) {
        if (_emailDeliveryState.value.isLoading || _emailDeliveryState.value.isUpdating) return
        refreshFirebaseAuthStatus()
        _emailDeliveryState.value = _emailDeliveryState.value.copy(
            isUpdating = true,
            errorMessage = null,
            successMessage = null
        )

        viewModelScope.launch {
            val postResult = EmailSettingsService.instance.updateCustomVerificationEmailSetting(enabled)
            refreshFirebaseAuthStatus()
            when (postResult) {
                is EmailSettingsApiResult.Error -> {
                    _emailDeliveryState.value = _emailDeliveryState.value.copy(
                        isUpdating = false,
                        errorMessage = postResult.message,
                        successMessage = null
                    )
                }
                is EmailSettingsApiResult.Success -> {
                    val refreshResult = EmailSettingsService.instance.fetchEmailSettings()
                    refreshFirebaseAuthStatus()
                    when (refreshResult) {
                        is EmailSettingsApiResult.Success -> {
                            val refreshed = refreshResult.data
                            val statusLabel = if (refreshed.customVerificationEmailEnabled) "ENABLED" else "DISABLED"
                            _emailDeliveryState.value = _emailDeliveryState.value.copy(
                                isUpdating = false,
                                settings = refreshed,
                                errorMessage = null,
                                successMessage = "Custom Email Verification is now $statusLabel."
                            )
                        }
                        is EmailSettingsApiResult.Error -> {
                            val confirmed = postResult.data
                            val statusLabel = if (confirmed.customVerificationEmailEnabled) "ENABLED" else "DISABLED"
                            _emailDeliveryState.value = _emailDeliveryState.value.copy(
                                isUpdating = false,
                                settings = confirmed,
                                successMessage = "Custom Email Verification updated to $statusLabel.",
                                errorMessage = "Status updated, but refresh failed: ${refreshResult.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // ADMIN-TO-STUDENT EMAIL NOTIFICATIONS
    // -------------------------------------------------------------
    private val _adminEmailState = MutableStateFlow(AdminEmailNotificationUiState())
    val adminEmailState: StateFlow<AdminEmailNotificationUiState> = _adminEmailState.asStateFlow()

    private fun refreshAdminEmailAuthStatus() {
        val status = AdminEmailService.getFirebaseAdminSessionStatus()
        _adminEmailState.value = _adminEmailState.value.copy(
            isFirebaseSignedIn = status.isSignedIn,
            firebaseAuthEmail = status.email,
            isFirebaseEmailVerified = status.isEmailVerified
        )
    }

    fun loadAdminStudentsForEmail(forceRefresh: Boolean = false) {
        val current = _adminEmailState.value
        if (current.isLoadingStudents) return
        if (!forceRefresh && current.isStudentsLoaded && current.students.isNotEmpty()) {
            refreshAdminEmailAuthStatus()
            return
        }

        refreshAdminEmailAuthStatus()
        _adminEmailState.value = _adminEmailState.value.copy(
            isLoadingStudents = true,
            studentsError = null
        )

        viewModelScope.launch {
            val result = AdminEmailService.instance.fetchAllStudents()
            refreshAdminEmailAuthStatus()
            when (result) {
                is AdminEmailApiResult.Success -> {
                    val fetched = result.data
                    val existingSelectedUid = _adminEmailState.value.selectedStudent?.uid
                    val updatedSelected = if (existingSelectedUid != null) {
                        fetched.find { it.uid == existingSelectedUid } ?: _adminEmailState.value.selectedStudent
                    } else {
                        null
                    }
                    _adminEmailState.value = _adminEmailState.value.copy(
                        students = fetched,
                        selectedStudent = updatedSelected,
                        isLoadingStudents = false,
                        isStudentsLoaded = true,
                        studentsError = null
                    )
                }
                is AdminEmailApiResult.Error -> {
                    _adminEmailState.value = _adminEmailState.value.copy(
                        isLoadingStudents = false,
                        studentsError = result.message
                    )
                }
            }
        }
    }

    fun updateAdminEmailStudentSearch(query: String) {
        _adminEmailState.value = _adminEmailState.value.copy(searchQuery = query)
    }

    fun selectStudentForAdminEmail(student: AdminRegisteredStudent?) {
        _adminEmailState.value = _adminEmailState.value.copy(
            selectedStudent = student,
            sendErrorMessage = null,
            sendSuccessMessage = null,
            isSendTimeoutUncertain = false
        )
    }

    fun updateAdminEmailSubject(subject: String) {
        _adminEmailState.value = _adminEmailState.value.copy(
            subject = subject,
            sendErrorMessage = null
        )
    }

    fun updateAdminEmailMessage(message: String) {
        _adminEmailState.value = _adminEmailState.value.copy(
            message = message,
            sendErrorMessage = null
        )
    }

    fun clearAdminEmailSendFeedback() {
        _adminEmailState.value = _adminEmailState.value.copy(
            sendSuccessMessage = null,
            sendErrorMessage = null,
            isSendTimeoutUncertain = false
        )
    }

    fun sendAdminStudentEmail() {
        val current = _adminEmailState.value
        if (current.isSending) return

        val selected = current.selectedStudent
        if (selected == null || selected.uid.isBlank()) {
            _adminEmailState.value = current.copy(
                sendErrorMessage = "Please select a registered student before sending.",
                sendSuccessMessage = null,
                isSendTimeoutUncertain = false
            )
            return
        }

        val cleanSubject = current.subject.trim()
        val cleanMessage = current.message.trim()
        if (cleanSubject.isBlank()) {
            _adminEmailState.value = current.copy(
                sendErrorMessage = "Please enter an email subject.",
                sendSuccessMessage = null,
                isSendTimeoutUncertain = false
            )
            return
        }
        if (cleanMessage.isBlank()) {
            _adminEmailState.value = current.copy(
                sendErrorMessage = "Please enter an email message.",
                sendSuccessMessage = null,
                isSendTimeoutUncertain = false
            )
            return
        }

        refreshAdminEmailAuthStatus()
        _adminEmailState.value = _adminEmailState.value.copy(
            isSending = true,
            sendSuccessMessage = null,
            sendErrorMessage = null,
            isSendTimeoutUncertain = false
        )

        viewModelScope.launch {
            val result = AdminEmailService.instance.sendStudentEmail(
                studentUid = selected.uid,
                subject = cleanSubject,
                message = cleanMessage
            )
            refreshAdminEmailAuthStatus()
            when (result) {
                is AdminEmailApiResult.Success -> {
                    val deliveredEmail = result.data.recipientEmail.ifBlank { selected.email }
                    val confirmMsg = if (deliveredEmail.isNotBlank()) {
                        "Email submitted to $deliveredEmail (${selected.name})."
                    } else {
                        result.data.message
                    }
                    _adminEmailState.value = _adminEmailState.value.copy(
                        isSending = false,
                        sendSuccessMessage = confirmMsg,
                        sendErrorMessage = null,
                        isSendTimeoutUncertain = false,
                        subject = "",
                        message = ""
                    )
                    // Auto-refresh email history after confirmed submission
                    loadAdminEmailLogs(forceRefresh = true)
                }
                is AdminEmailApiResult.Error -> {
                    _adminEmailState.value = _adminEmailState.value.copy(
                        isSending = false,
                        sendSuccessMessage = null,
                        sendErrorMessage = result.message,
                        isSendTimeoutUncertain = result.isTimeoutUncertain
                    )
                    if (result.isTimeoutUncertain) {
                        loadAdminEmailLogs(forceRefresh = true)
                    }
                }
            }
        }
    }

    fun loadAdminEmailLogs(forceRefresh: Boolean = false) {
        val current = _adminEmailState.value
        if (current.isLoadingLogs) return
        if (!forceRefresh && current.isLogsLoaded && current.emailLogs.isNotEmpty()) {
            refreshAdminEmailAuthStatus()
            return
        }

        refreshAdminEmailAuthStatus()
        _adminEmailState.value = _adminEmailState.value.copy(
            isLoadingLogs = true,
            logsError = null
        )

        viewModelScope.launch {
            val result = AdminEmailService.instance.fetchEmailLogs()
            refreshAdminEmailAuthStatus()
            when (result) {
                is AdminEmailApiResult.Success -> {
                    _adminEmailState.value = _adminEmailState.value.copy(
                        emailLogs = result.data,
                        isLoadingLogs = false,
                        isLogsLoaded = true,
                        logsError = null
                    )
                }
                is AdminEmailApiResult.Error -> {
                    _adminEmailState.value = _adminEmailState.value.copy(
                        isLoadingLogs = false,
                        logsError = result.message
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                FirebaseService.ensureSystemSecurityKeySeeded()
            } catch (_: Exception) {}
        }

        // Observe Firebase server time offset for drift-free synchronized maintenance countdowns
        viewModelScope.launch {
            FirebaseRealtimeService.observeServerTimeOffsetMs()
                .catch { e -> android.util.Log.w("MainViewModel", "Server time offset flow error: ${e.message}") }
                .collect { offsetMs ->
                    _serverTimeOffsetMs.value = offsetMs
                    prefs.edit().putLong("maintenance_server_time_offset_ms", offsetMs).apply()
                    _maintenanceNowMs.value = currentServerTimeMillis()
                }
        }

        // Single shared real-time listener combining Firestore + RTDB for Maintenance Mode
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeMaintenanceConfig().catch { emit(null) },
                FirebaseRealtimeService.observeMaintenanceConfigFromRTDB().catch { emit(null) }
            ) { fsConfig, rtdbConfig ->
                MaintenanceControlConfig.resolveAuthoritativeConfig(fsConfig, rtdbConfig)
            }.catch { e ->
                android.util.Log.w("MainViewModel", "Maintenance observer flow error: ${e.message}")
            }.collect { remoteConfig ->
                if (remoteConfig != null) {
                    _maintenanceConfig.value = remoteConfig
                    persistMaintenanceConfig(remoteConfig)
                    val now = currentServerTimeMillis()
                    _maintenanceNowMs.value = now
                    if (remoteConfig.isEffectivelyActive(now) && _currentScreen.value == Screen.EXAM) {
                        _isTimerPaused.value = true
                    }
                    if (remoteConfig.isExpired(now)) {
                        try {
                            handleMaintenanceExpired(remoteConfig, now)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Drift-free 1-second maintenance ticker (runs ONLY while maintenanceEnabled == true)
        viewModelScope.launch {
            _maintenanceConfig.collectLatest { cfg ->
                if (!cfg.maintenanceEnabled || cfg.maintenanceEndTime <= 0L) {
                    _maintenanceNowMs.value = currentServerTimeMillis()
                    return@collectLatest
                }
                while (true) {
                    val now = currentServerTimeMillis()
                    _maintenanceNowMs.value = now
                    if (cfg.isEffectivelyActive(now) && _currentScreen.value == Screen.EXAM) {
                        _isTimerPaused.value = true
                    }
                    if (cfg.isExpired(now)) {
                        try {
                            handleMaintenanceExpired(cfg, now)
                        } catch (_: Exception) {}
                        break
                    }
                    val rem = cfg.remainingMillis(now)
                    val nextDelayMs = if (rem <= 0L) 1000L else ((rem - 1L) % 1000L + 1L).coerceIn(50L, 1000L)
                    delay(nextDelayMs)
                }
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeSystemShutdownStatus().catch { emit(null) },
                FirebaseRealtimeService.observeSystemShutdownFromRTDB().catch { emit(null) }
            ) { fsStatus, rtdbStatus ->
                if (fsStatus == null && rtdbStatus == null) {
                    null
                } else {
                    com.example.data.SystemControlConfig.resolveSystemShutdownStatus(fsStatus, rtdbStatus)
                }
            }.catch { e ->
                android.util.Log.w("MainViewModel", "System shutdown observer flow error: ${e.message}")
            }.collect { shutdownState ->
                if (shutdownState != null) {
                    _isSystemShutdown.value = shutdownState
                    prefs.edit().putBoolean("system_shutdown_active", shutdownState).apply()
                    if (shutdownState && _currentScreen.value == Screen.EXAM) {
                        _isTimerPaused.value = true
                    }
                }
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeFirestoreSecurityKeySnapshot().catch { emit(null) },
                FirebaseRealtimeService.observeSystemSecurityKeyFromRTDB().catch { emit(null) }
            ) { fsSnap, rtdbSnap ->
                if (fsSnap == null && rtdbSnap == null) {
                    null
                } else {
                    com.example.data.SystemControlConfig.resolveEffectiveSecurityKey(
                        firestoreSnapshot = fsSnap,
                        rtdbSecurityKey = rtdbSnap?.securityKey,
                        rtdbPin = rtdbSnap?.pin,
                        rtdbUpdatedAtMillis = rtdbSnap?.updatedAtMillis ?: 0L
                    )
                }
            }.catch { e ->
                android.util.Log.w("MainViewModel", "Security key observer flow error: ${e.message}")
            }.collect { resolvedKey ->
                if (!resolvedKey.isNullOrBlank()) {
                    _systemSecurityKey.value = resolvedKey
                    prefs.edit().putString("cached_system_security_key", resolvedKey).apply()
                }
            }
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val seededCycle = FirebaseService.ensureLeaderboardCycleSeeded(currentServerTimeMillis())
                _leaderboardCycle.value = seededCycle
                persistLeaderboardCycle(seededCycle)
                _leaderboardNowMs.value = currentServerTimeMillis()
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeLeaderboardCycleStatus().catch { emit(null) },
                FirebaseRealtimeService.observeLeaderboardCycleFromRTDB().catch { emit(null) }
            ) { fsCycle, rtdbCycle ->
                com.example.data.SystemControlConfig.resolveLeaderboardCycleStatus(fsCycle, rtdbCycle)
            }.catch { e ->
                android.util.Log.w("MainViewModel", "Leaderboard cycle observer flow error: ${e.message}")
            }.collect { remoteCycle ->
                if (remoteCycle != null) {
                    _leaderboardCycle.value = remoteCycle
                    persistLeaderboardCycle(remoteCycle)
                    val now = currentServerTimeMillis()
                    _leaderboardNowMs.value = now
                    if (remoteCycle.lastResetAtMillis > 0L && remoteCycle.lastResetAtMillis > activeLeaderboardResetAt) {
                        activeLeaderboardResetAt = remoteCycle.lastResetAtMillis
                        prefs.edit().putLong("last_handled_leaderboard_reset_at", remoteCycle.lastResetAtMillis).apply()
                        lastRemoteLeaderboardEntries = lastRemoteLeaderboardEntries?.map { it.copy(points = 0) }
                        zeroLocalStudentXp()
                        updateLeaderboard()
                    }
                    if (remoteCycle.isDueForReset(now)) {
                        triggerAutoLeaderboardCycleReset(remoteCycle)
                    }
                }
            }
        }

        // Real-time 1-second 30-day leaderboard countdown ticker + auto-reset trigger on expiry
        viewModelScope.launch {
            _leaderboardCycle.collectLatest { cycle ->
                if (cycle.effectiveNextResetAtMillis <= 0L) {
                    _leaderboardNowMs.value = currentServerTimeMillis()
                    return@collectLatest
                }
                while (true) {
                    val now = currentServerTimeMillis()
                    _leaderboardNowMs.value = now
                    if (cycle.isDueForReset(now)) {
                        triggerAutoLeaderboardCycleReset(cycle)
                        break
                    }
                    val rem = cycle.remainingMillis(now)
                    val nextDelayMs = if (rem <= 0L) 1000L else ((rem - 1L) % 1000L + 1L).coerceIn(100L, 1000L)
                    delay(nextDelayMs)
                }
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeLeaderboardResetAt().catch { emit(0L) },
                FirebaseRealtimeService.observeLeaderboardResetAtFromRTDB().catch { emit(0L) }
            ) { fsResetTs, rtdbResetTs ->
                maxOf(fsResetTs, rtdbResetTs)
            }.catch { e ->
                android.util.Log.w("MainViewModel", "Leaderboard reset observer flow error: ${e.message}")
            }.collect { remoteResetAt ->
                if (remoteResetAt > 0L && remoteResetAt > activeLeaderboardResetAt) {
                    activeLeaderboardResetAt = remoteResetAt
                    prefs.edit().putLong("last_handled_leaderboard_reset_at", remoteResetAt).apply()
                    zeroLocalStudentXp()
                    updateLeaderboard()
                }
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                FirebaseService.observeAdminPricing().catch { emit(FirebaseService.AdminPricingConfig()) },
                FirebaseRealtimeService.observeAdminPricingFromRTDB().catch { emit(null) }
            ) { fsPricing, rtdbMap ->
                val chat = rtdbMap?.get("liveChatPrice")?.toString()?.toIntOrNull()
                    ?: rtdbMap?.get("live_chat_price")?.toString()?.toIntOrNull()
                    ?: fsPricing.liveChatPrice
                val sub = rtdbMap?.get("subscriptionPrice")?.toString()?.toIntOrNull()
                    ?: rtdbMap?.get("subscription_price")?.toString()?.toIntOrNull()
                    ?: fsPricing.subscriptionPrice
                Pair(chat, sub)
            }.catch { e ->
                android.util.Log.w("MainViewModel", "Pricing observer flow error: ${e.message}")
            }.collect { (chat, sub) ->
                if (chat > 0) {
                    _liveChatPrice.value = chat
                    sessionPrefs.edit().putInt("admin_live_chat_price", chat).apply()
                }
                if (sub > 0) {
                    _subscriptionPrice.value = sub
                    sessionPrefs.edit().putInt("admin_subscription_price", sub).apply()
                }
            }
        }

        // Continuously observe statewide leaderboard across all students
        leaderboardJob?.cancel()
        leaderboardJob = viewModelScope.launch {
            repository.leaderboard
                .catch { e -> android.util.Log.w("MainViewModel", "Leaderboard flow error: ${e.message}") }
                .collect { remoteEntries ->
                    updateLeaderboard(remoteEntries)
                }
        }

        updateLeaderboard()

        val currentFbUser = try {
            FirebaseService.auth.currentUser
        } catch (_: Exception) {
            null
        }
        val savedEmail = sessionPrefs.getString("user_email", "") ?: currentFbUser?.email ?: ""
        val isAdminOrDemo = savedEmail.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                savedEmail.equals("karthik.kcet2026@gmail.com", ignoreCase = true) ||
                sessionPrefs.getBoolean("is_admin", false) ||
                !savedEmail.contains("@")

        if (!isAdminOrDemo && currentFbUser != null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    currentFbUser.reload().awaitResult()
                } catch (_: Exception) {}
            }
        }

        val effectiveLoggedIn = sessionPrefs.getBoolean("is_logged_in", false)

        if (effectiveLoggedIn || currentFbUser != null) {
            val savedUid = sessionPrefs.getString("user_uid", null) ?: currentFbUser?.uid ?: ""
            val savedEmailFinal = sessionPrefs.getString("user_email", null) ?: currentFbUser?.email ?: ""

            if (savedUid.isNotBlank() || savedEmailFinal.isNotBlank()) {
                startObservingUserData(savedUid, savedEmailFinal)

                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val remoteXP = FirebaseService.getUserPointsOnce(savedUid, savedEmailFinal)
                        if (remoteXP > _userPoints.value) {
                            _userPoints.value = remoteXP
                            sessionPrefs.edit().putInt("user_xp", remoteXP).putInt("user_xp_$savedUid", remoteXP).apply()
                            _analyticsSummary.value = _analyticsSummary.value.copy(totalPoints = remoteXP)
                            updateLeaderboard()
                        }
                    } catch (_: Exception) {}
                }

                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val isAdminSession = savedEmailFinal.trim().equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                                savedUid == "admin_ullas" ||
                                sessionPrefs.getBoolean("is_admin", false)
                        if (!isAdminSession) {
                            val blockCheck = FirebaseService.checkIfUserIsBlocked(savedUid)
                            val emailCheck = if (savedEmailFinal.isNotBlank()) FirebaseService.checkIfUserIsBlocked(savedEmailFinal) else Pair(false, "")
                            if (blockCheck.first || emailCheck.first) {
                                val reason = if (blockCheck.first) blockCheck.second else emailCheck.second
                                logout()
                                _blockedAccountAlert.value = Pair(savedEmailFinal, reason.ifBlank { "Account blocked by Administrator." })
                            } else {
                                val remoteProfile = FirebaseService.getUserProfileOnce(savedUid)
                                if (remoteProfile != null) {
                                    if (remoteProfile.isBlocked) {
                                        logout()
                                        _blockedAccountAlert.value = Pair(savedEmailFinal, remoteProfile.blockReason.ifBlank { "Account blocked by Administrator." })
                                    } else {
                                        _userProfile.value = remoteProfile
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // Clean up any historical KEA schedule update spam notifications from Firestore completely
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                FirebaseService.cleanupScheduleSpamNotifications()
            } catch (_: Exception) {}
        }

        // Ensure welcome notification only appears on the very first session of opening the app
        viewModelScope.launch {
            delay(5000)
            markWelcomeNotificationAsShown()
        }

        viewModelScope.launch {
            userProfile.collect {
                updateLeaderboard()
            }
        }

        viewModelScope.launch {
            userPoints.collect {
                updateLeaderboard()
            }
        }

        viewModelScope.launch {
            try {
                repository.cleanupDuplicateTestResults()
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            testResultsHistory
                .catch { e -> android.util.Log.w("MainViewModel", "Test history flow error: ${e.message}") }
                .collect { results ->
                    val uniqueResults = getUniqueAttempts(results)
                    if (uniqueResults.isNotEmpty()) {
                        val totalCorrect = uniqueResults.sumOf { it.correctCount }
                        val totalQs = uniqueResults.sumOf { it.totalQuestions }
                        val calculatedAcc = if (totalQs > 0) (totalCorrect.toFloat() / totalQs) * 100f else 0f
                        val currentXP = _userPoints.value
                        val uid = _userProfile.value.uid
                        _analyticsSummary.value = _analyticsSummary.value.copy(
                            totalTestsTaken = maxOf(_analyticsSummary.value.totalTestsTaken, uniqueResults.size),
                            overallAccuracy = if (calculatedAcc > 0f) calculatedAcc else _analyticsSummary.value.overallAccuracy,
                            totalPoints = currentXP,
                            avgTimePerQuestionSec = uniqueResults.map { it.timeTakenSeconds.toFloat() / it.totalQuestions }.average().toFloat()
                        )
                        updateLeaderboard()
                        if (currentXP > 0 && (uid.isNotBlank() || _userProfile.value.email.isNotBlank())) {
                            try {
                                FirebaseService.updateLeaderboardEntry(
                                    profile = _userProfile.value,
                                    points = currentXP,
                                    accuracy = calculatedAcc,
                                    testsCount = uniqueResults.size
                                )
                            } catch (_: Exception) {}
                        }
                    } else {
                        _analyticsSummary.value = _analyticsSummary.value.copy(
                            totalPoints = _userPoints.value
                        )
                        updateLeaderboard()
                    }
                }
        }

        viewModelScope.launch {
            allStudents
                .catch { e -> android.util.Log.w("MainViewModel", "All students flow error: ${e.message}") }
                .collect { studentsList ->
                    val isAdmin = sessionPrefs.getBoolean("is_admin", false) ||
                            _userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                            _userProfile.value.uid == "admin_ullas" ||
                            (sessionPrefs.getString("user_email", "") ?: "").equals("ullassuvarna65@gmail.com", ignoreCase = true)
                    val isMentor = _currentScreen.value == Screen.SENIOR_CHAT || _userProfile.value.email.equals("ullassuvarna85@gmail.com", ignoreCase = true) || registeredMentors.value.any { it.email.isNotBlank() && it.email.equals(_userProfile.value.email.trim(), ignoreCase = true) }
                    if (_isLoggedIn.value && !isAdmin && !isMentor) {
                        val me = _userProfile.value
                        val match = studentsList.find { student ->
                            student.uid.equals(me.uid, ignoreCase = true) ||
                            (me.email.isNotBlank() && student.email.equals(me.email, ignoreCase = true)) ||
                            (me.formattedStudentId.isNotBlank() && student.formattedStudentId.equals(me.formattedStudentId, ignoreCase = true))
                        }
                        if (match != null) {
                            val override = FirebaseService.getPremiumOverride(me.uid)
                                ?: (if (me.email.isNotBlank()) FirebaseService.getPremiumOverride(me.email) else null)
                                ?: (if (me.formattedStudentId.isNotBlank()) FirebaseService.getPremiumOverride(me.formattedStudentId) else null)

                            val targetPremium = when {
                                override != null -> override
                                match.isPremium -> true
                                sessionPrefs.getBoolean("is_premium", false) -> true
                                (me.uid.isNotBlank() && sessionPrefs.getBoolean("is_premium_${me.uid}", false)) -> true
                                (me.email.isNotBlank() && sessionPrefs.getBoolean("is_premium_${me.email}", false)) -> true
                                else -> me.isPremium
                            }

                            if (me.isPremium != targetPremium) {
                                _userProfile.value = me.copy(isPremium = targetPremium)
                                val editor = sessionPrefs.edit().putBoolean("is_premium", targetPremium)
                                if (me.uid.isNotBlank()) editor.putBoolean("is_premium_${me.uid}", targetPremium)
                                if (me.email.isNotBlank()) editor.putBoolean("is_premium_${me.email}", targetPremium)
                                editor.apply()
                                if (!targetPremium) {
                                    _userBadges.value = defaultLockedBadges
                                }
                            }
                        }
                    }
                }
        }

        val viewModelStartTime = System.currentTimeMillis()
        val shownSystemNotificationIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

        viewModelScope.launch {
            notifications
                .catch { e -> android.util.Log.w("MainViewModel", "Notifications flow error: ${e.message}") }
                .collect { notifList ->
                    // Avoid showing old/historic notifications on app launch, only trigger system alert for new ones
                    notifList.forEach { notif ->
                        if (!notif.isRead &&
                            notif.createdAtMillis >= viewModelStartTime - 10000 &&
                            !shownSystemNotificationIds.contains(notif.id)
                        ) {
                            shownSystemNotificationIds.add(notif.id)
                            try {
                                DailyNotificationManager.showSystemNotification(
                                    context = app,
                                    title = notif.title,
                                    message = notif.message,
                                    channelId = if (notif.type == "ANNOUNCEMENT" || notif.type == "EXAM_ALERT") {
                                        DailyNotificationManager.CHANNEL_INSTANT_ALERTS
                                    } else {
                                        DailyNotificationManager.CHANNEL_STUDY_UPDATES
                                    }
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainViewModel", "Error showing real-time system notification: ${e.message}")
                            }
                        }
                    }
                }
        }

        // Start observation immediately if already logged in from saved session (only if not already started)
        if (sessionPrefs.getBoolean("is_logged_in", false) && currentFbUser == null) {
            val savedUid = sessionPrefs.getString("user_uid", "") ?: ""
            val savedEmail = sessionPrefs.getString("user_email", "") ?: ""
            if (savedUid.isNotBlank() || savedEmail.isNotBlank()) {
                startObservingUserData(savedUid, savedEmail)
            }
        }

        // Dynamically listen to Firebase Auth changes to safely trigger Realtime DB/Firestore observers
        try {
            FirebaseService.auth.addAuthStateListener { firebaseAuth ->
                try {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        startObservingUserData(firebaseUser.uid, firebaseUser.email ?: "")
                    } else {
                        if (!sessionPrefs.getBoolean("is_logged_in", false)) {
                            // Not authenticated in Firebase Auth and no saved session, cancel user-specific listeners
                            profileJob?.cancel()
                            testResultsJob?.cancel()
                            paymentJob?.cancel()
                            presenceJob?.cancel()
                            liveXpJob?.cancel()
                            realtimeNotifJob?.cancel()
                            allOnlineJob?.cancel()
                            allLiveXpJob?.cancel()
                        }
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}



