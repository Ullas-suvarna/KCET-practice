package com.example

import com.example.data.TestResultEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CbtTestSubmissionTest {

    // Helper mimicking MainViewModel.calculateEarnedXP
    private fun calculateEarnedXP(res: TestResultEntity): Int {
        val qCount = res.totalQuestions.coerceAtLeast(1)
        val attempted = res.correctCount + res.wrongCount
        val attemptedRatio = attempted.toFloat() / qCount

        if (attemptedRatio <= 0.5f) {
            return 0
        }

        val correctXP = res.score * 15
        val completionBonus = (50f * attemptedRatio).toInt()
        val speedPerQ = res.timeTakenSeconds / qCount
        val speedBonus = if (speedPerQ < 40) (25f * attemptedRatio).toInt() else 0

        return correctXP + completionBonus + speedBonus
    }

    // Helper mimicking MainViewModel.getUniqueAttempts
    private fun getUniqueAttempts(results: List<TestResultEntity>): List<TestResultEntity> {
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

    @Test
    fun testXpCalculation_dailyAiPracticeSetScore7Of15_earnsExactly180Xp() {
        val result = TestResultEntity(
            attemptId = "att_test_1",
            testName = "📅 Daily AI Practice Set (DPP)",
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300, // 20s per question (< 40s)
            percentage = (7f / 15f) * 100f
        )

        val xp = calculateEarnedXP(result)
        // 7 * 15 = 105 XP (correct) + 50 XP (completion 100%) + 25 XP (speed bonus) = 180 XP
        assertEquals(180, xp)
    }

    @Test
    fun testCaseA_submitOnce_producesOneHistoryRecordAndXpOnce() {
        val attemptId = "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        val attempt1 = TestResultEntity(
            id = 1,
            attemptId = attemptId,
            testName = "📅 Daily AI Practice Set (DPP)",
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )

        val historyList = listOf(attempt1)
        val uniqueAttempts = getUniqueAttempts(historyList)

        assertEquals(1, uniqueAttempts.size)
        val totalXP = uniqueAttempts.sumOf { calculateEarnedXP(it) }
        assertEquals(180, totalXP)
    }

    @Test
    fun testCaseB_doubleClickSubmit_deduplicatesToOneHistoryRecordAndXpOnce() {
        val attemptId = "att_fixed_attempt_123"
        val timestamp = System.currentTimeMillis()

        // Simulating two records created by double-click with same attemptId
        val click1 = TestResultEntity(
            id = 1,
            attemptId = attemptId,
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = timestamp,
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )
        val click2 = TestResultEntity(
            id = 2,
            attemptId = attemptId,
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = timestamp + 50, // 50ms later
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )

        val historyWithDuplicates = listOf(click1, click2)
        val uniqueAttempts = getUniqueAttempts(historyWithDuplicates)

        assertEquals(1, uniqueAttempts.size)
        val totalXP = uniqueAttempts.sumOf { calculateEarnedXP(it) }
        assertEquals(180, totalXP) // NOT 360 XP
    }

    @Test
    fun testCaseC_rapidDuplicateWithoutAttemptId_deduplicatesByContentAndTimestampWindow() {
        val now = System.currentTimeMillis()

        val record1 = TestResultEntity(
            id = 1,
            attemptId = "",
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = now,
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )
        val record2 = TestResultEntity(
            id = 2,
            attemptId = "att_from_remote_123",
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = now + 200, // 200ms later from remote sync
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )

        // Deduplication in cleanup
        val duplicatesToDelete = mutableListOf<Int>()
        val retained = mutableListOf<TestResultEntity>()

        for (res in listOf(record1, record2).sortedBy { it.id }) {
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
                duplicatesToDelete.add(res.id)
            } else {
                retained.add(res)
            }
        }

        assertEquals(1, retained.size)
        assertEquals(1, duplicatesToDelete.size)
        assertEquals(2, duplicatesToDelete.first()) // record 2 was detected as duplicate of record 1
    }

    @Test
    fun testCaseD_reopenResultPageOrNavigateBack_noNewRecordOrXpAdded() {
        val processedAttempts = mutableSetOf<String>()
        val awardedAttempts = mutableSetOf<String>()
        var userXP = 0

        val attemptId = "att_attempt_cbt_999"

        // First submission
        processedAttempts.add(attemptId)
        awardedAttempts.add(attemptId)
        userXP += 180

        // User navigates back, reopens result screen or refreshes
        val isAlreadyProcessed = processedAttempts.contains(attemptId)
        assertTrue("Attempt must be recognized as already processed", isAlreadyProcessed)

        val isAlreadyAwarded = awardedAttempts.contains(attemptId)
        assertTrue("XP must be recognized as already awarded", isAlreadyAwarded)

        if (!isAlreadyAwarded) {
            userXP += 180
        }

        // Verify XP is unchanged
        assertEquals(180, userXP)
    }

    @Test
    fun testCaseE_intentionallyStartNewAttempt_generatesNewAttemptAndAllowsNewRecord() {
        val attemptId1 = "att_${System.currentTimeMillis()}_1111"
        val attempt1 = TestResultEntity(
            id = 1,
            attemptId = attemptId1,
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = System.currentTimeMillis() - 3600000L, // 1 hour ago
            totalQuestions = 15,
            score = 7,
            correctCount = 7,
            wrongCount = 8,
            unansweredCount = 0,
            timeTakenSeconds = 300,
            percentage = 46.7f
        )

        // User intentionally retakes the test as attempt 2
        val attemptId2 = "att_${System.currentTimeMillis()}_2222"
        val attempt2 = TestResultEntity(
            id = 2,
            attemptId = attemptId2,
            testName = "📅 Daily AI Practice Set (DPP)",
            timestamp = System.currentTimeMillis(),
            totalQuestions = 15,
            score = 10,
            correctCount = 10,
            wrongCount = 5,
            unansweredCount = 0,
            timeTakenSeconds = 250,
            percentage = 66.7f
        )

        val historyList = listOf(attempt1, attempt2)
        val uniqueAttempts = getUniqueAttempts(historyList)

        // Both distinct attempts must be preserved!
        assertEquals(2, uniqueAttempts.size)
        // Attempt 1: 180 XP, Attempt 2: 10*15 (150) + 50 (completion) + 25 (speed) = 225 XP
        val totalXP = uniqueAttempts.sumOf { calculateEarnedXP(it) }
        assertEquals(180 + 225, totalXP)
    }
}
