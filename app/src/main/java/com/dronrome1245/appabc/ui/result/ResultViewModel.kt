package com.dronrome1245.appabc.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.core.audio.AudioPlayer
import com.dronrome1245.appabc.data.progression.LevelProgressionStore
import com.dronrome1245.appabc.data.repository.ProgressRepository
import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.session.LetterSessionBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val progressRepository: ProgressRepository,
    private val progressionStore: LevelProgressionStore,
    private val audioPlayer: AudioPlayer,
    private val sessionId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init { loadSummary() }

    private fun loadSummary() {
        viewModelScope.launch {
            val summary = progressRepository.finalizeSession(sessionId)
            val nextLevel = ApprovedCurriculum.curriculum.nextLevelAfter(summary.levelId)
            val unlockedLevel = if (summary.passed && nextLevel != null) {
                progressionStore.unlockAndSelect(nextLevel.id)
                nextLevel.id
            } else null

            _uiState.value = ResultUiState.Success(
                levelId = summary.levelId,
                correctAnswers = summary.correctAnswers,
                totalAnswers = summary.totalQuestions,
                accuracy = if (summary.totalQuestions == 0) 0 else summary.correctAnswers * 100 / summary.totalQuestions,
                passed = summary.passed,
                unlockedLevel = unlockedLevel,
                letters = summary.letters
            )
            if (summary.totalQuestions > 0) audioPlayer.playLevelComplete()
        }
    }

    override fun onCleared() {
        audioPlayer.stop()
        super.onCleared()
    }
}

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(
        val levelId: Int,
        val correctAnswers: Int,
        val totalAnswers: Int,
        val accuracy: Int,
        val passed: Boolean,
        val unlockedLevel: Int? = null,
        val letters: List<LetterSessionBreakdown>
    ) : ResultUiState()
}
