package com.dronrome1245.appabc.domain.learning

import com.dronrome1245.appabc.domain.model.Attempt

/** Owner-approved mastery model for LearningPolicy v3. */
enum class MasteryState {
    INTRODUCED,
    PRACTICING,
    MASTERED
}

data class LetterPerformance(
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val recentResults: List<Boolean> = emptyList(),
    val delayedSuccesses: Int = 0
) {
    val recentAccuracyPercent: Int
        get() = if (recentResults.isEmpty()) 0 else recentResults.count { it } * 100 / recentResults.size

    val recentErrors: Int
        get() = recentResults.count { !it }

    fun record(isCorrect: Boolean, isDelayedSuccess: Boolean): LetterPerformance {
        val updatedRecent = (recentResults + isCorrect).takeLast(LearningPolicyConfig.RECENT_ACCURACY_WINDOW)
        return copy(
            totalAttempts = totalAttempts + 1,
            correctAttempts = correctAttempts + if (isCorrect) 1 else 0,
            recentResults = updatedRecent,
            delayedSuccesses = delayedSuccesses + if (isDelayedSuccess) 1 else 0
        )
    }
}

class LearningPolicy {
    fun masteryState(performance: LetterPerformance): MasteryState = when {
        performance.totalAttempts <= LearningPolicyConfig.INTRODUCED_MAX_ATTEMPTS -> MasteryState.INTRODUCED
        performance.totalAttempts >= LearningPolicyConfig.MASTERED_MIN_ATTEMPTS &&
            performance.recentAccuracyPercent >= LearningPolicyConfig.MASTERED_ACCURACY_PERCENT -> MasteryState.MASTERED
        else -> MasteryState.PRACTICING
    }

    fun selectionWeight(performance: LetterPerformance): Double = when (masteryState(performance)) {
        MasteryState.MASTERED -> LearningPolicyConfig.MASTERED_WEIGHT
        MasteryState.INTRODUCED -> LearningPolicyConfig.INTRODUCED_WEIGHT
        MasteryState.PRACTICING -> {
            val recentErrorRatio = if (performance.recentResults.isEmpty()) {
                0.0
            } else {
                performance.recentErrors.toDouble() / performance.recentResults.size
            }
            val range = LearningPolicyConfig.PRACTICING_MAX_WEIGHT - LearningPolicyConfig.PRACTICING_BASE_WEIGHT
            LearningPolicyConfig.PRACTICING_BASE_WEIGHT + recentErrorRatio * range
        }
    }

    fun isDelayedSuccess(previousPresentationIndex: Int?, currentPresentationIndex: Int, isCorrect: Boolean): Boolean {
        if (!isCorrect || previousPresentationIndex == null) return false
        val questionsBetween = currentPresentationIndex - previousPresentationIndex - 1
        return questionsBetween >= LearningPolicyConfig.RETRY_MIN_SPACING
    }

    fun buildPerformances(symbols: List<String>, history: List<Attempt>): Map<String, LetterPerformance> {
        val delayedBySymbol = mutableMapOf<String, Int>()
        history.groupBy { it.sessionId }.values.forEach { sessionAttempts ->
            val ordered = sessionAttempts.sortedWith(compareBy<Attempt> { it.timestamp }.thenBy { it.id })
            val lastPosition = mutableMapOf<String, Int>()
            ordered.forEachIndexed { index, attempt ->
                if (isDelayedSuccess(lastPosition[attempt.targetLetter], index, attempt.isCorrect)) {
                    delayedBySymbol[attempt.targetLetter] = delayedBySymbol.getOrDefault(attempt.targetLetter, 0) + 1
                }
                lastPosition[attempt.targetLetter] = index
            }
        }

        return symbols.associateWith { symbol ->
            val attempts = history
                .filter { it.targetLetter == symbol }
                .sortedWith(compareBy<Attempt> { it.timestamp }.thenBy { it.id })
            LetterPerformance(
                totalAttempts = attempts.size,
                correctAttempts = attempts.count { it.isCorrect },
                recentResults = attempts.takeLast(LearningPolicyConfig.RECENT_ACCURACY_WINDOW).map { it.isCorrect },
                delayedSuccesses = delayedBySymbol[symbol] ?: 0
            )
        }
    }
}
