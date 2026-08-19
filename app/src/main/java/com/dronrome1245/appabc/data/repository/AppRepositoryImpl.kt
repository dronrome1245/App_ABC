package com.dronrome1245.appabc.data.repository

import com.dronrome1245.appabc.data.local.db.AttemptDao
import com.dronrome1245.appabc.data.local.db.AttemptEntity
import com.dronrome1245.appabc.data.local.db.LetterDao
import com.dronrome1245.appabc.data.local.db.LetterEntity
import com.dronrome1245.appabc.domain.m1.M1SessionConfig
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
        val levelLetters = M1SessionConfig.letters.map { letter ->
            LetterEntity(
                symbol = letter.symbol,
                spokenName = letter.spokenName,
                levelIntroduced = letter.levelIntroduced
            )
        }

        // Remove only obsolete Level 1 curriculum rows. Attempt history is stored separately and remains intact.
        // This removes the old "О" seed on devices that ran the earlier drifted M1 build.
        letterDao.deleteLettersOutsideLevelSet(
            levelId = M1SessionConfig.LEVEL_ID,
            allowedSymbols = levelLetters.map { it.symbol }
        )
        letterDao.insertLetters(levelLetters)
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
