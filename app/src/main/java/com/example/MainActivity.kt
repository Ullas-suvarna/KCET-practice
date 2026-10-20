package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import com.example.ui.components.WhiteLoadingScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.BadgeRewardCongratsDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ExamGuidelinesDialog
import com.example.ui.components.NotificationDashboardDialog
import com.example.ui.components.PremiumLockDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AdminPanelState
import com.example.ui.screens.AdminPanelActions
import com.example.ui.screens.AiCollegeRecommendationScreen
import com.example.ui.screens.AiDoubtSolverScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BranchExplorerScreen
import com.example.ui.screens.CounsellingScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.SyllabusVideosScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SavedBookmarksScreen
import com.example.ui.screens.SeniorLiveChatScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.DeveloperIntroScreen
import com.example.ui.theme.KcetPracticeTheme
import com.example.viewmodel.BottomTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.components.SidebarNavigationDrawer
import com.example.ui.screens.NotificationDashboardScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Android System Push Notification channels & schedule daily study alarms safely
        try {
            com.example.notification.DailyNotificationManager.createNotificationChannels(this)
            com.example.notification.DailyNotificationManager.scheduleDailyDigest(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize notifications/alarms: ${e.message}", e)
        }

        // Request POST_NOTIFICATIONS runtime permission for Android 13+ safely
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to request notification permission: ${e.message}", e)
        }

        setContent {
            KcetAppMain()
        }
    }
}

@Composable
fun KcetAppMain(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val latestResult by viewModel.latestResult.collectAsStateWithLifecycle()
    val questionsList by viewModel.questionsList.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val importantDates by viewModel.importantDates.collectAsStateWithLifecycle()
    val rankVsMarks by viewModel.rankVsMarks.collectAsStateWithLifecycle()
    val testHistory by viewModel.testResultsHistory.collectAsStateWithLifecycle()
    val showPremiumDialog by viewModel.showPremiumDialog.collectAsStateWithLifecycle()
    val seniorChatBookings by viewModel.seniorChatBookings.collectAsStateWithLifecycle()
    val mySeniorChatBookings by viewModel.mySeniorChatBookings.collectAsStateWithLifecycle()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsStateWithLifecycle()
    val helplines by viewModel.helplines.collectAsStateWithLifecycle()
    val paymentReceipts by viewModel.paymentReceipts.collectAsStateWithLifecycle()
    val currentUserPaymentReceipt by viewModel.currentUserPaymentReceipt.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }
    val hasSeenDeveloperIntro by viewModel.hasSeenDeveloperIntro.collectAsStateWithLifecycle()
    val blockedAlert by viewModel.blockedAccountAlert.collectAsStateWithLifecycle()
    val canNavigateBack by viewModel.canNavigateBack.collectAsStateWithLifecycle()
    val isSubmittingExam by viewModel.isSubmittingExam.collectAsStateWithLifecycle()

    var selectedChallengeOpponent by remember { androidx.compose.runtime.mutableStateOf<com.example.data.LeaderboardEntry?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val bookmarkedQuestionIds = remember(bookmarkedQuestions) { bookmarkedQuestions.map { it.questionId }.toSet() }

    val isAtRootHome = (currentScreen == Screen.MAIN && selectedTab == BottomTab.HOME)
    val isSystemBackExcluded = currentScreen == Screen.AUTH || currentScreen == Screen.SPLASH || currentScreen == Screen.EXAM || currentScreen == Screen.DEV_INTRO || currentScreen == Screen.ADMIN || currentScreen == Screen.SENIOR_CHAT
    val shouldHandleSystemBack = !isAtRootHome && !isSystemBackExcluded

    // System Back Handler:
    // 1. If Navigation Drawer is open, Back closes the drawer first
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // 2. If user is on any other screen/tab, Back navigates back to previous screen or shifts to Home page; only exiting on Home page
    BackHandler(enabled = shouldHandleSystemBack && !drawerState.isOpen) {
        viewModel.navigateBack()
    }


    // Gamification & Analytics States
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val userBadges by viewModel.userBadges.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val analyticsSummary by viewModel.analyticsSummary.collectAsStateWithLifecycle()
    val showBadgeCongratsDialog by viewModel.showBadgeCongratsDialog.collectAsStateWithLifecycle()
    val userRank = leaderboard.find { it.isCurrentUser }?.rank ?: 5

    val totalTestSeconds = remember(testHistory) { testHistory.sumOf { it.timeTakenSeconds } }
    val testTimeDisplay = remember(totalTestSeconds) {
        val sec = if (totalTestSeconds == 0L) 1800L else totalTestSeconds
        if (sec < 3600L) {
            "${sec / 60} mins"
        } else {
            String.format(java.util.Locale.US, "%.1f hrs", sec / 3600.0f)
        }
    }

    // Exam States
    val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerPaused by viewModel.isTimerPaused.collectAsStateWithLifecycle()
    val activeResult by viewModel.activeTestResult.collectAsStateWithLifecycle()
    val testSeriesSets by viewModel.testSeriesSets.collectAsStateWithLifecycle()
    val activeQuestionsList by viewModel.activeQuestionsList.collectAsStateWithLifecycle()
    val pendingExamInfo by viewModel.pendingExamInfo.collectAsStateWithLifecycle()

    // Daily AI Practice & Full Mock States
    val dailyPracticeQuestions by viewModel.dailyPracticeQuestions.collectAsStateWithLifecycle()
    val dailyFullMockQuestions by viewModel.dailyFullMockQuestions.collectAsStateWithLifecycle()
    val dailyWindowInfo by viewModel.dailyWindowInfo.collectAsStateWithLifecycle()
    val isDailyQuestionsLoading by viewModel.isDailyQuestionsLoading.collectAsStateWithLifecycle()
    val isDailyMockLoading by viewModel.isDailyMockLoading.collectAsStateWithLifecycle()

    // Admin Adjustable Pricing
    val liveChatPrice by viewModel.liveChatPrice.collectAsStateWithLifecycle()
    val subscriptionPrice by viewModel.subscriptionPrice.collectAsStateWithLifecycle()

    // Active Exam Screen Setup
    val activity = LocalContext.current as? android.app.Activity

    // Cold-start / App-launch white loading transition when launching directly to home/dashboard
    val isDirectLandingOnApp = remember {
        currentScreen == Screen.MAIN || currentScreen == Screen.ADMIN || currentScreen == Screen.SENIOR_CHAT
    }
    var isInitialAppLoading by remember { mutableStateOf(isDirectLandingOnApp) }

    LaunchedEffect(Unit) {
        if (isInitialAppLoading) {
            delay(1000)
            isInitialAppLoading = false
        }
    }

    KcetPracticeTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDarkMode) MaterialTheme.colorScheme.background else Color.White
        ) {
            if (isInitialAppLoading && (currentScreen == Screen.MAIN || currentScreen == Screen.ADMIN || currentScreen == Screen.SENIOR_CHAT)) {
                WhiteLoadingScreen(
                    message = when (currentScreen) {
                        Screen.ADMIN -> "Loading Admin Dashboard..."
                        Screen.SENIOR_CHAT -> "Loading Senior Mentorship..."
                        else -> "Loading Home Page..."
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) MaterialTheme.colorScheme.background else Color.White)
                ) {
                    when (currentScreen) {
                Screen.SPLASH -> {
                    SplashScreen(
                        isLoggedIn = isLoggedIn,
                        onNavigateNext = { loggedIn ->
                            if (!hasSeenDeveloperIntro) {
                                viewModel.resetNavigation(Screen.DEV_INTRO)
                            } else if (loggedIn) {
                                val isAdmin = viewModel.userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                                        viewModel.userProfile.value.uid == "admin_ullas" ||
                                        viewModel.currentScreen.value == Screen.ADMIN
                                val isMentor = viewModel.userProfile.value.email.equals("ullassuvarna85@gmail.com", ignoreCase = true) ||
                                        viewModel.currentScreen.value == Screen.SENIOR_CHAT
                                when {
                                    isAdmin -> viewModel.resetNavigation(Screen.ADMIN)
                                    isMentor -> viewModel.resetNavigation(Screen.SENIOR_CHAT)
                                    else -> viewModel.resetNavigation(Screen.MAIN)
                                }
                            } else {
                                viewModel.resetNavigation(Screen.ONBOARDING)
                            }
                        }
                    )
                }

                Screen.DEV_INTRO -> {
                    DeveloperIntroScreen(
                        onFinishIntro = {
                            viewModel.markDeveloperIntroSeen()
                            if (isLoggedIn) {
                                val isAdmin = viewModel.userProfile.value.email.equals("ullassuvarna65@gmail.com", ignoreCase = true) ||
                                        viewModel.userProfile.value.uid == "admin_ullas" ||
                                        viewModel.currentScreen.value == Screen.ADMIN
                                val isMentor = viewModel.userProfile.value.email.equals("ullassuvarna85@gmail.com", ignoreCase = true) ||
                                        viewModel.currentScreen.value == Screen.SENIOR_CHAT
                                when {
                                    isAdmin -> viewModel.resetNavigation(Screen.ADMIN)
                                    isMentor -> viewModel.resetNavigation(Screen.SENIOR_CHAT)
                                    else -> viewModel.resetNavigation(Screen.MAIN)
                                }
                            } else {
                                viewModel.resetNavigation(Screen.ONBOARDING)
                            }
                        }
                    )
                }

                Screen.ONBOARDING -> {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            viewModel.resetNavigation(Screen.AUTH)
                        }
                    )
                }

                Screen.AUTH -> {
                    AuthScreen(
                        onLoginSuccess = { email, name, method ->
                            viewModel.login(email, name, method)
                        },
                        onAdminLoginSuccess = {
                            viewModel.loginAdmin()
                        },
                        blockedAlert = blockedAlert,
                        onDismissBlockedAlert = {
                            viewModel.dismissBlockedAccountAlert()
                        }
                    )
                }

                Screen.MAIN -> {
                    SidebarNavigationDrawer(
                        drawerState = drawerState,
                        userProfile = userProfile,
                        unreadNotificationCount = unreadNotifCount,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onNavigateNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                        onNavigateSeniorChat = { viewModel.navigateTo(Screen.SENIOR_CHAT) },
                        onNavigateAiRecommendation = { viewModel.navigateTo(Screen.AI_RECOMMENDATION) },
                        onNavigateBranchExplorer = { viewModel.navigateTo(Screen.BRANCH_EXPLORER) },
                        onNavigateBookmarks = { viewModel.navigateTo(Screen.BOOKMARKS) },
                        onNavigateChallenge = { viewModel.navigateTo(Screen.CHALLENGE) },
                        onNavigateTab = { tab -> viewModel.selectBottomTab(tab) },
                        onNavigateAdmin = { viewModel.navigateTo(Screen.ADMIN) },
                        onOpenPremiumDialog = { viewModel.triggerPremiumDialog(true) },
                        liveChatPrice = liveChatPrice
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
                            bottomBar = {
                                BottomNavBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { tab ->
                                        viewModel.selectBottomTab(tab)
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            ) {
                                when (selectedTab) {
                                    BottomTab.HOME -> HomeScreen(
                                        userProfile = userProfile,
                                        latestResult = latestResult,
                                        announcements = announcements,
                                        importantDates = importantDates,
                                        rankVsMarksList = rankVsMarks,
                                        userPoints = userPoints,
                                        userRank = userRank,
                                        testTimeDisplay = testTimeDisplay,
                                        badges = userBadges,
                                        bookmarkedCount = bookmarkedQuestions.size,
                                        unreadNotificationCount = unreadNotifCount,
                                        onStartFreeTest = { viewModel.startFreeTest() },
                                        onUpgradeClick = { viewModel.triggerPremiumDialog(true) },
                                        onNavigateTab = { tab -> viewModel.selectBottomTab(tab) },
                                        onOpenSeniorChat = { viewModel.navigateTo(Screen.SENIOR_CHAT) },
                                        onOpenBookmarks = { viewModel.navigateTo(Screen.BOOKMARKS) },
                                        onOpenNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        liveChatPrice = liveChatPrice,
                                        subscriptionPrice = subscriptionPrice
                                    )

                                BottomTab.PRACTICE -> PracticeScreen(
                                    isPremiumUser = userProfile.isPremium,
                                    testSeriesSets = testSeriesSets,
                                    bookmarkedCount = bookmarkedQuestions.size,
                                    dailyQuestionsCount = dailyPracticeQuestions.size.let { if (it > 0) it else 15 },
                                    dailyMockCount = dailyFullMockQuestions.size.let { if (it > 0) it else 60 },
                                    dailyCountdownStr = dailyWindowInfo?.formattedCountdown ?: "",
                                    isDailyQuestionsLoading = isDailyQuestionsLoading,
                                    isDailyMockLoading = isDailyMockLoading,
                                    onStartFreeTest = { viewModel.startFreeTest() },
                                    onStartTestSet = { set -> viewModel.startTestForSet(set) },
                                    onStartDailyPractice = { viewModel.startDailyPracticeTest() },
                                    onStartDailyFullMock = { viewModel.startDailyFullMockTest() },
                                    onRefreshDailyQuestions = { viewModel.loadDailyPracticeSet(forceRefresh = true) },
                                    onRefreshDailyMock = { viewModel.loadDailyFullMockTest(forceRefresh = true) },
                                    onLockedFeatureClick = { viewModel.triggerPremiumDialog(true) },
                                    onOpenBookmarks = { viewModel.navigateTo(Screen.BOOKMARKS) }
                                )

                                BottomTab.DOUBTS -> AiDoubtSolverScreen(userEmail = userProfile.email)

                                BottomTab.VIDEOS -> SyllabusVideosScreen()

                                BottomTab.RESOURCES -> ResourcesScreen(
                                    resources = resources
                                )

                                BottomTab.COUNSELLING -> {
                                    val isKeaSyncing by viewModel.isKeaSyncing.collectAsStateWithLifecycle()
                                    val keaLastSyncedDate by viewModel.keaLastSyncedDate.collectAsStateWithLifecycle()
                                    CounsellingScreen(
                                        resources = resources,
                                        importantDates = importantDates,
                                        isSyncing = isKeaSyncing,
                                        lastSyncedDateStr = keaLastSyncedDate,
                                        onSyncKeaSchedule = { viewModel.syncKeaScheduleWithGemini() },
                                        onOpenSeniorChat = { viewModel.navigateTo(Screen.SENIOR_CHAT) }
                                    )
                                }

                                BottomTab.PROFILE -> ProfileScreen(
                                    userProfile = userProfile,
                                    testResults = testHistory,
                                    userPoints = userPoints,
                                    leaderboardEntries = leaderboard,
                                    badges = userBadges,
                                    analyticsSummary = analyticsSummary,
                                    helplines = helplines,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onUpdateProfile = { name, rank, stream, photoUrl ->
                                        viewModel.updateUserProfile(name, rank, stream, photoUrl)
                                    },
                                    onRemovePhoto = { viewModel.removeProfilePhoto() },
                                    onUpgradeToPremium = { viewModel.upgradeToPremium() },
                                    onLogout = { viewModel.logout() },
                                    onUnlockAllBadges = { viewModel.unlockAllBadges() },
                                    onNavigateTab = { tab -> viewModel.selectBottomTab(tab) },
                                    onDeleteTestResult = { id -> viewModel.deleteTestResult(id) },
                                    onClearTestHistory = { viewModel.clearAllTestHistory() },
                                    onChallengeStudent = { opponent -> selectedChallengeOpponent = opponent }
                                )
                            }
                        }
                    }
                }
                }

                Screen.EXAM -> {
                    val candidateCetNo by viewModel.candidateCetNumber.collectAsStateWithLifecycle()
                    val candidateVersionCode by viewModel.candidateBookletVersion.collectAsStateWithLifecycle()
                    val candidateSerialNo by viewModel.candidateBookletSerial.collectAsStateWithLifecycle()
                    val candidateName by viewModel.candidateName.collectAsStateWithLifecycle()
                    val isStrictRealExam by viewModel.isStrictRealExam.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        ExamScreen(
                            questions = activeQuestionsList.ifEmpty { questionsList },
                            currentIndex = currentIndex,
                            userAnswers = userAnswers,
                            timerSeconds = timerSeconds,
                            isTimerPaused = isTimerPaused,
                            bookmarkedQuestionIds = bookmarkedQuestionIds,
                            candidateCetNumber = candidateCetNo,
                            candidateBookletVersion = candidateVersionCode,
                            candidateBookletSerial = candidateSerialNo,
                            candidateName = candidateName,
                            isStrictRealExam = isStrictRealExam,
                            onSelectOption = { qId, opt -> viewModel.selectOption(qId, opt) },
                            onToggleMarkForReview = { qId -> viewModel.toggleMarkForReview(qId) },
                            onToggleBookmark = { q -> viewModel.toggleBookmark(q) },
                            onPrevious = { viewModel.previousQuestion() },
                            onNext = { viewModel.nextQuestion() },
                            onJumpToQuestion = { idx -> viewModel.selectQuestionIndex(idx) },
                            onTogglePauseTimer = { viewModel.togglePauseTimer() },
                            getQuestionStatus = { qId -> viewModel.getQuestionStatus(qId) },
                            onUpdateCandidateDetails = { name, cetNo, ver, serial -> viewModel.updateCandidateDetails(name, cetNo, ver, serial) },
                            onSubmitExam = { viewModel.submitExam() },
                            onQuitExam = { viewModel.quitExam(penaltyXP = 50) },
                            isSubmitting = isSubmittingExam,
                            is1v1Challenge = (viewModel.activeChallengeId != null)
                        )
                    }
                }

                Screen.RESULT -> {
                    activeResult?.let { result ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            ResultScreen(
                                result = result,
                                questions = activeQuestionsList.ifEmpty { questionsList },
                                userAnswers = userAnswers,
                                bookmarkedQuestionIds = bookmarkedQuestionIds,
                                onToggleBookmark = { q -> viewModel.toggleBookmark(q) },
                                onOpenBookmarks = { viewModel.navigateTo(Screen.BOOKMARKS) },
                                onBackToDashboard = { viewModel.navigateBack() },
                                onRetakeTest = { viewModel.startFreeTest() }
                            )
                        }
                    }
                }

                Screen.BOOKMARKS -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SavedBookmarksScreen(
                            bookmarks = bookmarkedQuestions,
                            onRemoveBookmark = { qId -> viewModel.removeBookmark(qId) },
                            onStartBookmarkPractice = { list -> viewModel.startPracticeWithBookmarks(list) },
                            onClearAllBookmarks = { viewModel.clearAllBookmarks() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }

                Screen.ADMIN -> {
                    val colleges by viewModel.colleges.collectAsStateWithLifecycle()
                    val branchCutoffs by viewModel.branchCutoffs.collectAsStateWithLifecycle()
                    val engineeringBranches by viewModel.engineeringBranches.collectAsStateWithLifecycle()
                    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
                    val allStudentTestResults by viewModel.allStudentTestResults.collectAsStateWithLifecycle()
                    val registeredMentors by viewModel.registeredMentors.collectAsStateWithLifecycle()
                    val adminRawNotifications by viewModel.allRawNotifications.collectAsStateWithLifecycle()
                    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
                    val adminChallenges by viewModel.adminChallenges.collectAsStateWithLifecycle()
                    val challengeQuestionSets by viewModel.challengeQuestionSets.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AdminPanelScreen(
                            state = AdminPanelState(
                                questions = questionsList,
                                resourcesList = resources,
                                importantDates = importantDates,
                                rankVsMarksList = rankVsMarks,
                                testSeriesList = testSeriesSets,
                                seniorChatBookings = seniorChatBookings,
                                helplines = helplines,
                                paymentReceipts = paymentReceipts,
                                colleges = colleges,
                                branchCutoffs = branchCutoffs,
                                allStudents = allStudents,
                                allStudentTestResults = allStudentTestResults,
                                announcements = announcements,
                                allNotifications = adminRawNotifications,
                                engineeringBranches = engineeringBranches,
                                registeredMentorsList = registeredMentors,
                                adminPin = adminPin,
                                adminChallenges = adminChallenges,
                                challengeQuestionSets = challengeQuestionSets,
                                liveChatPrice = liveChatPrice,
                                subscriptionPrice = subscriptionPrice
                            ),
                            actions = AdminPanelActions(
                                onAddQuestion = { sub, qText, a, b, c, d, opt, exp ->
                                    viewModel.addNewQuestion(sub, qText, a, b, c, d, opt, exp)
                                },
                                onGenerate180QuestionsForSet = { setId ->
                                    viewModel.generateAndUpload180QuestionsForSet(setId)
                                },
                                onDeleteQuestion = { id -> viewModel.deleteQuestion(id) },
                                onAddAnnouncement = { title, msg, imp ->
                                    viewModel.addAnnouncement(title, msg, imp)
                                },
                                onDeleteAnnouncement = { id ->
                                    viewModel.deleteAnnouncement(id)
                                },
                                onDeleteNotification = { id ->
                                    viewModel.deleteNotificationPermanently(id)
                                },
                                onSendDirectNotification = { title, msg, type, targetUid, targetEmail, action ->
                                    viewModel.sendAdminNotification(title, msg, type, targetUid, targetEmail, action)
                                },
                                onAddResource = { title, cat, yrSub, url, size, desc ->
                                    viewModel.addResource(title, cat, yrSub, url, size, desc)
                                },
                                onDeleteResource = { id ->
                                    viewModel.deleteResource(id)
                                },
                                onAddImportantDate = { title, dateVal, status, desc, cat, high ->
                                    viewModel.addImportantDate(title, dateVal, status, desc, cat, high)
                                },
                                onDeleteImportantDate = { id ->
                                    viewModel.deleteImportantDate(id)
                                },
                                onSyncKeaScheduleWithGemini = {
                                    viewModel.syncKeaScheduleWithGemini()
                                },
                                onAddRankVsMarks = { mRange, rRange, yr, desc, minM, maxM ->
                                    viewModel.addRankVsMarks(mRange, rRange, yr, desc, minM, maxM)
                                },
                                onDeleteRankVsMarks = { id ->
                                    viewModel.deleteRankVsMarks(id)
                                },
                                onAddTestSeriesSet = { title, cat, count, dur, prem, desc ->
                                    viewModel.addTestSeriesSet(title, cat, count, dur, prem, desc)
                                },
                                onAddTestSeriesWithQuestions = { title, cat, dur, prem, desc, qs ->
                                    viewModel.addTestSeriesWithQuestions(title, cat, dur, prem, desc, qs)
                                },
                                onAddQuestionToTestSeries = { setId, sub, qText, a, b, c, d, opt, exp, prem ->
                                    viewModel.addNewQuestion(sub, qText, a, b, c, d, opt, exp, setId, prem)
                                },
                                onDeleteTestSeriesSet = { id ->
                                    viewModel.deleteTestSeriesSet(id)
                                },
                                onUpdateBookingStatus = { bookingId, status, msg, mentorName, mentorEmail, rejectionReason ->
                                    viewModel.updateSeniorChatBookingStatus(bookingId, status, msg, mentorName, mentorEmail, rejectionReason)
                                },
                                onDeleteBooking = { bookingId ->
                                    viewModel.deleteSeniorChatBooking(bookingId)
                                },
                                onAddHelpline = { title, valStr, type, desc, url ->
                                    viewModel.addHelpline(title, valStr, type, desc, url)
                                },
                                onDeleteHelpline = { id ->
                                    viewModel.deleteHelpline(id)
                                },
                                onApproveReceipt = { receiptId, userId ->
                                    viewModel.approvePaymentReceipt(receiptId, userId)
                                },
                                onRejectReceipt = { receiptId, userId, reason ->
                                    viewModel.rejectPaymentReceipt(receiptId, userId, reason)
                                },
                                onSaveCollege = { college ->
                                    viewModel.addCollege(college)
                                },
                                onDeleteCollege = { id ->
                                    viewModel.deleteCollege(id)
                                },
                                onSaveBranchCutoff = { cutoff ->
                                    viewModel.saveBranchCutoff(cutoff)
                                },
                                onDeleteBranchCutoff = { collegeId, branchCode ->
                                    viewModel.deleteBranchCutoff(collegeId, branchCode)
                                },
                                onSaveBranch = { branch -> viewModel.addBranch(branch) },
                                onDeleteBranch = { id -> viewModel.deleteBranch(id) },
                                onUpdateStudentProfileByAdmin = { profile ->
                                    viewModel.updateUserProfileByAdmin(profile)
                                },
                                onForceLogoutStudent = { uid ->
                                    viewModel.forceLogoutStudent(uid)
                                },
                                onForceLogoutAllStudents = {
                                    viewModel.forceLogoutAllStudents()
                                },
                                onToggleBlockStudent = { uid, isBlocked, reason ->
                                    viewModel.toggleBlockStudent(uid, isBlocked, reason)
                                },
                                onGrantOrRevokePremiumByAdmin = { uid, isPremium ->
                                    viewModel.grantOrRevokePremiumByAdmin(uid, isPremium)
                                },
                                onDeleteStudentAccountByAdmin = { uid ->
                                    viewModel.deleteStudentAccountByAdmin(uid)
                                },
                                onDeleteStudentPermanently = { student ->
                                    viewModel.deleteStudentPermanentlyByAdmin(student)
                                },
                                onDeleteTestResultByAdmin = { docId ->
                                    viewModel.deleteTestResultByAdmin(docId)
                                },
                                onEditTestResultByAdmin = { docId, score, correct, wrong, percentage ->
                                    viewModel.editTestResultByAdmin(docId, score, correct, wrong, percentage)
                                },
                                onAdminPauseChallengeTimer = { id, reason -> viewModel.adminPauseChallengeTimer(id, reason) },
                                onAdminResumeChallengeTimer = { id -> viewModel.adminResumeChallengeTimer(id) },
                                onAdminTerminateChallenge = { id, reason -> viewModel.adminTerminateChallenge(id, reason) },
                                onAdminForceExpireChallenge = { id -> viewModel.adminForceExpireChallenge(id) },
                                onAdminToggleQuestionSetStatus = { id, active -> viewModel.adminToggleQuestionSetStatus(id, active) },
                                onAdminDeleteQuestionSet = { id -> viewModel.adminDeleteQuestionSet(id) },
                                onAdminVerified = {
                                    viewModel.loginAdmin()
                                },
                                onUpdateLiveChatPrice = { viewModel.updateLiveChatPrice(it) },
                                onUpdateSubscriptionPrice = { viewModel.updateSubscriptionPrice(it) },
                                onBack = {
                                    viewModel.logout()
                                }
                            )
                        )
                    }
                }

                Screen.SENIOR_CHAT -> {
                    val registeredMentors by viewModel.registeredMentors.collectAsStateWithLifecycle()
                    val seniorChatBookings by viewModel.seniorChatBookings.collectAsStateWithLifecycle()
                    val isMentorMode = registeredMentors.any { it.email.isNotBlank() && it.email.equals(userProfile.email.trim(), ignoreCase = true) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        SeniorLiveChatScreen(
                            bookings = if (isMentorMode) seniorChatBookings else mySeniorChatBookings,
                            registeredMentorsList = registeredMentors,
                            isMentorMode = isMentorMode,
                            onBookSession = { date, time, paymentRef, receiptUri ->
                                viewModel.bookSeniorChatSession(date, time, paymentRef, receiptUri)
                            },
                            onRescheduleSession = { bookingId, date, time ->
                                viewModel.rescheduleSeniorChatSession(bookingId, date, time)
                            },
                            onBack = {
                                if (isMentorMode) {
                                    viewModel.logout()
                                } else {
                                    viewModel.navigateBack()
                                }
                            },
                            onSignOut = {
                                viewModel.logout()
                            }
                        )
                    }
                }

                Screen.NOTIFICATIONS -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NotificationDashboardScreen(
                            notifications = notifications,
                            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                            onClearAll = { viewModel.clearAllNotifications() },
                            onDeleteNotification = { id -> viewModel.deleteNotification(id) },
                            onSendTestPushNotification = { viewModel.sendTestPushNotification() },
                            onBack = { viewModel.navigateBack() },
                            onNavigateAction = { action ->
                                when (action) {
                                    "NAV_SENIOR_CHAT" -> viewModel.navigateTo(Screen.SENIOR_CHAT)
                                    "NAV_PRACTICE" -> {
                                        viewModel.navigateTo(Screen.MAIN)
                                        viewModel.selectBottomTab(com.example.viewmodel.BottomTab.PRACTICE)
                                    }
                                    else -> viewModel.navigateBack()
                                }
                            }
                        )
                    }
                }

                Screen.AI_RECOMMENDATION -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AiCollegeRecommendationScreen(
                            viewModel = viewModel,
                            isPremiumUser = userProfile.isPremium,
                            onUpgradeClick = { viewModel.triggerPremiumDialog(true) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }

                Screen.BRANCH_EXPLORER -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BranchExplorerScreen(
                            viewModel = viewModel,
                            isPremiumUser = userProfile.isPremium,
                            onUpgradeClick = { viewModel.triggerPremiumDialog(true) },
                            onBack = { viewModel.navigateBack() },
                            liveChatPrice = liveChatPrice,
                            subscriptionPrice = subscriptionPrice
                        )
                    }
                }

                Screen.CHALLENGE -> {
                    val studentChallenges by viewModel.studentChallenges.collectAsStateWithLifecycle()
                    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.screens.ChallengeScreen(
                            userProfile = userProfile,
                            challenges = studentChallenges,
                            leaderboard = leaderboard,
                            onBack = { viewModel.navigateBack() },
                            onStartQuiz = { challengeData ->
                                val qSet = com.example.data.ChallengeService.getQuestionSetById(challengeData.questionSetId)
                                val questions = qSet?.questions ?: com.example.data.SampleChallengeQuestionSets.initialSets.first().questions
                                viewModel.startChallengeExam(challengeData.challengeId, questions)
                            },
                            onAcceptChallenge = { challengeId ->
                                viewModel.accept1v1Challenge(challengeId) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRejectChallenge = { challengeId ->
                                viewModel.reject1v1Challenge(challengeId) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onJoinChallenge = { challengeId ->
                                viewModel.join1v1Challenge(challengeId) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onNavigateToLeaderboard = {
                                viewModel.selectBottomTab(com.example.viewmodel.BottomTab.PROFILE)
                            },
                            onDeleteHistory = { challengeId ->
                                viewModel.delete1v1ChallengeHistory(challengeId) { success, msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onChallengeStudent = { opponent ->
                                selectedChallengeOpponent = opponent
                            }
                        )
                    }
                }

            }

            selectedChallengeOpponent?.let { opponent ->
                val context = LocalContext.current
                com.example.ui.components.ChallengeSetupDialog(
                    opponent = opponent,
                    userCurrentXp = userPoints,
                    onDismiss = { selectedChallengeOpponent = null },
                    onSendChallenge = { type ->
                        viewModel.send1v1Challenge(opponent, type) { success, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                        selectedChallengeOpponent = null
                    }
                )
            }

            if (showPremiumDialog) {
                PremiumLockDialog(
                    subscriptionPrice = subscriptionPrice,
                    paymentReceipt = currentUserPaymentReceipt,
                    isPremium = userProfile.isPremium,
                    onSubmitReceipt = { txRef, uriStr ->
                        viewModel.submitPaymentReceipt(txRef, uriStr)
                    },
                    onDismiss = { viewModel.triggerPremiumDialog(false) },
                    onUpgrade = {
                        viewModel.upgradeToPremium()
                        viewModel.triggerPremiumDialog(false)
                    }
                )
            }

            if (showBadgeCongratsDialog) {
                BadgeRewardCongratsDialog(
                    onDismiss = { viewModel.dismissBadgeCongratsDialog() }
                )
            }

            pendingExamInfo?.let { info ->
                ExamGuidelinesDialog(
                    pendingExamInfo = info,
                    onDismiss = { viewModel.dismissExamGuidelines() },
                    onConfirmStartExam = { cetNo, versionCode, serialNo, name ->
                        viewModel.confirmStartExam(cetNo, versionCode, serialNo, name)
                    }
                )
            }
        }
    }
}
}
}
