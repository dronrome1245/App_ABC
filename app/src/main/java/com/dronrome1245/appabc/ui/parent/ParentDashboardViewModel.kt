package com.dronrome1245.appabc.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.repository.ParentDashboardSnapshot
import com.dronrome1245.appabc.data.repository.ParentLetterProgress
import com.dronrome1245.appabc.data.repository.ProgressRepository
import com.dronrome1245.appabc.data.settings.SettingsRepository
import com.dronrome1245.appabc.domain.curriculum.RussianAlphabet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ParentDashboardUiState(
    val letters: List<ParentLetterProgress>,
    val completedSessions: Int,
    val overallAccuracyPercent: Int,
    val masteredLetters: Int,
    val isVoiceoverEnabled: Boolean = true,
    val isSoundEffectsEnabled: Boolean = true
)

class ParentDashboardViewModel(
    private val repository: ProgressRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState = combine(
        repository.observeParentDashboard(),
        settingsRepository.isVoiceoverEnabled,
        settingsRepository.isSoundEffectsEnabled
    ) { snapshot, voiceoverEnabled, soundEffectsEnabled ->
        toUiState(snapshot, voiceoverEnabled, soundEffectsEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = toUiState(ParentDashboardSnapshot.empty())
    )

    fun setVoiceoverEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVoiceoverEnabled(enabled) }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEffectsEnabled(enabled) }
    }

    fun resetAllProgress() {
        viewModelScope.launch { repository.resetAllProgress() }
    }

    companion object {
        internal fun toUiState(
            snapshot: ParentDashboardSnapshot,
            voiceoverEnabled: Boolean = true,
            soundEffectsEnabled: Boolean = true
        ): ParentDashboardUiState {
            val byLetter = snapshot.letters.associateBy { it.letter.uppercase() }
            return ParentDashboardUiState(
                letters = RussianAlphabet.symbols.map { symbol ->
                    byLetter[symbol] ?: ParentLetterProgress.notStarted(symbol)
                },
                completedSessions = snapshot.completedSessions,
                overallAccuracyPercent = snapshot.overallAccuracyPercent,
                masteredLetters = snapshot.masteredLetters,
                isVoiceoverEnabled = voiceoverEnabled,
                isSoundEffectsEnabled = soundEffectsEnabled
            )
        }
    }
}
