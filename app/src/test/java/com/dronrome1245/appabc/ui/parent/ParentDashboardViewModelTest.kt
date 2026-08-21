package com.dronrome1245.appabc.ui.parent

import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.data.local.db.SessionResultEntity
import com.dronrome1245.appabc.data.repository.ParentDashboardAggregator
import com.dronrome1245.appabc.data.repository.ParentDashboardSnapshot
import com.dronrome1245.appabc.data.repository.ParentLetterProgress
import com.dronrome1245.appabc.data.repository.ParentLetterStatus
import com.dronrome1245.appabc.domain.learning.LearningPolicyConfig
import com.dronrome1245.appabc.domain.model.Attempt
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentDashboardViewModelTest {
    @Test
    fun uiStateAlwaysContainsAll33LettersWithNotStartedDefaults() {
        val snapshot = ParentDashboardSnapshot(
            letters = listOf(
                ParentLetterProgress(
                    letter = "А",
                    status = ParentLetterStatus.PRACTICING,
                    attemptsCount = 3,
                    correctCount = 2,
                    accuracyPercent = 66,
                    averageResponseTimeMs = 900,
                    lastSeenTimestamp = 10L
                )
            ),
            completedSessions = 1,
            overallAccuracyPercent = 80,
            masteredLetters = 0
        )

        val state = ParentDashboardViewModel.toUiState(snapshot)

        assertEquals(33, state.letters.size)
        assertEquals("А", state.letters.first().letter)
        assertTrue(state.letters.any { it.letter == "Ё" })
        assertEquals(ParentLetterStatus.PRACTICING, state.letters.first { it.letter == "А" }.status)
        assertEquals(ParentLetterStatus.NOT_STARTED, state.letters.first { it.letter == "Я" }.status)
    }

    @Test
    fun aggregatorUsesLearningPolicyForMasteredAndSessionAccuracy() {
        val attempts = (1L..5L).map { id ->
            Attempt(
                id = id,
                targetLetter = "А",
                selectedLetter = "А",
                isCorrect = true,
                responseTimeMs = 700,
                timestamp = Instant.ofEpochMilli(id * 1_000),
                sessionId = "s1",
                levelId = 1
            )
        }
        val progressRows = listOf(
            LetterProgressEntity(
                letter = "А",
                attemptsCount = 5,
                correctCount = 5,
                lastSeenTimestamp = 5_000,
                averageResponseTimeMs = 700
            )
        )
        val sessions = listOf(
            SessionResultEntity(
                id = 1,
                sessionId = "s1",
                levelId = 1,
                totalQuestions = 10,
                correctAnswers = 8,
                passed = true,
                completedAt = 10_000
            )
        )

        val snapshot = ParentDashboardAggregator.calculate(
            attempts = attempts,
            progressRows = progressRows,
            sessions = sessions,
            currentTimeMillis = 5_000
        )

        assertEquals(33, snapshot.letters.size)
        assertEquals(ParentLetterStatus.MASTERED, snapshot.letters.first { it.letter == "А" }.status)
        assertFalse(snapshot.letters.first { it.letter == "А" }.requiresReview)
        assertEquals(1, snapshot.masteredLetters)
        assertEquals(1, snapshot.completedSessions)
        assertEquals(80, snapshot.overallAccuracyPercent)
    }

    @Test
    fun aggregatorMarksDecayedMasteredLetterAsRequiringReview() {
        val lastSuccessful = 1_700_000_000_000L
        val attempts = (1L..5L).map { id ->
            Attempt(
                id = id,
                targetLetter = "А",
                selectedLetter = "А",
                isCorrect = true,
                responseTimeMs = 700,
                timestamp = Instant.ofEpochMilli(lastSuccessful - (5L - id) * 1_000L),
                sessionId = "s1",
                levelId = 1,
                learningPolicyVersion = 3
            )
        }
        val progressRows = listOf(
            LetterProgressEntity(
                letter = "А",
                attemptsCount = 5,
                correctCount = 5,
                lastSeenTimestamp = lastSuccessful,
                averageResponseTimeMs = 700
            )
        )
        val now = lastSuccessful + LearningPolicyConfig.RETENTION_DECAY_MILLIS + 1_000L

        val snapshot = ParentDashboardAggregator.calculate(
            attempts = attempts,
            progressRows = progressRows,
            sessions = emptyList(),
            currentTimeMillis = now
        )
        val letter = snapshot.letters.first { it.letter == "А" }

        assertEquals(ParentLetterStatus.PRACTICING, letter.status)
        assertTrue(letter.requiresReview)
        assertEquals(lastSuccessful, letter.lastSeenTimestamp)
        assertEquals(0, snapshot.masteredLetters)
    }
}
