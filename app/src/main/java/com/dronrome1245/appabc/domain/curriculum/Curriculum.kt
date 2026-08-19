package com.dronrome1245.appabc.domain.curriculum

import com.dronrome1245.appabc.domain.model.Letter

data class CurriculumLevel(
    val id: Int,
    val introducedLetters: List<Letter>,
    val questionCount: Int
)

/**
 * Pure Kotlin curriculum structure. It supports an arbitrary number of levels without
 * hard-coding product decisions about future letter order or unlock thresholds.
 */
class Curriculum(levels: List<CurriculumLevel>) {
    private val levelsById = levels.associateBy { it.id }

    init {
        require(levels.isNotEmpty()) { "Curriculum must contain at least one level" }
        require(levels.all { it.id > 0 }) { "Level ids must be positive" }
        require(levelsById.size == levels.size) { "Level ids must be unique" }
        require(levels.all { it.questionCount > 0 }) { "Question count must be positive" }
    }

    fun level(id: Int): CurriculumLevel =
        levelsById[id] ?: throw IllegalArgumentException("Unknown curriculum level: $id")

    fun lettersAvailableAt(levelId: Int): List<Letter> = levelsById.values
        .filter { it.id <= levelId }
        .sortedBy { it.id }
        .flatMap { it.introducedLetters }
        .distinctBy { it.symbol }

    fun distractorPool(levelId: Int, targetSymbol: String): List<Letter> =
        lettersAvailableAt(levelId).filter { it.symbol != targetSymbol }
}

/**
 * Only owner-approved curriculum is populated here. Future levels remain absent until
 * their letter composition has an explicit decision source.
 */
object ApprovedCurriculum {
    const val VERSION = 1
    const val SESSION_QUESTION_COUNT = 10

    val level1 = CurriculumLevel(
        id = 1,
        introducedLetters = listOf(
            Letter(symbol = "А", spokenName = "а", levelIntroduced = 1),
            Letter(symbol = "М", spokenName = "эм", levelIntroduced = 1)
        ),
        questionCount = SESSION_QUESTION_COUNT
    )

    val curriculum = Curriculum(listOf(level1))

    fun findLetter(symbol: Char): Letter? = curriculum
        .lettersAvailableAt(level1.id)
        .firstOrNull { it.symbol == symbol.uppercaseChar().toString() }
}
