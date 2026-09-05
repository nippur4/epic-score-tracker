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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.CompactHeader
import com.epichypernova.scoretracker.ui.components.FinishMenu
import com.epichypernova.scoretracker.ui.components.SecondaryButton
import com.epichypernova.scoretracker.ui.components.ThinProgressBar
import com.epichypernova.scoretracker.ui.components.rowSurface
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private const val LABEL_COL = 34

/** Which entry sheet is open. */
private data class SheetReq(val roundIndex: Int, val bidsPhase: Boolean)

@Composable
fun GenericTableScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
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

    val bids = game.rules.bidsEnabled
    // A round is "points-pending" only in bids mode: it has bids but no hits yet.
    val pendingRound = if (bids) game.rounds.firstOrNull { r -> r.cells.any { it.hits == null } } else null

    var sheet by remember { mutableStateOf<SheetReq?>(null) }
    var menu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBg)) {
        Box(Modifier.fillMaxWidth()) {
            CompactHeader(
                title = game.name ?: "",
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
                onTrailing = { menu = true },
            )
            Box(Modifier.align(Alignment.TopEnd)) {
                FinishMenu(menu, { menu = false }, onFinish = { repo.update { AppActions.finishGeneric(it) } })
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.width(LABEL_COL.dp))
                    players.forEach { p ->
                        Column(Modifier.weight(1f).padding(horizontal = 3.dp)) {
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

            itemsIndexed(game.rounds) { _, round ->
                Row(
                    Modifier.fillMaxWidth().rowSurface(12).clickable { sheet = SheetReq(round.index, bidsPhase = false) }.padding(horizontal = 10.dp, vertical = 8.dp),
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
                                if (cell?.hits == null && bids) "—" else "${cell?.points ?: 0}",
                                color = Palette.TextPrimary,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFeatureSettings = "tnum"),
                            )
                            if (bids && cell?.bid != null) {
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

        // Bottom action
        Row(
            Modifier.fillMaxWidth().background(Palette.AppBg).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val (label, req) = when {
                Derivations.isFinished(game) -> null to null
                bids && pendingRound != null -> stringResource(R.string.load_points) to SheetReq(pendingRound.index, bidsPhase = false)
                bids -> stringResource(R.string.load_bids) to SheetReq(game.rounds.size, bidsPhase = true)
                else -> stringResource(R.string.load_points) to SheetReq(game.rounds.size, bidsPhase = false)
            }
            if (Derivations.isFinished(game)) {
                ChamferCta(text = stringResource(R.string.finish_game), onClick = { repo.update { AppActions.finishGeneric(it) } }, modifier = Modifier.weight(1f))
            } else {
                ChamferCta(text = label!!, onClick = { sheet = req }, modifier = Modifier.weight(1f))
            }
        }
    }

    sheet?.let { req ->
        LoadSheet(
            repo = repo,
            game = game,
            players = players,
            req = req,
            onDismiss = { sheet = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadSheet(
    repo: Repository,
    game: CurrentGame,
    players: List<User>,
    req: SheetReq,
    onDismiss: () -> Unit,
) {
    val existing = game.rounds.firstOrNull { it.index == req.roundIndex }
    val bids = game.rules.bidsEnabled
    val bidsPhase = req.bidsPhase && bids

    // Editable text state per player.
    val bidText = remember { mutableStateMapOf<String, String>().apply { players.forEach { p -> put(p.id, existing?.cells?.firstOrNull { it.playerId == p.id }?.bid?.toString() ?: "") } } }
    val hitsText = remember { mutableStateMapOf<String, String>().apply { players.forEach { p -> put(p.id, existing?.cells?.firstOrNull { it.playerId == p.id }?.hits?.toString() ?: "") } } }
    val extraText = remember { mutableStateMapOf<String, String>().apply { players.forEach { p -> put(p.id, existing?.cells?.firstOrNull { it.playerId == p.id }?.extra?.toString() ?: "") } } }
    val pointsText = remember { mutableStateMapOf<String, String>().apply { players.forEach { p -> put(p.id, if (!bids) existing?.cells?.firstOrNull { it.playerId == p.id }?.points?.toString() ?: "" else "") } } }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.SheetSurface,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 38.dp, height = 4.dp).clip(RoundedCornerShape(999.dp)).background(Palette.ButtonBorder)) },
    ) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 16.dp).padding(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val titleRes = if (bidsPhase) R.string.load_bids else R.string.load_points
            Text(
                stringResource(titleRes) + " · " + stringResource(R.string.hand_n, req.roundIndex + 1),
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                players.forEach { p ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(p.name, color = Color(p.color), maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp))
                        when {
                            bidsPhase -> {
                                FieldLabel(stringResource(R.string.col_bid))
                                NumField(bidText[p.id].orEmpty(), { bidText[p.id] = it }, accent = true)
                            }
                            bids -> {
                                Text(stringResource(R.string.col_bid) + ": " + (bidText[p.id].orEmpty().ifBlank { "—" }), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp))
                                FieldLabel(stringResource(R.string.col_hits))
                                NumField(hitsText[p.id].orEmpty(), { hitsText[p.id] = it }, accent = true)
                                FieldLabel(stringResource(R.string.col_extra))
                                NumField(extraText[p.id].orEmpty(), { extraText[p.id] = it }, accent = false)
                                val bidV = bidText[p.id]?.toIntOrNull()
                                val hitsV = hitsText[p.id]?.toIntOrNull() ?: 0
                                val pts = (if (bidV != null && bidV == hitsV) game.rules.pointsPerHit else 0) + (extraText[p.id]?.toIntOrNull() ?: 0)
                                Text("= $pts", color = Palette.CyanNumber, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFeatureSettings = "tnum"))
                            }
                            else -> {
                                NumField(pointsText[p.id].orEmpty(), { pointsText[p.id] = it }, accent = true)
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.cancel), onClick = onDismiss, modifier = Modifier.width(100.dp), height = 50)
                ChamferCta(
                    text = if (bidsPhase) stringResource(R.string.save_bids) else stringResource(R.string.save_hand),
                    onClick = {
                        repo.update { s ->
                            when {
                                bidsPhase -> AppActions.saveBids(s, req.roundIndex, players.associate { it.id to bidText[it.id]?.toIntOrNull() })
                                bids -> AppActions.saveHandPoints(
                                    s, req.roundIndex,
                                    players.associate { it.id to (hitsText[it.id]?.toIntOrNull() ?: 0) },
                                    players.associate { it.id to (extraText[it.id]?.toIntOrNull() ?: 0) },
                                )
                                else -> AppActions.savePoints(s, req.roundIndex, players.associate { it.id to (pointsText[it.id]?.toIntOrNull() ?: 0) })
                            }
                        }
                        onDismiss()
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
private fun FieldLabel(text: String) {
    Text(text, color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 9.sp, letterSpacing = 0.8.sp))
}

@Composable
private fun NumField(value: String, onValue: (String) -> Unit, accent: Boolean) {
    fun clamp(n: Int) = n.coerceIn(-99, 999)
    fun current() = value.toIntOrNull() ?: 0
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier.fillMaxWidth().height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (accent) Color(0x1A2FD3F0) else Palette.ControlFill)
                .border(1.5.dp, if (accent) Palette.Cyan else Palette.ButtonBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { new -> onValue(new.filter { it.isDigit() || it == '-' }.take(4)) },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = if (accent) Palette.CyanNumber else Palette.TextPrimary,
                    textAlign = TextAlign.Center, fontFeatureSettings = "tnum",
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(Palette.Cyan),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            StepBtn("−", Modifier.weight(1f)) { onValue(clamp(current() - 1).toString()) }
            StepBtn("+", Modifier.weight(1f)) { onValue(clamp(current() + 1).toString()) }
        }
    }
}

@Composable
private fun StepBtn(symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(26.dp).clip(RoundedCornerShape(8.dp)).background(Palette.ControlFill).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
    }
}
