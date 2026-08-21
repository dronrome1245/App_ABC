package com.dronrome1245.appabc.ui.exercise

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dronrome1245.appabc.domain.model.Letter

@Composable
fun ExerciseScreen(
    onFinish: (String) -> Unit,
    viewModel: ExerciseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ExerciseUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ExerciseUiState.Question -> {
            QuestionContent(state, onAnswer = viewModel::onAnswer, onRepeatClick = viewModel::speakTarget)
        }
        is ExerciseUiState.Finished -> {
            LaunchedEffect(Unit) {
                onFinish(state.sessionId)
            }
        }
    }
}

@Composable
fun QuestionContent(
    state: ExerciseUiState.Question,
    onAnswer: (Letter) -> Unit,
    onRepeatClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Вопрос ${state.currentStep} из ${state.totalSteps}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Найди букву", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onRepeatClick, modifier = Modifier.size(64.dp)) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Повторить звук", modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.options.forEach { letter ->
                LetterCard(
                    letter = letter,
                    isSelected = state.selectedLetter == letter,
                    isCorrect = if (state.selectedLetter == letter) state.isCorrect else null,
                    onClick = { if (state.selectedLetter == null) onAnswer(letter) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun LetterCard(
    letter: Letter,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected && isCorrect == true -> Color.Green
        isSelected && isCorrect == false -> Color.Red
        else -> Color.Transparent
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = answerCardScaleTarget(isPressed),
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 500f
        ),
        label = "answer-card-scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("answer-card-${letter.symbol}"),
        border = if (borderColor != Color.Transparent) BorderStroke(4.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = letter.symbol, fontSize = 64.sp, style = MaterialTheme.typography.displayLarge)
        }
    }
}

internal fun answerCardScaleTarget(isPressed: Boolean): Float = if (isPressed) 0.95f else 1f
