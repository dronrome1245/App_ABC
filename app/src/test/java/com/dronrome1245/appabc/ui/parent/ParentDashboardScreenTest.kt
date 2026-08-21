package com.dronrome1245.appabc.ui.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ParentDashboardScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsSwitchesAndSafeResetDialogAreInteractive() {
        var voiceoverValue: Boolean? = null
        var soundEffectsValue: Boolean? = null
        var resetCalls = 0
        val state = ParentDashboardUiState(
            letters = emptyList(),
            completedSessions = 0,
            overallAccuracyPercent = 0,
            masteredLetters = 0,
            isVoiceoverEnabled = false,
            isSoundEffectsEnabled = true
        )

        composeRule.setContent {
            MaterialTheme {
                ParentDashboardContent(
                    state = state,
                    onBack = {},
                    onVoiceoverChanged = { voiceoverValue = it },
                    onSoundEffectsChanged = { soundEffectsValue = it },
                    onResetAllProgress = { resetCalls += 1 }
                )
            }
        }

        composeRule.onNodeWithText("Настройки").assertIsDisplayed()
        composeRule.onNodeWithTag("voiceover-switch").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("sound-effects-switch").assertIsDisplayed().performClick()
        assertTrue(voiceoverValue == true)
        assertFalse(soundEffectsValue ?: true)

        composeRule.onNodeWithTag("reset-progress-button").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Сбросить весь прогресс?").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Все данные об изученных буквах и история тренировок будут удалены. Начать заново?"
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Отмена").performClick()
        assertEquals(0, resetCalls)

        composeRule.onNodeWithTag("reset-progress-button").performClick()
        composeRule.onNodeWithText("Сбросить").performClick()
        assertEquals(1, resetCalls)
    }
}
