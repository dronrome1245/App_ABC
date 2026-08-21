package com.dronrome1245.appabc.domain.learning

import com.dronrome1245.appabc.domain.model.Attempt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LearningPolicyTest {
    private val policy = LearningPolicy()

    @Test
    fun `mastery transitions introduced to practicing to mastered`() {
        val introduced = LetterPerformance(
            totalAttempts = 2,
            correctAttempts = 2,
            recentResults = listOf(true, true)
        )
        val practicing = LetterPerformance(
            totalAttempts = 3,
            correctAttempts = 3,
            recentResults = listOf(true, true, true)
        )
        val mastered = LetterPerformance(
            totalAttempts = 5,
            correctAttempts = 5,
            recentResults = List(5) { true }
        )

        assertEquals(MasteryState.INTRODUCED, policy.masteryState(introduced))
        assertEquals(MasteryState.PRACTICING, policy.masteryState(practicing))
        assertEquals(MasteryState.MASTERED, policy.masteryState(mastered))
    }

    @Test
    fun `five attempts below 85 percent remain practicing`() {
        val performance = LetterPerformance(
            totalAttempts = 5,
            correctAttempts = 4,
            recentResults = listOf(true, true, true, true, false)
        )

        assertEquals(80, performance.recentAccuracyPercent)
        assertEquals(MasteryState.PRACTICING, policy.masteryState(performance))
    }

    @Test
    fun `practicing weight grows with recent errors and mastered stays nonzero`() {
        val mastered = LetterPerformance(10, 10, List(10) { true })
        val weak = LetterPerformance(6, 2, listOf(false, false, true, false, false, true))

        val masteredWeight = policy.selectionWeight(mastered)
        val weakWeight = policy.selectionWeight(weak)

        assertEquals(1.0, masteredWeight, 0.0001)
        assertTrue(weakWeight >= 2.0)
        assertTrue(weakWeight <= 3.0)
        assertTrue(weakWeight > masteredWeight)
    }

    @Test
    fun `delayed success requires at least two other questions`() {
        assertFalse(policy.isDelayedSuccess(previousPresentationIndex = 0, currentPresentationIndex = 2, isCorrect = true))
        assertTrue(policy.isDelayedSuccess(previousPresentationIndex = 0, currentPresentationIndex = 3, isCorrect = true))
        assertFalse(policy.isDelayedSuccess(previousPresentationIndex = 0, currentPresentationIndex = 3, isCorrect = false))
    }

    @Test
    fun `mastered remains mastered within six days`() {
        val performance = masteredAt(T0)
        val evaluation = policy.masteryEvaluation(performance, T0 + 6 * DAY_MILLIS)

        assertEquals(MasteryState.MASTERED, evaluation.state)
        assertFalse(evaluation.isDecayed)
    }

    @Test
    fun `mastered remains mastered exactly at seven day boundary`() {
        val performance = masteredAt(T0)
        val evaluation = policy.masteryEvaluation(performance, T0 + LearningPolicyConfig.RETENTION_DECAY_MILLIS)

        assertEquals(MasteryState.MASTERED, evaluation.state)
        assertFalse(evaluation.isDecayed)
    }

    @Test
    fun `mastered decays to practicing after seven days plus one second`() {
        val performance = masteredAt(T0)
        val now = T0 + LearningPolicyConfig.RETENTION_DECAY_MILLIS + 1_000L
        val evaluation = policy.masteryEvaluation(performance, now)

        assertEquals(MasteryState.PRACTICING, evaluation.state)
        assertTrue(evaluation.isDecayed)
        assertEquals(LearningPolicyConfig.RETENTION_DECAY_WEIGHT, policy.selectionWeight(performance, now), 0.0001)
    }

    @Test
    fun `successful confirmation after decay restores mastered`() {
        val performance = masteredAt(T0)
        val now = T0 + 8 * DAY_MILLIS

        assertTrue(policy.masteryEvaluation(performance, now).isDecayed)

        val refreshed = performance.record(
            isCorrect = true,
            isDelayedSuccess = false,
            currentTimeMillis = now
        )

        assertEquals(now, refreshed.lastSeenTimestamp)
        assertEquals(now, refreshed.lastSuccessfulTimestamp)
        assertEquals(MasteryState.MASTERED, policy.masteryState(refreshed, now))
        assertFalse(policy.masteryEvaluation(refreshed, now).isDecayed)
    }

    @Test
    fun `time evaluation leaves historical attempts unchanged`() {
        val historical = (0L until 5L).map { offset ->
            attempt(
                isCorrect = true,
                policyVersion = 3,
                timestampMillis = T0 + offset * 1_000L,
                id = offset + 1L
            )
        }
        val before = historical.map { it.copy() }
        val performance = policy.buildPerformances(listOf("А"), historical).getValue("А")

        assertEquals(
            MasteryState.PRACTICING,
            policy.masteryState(performance, T0 + 8 * DAY_MILLIS)
        )
        assertEquals(before, historical)
        assertTrue(historical.all { it.learningPolicyVersion == 3 })
        assertEquals(4, LearningPolicyConfig.VERSION)
    }

    @Test
    fun `historical attempts preserve their policy version while new policy is version four`() {
        val historical = attempt(isCorrect = true, policyVersion = 2)

        assertEquals(2, historical.learningPolicyVersion)
        assertEquals(4, LearningPolicyConfig.VERSION)
    }

    private fun masteredAt(timestampMillis: Long) = LetterPerformance(
        totalAttempts = 5,
        correctAttempts = 5,
        recentResults = List(5) { true },
        lastSeenTimestamp = timestampMillis,
        lastSuccessfulTimestamp = timestampMillis
    )

    private fun attempt(
        isCorrect: Boolean,
        policyVersion: Int = LearningPolicyConfig.VERSION,
        timestampMillis: Long = 1_000L,
        id: Long = 0L
    ) = Attempt(
        id = id,
        targetLetter = "А",
        selectedLetter = if (isCorrect) "А" else "М",
        isCorrect = isCorrect,
        responseTimeMs = 500,
        timestamp = Instant.ofEpochMilli(timestampMillis),
        sessionId = "session",
        levelId = 1,
        learningPolicyVersion = policyVersion
    )

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1_000
        const val T0 = 1_700_000_000_000L
    }
}
