package com.dronrome1245.appabc.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.core.audio.TextToSpeechWrapper
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.engine.LearningEngine
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
    private val tts: TextToSpeechWrapper
) : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()
    private val sessionLength = 8 // Requirement: 5-8 steps
    private var currentStep = 0
    private var startTime = 0L

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private var engine: LearningEngine? = null
    private val lastTargets = mutableListOf<String>()

    init {
        loadLettersAndStart()
    }

    private fun loadLettersAndStart() {
        viewModelScope.launch {
            repository.getLettersForLevel(1).collect { letters ->
                if (letters.isNotEmpty()) {
                    engine = LearningEngine(letters)
                    nextTask()
                }
            }
        }
    }

    private fun nextTask() {
        if (currentStep >= sessionLength) {
            _uiState.value = ExerciseUiState.Finished(sessionId)
            return
        }

        val task = engine?.nextTask(lastTargets) ?: return
        lastTargets.add(task.target.symbol)
        _uiState.value = ExerciseUiState.Question(
            target = task.target,
            options = task.options,
            currentStep = currentStep + 1,
            totalSteps = sessionLength
        )
        startTime = System.currentTimeMillis()
        speakTarget()
    }

    fun speakTarget() {
        val state = _uiState.value
        if (state is ExerciseUiState.Question) {
            tts.speak(state.target.spokenName)
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
                    levelId = 1
                )
            )

            _uiState.value = state.copy(
                selectedLetter = selected,
                isCorrect = isCorrect
            )

            delay(700) // Delay for feedback
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
