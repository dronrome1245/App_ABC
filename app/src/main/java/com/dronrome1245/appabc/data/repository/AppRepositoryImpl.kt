package com.dronrome1245.appabc.data.repository

import com.dronrome1245.appabc.data.local.db.AttemptDao
import com.dronrome1245.appabc.data.local.db.AttemptEntity
import com.dronrome1245.appabc.data.local.db.LetterDao
import com.dronrome1245.appabc.data.local.db.LetterEntity
import com.dronrome1245.appabc.domain.model.Attempt
import com.dronrome1245.appabc.domain.model.Letter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepositoryImpl(
    private val attemptDao: AttemptDao,
    private val letterDao: LetterDao
) {
    suspend fun saveAttempt(attempt: Attempt) {
        attemptDao.insertAttempt(
            AttemptEntity(
                targetLetter = attempt.targetLetter,
                selectedLetter = attempt.selectedLetter,
                isCorrect = attempt.isCorrect,
                responseTimeMs = attempt.responseTimeMs,
                timestamp = attempt.timestamp.toEpochMilli(),
                sessionId = attempt.sessionId,
                levelId = attempt.levelId,
                learningPolicyVersion = attempt.learningPolicyVersion,
                curriculumVersion = attempt.curriculumVersion
            )
        )
    }

    fun getLettersForLevel(levelId: Int): Flow<List<Letter>> {
        return letterDao.getLettersForLevel(levelId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun ensureInitialLetters() {
        val initialLetters = listOf(
            LetterEntity("А", "а", 1),
            LetterEntity("О", "о", 1)
        )
        letterDao.insertLetters(initialLetters)
    }

    suspend fun getSessionSummary(sessionId: String): List<Attempt> {
        return attemptDao.getAttemptsBySession(sessionId).map { it.toDomain() }
    }

    private fun LetterEntity.toDomain() = Letter(symbol, spokenName, levelIntroduced)
    private fun AttemptEntity.toDomain() = Attempt(
        id = id,
        targetLetter = targetLetter,
        selectedLetter = selectedLetter,
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs,
        timestamp = java.time.Instant.ofEpochMilli(timestamp),
        sessionId = sessionId,
        levelId = levelId,
        learningPolicyVersion = learningPolicyVersion,
        curriculumVersion = curriculumVersion
    )
}
