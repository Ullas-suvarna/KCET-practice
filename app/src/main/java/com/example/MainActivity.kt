package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.PremiumLockDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CounsellingScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SeniorLiveChatScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.KcetPracticeTheme
import com.example.viewmodel.BottomTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val announcements by repositoryFlow(viewModel.repository.announcements)
    val resources by repositoryFlow(viewModel.repository.resources)
    val importantDates by viewModel.importantDates.collectAsStateWithLifecycle()
    val rankVsMarks by viewModel.rankVsMarks.collectAsStateWithLifecycle()
    val testHistory by viewModel.testResultsHistory.collectAsStateWithLifecycle()
    val showPremiumDialog by viewModel.showPremiumDialog.collectAsStateWithLifecycle()
    val seniorChatBookings by viewModel.seniorChatBookings.collectAsStateWithLifecycle()


    // Gamification & Analytics States
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val userBadges by viewModel.userBadges.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val analyticsSummary by viewModel.analyticsSummary.collectAsStateWithLifecycle()
    val userRank = leaderboard.find { it.isCurrentUser }?.rank ?: 5

    // Exam States
    val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerPaused by viewModel.isTimerPaused.collectAsStateWithLifecycle()
    val activeResult by viewModel.activeTestResult.collectAsStateWithLifecycle()

    KcetPracticeTheme(darkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.SPLASH -> {
                    SplashScreen(
                        isLoggedIn = isLoggedIn,
                        onNavigateNext = { loggedIn ->
                            if (loggedIn) {
                                viewModel.navigateTo(Screen.MAIN)
                            } else {
                                viewModel.navigateTo(Screen.ONBOARDING)
                            }
                        }
                    )
                }

                Screen.ONBOARDING -> {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            viewModel.navigateTo(Screen.AUTH)
                        }
                    )
                }

                Screen.AUTH -> {
                    AuthScreen(
                        onLoginSuccess = { email, name ->
                            viewModel.login(email, name)
                        }
                    )
                }

                Screen.MAIN -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
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
                                .windowInsetsPadding(WindowInsets.safeDrawing)
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
                                    badges = userBadges,
                                    onStartFreeTest = { viewModel.startFreeTest() },
                                    onUpgradeClick = { viewModel.triggerPremiumDialog(true) },
                                    onNavigateTab = { tab -> viewModel.selectBottomTab(tab) },
                                    onOpenAdmin = { viewModel.navigateTo(Screen.ADMIN) },
                                    onOpenSeniorChat = { viewModel.navigateTo(Screen.SENIOR_CHAT) }
                                )

                                BottomTab.PRACTICE -> PracticeScreen(
                                    isPremiumUser = userProfile.isPremium,
                                    onStartFreeTest = { viewModel.startFreeTest() },
                                    onLockedFeatureClick = { viewModel.triggerPremiumDialog(true) }
                                )

                                BottomTab.RESOURCES -> ResourcesScreen(
                                    resources = resources
                                )

                                BottomTab.COUNSELLING -> CounsellingScreen(
                                    importantDates = importantDates,
                                    onOpenSeniorChat = { viewModel.navigateTo(Screen.SENIOR_CHAT) }
                                )

                                BottomTab.PROFILE -> ProfileScreen(
                                    userProfile = userProfile,
                                    testResults = testHistory,
                                    userPoints = userPoints,
                                    leaderboardEntries = leaderboard,
                                    badges = userBadges,
                                    analyticsSummary = analyticsSummary,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onUpdateProfile = { name, rank, stream, photoUrl ->
                                        viewModel.updateUserProfile(name, rank, stream, photoUrl)
                                    },
                                    onUpgradeToPremium = { viewModel.upgradeToPremium() },
                                    onLogout = { viewModel.logout() },
                                    onOpenAdmin = { viewModel.navigateTo(Screen.ADMIN) },
                                    onNavigateTab = { tab -> viewModel.selectBottomTab(tab) }
                                )
                            }
                        }
                    }
                }

                Screen.EXAM -> {
                    Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        ExamScreen(
                            questions = questionsList,
                            currentIndex = currentIndex,
                            userAnswers = userAnswers,
                            timerSeconds = timerSeconds,
                            isTimerPaused = isTimerPaused,
                            onSelectOption = { qId, opt -> viewModel.selectOption(qId, opt) },
                            onToggleMarkForReview = { qId -> viewModel.toggleMarkForReview(qId) },
                            onPrevious = { viewModel.previousQuestion() },
                            onNext = { viewModel.nextQuestion() },
                            onJumpToQuestion = { idx -> viewModel.selectQuestionIndex(idx) },
                            onTogglePauseTimer = { viewModel.togglePauseTimer() },
                            getQuestionStatus = { qId -> viewModel.getQuestionStatus(qId) },
                            onSubmitExam = { viewModel.submitExam() }
                        )
                    }
                }

                Screen.RESULT -> {
                    activeResult?.let { result ->
                        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                            ResultScreen(
                                result = result,
                                questions = questionsList,
                                userAnswers = userAnswers,
                                onBackToDashboard = { viewModel.navigateTo(Screen.MAIN) },
                                onRetakeTest = { viewModel.startFreeTest() }
                            )
                        }
                    }
                }

                Screen.ADMIN -> {
                    Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        AdminPanelScreen(
                            questions = questionsList,
                            resourcesList = resources,
                            importantDates = importantDates,
                            rankVsMarksList = rankVsMarks,
                            onAddQuestion = { sub, qText, a, b, c, d, opt, exp ->
                                viewModel.addNewQuestion(sub, qText, a, b, c, d, opt, exp)
                            },
                            onDeleteQuestion = { id -> viewModel.deleteQuestion(id) },
                            onAddAnnouncement = { title, msg, imp ->
                                viewModel.addAnnouncement(title, msg, imp)
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
                            onAddRankVsMarks = { mRange, rRange, yr, desc, minM, maxM ->
                                viewModel.addRankVsMarks(mRange, rRange, yr, desc, minM, maxM)
                            },
                            onDeleteRankVsMarks = { id ->
                                viewModel.deleteRankVsMarks(id)
                            },
                            onBack = { viewModel.navigateTo(Screen.MAIN) }
                        )
                    }
                }

                Screen.SENIOR_CHAT -> {
                    Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        SeniorLiveChatScreen(
                            bookings = seniorChatBookings,
                            onBookSession = { date, time ->
                                viewModel.bookSeniorChatSession(date, time)
                            },
                            onBack = { viewModel.navigateTo(Screen.MAIN) }
                        )
                    }
                }

            }

            if (showPremiumDialog) {
                PremiumLockDialog(
                    onDismiss = { viewModel.triggerPremiumDialog(false) },
                    onUpgrade = {
                        viewModel.upgradeToPremium()
                        viewModel.triggerPremiumDialog(false)
                    }
                )
            }
        }
    }
}

@Composable
fun <T> repositoryFlow(flow: kotlinx.coroutines.flow.Flow<T>): androidx.compose.runtime.State<T> {
    return flow.collectAsStateWithLifecycle(
        initialValue = when (flow) {
            is kotlinx.coroutines.flow.StateFlow<*> -> flow.value as T
            else -> emptyList<Any>() as T
        }
    )
}
