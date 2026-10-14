package com.epichypernova.scoretracker.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GamePlayer
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.GameCatalog
import com.epichypernova.scoretracker.ui.components.Avatar
import com.epichypernova.scoretracker.ui.components.BackHeader
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.GameLogoBox
import com.epichypernova.scoretracker.ui.components.PickPlayersDialog
import com.epichypernova.scoretracker.ui.components.RulesLink
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.Stepper
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.rememberAdGate
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Guest colors, cycled by slot index. */
private val GUEST_COLORS = listOf(0xFF2FD3F0, 0xFF55E6A5, 0xFFFF6FA8, 0xFFA18AF5, 0xFF3B7BF7, 0xFFF27BA9, 0xFFFF8C42, 0xFFF2B33B)

/**
 * "Nueva partida" screen shared by every specific game whose player count can vary.
 * Asks how many people play, then fills that many slots with the app's saved players
 * (favorites first), each of which can be swapped for another user, a new user or a guest.
 * [maxPlayers] is null for the games whose board grows with the list (no ceiling).
 * [options] adds game-specific rules (target score, etc.) under the players.
 * With [countOnly] (poker's blind timer) no names are asked and [onStart] gets that many guests.
 */
@Composable
fun PlayerSetupScreen(
    repo: Repository,
    state: AppState,
    gameType: GameType,
    minPlayers: Int,
    maxPlayers: Int?,
    defaultCount: Int,
    onBack: () -> Unit,
    onStart: (List<GamePlayer>) -> Unit,
    countOnly: Boolean = false,
    options: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val guestFmt = stringResource(R.string.setup_guest_name)
    fun guest(i: Int) = GamePlayer(String.format(guestFmt, i + 1), GUEST_COLORS[i % GUEST_COLORS.size])

    val cap = maxPlayers ?: Int.MAX_VALUE
    var count by remember { mutableIntStateOf(defaultCount.coerceIn(minPlayers, cap)) }
    // Seats start empty: nobody is put in the game until the person says who's playing.
    val slots = remember {
        (0 until defaultCount.coerceIn(minPlayers, cap)).map { guest(it) }.toMutableStateList()
    }
    var pickFor by remember { mutableIntStateOf(-1) }
    val adGate = rememberAdGate(repo, state)
    val tint = GameCatalog.tintFor(gameType)

    /** Grows the slot list until it holds [n] seats. */
    fun grow(n: Int) {
        while (slots.size < n) slots.add(guest(slots.size))
    }

    /** Puts [p] in [slot], turning any spare slot that held the same saved player into a guest. */
    fun assign(slot: Int, p: GamePlayer) {
        grow(slot + 1)
        if (p.userId != null) {
            slots.indices.forEach { j -> if (j != slot && slots[j].userId == p.userId) slots[j] = guest(j) }
        }
        slots[slot] = p
    }

    Column(Modifier.fillMaxSize().background(Palette.AppBg)) {
        BackHeader(
            title = stringResource(GameCatalog.labelRes(gameType)),
            onBack = onBack,
            overtitle = stringResource(R.string.new_game),
            trailing = { GameLogoBox(gameType, tint, boxSize = 40, radius = 12, emblemSize = 22) },
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(Modifier.height(4.dp))

            // How many play
            Row(
                Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.setup_player_count), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                    Text(
                        if (maxPlayers != null) stringResource(R.string.setup_player_range, minPlayers, maxPlayers)
                        else stringResource(R.string.setup_player_range_open, minPlayers),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                    )
                }
                Stepper(
                    count,
                    onDec = { count = (count - 1).coerceAtLeast(minPlayers) },
                    onInc = { count = (count + 1).coerceAtMost(cap).also { grow(it) } },
                )
            }

            RulesLink(gameType)

            // Who plays
            if (!countOnly) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.players_label))
                    (0 until count).forEach { i ->
                        val p = slots.getOrNull(i) ?: guest(i)
                        val user = p.userId?.let { id -> state.users.firstOrNull { it.id == id } }
                        SlotRow(index = i, player = p, user = user, onClick = { pickFor = i })
                    }
                }
            }

            if (options != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(stringResource(R.string.rules))
                    options()
                }
            }

            Box(Modifier.height(8.dp))
        }

        // Sticky CTA
        Box(Modifier.fillMaxWidth().background(Palette.AppBg).navigationBarsPadding().padding(20.dp)) {
            ChamferCta(
                text = stringResource(R.string.start_game),
                onClick = {
                    if (!countOnly) grow(count)
                    val players = if (countOnly) (0 until count).map(::guest) else slots.take(count)
                    adGate { onStart(players) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (pickFor >= 0) {
        val idx = pickFor
        val taken = (0 until count).filter { it != idx }.mapNotNull { slots.getOrNull(it)?.userId }.toSet()
        // Seats a batch can land on: this one, the empty ones after it, and whatever room is left.
        val emptyAfter = ((idx + 1) until count).count { slots.getOrNull(it)?.userId == null }
        PickPlayersDialog(
            repo = repo,
            state = state,
            candidates = state.users.filterNot { it.id in taken },
            title = stringResource(R.string.setup_pick_title),
            maxPick = maxPlayers?.let { 1 + emptyAfter + (it - count).coerceAtLeast(0) },
            highlightedId = slots.getOrNull(idx)?.userId,
            allowGuest = true,
            onConfirm = { picked, _ ->
                // The first one takes the seat that was tapped; the rest fill the empty seats
                // after it, and only add new ones once there are none left.
                var slot = idx
                picked.forEach { p ->
                    if (slot >= cap) return@forEach
                    assign(slot, if (p.userId == null) guest(slot) else p)
                    if (slot >= count) count = slot + 1
                    slot = ((slot + 1) until count).firstOrNull { slots.getOrNull(it)?.userId == null } ?: count
                }
            },
            onClose = { pickFor = -1 },
        )
    }
}

@Composable
private fun SlotRow(index: Int, player: GamePlayer, user: User?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().cardSurface(16).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("${index + 1}", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
        Avatar(player.name, Color(player.color), size = 34, fontSize = 15, avatarId = user?.avatarId)
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
            Text(
                stringResource(if (user != null) R.string.setup_saved_player else R.string.setup_guest),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
            )
        }
        Text(stringResource(R.string.setup_change), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
    }
}

