package com.dronrome1245.appabc.domain.engine

import com.dronrome1245.appabc.domain.model.Letter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class LearningEngineTest {

    private val letters = listOf(
        Letter("А", "а", 1),
        Letter("О", "о", 1)
    )

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
        
        assertEquals("О", task.target.symbol)
    }

    @Test
    fun `nextTask shuffles options`() {
        // Use fixed seeds to verify shuffling
        val engine1 = LearningEngine(letters, random = Random(1))
        val task1 = engine1.nextTask()
        
        val engine2 = LearningEngine(letters, random = Random(2))
        val task2 = engine2.nextTask()
        
        // This is a probabilistic test, but with different seeds and 2 options,
        // we can check if they are ever different. 
        // For 2 letters [A, O], options are either [A, O] or [O, A].
        
        val tasks = (1..10).map { LearningEngine(letters, random = Random(it.toLong())).nextTask() }
        val allOptions = tasks.map { it.options.map { l -> l.symbol } }
        
        assertTrue(allOptions.contains(listOf("А", "О")))
        assertTrue(allOptions.contains(listOf("О", "А")))
    }
}
