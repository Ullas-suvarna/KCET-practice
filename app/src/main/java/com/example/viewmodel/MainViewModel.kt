package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnalyticsSummary
import com.example.data.Announcement
import com.example.data.AppDatabase
import com.example.data.BadgeItem
import com.example.data.FirebaseService
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.SeniorChatBooking

import com.example.data.BookmarkEntity

enum class Screen {
    SPLASH, ONBOARDING, AUTH, MAIN, EXAM, RESULT, ADMIN, SENIOR_CHAT, BOOKMARKS
}

enum class BottomTab {
    HOME, PRACTICE, DOUBTS, VIDEOS, RESOURCES, COUNSELLING, PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = KcetRepository(db.testResultDao(), db.bookmarkDao())

    // Navigation & Tab state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.HOME)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    // Senior Mentorship Live Chat Bookings
    val seniorChatBookings: StateFlow<List<SeniorChatBooking>> = repository.seniorChatBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun bookSeniorChatSession(date: String, time: String) {
        val user = userProfile.value
        val newBooking = SeniorChatBooking(
            bookingId = "KCET-LIVE-${(1000..9999).random()}",
            userId = user.uid,
            userName = user.name,
            userEmail = user.email,
            selectedDate = date,
            selectedTime = time,
            amountPaid = 50,
            status = "Waiting for Senior Confirmation",
            adminMessage = ""
        )
        repository.bookSeniorChatSession(newBooking)
    }

    fun updateSeniorChatBookingStatus(bookingId: String, status: String, adminMessage: String) {
        repository.updateSeniorChatBookingStatus(bookingId, status, adminMessage)
    }

    fun rescheduleSeniorChatSession(bookingId: String, date: String, time: String) {
        repository.rescheduleSeniorChatSession(bookingId, date, time)
    }

    fun deleteSeniorChatBooking(bookingId: String) {
        repository.deleteSeniorChatBooking(bookingId)
    }

    fun generateAndUpload180QuestionsForSet(testSetId: String) {
        val questions = com.example.data.SampleData.generate180QuestionsForSet(testSetId)
        repository.addQuestionBatch(questions)
    }

    // Dark Mode (Default OFF as requested by user)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Auth & User Profile State
    private val _userProfile = MutableStateFlow(
        UserProfile(
            uid = "kcet_std_001",
            name = "Karthik Gowda",
            email = "karthik.kcet2026@gmail.com",
            isPremium = false,
            kcetTargetRank = "Under 500",
            targetStream = "Engineering (B.E - CSE / AI)"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(email: String, name: String = "KCET Aspirant") {
        val cleanEmail = email.lowercase().trim()
        val calculatedUid = FirebaseService.auth.currentUser?.uid ?: cleanEmail.replace(".", "_").replace("@", "_").ifBlank { "kcet_std_001" }
        _isLoggedIn.value = true
        _currentScreen.value = Screen.MAIN

        viewModelScope.launch {
            val remoteProfile = FirebaseService.getUserProfileOnce(calculatedUid)
            if (remoteProfile != null) {
                val isNowPremium = remoteProfile.isPremium || _userProfile.value.isPremium
                _userProfile.value = remoteProfile.copy(
                    email = cleanEmail,
                    name = if (name.isNotBlank()) name else remoteProfile.name,
                    isPremium = isNowPremium
                )
            } else {
                _userProfile.value = _userProfile.value.copy(
                    uid = calculatedUid,
                    email = cleanEmail,
                    name = if (name.isNotBlank()) name else "KCET Aspirant"
                )
            }
            FirebaseService.saveUserProfile(_userProfile.value)
            startObservingUserData(_userProfile.value.uid)
        }
    }

    fun logout() {
        FirebaseService.auth.signOut()
        _isLoggedIn.value = false
        _currentScreen.value = Screen.AUTH
    }

    fun updateUserProfile(name: String, targetRank: String, targetStream: String, photoUrl: String? = null) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            kcetTargetRank = targetRank,
            targetStream = targetStream,
            photoUrl = photoUrl
        )
        viewModelScope.launch {
            FirebaseService.saveUserProfile(_userProfile.value)
            FirebaseService.updateLeaderboardEntry(
                profile = _userProfile.value,
                points = _userPoints.value,
                accuracy = _analyticsSummary.value.overallAccuracy,
                testsCount = _analyticsSummary.value.totalTestsTaken
            )
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
        if (bookmarkList.isEmpty()) return
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(bookmarkList.first().questionId)
        _timerSeconds.value = (bookmarkList.size * 90).coerceAtLeast(300)
        _isTimerPaused.value = false
        startTimer()
        _currentScreen.value = Screen.EXAM
    }

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

    fun triggerPremiumDialog(show: Boolean) {
        _showPremiumDialog.value = show
    }

    // Badge Reward Congrats Dialog Trigger
    private val _showBadgeCongratsDialog = MutableStateFlow(false)
    val showBadgeCongratsDialog: StateFlow<Boolean> = _showBadgeCongratsDialog.asStateFlow()

    fun dismissBadgeCongratsDialog() {
        _showBadgeCongratsDialog.value = false
    }

    fun unlockAllBadges() {
        val updated = _userBadges.value.map { it.copy(isUnlocked = true, progress = 1f, unlockedDate = "2026") }
        _userBadges.value = updated
        _userProfile.value = _userProfile.value.copy(isPremium = true)
        _showBadgeCongratsDialog.value = true
        viewModelScope.launch {
            FirebaseService.saveUserProfile(_userProfile.value)
            updated.forEach { badge ->
                FirebaseService.saveBadge(badge)
            }
        }
    }

    // Gamification & Points State
    private val _userPoints = MutableStateFlow(450)
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    // Badges State
    private val _userBadges = MutableStateFlow<List<BadgeItem>>(
        listOf(
            BadgeItem("b1", "KCET Pioneer", "Complete your first CBT mock test", "🎯", isUnlocked = true, unlockedDate = "2026", progress = 1f),
            BadgeItem("b2", "Top Scorer", "Score 80%+ on any mock exam", "🏆", isUnlocked = false, progress = 0.75f),
            BadgeItem("b3", "Perfect Score", "Achieve 100% accuracy on a test", "💯", isUnlocked = false, progress = 0.4f),
            BadgeItem("b4", "Speed Demon", "Finish exam with < 30s per question", "⚡", isUnlocked = false, progress = 0.5f),
            BadgeItem("b5", "Streak Master", "Attempt 3 or more mock tests", "🔥", isUnlocked = false, progress = 0.33f),
            BadgeItem("b6", "Physics Wizard", "Score 80%+ in Physics section", "⚛️", isUnlocked = false, progress = 0.7f),
            BadgeItem("b7", "Chemistry Ace", "Score 80%+ in Chemistry section", "🧪", isUnlocked = false, progress = 0.85f),
            BadgeItem("b8", "Maths Genius", "Score 80%+ in Mathematics section", "📐", isUnlocked = false, progress = 0.65f)
        )
    )
    val userBadges: StateFlow<List<BadgeItem>> = _userBadges.asStateFlow()

    // Leaderboard State
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Analytics Summary State
    private val _analyticsSummary = MutableStateFlow<AnalyticsSummary>(
        AnalyticsSummary(
            totalTestsTaken = 1,
            overallAccuracy = 75f,
            totalPoints = 450,
            avgTimePerQuestionSec = 35f,
            subjectBreakdown = listOf(
                SubjectAnalytics(Subject.PHYSICS, 7, 5, 2, 71.4f, 32f),
                SubjectAnalytics(Subject.CHEMISTRY, 7, 6, 1, 85.7f, 28f),
                SubjectAnalytics(Subject.MATHEMATICS, 6, 4, 2, 66.7f, 42f)
            ),
            topicAccuracies = listOf(
                TopicAccuracy("Mechanics & Motion", Subject.PHYSICS, 3, 4, 75f, "Strong"),
                TopicAccuracy("Optics & Lenses", Subject.PHYSICS, 2, 3, 66f, "Moderate"),
                TopicAccuracy("Organic Reactions", Subject.CHEMISTRY, 4, 4, 100f, "Strong"),
                TopicAccuracy("Electrochemistry & Solutions", Subject.CHEMISTRY, 2, 3, 66f, "Moderate"),
                TopicAccuracy("Matrices & Determinants", Subject.MATHEMATICS, 2, 2, 100f, "Strong"),
                TopicAccuracy("Calculus & Limits", Subject.MATHEMATICS, 2, 4, 50f, "Needs Practice")
            ),
            weakAreas = listOf(
                "Calculus & Limits (50% Accuracy)",
                "Optics & Lenses Formula Applications"
            ),
            recommendations = listOf(
                "Review Mathematics Calculus limits properties in Formula Sheets",
                "Solve Physics Optics PYQs to improve speed and accuracy",
                "Attempt Physics-focused sectional mock test"
            )
        )
    )
    val analyticsSummary: StateFlow<AnalyticsSummary> = _analyticsSummary.asStateFlow()

    private var profileJob: Job? = null
    private var testResultsJob: Job? = null

    private fun startObservingUserData(uid: String) {
        profileJob?.cancel()
        testResultsJob?.cancel()

        profileJob = viewModelScope.launch {
            FirebaseService.observeUserProfile(uid).collect { remoteProfile ->
                if (remoteProfile != null) {
                    val current = _userProfile.value
                    val mergedPremium = remoteProfile.isPremium || current.isPremium
                    _userProfile.value = current.copy(
                        isPremium = mergedPremium,
                        name = if (current.name != "Karthik Gowda" && current.name.isNotBlank()) current.name else remoteProfile.name,
                        kcetTargetRank = remoteProfile.kcetTargetRank.ifBlank { current.kcetTargetRank },
                        targetStream = remoteProfile.targetStream.ifBlank { current.targetStream },
                        photoUrl = remoteProfile.photoUrl ?: current.photoUrl
                    )
                    if (mergedPremium && !remoteProfile.isPremium) {
                        FirebaseService.saveUserProfile(_userProfile.value)
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
    }

    init {
        updateLeaderboard()
        startObservingUserData(_userProfile.value.uid)
        viewModelScope.launch {
            testResultsHistory.collect { results ->
                if (results.isNotEmpty()) {
                    val totalCorrect = results.sumOf { it.correctCount }
                    val totalQs = results.sumOf { it.totalQuestions }
                    val calculatedAcc = if (totalQs > 0) (totalCorrect.toFloat() / totalQs) * 100f else 75f
                    val earnedFromTests = results.sumOf { res ->
                        val qCount = res.totalQuestions.coerceAtLeast(1)
                        val speed = res.timeTakenSeconds / qCount
                        (res.score * 15) + 50 + (if (speed < 40) 25 else 0)
                    }
                    _userPoints.value = 450 + earnedFromTests.toInt()
                    _analyticsSummary.value = _analyticsSummary.value.copy(
                        totalTestsTaken = results.size,
                        overallAccuracy = calculatedAcc,
                        totalPoints = _userPoints.value
                    )
                    updateLeaderboard()
                }
            }
        }
        viewModelScope.launch {
            repository.badges.collect { firestoreBadges ->
                if (firestoreBadges.isNotEmpty()) {
                    _userBadges.value = firestoreBadges
                    if (firestoreBadges.all { it.isUnlocked }) {
                        if (!_userProfile.value.isPremium) {
                            _userProfile.value = _userProfile.value.copy(isPremium = true)
                            FirebaseService.saveUserProfile(_userProfile.value)
                        }
                    }
                }
            }
        }
    }

    private fun updateLeaderboard() {
        val currentName = userProfile.value.name
        val currentPoints = _userPoints.value
        val currentAcc = _analyticsSummary.value.overallAccuracy
        val currentTests = _analyticsSummary.value.totalTestsTaken
        val currentTargetRank = userProfile.value.kcetTargetRank

        val staticToppers = listOf(
            LeaderboardEntry(1, "Ananya Rao", "Under 100", 1450, 95.2f, 18, "AR"),
            LeaderboardEntry(2, "Rohan Kulkarni", "Under 250", 1320, 91.0f, 15, "RK"),
            LeaderboardEntry(3, "Deepika Bhat", "Under 350", 1180, 88.5f, 14, "DB"),
            LeaderboardEntry(4, "Preetham Shetty", "Under 500", 1020, 84.0f, 12, "PS"),
            LeaderboardEntry(5, "Suhas Patil", "Under 1200", 420, 78.0f, 6, "SP"),
            LeaderboardEntry(6, "Meghana Hegde", "Under 1500", 380, 72.5f, 5, "MH")
        )

        val initials = currentName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
        val currentUserEntry = LeaderboardEntry(
            rank = 0,
            name = currentName,
            kcetTargetRank = currentTargetRank,
            points = currentPoints,
            accuracy = currentAcc,
            testsCount = currentTests,
            avatarInitials = if (initials.isNotBlank()) initials else "KG",
            isCurrentUser = true
        )

        val combined = (staticToppers + currentUserEntry)
            .distinctBy { if (it.isCurrentUser) "me" else it.name }
            .sortedByDescending { it.points }
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }

        _leaderboard.value = combined

        viewModelScope.launch {
            FirebaseService.updateLeaderboardEntry(
                profile = _userProfile.value,
                points = currentPoints,
                accuracy = currentAcc,
                testsCount = currentTests
            )
        }
    }

    // Active test calculation summary
    private val _activeTestResult = MutableStateFlow<TestResultEntity?>(null)
    val activeTestResult: StateFlow<TestResultEntity?> = _activeTestResult.asStateFlow()

    private val _activeQuestionsList = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestionsList: StateFlow<List<Question>> = _activeQuestionsList.asStateFlow()

    private val _activeTestSet = MutableStateFlow<com.example.data.TestSeriesSet?>(null)
    val activeTestSet: StateFlow<com.example.data.TestSeriesSet?> = _activeTestSet.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectBottomTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    // Exam Controls
    fun startFreeTest() {
        val freeQs = questionsList.value.ifEmpty { com.example.data.SampleData.sampleQuestions }
        _activeQuestionsList.value = freeQs.take(20)
        _activeTestSet.value = null
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(_activeQuestionsList.value.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 1200 // 20 minutes
        _isTimerPaused.value = false
        _currentScreen.value = Screen.EXAM
        startTimer()
    }

    fun startTestForSet(testSet: com.example.data.TestSeriesSet) {
        _activeTestSet.value = testSet
        val setQuestions = questionsList.value.filter { it.testSetId == testSet.id }
        val finalQs = if (setQuestions.isNotEmpty()) {
            setQuestions
        } else {
            com.example.data.SampleData.generate180QuestionsForSet(testSet.id).take(testSet.questionCount)
        }
        _activeQuestionsList.value = finalQs
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(finalQs.firstOrNull()?.id ?: 1)
        _timerSeconds.value = (testSet.durationMins * 60).coerceAtLeast(300)
        _isTimerPaused.value = false
        _currentScreen.value = Screen.EXAM
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

    fun selectOption(questionId: Int, option: Option) {
        val currentAns = _userAnswers.value[questionId] ?: UserAnswer(questionId = questionId)
        val newAns = if (currentAns.selectedOption == option) {
            currentAns.copy(selectedOption = null) // Deselect
        } else {
            currentAns.copy(selectedOption = option)
        }
        _userAnswers.value = _userAnswers.value + (questionId to newAns)
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

    fun submitExam() {
        timerJob?.cancel()
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

        val testNameStr = _activeTestSet.value?.title ?: "KCET Grand Practice CBT Test"

        val result = TestResultEntity(
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
            repository.saveTestResult(result, _userProfile.value.uid)
        }

        // Award Gamification Points (15 XP per correct + 50 XP completion bonus + 25 speed bonus)
        val speedPerQ = if (total > 0) timeSpentSeconds / total else 60
        val earnedXP = (score * 15) + 50 + (if (speedPerQ < 40) 25 else 0)
        _userPoints.value = _userPoints.value + earnedXP

        // Evaluate Badges
        val testsCount = testResultsHistory.value.size + 1
        val newBadges = _userBadges.value.map { badge ->
            when (badge.id) {
                "b1" -> badge.copy(isUnlocked = true, progress = 1f)
                "b2" -> if (percentage >= 80f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (percentage / 80f).coerceIn(0f, 1f))
                "b3" -> if (percentage == 100f) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = (percentage / 100f).coerceIn(0f, 1f))
                "b4" -> if (speedPerQ <= 30) badge.copy(isUnlocked = true, progress = 1f) else badge
                "b5" -> if (testsCount >= 3) badge.copy(isUnlocked = true, progress = 1f) else badge.copy(progress = testsCount / 3f)
                "b6" -> if (score >= 12) badge.copy(isUnlocked = true, progress = 1f) else badge
                "b7" -> if (score >= 14) badge.copy(isUnlocked = true, progress = 1f) else badge
                "b8" -> if (score >= 15) badge.copy(isUnlocked = true, progress = 1f) else badge
                else -> badge
            }
        }
        _userBadges.value = newBadges

        viewModelScope.launch {
            newBadges.forEach { FirebaseService.saveBadge(it) }
        }

        if (newBadges.isNotEmpty() && newBadges.all { it.isUnlocked }) {
            _userProfile.value = _userProfile.value.copy(isPremium = true)
            _showBadgeCongratsDialog.value = true
            viewModelScope.launch {
                FirebaseService.saveUserProfile(_userProfile.value)
            }
        }

        // Calculate Subject Analytics Breakdown
        val physicsQs = questions.filter { it.subject == Subject.PHYSICS }
        val chemistryQs = questions.filter { it.subject == Subject.CHEMISTRY }
        val mathQs = questions.filter { it.subject == Subject.MATHEMATICS }

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

        updateLeaderboard()
        _currentScreen.value = Screen.RESULT
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

    fun deleteTestSeriesSet(id: String) {
        repository.deleteTestSeriesSet(id)
    }

    fun addAnnouncement(title: String, message: String, isImportant: Boolean) {
        val ann = Announcement(
            id = "ann_${System.currentTimeMillis()}",
            title = title,
            date = "Just now",
            message = message,
            isImportant = isImportant
        )
        repository.addAnnouncement(ann)
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
}

