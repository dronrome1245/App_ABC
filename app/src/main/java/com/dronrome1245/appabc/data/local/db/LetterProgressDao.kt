package com.dronrome1245.appabc.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterProgressDao {
    @Upsert
    suspend fun upsert(progress: LetterProgressEntity)

    @Query("SELECT * FROM letter_progress WHERE letter = :letter LIMIT 1")
    suspend fun getByLetter(letter: String): LetterProgressEntity?

    @Query("SELECT * FROM letter_progress ORDER BY letter ASC")
    fun observeAll(): Flow<List<LetterProgressEntity>>

    @Query("SELECT * FROM letter_progress ORDER BY letter ASC")
    suspend fun getAll(): List<LetterProgressEntity>

    @Query("DELETE FROM letter_progress")
    suspend fun clearAll()
}
