@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.dnd

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
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
import com.epichypernova.scoretracker.data.model.DndCharacter
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.AppToggle
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

private val HP_LOW = Color(0xFFEB5757)
private val INSPIRATION = Color(0xFFFFD98A)

/** App palette + a few fantasy-flavoured tones (gold, bone, crimson, forest). */
private val DND_COLORS = listOf(
    0xFFFF8C42, 0xFF2FD3F0, 0xFF55E6A5, 0xFFA18AF5, 0xFFFF6FA8, 0xFF3B7BF7,
    0xFFF2B33B, 0xFFF2ECD0, 0xFFE0492F, 0xFF4FB35B, 0xFF6E6A86,
)

private fun cardGradient(base: Color): List<Color> {
    val deep = Color(0xFF0E1B33)
    fun mix(t: Float) = Color(base.red * (1 - t) + deep.red * t, base.green * (1 - t) + deep.green * t, base.blue * (1 - t) + deep.blue * t, 1f)
    return listOf(mix(0.55f), mix(0.8f), deep)
}

@Composable
fun DndScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.dndGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.DND,
            minPlayers = 1, maxPlayers = 8, defaultCount = 4, onBack = onBack,
            onStart = { players -> repo.update { AppActions.dndStart(it, players) } },
        )
        return
    }
    // Keep the stored index next to each character so actions target the right one even when sorted.
    val ordered = game.characters.withIndex().toList().let { list ->
        if (game.sortByInitiative) list.sortedByDescending { it.value.initiative } else list
    }

    var padFor by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }   // (index, isDamage)
    var editFor by remember { mutableIntStateOf(-1) }
    var showConfig by remember { mutableStateOf(false) }
    var showRoll by remember { mutableStateOf(false) }
    // 0 = combat tracker, 1 = character sheets; survives rotation / process death
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var sheetFor by rememberSaveable { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize().gameInsets()) {
            ModeBar(mode = mode, onMode = { mode = it }, onConfig = { showConfig = true })
            if (mode == 0) {
                TopBar(
                    round = game.round,
                    onRound = { d -> repo.update { AppActions.dndRound(it, d) } },
                    onRoll = { showRoll = true },
                    onAdd = { repo.update { AppActions.dndAddCharacter(it) } },
                )
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(ordered, key = { it.index }) { (idx, c) ->
                        CharacterCard(
                            c = c, index = idx, repo = repo,
                            onEdit = { editFor = idx },
                            onSheet = { sheetFor = idx; mode = 1 },
                            onDamage = { padFor = idx to true },
                            onHeal = { padFor = idx to false },
                        )
                    }
                    item {
                        Box(
                            Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp))
                                .clickable { repo.update { AppActions.dndAddCharacter(it) } },
                            contentAlignment = Alignment.Center,
                        ) { Text(stringResource(R.string.dnd_add), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) }
                    }
                }
            } else {
                DndSheetSection(
                    game = game,
                    selected = sheetFor,
                    onSelect = { sheetFor = it },
                    repo = repo,
                    onEditCharacter = { editFor = it },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
        if (showRoll) D20Overlay(onClose = { showRoll = false })
    }

    padFor?.let { (idx, isDamage) ->
        val c = game.characters.getOrNull(idx)
        NumberPadSheet(
            title = stringResource(if (isDamage) R.string.dnd_damage_title else R.string.dnd_heal_title),
            subtitle = c?.let { "${it.name} · ${it.hp}/${it.maxHp}" + if (it.tempHp > 0) " +${it.tempHp}" else "" },
            accent = if (isDamage) HP_LOW else Palette.Mint,
            confirmLabel = stringResource(if (isDamage) R.string.dnd_damage else R.string.dnd_heal),
            onConfirm = { n -> repo.update { if (isDamage) AppActions.dndDamage(it, idx, n) else AppActions.dndHeal(it, idx, n) } },
            onClose = { padFor = null },
            quickAdds = listOf(1, 5, 10, 20),
            max = 999,
        )
    }
    if (editFor >= 0) {
        game.characters.getOrNull(editFor)?.let { c ->
            EditCharacterDialog(
                c = c,
                canRemove = game.characters.size > 1,
                onSave = { name, color, maxHp -> repo.update { AppActions.dndEdit(it, editFor, name, color, maxHp) } },
                onRemove = { repo.update { AppActions.dndRemoveCharacter(it, editFor) } },
                onClose = { editFor = -1 },
            )
        }
    }
    if (showConfig) {
        ConfigSheet(
            sortByInit = game.sortByInitiative,
            onToggleSort = { repo.update { AppActions.dndToggleSort(it) } },
            onLongRest = { repo.update { AppActions.dndLongRest(it) } },
            onReset = { repo.update { AppActions.dndReset(it) } },
            onNew = { repo.update { AppActions.dndNew(it) } },
            onFinish = { repo.update { AppActions.dndFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

/** Combate / Ficha switch plus the settings pill; always visible so swapping views is one tap. */
@Composable
private fun ModeBar(mode: Int, onMode: (Int) -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, Palette.GameDnd.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Segmented(
            options = listOf(stringResource(R.string.dnd_mode_combat), stringResource(R.string.dnd_mode_sheet)),
            selectedIndex = mode,
            onSelect = onMode,
            modifier = Modifier.weight(1f),
        )
        Pill("⚙", filled = false, onClick = onConfig)
    }
}

@Composable
private fun TopBar(round: Int, onRound: (Int) -> Unit, onRoll: () -> Unit, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameIcon(R.drawable.ic_hourglass, Palette.TextMuted, 14)
            Text(stringResource(R.string.dnd_round), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp))
            SmallRound("−") { onRound(-1) }
            Text("$round", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = 22.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFeatureSettings = "tnum"))
            SmallRound("+") { onRound(+1) }
        }
        Pill("d20", filled = true, onClick = onRoll, icon = R.drawable.ic_d20)
        Pill("＋", filled = false, onClick = onAdd)
    }
}

@Composable
private fun SmallRound(symbol: String, onClick: () -> Unit) {
    Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontSize = 16.sp))
    }
}

@Composable
private fun Pill(text: String, filled: Boolean, onClick: () -> Unit, @DrawableRes icon: Int? = null) {
    val fg = if (filled) Palette.OnAccent else Palette.TextSecondary
    Row(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp))
            .background(if (filled) Palette.GameDnd else Color.Transparent)
            .then(if (filled) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
            .clickable { onClick() }.padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (icon != null) GameIcon(icon, fg, 16)
        Text(text, color = fg, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp))
    }
}

@Composable
private fun CharacterCard(c: DndCharacter, index: Int, repo: Repository, onEdit: () -> Unit, onSheet: () -> Unit, onDamage: () -> Unit, onHeal: () -> Unit) {
    val tint = Color(c.color)
    val down = c.hp <= 0
    val dead = down && c.deathFail >= 3
    val grad = cardGradient(tint)
    val shape = RoundedCornerShape(18.dp)
    val bg = Modifier.drawBehind {
        if (dead) drawRect(Color(0xFF131A2E))
        else drawRect(brush = ShaderBrush(RadialGradientShader(Offset(0f, size.height), size.width * 1.1f, grad, listOf(0f, 0.6f, 1f))))
    }
    val hpColor = when {
        dead -> Color(0xFF3E4A6B)
        down || c.hp <= c.maxHp / 4 -> HP_LOW
        else -> Color.White
    }
    Column(Modifier.fillMaxWidth().clip(shape).then(bg).border(1.dp, tint.copy(alpha = if (dead) 0.15f else 0.35f), shape).padding(horizontal = 14.dp, vertical = 12.dp)) {
        // header: swatch + name (tap to edit) · inspiration star
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f).clickable { onEdit() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(tint).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(c.name.uppercase(), color = tint, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.9.sp))
                Text("✎", color = Palette.TextMuted, style = TextStyle(fontSize = 11.sp))
            }
            // jump to this character's sheet
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0x14FFFFFF)).clickable { onSheet() }, contentAlignment = Alignment.Center) {
                GameIcon(R.drawable.ic_sheet, Palette.TextSecondary, 15)
            }
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(if (c.inspiration) INSPIRATION.copy(alpha = 0.22f) else Color(0x14FFFFFF))
                    .clickable { repo.update { AppActions.dndToggleInspiration(it, index) } },
                contentAlignment = Alignment.Center,
            ) { GameIcon(R.drawable.ic_star, if (c.inspiration) INSPIRATION else Palette.TextMuted, 16) }
        }

        // HP row: − ♥ [hp / max] +
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            HpButton("−", enabled = !dead) { repo.update { AppActions.dndDamage(it, index, 1) } }
            Row(Modifier.padding(horizontal = 14.dp).widthIn(min = 120.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                GameIcon(R.drawable.ic_heart, hpColor, 18, Modifier.padding(bottom = 12.dp, end = 6.dp))
                Text("${c.hp}", color = hpColor, maxLines = 1, softWrap = false, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, fontFeatureSettings = "tnum"))
                Text("/${c.maxHp}", color = Palette.TextTertiary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFeatureSettings = "tnum"))
                if (c.tempHp > 0) {
                    Text("+${c.tempHp}", color = Palette.Cyan, modifier = Modifier.padding(bottom = 8.dp, start = 6.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFeatureSettings = "tnum"))
                }
            }
            HpButton("＋", enabled = !dead) { repo.update { AppActions.dndHeal(it, index, 1) } }
        }
        HpBar(c.hp, c.maxHp, c.tempHp, tint)

        if (down) {
            DeathSaves(c, dead, onSuccess = { repo.update { AppActions.dndDeathSave(it, index, true) } }, onFail = { repo.update { AppActions.dndDeathSave(it, index, false) } })
        }

        // actions + stat chips
        FlowRow(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionPill(stringResource(R.string.dnd_damage), HP_LOW, R.drawable.ic_sword, enabled = !dead, onClick = onDamage)
            ActionPill(stringResource(R.string.dnd_heal), Palette.Mint, R.drawable.ic_potion, enabled = !dead, onClick = onHeal)
            StatChip(stringResource(R.string.dnd_temp), c.tempHp, Palette.Cyan, R.drawable.ic_heart_outline,
                onDec = { repo.update { AppActions.dndTempHp(it, index, -1) } }, onInc = { repo.update { AppActions.dndTempHp(it, index, +1) } })
            StatChip(stringResource(R.string.dnd_ac), c.ac, Palette.TextSecondary, R.drawable.ic_shield,
                onDec = { repo.update { AppActions.dndAc(it, index, -1) } }, onInc = { repo.update { AppActions.dndAc(it, index, +1) } })
            StatChip(stringResource(R.string.dnd_init), c.initiative, INSPIRATION, R.drawable.ic_d20,
                onDec = { repo.update { AppActions.dndInitiative(it, index, -1) } }, onInc = { repo.update { AppActions.dndInitiative(it, index, +1) } })
        }
    }
}

@Composable
private fun HpButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(symbol, color = if (enabled) Color.White else Palette.TextMuted, style = TextStyle(fontSize = 24.sp)) }
}

@Composable
private fun HpBar(hp: Int, maxHp: Int, temp: Int, tint: Color) {
    val frac by animateFloatAsState((hp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f), label = "hp")
    val tempFrac = (temp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f - frac)
    Row(Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x22FFFFFF))) {
        if (frac > 0f) Box(Modifier.fillMaxWidth(frac).height(6.dp).background(if (frac <= 0.25f) HP_LOW else tint))
        if (tempFrac > 0f) Box(Modifier.fillMaxWidth(tempFrac / (1f - frac).coerceAtLeast(0.001f)).height(6.dp).background(Palette.Cyan.copy(alpha = 0.7f)))
    }
}

@Composable
private fun ActionPill(text: String, color: Color, @DrawableRes icon: Int, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.height(38.dp).clip(RoundedCornerShape(999.dp)).background(color.copy(alpha = if (enabled) 0.22f else 0.08f)).border(1.dp, color.copy(alpha = if (enabled) 0.6f else 0.2f), RoundedCornerShape(999.dp))
            .clickable(enabled = enabled) { onClick() }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GameIcon(icon, if (enabled) color else Palette.TextMuted, 16)
        Text(text, color = if (enabled) Palette.TextPrimary else Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp))
    }
}

/** Compact labelled counter with an icon and − / + buttons. */
@Composable
private fun StatChip(label: String, value: Int, color: Color, @DrawableRes icon: Int, onDec: () -> Unit, onInc: () -> Unit) {
    Row(
        Modifier.height(38.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GameIcon(icon, color, 14)
        Text(label.uppercase(), color = color, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.2.sp))
        Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).repeatingClickable(onClick = onDec), contentAlignment = Alignment.Center) {
            Text("−", color = Palette.TextPrimary, style = TextStyle(fontSize = 16.sp))
        }
        Text("$value", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = 22.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFeatureSettings = "tnum"))
        Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).repeatingClickable(onClick = onInc), contentAlignment = Alignment.Center) {
            Text("+", color = Palette.TextPrimary, style = TextStyle(fontSize = 16.sp))
        }
    }
}

/** Death saving throws: three ✓ and three ✗ pips; tapping a group advances it (wraps at 3). */
@Composable
private fun DeathSaves(c: DndCharacter, dead: Boolean, onSuccess: () -> Unit, onFail: () -> Unit) {
    val status = when {
        dead -> R.string.dnd_dead
        c.deathSuccess >= 3 -> R.string.dnd_stable
        else -> R.string.dnd_unconscious
    }
    val statusColor = when {
        dead -> HP_LOW
        c.deathSuccess >= 3 -> Palette.Mint
        else -> INSPIRATION
    }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(status), color = statusColor, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.8.sp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Pips("✓", c.deathSuccess, Palette.Mint, onSuccess)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GameIcon(R.drawable.ic_skull, statusColor, 14)
                Text(stringResource(R.string.dnd_death_saves), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp, letterSpacing = 1.sp))
            }
            Pips("✗", c.deathFail, HP_LOW, onFail)
        }
    }
}

@Composable
private fun Pips(symbol: String, count: Int, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0x17FFFFFF)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(symbol, color = color, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        repeat(3) { i ->
            Box(Modifier.size(12.dp).clip(CircleShape).background(if (i < count) color else Color.Transparent).border(1.dp, color.copy(alpha = 0.7f), CircleShape))
        }
    }
}

@Composable
private fun EditCharacterDialog(c: DndCharacter, canRemove: Boolean, onSave: (String, Long, Int) -> Unit, onRemove: () -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(c.name) }
    var color by remember { mutableStateOf(c.color) }
    var maxHp by remember { mutableIntStateOf(c.maxHp) }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = { onSave(name, color, maxHp); onClose() }) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        dismissButton = {
            Row {
                if (canRemove) TextButton(onClick = { onRemove(); onClose() }) { Text(stringResource(R.string.dnd_remove), color = HP_LOW) }
                TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) }
            }
        },
        title = { Text(stringResource(R.string.dnd_edit_title), color = Palette.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    SheetLabel(stringResource(R.string.name))
                    BasicTextField(
                        value = name, onValueChange = { name = it }, singleLine = true,
                        textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 18.sp, color = Palette.TextPrimary),
                        cursorBrush = SolidColor(Palette.Cyan),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    )
                    Box(Modifier.fillMaxWidth().height(1.5.dp).background(Palette.Cyan))
                }
                Column {
                    SheetLabel(stringResource(R.string.dnd_max_hp))
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        StepBtn("−") { maxHp = (maxHp - 1).coerceAtLeast(1) }
                        Text("$maxHp", color = Palette.Cyan, modifier = Modifier.widthIn(min = 56.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 28.sp, fontFeatureSettings = "tnum"))
                        StepBtn("＋") { maxHp = (maxHp + 1).coerceAtMost(999) }
                    }
                }
                Column {
                    SheetLabel(stringResource(R.string.color))
                    FlowRow(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DND_COLORS.forEach { opt ->
                            val active = opt == color
                            val swatch = Color(opt)
                            val lum = 0.299f * swatch.red + 0.587f * swatch.green + 0.114f * swatch.blue
                            Box(
                                Modifier.size(38.dp).clip(CircleShape).background(swatch)
                                    .then(if (active) Modifier.border(3.dp, Palette.TextPrimary, CircleShape) else Modifier.border(1.dp, Color(0x33FFFFFF), CircleShape))
                                    .clickable { color = opt },
                                contentAlignment = Alignment.Center,
                            ) { if (active) Text("✓", color = if (lum > 0.6f) Color.Black else Color.White, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)) }
                        }
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@Composable
private fun StepBtn(symbol: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, Palette.ButtonBorder, CircleShape).repeatingClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontSize = 20.sp))
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(text, color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(sortByInit: Boolean, onToggleSort: () -> Unit, onLongRest: () -> Unit, onReset: () -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dnd_sort_init), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                AppToggle(sortByInit, { onToggleSort() })
            }
            Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.GameDnd).clickable { onLongRest(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.dnd_long_rest), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onReset(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.dnd_new_encounter), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onNew(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.setup_new_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.finish_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
        }
    }
}

/** Animated d20 roll: spins through random faces, then lands. 20 = crit, 1 = fumble. */
@Composable
private fun D20Overlay(onClose: () -> Unit) {
    var face by remember { mutableIntStateOf(1) }
    var settled by remember { mutableStateOf(false) }
    var rollKey by remember { mutableIntStateOf(0) }
    val diceSound = rememberSoundEffect(R.raw.dice)
    val victorySound = rememberSoundEffect(R.raw.victory)

    LaunchedEffect(rollKey) {
        settled = false
        diceSound()
        var d = 50L
        repeat(18) {
            face = (1..20).random()
            delay(d)
            d += 10
        }
        face = (1..20).random()
        settled = true
        if (face == 20) victorySound()
    }

    val faceColor = when {
        !settled -> Palette.TextPrimary
        face == 20 -> Palette.Mint
        face == 1 -> HP_LOW
        else -> Palette.GameDnd
    }
    val s by animateFloatAsState(if (settled) 1.15f else 1f, label = "d20")
    Box(Modifier.fillMaxSize().background(Color(0xE60A1327)).clickable(enabled = settled) { onClose() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                when {
                    !settled -> stringResource(R.string.dnd_rolling)
                    face == 20 -> stringResource(R.string.dnd_crit)
                    face == 1 -> stringResource(R.string.dnd_fumble)
                    else -> "d20"
                },
                color = faceColor,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp),
            )
            Box(
                Modifier.scale(s).size(120.dp).clip(RoundedCornerShape(28.dp)).background(faceColor.copy(alpha = 0.16f)).border(2.dp, faceColor, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("$face", color = faceColor, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, fontFeatureSettings = "tnum")) }
            if (settled) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill(stringResource(R.string.dnd_roll), filled = true, onClick = { rollKey++ })
                    Pill(stringResource(R.string.done), filled = false, onClick = onClose)
                }
            }
        }
    }
}
