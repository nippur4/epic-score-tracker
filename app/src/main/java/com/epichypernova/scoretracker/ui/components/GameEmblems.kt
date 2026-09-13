package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.epichypernova.scoretracker.data.model.GameType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Original, minimalist per-game emblems drawn with Canvas (no trademarked logos or
 * copyrighted characters — just generic objects that evoke each game's category).
 *
 * [GameLogoBox] resolves a per-game image in priority order:
 *   1. a drawable named `logo_<gametype>` (png / webp / vector xml), then
 *   2. an SVG at `assets/logos/<gametype>.svg` (rendered via Coil's SVG decoder), then
 *   3. the built-in vector emblem.
 * So official art can be dropped in later with no code changes.
 */

@Composable
fun GameLogoBox(
    type: GameType,
    tint: Color,
    boxSize: Int = 42,
    radius: Int = 13,
    emblemSize: Int = 24,
) {
    val ctx = LocalContext.current
    val key = type.name.lowercase()
    val resId = remember(type) { ctx.resources.getIdentifier("logo_$key", "drawable", ctx.packageName) }
    val svgAsset = remember(type) {
        runCatching { ctx.assets.list("logos")?.contains("$key.svg") == true }.getOrDefault(false)
    }
    when {
        resId != 0 -> Box(Modifier.size(boxSize.dp).clip(RoundedCornerShape(radius.dp)), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(4.dp),
            )
        }
        svgAsset -> {
            val model = remember(type) {
                ImageRequest.Builder(ctx)
                    .data("file:///android_asset/logos/$key.svg")
                    .decoderFactory(SvgDecoder.Factory())
                    .build()
            }
            Box(Modifier.size(boxSize.dp).clip(RoundedCornerShape(radius.dp)), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            }
        }
        else -> Box(
            Modifier.size(boxSize.dp)
                .clip(RoundedCornerShape(radius.dp))
                .background(tint.copy(alpha = 0.16f))
                .border(1.dp, tint.copy(alpha = 0.30f), RoundedCornerShape(radius.dp)),
            contentAlignment = Alignment.Center,
        ) { GameEmblem(type, tint, emblemSize) }
    }
}

@Composable
fun GameEmblem(type: GameType, tint: Color, size: Int = 24) {
    Canvas(Modifier.size(size.dp)) {
        when (type) {
            GameType.MAGIC -> manaPentagon(tint)
            GameType.POKEMON -> pawPrint(tint)
            GameType.YUGIOH -> tradingCard(tint)
            GameType.DIGIMON -> egg(tint)
            GameType.LORCANA -> inkDrop(tint)
            GameType.ONEPIECE -> anchor(tint)
            GameType.TRUCO -> club(tint)
            GameType.CHINCHON -> cardFan(tint)
            GameType.BURAKO -> tiles(tint)
            GameType.DARTS -> dartboard(tint)
            else -> Unit
        }
    }
}

// ---- individual emblems (unit box: w = h = size) ----

private fun DrawScope.manaPentagon(c: Color) {
    val w = size.width; val h = size.height
    val cx = 0.5f * w; val cy = 0.52f * h; val r = 0.4f * w
    val pts = (0 until 5).map {
        val a = (-90f + it * 72f) * PI.toFloat() / 180f
        Offset(cx + r * cos(a), cy + r * sin(a))
    }
    for (i in 0 until 5) {
        drawLine(c.copy(alpha = 0.5f), pts[i], pts[(i + 1) % 5], strokeWidth = w * 0.045f, cap = StrokeCap.Round)
    }
    pts.forEach { drawCircle(c, radius = w * 0.095f, center = it) }
}

private fun DrawScope.pawPrint(c: Color) {
    val w = size.width; val h = size.height
    // main pad
    drawPath(Path().apply {
        addRoundRect(RoundRect(0.28f * w, 0.5f * h, 0.72f * w, 0.86f * h, CornerRadius(0.22f * w, 0.2f * h)))
    }, c)
    // toes
    val toes = listOf(0.30f to 0.34f, 0.46f to 0.26f, 0.62f to 0.28f, 0.74f to 0.4f)
    toes.forEachIndexed { i, (fx, fy) ->
        val rr = if (i == 0 || i == 3) 0.075f else 0.085f
        drawCircle(c, radius = rr * w, center = Offset(fx * w, fy * h))
    }
}

private fun DrawScope.tradingCard(c: Color) {
    val w = size.width; val h = size.height
    val sw = w * 0.06f
    drawPath(Path().apply {
        addRoundRect(RoundRect(0.28f * w, 0.14f * h, 0.72f * w, 0.86f * h, CornerRadius(0.07f * w, 0.07f * w)))
    }, c, style = Stroke(width = sw))
    // little star in the middle
    star(Offset(0.5f * w, 0.46f * h), outer = 0.13f * w, inner = 0.055f * w, c)
    drawLine(c, Offset(0.36f * w, 0.68f * h), Offset(0.64f * w, 0.68f * h), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
}

private fun DrawScope.egg(c: Color) {
    val w = size.width; val h = size.height
    val sw = w * 0.06f
    // egg outline: narrower on top, rounder at bottom
    val p = Path().apply {
        moveTo(0.5f * w, 0.12f * h)
        cubicTo(0.82f * w, 0.30f * h, 0.86f * w, 0.66f * h, 0.5f * w, 0.9f * h)
        cubicTo(0.14f * w, 0.66f * h, 0.18f * w, 0.30f * h, 0.5f * w, 0.12f * h)
        close()
    }
    drawPath(p, c, style = Stroke(width = sw))
    // zigzag band
    val zig = Path().apply {
        moveTo(0.24f * w, 0.56f * h)
        lineTo(0.36f * w, 0.48f * h); lineTo(0.48f * w, 0.56f * h)
        lineTo(0.6f * w, 0.48f * h); lineTo(0.72f * w, 0.56f * h)
    }
    drawPath(zig, c, style = Stroke(width = sw * 0.8f, cap = StrokeCap.Round))
}

private fun DrawScope.inkDrop(c: Color) {
    val w = size.width; val h = size.height
    val p = Path().apply {
        moveTo(0.5f * w, 0.14f * h)
        cubicTo(0.84f * w, 0.5f * h, 0.82f * w, 0.72f * h, 0.62f * w, 0.84f * h)
        cubicTo(0.48f * w, 0.92f * h, 0.32f * w, 0.86f * h, 0.28f * w, 0.7f * h)
        cubicTo(0.24f * w, 0.54f * h, 0.34f * w, 0.4f * h, 0.5f * w, 0.14f * h)
        close()
    }
    drawPath(p, c)
    star(Offset(0.72f * w, 0.28f * h), outer = 0.11f * w, inner = 0.04f * w, Color.White.copy(alpha = 0.9f))
}

private fun DrawScope.anchor(c: Color) {
    val w = size.width; val h = size.height
    val sw = w * 0.07f
    // ring
    drawCircle(c, radius = 0.09f * w, center = Offset(0.5f * w, 0.16f * h), style = Stroke(width = sw))
    // shaft
    drawLine(c, Offset(0.5f * w, 0.25f * h), Offset(0.5f * w, 0.82f * h), strokeWidth = sw, cap = StrokeCap.Round)
    // crossbar
    drawLine(c, Offset(0.34f * w, 0.36f * h), Offset(0.66f * w, 0.36f * h), strokeWidth = sw, cap = StrokeCap.Round)
    // bottom arc (flukes)
    drawArc(c, startAngle = 20f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(0.22f * w, 0.5f * h), size = Size(0.56f * w, 0.4f * h),
        style = Stroke(width = sw, cap = StrokeCap.Round))
}

private fun DrawScope.club(c: Color) {
    val w = size.width; val h = size.height
    val r = 0.15f * w
    drawCircle(c, r, Offset(0.5f * w, 0.28f * h))
    drawCircle(c, r, Offset(0.34f * w, 0.5f * h))
    drawCircle(c, r, Offset(0.66f * w, 0.5f * h))
    // stem
    drawPath(Path().apply {
        moveTo(0.44f * w, 0.52f * h)
        lineTo(0.56f * w, 0.52f * h)
        lineTo(0.62f * w, 0.86f * h)
        lineTo(0.38f * w, 0.86f * h)
        close()
    }, c)
}

private fun DrawScope.cardFan(c: Color) {
    val w = size.width; val h = size.height
    val sw = w * 0.05f
    val angles = listOf(-20f, 0f, 20f)
    angles.forEach { ang ->
        rotate(ang, pivot = Offset(0.5f * w, 0.9f * h)) {
            drawPath(Path().apply {
                addRoundRect(RoundRect(0.4f * w, 0.24f * h, 0.6f * w, 0.9f * h, CornerRadius(0.03f * w, 0.03f * w)))
            }, c, style = Stroke(width = sw))
        }
    }
}

private fun DrawScope.tiles(c: Color) {
    val w = size.width; val h = size.height
    val sw = w * 0.06f
    drawPath(Path().apply {
        addRoundRect(RoundRect(0.2f * w, 0.28f * h, 0.56f * w, 0.82f * h, CornerRadius(0.06f * w, 0.06f * w)))
    }, c, style = Stroke(width = sw))
    drawPath(Path().apply {
        addRoundRect(RoundRect(0.46f * w, 0.18f * h, 0.82f * w, 0.72f * h, CornerRadius(0.06f * w, 0.06f * w)))
    }, c, style = Stroke(width = sw))
    drawCircle(c, radius = 0.05f * w, center = Offset(0.64f * w, 0.45f * h))
}

private fun DrawScope.dartboard(c: Color) {
    val w = size.width; val h = size.height
    val cx = 0.5f * w; val cy = 0.5f * h
    val sw = w * 0.055f
    drawCircle(c, radius = 0.42f * w, center = Offset(cx, cy), style = Stroke(width = sw))
    drawCircle(c, radius = 0.26f * w, center = Offset(cx, cy), style = Stroke(width = sw))
    drawCircle(c, radius = 0.08f * w, center = Offset(cx, cy))
    // crosshair spokes
    val r0 = 0.26f * w; val r1 = 0.42f * w
    listOf(0f, 90f, 180f, 270f).forEach { deg ->
        val a = deg * PI.toFloat() / 180f
        drawLine(c, Offset(cx + r0 * cos(a), cy + r0 * sin(a)), Offset(cx + r1 * cos(a), cy + r1 * sin(a)), strokeWidth = sw * 0.8f)
    }
}

private fun DrawScope.star(center: Offset, outer: Float, inner: Float, c: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = (-90f + i * 36f) * PI.toFloat() / 180f
        val x = center.x + r * cos(a); val y = center.y + r * sin(a)
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, c)
}
