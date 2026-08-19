package com.dronrome1245.appabc.domain.curriculum

import com.dronrome1245.appabc.domain.model.Letter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun approvedLevel1KeepsOwnerAcceptedLettersAndSessionLength() {
        assertEquals(10, ApprovedCurriculum.level1.questionCount)
        assertEquals(listOf("А", "М"), ApprovedCurriculum.level1.introducedLetters.map { it.symbol })
        assertEquals(listOf("А", "М"), ApprovedCurriculum.curriculum.lettersAvailableAt(1).map { it.symbol })
    }

    @Test
    fun genericCurriculumAccumulatesEarlierLettersAcrossThreeLevels() {
        val curriculum = Curriculum(
            listOf(
                level(1, "А", "М"),
                level(2, "X", "Y"),
                level(3, "Z")
            )
        )

        assertEquals(listOf("А", "М"), curriculum.lettersAvailableAt(1).map { it.symbol })
        assertEquals(listOf("А", "М", "X", "Y"), curriculum.lettersAvailableAt(2).map { it.symbol })
        assertEquals(listOf("А", "М", "X", "Y", "Z"), curriculum.lettersAvailableAt(3).map { it.symbol })
    }

    @Test
    fun distractorPoolUsesStudiedPoolAndExcludesTarget() {
        val curriculum = Curriculum(
            listOf(
                level(1, "А", "М"),
                level(2, "X", "Y")
            )
        )

        val distractors = curriculum.distractorPool(2, "X").map { it.symbol }

        assertTrue(distractors.containsAll(listOf("А", "М", "Y")))
        assertFalse(distractors.contains("X"))
    }

    private fun level(id: Int, vararg symbols: String) = CurriculumLevel(
        id = id,
        introducedLetters = symbols.map { symbol ->
            Letter(symbol = symbol, spokenName = symbol.lowercase(), levelIntroduced = id)
        },
        questionCount = 10
    )
}
