@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.magic

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.scale
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
import com.epichypernova.scoretracker.data.model.MagicPlayer
import com.epichypernova.scoretracker.ui.components.DieFace
import com.epichypernova.scoretracker.ui.components.EnergyIcon
import com.epichypernova.scoretracker.ui.components.ExperienceIcon
import com.epichypernova.scoretracker.ui.components.PoisonIcon
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.AppToggle
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

private val EXP_COLOR = Color(0xFFFFD98A)

/** App palette + the five Magic colors (W U B R G). */
val MAGIC_COLORS = listOf(
    0xFF2FD3F0, 0xFF55E6A5, 0xFFFF6FA8, 0xFFA18AF5, 0xFF3B7BF7, 0xFFF27BA9,
    0xFFF2ECD0, 0xFF4AA3FF, 0xFF6E6A86, 0xFFE0492F, 0xFF4FB35B,
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
fun MagicScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.magicEnsureExists(it) } }
    val game = state.magicGame ?: return
    val players = game.players

    var showDraw by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    var editLife by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }

    val bar: @Composable () -> Unit = {
        CentralBar(
            life = game.startingLife,
            commander = game.commander,
            onEditLife = { editLife = true },
            onReset = { repo.update { AppActions.magicReset(it) } },
            onShuffle = { showDraw = true },
            onConfig = { showConfig = true },
        )
    }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            when (players.size) {
                2 -> {
                    MagicPane(players[0], 0, repo, game.commander, rotated = true, wide = true, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                    bar()
                    MagicPane(players[1], 1, repo, game.commander, rotated = false, wide = true, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                3 -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        MagicPane(players[0], 0, repo, game.commander, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        MagicPane(players[1], 1, repo, game.commander, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    MagicPane(players[2], 2, repo, game.commander, rotated = false, wide = true, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                else -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        MagicPane(players[0], 0, repo, game.commander, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        MagicPane(players[1], 1, repo, game.commander, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        MagicPane(players[2], 2, repo, game.commander, rotated = false, wide = false, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        MagicPane(players[3], 3, repo, game.commander, rotated = false, wide = false, onPickColor = { colorForIndex = 3 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        if (showDraw) {
            StarterDrawOverlay(
                players = players,
                onSettled = { idx -> if (game.commander) repo.update { AppActions.magicExperience(it, idx, +1) } },
                onClose = { showDraw = false },
            )
        }
    }

    if (showConfig) {
        MagicConfigSheet(
            count = players.size,
            commander = game.commander,
            onApply = { count, commander -> repo.update { AppActions.magicConfigure(it, count, commander) } },
            onFinish = { repo.update { AppActions.magicFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (editLife) {
        LifeEditDialog(
            current = game.startingLife,
            onSet = { newLife -> repo.update { AppActions.magicSetLife(it, newLife) } },
            onClose = { editLife = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.magicSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
}

@Composable
private fun MagicPane(
    player: MagicPlayer,
    index: Int,
    repo: Repository,
    commander: Boolean,
    rotated: Boolean,
    wide: Boolean,
    onPickColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grad = paneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        if (player.eliminated) drawRect(Color(0xFF131A2E))
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
    val lifeSize = if (wide) 66 else 46
    val btnSize = if (wide) 56 else 46
    Box(modifier.then(bg)) {
        Column(
            Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // name + tappable color swatch
            Row(
                Modifier.clickable { onPickColor() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(player.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(
                    player.name.uppercase(),
                    color = if (player.eliminated) Palette.Cyan else Color(player.color),
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp),
                )
            }
            if (player.eliminated) {
                Text("${player.life}", color = Color(0xFF3E4A6B), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = lifeSize.sp, fontFeatureSettings = "tnum"))
                Text(stringResource(R.string.magic_eliminated), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.6.sp))
            } else {
                // life + / − always on one line
                Row(
                    Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 8.dp),
                ) {
                    LifeButton("−", btnSize) { repo.update { AppActions.magicLife(it, index, -1) } }
                    Text(
                        "${player.life}",
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = lifeSize.sp, fontFeatureSettings = "tnum"),
                    )
                    LifeButton("＋", btnSize) { repo.update { AppActions.magicLife(it, index, +1) } }
                }
                // counters stacked, one per row
                Column(Modifier.padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    CounterChip(player.poison, Palette.Mint,
                        onDec = { repo.update { AppActions.magicPoison(it, index, -1) } },
                        onInc = { repo.update { AppActions.magicPoison(it, index, +1) } }) { PoisonIcon(Palette.Mint, 18) }
                    CounterChip(player.energy, Palette.Cyan,
                        onDec = { repo.update { AppActions.magicEnergy(it, index, -1) } },
                        onInc = { repo.update { AppActions.magicEnergy(it, index, +1) } }) { EnergyIcon(Palette.Cyan, 18) }
                    if (commander) {
                        CounterChip(player.experience, EXP_COLOR,
                            onDec = { repo.update { AppActions.magicExperience(it, index, -1) } },
                            onInc = { repo.update { AppActions.magicExperience(it, index, +1) } }) { ExperienceIcon(EXP_COLOR, 18) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LifeButton(symbol: String, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 26.sp))
    }
}

/** Counter pill with an icon and explicit − / + buttons (both directions). */
@Composable
private fun CounterChip(value: Int, color: Color, onDec: () -> Unit, onInc: () -> Unit, icon: @Composable () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        icon()
        Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { onDec() }, contentAlignment = Alignment.Center) {
            Text("−", color = Palette.TextPrimary, style = TextStyle(fontSize = 18.sp))
        }
        Text("$value", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = 22.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFeatureSettings = "tnum"))
        Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { onInc() }, contentAlignment = Alignment.Center) {
            Text("+", color = Palette.TextPrimary, style = TextStyle(fontSize = 18.sp))
        }
    }
}

@Composable
private fun CentralBar(
    life: Int,
    commander: Boolean,
    onEditLife: () -> Unit,
    onReset: () -> Unit,
    onShuffle: () -> Unit,
    onConfig: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x472FD3F0), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.weight(1f).clickable { onEditLife() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                (if (commander) "COMMANDER" else "VIDAS") + " $life",
                color = Palette.TextMuted,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp),
            )
            Text("✎", color = Palette.TextMuted, style = TextStyle(fontSize = 11.sp))
        }
        PillButton(stringResource(R.string.magic_reset), filled = false, onClick = onReset)
        PillButton(stringResource(R.string.magic_shuffle), filled = true, onClick = onShuffle)
        PillButton("⚙", filled = false, onClick = onConfig)
    }
}

@Composable
private fun PillButton(text: String, filled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp))
            .background(if (filled) Palette.Cyan else Color.Transparent)
            .then(if (filled) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (filled) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MagicConfigSheet(
    count: Int,
    commander: Boolean,
    onApply: (Int, Boolean) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var c by remember { mutableIntStateOf(count) }
    var cmd by remember { mutableStateOf(commander) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Jugadores", color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Segmented(
                options = listOf("2", "3", "4"),
                selectedIndex = (c - 2).coerceIn(0, 2),
                onSelect = { c = it + 2 },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Commander", color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                    Text("40 vidas + experiencia", color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp))
                }
                AppToggle(cmd, { cmd = it })
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(c, cmd); onClose() },
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
private fun LifeEditDialog(current: Int, onSet: (Int) -> Unit, onClose: () -> Unit) {
    var life by remember { mutableIntStateOf(current) }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = { onSet(life); onClose() }) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) } },
        title = { Text("Vida inicial", color = Palette.TextPrimary) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LifeStep("−") { life = (life - 1).coerceAtLeast(1) }
                    Text("$life", color = Palette.Cyan, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 30.sp, fontFeatureSettings = "tnum"))
                    LifeStep("＋") { life += 1 }
                }
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20, 30, 40).forEach { preset ->
                        Box(Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { life = preset }.padding(horizontal = 14.dp, vertical = 7.dp)) {
                            Text("$preset", color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp))
                        }
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@Composable
private fun LifeStep(symbol: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, Palette.ButtonBorder, CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontSize = 20.sp))
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MAGIC_COLORS.forEach { c ->
                    val active = c == current
                    Box(
                        Modifier.size(44.dp).then(if (active) Modifier.border(2.dp, Palette.Cyan, CircleShape) else Modifier).clickable { onPick(c); onClose() },
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.size(38.dp).clip(CircleShape).background(Color(c))) }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StarterDrawOverlay(
    players: List<MagicPlayer>,
    onSettled: (Int) -> Unit,
    onClose: () -> Unit,
) {
    var faces by remember { mutableStateOf(List(players.size) { 1 }) }
    var settled by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(-1) }
    val diceSound = rememberSoundEffect(R.raw.dice)

    LaunchedEffect(Unit) {
        diceSound()
        var d = 55L
        repeat(20) {
            faces = List(players.size) { (1..6).random() }
            delay(d)
            d += 9
        }
        val finalFaces = List(players.size) { (1..6).random() }
        faces = finalFaces
        val max = finalFaces.max()
        winner = finalFaces.indices.filter { finalFaces[it] == max }.random()
        settled = true
        onSettled(winner)
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xE60A1327)).clickable(enabled = settled) { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                if (settled) stringResource(R.string.magic_starts, players.getOrNull(winner)?.name ?: "") else stringResource(R.string.magic_rolling),
                color = if (settled) Palette.Mint else Palette.TextSecondary,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = if (settled) 26.sp else 18.sp, letterSpacing = 1.sp),
            )
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                players.forEachIndexed { i, p ->
                    val isWinner = settled && i == winner
                    val s by animateFloatAsState(if (isWinner) 1.18f else if (settled) 0.86f else 1f, label = "die")
                    Box(Modifier.scale(s)) {
                        DieFace(faces.getOrElse(i) { 1 }, Color(p.color), Palette.OnAccent, size = 54)
                    }
                }
            }
            if (settled) PillButton(stringResource(R.string.magic_ok), filled = true, onClick = onClose)
        }
    }
}
