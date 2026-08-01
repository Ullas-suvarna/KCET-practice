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
            try {
                FirebaseService.observeUserBookmarks().collect { firestoreBookmarks ->
                    firestoreBookmarks.forEach { bookmark ->
                        bookmarkDao.addBookmark(bookmark)
                    }
                }
            } catch (e: Exception) {
                // Fallback safely
            }
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
    fun observeFirestoreTestResults(userId: String = ""): Flow<List<TestResultEntity>> = FirebaseService.observeUserTestResults(userId)
    val firestoreTestResults: Flow<List<TestResultEntity>> = FirebaseService.observeUserTestResults()
    val allResults: Flow<List<TestResultEntity>> = testResultDao.getAllResults()
    val latestResult: Flow<TestResultEntity?> = testResultDao.getLatestResult()

    suspend fun saveTestResult(result: TestResultEntity, userId: String = ""): Long {
        scope.launch {
            FirebaseService.saveTestResult(result, userId)
        }
        return testResultDao.insertResult(result)
    }

    suspend fun syncTestResultsToRoom(results: List<TestResultEntity>) {
        results.forEach { result ->
            testResultDao.insertResult(result)
        }
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

    fun addQuestionBatch(questions: List<Question>) {
        scope.launch {
            FirebaseService.saveQuestionBatch(questions)
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

    // Senior Chat Bookings Management (Firestore)
    val seniorChatBookings: Flow<List<SeniorChatBooking>> = FirebaseService.observeSeniorChatBookings()

    fun bookSeniorChatSession(booking: SeniorChatBooking) {
        scope.launch {
            FirebaseService.saveSeniorChatBooking(booking)
        }
    }

    fun updateSeniorChatBookingStatus(bookingId: String, status: String, adminMessage: String) {
        scope.launch {
            FirebaseService.updateSeniorChatBookingStatus(bookingId, status, adminMessage)
        }
    }

    fun rescheduleSeniorChatSession(bookingId: String, newDate: String, newTime: String) {
        scope.launch {
            FirebaseService.rescheduleSeniorChatBooking(bookingId, newDate, newTime)
        }
    }

    fun deleteSeniorChatBooking(bookingId: String) {
        scope.launch {
            FirebaseService.deleteSeniorChatBooking(bookingId)
        }
    }

    // Helplines Management (Firestore)
    val helplines: Flow<List<HelplineItem>> = FirebaseService.observeHelplines()

    fun addOrUpdateHelpline(item: HelplineItem) {
        scope.launch {
            FirebaseService.saveHelpline(item)
        }
    }

    fun deleteHelpline(id: String) {
        scope.launch {
            FirebaseService.deleteHelpline(id)
        }
    }


    // Bookmarks - Room Database with Firebase Firestore Sync
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun toggleBookmark(question: Question, currentlyBookmarked: Boolean, userId: String = "kcet_std_001") {
        if (currentlyBookmarked) {
            bookmarkDao.removeBookmark(question.id)
            scope.launch {
                FirebaseService.deleteUserBookmark(userId, question.id)
            }
        } else {
            val bookmark = BookmarkEntity(
                questionId = question.id,
                subject = question.subject.name,
                questionText = question.questionText,
                imageUrl = question.imageUrl,
                optionA = question.optionA,
                optionB = question.optionB,
                optionC = question.optionC,
                optionD = question.optionD,
                correctOption = question.correctOption.name,
                explanation = question.explanation
            )
            bookmarkDao.addBookmark(bookmark)
            scope.launch {
                FirebaseService.saveUserBookmark(userId, bookmark)
            }
        }
    }

    suspend fun removeBookmarkById(questionId: Int, userId: String = "kcet_std_001") {
        bookmarkDao.removeBookmark(questionId)
        scope.launch {
            FirebaseService.deleteUserBookmark(userId, questionId)
        }
    }

    suspend fun clearAllBookmarks(userId: String = "kcet_std_001", currentBookmarks: List<BookmarkEntity>) {
        currentBookmarks.forEach { bookmark ->
            bookmarkDao.removeBookmark(bookmark.questionId)
            scope.launch {
                FirebaseService.deleteUserBookmark(userId, bookmark.questionId)
            }
        }
    }

    fun isBookmarked(questionId: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(questionId)
    }
}

