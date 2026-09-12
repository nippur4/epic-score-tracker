package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.epichypernova.scoretracker.R

/**
 * Counter icons. Poison and energy use the official MTG counter symbols
 * (vector drawables sourced from Scryfall's card-symbols), tinted to match the
 * app palette. Experience keeps an app-styled 4-point sparkle (Scryfall has no
 * dedicated experience symbol).
 */

@Composable
fun EnergyIcon(color: Color, size: Int = 14) {
    Icon(
        painter = painterResource(R.drawable.sym_energy),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(size.dp),
    )
}

@Composable
fun PoisonIcon(color: Color, size: Int = 14) {
    Icon(
        painter = painterResource(R.drawable.sym_poison),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(size.dp),
    )
}

@Composable
fun ExperienceIcon(color: Color, size: Int = 14) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width; val h = this.size.height
        val cx = 0.5f * w; val cy = 0.5f * h
        val outer = 0.5f; val inner = 0.17f
        val pts = listOf(
            0f to -outer, inner to -inner, outer to 0f, inner to inner,
            0f to outer, -inner to inner, -outer to 0f, -inner to -inner,
        )
        val p = Path().apply {
            pts.forEachIndexed { i, (dx, dy) ->
                val x = cx + dx * w; val y = cy + dy * h
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(p, color)
    }
}

// ---- Bottom navigation icons (minimalist, app-styled) ----

/** Juegos: a spade suit. */
@Composable
fun GamesTabIcon(color: Color, size: Int = 22) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width; val h = this.size.height
        val spade = Path().apply {
            moveTo(0.5f * w, 0.08f * h)
            cubicTo(0.86f * w, 0.30f * h, 1.02f * w, 0.56f * h, 0.5f * w, 0.72f * h)
            cubicTo(-0.02f * w, 0.56f * h, 0.14f * w, 0.30f * h, 0.5f * w, 0.08f * h)
            close()
            moveTo(0.42f * w, 0.62f * h)
            lineTo(0.30f * w, 0.92f * h)
            lineTo(0.70f * w, 0.92f * h)
            lineTo(0.58f * w, 0.62f * h)
            close()
        }
        drawPath(spade, color)
    }
}

/** Historial: a clock. */
@Composable
fun HistoryTabIcon(color: Color, size: Int = 22) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width; val h = this.size.height
        val sw = w * 0.085f
        drawCircle(color, radius = w * 0.42f, center = Offset(0.5f * w, 0.5f * h), style = Stroke(width = sw))
        drawLine(color, Offset(0.5f * w, 0.5f * h), Offset(0.5f * w, 0.24f * h), strokeWidth = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, Offset(0.5f * w, 0.5f * h), Offset(0.70f * w, 0.56f * h), strokeWidth = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

/** Jugadores: a person. */
@Composable
fun PlayersTabIcon(color: Color, size: Int = 22) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width; val h = this.size.height
        drawCircle(color, radius = w * 0.17f, center = Offset(0.5f * w, 0.28f * h))
        val body = Path().apply {
            moveTo(0.16f * w, 0.94f * h)
            cubicTo(0.16f * w, 0.58f * h, 0.84f * w, 0.58f * h, 0.84f * w, 0.94f * h)
            close()
        }
        drawPath(body, color)
    }
}

/** Ajustes: a gear. */
@Composable
fun SettingsTabIcon(color: Color, size: Int = 22) {
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width; val h = this.size.height
        val cx = 0.5f * w; val cy = 0.5f * h
        val r = 0.30f * w
        drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(width = w * 0.11f))
        repeat(8) { k ->
            val a = k * (kotlin.math.PI / 4).toFloat()
            val c = kotlin.math.cos(a); val s = kotlin.math.sin(a)
            drawLine(
                color,
                Offset(cx + c * r * 1.05f, cy + s * r * 1.05f),
                Offset(cx + c * r * 1.55f, cy + s * r * 1.55f),
                strokeWidth = w * 0.11f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        drawCircle(color, radius = w * 0.10f, center = Offset(cx, cy))
    }
}

/** A single die face (rounded square + pips) used by the starter draw. */
@Composable
fun DieFace(value: Int, color: Color, pip: Color, size: Int = 56) {
    Canvas(Modifier.size(size.dp)) {
        val s = this.size.minDimension
        val r = s * 0.18f
        drawPath(
            Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        0f, 0f, s, s,
                        androidx.compose.ui.geometry.CornerRadius(r, r),
                    )
                )
            },
            color,
        )
        // border
        drawPath(
            Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        1.5f, 1.5f, s - 1.5f, s - 1.5f,
                        androidx.compose.ui.geometry.CornerRadius(r, r),
                    )
                )
            },
            pip,
            style = Stroke(width = s * 0.03f),
        )
        val pr = s * 0.085f
        fun dot(fx: Float, fy: Float) = drawCircle(pip, pr, Offset(fx * s, fy * s))
        val a = 0.28f; val b = 0.5f; val c = 0.72f
        when (value.coerceIn(1, 6)) {
            1 -> dot(b, b)
            2 -> { dot(a, a); dot(c, c) }
            3 -> { dot(a, a); dot(b, b); dot(c, c) }
            4 -> { dot(a, a); dot(c, a); dot(a, c); dot(c, c) }
            5 -> { dot(a, a); dot(c, a); dot(b, b); dot(a, c); dot(c, c) }
            6 -> { dot(a, a); dot(c, a); dot(a, b); dot(c, b); dot(a, c); dot(c, c) }
        }
    }
}
