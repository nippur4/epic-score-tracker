package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/**
 * A tappable game row: glyph icon, title + subtitle, an optional favorite star, and a
 * trailing chevron. Passing [onToggleFavorite] shows a star that toggles independently of
 * the row tap.
 */
@Composable
fun GameRow(
    glyph: String,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: String = "›",
    favorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(16)
            .clickable(enabled = enabled) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBox(glyph = glyph, tint = tint, boxSize = 42, radius = 13, glyphSize = 18)
        androidx.compose.foundation.layout.Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) Palette.TextPrimary else Palette.TextMuted,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp),
            )
            Text(
                text = subtitle,
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal, fontSize = 12.5.sp),
            )
        }
        if (onToggleFavorite != null) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onToggleFavorite() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (favorite) "★" else "☆",
                    color = if (favorite) Palette.Cyan else Color(0xFF556792),
                    style = TextStyle(fontSize = 20.sp),
                )
            }
        }
        Text(
            text = trailing,
            color = Color(0xFF556792),
            style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 16.sp),
        )
    }
}
