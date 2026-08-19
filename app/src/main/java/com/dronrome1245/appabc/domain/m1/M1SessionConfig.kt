package com.dronrome1245.appabc.domain.m1

import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.model.Letter

/**
 * Compatibility facade for owner-approved Milestone 1 parameters.
 * Canonical letter content now lives in ApprovedCurriculum.
 */
object M1SessionConfig {
    const val LEVEL_ID = 1
    const val QUESTION_COUNT = ApprovedCurriculum.SESSION_QUESTION_COUNT
    const val MASTERY_THRESHOLD_PERCENT = 80
    const val MASTERY_CORRECT_ANSWERS = 8

    val letters: List<Letter> = ApprovedCurriculum.level1.introducedLetters
}
