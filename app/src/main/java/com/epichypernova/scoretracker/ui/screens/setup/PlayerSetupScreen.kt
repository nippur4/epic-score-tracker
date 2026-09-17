package com.epichypernova.scoretracker.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
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
 * [options] adds game-specific rules (target score, etc.) under the players.
 * With [countOnly] (poker's blind timer) no names are asked and [onStart] gets that many guests.
 */
@Composable
fun PlayerSetupScreen(
    repo: Repository,
    state: AppState,
    gameType: GameType,
    minPlayers: Int,
    maxPlayers: Int,
    defaultCount: Int,
    onBack: () -> Unit,
    onStart: (List<GamePlayer>) -> Unit,
    countOnly: Boolean = false,
    options: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val guestFmt = stringResource(R.string.setup_guest_name)
    fun guest(i: Int) = GamePlayer(String.format(guestFmt, i + 1), GUEST_COLORS[i % GUEST_COLORS.size])

    var count by remember { mutableIntStateOf(defaultCount.coerceIn(minPlayers, maxPlayers)) }
    val slots = remember {
        val ordered = state.users.sortedByDescending { it.favorite }
        (0 until maxPlayers).map { i ->
            ordered.getOrNull(i)?.let { GamePlayer(it.name, it.color, it.id) } ?: guest(i)
        }.toMutableStateList()
    }
    var pickFor by remember { mutableIntStateOf(-1) }
    val adGate = rememberAdGate(repo, state)
    val tint = GameCatalog.tintFor(gameType)

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
                    Text(stringResource(R.string.setup_player_range, minPlayers, maxPlayers), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp))
                }
                Stepper(count, onDec = { count = (count - 1).coerceAtLeast(minPlayers) }, onInc = { count = (count + 1).coerceAtMost(maxPlayers) })
            }

            // Who plays
            if (!countOnly) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.players_label))
                    (0 until count).forEach { i ->
                        val p = slots[i]
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
                    val players = if (countOnly) (0 until count).map(::guest) else slots.take(count)
                    adGate { onStart(players) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (pickFor >= 0) {
        val idx = pickFor
        val taken = (0 until count).filter { it != idx }.mapNotNull { slots[it].userId }.toSet()
        PickPlayerDialog(
            repo = repo,
            state = state,
            available = state.users.filterNot { it.id in taken },
            currentUserId = slots[idx].userId,
            onPickUser = { u -> slots[idx] = GamePlayer(u.name, u.color, u.id) },
            onPickGuest = { slots[idx] = guest(idx) },
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

/** Pick a saved user for a slot, mark it as a guest, or create a new user on the spot. */
@Composable
private fun PickPlayerDialog(
    repo: Repository,
    state: AppState,
    available: List<User>,
    currentUserId: String?,
    onPickUser: (User) -> Unit,
    onPickGuest: () -> Unit,
    onClose: () -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) } },
        title = { Text(stringResource(R.string.setup_pick_title), color = Palette.TextPrimary) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                available.forEach { u ->
                    val active = u.id == currentUserId
                    Row(
                        Modifier.fillMaxWidth().clickable { onPickUser(u); onClose() }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(u.name, Color(u.color), size = 32, fontSize = 15, avatarId = u.avatarId)
                        Text(u.name, color = if (active) Palette.Cyan else Palette.TextPrimary, modifier = Modifier.weight(1f).padding(start = 12.dp))
                        if (u.favorite) Text("★", color = Palette.TextMuted, style = TextStyle(fontSize = 13.sp))
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clickable { onPickGuest(); onClose() }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, Palette.ButtonBorder, CircleShape), contentAlignment = Alignment.Center) {
                        Text("?", color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                    }
                    Text(stringResource(R.string.setup_guest), color = Palette.TextSecondary, modifier = Modifier.padding(start = 12.dp))
                }

                // Create a new player without leaving this screen
                Text(
                    stringResource(R.string.or_create_new),
                    color = Palette.TextTertiary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.2.sp),
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                )
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Palette.ControlFill)
                        .border(1.dp, Palette.CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (newName.isEmpty()) {
                            Text(stringResource(R.string.new_player_name_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
                        }
                        BasicTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary),
                            cursorBrush = SolidColor(Palette.Cyan),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(999.dp))
                        .background(if (newName.isBlank()) Palette.Cyan.copy(alpha = 0.4f) else Palette.Cyan)
                        .clickable(enabled = newName.isNotBlank()) {
                            val color = GUEST_COLORS[state.users.size % GUEST_COLORS.size]
                            val user = User(AppActions.newId("u"), newName.trim(), color)
                            repo.update { AppActions.addExistingUser(it, user) }
                            onPickUser(user)
                            onClose()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.create_and_add), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}
