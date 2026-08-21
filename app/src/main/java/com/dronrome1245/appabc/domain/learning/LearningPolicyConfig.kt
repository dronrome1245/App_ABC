package com.dronrome1245.appabc.domain.learning

object LearningPolicyConfig {
    const val VERSION = 4
    const val SESSION_QUESTION_COUNT = 10
    const val LEVEL_UNLOCK_ACCURACY_PERCENT = 80
    const val LEVEL_UNLOCK_CORRECT_ANSWERS = 8

    const val RECENT_ACCURACY_WINDOW = 10
    const val INTRODUCED_MAX_ATTEMPTS = 2
    const val MASTERED_MIN_ATTEMPTS = 5
    const val MASTERED_ACCURACY_PERCENT = 85

    const val RETENTION_DECAY_MILLIS = 7L * 24 * 60 * 60 * 1000

    const val MASTERED_WEIGHT = 1.0
    const val INTRODUCED_WEIGHT = 2.0
    const val PRACTICING_BASE_WEIGHT = 2.0
    const val PRACTICING_MAX_WEIGHT = 3.0
    const val RETENTION_DECAY_WEIGHT = 2.0

    const val RETRY_MIN_SPACING = 2
    const val RETRY_MAX_SPACING = 4
    const val MAX_CONSECUTIVE_TARGETS = 2
}

class LevelUnlockPolicy(
    private val requiredQuestionCount: Int = LearningPolicyConfig.SESSION_QUESTION_COUNT,
    private val requiredAccuracyPercent: Int = LearningPolicyConfig.LEVEL_UNLOCK_ACCURACY_PERCENT
) {
    fun isNextLevelUnlocked(correctAnswers: Int, totalAnswers: Int): Boolean {
        if (totalAnswers != requiredQuestionCount || totalAnswers <= 0) return false
        if (correctAnswers !in 0..totalAnswers) return false
        return correctAnswers * 100 >= requiredAccuracyPercent * totalAnswers
    }
}
