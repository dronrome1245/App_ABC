package com.dronrome1245.appabc.ui.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal const val CELEBRATION_PARTICLE_COUNT = 36
internal const val CELEBRATION_DURATION_MILLIS = 2_800

private data class CelebrationParticle(
    val angleRadians: Double,
    val speed: Float,
    val sizeScale: Float,
    val spinDegrees: Float,
    val colorIndex: Int,
    val isCircle: Boolean
)

private val celebrationPalette = listOf(
    Color(0xFFE91E63),
    Color(0xFFFFC107),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFF9C27B0),
    Color(0xFFFF7043)
)

@Composable
internal fun CelebrationConfetti(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var isRunning by remember { mutableStateOf(true) }
    val particles = remember { createCelebrationParticles(CELEBRATION_PARTICLE_COUNT) }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        isRunning = true
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = CELEBRATION_DURATION_MILLIS,
                easing = LinearEasing
            )
        )
        isRunning = false
    }

    if (!isRunning) return

    Canvas(modifier = modifier.testTag("celebration-confetti")) {
        val progressValue = progress.value
        val fadeAlpha = if (progressValue < 0.72f) {
            1f
        } else {
            (1f - (progressValue - 0.72f) / 0.28f).coerceIn(0f, 1f)
        }
        val origin = Offset(size.width / 2f, size.height * 0.42f)
        val horizontalTravel = size.width * 0.52f
        val verticalTravel = size.height * 0.36f
        val gravity = size.height * 0.68f
        val baseParticleSize = (size.width.coerceAtMost(size.height) * 0.018f).coerceAtLeast(5f)

        particles.forEach { particle ->
            val travel = particle.speed * progressValue
            val x = origin.x + cos(particle.angleRadians).toFloat() * horizontalTravel * travel
            val y = origin.y +
                sin(particle.angleRadians).toFloat() * verticalTravel * travel +
                gravity * progressValue * progressValue
            val particleSize = baseParticleSize * particle.sizeScale
            val color = celebrationPalette[particle.colorIndex].copy(alpha = fadeAlpha)

            if (particle.isCircle) {
                drawCircle(
                    color = color,
                    radius = particleSize * 0.55f,
                    center = Offset(x, y)
                )
            } else {
                rotate(
                    degrees = particle.spinDegrees * progressValue,
                    pivot = Offset(x, y)
                ) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x - particleSize * 0.7f, y - particleSize * 0.3f),
                        size = Size(particleSize * 1.4f, particleSize * 0.6f)
                    )
                }
            }
        }
    }
}

private fun createCelebrationParticles(count: Int): List<CelebrationParticle> {
    val random = Random(20260821)
    return List(count) { index ->
        CelebrationParticle(
            angleRadians = -2.75 + random.nextDouble() * 2.36,
            speed = 0.68f + random.nextFloat() * 0.48f,
            sizeScale = 0.75f + random.nextFloat() * 0.75f,
            spinDegrees = 180f + random.nextFloat() * 540f,
            colorIndex = index % celebrationPalette.size,
            isCircle = index % 3 == 0
        )
    }
}
