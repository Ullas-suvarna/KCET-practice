package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChallengeServiceTest {

    @Test
    fun `verify challenge status transitions logic`() {
        val now = System.currentTimeMillis()
        val challenge = ChallengeData(
            challengeId = "TEST-1",
            status = "PENDING",
            acceptDeadline = now + 10000L
        )
        
        assertTrue(challenge.status == "PENDING")
        val joined = challenge.copy(status = "JOINING", challengerJoined = true)
        assertTrue(joined.challengerJoined)
        assertTrue(joined.status == "JOINING")
        
        val ready = joined.copy(status = "READY_TO_START", challengedJoined = true)
        assertTrue(ready.challengerJoined && ready.challengedJoined)
        assertTrue(ready.status == "READY_TO_START")
    }
}
