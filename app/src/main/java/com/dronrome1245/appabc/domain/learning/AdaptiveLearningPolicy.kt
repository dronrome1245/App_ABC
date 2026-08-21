package com.dronrome1245.appabc.domain.learning

import com.dronrome1245.appabc.domain.model.Attempt

/** Owner-approved mastery model for LearningPolicy v4. */
enum class MasteryState {
    INTRODUCED,
    PRACTICING,
    MASTERED
}

data class MasteryEvaluation(
    val state: MasteryState,
    val isDecayed: Boolean = false
)

data class LetterPerformance(
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val recentResults: List<Boolean> = emptyList(),
    val delayedSuccesses: Int = 0,
    val lastSeenTimestamp: Long? = null,
    val lastSuccessfulTimestamp: Long? = null
) {
    val recentAccuracyPercent: Int
        get() = if (recentResults.isEmpty()) 0 else recentResults.count { it } * 100 / recentResults.size

    val recentErrors: Int
        get() = recentResults.count { !it }

    fun record(
        isCorrect: Boolean,
        isDelayedSuccess: Boolean,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): LetterPerformance {
        val updatedRecent = (recentResults + isCorrect).takeLast(LearningPolicyConfig.RECENT_ACCURACY_WINDOW)
        return copy(
            totalAttempts = totalAttempts + 1,
            correctAttempts = correctAttempts + if (isCorrect) 1 else 0,
            recentResults = updatedRecent,
            delayedSuccesses = delayedSuccesses + if (isDelayedSuccess) 1 else 0,
            lastSeenTimestamp = currentTimeMillis,
            lastSuccessfulTimestamp = if (isCorrect) currentTimeMillis else lastSuccessfulTimestamp
        )
    }
}

class LearningPolicy {
    fun masteryState(
        performance: LetterPerformance,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): MasteryState = masteryEvaluation(performance, currentTimeMillis).state

    fun masteryEvaluation(
        performance: LetterPerformance,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): MasteryEvaluation {
        val baseState = baseMasteryState(performance)
        if (baseState != MasteryState.MASTERED) return MasteryEvaluation(baseState)

        val retentionAnchor = performance.lastSuccessfulTimestamp ?: performance.lastSeenTimestamp
        val isDecayed = retentionAnchor != null &&
            currentTimeMillis > retentionAnchor &&
            currentTimeMillis - retentionAnchor > LearningPolicyConfig.RETENTION_DECAY_MILLIS

        return if (isDecayed) {
            MasteryEvaluation(MasteryState.PRACTICING, isDecayed = true)
        } else {
            MasteryEvaluation(MasteryState.MASTERED)
        }
    }

    fun selectionWeight(
        performance: LetterPerformance,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Double {
        val evaluation = masteryEvaluation(performance, currentTimeMillis)
        if (evaluation.isDecayed) return LearningPolicyConfig.RETENTION_DECAY_WEIGHT

        return when (evaluation.state) {
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
                delayedSuccesses = delayedBySymbol[symbol] ?: 0,
                lastSeenTimestamp = attempts.lastOrNull()?.timestamp?.toEpochMilli(),
                lastSuccessfulTimestamp = attempts.lastOrNull { it.isCorrect }?.timestamp?.toEpochMilli()
            )
        }
    }

    private fun baseMasteryState(performance: LetterPerformance): MasteryState = when {
        performance.totalAttempts <= LearningPolicyConfig.INTRODUCED_MAX_ATTEMPTS -> MasteryState.INTRODUCED
        performance.totalAttempts >= LearningPolicyConfig.MASTERED_MIN_ATTEMPTS &&
            performance.recentAccuracyPercent >= LearningPolicyConfig.MASTERED_ACCURACY_PERCENT -> MasteryState.MASTERED
        else -> MasteryState.PRACTICING
    }
}
