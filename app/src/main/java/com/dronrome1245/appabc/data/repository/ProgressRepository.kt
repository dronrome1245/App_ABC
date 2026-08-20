package com.dronrome1245.appabc.data.repository

import androidx.room.withTransaction
import com.dronrome1245.appabc.data.local.db.AppDatabase
import com.dronrome1245.appabc.data.local.db.AttemptEntity
import com.dronrome1245.appabc.data.local.db.LetterProgressEntity
import com.dronrome1245.appabc.data.local.db.SessionResultEntity
import com.dronrome1245.appabc.domain.learning.LevelUnlockPolicy
import com.dronrome1245.appabc.domain.model.Attempt
import com.dronrome1245.appabc.domain.session.LetterProgressAccumulator
import com.dronrome1245.appabc.domain.session.LetterSessionBreakdown
import com.dronrome1245.appabc.domain.session.SessionLetterBreakdownCalculator
import com.dronrome1245.appabc.domain.session.SessionProgressCalculator
import kotlinx.coroutines.flow.Flow
import java.time.Instant

data class PersistedSessionSummary(
    val sessionId: String,
    val levelId: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val passed: Boolean,
    val completedAt: Long,
    val letters: List<LetterSessionBreakdown>
)

/**
 * Persists derived progress while keeping Attempt rows as the detailed source of truth.
 * Finalization is idempotent by unique sessionId, so recreating ResultScreen cannot double-count progress.
 */
class ProgressRepository(private val database: AppDatabase) {
    private val attemptDao = database.attemptDao()
    private val letterProgressDao = database.letterProgressDao()
    private val sessionResultDao = database.sessionResultDao()

    suspend fun finalizeSession(sessionId: String): PersistedSessionSummary = database.withTransaction {
        val attempts = attemptDao.getAttemptsBySession(sessionId).map { it.toDomain() }
        require(attempts.isNotEmpty()) { "Session $sessionId has no attempts" }

        val existingResult = sessionResultDao.getBySessionId(sessionId)
        if (existingResult == null) {
            attempts.groupBy { it.targetLetter }.forEach { (letter, letterAttempts) ->
                val current = letterProgressDao.getByLetter(letter)
                letterProgressDao.upsert(LetterProgressAccumulator.accumulate(current, letterAttempts))
            }

            val progress = SessionProgressCalculator.calculate(attempts)
            val completedAt = attempts.maxOf { it.timestamp.toEpochMilli() }
            sessionResultDao.upsert(
                SessionResultEntity(
                    sessionId = sessionId,
                    levelId = attempts.first().levelId,
                    totalQuestions = progress.totalAnswers,
                    correctAnswers = progress.correctAnswers,
                    passed = LevelUnlockPolicy().isNextLevelUnlocked(
                        correctAnswers = progress.correctAnswers,
                        totalAnswers = progress.totalAnswers
                    ),
                    completedAt = completedAt
                )
            )
        }

        buildSummary(
            result = sessionResultDao.getBySessionId(sessionId)
                ?: error("Session result was not persisted for $sessionId"),
            attempts = attempts
        )
    }

    fun observeLetterProgress(): Flow<List<LetterProgressEntity>> = letterProgressDao.observeAll()

    suspend fun getLetterProgress(): List<LetterProgressEntity> = letterProgressDao.getAll()

    fun observeSessionHistory(): Flow<List<SessionResultEntity>> = sessionResultDao.observeAll()

    suspend fun getSessionHistory(): List<SessionResultEntity> = sessionResultDao.getAll()

    private fun buildSummary(
        result: SessionResultEntity,
        attempts: List<Attempt>
    ) = PersistedSessionSummary(
        sessionId = result.sessionId,
        levelId = result.levelId,
        totalQuestions = result.totalQuestions,
        correctAnswers = result.correctAnswers,
        passed = result.passed,
        completedAt = result.completedAt,
        letters = SessionLetterBreakdownCalculator.calculate(attempts)
    )

    private fun AttemptEntity.toDomain() = Attempt(
        id = id,
        targetLetter = targetLetter,
        selectedLetter = selectedLetter,
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs,
        timestamp = Instant.ofEpochMilli(timestamp),
        sessionId = sessionId,
        levelId = levelId,
        learningPolicyVersion = learningPolicyVersion,
        curriculumVersion = curriculumVersion
    )
}
