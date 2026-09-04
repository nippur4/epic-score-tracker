package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Solid deep app background. */
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Palette.AppBg)) { content() }
}

/** Menu radial backdrop: radial-gradient(130% 70% at 50% -10%, #1B3768, #0C1730 58%, #0A1327). */
fun Modifier.menuBackdrop(): Modifier = this.drawBehind {
    val shader = RadialGradientShader(
        center = Offset(size.width * 0.5f, size.height * -0.10f),
        radius = size.width * 1.15f,
        colors = listOf(Color(0xFF1B3768), Palette.AppBg, Palette.AppBgDeep),
        colorStops = listOf(0f, 0.58f, 1f),
    )
    drawRect(brush = Palette.AppBg.let { Brush.linearGradient(listOf(it, it)) })
    drawRect(brush = ShaderBrush(shader))
}

/** 10px uppercase section label with 0.2em tracking. */
@Composable
fun SectionLabel(text: String, color: Color = Palette.TextMuted, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        style = TextStyle(
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 2.0.sp,
        ),
    )
}

/** Standard card surface modifier: fill, border, bevel. */
fun Modifier.cardSurface(radius: Int = 16): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(Palette.CardSurface)
    .border(1.dp, Palette.CardBorder, RoundedCornerShape(radius.dp))

fun Modifier.rowSurface(radius: Int = 12): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(Palette.RowSurface)
    .border(1.dp, Palette.RowBorder, RoundedCornerShape(radius.dp))

/** Circular avatar with a Cinzel monogram over the player color. */
@Composable
fun Avatar(name: String, color: Color, size: Int, fontSize: Int = (size * 0.42f).toInt()) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            color = Palette.OnAccent,
            style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = fontSize.sp),
        )
    }
}

/** Rounded-square icon box holding a glyph, tinted by [tint]. */
@Composable
fun GlyphBox(glyph: String, tint: Color, boxSize: Int = 42, radius: Int = 13, glyphSize: Int = 19) {
    Box(
        modifier = Modifier
            .size(boxSize.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(tint.copy(alpha = 0.16f))
            .border(1.dp, tint.copy(alpha = 0.30f), RoundedCornerShape(radius.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            color = tint,
            style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = glyphSize.sp),
        )
    }
}

/** Thin progress bar (height 4) with a track and a colored fill fraction. */
@Composable
fun ThinProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Palette.CardBorder),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

/** Dashed rounded border (used by "add" affordances and dashed tiles). */
fun Modifier.dashedBorder(color: Color, radius: Int = 16, width: Float = 1f): Modifier = this.drawBehind {
    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
        width = width * density,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
            floatArrayOf(10f * density, 8f * density), 0f,
        ),
    )
    val r = radius * density
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
}

/** Ellipsized single-line body text helper. */
@Composable
fun EllipsisText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier, align: TextAlign? = null) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
    )
}
