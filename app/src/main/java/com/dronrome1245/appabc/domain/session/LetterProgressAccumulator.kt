package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.domain.model.Attempt

object LetterProgressAccumulator {
    fun accumulate(
        existing: LetterProgressEntity?,
        newAttempts: List<Attempt>
    ): LetterProgressEntity {
        require(newAttempts.isNotEmpty()) { "At least one attempt is required" }
        val letter = newAttempts.first().targetLetter
        require(newAttempts.all { it.targetLetter == letter }) { "All attempts must target the same letter" }

        val previousAttempts = existing?.attemptsCount ?: 0
        val previousCorrect = existing?.correctCount ?: 0
        val previousResponseTotal = (existing?.averageResponseTimeMs ?: 0L) * previousAttempts
        val newResponseTotal = newAttempts.sumOf { it.responseTimeMs }
        val totalAttempts = previousAttempts + newAttempts.size

        return LetterProgressEntity(
            letter = letter,
            attemptsCount = totalAttempts,
            correctCount = previousCorrect + newAttempts.count { it.isCorrect },
            lastSeenTimestamp = newAttempts.maxOf { it.timestamp.toEpochMilli() },
            averageResponseTimeMs = if (totalAttempts == 0) 0L else (previousResponseTotal + newResponseTotal) / totalAttempts
        )
    }
}
