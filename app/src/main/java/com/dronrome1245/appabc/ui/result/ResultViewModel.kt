package com.dronrome1245.appabc.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.model.Attempt
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
            val attempts = repository.getSessionSummary(sessionId)
            val correct = attempts.count { it.isCorrect }
            val total = attempts.size
            val accuracy = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

            _uiState.value = ResultUiState.Success(
                correctAnswers = correct,
                totalAnswers = total,
                accuracy = accuracy
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
