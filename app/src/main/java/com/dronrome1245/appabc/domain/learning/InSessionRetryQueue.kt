package com.dronrome1245.appabc.domain.learning

import java.util.Random

class InSessionRetryQueue(
    private val random: Random,
    private val minSpacing: Int = LearningPolicyConfig.RETRY_MIN_SPACING,
    private val maxSpacing: Int = LearningPolicyConfig.RETRY_MAX_SPACING
) {
    private data class RetryEntry(val symbol: String, val dueAfterCompletedQuestions: Int)

    private val entries = mutableListOf<RetryEntry>()

    fun schedule(symbol: String, completedQuestions: Int, remainingQuestions: Int) {
        if (remainingQuestions < minSpacing + 1) return
        val effectiveMaxSpacing = minOf(maxSpacing, remainingQuestions - 1)
        if (effectiveMaxSpacing < minSpacing) return

        val spacing = minSpacing + random.nextInt(effectiveMaxSpacing - minSpacing + 1)
        entries.removeAll { it.symbol == symbol }
        entries += RetryEntry(symbol, completedQuestions + spacing)
    }

    fun pollDue(completedQuestions: Int, excludedSymbols: Set<String> = emptySet()): String? {
        val candidate = entries
            .filter { it.dueAfterCompletedQuestions <= completedQuestions && it.symbol !in excludedSymbols }
            .minByOrNull { it.dueAfterCompletedQuestions }
            ?: return null
        entries.remove(candidate)
        return candidate.symbol
    }

    fun waitingSymbols(): Set<String> = entries.mapTo(mutableSetOf()) { it.symbol }

    fun pendingCount(): Int = entries.size
}
