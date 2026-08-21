package com.dronrome1245.appabc.domain.engine

import com.dronrome1245.appabc.domain.learning.InSessionRetryQueue
import com.dronrome1245.appabc.domain.learning.LearningPolicy
import com.dronrome1245.appabc.domain.learning.LearningPolicyConfig
import com.dronrome1245.appabc.domain.learning.LetterPerformance
import com.dronrome1245.appabc.domain.model.Attempt
import com.dronrome1245.appabc.domain.model.Letter
import java.util.Random

/**
 * Pure-Kotlin adaptive generator used by the training flow in LearningPolicy v4.
 */
class AdaptiveSessionGenerator(
    private val availableLetters: List<Letter>,
    history: List<Attempt> = emptyList(),
    private val random: Random = Random(),
    private val policy: LearningPolicy = LearningPolicy(),
    private val sessionLength: Int = LearningPolicyConfig.SESSION_QUESTION_COUNT,
    private val currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() }
) {
    init {
        require(availableLetters.size >= 2) { "Adaptive session requires at least two letters" }
        require(sessionLength > 0) { "Session length must be positive" }
    }

    private val lettersBySymbol = availableLetters.associateBy { it.symbol }
    private val performances = policy
        .buildPerformances(availableLetters.map { it.symbol }, history)
        .toMutableMap()
    private val retryQueue = InSessionRetryQueue(random)
    private val targetHistory = mutableListOf<String>()
    private val lastPresentationIndex = mutableMapOf<String, Int>()
    private val confusionCounts = history
        .filter { !it.isCorrect }
        .groupingBy { it.targetLetter to it.selectedLetter }
        .eachCount()
        .toMutableMap()

    private var completedQuestions = 0
    private var currentTask: Task? = null
    private var currentPreviousPresentationIndex: Int? = null

    fun nextTask(): Task {
        check(completedQuestions < sessionLength) { "Session is already complete" }

        val streakExcluded = streakExcludedSymbols()
        val retrySymbol = retryQueue.pollDue(completedQuestions, streakExcluded)
        val target = retrySymbol?.let(lettersBySymbol::get) ?: selectWeightedTarget(streakExcluded)

        val distractorPool = availableLetters.filter { it.symbol != target.symbol }
        val distractor = distractorPool[random.nextInt(distractorPool.size)]
        val options = if (random.nextBoolean()) listOf(target, distractor) else listOf(distractor, target)

        val presentationIndex = targetHistory.size
        currentPreviousPresentationIndex = lastPresentationIndex[target.symbol]
        lastPresentationIndex[target.symbol] = presentationIndex
        targetHistory += target.symbol

        return Task(target, options).also { currentTask = it }
    }

    fun recordAnswer(targetSymbol: String, selectedSymbol: String, isCorrect: Boolean) {
        val task = currentTask ?: error("No active task to record")
        require(task.target.symbol == targetSymbol) { "Answer target does not match active task" }

        val currentIndex = targetHistory.lastIndex
        val delayedSuccess = policy.isDelayedSuccess(currentPreviousPresentationIndex, currentIndex, isCorrect)
        val currentPerformance = performances[targetSymbol] ?: LetterPerformance()
        performances[targetSymbol] = currentPerformance.record(
            isCorrect = isCorrect,
            isDelayedSuccess = delayedSuccess,
            currentTimeMillis = currentTimeMillisProvider()
        )

        if (!isCorrect) {
            val key = targetSymbol to selectedSymbol
            confusionCounts[key] = confusionCounts.getOrDefault(key, 0) + 1
        }

        completedQuestions++
        if (!isCorrect) {
            val remainingQuestions = sessionLength - completedQuestions
            retryQueue.schedule(targetSymbol, completedQuestions, remainingQuestions)
        }
        currentTask = null
        currentPreviousPresentationIndex = null
    }

    fun performance(symbol: String): LetterPerformance = performances[symbol] ?: LetterPerformance()

    fun pendingRetryCount(): Int = retryQueue.pendingCount()

    fun confusionCount(targetSymbol: String, selectedSymbol: String): Int =
        confusionCounts[targetSymbol to selectedSymbol] ?: 0

    fun targets(): List<String> = targetHistory.toList()

    private fun selectWeightedTarget(streakExcluded: Set<String>): Letter {
        val waiting = retryQueue.waitingSymbols()
        var candidates = availableLetters.filter { it.symbol !in streakExcluded && it.symbol !in waiting }
        if (candidates.isEmpty()) {
            candidates = availableLetters.filter { it.symbol !in streakExcluded }
        }
        if (candidates.isEmpty()) candidates = availableLetters

        val now = currentTimeMillisProvider()
        val weighted = candidates.map { letter ->
            letter to policy.selectionWeight(
                performances[letter.symbol] ?: LetterPerformance(),
                currentTimeMillis = now
            )
        }
        val totalWeight = weighted.sumOf { it.second }
        var cursor = random.nextDouble() * totalWeight
        weighted.forEach { (letter, weight) ->
            cursor -= weight
            if (cursor < 0.0) return letter
        }
        return weighted.last().first
    }

    private fun streakExcludedSymbols(): Set<String> {
        val max = LearningPolicyConfig.MAX_CONSECUTIVE_TARGETS
        if (targetHistory.size < max) return emptySet()
        val tail = targetHistory.takeLast(max)
        return if (tail.distinct().size == 1) setOf(tail.last()) else emptySet()
    }

    data class Task(val target: Letter, val options: List<Letter>)
}
