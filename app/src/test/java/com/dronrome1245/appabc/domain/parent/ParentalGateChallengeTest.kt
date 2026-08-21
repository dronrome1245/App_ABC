package com.dronrome1245.appabc.domain.parent

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentalGateChallengeTest {
    @Test
    fun generatedChallengesNeverHaveNegativeAnswer() {
        val random = Random(42)
        repeat(500) {
            assertTrue(ParentalGateChallenge.generate(random).correctAnswer >= 0)
        }
    }

    @Test
    fun verifyAcceptsOnlyCorrectAnswer() {
        val challenge = ParentalGateChallenge(15, 4, GateOperation.SUBTRACT)

        assertTrue(challenge.verify(11))
        assertFalse(challenge.verify(10))
        assertFalse(challenge.verify(12))
    }

    @Test
    fun answerOptionsContainCorrectAnswerAndAreUnique() {
        val challenge = ParentalGateChallenge(7, 6, GateOperation.ADD)
        val options = challenge.answerOptions(Random(7))

        assertEquals(4, options.size)
        assertEquals(4, options.toSet().size)
        assertTrue(13 in options)
        assertTrue(options.all { it >= 0 })
    }
}
