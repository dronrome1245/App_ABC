package com.dronrome1245.appabc.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Derived aggregate cache. Raw Attempt rows remain the detailed source of truth. */
@Entity(tableName = "letter_progress")
data class LetterProgressEntity(
    @PrimaryKey val letter: String,
    val attemptsCount: Int,
    val correctCount: Int,
    val lastSeenTimestamp: Long,
    val averageResponseTimeMs: Long
)
