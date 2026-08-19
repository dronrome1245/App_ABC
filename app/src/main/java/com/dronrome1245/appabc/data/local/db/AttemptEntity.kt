package com.dronrome1245.appabc.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetLetter: String,
    val selectedLetter: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Long,
    val sessionId: String,
    val levelId: Int,
    val learningPolicyVersion: Int,
    val curriculumVersion: Int
)
