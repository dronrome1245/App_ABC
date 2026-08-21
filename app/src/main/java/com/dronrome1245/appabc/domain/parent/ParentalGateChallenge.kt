package com.dronrome1245.appabc.domain.parent

import kotlin.random.Random

enum class GateOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-")
}

data class ParentalGateChallenge(
    val left: Int,
    val right: Int,
    val operation: GateOperation
) {
    val correctAnswer: Int = when (operation) {
        GateOperation.ADD -> left + right
        GateOperation.SUBTRACT -> left - right
    }

    val prompt: String get() = "$left ${operation.symbol} $right = ?"

    fun verify(answer: Int): Boolean = answer == correctAnswer

    fun answerOptions(random: Random = Random.Default): List<Int> {
        val options = linkedSetOf(correctAnswer)
        while (options.size < 4) {
            val candidate = (correctAnswer + random.nextInt(-6, 7)).coerceAtLeast(0)
            options += candidate
        }
        return options.shuffled(random)
    }

    companion object {
        fun generate(random: Random = Random.Default): ParentalGateChallenge {
            return if (random.nextBoolean()) {
                ParentalGateChallenge(
                    left = random.nextInt(2, 16),
                    right = random.nextInt(2, 16),
                    operation = GateOperation.ADD
                )
            } else {
                val first = random.nextInt(6, 21)
                val second = random.nextInt(1, first + 1)
                ParentalGateChallenge(
                    left = first,
                    right = second,
                    operation = GateOperation.SUBTRACT
                )
            }
        }
    }
}
