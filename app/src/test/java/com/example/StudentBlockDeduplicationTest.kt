package com.example

import com.example.data.FirebaseService
import com.example.data.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudentBlockDeduplicationTest {

    @Test
    fun `students with same name but different emails are not merged or falsely blocked`() {
        val blockedStudent = UserProfile(
            uid = "uid_1",
            name = "Ullas Suvarna",
            email = "ullas1@gmail.com",
            isBlocked = true,
            blockReason = "Violation",
            lastUpdatedMillis = 2000L
        )
        val activeStudent = UserProfile(
            uid = "uid_2",
            name = "Ullas Suvarna",
            email = "ullas2@gmail.com",
            isBlocked = false,
            lastUpdatedMillis = 3000L
        )

        assertFalse(FirebaseService.areSameStudent(blockedStudent, activeStudent))

        val deduplicated = FirebaseService.deduplicateStudentProfiles(listOf(blockedStudent, activeStudent))
        assertEquals(2, deduplicated.size)
        val s1 = deduplicated.first { it.email == "ullas1@gmail.com" }
        val s2 = deduplicated.first { it.email == "ullas2@gmail.com" }
        assertTrue(s1.isBlocked)
        assertFalse(s2.isBlocked)
    }

    @Test
    fun `newer active record overrides stale blocked record for same student`() {
        val staleBlockedDoc = UserProfile(
            uid = "uid_abc",
            name = "Karthik",
            email = "karthik@example.com",
            isBlocked = true,
            blockReason = "Old block",
            lastUpdatedMillis = 1000L
        )
        val newerActiveDoc = UserProfile(
            uid = "uid_abc",
            name = "Karthik",
            email = "karthik@example.com",
            isBlocked = false,
            blockReason = "",
            lastUpdatedMillis = 5000L
        )

        val deduplicated = FirebaseService.deduplicateStudentProfiles(listOf(staleBlockedDoc, newerActiveDoc))
        assertEquals(1, deduplicated.size)
        assertFalse(deduplicated.first().isBlocked)
        assertEquals("", deduplicated.first().blockReason)
    }
}
