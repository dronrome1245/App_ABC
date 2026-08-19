package com.dronrome1245.appabc.domain.model

import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.learning.LearningPolicyConfig
import java.time.Instant

/**
 * Represents a single user attempt at identifying a letter.
 */
data class Attempt(
    val id: Long = 0,
    val targetLetter: String,
    val selectedLetter: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Instant = Instant.now(),
    val sessionId: String,
    val levelId: Int,
    val learningPolicyVersion: Int = LearningPolicyConfig.VERSION,
    val curriculumVersion: Int = ApprovedCurriculum.VERSION
)
