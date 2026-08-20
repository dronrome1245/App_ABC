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
    version = AppDatabase.SCHEMA_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attemptDao(): AttemptDao
    abstract fun letterDao(): LetterDao
    abstract fun letterProgressDao(): LetterProgressDao
    abstract fun sessionResultDao(): SessionResultDao

    companion object {
        const val SCHEMA_VERSION = 2
    }
}
