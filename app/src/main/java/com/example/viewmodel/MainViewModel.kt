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

enum class Screen {
    SPLASH, ONBOARDING, AUTH, MAIN, EXAM, RESULT, ADMIN
}

enum class BottomTab {
    HOME, PRACTICE, RESOURCES, COUNSELLING, PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = KcetRepository(db.testResultDao(), db.bookmarkDao())

    // Navigation & Tab state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(BottomTab.HOME)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    // Dark Mode
    private val _isDarkMode = MutableStateFlow(true)
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
        _userProfile.value = _userProfile.value.copy(
            email = email,
            name = if (name.isNotBlank()) name else "KCET Aspirant"
        )
        _isLoggedIn.value = true
        _currentScreen.value = Screen.MAIN
        viewModelScope.launch {
            FirebaseService.saveUserProfile(_userProfile.value)
        }
    }

    fun logout() {
        FirebaseService.auth.signOut()
        _isLoggedIn.value = false
        _currentScreen.value = Screen.AUTH
    }

    fun updateUserProfile(name: String, targetRank: String, targetStream: String) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            kcetTargetRank = targetRank,
            targetStream = targetStream
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

    init {
        updateLeaderboard()
    }

    private fun updateLeaderboard() {
        val staticToppers = listOf(
            LeaderboardEntry(1, "Ananya Rao", "Under 100", 1450, 95.2f, 18, "AR"),
            LeaderboardEntry(2, "Rohan Kulkarni", "Under 250", 1320, 91.0f, 15, "RK"),
            LeaderboardEntry(3, "Deepika Bhat", "Under 350", 1180, 88.5f, 14, "DB"),
            LeaderboardEntry(4, "Preetham Shetty", "Under 500", 1020, 84.0f, 12, "PS")
        )
        val userEntry = LeaderboardEntry(
            rank = 5,
            name = userProfile.value.name,
            kcetTargetRank = userProfile.value.kcetTargetRank,
            points = _userPoints.value,
            accuracy = _analyticsSummary.value.overallAccuracy,
            testsCount = _analyticsSummary.value.totalTestsTaken,
            avatarInitials = userProfile.value.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""),
            isCurrentUser = true
        )
        val lowerToppers = listOf(
            LeaderboardEntry(6, "Suhas Patil", "Under 1200", 420, 78.0f, 6, "SP"),
            LeaderboardEntry(7, "Meghana Hegde", "Under 1500", 380, 72.5f, 5, "MH")
        )

        val fullList = (staticToppers + userEntry + lowerToppers)
            .sortedByDescending { it.points }
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }

        _leaderboard.value = fullList
    }

    // Active test calculation summary
    private val _activeTestResult = MutableStateFlow<TestResultEntity?>(null)
    val activeTestResult: StateFlow<TestResultEntity?> = _activeTestResult.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectBottomTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    // Exam Controls
    fun startFreeTest() {
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _visitedQuestions.value = setOf(questionsList.value.firstOrNull()?.id ?: 1)
        _timerSeconds.value = 1200 // 20 minutes
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
        if (index in questionsList.value.indices) {
            _currentQuestionIndex.value = index
            val qId = questionsList.value[index].id
            _visitedQuestions.value = _visitedQuestions.value + qId
        }
    }

    fun nextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < questionsList.value.size) {
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
        val questions = questionsList.value
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
        val timeSpentSeconds = 1200 - _timerSeconds.value

        val result = TestResultEntity(
            testName = "KCET Grand Sample CBT Test 1",
            totalQuestions = total,
            score = score,
            correctCount = correct,
            wrongCount = wrong,
            unansweredCount = unanswered,
            timeTakenSeconds = timeSpentSeconds.toLong(),
            percentage = percentage
        )

        _activeTestResult.value = result
        viewModelScope.launch {
            repository.saveTestResult(result)
        }

        // Award Gamification Points (15 XP per correct + 50 XP completion bonus + 25 speed bonus)
        val speedPerQ = if (total > 0) timeSpentSeconds / total else 60
        val earnedXP = (score * 15) + 50 + (if (speedPerQ < 40) 25 else 0)
        _userPoints.value = _userPoints.value + earnedXP

        // Evaluate Badges
        val testsCount = testResultsHistory.value.size + 1
        _userBadges.value = _userBadges.value.map { badge ->
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
        explanation: String
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
            explanation = explanation
        )
        repository.addQuestion(newQ)
    }

    fun deleteQuestion(questionId: Int) {
        repository.deleteQuestion(questionId)
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

    fun addResource(title: String, category: String, size: String, desc: String) {
        val res = ResourceItem(
            id = "res_${System.currentTimeMillis()}",
            title = title,
            category = category,
            fileSize = size,
            yearOrSubject = "2026",
            downloadUrl = "https://example.com/kcet_notes.pdf",
            description = desc
        )
        repository.addResource(res)
    }
}
