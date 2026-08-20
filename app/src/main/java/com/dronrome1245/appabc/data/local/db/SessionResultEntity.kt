package com.dronrome1245.appabc.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_results",
    indices = [Index(value = ["sessionId"], unique = true)]
)
data class SessionResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val levelId: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val passed: Boolean,
    val completedAt: Long
)
