package com.dronrome1245.appabc.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dronrome1245.appabc.domain.session.LetterSessionBreakdown

@Composable
fun ResultScreen(
    onRepeatLevel: (Int) -> Unit,
    onContinue: () -> Unit,
    viewModel: ResultViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ResultUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ResultUiState.Success -> ResultContent(state, onRepeatLevel, onContinue)
    }
}

@Composable
internal fun ResultContent(
    state: ResultUiState.Success,
    onRepeatLevel: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("result-content")
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "${state.correctAnswers} / ${state.totalAnswers}", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.passed) "Отличная работа!" else "Попробуй ещё раз",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "Точность: ${state.accuracy}%", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "По буквам", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            state.letters.forEach { LetterBreakdownRow(it) }

            Spacer(modifier = Modifier.height(16.dp))
            when {
                state.unlockedLevel != null -> Text(
                    text = "Следующий уровень разблокирован!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                state.passed -> Text(
                    text = "Уровень пройден",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                else -> Text(
                    text = "Для перехода нужно минимум 8 из 10",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onRepeatLevel(state.levelId) }) {
                Text(text = "Повторить уровень")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onContinue) {
                Text(text = if (state.unlockedLevel != null) "Далее" else "К выбору уровней")
            }
        }

        if (state.passed) {
            CelebrationConfetti(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun LetterBreakdownRow(item: LetterSessionBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.letter, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = if (item.errors == 0) {
                "${item.correct}/${item.attempts} верно"
            } else {
                "${item.correct}/${item.attempts} — были ошибки"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (item.errors == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
