package com.dronrome1245.appabc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.model.Letter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repository: AppRepositoryImpl) : ViewModel() {
    val availableLetters: StateFlow<List<Letter>> = repository.getLettersForLevel(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
