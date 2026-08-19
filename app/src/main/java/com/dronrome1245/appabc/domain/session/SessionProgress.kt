package com.dronrome1245.appabc.domain.session

import com.dronrome1245.appabc.domain.model.Attempt

data class SessionProgress(
    val correctAnswers: Int,
    val totalAnswers: Int,
    val accuracyPercent: Int
)

object SessionProgressCalculator {
    fun calculate(attempts: List<Attempt>): SessionProgress {
        val total = attempts.size
        val correct = attempts.count { it.isCorrect }
        val accuracy = if (total == 0) 0 else correct * 100 / total

        return SessionProgress(
            correctAnswers = correct,
            totalAnswers = total,
            accuracyPercent = accuracy
        )
    }
}
