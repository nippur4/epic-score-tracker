package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GamePlayer
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Colors handed to players created on the fly, cycled by how many users already exist. */
val NewPlayerColors = listOf(
    0xFF2FD3F0, 0xFF55E6A5, 0xFFFF6FA8, 0xFFA18AF5, 0xFF3B7BF7, 0xFFF27BA9, 0xFFFF8C42, 0xFFF2B33B,
)

/** A guest pick: the game names and colors it by position. */
private val GuestPick = GamePlayer("", 0L)

/** `"Ana, Beto; Caro"` → `["Ana", "Beto", "Caro"]`, dropping blanks and repeats. */
fun parsePlayerNames(raw: String): List<String> =
    raw.split(',', ';', '\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }

/**
 * Pick several players in one go: tap every saved player that's in, or type one or more
 * names (comma separated) to create them on the spot, then confirm once.
 *
 * [candidates] are the users still up for grabs — the caller filters out whoever is already
 * in the game. [maxPick] caps the selection (free slots left); null means no cap.
 * [allowGuest] offers an unnamed player, handed over with a blank name for the game to number.
 * [startScore], when set, asks what score the newcomers join with and seeds the field with it.
 * [onConfirm] gets the picks in tap order, plus that score (0 when it wasn't asked).
 */
@Composable
fun PickPlayersDialog(
    repo: Repository,
    state: AppState,
    candidates: List<User>,
    title: String,
    maxPick: Int? = null,
    highlightedId: String? = null,
    allowGuest: Boolean = false,
    startScore: Int? = null,
    onConfirm: (List<GamePlayer>, Int) -> Unit,
    onClose: () -> Unit,
) {
    val picked = remember { mutableStateListOf<GamePlayer>() }
    val created = remember { mutableStateListOf<User>() }
    var names by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(startScore?.toString() ?: "") }

    // Players created here show up right away, before the new state reaches [candidates];
    // frequent players come first, which is what marking one as a favorite buys you.
    val rows = (candidates + created.filterNot { c -> candidates.any { it.id == c.id } })
        .sortedByDescending { it.favorite }
    val full = maxPick != null && picked.size >= maxPick

    fun toggle(u: User) {
        val at = picked.indexOfFirst { it.userId == u.id }
        if (at >= 0) picked.removeAt(at) else if (!full) picked.add(GamePlayer(u.name, u.color, u.id))
    }

    /** Creates every name typed in the field and picks them, reusing saved players by name. */
    fun createTyped() {
        val parsed = parsePlayerNames(names)
        val room = if (maxPick == null) parsed.size else (maxPick - picked.size).coerceAtLeast(0)
        val fresh = mutableListOf<User>()
        val toPick = mutableListOf<User>()
        for (name in parsed) {
            if (toPick.size >= room) break
            val existing = state.users.firstOrNull { it.name.equals(name, ignoreCase = true) }
            if (existing != null) {
                // Don't make a twin of a saved player; skip them if they're already in the game.
                val free = rows.any { it.id == existing.id } && picked.none { it.userId == existing.id }
                if (free) toPick += existing
            } else {
                val color = NewPlayerColors[(state.users.size + fresh.size) % NewPlayerColors.size]
                val user = User(AppActions.newId("u"), name, color)
                fresh += user
                toPick += user
            }
        }
        if (fresh.isNotEmpty()) {
            repo.update { s -> fresh.fold(s) { acc, u -> AppActions.addExistingUser(acc, u) } }
            created += fresh
        }
        picked += toPick.map { GamePlayer(it.name, it.color, it.id) }
        names = ""
    }

    AlertDialog(
        onDismissRequest = onClose,
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) } },
        confirmButton = {
            TextButton(onClick = {
                if (picked.isNotEmpty()) onConfirm(picked.toList(), score.toIntOrNull() ?: startScore ?: 0)
                onClose()
            }) {
                Text(
                    if (picked.isEmpty()) stringResource(R.string.done) else stringResource(R.string.pick_players_confirm, picked.size),
                    color = Palette.Cyan,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold),
                )
            }
        },
        title = { Text(title, color = Palette.TextPrimary) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (maxPick == null || maxPick > 1) {
                    Text(
                        if (maxPick == null) stringResource(R.string.pick_players_hint) else stringResource(R.string.pick_players_hint_max, maxPick),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }

                rows.forEach { u ->
                    val selected = picked.any { it.userId == u.id }
                    PickRow(
                        user = u,
                        selected = selected,
                        dimmed = full && !selected,
                        highlighted = u.id == highlightedId,
                        onClick = { toggle(u) },
                    )
                }

                if (allowGuest) {
                    val selected = picked.contains(GuestPick)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = selected || !full) { if (selected) picked.remove(GuestPick) else picked.add(GuestPick) }
                            .alpha(if (full && !selected) 0.4f else 1f)
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, Palette.ButtonBorder, CircleShape), contentAlignment = Alignment.Center) {
                            Text("?", color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                        }
                        Text(stringResource(R.string.setup_guest), color = Palette.TextSecondary, modifier = Modifier.weight(1f).padding(start = 12.dp))
                        CheckDot(selected)
                    }
                }

                // Create players without leaving this screen — one name or several at once.
                Text(
                    stringResource(R.string.or_create_new),
                    color = Palette.TextTertiary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.2.sp),
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
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
                        if (names.isEmpty()) {
                            Text(stringResource(R.string.new_player_names_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
                        }
                        BasicTextField(
                            value = names,
                            onValueChange = { names = it },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary),
                            cursorBrush = SolidColor(Palette.Cyan),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (!full) createTyped() }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                val canCreate = parsePlayerNames(names).isNotEmpty() && !full
                Box(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(999.dp))
                        .background(Palette.Cyan).alpha(if (canCreate) 1f else 0.4f)
                        .clickable(enabled = canCreate) { createTyped() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.create_and_add), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp))
                }

                // What the newcomers join with, for games that keep a running score.
                if (startScore != null) {
                    Text(
                        stringResource(R.string.player_start_score),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.2.sp),
                        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.ControlFill)
                            .border(1.dp, Palette.CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = score,
                            onValueChange = { v -> score = v.filterIndexed { i, c -> c.isDigit() || (c == '-' && i == 0) }.take(6) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Palette.TextPrimary, fontFeatureSettings = "tnum"),
                            cursorBrush = SolidColor(Palette.Cyan),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            stringResource(R.string.player_start_score_hint, startScore),
                            color = Palette.TextMuted,
                            style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp),
                        )
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@Composable
private fun PickRow(user: User, selected: Boolean, dimmed: Boolean, highlighted: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !dimmed) { onClick() }
            .alpha(if (dimmed) 0.4f else 1f)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(user.name, Color(user.color), size = 32, fontSize = 15, avatarId = user.avatarId)
        Text(
            user.name,
            color = if (highlighted) Palette.Cyan else Palette.TextPrimary,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
        if (user.favorite) {
            Text("★", color = Palette.TextMuted, style = TextStyle(fontSize = 13.sp), modifier = Modifier.padding(end = 10.dp))
        }
        CheckDot(selected)
    }
}

@Composable
private fun CheckDot(selected: Boolean) {
    Box(
        Modifier.size(22.dp).clip(CircleShape)
            .background(if (selected) Palette.Cyan else Color.Transparent)
            .border(1.dp, if (selected) Palette.Cyan else Palette.ButtonBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text("✓", color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp))
        }
    }
}
