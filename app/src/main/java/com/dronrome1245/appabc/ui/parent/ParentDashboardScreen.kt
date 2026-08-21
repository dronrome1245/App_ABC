package com.dronrome1245.appabc.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dronrome1245.appabc.data.repository.ParentLetterProgress
import com.dronrome1245.appabc.data.repository.ParentLetterStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentDashboardScreen(
    onBack: () -> Unit,
    viewModel: ParentDashboardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    ParentDashboardContent(
        state = state,
        onBack = onBack,
        onVoiceoverChanged = viewModel::setVoiceoverEnabled,
        onSoundEffectsChanged = viewModel::setSoundEffectsEnabled,
        onResetAllProgress = viewModel::resetAllProgress
    )
}

@Composable
internal fun ParentDashboardContent(
    state: ParentDashboardUiState,
    onBack: () -> Unit,
    onVoiceoverChanged: (Boolean) -> Unit,
    onSoundEffectsChanged: (Boolean) -> Unit,
    onResetAllProgress: () -> Unit
) {
    var selectedLetter by remember { mutableStateOf<ParentLetterProgress?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Прогресс", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack) { Text("Назад к тренировкам") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("Освоено", "${state.masteredLetters} из 33", Modifier.weight(1f))
            SummaryCard("Тренировок", state.completedSessions.toString(), Modifier.weight(1f))
            SummaryCard("Точность", "${state.overallAccuracyPercent}%", Modifier.weight(1f))
        }

        Text("Настройки", style = MaterialTheme.typography.titleLarge)
        SettingRow(
            title = "Озвучка букв",
            checked = state.isVoiceoverEnabled,
            onCheckedChange = onVoiceoverChanged,
            testTag = "voiceover-switch"
        )
        SettingRow(
            title = "Звуковые эффекты",
            checked = state.isSoundEffectsEnabled,
            onCheckedChange = onSoundEffectsChanged,
            testTag = "sound-effects-switch"
        )

        Text("Управление данными", style = MaterialTheme.typography.titleLarge)
        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier.testTag("reset-progress-button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Сбросить весь прогресс")
        }

        Text("Все буквы", style = MaterialTheme.typography.titleLarge)

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.letters, key = { it.letter }) { letter ->
                LetterTile(letter = letter, onClick = { selectedLetter = letter })
            }
        }
    }

    selectedLetter?.let { letter ->
        AlertDialog(
            onDismissRequest = { selectedLetter = null },
            title = { Text("Буква ${letter.letter}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Статус: ${statusLabel(letter.status)}")
                    if (letter.requiresReview) {
                        Text("Требует повторения", fontWeight = FontWeight.Bold)
                        letter.lastSeenTimestamp?.let { timestamp ->
                            Text("Последняя тренировка: ${formatLastTrainingDate(timestamp)}")
                        }
                    }
                    Text("Показов: ${letter.attemptsCount}")
                    Text("Правильно: ${letter.correctCount}")
                    Text("Точность: ${letter.accuracyPercent}%")
                    if (letter.attemptsCount > 0) {
                        Text("Среднее время ответа: ${letter.averageResponseTimeMs} мс")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedLetter = null }) { Text("Закрыть") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить весь прогресс?") },
            text = {
                Text("Все данные об изученных буквах и история тренировок будут удалены. Начать заново?")
            },
            dismissButton = {
                Button(onClick = { showResetDialog = false }) { Text("Отмена") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetAllProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Сбросить")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LetterTile(letter: ParentLetterProgress, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(statusColor(letter.status), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(letter.letter, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun statusLabel(status: ParentLetterStatus): String = when (status) {
    ParentLetterStatus.NOT_STARTED -> "Не начато"
    ParentLetterStatus.INTRODUCED -> "Знакомство"
    ParentLetterStatus.PRACTICING -> "В процессе"
    ParentLetterStatus.MASTERED -> "Освоено"
}

private fun statusColor(status: ParentLetterStatus): Color = when (status) {
    ParentLetterStatus.NOT_STARTED -> Color(0xFFE0E0E0)
    ParentLetterStatus.INTRODUCED -> Color(0xFFFFE082)
    ParentLetterStatus.PRACTICING -> Color(0xFF90CAF9)
    ParentLetterStatus.MASTERED -> Color(0xFFA5D6A7)
}

private fun formatLastTrainingDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
