package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.domain.model.Attempt

data class LetterSessionBreakdown(
    val letter: String,
    val attempts: Int,
    val correct: Int,
    val errors: Int,
    val averageResponseTimeMs: Long
)

object SessionLetterBreakdownCalculator {
    fun calculate(attempts: List<Attempt>): List<LetterSessionBreakdown> = attempts
        .groupBy { it.targetLetter }
        .map { (letter, letterAttempts) ->
            val correct = letterAttempts.count { it.isCorrect }
            LetterSessionBreakdown(
                letter = letter,
                attempts = letterAttempts.size,
                correct = correct,
                errors = letterAttempts.size - correct,
                averageResponseTimeMs = letterAttempts.map { it.responseTimeMs }.average().toLong()
            )
        }
        .sortedBy { it.letter }
}
