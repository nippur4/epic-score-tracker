package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Shared building blocks for the specific-game screens (pills, chips, sheet buttons, name dialog). */

/**
 * Keeps a game board clear of the status bar, the camera cutout and the system navigation
 * bar / gesture area. Apply after the background so the backdrop still paints edge to edge.
 */
@Composable
fun Modifier.gameInsets(): Modifier = windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))

/** Single-tint vector icon from `res/drawable` (the per-game symbols live there as `ic_*`). */
@Composable
fun GameIcon(@DrawableRes id: Int, tint: Color, size: Int = 16, modifier: Modifier = Modifier) {
    Icon(painter = painterResource(id), contentDescription = null, tint = tint, modifier = modifier.size(size.dp))
}

/** Three-stop gradient from a player color into the deep app background, for pane/card backdrops. */
fun gamePaneGradient(base: Color, start: Float = 0.42f): List<Color> {
    val deep = Color(0xFF0E1B33)
    fun mix(t: Float) = Color(base.red * (1 - t) + deep.red * t, base.green * (1 - t) + deep.green * t, base.blue * (1 - t) + deep.blue * t, 1f)
    return listOf(mix(start), mix(0.72f), deep)
}

@Composable
fun GamePill(text: String, filled: Boolean = false, accent: Color = Palette.Cyan, onClick: () -> Unit) {
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp))
            .background(if (filled) accent else Color.Transparent)
            .then(if (filled) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
            .clickable { onClick() }.padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (filled) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

/** Small round − / + used inside counters. Repeats while held. */
@Composable
fun MiniStep(symbol: String, size: Int = 26, onClick: () -> Unit) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).repeatingClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontSize = (size * 0.65f).sp))
    }
}

/** Labelled counter pill: [icon] LABEL  [−] value [+]. */
@Composable
fun LabeledCounter(label: String, value: Int, color: Color, onDec: () -> Unit, onInc: () -> Unit, big: Boolean = false, @DrawableRes icon: Int? = null) {
    Row(
        Modifier.height(if (big) 46.dp else 38.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (big) 10.dp else 6.dp),
    ) {
        if (icon != null) GameIcon(icon, color, if (big) 18 else 14)
        Text(label.uppercase(), color = color, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = if (big) 11.sp else 10.sp, letterSpacing = 1.2.sp))
        MiniStep("−", if (big) 32 else 24, onDec)
        Text("$value", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = if (big) 34.dp else 22.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = if (big) 20.sp else 15.sp, fontFeatureSettings = "tnum"))
        MiniStep("+", if (big) 32 else 24, onInc)
    }
}

@Composable
fun SheetLabel(text: String) {
    Text(text, color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
}

@Composable
fun SheetPrimaryButton(text: String, accent: Color = Palette.Cyan, @DrawableRes icon: Int? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(accent).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (icon != null) GameIcon(icon, Palette.OnAccent, 18)
        Text(text, color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
    }
}

@Composable
fun SheetSecondaryButton(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
    }
}

/** Row of preset values rendered as selectable chips. */
@Composable
fun PresetChips(options: List<Int>, selected: Int, accent: Color = Palette.Cyan, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { p ->
            val active = p == selected
            Box(Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) accent else Color.Transparent).then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp))).clickable { onSelect(p) }.padding(horizontal = 18.dp, vertical = 9.dp)) {
                Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
        }
    }
}

/** Simple rename dialog with the app's underlined text field. */
@Composable
fun NameEditDialog(current: String, onSave: (String) -> Unit, onClose: () -> Unit, onRemove: (() -> Unit)? = null) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = { onSave(name); onClose() }) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        dismissButton = {
            Row {
                if (onRemove != null) TextButton(onClick = { onRemove(); onClose() }) { Text(stringResource(R.string.player_remove), color = Color(0xFFEB5757)) }
                TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) }
            }
        },
        title = { Text(stringResource(R.string.name), color = Palette.TextPrimary) },
        text = {
            Column {
                BasicTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 18.sp, color = Palette.TextPrimary),
                    cursorBrush = SolidColor(Palette.Cyan),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
                Box(Modifier.fillMaxWidth().height(1.5.dp).background(Palette.Cyan))
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

/** Name row with a color swatch; tap to rename. */
@Composable
fun PlayerNameRow(name: String, color: Color, onClick: (() -> Unit)? = null, size: Int = 11) {
    Row(
        (if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color).border(1.dp, Color(0x33FFFFFF), CircleShape))
        Text(name.uppercase(), color = color, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = size.sp, letterSpacing = 1.9.sp))
        if (onClick != null) Text("✎", color = Palette.TextMuted, style = TextStyle(fontSize = 11.sp))
    }
}
