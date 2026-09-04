package com.epichypernova.scoretracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Corner radii from the handoff. */
object Radii {
    val pill = RoundedCornerShape(999.dp)
    val card = RoundedCornerShape(16.dp)
    val cardLg = RoundedCornerShape(20.dp)
    val icon = RoundedCornerShape(13.dp)
    val icon11 = RoundedCornerShape(11.dp)
    val key = RoundedCornerShape(12.dp)
    val cell = RoundedCornerShape(10.dp)
    val sheetTop = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
}

/**
 * CTA chamfer, equivalent to the design's
 * clip-path: polygon(14px 0, 100% 0, 100% calc(100% - 14px), calc(100% - 14px) 100%, 0 100%, 0 14px).
 * Top-left and bottom-right corners are bevelled by [cut].
 */
fun chamferShape(cut: Dp = 14.dp): Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val c = with(density) { cut.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        val path = Path().apply {
            moveTo(c, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - c)
            lineTo(size.width - c, size.height)
            lineTo(0f, size.height)
            lineTo(0f, c)
            close()
        }
        return Outline.Generic(path)
    }
}
