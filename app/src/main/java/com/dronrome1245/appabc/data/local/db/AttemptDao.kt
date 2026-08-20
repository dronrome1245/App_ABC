package com.dronrome1245.appabc.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Query("SELECT * FROM attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE sessionId = :sessionId")
    suspend fun getAttemptsBySession(sessionId: String): List<AttemptEntity>

    @Query("SELECT * FROM attempts WHERE targetLetter IN (:letters) ORDER BY timestamp ASC, id ASC")
    suspend fun getAttemptsForLetters(letters: List<String>): List<AttemptEntity>
}
