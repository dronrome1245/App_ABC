package com.dronrome1245.appabc.domain.m1

import com.dronrome1245.appabc.domain.model.Letter

/**
 * Owner-approved Milestone 1 parameters.
 *
 * This is intentionally limited to M1 and does not implement M2 LearningPolicy rules.
 */
object M1SessionConfig {
    const val LEVEL_ID = 1
    const val QUESTION_COUNT = 10
    const val MASTERY_THRESHOLD_PERCENT = 80
    const val MASTERY_CORRECT_ANSWERS = 8

    val letters: List<Letter> = listOf(
        Letter(symbol = "А", spokenName = "а", levelIntroduced = LEVEL_ID),
        Letter(symbol = "М", spokenName = "эм", levelIntroduced = LEVEL_ID)
    )
}
