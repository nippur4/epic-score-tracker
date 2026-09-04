package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Header with a back chevron, a Cinzel screen title and an optional cyan overtitle. */
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    overtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().background(Palette.AppBg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Palette.TextSecondary, modifier = Modifier.clickable { onBack() }, style = TextStyle(fontSize = 26.sp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                if (overtitle != null) {
                    Text(
                        overtitle.uppercase(),
                        color = Palette.Cyan,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.76.sp),
                    )
                }
                Text(
                    title.uppercase(),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 0.8.sp),
                )
            }
            trailing?.invoke()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x17FFFFFF)))
    }
}

/** Compact header used by the game boards (small title + meta + trailing glyph). */
@Composable
fun CompactHeader(
    title: String,
    meta: @Composable () -> Unit,
    onBack: () -> Unit,
    trailing: String? = "⋮",
    onTrailing: () -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().background(Palette.AppBg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = Palette.TextSecondary, modifier = Modifier.clickable { onBack() }, style = TextStyle(fontSize = 26.sp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    title,
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                )
                meta()
            }
            if (trailing != null) {
                Text(trailing, color = Palette.TextSecondary, modifier = Modifier.clickable { onTrailing() }.width(24.dp), style = TextStyle(fontSize = 20.sp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x17FFFFFF)))
    }
}
