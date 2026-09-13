@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.onepiece

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
import com.epichypernova.scoretracker.data.model.OnePiecePlayer
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlin.random.Random

private val DAMAGE = Color(0xFFFF6B6B)
private val HEAL = Palette.Mint
private val DON = Color(0xFFF2C94C)

private val OP_COLORS = listOf(
    0xFFE0492F, 0xFF4AA3FF, 0xFF55E6A5, 0xFFA18AF5, 0xFF2FD3F0, 0xFFFF6FA8,
    0xFFF27BA9, 0xFFF2B33B, 0xFFF2ECD0, 0xFF6E6A86, 0xFF4FB35B,
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
fun OnePieceScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { repo.update { AppActions.onePieceEnsureExists(it) } }
    val game = state.onePieceGame ?: return
    val players = game.players
    if (players.size < 2) return

    var showConfig by remember { mutableStateOf(false) }
    var showCoin by remember { mutableStateOf(false) }
    var colorForIndex by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            OnePiecePane(players[0], 0, game.startingLife, repo, rotated = true, onPickColor = { colorForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
            CentralBar(
                onCoin = { showCoin = true },
                onReset = { repo.update { AppActions.onePieceReset(it) } },
                onConfig = { showConfig = true },
            )
            OnePiecePane(players[1], 1, game.startingLife, repo, rotated = false, onPickColor = { colorForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
        }

        if (showCoin) CoinFlipOverlay(players = players, onClose = { showCoin = false })
    }

    if (showConfig) {
        OnePieceConfigSheet(
            startingLife = game.startingLife,
            onApply = { life -> repo.update { AppActions.onePieceSetLife(it, life) } },
            onFinish = { repo.update { AppActions.onePieceFinish(it) } },
            onClose = { showConfig = false },
        )
    }
    if (colorForIndex >= 0) {
        ColorPickDialog(
            current = players.getOrNull(colorForIndex)?.color ?: 0,
            onPick = { c -> repo.update { AppActions.onePieceSetColor(it, colorForIndex, c) } },
            onClose = { colorForIndex = -1 },
        )
    }
}

@Composable
private fun OnePiecePane(
    player: OnePiecePlayer,
    index: Int,
    startingLife: Int,
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
                Text(stringResource(R.string.onepiece_defeated), color = Palette.Cyan, modifier = Modifier.padding(top = 10.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.8.sp))
            } else {
                Text(stringResource(R.string.onepiece_life), color = Palette.TextTertiary, modifier = Modifier.padding(top = 8.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.6.sp))
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(startingLife) { i ->
                        val filled = i < player.life
                        Box(
                            Modifier.size(16.dp).clip(CircleShape)
                                .background(if (filled) DAMAGE else Color(0x22FFFFFF))
                                .border(1.dp, if (filled) Color(0x55FFFFFF) else Color(0x22FFFFFF), CircleShape),
                        )
                    }
                }
                Row(
                    Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StepButton("−", DAMAGE) { repo.update { AppActions.onePieceLife(it, index, -1) } }
                    Text(
                        "${player.life}",
                        color = Color.White,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, fontFeatureSettings = "tnum"),
                    )
                    StepButton("＋", HEAL) { repo.update { AppActions.onePieceLife(it, index, +1) } }
                }
                // DON!! counter
                Row(
                    Modifier.padding(top = 10.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.onepiece_don), color = DON, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp))
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { repo.update { AppActions.onePieceDon(it, index, -1) } }, contentAlignment = Alignment.Center) {
                        Text("−", color = Palette.TextPrimary, style = TextStyle(fontSize = 18.sp))
                    }
                    Text("${player.don}", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = 24.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFeatureSettings = "tnum"))
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { repo.update { AppActions.onePieceDon(it, index, +1) } }, contentAlignment = Alignment.Center) {
                        Text("+", color = Palette.TextPrimary, style = TextStyle(fontSize = 18.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(symbol: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp).clip(CircleShape).border(1.dp, accent.copy(alpha = 0.55f), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = accent, style = TextStyle(fontSize = 26.sp))
    }
}

@Composable
private fun CentralBar(onCoin: () -> Unit, onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x47E0492F), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.weight(1f))
        PillButton("⊚") { onCoin() }
        PillButton(stringResource(R.string.onepiece_reset)) { onReset() }
        PillButton("⚙") { onConfig() }
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
private fun OnePieceConfigSheet(
    startingLife: Int,
    onApply: (Int) -> Unit,
    onFinish: () -> Unit,
    onClose: () -> Unit,
) {
    var life by remember { mutableIntStateOf(startingLife) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(5, 4, 3)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.onepiece_starting_life), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == life
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) Palette.Cyan else Color.Transparent)
                            .then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { life = p }.padding(horizontal = 18.dp, vertical = 9.dp),
                    ) { Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
                }
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(life); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.onepiece_finish), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
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
                OP_COLORS.forEach { c ->
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

@Composable
private fun CoinFlipOverlay(players: List<OnePiecePlayer>, onClose: () -> Unit) {
    var settled by remember { mutableStateOf(false) }
    var heads by remember { mutableStateOf(true) }
    var starter by remember { mutableIntStateOf(-1) }
    val rotation = remember { Animatable(0f) }
    val whoosh = rememberSoundEffect(R.raw.whoosh)
    val victory = rememberSoundEffect(R.raw.victory)

    LaunchedEffect(Unit) {
        whoosh()
        val landsHeads = Random.nextBoolean()
        rotation.animateTo(360f * 6 + (if (landsHeads) 0f else 180f), animationSpec = tween(1500))
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
                    .graphicsLayer { rotationY = rotation.value; cameraDistance = 14f * density }
                    .clip(CircleShape)
                    .background(if (showingHeads) Palette.GameOnePiece else Palette.Cyan)
                    .border(3.dp, Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(if (showingHeads) R.string.yugioh_heads else R.string.yugioh_tails),
                    color = Palette.OnAccentDeep,
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
            if (settled) Box(
                Modifier.height(40.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onClose() }.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.magic_ok), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp)) }
        }
    }
}
