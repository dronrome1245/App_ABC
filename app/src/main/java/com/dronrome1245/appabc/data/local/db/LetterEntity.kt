package com.dronrome1245.appabc.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letters")
data class LetterEntity(
    @PrimaryKey val symbol: String,
    val spokenName: String,
    val levelIntroduced: Int
)
