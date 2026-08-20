package com.dronrome1245.appabc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.progression.LevelProgressionStore
import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import com.dronrome1245.appabc.domain.model.Letter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val highestUnlockedLevel: Int,
    val selectedLevel: Int,
    val availableLetters: List<Letter>
)

class HomeViewModel(
    private val progressionStore: LevelProgressionStore
) : ViewModel() {
    private val initialState = HomeUiState(
        highestUnlockedLevel = 1,
        selectedLevel = 1,
        availableLetters = ApprovedCurriculum.curriculum.lettersAvailableAt(1)
    )

    val uiState: StateFlow<HomeUiState> = progressionStore.state
        .map { progression ->
            HomeUiState(
                highestUnlockedLevel = progression.highestUnlockedLevel,
                selectedLevel = progression.selectedLevel,
                availableLetters = ApprovedCurriculum.curriculum.lettersAvailableAt(progression.selectedLevel)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialState
        )

    fun selectLevel(levelId: Int) {
        viewModelScope.launch {
            progressionStore.selectLevel(levelId)
        }
    }
}
