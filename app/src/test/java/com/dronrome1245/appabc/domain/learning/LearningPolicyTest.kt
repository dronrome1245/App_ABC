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
    fun `historical attempts preserve their policy version while new policy is version three`() {
        val historical = attempt(isCorrect = true, policyVersion = 2)

        assertEquals(2, historical.learningPolicyVersion)
        assertEquals(3, LearningPolicyConfig.VERSION)
    }

    private fun attempt(isCorrect: Boolean, policyVersion: Int = LearningPolicyConfig.VERSION) = Attempt(
        targetLetter = "А",
        selectedLetter = if (isCorrect) "А" else "М",
        isCorrect = isCorrect,
        responseTimeMs = 500,
        timestamp = Instant.ofEpochMilli(1_000),
        sessionId = "session",
        levelId = 1,
        learningPolicyVersion = policyVersion
    )
}
