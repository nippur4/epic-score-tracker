package com.epichypernova.scoretracker.ui.screens.magic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.epichypernova.scoretracker.data.model.MagicMode
import com.epichypernova.scoretracker.data.model.MagicPlayer
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val paneGradients = listOf(
    listOf(Color(0xFF2A2354), Color(0xFF171B3D), Color(0xFF121533)),  // violet
    listOf(Color(0xFF4A2338), Color(0xFF201430), Color(0xFF161029)),  // pink
    listOf(Color(0xFF123A48), Color(0xFF10283A), Color(0xFF0E2033)),  // cyan
    listOf(Color(0xFF1D3A5C), Color(0xFF13253F), Color(0xFF101B33)),  // blue
)

@Composable
fun MagicScreen(
    repo: Repository,
    state: AppState,
    commander: Boolean,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit,
) {
    val mode = if (commander) MagicMode.COMMANDER else MagicMode.ONE_V_ONE
    LaunchedEffect(mode) { repo.update { AppActions.magicEnsure(it, mode) } }
    val game = state.magicGame ?: return
    if (game.mode != mode) return

    var starter by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        if (!commander) {
            MagicPane(game.players[0], 0, repo, commander = false, rotated = true, modifier = Modifier.weight(1f).fillMaxWidth())
            CentralBar(
                label = stringResource(R.string.magic_lives, game.startingLife),
                onReset = { repo.update { AppActions.magicReset(it) } },
                onShuffle = { starter = (0 until game.players.size).random() },
                onSwitchMode = onSwitchMode,
                switchLabel = stringResource(R.string.magic_mode_commander),
            )
            MagicPane(game.players[1], 1, repo, commander = false, rotated = false, modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                MagicPane(game.players[0], 0, repo, commander = true, rotated = true, modifier = Modifier.weight(1f).fillMaxHeight())
                MagicPane(game.players[1], 1, repo, commander = true, rotated = true, modifier = Modifier.weight(1f).fillMaxHeight())
            }
            Box(Modifier.height(2.dp))
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                MagicPane(game.players[2], 2, repo, commander = true, rotated = false, modifier = Modifier.weight(1f).fillMaxHeight())
                MagicPane(game.players[3], 3, repo, commander = true, rotated = false, modifier = Modifier.weight(1f).fillMaxHeight())
            }
            CentralBar(
                label = stringResource(R.string.magic_commander_lives, game.startingLife),
                onReset = { repo.update { AppActions.magicReset(it) } },
                onShuffle = { starter = (0 until game.players.size).random() },
                onSwitchMode = onSwitchMode,
                switchLabel = stringResource(R.string.magic_mode_1v1),
            )
        }
    }

    if (starter != null) {
        val name = game.players.getOrNull(starter!!)?.name ?: "?"
        AlertDialog(
            onDismissRequest = { starter = null },
            confirmButton = { TextButton(onClick = { starter = null }) { Text(stringResource(R.string.magic_ok), color = Palette.Cyan) } },
            title = { Text(stringResource(R.string.magic_starts, name), color = Palette.TextPrimary) },
            containerColor = Palette.SheetSurface,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MagicPane(
    player: MagicPlayer,
    index: Int,
    repo: Repository,
    commander: Boolean,
    rotated: Boolean,
    modifier: Modifier = Modifier,
) {
    val grad = paneGradients[index % paneGradients.size]
    val bg = Modifier.drawBehind {
        if (player.eliminated) {
            drawRect(Color(0xFF131A2E))
        } else {
            val shader = RadialGradientShader(
                center = Offset(size.width * 0.5f, size.height * 1.0f),
                radius = size.height * 0.95f,
                colors = grad,
                colorStops = listOf(0f, 0.6f, 1f),
            )
            drawRect(brush = ShaderBrush(shader))
        }
    }
    Box(modifier.then(bg)) {
        Column(
            Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                player.name.uppercase(),
                color = if (player.eliminated) Palette.Cyan else Color(player.color),
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = if (commander) 10.5.sp else 11.sp, letterSpacing = 1.9.sp),
            )
            if (player.eliminated) {
                Text("${player.life}", color = Color(0xFF3E4A6B), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = if (commander) 62.sp else 88.sp, fontFeatureSettings = "tnum"))
                Text(stringResource(R.string.magic_eliminated), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.6.sp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (commander) 12.dp else 20.dp)) {
                    LifeButton("−") { repo.update { AppActions.magicLife(it, index, -1) } }
                    Text(
                        "${player.life}",
                        color = Color.White,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = if (commander) 62.sp else 88.sp, fontFeatureSettings = "tnum"),
                    )
                    LifeButton("＋") { repo.update { AppActions.magicLife(it, index, +1) } }
                }
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CounterChip("☠", player.poison, Palette.Mint,
                        onInc = { repo.update { AppActions.magicPoison(it, index, +1) } },
                        onDec = { repo.update { AppActions.magicPoison(it, index, -1) } })
                    if (commander) {
                        CounterChip("⚔", player.commanderDamage, Palette.MagentaNumber,
                            onInc = { repo.update { AppActions.magicCommanderDamage(it, index, +1) } },
                            onDec = { repo.update { AppActions.magicCommanderDamage(it, index, -1) } })
                    } else {
                        CounterChip("⚡", player.energy, Palette.Cyan,
                            onInc = { repo.update { AppActions.magicEnergy(it, index, +1) } },
                            onDec = { repo.update { AppActions.magicEnergy(it, index, -1) } })
                    }
                }
            }
        }
    }
}

@Composable
private fun LifeButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier.size(58.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 26.sp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CounterChip(glyph: String, value: Int, glyphColor: Color, onInc: () -> Unit, onDec: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).combinedClickable(onClick = onInc, onLongClick = onDec).padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(glyph, color = glyphColor, style = TextStyle(fontSize = 13.sp))
        Text("$value", color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
    }
}

@Composable
private fun CentralBar(
    label: String,
    onReset: () -> Unit,
    onShuffle: () -> Unit,
    onSwitchMode: () -> Unit,
    switchLabel: String,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Palette.AppBgDeep)
            .border(width = 1.dp, color = Color(0x472FD3F0), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label.uppercase(), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.5.sp), modifier = Modifier.weight(1f))
        PillButton(stringResource(R.string.magic_reset), filled = false, onClick = onReset)
        PillButton(stringResource(R.string.magic_shuffle), filled = true, onClick = onShuffle)
        PillButton(switchLabel, filled = false, onClick = onSwitchMode)
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
