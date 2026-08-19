package com.dronrome1245.appabc.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.session.SessionProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val repository: AppRepositoryImpl,
    private val sessionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val progress = SessionProgressCalculator.calculate(
                repository.getSessionSummary(sessionId)
            )

            _uiState.value = ResultUiState.Success(
                correctAnswers = progress.correctAnswers,
                totalAnswers = progress.totalAnswers,
                accuracy = progress.accuracyPercent
            )
        }
    }
}

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(
        val correctAnswers: Int,
        val totalAnswers: Int,
        val accuracy: Int
    ) : ResultUiState()
}
