package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.jarvis.assistant.domain.model.AiState
import kotlin.math.cos
import kotlin.math.sin

private val Cyan = Color(0xFF34E8FF)
private val Orange = Color(0xFFFF8A34)
private val Red = Color(0xFFFF4040)

/**
 * The holographic AI face / core. Everything it draws is a pure function of [state]
 * plus a continuously running "idle" animator — no state plays without being tied to
 * a real AiState value.
 */
@Composable
fun AiFace(state: AiState, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "jarvis-core")

    // Base rotation: always running, speed depends on state.
    val rotationSpeedMs = when (state) {
        is AiState.Thinking -> 1400
        is AiState.Listening -> 2200
        is AiState.Activating -> 900
        else -> 6000
    }
    val rotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(rotationSpeedMs, easing = LinearEasing)),
        label = "rotation",
    )

    // Core pulse: amplitude/speed changes per state.
    val pulse by infinite.animateFloat(
        initialValue = 0.85f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (state is AiState.Thinking) 450 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val coreColor = when (state) {
        is AiState.Error -> Red
        is AiState.Thinking -> Orange
        else -> Orange.copy(alpha = 0.85f)
    }
    val ringColor = if (state is AiState.Error) Red else Cyan

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // Outer scanning ring — brighter + faster when Listening.
        val ringAlpha = when (state) {
            is AiState.Listening -> 1f
            is AiState.Error -> 0.9f
            else -> 0.45f
        }
        rotate(degrees = rotation, pivot = center) {
            drawCircle(
                color = ringColor.copy(alpha = ringAlpha),
                radius = maxRadius * 0.92f,
                center = center,
                style = Stroke(width = 6f),
            )
        }

        // Mid ring, counter-rotating — this is what "particles rotate while thinking" maps to.
        rotate(degrees = -rotation * 1.6f, pivot = center) {
            drawCircle(
                color = Cyan.copy(alpha = 0.3f),
                radius = maxRadius * 0.68f,
                center = center,
                style = Stroke(width = 3f),
            )
        }

        // Neural core — pulses orange; this is the "AI face" anchor point in Phase 1 scaffolding.
        // Replace this placeholder disc with your reference-video face art/geometry in Phase 3.
        val coreRadius = maxRadius * 0.32f * pulse
        drawCircle(color = coreColor.copy(alpha = 0.9f), radius = coreRadius, center = center)
        drawCircle(color = coreColor.copy(alpha = 0.25f), radius = coreRadius * 1.6f, center = center)

        // Speaking waveform: amplitude comes straight from real TTS progress, not a random timer.
        if (state is AiState.Speaking) {
            val amp = (0.2f + state.amplitude * 0.8f) * maxRadius * 0.5f
            val points = 48
            for (i in 0 until points) {
                val angle = (2 * Math.PI * i / points).toFloat()
                val wobble = sin(angle * 5 + rotation) * amp
                val r = maxRadius * 0.5f + wobble
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                drawCircle(color = Cyan.copy(alpha = 0.6f), radius = 2.5f, center = Offset(x, y))
            }
        }

        // Error state: subtle red flash ring per spec — no other animation hijacked.
        if (state is AiState.Error) {
            drawCircle(
                color = Red.copy(alpha = 0.5f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 4f),
            )
        }
    }
}
