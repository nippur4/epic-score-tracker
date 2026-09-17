@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.dnd

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.DndRules
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.DndAbility
import com.epichypernova.scoretracker.data.model.DndAttack
import com.epichypernova.scoretracker.data.model.DndCharacter
import com.epichypernova.scoretracker.data.model.DndGame
import com.epichypernova.scoretracker.data.model.DndSkill
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.MiniStep
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val GOLD = Color(0xFFFFD98A)

@StringRes
private fun abilityRes(a: DndAbility): Int = when (a) {
    DndAbility.STR -> R.string.dnd_ab_str
    DndAbility.DEX -> R.string.dnd_ab_dex
    DndAbility.CON -> R.string.dnd_ab_con
    DndAbility.INT -> R.string.dnd_ab_int
    DndAbility.WIS -> R.string.dnd_ab_wis
    DndAbility.CHA -> R.string.dnd_ab_cha
}

@StringRes
private fun skillRes(s: DndSkill): Int = when (s) {
    DndSkill.ACROBATICS -> R.string.dnd_sk_acrobatics
    DndSkill.ANIMAL_HANDLING -> R.string.dnd_sk_animal_handling
    DndSkill.ARCANA -> R.string.dnd_sk_arcana
    DndSkill.ATHLETICS -> R.string.dnd_sk_athletics
    DndSkill.DECEPTION -> R.string.dnd_sk_deception
    DndSkill.HISTORY -> R.string.dnd_sk_history
    DndSkill.INSIGHT -> R.string.dnd_sk_insight
    DndSkill.INTIMIDATION -> R.string.dnd_sk_intimidation
    DndSkill.INVESTIGATION -> R.string.dnd_sk_investigation
    DndSkill.MEDICINE -> R.string.dnd_sk_medicine
    DndSkill.NATURE -> R.string.dnd_sk_nature
    DndSkill.PERCEPTION -> R.string.dnd_sk_perception
    DndSkill.PERFORMANCE -> R.string.dnd_sk_performance
    DndSkill.PERSUASION -> R.string.dnd_sk_persuasion
    DndSkill.RELIGION -> R.string.dnd_sk_religion
    DndSkill.SLEIGHT_OF_HAND -> R.string.dnd_sk_sleight_of_hand
    DndSkill.STEALTH -> R.string.dnd_sk_stealth
    DndSkill.SURVIVAL -> R.string.dnd_sk_survival
}

/**
 * "Ficha" half of the D&D screen: a character picker on top and, below it, the selected
 * character's sheet (info, vitals, abilities, skills, attacks, spell slots, free text).
 * Everything edits in place and is persisted through [repo] like the combat tracker.
 */
@Composable
fun DndSheetSection(
    game: DndGame,
    selected: Int,
    onSelect: (Int) -> Unit,
    repo: Repository,
    onEditCharacter: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val index = selected.coerceIn(0, (game.characters.size - 1).coerceAtLeast(0))
    val c = game.characters.getOrNull(index) ?: return
    var attackFor by remember { mutableStateOf<Int?>(null) }   // -1 = new, ≥0 = editing that attack
    var showAttack by remember { mutableStateOf(false) }

    // Text buffers are keyed by slot + name so removing a character never shows another one's text.
    val key = "$index/${c.name}"

    // imePadding on the whole section shrinks the viewport, so the focused field scrolls above the keyboard.
    Column(modifier.imePadding()) {
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(game.characters) { i, ch -> CharacterChip(ch, active = i == index) { onSelect(i) } }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeaderCard(c, index, key, repo, onEdit = { onEditCharacter(index) })
            VitalsCard(c, index, repo, onEditHp = { onEditCharacter(index) })
            AbilitiesCard(c, index, repo)
            SkillsCard(c, index, repo)
            AttacksCard(c, onAdd = { attackFor = -1; showAttack = true }, onEdit = { attackFor = it; showAttack = true })
            SlotsCard(c, index, repo)
            TextCard(key = key, title = stringResource(R.string.dnd_features), hint = stringResource(R.string.dnd_features_hint), text = c.features) { repo.update { s -> AppActions.dndSetFeatures(s, index, it) } }
            TextCard(key = key, title = stringResource(R.string.dnd_notes), hint = stringResource(R.string.dnd_notes_hint), text = c.notes) { repo.update { s -> AppActions.dndSetNotes(s, index, it) } }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showAttack) {
        val ai = attackFor ?: -1
        AttackDialog(
            initial = c.attacks.getOrNull(ai),
            onSave = { a -> repo.update { s -> AppActions.dndPutAttack(s, index, ai.takeIf { it >= 0 }, a) } },
            onRemove = if (ai >= 0) ({ repo.update { s -> AppActions.dndRemoveAttack(s, index, ai) } }) else null,
            onClose = { showAttack = false },
        )
    }
}

@Composable
private fun CharacterChip(c: DndCharacter, active: Boolean, onClick: () -> Unit) {
    val tint = Color(c.color)
    Row(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp))
            .background(if (active) tint.copy(alpha = 0.22f) else Color(0x0DFFFFFF))
            .border(1.dp, tint.copy(alpha = if (active) 0.9f else 0.3f), RoundedCornerShape(999.dp))
            .clickable { onClick() }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(tint))
        Text(c.name, color = if (active) Palette.TextPrimary else Palette.TextSecondary, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
    }
}

/** Rounded card with a small caps title, the same surface the combat cards use. */
@Composable
private fun SheetCard(title: String, trailing: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(Color(0x0DFFFFFF)).border(1.dp, Palette.CardBorder, shape).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { SheetLabel(title.uppercase()) }
            trailing?.invoke()
        }
        content()
    }
}

// ---- header: name / class / race / level ----

@Composable
private fun HeaderCard(c: DndCharacter, index: Int, key: String, repo: Repository, onEdit: () -> Unit) {
    val tint = Color(c.color)
    SheetCard(stringResource(R.string.dnd_edit_title)) {
        Row(Modifier.fillMaxWidth().clickable { onEdit() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(tint).border(1.dp, Color(0x33FFFFFF), CircleShape))
            Text(c.name, color = tint, modifier = Modifier.weight(1f), maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 20.sp))
            Text("✎", color = Palette.TextMuted, style = TextStyle(fontSize = 13.sp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InlineField(key = key, label = stringResource(R.string.dnd_class), text = c.dndClass, modifier = Modifier.weight(1f)) { repo.update { s -> AppActions.dndSetClass(s, index, it) } }
            InlineField(key = key, label = stringResource(R.string.dnd_race), text = c.race, modifier = Modifier.weight(1f)) { repo.update { s -> AppActions.dndSetRace(s, index, it) } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(stringResource(R.string.dnd_level), "${c.level}", Modifier.weight(1f),
                onDec = { repo.update { s -> AppActions.dndLevel(s, index, -1) } }, onInc = { repo.update { s -> AppActions.dndLevel(s, index, +1) } })
            StatTile(stringResource(R.string.dnd_prof), DndRules.signed(DndRules.proficiency(c.level)), Modifier.weight(1f), accent = GOLD)
        }
    }
}

/** Single-line text with the app's underline look; keeps its own buffer so typing stays smooth. */
@Composable
private fun InlineField(key: Any, label: String, text: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    var buffer by remember(key) { mutableStateOf(text) }
    Column(modifier) {
        Text(label.uppercase(), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.2.sp))
        BasicTextField(
            value = buffer,
            onValueChange = { buffer = it; onChange(it) },
            singleLine = true,
            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 15.sp, color = Palette.TextPrimary),
            cursorBrush = SolidColor(Palette.Cyan),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            decorationBox = { inner ->
                Box { if (buffer.isEmpty()) Text("—", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 15.sp)); inner() }
            },
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Palette.Divider))
    }
}

/** Compact stat block: label, big value and optional − / + steppers. */
@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color = Palette.TextPrimary, onDec: (() -> Unit)? = null, onInc: (() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x14FFFFFF)).then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label.uppercase(), color = Palette.TextTertiary, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 1.sp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (onDec != null) MiniStep("−", 22, onDec)
            Text(value, color = accent, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 30.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFeatureSettings = "tnum"))
            if (onInc != null) MiniStep("+", 22, onInc)
        }
    }
}

// ---- vitals: HP, AC, speed, initiative bonus, passive perception ----

@Composable
private fun VitalsCard(c: DndCharacter, index: Int, repo: Repository, onEditHp: () -> Unit) {
    SheetCard(stringResource(R.string.dnd_mode_combat)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(stringResource(R.string.dnd_max_hp), "${c.hp}/${c.maxHp}", Modifier.weight(1.2f), onClick = onEditHp)
            StatTile(stringResource(R.string.dnd_ac), "${c.ac}", Modifier.weight(1.4f),
                onDec = { repo.update { s -> AppActions.dndAc(s, index, -1) } }, onInc = { repo.update { s -> AppActions.dndAc(s, index, +1) } })
            StatTile(stringResource(R.string.dnd_speed), "${c.speed}", Modifier.weight(1.4f),
                onDec = { repo.update { s -> AppActions.dndSpeed(s, index, -5) } }, onInc = { repo.update { s -> AppActions.dndSpeed(s, index, +5) } })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(stringResource(R.string.dnd_init_bonus), DndRules.signed(DndRules.initiativeBonus(c)), Modifier.weight(1f), accent = GOLD)
            StatTile(stringResource(R.string.dnd_passive), "${DndRules.passivePerception(c)}", Modifier.weight(1f))
        }
    }
}

// ---- ability scores + saving throws ----

@Composable
private fun AbilitiesCard(c: DndCharacter, index: Int, repo: Repository) {
    SheetCard(stringResource(R.string.dnd_abilities)) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 3) {
            DndAbility.entries.forEach { a ->
                val score = DndRules.score(c, a)
                val proficient = a in c.saveProficiencies
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0x14FFFFFF)).padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(abilityRes(a)), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp))
                    Text(DndRules.signed(DndRules.modifier(score)), color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, fontFeatureSettings = "tnum"))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniStep("−", 22) { repo.update { s -> AppActions.dndAbility(s, index, a, -1) } }
                        Text("$score", color = Palette.TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 24.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFeatureSettings = "tnum"))
                        MiniStep("+", 22) { repo.update { s -> AppActions.dndAbility(s, index, a, +1) } }
                    }
                    // saving throw: tap to toggle proficiency
                    Row(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(if (proficient) GOLD.copy(alpha = 0.22f) else Color.Transparent)
                            .border(1.dp, if (proficient) GOLD.copy(alpha = 0.7f) else Palette.ButtonBorder, RoundedCornerShape(999.dp))
                            .clickable { repo.update { s -> AppActions.dndToggleSave(s, index, a) } }.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GameIcon(R.drawable.ic_shield, if (proficient) GOLD else Palette.TextMuted, 11)
                        Text("${stringResource(R.string.dnd_save)} ${DndRules.signed(DndRules.saveBonus(c, a))}", color = if (proficient) GOLD else Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp))
                    }
                }
            }
        }
    }
}

// ---- skills ----

@Composable
private fun SkillsCard(c: DndCharacter, index: Int, repo: Repository) {
    SheetCard(stringResource(R.string.dnd_skills)) {
        Text(stringResource(R.string.dnd_skills_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp))
        Column {
            DndSkill.entries.forEach { sk ->
                val prof = c.skillProficiency[sk] ?: 0
                Row(
                    Modifier.fillMaxWidth().height(34.dp).clickable { repo.update { s -> AppActions.dndCycleSkill(s, index, sk) } },
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProfPip(prof)
                    Text(stringResource(skillRes(sk)), color = if (prof > 0) Palette.TextPrimary else Palette.TextSecondary, modifier = Modifier.weight(1f), maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = if (prof > 0) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp))
                    Text(stringResource(abilityRes(sk.ability)), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp, letterSpacing = 1.sp))
                    Text(DndRules.signed(DndRules.skillBonus(c, sk)), color = if (prof > 0) GOLD else Palette.TextSecondary, textAlign = TextAlign.End, modifier = Modifier.width(36.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFeatureSettings = "tnum"))
                }
            }
        }
    }
}

/** Proficiency marker: empty ring · filled (proficient) · filled with a halo (expertise). */
@Composable
private fun ProfPip(level: Int) {
    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        if (level == 2) Box(Modifier.size(18.dp).clip(CircleShape).border(1.5.dp, GOLD, CircleShape))
        Box(Modifier.size(11.dp).clip(CircleShape).background(if (level > 0) GOLD else Color.Transparent).border(1.dp, if (level > 0) GOLD else Palette.TextMuted, CircleShape))
    }
}

// ---- attacks ----

@Composable
private fun AttacksCard(c: DndCharacter, onAdd: () -> Unit, onEdit: (Int) -> Unit) {
    SheetCard(stringResource(R.string.dnd_attacks), trailing = {
        Box(Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onAdd() }.padding(horizontal = 10.dp, vertical = 5.dp)) {
            Text(stringResource(R.string.dnd_attack_add), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
        }
    }) {
        if (c.attacks.isEmpty()) {
            Text("—", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp))
        }
        c.attacks.forEachIndexed { i, a ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0x0FFFFFFF)).clickable { onEdit(i) }.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GameIcon(R.drawable.ic_sword, Palette.TextTertiary, 14)
                Text(a.name, color = Palette.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                Text(DndRules.signed(a.bonus), color = GOLD, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFeatureSettings = "tnum"))
                if (a.damage.isNotBlank()) Text(a.damage, color = Palette.TextSecondary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 12.sp))
            }
        }
    }
}

@Composable
private fun AttackDialog(initial: DndAttack?, onSave: (DndAttack) -> Unit, onRemove: (() -> Unit)?, onClose: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bonus by remember { mutableIntStateOf(initial?.bonus ?: 0) }
    var damage by remember { mutableStateOf(initial?.damage ?: "") }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = { onSave(DndAttack(name, bonus, damage)); onClose() }) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        dismissButton = {
            Row {
                if (onRemove != null) TextButton(onClick = { onRemove(); onClose() }) { Text(stringResource(R.string.dnd_attack_remove), color = Color(0xFFEB5757)) }
                TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) }
            }
        },
        title = { Text(stringResource(R.string.dnd_attack_title), color = Palette.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DialogField(stringResource(R.string.name), name) { name = it }
                Column {
                    SheetLabel(stringResource(R.string.dnd_attack_bonus))
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStep("−", 32) { bonus = (bonus - 1).coerceAtLeast(-10) }
                        Text(DndRules.signed(bonus), color = Palette.Cyan, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 48.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFeatureSettings = "tnum"))
                        MiniStep("+", 32) { bonus = (bonus + 1).coerceAtMost(30) }
                    }
                }
                DialogField(stringResource(R.string.dnd_attack_damage), damage) { damage = it }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@Composable
private fun DialogField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        SheetLabel(label)
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = true,
            textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 17.sp, color = Palette.TextPrimary),
            cursorBrush = SolidColor(Palette.Cyan),
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(Palette.Cyan))
    }
}

// ---- spell slots ----

@Composable
private fun SlotsCard(c: DndCharacter, index: Int, repo: Repository) {
    // rows for every level in use plus one spare so the next level can be added
    val rows = (DndRules.highestSlotLevel(c) + 1).coerceIn(1, DndRules.MAX_SPELL_LEVEL)
    SheetCard(stringResource(R.string.dnd_slots)) {
        Text(stringResource(R.string.dnd_slots_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp))
        (1..rows).forEach { level ->
            val max = DndRules.slotMax(c, level)
            val used = DndRules.slotUsed(c, level)
            Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.dnd_slot_level, level), color = if (max > 0) Palette.TextSecondary else Palette.TextMuted, modifier = Modifier.width(64.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(max) { i ->
                        // filled = still available, hollow = spent
                        val available = i < max - used
                        Box(
                            Modifier.size(18.dp).clip(CircleShape).background(if (available) Palette.GameDnd else Color.Transparent).border(1.5.dp, Palette.GameDnd.copy(alpha = if (available) 1f else 0.6f), CircleShape)
                                .clickable { repo.update { s -> AppActions.dndSlotUsed(s, index, level, if (available) used + 1 else used - 1) } },
                        )
                    }
                }
                MiniStep("−", 22) { repo.update { s -> AppActions.dndSlotMax(s, index, level, -1) } }
                Text("$max", color = Palette.TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 16.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFeatureSettings = "tnum"))
                MiniStep("+", 22) { repo.update { s -> AppActions.dndSlotMax(s, index, level, +1) } }
            }
        }
    }
}

// ---- free text (features / notes) ----

@Composable
private fun TextCard(key: Any, title: String, hint: String, text: String, onChange: (String) -> Unit) {
    var buffer by remember(key) { mutableStateOf(text) }
    SheetCard(title) {
        Box(Modifier.fillMaxWidth().heightIn(min = 90.dp).clip(RoundedCornerShape(12.dp)).background(Palette.ControlFill).padding(12.dp)) {
            if (buffer.isEmpty()) Text(hint, color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
            BasicTextField(
                value = buffer,
                onValueChange = { buffer = it; onChange(it) },
                textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary, lineHeight = 20.sp),
                cursorBrush = SolidColor(Palette.Cyan),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
