package com.dronrome1245.appabc.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AttemptEntity::class, LetterEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attemptDao(): AttemptDao
    abstract fun letterDao(): LetterDao
}
