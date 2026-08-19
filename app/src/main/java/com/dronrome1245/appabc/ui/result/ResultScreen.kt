package com.dronrome1245.appabc.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    onRestart: () -> Unit,
    viewModel: ResultViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ResultUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ResultUiState.Success -> {
            ResultContent(state, onRestart)
        }
    }
}

@Composable
fun ResultContent(
    state: ResultUiState.Success,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Результат", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Правильно: ${state.correctAnswers} из ${state.totalAnswers}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Точность: ${state.accuracy}%",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart) {
            Text(text = "На главную")
        }
    }
}
