package com.dronrome1245.appabc.ui.home

import androidx.lifecycle.ViewModel
import com.dronrome1245.appabc.data.repository.AppRepositoryImpl
import com.dronrome1245.appabc.domain.m1.M1SessionConfig
import com.dronrome1245.appabc.domain.model.Letter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    @Suppress("UNUSED_PARAMETER") repository: AppRepositoryImpl
) : ViewModel() {
    private val _availableLetters = MutableStateFlow(M1SessionConfig.letters)
    val availableLetters: StateFlow<List<Letter>> = _availableLetters.asStateFlow()
}
