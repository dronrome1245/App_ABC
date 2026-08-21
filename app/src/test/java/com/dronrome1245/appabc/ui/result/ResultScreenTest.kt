package com.dronrome1245.appabc.ui.result

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.dronrome1245.appabc.domain.session.LetterSessionBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResultScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun passedSummary_showsVictoryStateAndCelebration() {
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            MaterialTheme {
                ResultContent(
                    state = resultState(
                        correctAnswers = 8,
                        passed = true,
                        unlockedLevel = 2
                    ),
                    onRepeatLevel = {},
                    onContinue = {}
                )
            }
        }

        composeRule.onNodeWithText("Отличная работа!").assertIsDisplayed()
        composeRule.onNodeWithText("Следующий уровень разблокирован!").assertIsDisplayed()
        composeRule.onNodeWithTag("celebration-confetti").assertIsDisplayed()
        assertEquals(36, CELEBRATION_PARTICLE_COUNT)
        assertTrue(CELEBRATION_DURATION_MILLIS in 2_500..3_000)
    }

    @Test
    fun failedSummary_showsRetryStateWithoutCelebration() {
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            MaterialTheme {
                ResultContent(
                    state = resultState(
                        correctAnswers = 7,
                        passed = false,
                        unlockedLevel = null
                    ),
                    onRepeatLevel = {},
                    onContinue = {}
                )
            }
        }

        composeRule.onNodeWithText("Попробуй ещё раз").assertIsDisplayed()
        composeRule.onNodeWithText("Для перехода нужно минимум 8 из 10").assertIsDisplayed()
        composeRule.onNodeWithTag("celebration-confetti").assertDoesNotExist()
    }

    private fun resultState(
        correctAnswers: Int,
        passed: Boolean,
        unlockedLevel: Int?
    ) = ResultUiState.Success(
        levelId = 1,
        correctAnswers = correctAnswers,
        totalAnswers = 10,
        accuracy = correctAnswers * 10,
        passed = passed,
        unlockedLevel = unlockedLevel,
        letters = listOf(
            LetterSessionBreakdown(
                letter = "А",
                attempts = 5,
                correct = if (passed) 5 else 4,
                errors = if (passed) 0 else 1,
                averageResponseTimeMs = 750
            )
        )
    )
}
