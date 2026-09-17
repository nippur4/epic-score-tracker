package com.epichypernova.scoretracker.ui.screens.generala

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.GeneralaCat
import com.epichypernova.scoretracker.ui.components.DieFace
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.NameEditDialog
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameGenerala
private val CAT_W = 96.dp
private val COL_W = 58.dp
private val ROW_H = 44.dp

@Composable
private fun catLabel(cat: GeneralaCat): String = when (cat) {
    GeneralaCat.ESCALERA -> stringResource(R.string.generala_cat_escalera)
    GeneralaCat.FULL -> stringResource(R.string.generala_cat_full)
    GeneralaCat.POKER -> stringResource(R.string.generala_cat_poker)
    GeneralaCat.GENERALA -> stringResource(R.string.generala_cat_generala)
    GeneralaCat.DOBLE -> stringResource(R.string.generala_cat_doble)
    else -> "×${cat.face}"
}

/** Row label: a real die face for the number categories, text for the combinations. */
@Composable
private fun CatLabel(cat: GeneralaCat, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (cat.isNumber) DieFace(cat.face, Palette.TextPrimary, Palette.AppBgDeep, size = 20)
        Text(catLabel(cat), color = Palette.TextSecondary, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 12.sp))
    }
}

@Composable
fun GeneralaScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.generalaGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.GENERALA,
            minPlayers = 1, maxPlayers = 12, defaultCount = 2, onBack = onBack,
            onStart = { players -> repo.update { AppActions.generalaStart(it, players) } },
        )
        return
    }
    val players = game.players
    val turn = AppActions.generalaTurn(game)

    var cell by remember { mutableStateOf<Pair<Int, GeneralaCat>?>(null) }
    var renameFor by remember { mutableIntStateOf(-1) }
    var showConfig by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep).gameInsets()) {
        // top bar
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.generala_turn, players.getOrNull(turn)?.name ?: ""),
                color = Color(players.getOrNull(turn)?.color ?: 0xFFFFFFFF), modifier = Modifier.weight(1f), maxLines = 1,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.4.sp),
            )
            GamePill("＋") { repo.update { AppActions.generalaAddPlayer(it) } }
            GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.generalaReset(it) } }
            GamePill("⚙") { showConfig = true }
        }

        val hScroll = rememberScrollState()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).horizontalScroll(hScroll).padding(12.dp)) {
            // header
            Row(Modifier.height(ROW_H), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(CAT_W))
                players.forEachIndexed { i, p ->
                    val isTurn = i == turn
                    Box(
                        Modifier.width(COL_W).height(36.dp).padding(horizontal = 3.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (isTurn) Color(p.color).copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, Color(p.color).copy(alpha = if (isTurn) 0.9f else 0.35f), RoundedCornerShape(10.dp))
                            .clickable { renameFor = i },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(p.name.take(6).uppercase(), color = Color(p.color), maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.8.sp))
                    }
                }
            }
            GeneralaCat.entries.forEach { cat ->
                val sectionStart = cat == GeneralaCat.ESCALERA
                Row(
                    Modifier.height(ROW_H).then(if (sectionStart) Modifier.padding(top = 6.dp) else Modifier).background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CatLabel(cat, Modifier.width(CAT_W).padding(start = 8.dp))
                    players.forEachIndexed { i, p ->
                        val v = p.scores[cat]
                        Box(
                            Modifier.width(COL_W).height(36.dp).padding(horizontal = 3.dp).clip(RoundedCornerShape(8.dp))
                                .background(if (v == null) Color(0x14FFFFFF) else Color(p.color).copy(alpha = 0.16f))
                                .clickable { cell = i to cat },
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                v == null -> Text("·", color = Palette.TextMuted, style = TextStyle(fontSize = 16.sp))
                                v == 0 -> Text("✕", color = Palette.TextMuted, style = TextStyle(fontSize = 14.sp))
                                else -> Text("$v", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFeatureSettings = "tnum"))
                            }
                        }
                    }
                }
            }
            // totals
            Row(Modifier.height(ROW_H).padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.generala_total), color = ACCENT, modifier = Modifier.width(CAT_W).padding(start = 8.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp))
                players.forEach { p ->
                    Text("${AppActions.generalaTotal(p)}", color = Color(p.color), modifier = Modifier.width(COL_W), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, fontFeatureSettings = "tnum"))
                }
            }
        }
    }

    cell?.let { (idx, cat) ->
        val p = players.getOrNull(idx) ?: return@let
        CellSheet(
            playerName = p.name, cat = cat, current = p.scores[cat],
            onPick = { pts, servidaWin -> repo.update { AppActions.generalaScore(it, idx, cat, pts, servidaWin) } },
            onClear = { repo.update { AppActions.generalaClear(it, idx, cat) } },
            onClose = { cell = null },
        )
    }
    if (renameFor >= 0) {
        NameEditDialog(
            players.getOrNull(renameFor)?.name ?: "",
            onSave = { n -> repo.update { AppActions.generalaSetName(it, renameFor, n) } },
            onClose = { renameFor = -1 },
            onRemove = if (players.size > 1) ({ repo.update { AppActions.generalaRemovePlayer(it, renameFor) } }) else null,
        )
    }
    if (showConfig) {
        ConfigSheet(
            onAdd = { repo.update { AppActions.generalaAddPlayer(it) } },
            onNew = { repo.update { AppActions.generalaNew(it) } },
            onFinish = { repo.update { AppActions.generalaFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CellSheet(playerName: String, cat: GeneralaCat, current: Int?, onPick: (Int, Boolean) -> Unit, onClear: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cat.isNumber) DieFace(cat.face, Palette.TextPrimary, Palette.SheetSurface, size = 22)
                Text("$playerName · ${catLabel(cat)}", color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }
            if (cat.isNumber) {
                SheetLabel(stringResource(R.string.generala_dice))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { n ->
                        val v = n * cat.face
                        Box(Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).clickable { onPick(v, false); onClose() }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                    repeat(n) { DieFace(cat.face, Palette.TextTertiary, Color(0xFF1C2A4A), size = 9) }
                                }
                                Text("$v", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp))
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(14.dp)).background(ACCENT).clickable { onPick(cat.fixed, false); onClose() }, contentAlignment = Alignment.Center) {
                        Text("${cat.fixed}", color = Palette.OnAccent, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 20.sp))
                    }
                    if (cat != GeneralaCat.DOBLE) {
                        val isGen = cat == GeneralaCat.GENERALA
                        Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(14.dp)).background(Palette.Mint.copy(alpha = 0.25f)).border(1.dp, Palette.Mint.copy(alpha = 0.7f), RoundedCornerShape(14.dp)).clickable { onPick(cat.servida, isGen); onClose() }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.generala_servida), color = Palette.Mint, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                                Text(if (isGen) "★" else "${cat.servida}", color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                            }
                        }
                    }
                }
                if (cat == GeneralaCat.GENERALA) Text(stringResource(R.string.generala_servida_win), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp))
            }
            SheetSecondaryButton("✕ " + stringResource(R.string.generala_tachar)) { onPick(0, false); onClose() }
            if (current != null) SheetSecondaryButton(stringResource(R.string.generala_clear)) { onClear(); onClose() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(onAdd: () -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetPrimaryButton(stringResource(R.string.player_add), ACCENT) { onAdd(); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
