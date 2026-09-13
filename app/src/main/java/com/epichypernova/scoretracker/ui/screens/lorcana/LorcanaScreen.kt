@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.lorcana

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.LorcanaPlayer
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val LORE = Color(0xFFF2C94C)

private val LORCANA_COLORS = listOf(
    0xFFF2B33B, 0xFF4AA3FF, 0xFF55E6A5, 0xFFA18AF5, 0xFF2FD3F0, 0xFFFF6FA8,
    0xFFF27BA9, 0xFFE0492F, 0xFFF2ECD0, 0xFF6E6A86, 0xFF4FB35B,
)

private fun paneGradient(base: Color): List<Color> {
    val deep = Color(0xFF0E1B33)
    fun mix(t: Float) = Color(
        red = base.red * (1 - t) + deep.red * t,
        green = base.green * (1 - t) + deep.green * t,
        blue = base.blue * (1 - t) + deep.blue * t,
        alpha = 1f,
    )
    return listOf(mix(0.42f), mix(0.72f), deep)
}

@Composable
fun LorcanaScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.lorcanaEnsureExists(it) } }
    val game = state.lorcanaGame ?: return
    val players = game.players

    var showConfig by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }

    val bar: @Composable () -> Unit = {
        CentralBar(
            target = game.targetLore,
            onReset = { repo.update { AppActions.lorcanaReset(it) } },
            onConfig = { showConfig = true },
        )
    }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            when (players.size) {
                2 -> {
                    LorcanaPane(players[0], 0, game.targetLore, repo, rotated = true, wide = true, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                    bar()
                    LorcanaPane(players[1], 1, game.targetLore, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                3 -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        LorcanaPane(players[0], 0, game.targetLore, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        LorcanaPane(players[1], 1, game.targetLore, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    LorcanaPane(players[2], 2, game.targetLore, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                else -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        LorcanaPane(players[0], 0, game.targetLore, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        LorcanaPane(players[1], 1, game.targetLore, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        LorcanaPane(players[2], 2, game.targetLore, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        LorcanaPane(players[3], 3, game.targetLore, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 3 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }

    if (showConfig) {
        LorcanaConfigSheet(
            count = players.size,
            target = game.targetLore,
            onApply = { count, target -> repo.update { AppActions.lorcanaConfigure(it, count, target) } },
            onFinish = { repo.update { AppActions.lorcanaFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.lorcanaSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
}

@Composable
private fun LorcanaPane(
    player: LorcanaPlayer,
    index: Int,
    target: Int,
    repo: Repository,
    rotated: Boolean,
    wide: Boolean,
    onPickColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grad = paneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        drawRect(
            brush = ShaderBrush(
                RadialGradientShader(
                    center = Offset(size.width * 0.5f, size.height * 1.0f),
                    radius = size.height * 0.95f,
                    colors = grad,
                    colorStops = listOf(0f, 0.6f, 1f),
                )
            )
        )
    }
    val loreSize = if (wide) 64 else 46
    val btnSize = if (wide) 56 else 46
    Box(modifier.then(bg)) {
        Column(
            Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                Modifier.clickable { onPickColor() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(player.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(
                    player.name.uppercase(),
                    color = if (player.won) Palette.Mint else Color(player.color),
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp),
                )
            }
            if (player.won) {
                Text(stringResource(R.string.lorcana_won), color = Palette.Mint, modifier = Modifier.padding(top = 6.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.8.sp))
            } else {
                Text(stringResource(R.string.lorcana_lore), color = Palette.TextTertiary, modifier = Modifier.padding(top = 10.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
            }
            Row(
                Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 8.dp),
            ) {
                LoreButton("−", btnSize) { repo.update { AppActions.lorcanaLore(it, index, -1) } }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${player.lore}",
                        color = if (player.won) Palette.Mint else LORE,
                        maxLines = 1, softWrap = false,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = loreSize.sp, fontFeatureSettings = "tnum"),
                    )
                    Text(
                        "/$target",
                        color = Palette.TextMuted,
                        modifier = Modifier.padding(bottom = (loreSize * 0.14f).dp),
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = (loreSize * 0.34f).sp),
                    )
                }
                LoreButton("＋", btnSize) { repo.update { AppActions.lorcanaLore(it, index, +1) } }
            }
        }
    }
}

@Composable
private fun LoreButton(symbol: String, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 26.sp))
    }
}

@Composable
private fun CentralBar(target: Int, onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x47F2B33B), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.lorcana_target) + ": $target",
            color = Palette.TextMuted,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.2.sp),
        )
        PillButton(stringResource(R.string.lorcana_reset), onReset)
        PillButton("⚙", onConfig)
    }
}

@Composable
private fun PillButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LorcanaConfigSheet(
    count: Int,
    target: Int,
    onApply: (Int, Int) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var c by remember { mutableIntStateOf(count) }
    var t by remember { mutableIntStateOf(target) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(20, 25, 30)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.lorcana_players), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Segmented(options = listOf("2", "3", "4"), selectedIndex = (c - 2).coerceIn(0, 2), onSelect = { c = it + 2 })
            Text(stringResource(R.string.lorcana_target), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == t
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) Palette.Cyan else Color.Transparent)
                            .then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { t = p }.padding(horizontal = 18.dp, vertical = 9.dp),
                    ) { Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(c, t); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.finish_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
        }
    }
}

@Composable
private fun ColorPickDialog(current: Long, onPick: (Long) -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        title = { Text(stringResource(R.string.color), color = Palette.TextPrimary) },
        text = {
            androidx.compose.foundation.layout.FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LORCANA_COLORS.forEach { c ->
                    val active = c == current
                    val swatch = Color(c)
                    val lum = 0.299f * swatch.red + 0.587f * swatch.green + 0.114f * swatch.blue
                    val onColor = if (lum > 0.6f) Color.Black else Color.White
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(swatch)
                            .then(if (active) Modifier.border(3.dp, Palette.TextPrimary, CircleShape) else Modifier.border(1.dp, Color(0x33FFFFFF), CircleShape))
                            .clickable { onPick(c); onClose() },
                        contentAlignment = Alignment.Center,
                    ) { if (active) Text("✓", color = onColor, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black)) }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}
