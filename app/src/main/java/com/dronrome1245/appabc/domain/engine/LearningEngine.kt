package com.dronrome1245.appabc.domain.engine

import com.dronrome1245.appabc.domain.model.Letter
import com.dronrome1245.appabc.domain.model.Attempt
import java.util.Random

/**
 * Core learning engine that determines the next letter to present to the child.
 * Pure Kotlin, no Android dependencies.
 */
class LearningEngine(
    private val availableLetters: List<Letter>,
    private val history: List<Attempt> = emptyList(),
    private val random: Random = Random()
) {
    /**
     * Generates the next task: target letter and options (shuffled).
     * For M1.2, we always show 2 options.
     */
    fun nextTask(lastTargets: List<String> = emptyList()): Task {
        if (availableLetters.isEmpty()) throw IllegalStateException("No letters available")

        // 1. Select target letter (avoid long series of the same target)
        val target = selectTarget(lastTargets)

        // 2. Select distractors (for now, just the other available letters)
        val distractors = availableLetters.filter { it.symbol != target.symbol }
        
        // 3. Prepare options and shuffle them to avoid position-based guessing
        val options = (distractors.shuffled(random).take(1) + target).shuffled(random)

        return Task(target, options)
    }

    private fun selectTarget(lastTargets: List<String>): Letter {
        // Simple logic: don't repeat the same target more than 2 times in a row
        val possibleTargets = if (lastTargets.size >= 2 && lastTargets.takeLast(2).all { it == lastTargets.last() }) {
            availableLetters.filter { it.symbol != lastTargets.last() }
        } else {
            availableLetters
        }
        
        return (possibleTargets.ifEmpty { availableLetters }).random(random)
    }

    data class Task(
        val target: Letter,
        val options: List<Letter>
    )
}

private fun <T> List<T>.random(random: Random): T = this[random.nextInt(size)]
