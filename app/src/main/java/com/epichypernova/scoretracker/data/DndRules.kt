package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.DndAbility
import com.epichypernova.scoretracker.data.model.DndCharacter
import com.epichypernova.scoretracker.data.model.DndSkill

/** Pure 5e arithmetic for the character sheet (modifiers, proficiency, derived bonuses). */
object DndRules {
    const val MAX_SPELL_LEVEL = 9

    fun score(c: DndCharacter, a: DndAbility): Int = c.abilities[a] ?: 10

    /** Ability modifier: floor((score − 10) / 2). */
    fun modifier(score: Int): Int = Math.floorDiv(score - 10, 2)

    fun modifier(c: DndCharacter, a: DndAbility): Int = modifier(score(c, a))

    /** Proficiency bonus by level: +2 at 1–4, +3 at 5–8, … +6 at 17–20. */
    fun proficiency(level: Int): Int = 2 + (level.coerceIn(1, 20) - 1) / 4

    fun saveBonus(c: DndCharacter, a: DndAbility): Int =
        modifier(c, a) + if (a in c.saveProficiencies) proficiency(c.level) else 0

    /** Skill bonus: ability modifier + proficiency (×2 for expertise). */
    fun skillBonus(c: DndCharacter, s: DndSkill): Int =
        modifier(c, s.ability) + proficiency(c.level) * (c.skillProficiency[s] ?: 0)

    fun passivePerception(c: DndCharacter): Int = 10 + skillBonus(c, DndSkill.PERCEPTION)

    fun initiativeBonus(c: DndCharacter): Int = modifier(c, DndAbility.DEX)

    fun slotMax(c: DndCharacter, level: Int): Int = c.slotMax.getOrNull(level - 1) ?: 0

    fun slotUsed(c: DndCharacter, level: Int): Int = c.slotUsed.getOrNull(level - 1) ?: 0

    /** Highest spell level with slots, so the sheet can show just the rows in use (plus one to grow). */
    fun highestSlotLevel(c: DndCharacter): Int = c.slotMax.indexOfLast { it > 0 } + 1

    fun signed(n: Int): String = if (n >= 0) "+$n" else "$n"
}
