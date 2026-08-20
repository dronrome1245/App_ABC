package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.domain.model.Attempt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SessionLetterBreakdownCalculatorTest {
    @Test
    fun calculatesPerLetterBreakdownForCurrentSession() {
        val attempts = listOf(
            attempt("А", true, 1000),
            attempt("А", false, 2000),
            attempt("М", true, 900),
            attempt("А", true, 1500),
            attempt("М", true, 1100)
        )

        val result = SessionLetterBreakdownCalculator.calculate(attempts)

        assertEquals(2, result.size)
        assertEquals(LetterSessionBreakdown("А", 3, 2, 1, 1500), result[0])
        assertEquals(LetterSessionBreakdown("М", 2, 2, 0, 1000), result[1])
    }

    private fun attempt(letter: String, correct: Boolean, responseTimeMs: Long) = Attempt(
        targetLetter = letter,
        selectedLetter = if (correct) letter else "М",
        isCorrect = correct,
        responseTimeMs = responseTimeMs,
        timestamp = Instant.ofEpochMilli(1_000),
        sessionId = "session",
        levelId = 1
    )
}
