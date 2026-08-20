package com.dronrome1245.appabc.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionResultDao {
    @Upsert
    suspend fun upsert(result: SessionResultEntity)

    @Query("SELECT * FROM session_results WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getBySessionId(sessionId: String): SessionResultEntity?

    @Query("SELECT * FROM session_results ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<SessionResultEntity>>

    @Query("SELECT * FROM session_results ORDER BY completedAt DESC")
    suspend fun getAll(): List<SessionResultEntity>
}
