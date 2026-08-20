package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.domain.model.Attempt
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class LetterProgressAccumulatorTest {
    @Test
    fun mergesNewAttemptsIntoPersistentLetterProgress() {
        val existing = LetterProgressEntity(
            letter = "А",
            attemptsCount = 2,
            correctCount = 1,
            lastSeenTimestamp = 500,
            averageResponseTimeMs = 1000
        )
        val newAttempts = listOf(
            attempt(correct = true, responseTimeMs = 2000, timestamp = 1000),
            attempt(correct = true, responseTimeMs = 1000, timestamp = 2000)
        )

        val result = LetterProgressAccumulator.accumulate(existing, newAttempts)

        assertEquals("А", result.letter)
        assertEquals(4, result.attemptsCount)
        assertEquals(3, result.correctCount)
        assertEquals(2000, result.lastSeenTimestamp)
        assertEquals(1250, result.averageResponseTimeMs)
    }

    @Test
    fun createsProgressWhenLetterHasNoExistingAggregate() {
        val result = LetterProgressAccumulator.accumulate(
            existing = null,
            newAttempts = listOf(
                attempt(correct = false, responseTimeMs = 900, timestamp = 1000),
                attempt(correct = true, responseTimeMs = 1100, timestamp = 2000)
            )
        )

        assertEquals(2, result.attemptsCount)
        assertEquals(1, result.correctCount)
        assertEquals(1000, result.averageResponseTimeMs)
    }

    private fun attempt(correct: Boolean, responseTimeMs: Long, timestamp: Long) = Attempt(
        targetLetter = "А",
        selectedLetter = if (correct) "А" else "М",
        isCorrect = correct,
        responseTimeMs = responseTimeMs,
        timestamp = Instant.ofEpochMilli(timestamp),
        sessionId = "session",
        levelId = 1
    )
}
