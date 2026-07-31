package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KcetRepository(
    private val testResultDao: TestResultDao,
    private val bookmarkDao: BookmarkDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            FirebaseService.seedSampleQuestionsIfEmpty()
            FirebaseService.seedResourcesAndAnnouncementsIfEmpty()
        }
    }

    // Dynamic Questions List from Firestore
    val questions: Flow<List<Question>> = FirebaseService.observeQuestions()

    // Test Series & Question Sets List from Firestore
    val testSeriesSets: Flow<List<TestSeriesSet>> = FirebaseService.observeTestSeriesSets()

    // Announcements List from Firestore
    val announcements: Flow<List<Announcement>> = FirebaseService.observeAnnouncements()

    // Resources List from Firestore
    val resources: Flow<List<ResourceItem>> = FirebaseService.observeResources()

    // Important Dates & Schedule List from Firestore
    val importantDates: Flow<List<ImportantDateItem>> = FirebaseService.observeImportantDates()

    // Rank vs Marks List from Firestore
    val rankVsMarks: Flow<List<RankVsMarksItem>> = FirebaseService.observeRankVsMarks()


    // Leaderboard from Firestore
    val leaderboard: Flow<List<LeaderboardEntry>> = FirebaseService.observeLeaderboard()

    // Badges from Firestore
    val badges: Flow<List<BadgeItem>> = FirebaseService.observeBadges()

    // Test Results from Firestore with local Room fallback
    val firestoreTestResults: Flow<List<TestResultEntity>> = FirebaseService.observeUserTestResults()
    val allResults: Flow<List<TestResultEntity>> = testResultDao.getAllResults()
    val latestResult: Flow<TestResultEntity?> = testResultDao.getLatestResult()

    suspend fun saveTestResult(result: TestResultEntity): Long {
        scope.launch {
            FirebaseService.saveTestResult(result)
        }
        return testResultDao.insertResult(result)
    }

    suspend fun clearTestHistory() {
        testResultDao.clearHistory()
    }

    // Question Management (Admin & Firestore)
    fun addQuestion(question: Question) {
        scope.launch {
            FirebaseService.saveQuestion(question)
        }
    }

    fun updateQuestion(question: Question) {
        scope.launch {
            FirebaseService.saveQuestion(question)
        }
    }

    fun deleteQuestion(questionId: Int) {
        scope.launch {
            FirebaseService.deleteQuestion(questionId)
        }
    }

    // Test Series Sets Management (Admin & Firestore)
    fun addTestSeriesSet(testSet: TestSeriesSet) {
        scope.launch {
            FirebaseService.saveTestSeriesSet(testSet)
        }
    }

    fun deleteTestSeriesSet(id: String) {
        scope.launch {
            FirebaseService.deleteTestSeriesSet(id)
        }
    }

    // Resources & Announcements Management (Admin & Firestore)
    fun addResource(resource: ResourceItem) {
        scope.launch {
            FirebaseService.saveResource(resource)
        }
    }

    fun deleteResource(id: String) {
        scope.launch {
            FirebaseService.deleteResource(id)
        }
    }

    fun addAnnouncement(announcement: Announcement) {
        scope.launch {
            FirebaseService.saveAnnouncement(announcement)
        }
    }

    fun addImportantDate(item: ImportantDateItem) {
        scope.launch {
            FirebaseService.saveImportantDate(item)
        }
    }

    fun deleteImportantDate(id: String) {
        scope.launch {
            FirebaseService.deleteImportantDate(id)
        }
    }

    fun addRankVsMarks(item: RankVsMarksItem) {
        scope.launch {
            FirebaseService.saveRankVsMarks(item)
        }
    }

    fun deleteRankVsMarks(id: String) {
        scope.launch {
            FirebaseService.deleteRankVsMarks(id)
        }
    }


    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun toggleBookmark(question: Question, isBookmarked: Boolean) {
        if (isBookmarked) {
            bookmarkDao.removeBookmark(question.id)
        } else {
            bookmarkDao.addBookmark(
                BookmarkEntity(
                    questionId = question.id,
                    subject = question.subject.name,
                    questionText = question.questionText
                )
            )
        }
    }

    fun isBookmarked(questionId: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(questionId)
    }
}

