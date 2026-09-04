package com.epichypernova.scoretracker.ui.screens.generic

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Derivations
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.CompactHeader
import com.epichypernova.scoretracker.ui.components.SecondaryButton
import com.epichypernova.scoretracker.ui.components.ThinProgressBar
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private const val LABEL_COL = 34

@Composable
fun GenericTableScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    val game = state.currentGame
    if (game == null) {
        Box(Modifier.fillMaxSize().background(Palette.AppBg), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_current_game), color = Palette.TextTertiary)
        }
        return
    }
    val players = game.playerIds.mapNotNull { id -> state.users.firstOrNull { it.id == id } }
    val totals = Derivations.totals(game)
    val best = Derivations.bestTotal(game)
    val leaderName = Derivations.leaders(game).firstOrNull()?.let { id -> players.firstOrNull { it.id == id }?.name } ?: "—"

    var sheetRound by remember { mutableStateOf<Int?>(null) }  // round index being edited/created

    Column(Modifier.fillMaxSize().background(Palette.AppBg)) {
        CompactHeader(
            title = game.name ?: "Partida",
            meta = {
                Text(
                    stringResource(
                        R.string.table_meta,
                        Derivations.currentRoundNumber(game),
                        game.rules.targetScore,
                        stringResource(if (game.rules.lowWins) R.string.wins_menor_word else R.string.wins_mayor_word),
                    ),
                    color = Palette.TextTertiary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                )
            },
            onBack = onBack,
        )

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Totals + bars
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.width(LABEL_COL.dp))
                    players.forEach { p ->
                        Column(
                            Modifier.weight(1f).padding(horizontal = 3.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                p.name,
                                color = Color(p.color),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp, letterSpacing = 0.4.sp),
                            )
                            Text(
                                "${totals[p.id] ?: 0}",
                                color = if ((totals[p.id] ?: 0) == best) Palette.Mint else Palette.TextPrimary,
                                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 25.sp, fontFeatureSettings = "tnum"),
                            )
                            ThinProgressBar(
                                fraction = Derivations.barFraction(game, p.id),
                                color = Color(p.color),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            // Hand rows
            itemsIndexed(game.rounds) { _, round ->
                Row(
                    Modifier.fillMaxWidth().rowSurfaceLocal().clickable { sheetRound = round.index }.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "M${round.index + 1}",
                        modifier = Modifier.width((LABEL_COL - 10).dp),
                        color = Palette.TextMuted,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 0.9.sp),
                    )
                    players.forEach { p ->
                        val cell = round.cells.firstOrNull { it.playerId == p.id }
                        Column(Modifier.weight(1f).padding(horizontal = 3.dp)) {
                            Text(
                                "${cell?.points ?: 0}",
                                color = Palette.TextPrimary,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFeatureSettings = "tnum"),
                            )
                            if (game.rules.bidsEnabled && cell?.bid != null) {
                                Text(
                                    stringResource(R.string.bid_pedia, cell.bid),
                                    color = Color(0xFF5E7099),
                                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp),
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.leader_note, leaderName, best, game.rules.targetScore),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp, end = 16.dp),
                    color = Palette.TextTertiary,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp),
                )
            }
        }

        // Bottom actions
        Row(
            Modifier.fillMaxWidth().background(Palette.AppBg).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (Derivations.isFinished(game)) {
                ChamferCta(
                    text = stringResource(R.string.finish_game),
                    onClick = { repo.update { AppActions.finishGeneric(it) }; onFinished() },
                    modifier = Modifier.weight(1f),
                )
            } else {
                ChamferCta(
                    text = stringResource(R.string.load_hand),
                    onClick = { sheetRound = game.rounds.size },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (sheetRound != null) {
        LoadRoundSheet(
            game = game,
            players = players,
            roundIndex = sheetRound!!,
            onDismiss = { sheetRound = null },
            onSave = { points, bids ->
                repo.update { AppActions.saveRound(it, sheetRound!!, points, bids) }
                sheetRound = null
            },
        )
    }
}

/** Local row surface to avoid import ambiguity within this file. */
private fun Modifier.rowSurfaceLocal(): Modifier = this
    .clip(RoundedCornerShape(12.dp))
    .background(Palette.RowSurface)
    .border(1.dp, Palette.RowBorder, RoundedCornerShape(12.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadRoundSheet(
    game: CurrentGame,
    players: List<com.epichypernova.scoretracker.data.model.User>,
    roundIndex: Int,
    onDismiss: () -> Unit,
    onSave: (Map<String, Int>, Map<String, Int?>) -> Unit,
) {
    val existing = game.rounds.firstOrNull { it.index == roundIndex }
    val points = remember {
        players.associate { p -> p.id to (existing?.cells?.firstOrNull { it.playerId == p.id }?.points?.toString() ?: "") }
            .toMutableMap()
    }
    val bids = remember {
        players.associate { p -> p.id to (existing?.cells?.firstOrNull { it.playerId == p.id }?.bid?.toString() ?: "") }
            .toMutableMap()
    }
    var focused by remember { mutableStateOf(players.firstOrNull()?.id) }
    var editingBid by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) } // force recompose on map edits

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.SheetSurface,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 38.dp, height = 4.dp).clip(RoundedCornerShape(999.dp)).background(Palette.ButtonBorder)) },
    ) {
        @Suppress("UNUSED_EXPRESSION") tick
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.hand_n, roundIndex + 1), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp))
                if (game.rules.bidsEnabled) {
                    Text(stringResource(R.string.bet_points_hint), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                players.forEach { p ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(p.name, color = Color(p.color), maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp))
                        if (game.rules.bidsEnabled) {
                            CellBox(
                                value = bids[p.id].orEmpty(),
                                height = 34,
                                accent = false,
                                focused = focused == p.id && editingBid,
                                onClick = { focused = p.id; editingBid = true },
                            )
                        }
                        CellBox(
                            value = points[p.id].orEmpty(),
                            height = 50,
                            accent = true,
                            focused = focused == p.id && !editingBid,
                            onClick = { focused = p.id; editingBid = false },
                        )
                    }
                }
            }

            // Quick keypad
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3", "5", "⌫").forEach { key ->
                    Box(
                        Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(11.dp)).background(Palette.ControlFill)
                            .clickable {
                                val id = focused ?: return@clickable
                                val map = if (editingBid) bids else points
                                val cur = map[id].orEmpty()
                                map[id] = if (key == "⌫") cur.dropLast(1) else (cur + key).take(3)
                                tick++
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(key, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp))
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.cancel), onClick = onDismiss, modifier = Modifier.width(94.dp), height = 50)
                ChamferCta(
                    text = stringResource(R.string.save_hand),
                    onClick = {
                        onSave(
                            players.associate { it.id to (points[it.id]?.toIntOrNull() ?: 0) },
                            players.associate { it.id to (bids[it.id]?.toIntOrNull()) },
                        )
                    },
                    modifier = Modifier.weight(1f),
                    height = 50,
                    cut = 12,
                    fontSize = 14,
                )
            }
        }
    }
}

@Composable
private fun CellBox(value: String, height: Int, accent: Boolean, focused: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(height.dp)
            .clip(RoundedCornerShape(if (accent) 12.dp else 10.dp))
            .background(if (accent) Color(0x1A2FD3F0) else Palette.ControlFill)
            .border(
                width = if (accent) 1.5.dp else if (focused) 1.5.dp else 0.dp,
                color = if (accent || focused) Palette.Cyan else Color.Transparent,
                shape = RoundedCornerShape(if (accent) 12.dp else 10.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            value,
            color = if (accent) Palette.CyanNumber else Palette.TextPrimary,
            style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = if (accent) 19.sp else 15.sp, fontFeatureSettings = "tnum"),
        )
    }
}
