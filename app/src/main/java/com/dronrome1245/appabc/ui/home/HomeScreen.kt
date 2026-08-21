package com.dronrome1245.appabc.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dronrome1245.appabc.ui.parent.ParentalGateDialog

private const val TOTAL_LEVELS = 8
private const val LEVELS_PER_ROW = 4

@Composable
fun HomeScreen(
    onStartClick: (Int) -> Unit,
    onParentClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showParentalGate by remember { mutableStateOf(false) }

    HomeContent(
        state = state,
        onLevelSelected = viewModel::selectLevel,
        onStartClick = onStartClick,
        onParentButtonClick = { showParentalGate = true }
    )

    if (showParentalGate) {
        ParentalGateDialog(
            onDismiss = { showParentalGate = false },
            onUnlocked = {
                showParentalGate = false
                onParentClick()
            }
        )
    }
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onLevelSelected: (Int) -> Unit,
    onStartClick: (Int) -> Unit,
    onParentButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("home-content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Буквы", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))

        LevelSelectorGrid(
            selectedLevel = state.selectedLevel,
            highestUnlockedLevel = state.highestUnlockedLevel,
            onLevelSelected = onLevelSelected
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Уровень ${state.selectedLevel}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Изучаем: ${state.availableLetters.joinToString { it.symbol }}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = { onStartClick(state.selectedLevel) }) {
            Text(text = "Начать тренировку")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onParentButtonClick) {
            Text("Родителям ⚙️")
        }
    }
}

@Composable
private fun LevelSelectorGrid(
    selectedLevel: Int,
    highestUnlockedLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..TOTAL_LEVELS).chunked(LEVELS_PER_ROW).forEach { rowLevels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLevels.forEach { levelId ->
                    val isUnlocked = levelId <= highestUnlockedLevel
                    val isSelected = levelId == selectedLevel
                    val isCompleted = levelId < highestUnlockedLevel
                    val label = buildString {
                        append(levelId)
                        if (isCompleted) append(" ⭐")
                        if (!isUnlocked) append(" 🔒")
                    }
                    val modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .testTag("level-$levelId")
                    val shape = RoundedCornerShape(16.dp)
                    val contentPadding = PaddingValues(0.dp)

                    if (isSelected) {
                        Button(
                            onClick = { onLevelSelected(levelId) },
                            enabled = isUnlocked,
                            modifier = modifier,
                            shape = shape,
                            contentPadding = contentPadding
                        ) {
                            Text(label, style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onLevelSelected(levelId) },
                            enabled = isUnlocked,
                            modifier = modifier,
                            shape = shape,
                            contentPadding = contentPadding
                        ) {
                            Text(label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
