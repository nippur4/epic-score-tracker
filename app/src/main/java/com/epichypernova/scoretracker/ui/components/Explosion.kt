package com.epichypernova.scoretracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(val angle: Float, val speed: Float, val color: Color, val size: Float)
private data class Star(val x: Float, val y: Float, val r: Float, val phase: Float)

private val burstColors = listOf(
    Color(0xFF2FD3F0), Color(0xFF55E6A5), Color(0xFFE24BD6), Color(0xFFFFD98A), Color(0xFFA18AF5),
)

/**
 * Looping epic explosion: expanding shockwave rings, radial particle burst and a twinkling
 * starfield. Purely decorative; drawn with Canvas so there are no assets.
 */
@Composable
fun ExplosionEffect(modifier: Modifier = Modifier) {
    val rnd = remember { Random(42) }
    val particles = remember {
        List(30) {
            Particle(
                angle = (it / 30f) * (2f * PI.toFloat()) + rnd.nextFloat() * 0.3f,
                speed = 0.55f + rnd.nextFloat() * 0.45f,
                color = burstColors[rnd.nextInt(burstColors.size)],
                size = 2.5f + rnd.nextFloat() * 3.5f,
            )
        }
    }
    val stars = remember {
        List(46) { Star(rnd.nextFloat(), rnd.nextFloat(), 0.6f + rnd.nextFloat() * 1.6f, rnd.nextFloat() * 6.28f) }
    }

    val t = rememberInfiniteTransition(label = "boom")
    val p1 by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "p1")
    val p2 by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart, initialStartOffset = androidx.compose.animation.core.StartOffset(750)), label = "p2")
    val twinkle by t.animateFloat(0f, 6.28f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart), label = "tw")

    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height * 0.42f)
        val maxR = size.minDimension * 0.55f

        // starfield
        stars.forEach { s ->
            val a = 0.25f + 0.55f * (0.5f + 0.5f * sin(twinkle + s.phase))
            drawCircle(Color.White.copy(alpha = a * 0.5f), s.r, Offset(s.x * size.width, s.y * size.height))
        }

        listOf(p1, p2).forEach { p ->
            // shockwave ring
            val ringAlpha = (1f - p).coerceIn(0f, 1f)
            drawCircle(
                color = Color(0xFF2FD3F0).copy(alpha = ringAlpha * 0.45f),
                radius = p * maxR,
                center = center,
                style = Stroke(width = (1f - p) * 6f + 1f),
            )
            // central flash
            val flash = (1f - p * 4f).coerceIn(0f, 1f)
            if (flash > 0f) drawCircle(Color.White.copy(alpha = flash * 0.8f), maxR * 0.12f * flash, center)
            // particles
            particles.forEach { part ->
                val dist = p * maxR * part.speed
                val pos = Offset(center.x + cos(part.angle) * dist, center.y + sin(part.angle) * dist)
                val a = (1f - p).coerceIn(0f, 1f)
                drawCircle(part.color.copy(alpha = a), part.size * (1f - p * 0.6f), pos)
            }
        }
    }
}
