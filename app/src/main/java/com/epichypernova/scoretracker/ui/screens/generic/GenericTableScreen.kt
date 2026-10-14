package com.epichypernova.scoretracker.ui.screens.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Derivations
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.Cell
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.CompactHeader
import com.epichypernova.scoretracker.ui.components.FinishMenu
import com.epichypernova.scoretracker.ui.components.PickPlayersDialog
import com.epichypernova.scoretracker.ui.components.RulesDialog
import com.epichypernova.scoretracker.ui.components.rememberFinishConfirm
import com.epichypernova.scoretracker.ui.components.SecondaryButton
import com.epichypernova.scoretracker.ui.components.Standing
import com.epichypernova.scoretracker.ui.components.StandingsPill
import com.epichypernova.scoretracker.ui.components.StandingsSheet
import com.epichypernova.scoretracker.ui.components.ThinProgressBar
import com.epichypernova.scoretracker.ui.components.rowSurface
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Pinned column holding the hand number ("M12"). */
private val LABEL_W = 40.dp

/** Narrowest a player column gets before the board starts scrolling sideways. */
private val MIN_COL_W = 64.dp

private val BOARD_PAD = 16.dp

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
    var showAdd by remember { mutableStateOf(false) }
    var showRules by remember { mutableStateOf(false) }
    var showStandings by remember { mutableStateOf(false) }
    val askFinish = rememberFinishConfirm { repo.update { AppActions.finishGeneric(it) } }
    var removeFor by remember { mutableStateOf<User?>(null) }

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
                actions = { StandingsPill { showStandings = true } },
            )
            Box(Modifier.align(Alignment.TopEnd)) {
                FinishMenu(
                    menu,
                    { menu = false },
                    onFinish = askFinish,
                    onAddPlayer = { showAdd = true },
                    onRules = { showRules = true },
                )
            }
        }

        // Names and totals stay put on top; one shared scroll moves every row sideways at once.
        val hScroll = rememberScrollState()

        BoxWithConstraints(Modifier.weight(1f)) {
            // Columns split the width while they fit, then settle on MIN_COL_W and scroll.
            val colW = maxOf(MIN_COL_W, (maxWidth - BOARD_PAD * 2 - LABEL_W) / players.size.coerceAtLeast(1))

            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = BOARD_PAD, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Box(Modifier.width(LABEL_W))
                    Row(Modifier.horizontalScroll(hScroll)) {
                        players.forEach { p ->
                            TotalCell(
                                name = p.name,
                                color = Color(p.color),
                                total = totals[p.id] ?: 0,
                                leading = (totals[p.id] ?: 0) == best,
                                fraction = Derivations.barFraction(game, p.id),
                                width = colW,
                                onClick = { removeFor = p },
                            )
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x17FFFFFF)))

                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(start = BOARD_PAD, end = BOARD_PAD, top = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(game.rounds) { _, round ->
                        Row(
                            Modifier.fillMaxWidth().rowSurface(12).clickable { sheet = SheetReq(round.index, bidsPhase = false) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "M${round.index + 1}",
                                modifier = Modifier.width(LABEL_W).padding(start = 10.dp),
                                color = Palette.TextMuted,
                                maxLines = 1,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 0.9.sp),
                            )
                            Row(Modifier.horizontalScroll(hScroll)) {
                                players.forEach { p ->
                                    RoundCell(
                                        cell = round.cells.firstOrNull { it.playerId == p.id },
                                        bids = bids,
                                        width = colW,
                                    )
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

    if (showAdd) {
        PickPlayersDialog(
            repo = repo,
            state = state,
            candidates = state.users.filterNot { it.id in game.playerIds },
            title = stringResource(R.string.add_player_title),
            startScore = 0,
            onConfirm = { added, start -> repo.update { AppActions.genericAddPlayers(it, added.mapNotNull { p -> p.userId }, start) } },
            onClose = { showAdd = false },
        )
    }

    if (showRules) {
        RulesDialog(game.gameType) { showRules = false }
    }

    if (showStandings) {
        StandingsSheet(
            entries = players.map { Standing(it.name, Color(it.color), totals[it.id] ?: 0) },
            lowWins = game.rules.lowWins,
            onClose = { showStandings = false },
        )
    }

    removeFor?.let { p ->
        RemovePlayerDialog(
            name = p.name,
            canRemove = players.size > 2,
            onRemove = { repo.update { AppActions.genericRemovePlayer(it, p.id) } },
            onClose = { removeFor = null },
        )
    }
}

/** One player's column head: name, running total and progress toward the target. */
@Composable
private fun TotalCell(
    name: String,
    color: Color,
    total: Int,
    leading: Boolean,
    fraction: Float,
    width: Dp,
    onClick: () -> Unit,
) {
    Column(Modifier.width(width).clickable { onClick() }.padding(horizontal = 3.dp)) {
        Text(
            name,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp, letterSpacing = 0.4.sp),
        )
        Text(
            "$total",
            maxLines = 1,
            color = if (leading) Palette.Mint else Palette.TextPrimary,
            style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFeatureSettings = "tnum"),
        )
        ThinProgressBar(
            fraction = fraction,
            color = color,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/** One player's points for a hand, with the bid they called underneath in bids mode. */
@Composable
private fun RoundCell(cell: Cell?, bids: Boolean, width: Dp) {
    Column(Modifier.width(width).padding(horizontal = 3.dp)) {
        Text(
            if (cell?.hits == null && bids) "—" else "${cell?.points ?: 0}",
            maxLines = 1,
            color = Palette.TextPrimary,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFeatureSettings = "tnum"),
        )
        if (bids && cell?.bid != null) {
            Text(
                stringResource(R.string.bid_pedia, cell.bid),
                maxLines = 1,
                color = Color(0xFF5E7099),
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp),
            )
        }
    }
}

/** Confirms taking a player out of the running game, scores and all. */
@Composable
private fun RemovePlayerDialog(name: String, canRemove: Boolean, onRemove: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) } },
        confirmButton = {
            if (canRemove) {
                TextButton(onClick = { onRemove(); onClose() }) {
                    Text(stringResource(R.string.player_remove), color = Color(0xFFEB5757))
                }
            }
        },
        title = { Text(name, color = Palette.TextPrimary) },
        text = {
            Text(
                if (canRemove) stringResource(R.string.player_remove_note) else stringResource(R.string.player_remove_min),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp),
            )
        },
        containerColor = Palette.SheetSurface,
    )
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

            // A row per player: reads the same with two of them or with twenty.
            val fieldW = if (bids && !bidsPhase) 62.dp else 76.dp
            if (bids) {
                Row(Modifier.fillMaxWidth().padding(end = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f))
                    Box(Modifier.width(fieldW)) { FieldLabel(if (bidsPhase) stringResource(R.string.col_bid) else stringResource(R.string.col_hits)) }
                    if (!bidsPhase) Box(Modifier.width(fieldW)) { FieldLabel(stringResource(R.string.col_extra)) }
                }
            }
            Column(
                Modifier.weight(1f, fill = false)
                    .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.55f).dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                players.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth().rowSurface(12).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, color = Color(p.color), maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp))
                            if (bids && !bidsPhase) {
                                val said = bidText[p.id].orEmpty().ifBlank { "—" }
                                Text(stringResource(R.string.col_bid) + ": " + said, color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp))
                            }
                        }
                        when {
                            bidsPhase -> NumField(bidText[p.id].orEmpty(), { bidText[p.id] = it }, accent = true, width = fieldW)
                            bids -> {
                                NumField(hitsText[p.id].orEmpty(), { hitsText[p.id] = it }, accent = true, width = fieldW)
                                NumField(extraText[p.id].orEmpty(), { extraText[p.id] = it }, accent = false, width = fieldW)
                                val bidV = bidText[p.id]?.toIntOrNull()
                                val hitsV = hitsText[p.id]?.toIntOrNull() ?: 0
                                val pts = (if (bidV != null && bidV == hitsV) game.rules.pointsPerHit else 0) + (extraText[p.id]?.toIntOrNull() ?: 0)
                                Text("= $pts", color = Palette.CyanNumber, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFeatureSettings = "tnum"))
                            }
                            else -> NumField(pointsText[p.id].orEmpty(), { pointsText[p.id] = it }, accent = true, width = fieldW)
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
private fun NumField(value: String, onValue: (String) -> Unit, accent: Boolean, width: Dp) {
    fun clamp(n: Int) = n.coerceIn(-99, 999)
    fun current() = value.toIntOrNull() ?: 0
    Column(Modifier.width(width), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
