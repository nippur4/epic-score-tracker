package com.epichypernova.scoretracker.ui.screens.escoba

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
import androidx.compose.runtime.LaunchedEffect
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
import com.epichypernova.scoretracker.data.model.EscobaPlayer
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.LabeledCounter
import com.epichypernova.scoretracker.ui.components.NameEditDialog
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameEscoba

@Composable
fun EscobaScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.escobaEnsureExists(it) } }
    val game = state.escobaGame ?: return
    val players = game.players

    var renameFor by remember { mutableIntStateOf(-1) }
    var showConfig by remember { mutableStateOf(false) }
    var showRound by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("${stringResource(R.string.score_target).uppercase()} ${game.target}", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp))
            GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.escobaReset(it) } }
            GamePill("⚙") { showConfig = true }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(players) { i, p ->
                PlayerCard(p, leader = p.score == players.maxOf { it.score } && p.score > 0, onRename = { renameFor = i },
                    onDec = { repo.update { AppActions.escobaEscoba(it, i, -1) } }, onInc = { repo.update { AppActions.escobaEscoba(it, i, +1) } })
            }
        }
        Box(Modifier.fillMaxWidth().padding(12.dp)) {
            SheetPrimaryButton(stringResource(R.string.escoba_close), ACCENT) { showRound = true }
        }
    }

    if (renameFor >= 0) {
        NameEditDialog(players.getOrNull(renameFor)?.name ?: "", onSave = { n -> repo.update { AppActions.escobaSetName(it, renameFor, n) } }, onClose = { renameFor = -1 })
    }
    if (showRound) {
        RoundSheet(players = players, onApply = { c, o, v, s -> repo.update { AppActions.escobaRound(it, c, o, v, s) } }, onClose = { showRound = false })
    }
    if (showConfig) {
        ConfigSheet(count = players.size, target = game.target,
            onApply = { n, t -> repo.update { AppActions.escobaConfigure(it, n, t) } },
            onFinish = { repo.update { AppActions.escobaFinish(it) } },
            onClose = { showConfig = false })
    }
}

@Composable
private fun PlayerCard(p: EscobaPlayer, leader: Boolean, onRename: () -> Unit, onDec: () -> Unit, onInc: () -> Unit) {
    val tint = Color(p.color)
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(Color(0x0DFFFFFF)).border(1.dp, tint.copy(alpha = if (leader) 0.8f else 0.25f), shape).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayerNameRow(p.name, tint, onClick = onRename)
            LabeledCounter(stringResource(R.string.escoba_escobas), p.escobas, ACCENT, onDec = onDec, onInc = onInc)
        }
        Text("${p.score}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, fontFeatureSettings = "tnum"))
    }
}

/** Who takes each of the four fixed points; -1 = nobody (tie). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundSheet(players: List<EscobaPlayer>, onApply: (Int, Int, Int, Int) -> Unit, onClose: () -> Unit) {
    var cartas by remember { mutableIntStateOf(-1) }
    var oros by remember { mutableIntStateOf(-1) }
    var velo by remember { mutableIntStateOf(-1) }
    var setenta by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SheetLabel(stringResource(R.string.escoba_hand_title))
            PickRow(stringResource(R.string.escoba_cartas), players, cartas) { cartas = it }
            PickRow(stringResource(R.string.escoba_oros), players, oros) { oros = it }
            PickRow(stringResource(R.string.escoba_velo), players, velo) { velo = it }
            PickRow(stringResource(R.string.escoba_setenta), players, setenta) { setenta = it }
            Text(
                players.joinToString(" · ") { p -> "${p.name} +${listOf(cartas, oros, velo, setenta).count { it == players.indexOf(p) } + p.escobas}" },
                color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
            )
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(cartas, oros, velo, setenta); onClose() }
        }
    }
}

@Composable
private fun PickRow(label: String, players: List<EscobaPlayer>, selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 13.sp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (listOf(-1) + players.indices).forEach { i ->
                val active = i == selected
                val tint = if (i < 0) Palette.TextMuted else Color(players[i].color)
                Box(
                    Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(999.dp))
                        .background(if (active) tint.copy(alpha = if (i < 0) 0.3f else 0.9f) else Color.Transparent)
                        .border(1.dp, if (active) tint else Palette.ButtonBorder, RoundedCornerShape(999.dp))
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (i < 0) stringResource(R.string.escoba_nobody) else players[i].name.take(8), color = if (active && i >= 0) Palette.OnAccent else Palette.TextSecondary, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(count: Int, target: Int, onApply: (Int, Int) -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var c by remember { mutableIntStateOf(count) }
    var t by remember { mutableIntStateOf(target) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.score_players))
            Segmented(options = listOf("2", "3", "4"), selectedIndex = (c - 2).coerceIn(0, 2), onSelect = { c = it + 2 })
            SheetLabel(stringResource(R.string.score_target))
            PresetChips(listOf(15, 21, 30), t, ACCENT) { t = it }
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(c, t); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
