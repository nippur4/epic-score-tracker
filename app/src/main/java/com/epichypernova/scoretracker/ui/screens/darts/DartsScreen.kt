package com.epichypernova.scoretracker.ui.screens.darts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.epichypernova.scoretracker.data.model.DartsPlayer
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.components.gamePaneGradient
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameDarts

@Composable
fun DartsScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.dartsGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        var start by remember { mutableIntStateOf(501) }
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.DARTS,
            minPlayers = 2, maxPlayers = 8, defaultCount = 2, onBack = onBack,
            options = {
                SheetLabel(stringResource(R.string.darts_start))
                PresetChips(listOf(301, 501, 701), start, ACCENT) { start = it }
            },
            onStart = { players -> repo.update { AppActions.dartsStart(it, players, start) } },
        )
        return
    }
    val players = game.players

    var showConfig by remember { mutableStateOf(false) }
    var throwForIndex by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize().gameInsets()) {
            if (players.size == 2) {
                // Head to head: the top pane is flipped so the player across the table reads it.
                DartPane(players[0], rotated = true, onThrow = { throwForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
                CentralBar(game.startScore, onReset = { repo.update { AppActions.dartsReset(it) } }, onConfig = { showConfig = true })
                DartPane(players[1], rotated = false, onThrow = { throwForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                CentralBar(game.startScore, onReset = { repo.update { AppActions.dartsReset(it) } }, onConfig = { showConfig = true })
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(players) { i, p -> DartCard(p, onThrow = { throwForIndex = i }) }
                }
            }
        }
    }

    if (throwForIndex >= 0) {
        val idx = throwForIndex
        NumberPadSheet(
            title = stringResource(R.string.darts_throw_title),
            subtitle = "${players.getOrNull(idx)?.name ?: ""} · ${players.getOrNull(idx)?.remaining ?: 0} → ?",
            accent = ACCENT,
            confirmLabel = stringResource(R.string.darts_throw),
            onConfirm = { amount -> repo.update { AppActions.dartsThrow(it, idx, amount) } },
            onClose = { throwForIndex = -1 },
            quickAdds = listOf(100, 60, 40, 20),
            max = 180,
        )
    }
    if (showConfig) {
        ConfigSheet(
            start = game.startScore,
            onApply = { st -> repo.update { AppActions.dartsSetStart(it, st) } },
            onNew = { repo.update { AppActions.dartsNew(it) } },
            onFinish = { repo.update { AppActions.dartsFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun ThrowButton(onThrow: () -> Unit, compact: Boolean = false) {
    Row(
        Modifier.height(if (compact) 40.dp else 46.dp).clip(RoundedCornerShape(999.dp)).background(ACCENT).clickable { onThrow() }.padding(horizontal = if (compact) 18.dp else 26.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GameIcon(R.drawable.ic_target, Color.White, if (compact) 16 else 18)
        Text(stringResource(R.string.darts_throw), color = Color.White, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = if (compact) 13.sp else 15.sp))
    }
}

@Composable
private fun DartPane(player: DartsPlayer, rotated: Boolean, onThrow: () -> Unit, modifier: Modifier = Modifier) {
    val grad = gamePaneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(player.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(player.name.uppercase(), color = Color(player.color), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp))
            }
            Text("${player.remaining}", color = Color.White, modifier = Modifier.padding(top = 2.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 66.sp, fontFeatureSettings = "tnum"))
            Box(Modifier.padding(top = 6.dp)) { ThrowButton(onThrow) }
        }
    }
}

/** Compact row used when more than two people play. */
@Composable
private fun DartCard(p: DartsPlayer, onThrow: () -> Unit) {
    val tint = Color(p.color)
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(Color(0x0DFFFFFF)).border(1.dp, tint.copy(alpha = 0.35f), shape).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f)) { PlayerNameRow(p.name, tint) }
        Text("${p.remaining}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, fontFeatureSettings = "tnum"))
        ThrowButton(onThrow, compact = true)
    }
}

@Composable
private fun CentralBar(start: Int, onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GameIcon(R.drawable.ic_target, ACCENT, 16)
        Text("$start", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.2.sp))
        GamePill(stringResource(R.string.score_reset)) { onReset() }
        GamePill("⚙") { onConfig() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(start: Int, onApply: (Int) -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var st by remember { mutableIntStateOf(start) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.darts_start))
            PresetChips(listOf(301, 501, 701), st, ACCENT) { st = it }
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(st); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
