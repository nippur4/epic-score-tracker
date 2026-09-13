@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.digimon

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.DigimonPlayer
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val DAMAGE = Color(0xFFFF6B6B)
private val HEAL = Palette.Mint

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
fun DigimonScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.digimonEnsureExists(it) } }
    val game = state.digimonGame ?: return
    val players = game.players
    if (players.size < 2) return

    var showConfig by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            DigimonPane(players[0], 0, game.startingSecurity, repo, rotated = true, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
            MemoryBar(
                memory = game.memory,
                topColor = Color(players[0].color),
                bottomColor = Color(players[1].color),
                onSet = { v -> repo.update { AppActions.digimonSetMemory(it, v) } },
                onNudge = { d -> repo.update { AppActions.digimonMemory(it, d) } },
                onReset = { repo.update { AppActions.digimonReset(it) } },
                onConfig = { showConfig = true },
            )
            DigimonPane(players[1], 1, game.startingSecurity, repo, rotated = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }

    if (showConfig) {
        DigimonConfigSheet(
            startingSecurity = game.startingSecurity,
            onApply = { sec -> repo.update { AppActions.digimonSetSecurity(it, sec) } },
            onFinish = { repo.update { AppActions.digimonFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.digimonSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
}

@Composable
private fun DigimonPane(
    player: DigimonPlayer,
    index: Int,
    startingSecurity: Int,
    repo: Repository,
    rotated: Boolean,
    onPickColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grad = paneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        if (player.defeated) drawRect(Color(0xFF131A2E))
        else drawRect(
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
    Box(modifier.then(bg)) {
        Column(
            Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp),
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
                    color = if (player.defeated) Palette.Cyan else Color(player.color),
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp),
                )
            }

            if (player.defeated) {
                Text(stringResource(R.string.digimon_defeated), color = Palette.Cyan, modifier = Modifier.padding(top = 10.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.8.sp))
            } else {
                Text(stringResource(R.string.digimon_security), color = Palette.TextTertiary, modifier = Modifier.padding(top = 12.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
                // security pips
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(startingSecurity) { i ->
                        val filled = i < player.security
                        Box(
                            Modifier.size(18.dp).clip(CircleShape)
                                .background(if (filled) Color(player.color) else Color(0x22FFFFFF))
                                .border(1.dp, if (filled) Color(0x55FFFFFF) else Color(0x22FFFFFF), CircleShape),
                        )
                    }
                }
                Row(
                    Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SecurityButton("−", DAMAGE) { repo.update { AppActions.digimonSecurity(it, index, -1) } }
                    Text(
                        "${player.security}",
                        color = Color.White,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, fontFeatureSettings = "tnum"),
                    )
                    SecurityButton("＋", HEAL) { repo.update { AppActions.digimonSecurity(it, index, +1) } }
                }
            }
        }
    }
}

@Composable
private fun SecurityButton(symbol: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp).clip(CircleShape).border(1.dp, accent.copy(alpha = 0.55f), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = accent, style = TextStyle(fontSize = 26.sp))
    }
}

@Composable
private fun MemoryBar(
    memory: Int,
    topColor: Color,
    bottomColor: Color,
    onSet: (Int) -> Unit,
    onNudge: (Int) -> Unit,
    onReset: () -> Unit,
    onConfig: () -> Unit,
) {
    // side that currently holds the memory determines the accent
    val accent = when {
        memory < 0 -> topColor
        memory > 0 -> bottomColor
        else -> Palette.TextMuted
    }
    Column(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x473B7BF7), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.digimon_memory), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
            Text(
                "${kotlin.math.abs(memory)}",
                color = accent,
                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFeatureSettings = "tnum"),
            )
        }
        // gauge: -10..+10, tap a cell to set, − / + to nudge
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MemButton("−") { onNudge(-1) }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                (-10..10).forEach { v ->
                    val active = v == memory
                    val cellColor = when {
                        active && v < 0 -> topColor
                        active && v > 0 -> bottomColor
                        active -> Palette.TextPrimary
                        v < 0 -> topColor.copy(alpha = 0.18f)
                        v > 0 -> bottomColor.copy(alpha = 0.18f)
                        else -> Color(0x33FFFFFF)
                    }
                    Box(
                        Modifier.weight(1f).height(26.dp).clip(RoundedCornerShape(5.dp)).background(cellColor).clickable { onSet(v) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) Text("${kotlin.math.abs(v)}", color = Palette.OnAccentDeep, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                        else if (v == 0) Text("0", color = Palette.TextMuted, style = TextStyle(fontFamily = Orbitron, fontSize = 10.sp))
                    }
                }
            }
            MemButton("＋") { onNudge(+1) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton(stringResource(R.string.digimon_reset), onReset)
            PillButton("⚙", onConfig)
        }
    }
}

@Composable
private fun MemButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape).border(1.dp, Palette.ButtonBorder, CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Palette.TextSecondary, style = TextStyle(fontSize = 20.sp))
    }
}

@Composable
private fun PillButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.height(32.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DigimonConfigSheet(
    startingSecurity: Int,
    onApply: (Int) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var sec by remember { mutableIntStateOf(startingSecurity) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(5, 3, 7)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.digimon_starting_security), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == sec
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp))
                            .background(if (active) Palette.Cyan else Color.Transparent)
                            .then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { sec = p }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    ) {
                        Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(sec); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.digimon_finish), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
        }
    }
}

@Composable
private fun ColorPickDialog(current: Long, onPick: (Long) -> Unit, onClose: () -> Unit) {
    val colors = listOf(
        0xFF3B7BF7, 0xFFF2B33B, 0xFF55E6A5, 0xFFFF6FA8, 0xFF2FD3F0, 0xFFA18AF5,
        0xFFF27BA9, 0xFFE0492F, 0xFFF2ECD0, 0xFF6E6A86, 0xFF4FB35B,
    )
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
                colors.forEach { c ->
                    val active = c == current
                    val swatch = Color(c)
                    val lum = 0.299f * swatch.red + 0.587f * swatch.green + 0.114f * swatch.blue
                    val onColor = if (lum > 0.6f) Color.Black else Color.White
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(swatch)
                            .then(if (active) Modifier.border(3.dp, Palette.TextPrimary, CircleShape) else Modifier.border(1.dp, Color(0x33FFFFFF), CircleShape))
                            .clickable { onPick(c); onClose() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) Text("✓", color = onColor, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black))
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}
