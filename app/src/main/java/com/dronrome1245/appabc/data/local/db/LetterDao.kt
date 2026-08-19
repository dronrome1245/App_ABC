package com.dronrome1245.appabc.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLetters(letters: List<LetterEntity>)

    @Query("SELECT * FROM letters WHERE levelIntroduced <= :levelId")
    fun getLettersForLevel(levelId: Int): Flow<List<LetterEntity>>
    
    @Query("SELECT * FROM letters")
    suspend fun getAllLetters(): List<LetterEntity>
}
