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

    // Student Notifications List from Realtime Database & Firestore fallback
    val notifications: Flow<List<StudentNotification>> = FirebaseService.observeNotifications()
    val globallyDeletedNotificationIds: Flow<Set<String>> = FirebaseService.observeGloballyDeletedNotificationIds()
    fun observeRealtimeNotifications(uid: String): Flow<List<StudentNotification>> =
        FirebaseRealtimeService.observeStudentNotifications(uid)

    // Resources List from Firestore
    val resources: Flow<List<ResourceItem>> = FirebaseService.observeResources()

    // Important Dates & Schedule List from Firestore
    val importantDates: Flow<List<ImportantDateItem>> = FirebaseService.observeImportantDates()

    // Rank vs Marks List from Firestore
    val rankVsMarks: Flow<List<RankVsMarksItem>> = FirebaseService.observeRankVsMarks()

    // Leaderboard from Firebase Firestore
    val leaderboard: Flow<List<LeaderboardEntry>> = FirebaseService.observeLeaderboard()

    // Online Status / Presence from Realtime Database
    val onlineUsers: Flow<List<UserPresence>> = FirebaseRealtimeService.observeAllOnlineUsers()
    fun observeUserPresence(uid: String): Flow<UserPresence> = FirebaseRealtimeService.observeUserPresence(uid)
    fun setupPresenceTracking(userName: String, role: String = "Student") {
        FirebaseRealtimeService.setupPresenceTracking(userName, role)
    }

    // Live XP & Rank from Realtime Database
    fun observeLiveXP(uid: String): Flow<LiveXpData> = FirebaseRealtimeService.observeLiveXP(uid)
    val allLiveXP: Flow<Map<String, LiveXpData>> = FirebaseRealtimeService.observeAllLiveXP()

    // Live Senior Chat Messages from Realtime Database
    fun observeSeniorChatMessages(chatId: String): Flow<List<SeniorChatMessage>> =
        FirebaseRealtimeService.observeSeniorChatMessages(chatId)

    suspend fun sendSeniorChatMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        isSenior: Boolean = false,
        avatarUrl: String? = null
    ): Boolean = FirebaseRealtimeService.sendSeniorChatMessage(
        chatId, senderId, senderName, messageText, isSenior, avatarUrl
    )

    // Badges from Firestore
    fun observeBadges(uid: String): Flow<List<BadgeItem>> = FirebaseService.observeBadges(uid)

    // Test Results from Firestore with local Room fallback
    fun observeFirestoreTestResults(userId: String = ""): Flow<List<TestResultEntity>> = FirebaseService.observeUserTestResults(userId)
    val firestoreTestResults: Flow<List<TestResultEntity>> = FirebaseService.observeUserTestResults()
    val allResults: Flow<List<TestResultEntity>> = testResultDao.getAllResults()
    val latestResult: Flow<TestResultEntity?> = testResultDao.getLatestResult()

    suspend fun saveTestResult(
        result: TestResultEntity,
        userId: String = "",
        userName: String = "",
        userEmail: String = "",
        studentId: String = ""
    ): Long {
        // 1. Idempotency Check in Room by Attempt ID
        if (result.attemptId.isNotBlank()) {
            val existing = testResultDao.getResultByAttemptId(result.attemptId)
            if (existing != null) {
                android.util.Log.d("KcetRepository", "Attempt ${result.attemptId} already saved in Room (ID: ${existing.id}), skipping duplicate insert")
                return existing.id.toLong()
            }
        }

        // 2. Exact match check
        val exactDup = testResultDao.findExactDuplicate(result.testName, result.timestamp, result.score)
        if (exactDup != null && (result.attemptId.isBlank() || exactDup.attemptId == result.attemptId)) {
            android.util.Log.d("KcetRepository", "Exact duplicate test result found in Room, skipping insert")
            return exactDup.id.toLong()
        }

        // 3. Recent duplicate check (within 15s window with same test name, score, total questions)
        val recentDup = testResultDao.findRecentDuplicate(
            testName = result.testName,
            score = result.score,
            totalQuestions = result.totalQuestions,
            minTimestamp = result.timestamp - 15000L,
            maxTimestamp = result.timestamp + 15000L
        )
        if (recentDup != null && (result.attemptId.isBlank() || recentDup.attemptId == result.attemptId)) {
            android.util.Log.d("KcetRepository", "Recent duplicate test result found in Room within 15s window, skipping insert")
            return recentDup.id.toLong()
        }

        // 4. Insert into Room
        val insertedId = testResultDao.insertResult(result)

        // 5. Sync to Firebase asynchronously with idempotent attemptId
        scope.launch {
            try {
                FirebaseService.saveTestResult(
                    result = result.copy(id = insertedId.toInt()),
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    studentId = studentId
                )
            } catch (e: Exception) {
                android.util.Log.e("KcetRepository", "Error syncing test result to Firebase: ${e.message}")
            }
        }
        return insertedId
    }

    suspend fun saveTestResult(result: TestResultEntity, userProfile: UserProfile): Long {
        return saveTestResult(
            result = result,
            userId = userProfile.uid,
            userName = userProfile.name,
            userEmail = userProfile.email,
            studentId = userProfile.formattedStudentId
        )
    }

    suspend fun syncTestResultsToRoom(results: List<TestResultEntity>) {
        val existingRoomResults = testResultDao.getAllResultsList().toMutableList()
        val processedKeys = mutableSetOf<String>()

        results.forEach { remote ->
            val remoteKey = if (remote.attemptId.isNotBlank()) {
                remote.attemptId
            } else {
                "${remote.testName}_${remote.timestamp}_${remote.score}_${remote.totalQuestions}"
            }
            if (!processedKeys.add(remoteKey)) {
                return@forEach
            }

            // Check if already in Room by Attempt ID
            val matchedByAttemptId = if (remote.attemptId.isNotBlank()) {
                existingRoomResults.find { it.attemptId.isNotBlank() && it.attemptId == remote.attemptId }
            } else null

            // Check by identical score, name, questions count, and close timestamp window
            val matchedByContent = if (matchedByAttemptId == null) {
                existingRoomResults.find { local ->
                    local.testName == remote.testName &&
                    local.score == remote.score &&
                    local.totalQuestions == remote.totalQuestions &&
                    local.correctCount == remote.correctCount &&
                    Math.abs(local.timestamp - remote.timestamp) < 15000L
                }
            } else null

            val existingLocal = matchedByAttemptId ?: matchedByContent

            if (existingLocal == null) {
                // Not in Room yet, insert it
                val newId = testResultDao.insertResult(remote.copy(id = 0))
                existingRoomResults.add(remote.copy(id = newId.toInt()))
            } else {
                // Already in Room! If local attemptId was blank and remote has it, update it
                if (existingLocal.attemptId.isBlank() && remote.attemptId.isNotBlank()) {
                    val updated = existingLocal.copy(attemptId = remote.attemptId)
                    testResultDao.insertResult(updated)
                    val idx = existingRoomResults.indexOfFirst { it.id == existingLocal.id }
                    if (idx != -1) {
                        existingRoomResults[idx] = updated
                    }
                }
            }
        }
    }

    suspend fun cleanupDuplicateTestResults() {
        val allResults = testResultDao.getAllResultsList()
        if (allResults.size <= 1) return

        val duplicatesToDelete = mutableListOf<Int>()
        val retained = mutableListOf<TestResultEntity>()

        // Sort by id ascending to keep the first legitimate original attempt
        allResults.sortedBy { it.id }.forEach { res ->
            val duplicateOf = retained.find { existing ->
                (res.attemptId.isNotBlank() && existing.attemptId.isNotBlank() && res.attemptId == existing.attemptId) ||
                (res.testName == existing.testName &&
                 res.score == existing.score &&
                 res.totalQuestions == existing.totalQuestions &&
                 res.correctCount == existing.correctCount &&
                 res.wrongCount == existing.wrongCount &&
                 Math.abs(res.timestamp - existing.timestamp) < 15000L)
            }
            if (duplicateOf != null) {
                // If the existing one had no attemptId but this one does, update the existing one
                if (duplicateOf.attemptId.isBlank() && res.attemptId.isNotBlank()) {
                    testResultDao.insertResult(duplicateOf.copy(attemptId = res.attemptId))
                }
                duplicatesToDelete.add(res.id)
            } else {
                retained.add(res)
            }
        }

        duplicatesToDelete.forEach { id ->
            testResultDao.deleteResultById(id)
        }
    }

    suspend fun clearTestHistory() {
        testResultDao.clearHistory()
    }

    suspend fun deleteTestResult(id: Int) {
        testResultDao.deleteResultById(id)
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

    fun deleteAnnouncement(id: String) {
        scope.launch {
            FirebaseService.deleteAnnouncement(id)
        }
    }

    fun addNotification(notification: StudentNotification) {
        scope.launch {
            FirebaseService.saveNotification(notification)
        }
    }

    fun deleteNotification(id: String) {
        scope.launch {
            FirebaseService.deleteNotification(id)
        }
    }

    fun addImportantDate(item: ImportantDateItem) {
        scope.launch {
            FirebaseService.saveImportantDate(item)
        }
    }

    suspend fun syncKeaScheduleWithGemini(currentDateStr: String = "August 08, 2026"): Result<List<ImportantDateItem>> {
        val result = GeminiCounsellingSyncService.fetchLatestKeaSchedule(currentDateStr)
        if (result.isSuccess) {
            val items = result.getOrNull() ?: emptyList()
            if (items.isNotEmpty()) {
                FirebaseService.replaceAllImportantDates(items)
            }
        }
        return result
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

    fun updateSeniorChatBookingStatus(
        bookingId: String,
        status: String,
        adminMessage: String,
        mentorName: String = "",
        assignedMentorEmail: String = "",
        rejectionReason: String = ""
    ) {
        scope.launch {
            FirebaseService.updateSeniorChatBookingStatus(
                bookingId, status, adminMessage, mentorName, assignedMentorEmail, rejectionReason
            )
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

    // Payment Receipts Management (Firestore)
    val paymentReceipts: Flow<List<PaymentReceipt>> = FirebaseService.observePaymentReceipts()

    fun observeUserPaymentReceipt(userId: String): Flow<PaymentReceipt?> = FirebaseService.observeUserPaymentReceipt(userId)

    fun submitPaymentReceipt(receipt: PaymentReceipt) {
        scope.launch {
            FirebaseService.submitPaymentReceipt(receipt)
        }
    }

    fun updatePaymentReceiptStatus(receiptId: String, userId: String, status: String, rejectionReason: String = "") {
        scope.launch {
            FirebaseService.updatePaymentReceiptStatus(receiptId, userId, status, rejectionReason)
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

    // Colleges and Branch Explorer
    val colleges: Flow<List<CollegeInfo>> = FirebaseService.observeColleges()
    val engineeringBranches: Flow<List<EngineeringBranchInfo>> = FirebaseService.observeBranches()
    val branchCutoffs: Flow<List<BranchCutoffInfo>> = FirebaseService.observeBranchCutoffs()

    fun addCollege(college: CollegeInfo) {
        scope.launch {
            FirebaseService.saveCollege(college)
        }
    }

    fun deleteCollege(id: String) {
        scope.launch {
            FirebaseService.deleteCollege(id)
        }
    }

    fun addBranch(branch: EngineeringBranchInfo) {
        scope.launch {
            FirebaseService.saveBranch(branch)
        }
    }

    fun deleteBranch(id: String) {
        scope.launch {
            FirebaseService.deleteBranch(id)
        }
    }

    fun saveBranchCutoff(cutoff: BranchCutoffInfo) {
        scope.launch {
            FirebaseService.saveBranchCutoff(cutoff)
        }
    }

    fun deleteBranchCutoff(collegeId: String, branchCode: String) {
        scope.launch {
            FirebaseService.deleteBranchCutoff(collegeId, branchCode)
        }
    }
}

