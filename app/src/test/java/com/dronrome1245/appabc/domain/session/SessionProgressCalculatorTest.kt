package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.domain.m1.M1SessionConfig
import com.dronrome1245.appabc.domain.model.Attempt
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionProgressCalculatorTest {

    @Test
    fun `eight correct answers out of ten equals eighty percent`() {
        val attempts = (0 until M1SessionConfig.QUESTION_COUNT).map { index ->
            Attempt(
                targetLetter = if (index % 2 == 0) "А" else "М",
                selectedLetter = if (index < 8) {
                    if (index % 2 == 0) "А" else "М"
                } else {
                    if (index % 2 == 0) "М" else "А"
                },
                isCorrect = index < 8,
                responseTimeMs = 500,
                sessionId = "test-session",
                levelId = M1SessionConfig.LEVEL_ID
            )
        }

        val progress = SessionProgressCalculator.calculate(attempts)

        assertEquals(8, progress.correctAnswers)
        assertEquals(10, progress.totalAnswers)
        assertEquals(80, progress.accuracyPercent)
    }

    @Test
    fun `empty session has zero progress`() {
        val progress = SessionProgressCalculator.calculate(emptyList())

        assertEquals(0, progress.correctAnswers)
        assertEquals(0, progress.totalAnswers)
        assertEquals(0, progress.accuracyPercent)
    }
}
