package com.dronrome1245.appabc.domain.engine

import com.dronrome1245.appabc.domain.m1.M1SessionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class LearningEngineTest {

    private val letters = M1SessionConfig.letters

    @Test
    fun `M1 config uses approved letters and ten questions`() {
        assertEquals(10, M1SessionConfig.QUESTION_COUNT)
        assertEquals(80, M1SessionConfig.MASTERY_THRESHOLD_PERCENT)
        assertEquals(8, M1SessionConfig.MASTERY_CORRECT_ANSWERS)
        assertEquals(setOf("А", "М"), letters.map { it.symbol }.toSet())
    }

    @Test
    fun `nextTask returns a task with target and 2 options`() {
        val engine = LearningEngine(letters)
        val task = engine.nextTask()

        assertTrue(letters.contains(task.target))
        assertEquals(2, task.options.size)
        assertTrue(task.options.contains(task.target))
    }

    @Test
    fun `nextTask avoids long series of the same target`() {
        val engine = LearningEngine(letters)
        val lastTargets = listOf("А", "А")

        val task = engine.nextTask(lastTargets)

        assertEquals("М", task.target.symbol)
    }

    @Test
    fun `ten question M1 session stays inside approved letter set and repeat limit`() {
        val engine = LearningEngine(letters, random = Random(42))
        val targets = mutableListOf<String>()

        repeat(M1SessionConfig.QUESTION_COUNT) {
            val task = engine.nextTask(targets)
            assertTrue(task.target.symbol in setOf("А", "М"))
            assertEquals(setOf("А", "М"), task.options.map { it.symbol }.toSet())
            targets += task.target.symbol
        }

        assertEquals(10, targets.size)
        assertFalse(targets.windowed(3).any { window -> window.distinct().size == 1 })
    }

    @Test
    fun `fixed seed produces reproducible ten question target sequence`() {
        fun generate(seed: Long): List<String> {
            val engine = LearningEngine(letters, random = Random(seed))
            val targets = mutableListOf<String>()
            repeat(M1SessionConfig.QUESTION_COUNT) {
                targets += engine.nextTask(targets).target.symbol
            }
            return targets
        }

        assertEquals(generate(123), generate(123))
    }

    @Test
    fun `nextTask shuffles options`() {
        val tasks = (1..10).map {
            LearningEngine(letters, random = Random(it.toLong())).nextTask()
        }
        val allOptions = tasks.map { task -> task.options.map { letter -> letter.symbol } }

        assertTrue(allOptions.contains(listOf("А", "М")))
        assertTrue(allOptions.contains(listOf("М", "А")))
    }
}
