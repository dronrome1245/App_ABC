package com.dronrome1245.appabc.domain.curriculum

import com.dronrome1245.appabc.domain.engine.LearningEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class CurriculumTest {

    @Test
    fun levelsContainOwnerApprovedLettersAndTenQuestions() {
        val curriculum = ApprovedCurriculum.curriculum

        assertEquals(listOf("А", "М"), curriculum.level(1).introducedLetters.map { it.symbol })
        assertEquals(listOf("О", "У"), curriculum.level(2).introducedLetters.map { it.symbol })
        assertEquals(listOf("С", "Н"), curriculum.level(3).introducedLetters.map { it.symbol })
        assertTrue(curriculum.levels.all { it.questionCount == 10 })
    }

    @Test
    fun studiedPoolAccumulatesPreviousLevels() {
        val curriculum = ApprovedCurriculum.curriculum

        assertEquals(listOf("А", "М"), curriculum.lettersAvailableAt(1).map { it.symbol })
        assertEquals(listOf("А", "М", "О", "У"), curriculum.lettersAvailableAt(2).map { it.symbol })
        assertEquals(listOf("А", "М", "О", "У", "С", "Н"), curriculum.lettersAvailableAt(3).map { it.symbol })
    }

    @Test
    fun distractorPoolUsesStudiedLettersAndExcludesTarget() {
        val distractors = ApprovedCurriculum.curriculum.distractorPool(3, "С").map { it.symbol }

        assertTrue(distractors.containsAll(listOf("А", "М", "О", "У", "Н")))
        assertFalse(distractors.contains("С"))
    }

    @Test
    fun learningEngineGeneratesTenValidQuestionsForEachApprovedLevel() {
        (1..3).forEach { levelId ->
            val pool = ApprovedCurriculum.curriculum.lettersAvailableAt(levelId)
            val allowed = pool.map { it.symbol }.toSet()
            val engine = LearningEngine(pool, random = Random(levelId.toLong()))
            val lastTargets = mutableListOf<String>()

            repeat(10) {
                val task = engine.nextTask(lastTargets)
                assertTrue(task.target.symbol in allowed)
                assertEquals(2, task.options.size)
                assertTrue(task.options.any { option -> option.symbol == task.target.symbol })
                assertTrue(task.options.all { option -> option.symbol in allowed })
                assertEquals(2, task.options.map { option -> option.symbol }.distinct().size)
                lastTargets += task.target.symbol
            }
        }
    }
}
