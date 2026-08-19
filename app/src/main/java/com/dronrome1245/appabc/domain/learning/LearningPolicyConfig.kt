package com.dronrome1245.appabc.domain.learning

object LearningPolicyConfig {
    const val VERSION = 2
    const val SESSION_QUESTION_COUNT = 10
    const val LEVEL_UNLOCK_ACCURACY_PERCENT = 80
    const val LEVEL_UNLOCK_CORRECT_ANSWERS = 8
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
