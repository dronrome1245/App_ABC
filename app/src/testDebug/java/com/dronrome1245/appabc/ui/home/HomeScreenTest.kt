package com.dronrome1245.appabc.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.dronrome1245.appabc.domain.curriculum.ApprovedCurriculum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun levelSelector_showsAllEightLevelsInsideCompactFourByTwoGrid() {
        val state = HomeUiState(
            highestUnlockedLevel = 3,
            selectedLevel = 2,
            availableLetters = ApprovedCurriculum.curriculum.lettersAvailableAt(2)
        )

        composeRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.size(width = 360.dp, height = 800.dp)) {
                    HomeContent(
                        state = state,
                        onLevelSelected = {},
                        onStartClick = {},
                        onParentButtonClick = {}
                    )
                }
            }
        }

        val homeBounds = composeRule.onNodeWithTag("home-content").fetchSemanticsNode().boundsInRoot
        val levelBounds = (1..8).associateWith { levelId ->
            composeRule.onNodeWithTag("level-$levelId")
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
        }

        (1..3).forEach { composeRule.onNodeWithTag("level-$it").assertIsEnabled() }
        (4..8).forEach { composeRule.onNodeWithTag("level-$it").assertIsNotEnabled() }

        assertSameRow(levelBounds.getValue(1), levelBounds.getValue(4))
        assertSameRow(levelBounds.getValue(5), levelBounds.getValue(8))
        assertTrue(levelBounds.getValue(5).top > levelBounds.getValue(1).top)

        levelBounds.values.forEach { bounds ->
            assertTrue(bounds.left >= homeBounds.left)
            assertTrue(bounds.right <= homeBounds.right)
        }
    }

    private fun assertSameRow(first: Rect, last: Rect) {
        assertEquals(first.top, last.top, 0.5f)
        assertEquals(first.bottom, last.bottom, 0.5f)
    }
}
