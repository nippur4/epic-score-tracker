package com.epichypernova.scoretracker.ui.screens.chinchon

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
import androidx.compose.runtime.Composable
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
import com.epichypernova.scoretracker.data.model.ChinchonPlayer
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.GameEmblem
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private fun paneGradient(base: Color): List<Color> {
    val deep = Color(0xFF0E1B33)
    fun mix(t: Float) = Color(base.red * (1 - t) + deep.red * t, base.green * (1 - t) + deep.green * t, base.blue * (1 - t) + deep.blue * t, 1f)
    return listOf(mix(0.42f), mix(0.72f), deep)
}

@Composable
fun ChinchonScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.chinchonGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        var target by remember { mutableIntStateOf(100) }
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.CHINCHON,
            minPlayers = 2, maxPlayers = 4, defaultCount = 2, onBack = onBack,
            options = {
                SheetLabel(stringResource(R.string.score_target))
                PresetChips(listOf(50, 100, 150), target, Palette.Cyan) { target = it }
            },
            onStart = { players -> repo.update { AppActions.chinchonStart(it, players, target) } },
        )
        return
    }
    val players = game.players

    var showConfig by remember { mutableStateOf(false) }
    var addForIndex by remember { mutableIntStateOf(-1) }

    val bar: @Composable () -> Unit = {
        CentralBar(game.target, onReset = { repo.update { AppActions.chinchonReset(it) } }, onConfig = { showConfig = true })
    }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize().gameInsets()) {
            when (players.size) {
                2 -> {
                    Pane(players[0], 0, game.target, repo, rotated = true, onAdd = { addForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                    bar()
                    Pane(players[1], 1, game.target, repo, rotated = false, onAdd = { addForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                3 -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Pane(players[0], 0, game.target, repo, rotated = true, onAdd = { addForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        Pane(players[1], 1, game.target, repo, rotated = true, onAdd = { addForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Pane(players[2], 2, game.target, repo, rotated = false, onAdd = { addForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxWidth())
                }
                else -> {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Pane(players[0], 0, game.target, repo, rotated = true, onAdd = { addForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        Pane(players[1], 1, game.target, repo, rotated = true, onAdd = { addForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    bar()
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Pane(players[2], 2, game.target, repo, rotated = false, onAdd = { addForIndex = 2 }, modifier = Modifier.weight(1f).fillMaxHeight())
                        Pane(players[3], 3, game.target, repo, rotated = false, onAdd = { addForIndex = 3 }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }

    if (addForIndex >= 0) {
        val idx = addForIndex
        NumberPadSheet(
            title = stringResource(R.string.chinchon_add_title),
            accent = Palette.Cyan,
            confirmLabel = stringResource(R.string.score_add),
            onConfirm = { amount -> repo.update { AppActions.chinchonAdd(it, idx, amount) } },
            onClose = { addForIndex = -1 },
            max = 200,
        )
    }
    if (showConfig) {
        ConfigSheet(
            target = game.target,
            onApply = { t -> repo.update { AppActions.chinchonSetTarget(it, t) } },
            onNew = { repo.update { AppActions.chinchonNew(it) } },
            onFinish = { repo.update { AppActions.chinchonFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun Pane(player: ChinchonPlayer, index: Int, target: Int, repo: Repository, rotated: Boolean, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val grad = paneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(player.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(player.name.uppercase(), color = Color(player.color), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp))
            }
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                Text("${player.score}", color = Color.White, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 58.sp, fontFeatureSettings = "tnum"))
                Text("/$target", color = Palette.TextMuted, modifier = Modifier.padding(bottom = 8.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp))
            }
            Box(
                Modifier.padding(top = 6.dp).height(44.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onAdd() }.padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.score_add_short), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp)) }
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(stringResource(R.string.chinchon_corte), icon = { GameIcon(R.drawable.ic_scissors, Palette.TextSecondary, 14) }) { repo.update { AppActions.chinchonAdd(it, index, -10) } }
                Chip(stringResource(R.string.chinchon_win), icon = { GameEmblem(GameType.CHINCHON, Palette.GameChinchon, 16) }) { repo.update { AppActions.chinchonInstantWin(it, index) } }
            }
        }
    }
}

@Composable
private fun Chip(text: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@Composable
private fun CentralBar(target: Int, onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, Color(0x474FB35B), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.score_target) + ": $target", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.2.sp))
        Pill(stringResource(R.string.score_reset), onReset)
        Pill("⚙", onConfig)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(Modifier.height(36.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(target: Int, onApply: (Int) -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var t by remember { mutableIntStateOf(target) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.score_target))
            PresetChips(listOf(50, 100, 150), t, Palette.Cyan) { t = it }
            SheetPrimaryButton(stringResource(R.string.done)) { onApply(t); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
