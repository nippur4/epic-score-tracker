@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.yugioh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.epichypernova.scoretracker.data.model.YuGiOhPlayer
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlin.random.Random

/** App palette + a few extra shades to pick a duelist color from. */
private val YUGIOH_COLORS = listOf(
    0xFFFF6FA8, 0xFF3B7BF7, 0xFF55E6A5, 0xFFA18AF5, 0xFF2FD3F0, 0xFFF27BA9,
    0xFFF2ECD0, 0xFFE0492F, 0xFFF2B33B, 0xFF6E6A86, 0xFF4FB35B,
)

/** Damage-step options for the inline − / + buttons. */
private val STEP_OPTIONS = listOf(50, 100, 500, 1000)

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
fun YuGiOhScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.yugiohEnsureExists(it) } }
    val game = state.yugiohGame ?: return
    val players = game.players

    var showCoin by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }
    var calcForIndex by remember { mutableIntStateOf(-1) }

    val bar: @Composable () -> Unit = {
        CentralBar(
            step = game.step,
            onStep = { s -> repo.update { AppActions.yugiohSetStep(it, s) } },
            onReset = { repo.update { AppActions.yugiohReset(it) } },
            onCoin = { showCoin = true },
            onConfig = { showConfig = true },
        )
    }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            when (players.size) {
                2 -> {
                    YuGiOhPane(players[0], 0, game.step, repo, rotated = true, wide = true, onPickColor = { colorForIndex = 0 }, onOpenCalc = { calcForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                    bar()
                    YuGiOhPane(players[1], 1, game.step, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 1 }, onOpenCalc = { calcForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                3 -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        YuGiOhPane(players[0], 0, game.step, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, onOpenCalc = { calcForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        YuGiOhPane(players[1], 1, game.step, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, onOpenCalc = { calcForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    YuGiOhPane(players[2], 2, game.step, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 2 }, onOpenCalc = { calcForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                else -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        YuGiOhPane(players[0], 0, game.step, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, onOpenCalc = { calcForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        YuGiOhPane(players[1], 1, game.step, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, onOpenCalc = { calcForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        YuGiOhPane(players[2], 2, game.step, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 2 }, onOpenCalc = { calcForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        YuGiOhPane(players[3], 3, game.step, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 3 }, onOpenCalc = { calcForIndex = 3 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        if (showCoin) {
            CoinFlipOverlay(players = players, onClose = { showCoin = false })
        }
    }

    if (showConfig) {
        YuGiOhConfigSheet(
            count = players.size,
            startingLife = game.startingLife,
            onApply = { count, life -> repo.update { AppActions.yugiohConfigure(AppActions.yugiohSetLife(it, life), count) } },
            onFinish = { repo.update { AppActions.yugiohFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.yugiohSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
    if (calcForIndex >= 0) {
        val idx = calcForIndex
        LpCalculatorSheet(
            player = players.getOrNull(idx),
            onDamage = { amount -> repo.update { AppActions.yugiohLife(it, idx, -amount) } },
            onHeal = { amount -> repo.update { AppActions.yugiohLife(it, idx, +amount) } },
            onSet = { value -> repo.update { AppActions.yugiohSetExact(it, idx, value) } },
            onHalve = { repo.update { s -> AppActions.yugiohSetExact(s, idx, (s.yugiohGame?.players?.getOrNull(idx)?.life ?: 0) / 2) } },
            onClose = { calcForIndex = -1 },
        )
    }
}

@Composable
private fun YuGiOhPane(
    player: YuGiOhPlayer,
    index: Int,
    step: Int,
    repo: Repository,
    rotated: Boolean,
    wide: Boolean,
    onPickColor: () -> Unit,
    onOpenCalc: () -> Unit,
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
    val lpSize = if (wide) 52 else 34
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
                Text("${player.life}", color = Color(0xFF3E4A6B), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = lpSize.sp, fontFeatureSettings = "tnum"))
                Text(stringResource(R.string.yugioh_eliminated), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.6.sp))
            } else {
                // LP tappable → calculator
                Text(
                    "${player.life}",
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 4.dp).clickable { onOpenCalc() },
                    style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = lpSize.sp, fontFeatureSettings = "tnum"),
                )
                // − step / + step
                Row(
                    Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 10.dp),
                ) {
                    StepButton("−", step, btnSize, DAMAGE) { repo.update { AppActions.yugiohLife(it, index, -step) } }
                    StepButton("＋", step, btnSize, HEAL) { repo.update { AppActions.yugiohLife(it, index, +step) } }
                }
            }
        }
    }
}

@Composable
private fun StepButton(symbol: String, step: Int, size: Int, accent: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(
            Modifier.size(size.dp).clip(CircleShape).border(1.dp, accent.copy(alpha = 0.55f), CircleShape).repeatingClickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, color = accent, style = TextStyle(fontSize = 26.sp))
        }
        Text("$step", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp, letterSpacing = 0.5.sp))
    }
}

@Composable
private fun CentralBar(
    step: Int,
    onStep: (Int) -> Unit,
    onReset: () -> Unit,
    onCoin: () -> Unit,
    onConfig: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x47FF6FA8), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Segmented(
            options = STEP_OPTIONS.map { it.toString() },
            selectedIndex = STEP_OPTIONS.indexOf(step).coerceAtLeast(0),
            onSelect = { onStep(STEP_OPTIONS[it]) },
            modifier = Modifier.weight(1f),
        )
        PillButton(stringResource(R.string.yugioh_coin), filled = false, onClick = onCoin)
        PillButton("⟳", filled = false, onClick = onReset)
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
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (filled) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YuGiOhConfigSheet(
    count: Int,
    startingLife: Int,
    onApply: (Int, Int) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var c by remember { mutableIntStateOf(count) }
    var life by remember { mutableIntStateOf(startingLife) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(8000, 4000, 2000)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.yugioh_players), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Segmented(
                options = listOf("2", "3", "4"),
                selectedIndex = (c - 2).coerceIn(0, 2),
                onSelect = { c = it + 2 },
            )
            Text(stringResource(R.string.yugioh_starting_lp), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == life
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp))
                            .background(if (active) Palette.Cyan else Color.Transparent)
                            .then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { life = p }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(c, life); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.finish_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LpCalculatorSheet(
    player: YuGiOhPlayer?,
    onDamage: (Int) -> Unit,
    onHeal: (Int) -> Unit,
    onSet: (Int) -> Unit,
    onHalve: () -> Unit,
    onClose: () -> Unit,
) {
    if (player == null) return
    var amount by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val newDamage = (player.life - amount).coerceAtLeast(0)
    val newHeal = player.life + amount

    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.yugioh_calc_title, player.name),
                color = Color(player.color),
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.4.sp),
            )
            // current LP → preview
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${player.life}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, fontFeatureSettings = "tnum"))
                if (amount != 0) {
                    Text("→", color = Palette.TextMuted, style = TextStyle(fontSize = 22.sp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("−$amount → $newDamage", color = DAMAGE, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
                        Text("+$amount → $newHeal", color = HEAL, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
                    }
                }
            }
            // amount display
            Text(
                "$amount",
                color = Palette.CyanNumber,
                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 26.sp, fontFeatureSettings = "tnum"),
            )
            // numpad
            val rows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("00", "0", "⌫"))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { key ->
                            NumKey(key, Modifier.weight(1f)) {
                                amount = when (key) {
                                    "⌫" -> amount / 10
                                    "00" -> (amount * 100).coerceAtMost(9_999_999)
                                    else -> (amount * 10 + key.toInt()).coerceAtMost(9_999_999)
                                }
                            }
                        }
                    }
                }
            }
            // actions
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcAction(stringResource(R.string.yugioh_damage), DAMAGE, Modifier.weight(1f), enabled = amount != 0) { onDamage(amount); onClose() }
                CalcAction(stringResource(R.string.yugioh_heal), HEAL, Modifier.weight(1f), enabled = amount != 0) { onHeal(amount); onClose() }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcAction(stringResource(R.string.yugioh_set), Palette.Cyan, Modifier.weight(1f), enabled = amount != 0, outline = true) { onSet(amount); onClose() }
                CalcAction(stringResource(R.string.yugioh_halve), Palette.TextSecondary, Modifier.weight(1f), enabled = true, outline = true) { onHalve(); onClose() }
            }
        }
    }
}

@Composable
private fun NumKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 20.sp))
    }
}

@Composable
private fun CalcAction(text: String, accent: Color, modifier: Modifier = Modifier, enabled: Boolean, outline: Boolean = false, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.35f
    Box(
        modifier.height(50.dp).clip(RoundedCornerShape(999.dp))
            .then(if (outline) Modifier.border(1.dp, accent.copy(alpha = 0.6f * alpha), RoundedCornerShape(999.dp)) else Modifier.background(accent.copy(alpha = alpha)))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (outline) accent.copy(alpha = alpha) else Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp))
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
                YUGIOH_COLORS.forEach { c ->
                    val active = c == current
                    val swatch = Color(c)
                    val lum = 0.299f * swatch.red + 0.587f * swatch.green + 0.114f * swatch.blue
                    val onColor = if (lum > 0.6f) Color.Black else Color.White
                    Box(
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(swatch)
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

@Composable
private fun CoinFlipOverlay(
    players: List<YuGiOhPlayer>,
    onClose: () -> Unit,
) {
    var settled by remember { mutableStateOf(false) }
    var heads by remember { mutableStateOf(true) }
    var starter by remember { mutableIntStateOf(-1) }
    val rotation = remember { Animatable(0f) }
    val whoosh = rememberSoundEffect(R.raw.whoosh)
    val victory = rememberSoundEffect(R.raw.victory)

    LaunchedEffect(Unit) {
        whoosh()
        val landsHeads = Random.nextBoolean()
        val target = 360f * 6 + (if (landsHeads) 0f else 180f)
        rotation.animateTo(target, animationSpec = tween(durationMillis = 1500))
        heads = landsHeads
        starter = players.indices.random()
        settled = true
        victory()
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xE60A1327)).clickable(enabled = settled) { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
            // The coin: rotate on Y; show the face that is currently toward the viewer.
            val angle = ((rotation.value % 360f) + 360f) % 360f
            val showingHeads = if (settled) heads else angle < 90f || angle > 270f
            Box(
                Modifier.size(120.dp)
                    .graphicsLayer {
                        rotationY = rotation.value
                        cameraDistance = 14f * density
                    }
                    .clip(CircleShape)
                    .background(if (showingHeads) Palette.GameYugioh else Palette.Cyan)
                    .border(3.dp, Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(if (showingHeads) R.string.yugioh_heads else R.string.yugioh_tails),
                    color = Palette.OnAccentDeep,
                    // Counter-rotate the back face so its text isn't mirrored.
                    modifier = if (showingHeads) Modifier else Modifier.graphicsLayer { rotationY = 180f },
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp),
                )
            }
            Text(
                if (settled) stringResource(R.string.yugioh_starts, players.getOrNull(starter)?.name ?: "") else stringResource(R.string.yugioh_flipping),
                color = if (settled) Palette.Mint else Palette.TextSecondary,
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = if (settled) 24.sp else 18.sp, letterSpacing = 1.sp),
            )
            if (settled) PillButton(stringResource(R.string.magic_ok), filled = true, onClick = onClose)
        }
    }
}
