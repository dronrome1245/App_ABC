package com.dronrome1245.appabc.domain.engine

import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.learning.LearningPolicyConfig
import com.dronrome1245.appabc.domain.model.Attempt
import com.dronrome1245.appabc.domain.model.Letter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.Random

class AdaptiveSessionGeneratorTest {

    @Test
    fun `distractors never contain the target letter`() {
        val generator = AdaptiveSessionGenerator(level2Letters(), random = Random(11))

        repeat(10) {
            val task = generator.nextTask()
            assertEquals(2, task.options.size)
            assertEquals(1, task.options.count { option -> option.symbol == task.target.symbol })
            val distractors = task.options.filter { it.symbol != task.target.symbol }
            assertTrue(distractors.none { it.symbol == task.target.symbol })
            generator.recordAnswer(task.target.symbol, task.target.symbol, true)
        }
    }

    @Test
    fun `wrong target returns after at least two other questions`() {
        val generator = AdaptiveSessionGenerator(level2Letters(), random = Random(7))
        val first = generator.nextTask()
        val wrongSelection = first.options.first { it.symbol != first.target.symbol }
        generator.recordAnswer(first.target.symbol, wrongSelection.symbol, false)

        var retryIndex: Int? = null
        while (generator.targets().size < LearningPolicyConfig.SESSION_QUESTION_COUNT && retryIndex == null) {
            val task = generator.nextTask()
            val index = generator.targets().lastIndex
            if (task.target.symbol == first.target.symbol) retryIndex = index
            generator.recordAnswer(task.target.symbol, task.target.symbol, true)
        }

        assertNotNull(retryIndex)
        val questionsBetween = retryIndex!! - 1
        assertTrue("retry spacing was $questionsBetween", questionsBetween >= LearningPolicyConfig.RETRY_MIN_SPACING)
        assertTrue(questionsBetween <= LearningPolicyConfig.RETRY_MAX_SPACING)
        assertEquals(0, generator.pendingRetryCount())
    }

    @Test
    fun `retry queue remains bounded by the ten question session`() {
        val generator = AdaptiveSessionGenerator(level2Letters(), random = Random(19))

        repeat(LearningPolicyConfig.SESSION_QUESTION_COUNT) {
            val task = generator.nextTask()
            val selected = if (it == 0) task.options.first { option -> option.symbol != task.target.symbol } else task.target
            generator.recordAnswer(task.target.symbol, selected.symbol, selected.symbol == task.target.symbol)
        }

        assertEquals(10, generator.targets().size)
        assertFalse(generator.targets().windowed(3).any { it.distinct().size == 1 })
    }

    @Test
    fun `practicing letter is selected more often than mastered letter deterministically`() {
        val letters = ApprovedCurriculum.level1.introducedLetters
        val history = buildList {
            repeat(10) { index -> add(attempt("А", true, index.toLong())) }
            add(attempt("М", false, 20))
            add(attempt("М", false, 21))
            add(attempt("М", true, 22))
            add(attempt("М", false, 23))
            add(attempt("М", false, 24))
            add(attempt("М", true, 25))
        }
        val generator = AdaptiveSessionGenerator(
            availableLetters = letters,
            history = history,
            random = Random(1234),
            currentTimeMillisProvider = { 2_000L }
        )
        val counts = mutableMapOf("А" to 0, "М" to 0)

        repeat(500) {
            val target = generator.nextTask().target.symbol
            counts[target] = counts.getValue(target) + 1
        }

        assertTrue("counts=$counts", counts.getValue("М") > counts.getValue("А"))
        assertTrue(counts.getValue("А") > 0)
    }

    @Test
    fun `decayed mastered letter gets priority weight two`() {
        val letters = ApprovedCurriculum.level1.introducedLetters
        val now = T0 + 8 * DAY_MILLIS
        val history = buildList {
            repeat(5) { index -> add(attemptAt("А", true, T0 + index * 1_000L)) }
            repeat(5) { index -> add(attemptAt("М", true, now - 10_000L + index * 1_000L)) }
        }
        val generator = AdaptiveSessionGenerator(
            availableLetters = letters,
            history = history,
            random = Random(1234),
            currentTimeMillisProvider = { now }
        )
        val counts = mutableMapOf("А" to 0, "М" to 0)

        repeat(500) {
            val target = generator.nextTask().target.symbol
            counts[target] = counts.getValue(target) + 1
        }

        assertTrue("counts=$counts", counts.getValue("А") > counts.getValue("М"))
        assertTrue(counts.getValue("М") > 0)
    }

    @Test
    fun `fixed seed produces the same adaptive target sequence`() {
        fun sequence(seed: Long): List<String> {
            val generator = AdaptiveSessionGenerator(level2Letters(), random = Random(seed))
            return buildList {
                repeat(10) {
                    val task = generator.nextTask()
                    add(task.target.symbol)
                    generator.recordAnswer(task.target.symbol, task.target.symbol, true)
                }
            }
        }

        assertEquals(sequence(55), sequence(55))
    }

    @Test
    fun `wrong selection updates the correct confusion pair`() {
        val generator = AdaptiveSessionGenerator(level2Letters(), random = Random(3))
        val task = generator.nextTask()
        val wrong = task.options.first { it.symbol != task.target.symbol }

        generator.recordAnswer(task.target.symbol, wrong.symbol, false)

        assertEquals(1, generator.confusionCount(task.target.symbol, wrong.symbol))
    }

    private fun level2Letters(): List<Letter> = ApprovedCurriculum.curriculum.lettersAvailableAt(2)

    private fun attempt(symbol: String, correct: Boolean, order: Long) = attemptAt(
        symbol = symbol,
        correct = correct,
        timestampMillis = 1_000 + order
    )

    private fun attemptAt(symbol: String, correct: Boolean, timestampMillis: Long) = Attempt(
        targetLetter = symbol,
        selectedLetter = if (correct) symbol else if (symbol == "А") "М" else "А",
        isCorrect = correct,
        responseTimeMs = 400,
        timestamp = Instant.ofEpochMilli(timestampMillis),
        sessionId = "history-$symbol",
        levelId = 1,
        learningPolicyVersion = 2
    )

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1_000
        const val T0 = 1_700_000_000_000L
    }
}
