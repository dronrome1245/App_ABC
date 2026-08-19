package com.dronrome1245.appabc.domain.curriculum

import com.dronrome1245.appabc.domain.model.Letter

data class CurriculumLevel(
    val id: Int,
    val introducedLetters: List<Letter>,
    val questionCount: Int
)

/**
 * Pure Kotlin curriculum structure. A level introduces new letters while the studied
 * pool contains all letters from the current and earlier levels.
 */
class Curriculum(val levels: List<CurriculumLevel>) {
    private val levelsById = levels.associateBy { it.id }

    init {
        require(levels.isNotEmpty()) { "Curriculum must contain at least one level" }
        require(levels.all { it.id > 0 }) { "Level ids must be positive" }
        require(levelsById.size == levels.size) { "Level ids must be unique" }
        require(levels.all { it.questionCount > 0 }) { "Question count must be positive" }
    }

    fun level(id: Int): CurriculumLevel =
        levelsById[id] ?: throw IllegalArgumentException("Unknown curriculum level: $id")

    fun lettersAvailableAt(levelId: Int): List<Letter> = levels
        .filter { it.id <= levelId }
        .sortedBy { it.id }
        .flatMap { it.introducedLetters }
        .distinctBy { it.symbol }

    fun distractorPool(levelId: Int, targetSymbol: String): List<Letter> =
        lettersAvailableAt(levelId).filter { it.symbol != targetSymbol }

    fun nextLevelAfter(levelId: Int): CurriculumLevel? = levels
        .filter { it.id > levelId }
        .minByOrNull { it.id }
}

/** Owner-approved curriculum version introduced in M2.1. */
object ApprovedCurriculum {
    const val VERSION = 2
    const val SESSION_QUESTION_COUNT = 10

    val level1 = CurriculumLevel(
        id = 1,
        introducedLetters = listOf(
            Letter(symbol = "А", spokenName = "а", levelIntroduced = 1),
            Letter(symbol = "М", spokenName = "эм", levelIntroduced = 1)
        ),
        questionCount = SESSION_QUESTION_COUNT
    )

    val level2 = CurriculumLevel(
        id = 2,
        introducedLetters = listOf(
            Letter(symbol = "О", spokenName = "о", levelIntroduced = 2),
            Letter(symbol = "У", spokenName = "у", levelIntroduced = 2)
        ),
        questionCount = SESSION_QUESTION_COUNT
    )

    val level3 = CurriculumLevel(
        id = 3,
        introducedLetters = listOf(
            Letter(symbol = "С", spokenName = "эс", levelIntroduced = 3),
            Letter(symbol = "Н", spokenName = "эн", levelIntroduced = 3)
        ),
        questionCount = SESSION_QUESTION_COUNT
    )

    val curriculum = Curriculum(listOf(level1, level2, level3))

    fun findLetter(symbol: Char): Letter? = curriculum
        .lettersAvailableAt(level3.id)
        .firstOrNull { it.symbol == symbol.uppercaseChar().toString() }
}
