package com.epichypernova.scoretracker.ui.screens.bowling

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.BowlingFrame
import com.epichypernova.scoretracker.data.BowlingScoring
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.BowlingPlayer
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.NameEditDialog
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameBowling

@Composable
fun BowlingScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.bowlingGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.BOWLING,
            minPlayers = 1, maxPlayers = 8, defaultCount = 2, onBack = onBack,
            onStart = { players -> repo.update { AppActions.bowlingStart(it, players) } },
        )
        return
    }
    val players = game.players
    val current = players.getOrNull(game.turn)
    val standing = current?.let { BowlingScoring.pinsStanding(it.rolls) } ?: 0
    val frameNo = current?.let { BowlingScoring.currentFrame(it.rolls) + 1 } ?: 10

    var renameFor by remember { mutableIntStateOf(-1) }
    var showConfig by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep).gameInsets()) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(R.drawable.ic_pin, Color(current?.color ?: 0xFFFFFFFF), 16)
            Text(
                if (current != null && !game.finished) stringResource(R.string.bowling_turn, current.name) + " · ${stringResource(R.string.bowling_frame)} ${frameNo.coerceAtMost(10)}" else "—",
                color = Color(current?.color ?: 0xFFFFFFFF), modifier = Modifier.weight(1f), maxLines = 1,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp),
            )
            GamePill(stringResource(R.string.bowling_undo)) { repo.update { AppActions.bowlingUndo(it) } }
            GamePill("＋") { repo.update { AppActions.bowlingAddPlayer(it) } }
            GamePill("⚙") { showConfig = true }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(players) { i, p ->
                PlayerCard(p, active = i == game.turn && !game.finished, onRename = { renameFor = i })
            }
        }

        // pin pad
        Column(Modifier.fillMaxWidth().background(Color(0xFF0E1B33)).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GameIcon(R.drawable.ic_pin, ACCENT, 14)
                SheetLabel(stringResource(R.string.bowling_pins))
            }
            listOf(0..5, 6..10).forEach { range ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    range.forEach { n ->
                        val enabled = !game.finished && n <= standing
                        val isAll = n == standing && n > 0
                        Box(
                            Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp))
                                .background(if (!enabled) Color(0x0AFFFFFF) else if (isAll) ACCENT else Color(0x14FFFFFF))
                                .clickable(enabled = enabled) { repo.update { AppActions.bowlingRoll(it, n) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (isAll && n == 10) "X" else if (isAll) "/" else "$n", color = if (!enabled) Palette.TextMuted else if (isAll) Palette.OnAccent else Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp))
                        }
                    }
                    if (range.first == 6) Box(Modifier.weight(1f))
                }
            }
        }
    }

    if (renameFor >= 0) {
        NameEditDialog(
            players.getOrNull(renameFor)?.name ?: "",
            onSave = { n -> repo.update { AppActions.bowlingSetName(it, renameFor, n) } },
            onClose = { renameFor = -1 },
            onRemove = if (players.size > 1) ({ repo.update { AppActions.bowlingRemovePlayer(it, renameFor) } }) else null,
        )
    }
    if (showConfig) {
        ConfigSheet(
            onAdd = { repo.update { AppActions.bowlingAddPlayer(it) } },
            onReset = { repo.update { AppActions.bowlingReset(it) } },
            onNew = { repo.update { AppActions.bowlingNew(it) } },
            onFinish = { repo.update { AppActions.bowlingFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

/** Scoresheet symbol for roll [i] of a frame: X strike, / spare, - gutter, or the pin count. */
private fun mark(fr: BowlingFrame, i: Int): String {
    val r = fr.rolls.getOrNull(i) ?: return ""
    val prev = fr.rolls.getOrNull(i - 1)
    val strike = r == 10 && (i == 0 || prev == 10 || (i == 2 && fr.rolls[0] != 10 && fr.rolls[0] + fr.rolls[1] == 10))
    return when {
        strike -> "X"
        i > 0 && prev != null && prev != 10 && prev + r == 10 -> "/"
        r == 0 -> "-"
        else -> "$r"
    }
}

@Composable
private fun PlayerCard(p: BowlingPlayer, active: Boolean, onRename: () -> Unit) {
    val tint = Color(p.color)
    val frames = BowlingScoring.frames(p.rolls)
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(if (active) tint.copy(alpha = 0.14f) else Color(0x0DFFFFFF)).border(1.dp, tint.copy(alpha = if (active) 0.8f else 0.25f), shape).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { PlayerNameRow(p.name, tint, onClick = onRename) }
            Text("${BowlingScoring.total(p.rolls)}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, fontFeatureSettings = "tnum"))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            frames.forEachIndexed { fi, fr ->
                val tenth = fi == 9
                Column(
                    Modifier.weight(if (tenth) 1.4f else 1f).clip(RoundedCornerShape(6.dp)).background(Color(0x14FFFFFF)).border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(6.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(Modifier.fillMaxWidth().height(18.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        val slots = if (tenth) 3 else 2
                        repeat(slots) { i ->
                            val m = mark(fr, i)
                            Text(m, color = if (m == "X" || m == "/") ACCENT else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(22.dp).background(Color(0x0FFFFFFF)), contentAlignment = Alignment.Center) {
                        Text(fr.score?.toString() ?: "", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFeatureSettings = "tnum"))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(onAdd: () -> Unit, onReset: () -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetPrimaryButton(stringResource(R.string.player_add), ACCENT) { onAdd(); onClose() }
            SheetSecondaryButton(stringResource(R.string.score_reset)) { onReset(); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
