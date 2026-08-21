package com.dronrome1245.appabc.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dronrome1245.appabc.data.local.db.AppDatabase
import com.dronrome1245.appabc.data.local.db.AttemptEntity
import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.data.local.db.SessionResultEntity
import com.dronrome1245.appabc.data.progression.LevelProgressionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProgressRepositoryResetTest {
    private lateinit var database: AppDatabase
    private lateinit var progressionStore: LevelProgressionStore

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        progressionStore = LevelProgressionStore(context)
        progressionStore.resetToLevelOne()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun resetAllProgressClearsLearningTablesAndReturnsToLevelOne() = runBlocking {
        database.attemptDao().insertAttempt(
            AttemptEntity(
                targetLetter = "А",
                selectedLetter = "А",
                isCorrect = true,
                responseTimeMs = 500,
                timestamp = 1_000,
                sessionId = "session-reset",
                levelId = 4,
                learningPolicyVersion = 4,
                curriculumVersion = 3
            )
        )
        database.letterProgressDao().upsert(
            LetterProgressEntity(
                letter = "А",
                attemptsCount = 5,
                correctCount = 5,
                lastSeenTimestamp = 1_000,
                averageResponseTimeMs = 500
            )
        )
        database.sessionResultDao().upsert(
            SessionResultEntity(
                sessionId = "session-reset",
                levelId = 4,
                totalQuestions = 10,
                correctAnswers = 9,
                passed = true,
                completedAt = 1_000
            )
        )
        progressionStore.unlockAndSelect(4)

        val repository = ProgressRepository(database, progressionStore)
        repository.resetAllProgress()

        assertTrue(database.attemptDao().getAllAttempts().first().isEmpty())
        assertTrue(database.letterProgressDao().getAll().isEmpty())
        assertTrue(database.sessionResultDao().getAll().isEmpty())
        val progression = progressionStore.state.first()
        assertEquals(1, progression.highestUnlockedLevel)
        assertEquals(1, progression.selectedLevel)
    }
}
