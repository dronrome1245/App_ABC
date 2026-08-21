package com.dronrome1245.appabc.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.repository.ParentDashboardSnapshot
import com.dronrome1245.appabc.data.repository.ParentLetterProgress
import com.dronrome1245.appabc.data.repository.ProgressRepository
import com.dronrome1245.appabc.domain.curriculum.RussianAlphabet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ParentDashboardUiState(
    val letters: List<ParentLetterProgress>,
    val completedSessions: Int,
    val overallAccuracyPercent: Int,
    val masteredLetters: Int
)

class ParentDashboardViewModel(repository: ProgressRepository) : ViewModel() {
    val uiState = repository.observeParentDashboard()
        .map(::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toUiState(ParentDashboardSnapshot.empty())
        )

    companion object {
        internal fun toUiState(snapshot: ParentDashboardSnapshot): ParentDashboardUiState {
            val byLetter = snapshot.letters.associateBy { it.letter.uppercase() }
            return ParentDashboardUiState(
                letters = RussianAlphabet.symbols.map { symbol ->
                    byLetter[symbol] ?: ParentLetterProgress.notStarted(symbol)
                },
                completedSessions = snapshot.completedSessions,
                overallAccuracyPercent = snapshot.overallAccuracyPercent,
                masteredLetters = snapshot.masteredLetters
            )
        }
    }
}
