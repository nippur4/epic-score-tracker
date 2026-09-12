@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.pokemon

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
import com.epichypernova.scoretracker.data.model.PokemonPlayer
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlin.random.Random

private val POKEMON_COLORS = listOf(
    0xFF55E6A5, 0xFFF27BA9, 0xFF4AA3FF, 0xFFF2B33B, 0xFFFF6FA8, 0xFFA18AF5,
    0xFF2FD3F0, 0xFFE0492F, 0xFFF2ECD0, 0xFF6E6A86, 0xFF4FB35B,
)

/** Damage-step options for the inline − / + buttons (Pokémon damage is in tens). */
private val STEP_OPTIONS = listOf(10, 20, 50, 100)

private val PRIZE = Color(0xFFF2C94C)
private val DAMAGE = Color(0xFFFF6B6B)

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
fun PokemonScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.pokemonEnsureExists(it) } }
    val game = state.pokemonGame ?: return
    val players = game.players

    var showCoin by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }

    val bar: @Composable () -> Unit = {
        CentralBar(
            step = game.damageStep,
            onStep = { s -> repo.update { AppActions.pokemonSetStep(it, s) } },
            onReset = { repo.update { AppActions.pokemonReset(it) } },
            onCoin = { showCoin = true },
            onConfig = { showConfig = true },
        )
    }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            when (players.size) {
                2 -> {
                    PokemonPane(players[0], 0, game.startingPrizes, game.damageStep, repo, rotated = true, wide = true, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                    bar()
                    PokemonPane(players[1], 1, game.startingPrizes, game.damageStep, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                3 -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        PokemonPane(players[0], 0, game.startingPrizes, game.damageStep, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        PokemonPane(players[1], 1, game.startingPrizes, game.damageStep, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    PokemonPane(players[2], 2, game.startingPrizes, game.damageStep, repo, rotated = false, wide = true, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                else -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        PokemonPane(players[0], 0, game.startingPrizes, game.damageStep, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        PokemonPane(players[1], 1, game.startingPrizes, game.damageStep, repo, rotated = true, wide = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        PokemonPane(players[2], 2, game.startingPrizes, game.damageStep, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        PokemonPane(players[3], 3, game.startingPrizes, game.damageStep, repo, rotated = false, wide = false, onPickColor = { colorForIndex = 3 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        if (showCoin) {
            CoinFlipOverlay(players = players, onClose = { showCoin = false })
        }
    }

    if (showConfig) {
        PokemonConfigSheet(
            count = players.size,
            startingPrizes = game.startingPrizes,
            onApply = { count, prizes -> repo.update { AppActions.pokemonConfigure(it, count, prizes) } },
            onFinish = { repo.update { AppActions.pokemonFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.pokemonSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
}

@Composable
private fun PokemonPane(
    player: PokemonPlayer,
    index: Int,
    startingPrizes: Int,
    step: Int,
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
    val pipSize = if (wide) 22 else 15
    val btnSize = if (wide) 48 else 40
    val dmgSize = if (wide) 40 else 28
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
                    color = if (player.won) Palette.Mint else Color(player.color),
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp),
                )
            }

            // Prizes: tap a pip to set the remaining level.
            Text(stringResource(R.string.pokemon_prizes), color = Palette.TextTertiary, modifier = Modifier.padding(top = if (wide) 12.dp else 6.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(if (wide) 10.dp else 6.dp)) {
                repeat(startingPrizes) { i ->
                    val filled = i < player.prizes
                    Box(
                        Modifier.size(pipSize.dp)
                            .clip(CircleShape)
                            .background(if (filled) PRIZE else Color(0x22FFFFFF))
                            .border(1.dp, if (filled) Color(0x55FFFFFF) else Color(0x22FFFFFF), CircleShape)
                            .clickable {
                                val target = if (filled) i else i + 1
                                repo.update { AppActions.pokemonPrize(it, index, target - player.prizes) }
                            },
                    )
                }
            }
            Text(
                "${player.prizes}",
                color = if (player.won) Palette.Mint else PRIZE,
                modifier = Modifier.padding(top = 4.dp),
                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = (if (wide) 34 else 24).sp, fontFeatureSettings = "tnum"),
            )

            // Damage on the active Pokémon.
            Text(stringResource(R.string.pokemon_damage), color = Palette.TextTertiary, modifier = Modifier.padding(top = if (wide) 14.dp else 8.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
            Row(
                Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (wide) 14.dp else 8.dp),
            ) {
                StepButton("−", btnSize) { repo.update { AppActions.pokemonDamage(it, index, -step) } }
                Text(
                    "${player.damage}",
                    color = if (player.damage > 0) DAMAGE else Palette.TextMuted,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.clickable { repo.update { AppActions.pokemonResetDamage(it, index) } },
                    style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = dmgSize.sp, fontFeatureSettings = "tnum"),
                )
                StepButton("＋", btnSize) { repo.update { AppActions.pokemonDamage(it, index, +step) } }
            }
        }
    }
}

@Composable
private fun StepButton(symbol: String, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 24.sp))
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
            .border(width = 1.dp, color = Color(0x4755E6A5), shape = RoundedCornerShape(0.dp))
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
        PillButton(stringResource(R.string.pokemon_coin), filled = false, onClick = onCoin)
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
private fun PokemonConfigSheet(
    count: Int,
    startingPrizes: Int,
    onApply: (Int, Int) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var c by remember { mutableIntStateOf(count) }
    var prizes by remember { mutableIntStateOf(startingPrizes) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(6, 4, 3)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.pokemon_players), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Segmented(
                options = listOf("2", "3", "4"),
                selectedIndex = (c - 2).coerceIn(0, 2),
                onSelect = { c = it + 2 },
            )
            Text(stringResource(R.string.pokemon_starting_prizes), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == prizes
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp))
                            .background(if (active) Palette.Cyan else Color.Transparent)
                            .then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { prizes = p }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    ) {
                        Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(c, prizes); onClose() },
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
                POKEMON_COLORS.forEach { c ->
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
    players: List<PokemonPlayer>,
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
            val angle = ((rotation.value % 360f) + 360f) % 360f
            val showingHeads = if (settled) heads else angle < 90f || angle > 270f
            Box(
                Modifier.size(120.dp)
                    .graphicsLayer {
                        rotationY = rotation.value
                        cameraDistance = 14f * density
                    }
                    .clip(CircleShape)
                    .background(if (showingHeads) Palette.GamePokemon else Palette.Cyan)
                    .border(3.dp, Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(if (showingHeads) R.string.pokemon_heads else R.string.pokemon_tails),
                    color = Palette.OnAccentDeep,
                    modifier = if (showingHeads) Modifier else Modifier.graphicsLayer { rotationY = 180f },
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp),
                )
            }
            Text(
                if (settled) stringResource(R.string.pokemon_starts, players.getOrNull(starter)?.name ?: "") else stringResource(R.string.pokemon_flipping),
                color = if (settled) Palette.Mint else Palette.TextSecondary,
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = if (settled) 24.sp else 18.sp, letterSpacing = 1.sp),
            )
            if (settled) PillButton(stringResource(R.string.magic_ok), filled = true, onClick = onClose)
        }
    }
}
