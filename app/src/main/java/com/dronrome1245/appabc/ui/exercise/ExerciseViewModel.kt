package com.dronrome1245.appabc.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.core.audio.AudioPlayer
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.engine.AdaptiveSessionGenerator
import com.dronrome1245.appabc.domain.model.Attempt
import com.dronrome1245.appabc.domain.model.Letter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExerciseViewModel(
    private val repository: AppRepositoryImpl,
    private val audioPlayer: AudioPlayer,
    private val levelId: Int
) : ViewModel() {
    private val level = ApprovedCurriculum.curriculum.level(levelId)
    private val availableLetters = ApprovedCurriculum.curriculum.lettersAvailableAt(levelId)
    private val sessionId = UUID.randomUUID().toString()
    private val sessionLength = level.questionCount
    private var currentStep = 0
    private var startTime = 0L
    private var generator: AdaptiveSessionGenerator? = null

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val history = repository.getAttemptsForLetters(availableLetters.map { it.symbol })
            generator = AdaptiveSessionGenerator(
                availableLetters = availableLetters,
                history = history,
                sessionLength = sessionLength
            )
            nextTask()
        }
    }

    private fun nextTask() {
        if (currentStep >= sessionLength) {
            _uiState.value = ExerciseUiState.Finished(sessionId)
            return
        }
        val task = checkNotNull(generator) { "AdaptiveSessionGenerator is not initialized" }.nextTask()
        _uiState.value = ExerciseUiState.Question(task.target, task.options, currentStep + 1, sessionLength)
        startTime = System.currentTimeMillis()
        speakTarget()
    }

    fun speakTarget() {
        val state = _uiState.value
        if (state is ExerciseUiState.Question) {
            state.target.symbol.firstOrNull()?.let(audioPlayer::playLetterSound)
        }
    }

    fun onAnswer(selected: Letter) {
        val state = _uiState.value as? ExerciseUiState.Question ?: return
        val isCorrect = selected.symbol == state.target.symbol
        val responseTime = System.currentTimeMillis() - startTime
        viewModelScope.launch {
            repository.saveAttempt(
                Attempt(
                    targetLetter = state.target.symbol,
                    selectedLetter = selected.symbol,
                    isCorrect = isCorrect,
                    responseTimeMs = responseTime,
                    sessionId = sessionId,
                    levelId = levelId
                )
            )
            checkNotNull(generator).recordAnswer(state.target.symbol, selected.symbol, isCorrect)
            _uiState.value = state.copy(selectedLetter = selected, isCorrect = isCorrect)
            audioPlayer.playFeedback(isCorrect)
            delay(700)
            currentStep++
            nextTask()
        }
    }
}

sealed class ExerciseUiState {
    object Loading : ExerciseUiState()
    data class Question(
        val target: Letter,
        val options: List<Letter>,
        val currentStep: Int,
        val totalSteps: Int,
        val selectedLetter: Letter? = null,
        val isCorrect: Boolean? = null
    ) : ExerciseUiState()
    data class Finished(val sessionId: String) : ExerciseUiState()
}
