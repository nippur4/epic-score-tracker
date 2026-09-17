package com.epichypernova.scoretracker.ui.screens.uno

import androidx.compose.animation.core.animateFloatAsState
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
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.UnoPlayer
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.MiniStep
import com.epichypernova.scoretracker.ui.components.NameEditDialog
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameUno

@Composable
fun UnoScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.unoGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        var target by remember { mutableIntStateOf(500) }
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.UNO,
            minPlayers = 2, maxPlayers = 8, defaultCount = 4, onBack = onBack,
            options = {
                SheetLabel(stringResource(R.string.score_target))
                PresetChips(listOf(200, 300, 500), target, ACCENT) { target = it }
            },
            onStart = { players -> repo.update { AppActions.unoStart(it, players, target) } },
        )
        return
    }
    val players = game.players

    var renameFor by remember { mutableIntStateOf(-1) }
    var showConfig by remember { mutableStateOf(false) }
    var showRound by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep).gameInsets()) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(R.drawable.ic_card, ACCENT, 16)
            Text(
                "${stringResource(R.string.uno_rounds).uppercase()} ${game.rounds} · ${stringResource(R.string.score_target).uppercase()} ${game.target}",
                color = Palette.TextMuted, modifier = Modifier.weight(1f), maxLines = 1,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp),
            )
            GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.unoReset(it) } }
            GamePill("⚙") { showConfig = true }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(players) { i, p ->
                PlayerCard(p, game.target, leader = p.score == players.maxOf { it.score } && p.score > 0,
                    onRename = { renameFor = i },
                    onDec = { repo.update { AppActions.unoAdjust(it, i, -1) } },
                    onInc = { repo.update { AppActions.unoAdjust(it, i, +1) } })
            }
        }
        Box(Modifier.fillMaxWidth().padding(12.dp)) {
            SheetPrimaryButton(stringResource(R.string.uno_end_round), ACCENT, icon = R.drawable.ic_card) { showRound = true }
        }
    }

    if (renameFor >= 0) {
        NameEditDialog(players.getOrNull(renameFor)?.name ?: "", onSave = { n -> repo.update { AppActions.unoSetName(it, renameFor, n) } }, onClose = { renameFor = -1 })
    }
    if (showRound) {
        RoundSheet(players = players, onApply = { w, pts -> repo.update { AppActions.unoRound(it, w, pts) } }, onClose = { showRound = false })
    }
    if (showConfig) {
        ConfigSheet(
            target = game.target,
            onApply = { t -> repo.update { AppActions.unoSetTarget(it, t) } },
            onNew = { repo.update { AppActions.unoNew(it) } },
            onFinish = { repo.update { AppActions.unoFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun PlayerCard(p: UnoPlayer, target: Int, leader: Boolean, onRename: () -> Unit, onDec: () -> Unit, onInc: () -> Unit) {
    val tint = Color(p.color)
    val shape = RoundedCornerShape(16.dp)
    val frac by animateFloatAsState((p.score.toFloat() / target).coerceIn(0f, 1f), label = "uno")
    Column(Modifier.fillMaxWidth().clip(shape).background(Color(0x0DFFFFFF)).border(1.dp, tint.copy(alpha = if (leader) 0.8f else 0.25f), shape).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { PlayerNameRow(p.name, tint, onClick = onRename) }
            if (leader) GameIcon(R.drawable.ic_star, ACCENT, 16)
            MiniStep("−", 26, onDec)
            Text("${p.score}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, fontFeatureSettings = "tnum"))
            MiniStep("+", 26, onInc)
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x22FFFFFF))) {
            Box(Modifier.fillMaxWidth(frac).height(6.dp).background(tint))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundSheet(players: List<UnoPlayer>, onApply: (Int, Int) -> Unit, onClose: () -> Unit) {
    var winner by remember { mutableIntStateOf(-1) }
    var handIdx by remember { mutableIntStateOf(0) }
    var hands by remember { mutableStateOf(List(players.size) { 0 }) }
    var currentHand by remember { mutableStateOf(listOf<Int>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val losers = players.indices.filter { it != winner }
    val total = hands.sum() + currentHand.sum()

    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (winner < 0) {
                SheetLabel(stringResource(R.string.uno_who_won))
                players.forEachIndexed { i, p ->
                    Box(Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(999.dp)).background(Color(p.color).copy(alpha = 0.18f)).border(1.dp, Color(p.color).copy(alpha = 0.6f), RoundedCornerShape(999.dp)).clickable { winner = i }, contentAlignment = Alignment.Center) {
                        Text(p.name, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                    }
                }
            } else {
                val li = losers.getOrNull(handIdx)
                val loser = li?.let { players[it] }
                val last = handIdx >= losers.size - 1
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SheetLabel(stringResource(R.string.uno_hand_of, loser?.name ?: ""))
                        Text("${handIdx + 1}/${losers.size}", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp))
                    }
                    Text("${currentHand.sum()}", color = ACCENT, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 34.sp, fontFeatureSettings = "tnum"))
                }
                listOf(listOf(0, 1, 2, 3, 4), listOf(5, 6, 7, 8, 9)).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { n ->
                            Box(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).clickable { currentHand = currentHand + n }, contentAlignment = Alignment.Center) {
                                Text("$n", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x2FFF6FA8)).clickable { currentHand = currentHand + 20 }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                        GameIcon(R.drawable.ic_skip, Color(0xFFFF6FA8), 18)
                        Text(stringResource(R.string.uno_action), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                    }
                    Row(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x2FA18AF5)).clickable { currentHand = currentHand + 50 }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                        GameIcon(R.drawable.ic_wild, Color(0xFFA18AF5), 18)
                        Text(stringResource(R.string.uno_wild), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                    }
                    Box(Modifier.height(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).clickable { currentHand = currentHand.dropLast(1) }.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                        Text("⌫", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp))
                    }
                }
                if (last) {
                    SheetPrimaryButton(stringResource(R.string.uno_apply, total), ACCENT) { onApply(winner, total); onClose() }
                } else {
                    SheetPrimaryButton(stringResource(R.string.uno_next), ACCENT) {
                        if (li != null) hands = hands.toMutableList().also { it[li] = currentHand.sum() }
                        currentHand = emptyList()
                        handIdx += 1
                    }
                }
                Text("${stringResource(R.string.generala_total)} ${players[winner].name}: +$total", color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp))
            }
        }
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
            PresetChips(listOf(200, 300, 500), t, ACCENT) { t = it }
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(t); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
