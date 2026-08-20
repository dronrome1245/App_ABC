package com.dronrome1245.appabc.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AttemptEntity::class,
        LetterEntity::class,
        LetterProgressEntity::class,
        SessionResultEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attemptDao(): AttemptDao
    abstract fun letterDao(): LetterDao
    abstract fun letterProgressDao(): LetterProgressDao
    abstract fun sessionResultDao(): SessionResultDao
}
