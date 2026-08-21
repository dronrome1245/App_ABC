package com.dronrome1245.appabc.data.repository

import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.data.local.db.SessionResultEntity
import com.dronrome1245.appabc.domain.curriculum.RussianAlphabet
import com.dronrome1245.appabc.domain.learning.LearningPolicy
import com.dronrome1245.appabc.domain.learning.MasteryState
import com.dronrome1245.appabc.domain.model.Attempt

enum class ParentLetterStatus {
    NOT_STARTED,
    INTRODUCED,
    PRACTICING,
    MASTERED
}

data class ParentLetterProgress(
    val letter: String,
    val status: ParentLetterStatus,
    val attemptsCount: Int,
    val correctCount: Int,
    val accuracyPercent: Int,
    val averageResponseTimeMs: Long,
    val lastSeenTimestamp: Long?
) {
    companion object {
        fun notStarted(letter: String) = ParentLetterProgress(
            letter = letter,
            status = ParentLetterStatus.NOT_STARTED,
            attemptsCount = 0,
            correctCount = 0,
            accuracyPercent = 0,
            averageResponseTimeMs = 0,
            lastSeenTimestamp = null
        )
    }
}

data class ParentDashboardSnapshot(
    val letters: List<ParentLetterProgress>,
    val completedSessions: Int,
    val overallAccuracyPercent: Int,
    val masteredLetters: Int
) {
    companion object {
        fun empty() = ParentDashboardSnapshot(
            letters = RussianAlphabet.symbols.map(ParentLetterProgress::notStarted),
            completedSessions = 0,
            overallAccuracyPercent = 0,
            masteredLetters = 0
        )
    }
}

object ParentDashboardAggregator {
    fun calculate(
        attempts: List<Attempt>,
        progressRows: List<LetterProgressEntity>,
        sessions: List<SessionResultEntity>
    ): ParentDashboardSnapshot {
        val policy = LearningPolicy()
        val performances = policy.buildPerformances(RussianAlphabet.symbols, attempts)
        val progressByLetter = progressRows.associateBy { it.letter.uppercase() }

        val letters = RussianAlphabet.symbols.map { symbol ->
            val row = progressByLetter[symbol]
            if (row == null || row.attemptsCount <= 0) {
                ParentLetterProgress.notStarted(symbol)
            } else {
                val performance = performances.getValue(symbol)
                val status = when (policy.masteryState(performance)) {
                    MasteryState.INTRODUCED -> ParentLetterStatus.INTRODUCED
                    MasteryState.PRACTICING -> ParentLetterStatus.PRACTICING
                    MasteryState.MASTERED -> ParentLetterStatus.MASTERED
                }
                ParentLetterProgress(
                    letter = symbol,
                    status = status,
                    attemptsCount = row.attemptsCount,
                    correctCount = row.correctCount,
                    accuracyPercent = row.correctCount * 100 / row.attemptsCount,
                    averageResponseTimeMs = row.averageResponseTimeMs,
                    lastSeenTimestamp = row.lastSeenTimestamp
                )
            }
        }

        val totalQuestions = sessions.sumOf { it.totalQuestions }
        val totalCorrect = sessions.sumOf { it.correctAnswers }
        return ParentDashboardSnapshot(
            letters = letters,
            completedSessions = sessions.size,
            overallAccuracyPercent = if (totalQuestions == 0) 0 else totalCorrect * 100 / totalQuestions,
            masteredLetters = letters.count { it.status == ParentLetterStatus.MASTERED }
        )
    }
}
