package com.dronrome1245.appabc.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dronrome1245.appabc.domain.parent.ParentalGateChallenge

@Composable
fun ParentalGateDialog(
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit
) {
    var challenge by remember { mutableStateOf(ParentalGateChallenge.generate()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val options = remember(challenge) { challenge.answerOptions() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Для родителей") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Решите пример, чтобы открыть статистику:")
                Text(challenge.prompt)
                options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { option ->
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (challenge.verify(option)) {
                                        onUnlocked()
                                    } else {
                                        errorMessage = "Ответ не подошёл. Попробуйте новый пример."
                                        challenge = ParentalGateChallenge.generate()
                                    }
                                }
                            ) {
                                Text(option.toString())
                            }
                        }
                    }
                }
                errorMessage?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
