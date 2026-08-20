package com.dronrome1245.appabc.domain.learning

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelUnlockPolicyTest {

    private val policy = LevelUnlockPolicy()

    @Test
    fun sevenOfTenDoesNotUnlockNextLevel() {
        assertFalse(policy.isNextLevelUnlocked(correctAnswers = 7, totalAnswers = 10))
    }

    @Test
    fun eightOfTenUnlocksNextLevel() {
        assertTrue(policy.isNextLevelUnlocked(correctAnswers = 8, totalAnswers = 10))
    }

    @Test
    fun tenOfTenUnlocksNextLevel() {
        assertTrue(policy.isNextLevelUnlocked(correctAnswers = 10, totalAnswers = 10))
    }

    @Test
    fun incompleteSessionDoesNotUnlockEvenAtHighPercentage() {
        assertFalse(policy.isNextLevelUnlocked(correctAnswers = 4, totalAnswers = 5))
    }
}
