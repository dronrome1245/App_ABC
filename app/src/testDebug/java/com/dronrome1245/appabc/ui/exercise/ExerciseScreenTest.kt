package com.dronrome1245.appabc.ui.exercise

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.dronrome1245.appabc.domain.model.Letter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExerciseScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun answerCard_scaleTargetsUsePressedAndRestingValues() {
        assertEquals(0.95f, answerCardScaleTarget(isPressed = true), 0.0001f)
        assertEquals(1.0f, answerCardScaleTarget(isPressed = false), 0.0001f)
    }

    @Test
    fun answerCard_remainsClickableWithPressAnimation() {
        var clickCount = 0
        val letter = Letter(symbol = "А", spokenName = "а", levelIntroduced = 1)

        composeRule.setContent {
            MaterialTheme {
                LetterCard(
                    letter = letter,
                    isSelected = false,
                    isCorrect = null,
                    onClick = { clickCount += 1 }
                )
            }
        }

        composeRule.onNodeWithTag("answer-card-А")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, clickCount)
    }
}
