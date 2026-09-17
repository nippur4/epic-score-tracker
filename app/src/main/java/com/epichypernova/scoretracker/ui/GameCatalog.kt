package com.epichypernova.scoretracker.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.theme.Palette

/** Presentation metadata for a game row/tile. Text is referenced by string resource id. */
data class GameEntry(
    val gameType: GameType,
    val glyph: String,
    val tint: Color,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val available: Boolean = true,
)

/** Menu grouping for the specific games. [key] is what gets persisted in the collapsed-sections set. */
enum class GameCategory(val key: String, @StringRes val titleRes: Int) {
    CARDS("cards", R.string.cat_cards),
    TCG("tcg", R.string.cat_tcg),
    RPG("rpg", R.string.cat_rpg),
    DICE("dice", R.string.cat_dice),
}

object GameCatalog {
    val generics = listOf(
        GameEntry(GameType.MANOS_Y_PUNTOS, "♠", Palette.GameGeneric, R.string.game_manos_title, R.string.game_manos_sub),
        GameEntry(GameType.CONTADOR_SIMPLE, "±", Palette.GameGeneric, R.string.game_contador_title, R.string.game_contador_sub),
    )

    val specifics = listOf(
        GameEntry(GameType.TRUCO, "♣", Palette.GameGeneric, R.string.game_truco_title, R.string.game_truco_sub),
        GameEntry(GameType.MAGIC, "M", Palette.GameMagic, R.string.game_magic_title, R.string.game_magic_sub),
        GameEntry(GameType.POKEMON, "P", Palette.GamePokemon, R.string.game_pokemon_title, R.string.game_pokemon_sub),
        GameEntry(GameType.YUGIOH, "Y", Palette.GameYugioh, R.string.game_yugioh_title, R.string.game_yugioh_sub),
        GameEntry(GameType.DIGIMON, "D", Palette.GameDigimon, R.string.game_digimon_title, R.string.game_digimon_sub),
        GameEntry(GameType.LORCANA, "L", Palette.GameLorcana, R.string.game_lorcana_title, R.string.game_lorcana_sub),
        GameEntry(GameType.ONEPIECE, "OP", Palette.GameOnePiece, R.string.game_onepiece_title, R.string.game_onepiece_sub),
        GameEntry(GameType.CHINCHON, "CH", Palette.GameChinchon, R.string.game_chinchon_title, R.string.game_chinchon_sub),
        GameEntry(GameType.BURAKO, "BK", Palette.GameBurako, R.string.game_burako_title, R.string.game_burako_sub),
        GameEntry(GameType.DARTS, "🎯", Palette.GameDarts, R.string.game_darts_title, R.string.game_darts_sub),
        GameEntry(GameType.DND, "🎲", Palette.GameDnd, R.string.game_dnd_title, R.string.game_dnd_sub),
        GameEntry(GameType.GENERALA, "⚄", Palette.GameGenerala, R.string.game_generala_title, R.string.game_generala_sub),
        GameEntry(GameType.BOWLING, "🎳", Palette.GameBowling, R.string.game_bowling_title, R.string.game_bowling_sub),
        GameEntry(GameType.UNO, "U", Palette.GameUno, R.string.game_uno_title, R.string.game_uno_sub),
        GameEntry(GameType.WARHAMMER, "W", Palette.GameWarhammer, R.string.game_warhammer_title, R.string.game_warhammer_sub),
        GameEntry(GameType.POKER, "♠", Palette.GamePoker, R.string.game_poker_title, R.string.game_poker_sub),
        GameEntry(GameType.SWU, "SW", Palette.GameSwu, R.string.game_swu_title, R.string.game_swu_sub),
        GameEntry(GameType.ESCOBA, "15", Palette.GameEscoba, R.string.game_escoba_title, R.string.game_escoba_sub),
        GameEntry(GameType.MUS, "♦", Palette.GameMus, R.string.game_mus_title, R.string.game_mus_sub),
    )

    private val categoryOf = mapOf(
        GameType.TRUCO to GameCategory.CARDS, GameType.CHINCHON to GameCategory.CARDS, GameType.BURAKO to GameCategory.CARDS,
        GameType.ESCOBA to GameCategory.CARDS, GameType.MUS to GameCategory.CARDS, GameType.UNO to GameCategory.CARDS, GameType.POKER to GameCategory.CARDS,
        GameType.MAGIC to GameCategory.TCG, GameType.POKEMON to GameCategory.TCG, GameType.YUGIOH to GameCategory.TCG, GameType.DIGIMON to GameCategory.TCG,
        GameType.LORCANA to GameCategory.TCG, GameType.ONEPIECE to GameCategory.TCG, GameType.SWU to GameCategory.TCG,
        GameType.DND to GameCategory.RPG, GameType.WARHAMMER to GameCategory.RPG,
        GameType.GENERALA to GameCategory.DICE, GameType.DARTS to GameCategory.DICE, GameType.BOWLING to GameCategory.DICE,
    )

    /** Specific games grouped by category, in menu order. Anything unmapped falls into CARDS. */
    val byCategory: List<Pair<GameCategory, List<GameEntry>>> = GameCategory.entries.map { cat ->
        cat to specifics.filter { (categoryOf[it.gameType] ?: GameCategory.CARDS) == cat }
    }.filter { it.second.isNotEmpty() }

    fun tintFor(type: GameType): Color = when (type) {
        GameType.MAGIC -> Palette.GameMagic
        GameType.POKEMON -> Palette.GamePokemon
        GameType.YUGIOH -> Palette.GameYugioh
        GameType.DIGIMON -> Palette.GameDigimon
        GameType.LORCANA -> Palette.GameLorcana
        GameType.ONEPIECE -> Palette.GameOnePiece
        GameType.CHINCHON -> Palette.GameChinchon
        GameType.BURAKO -> Palette.GameBurako
        GameType.DARTS -> Palette.GameDarts
        GameType.DND -> Palette.GameDnd
        GameType.GENERALA -> Palette.GameGenerala
        GameType.BOWLING -> Palette.GameBowling
        GameType.UNO -> Palette.GameUno
        GameType.WARHAMMER -> Palette.GameWarhammer
        GameType.POKER -> Palette.GamePoker
        GameType.SWU -> Palette.GameSwu
        GameType.ESCOBA -> Palette.GameEscoba
        GameType.MUS -> Palette.GameMus
        else -> Palette.GameGeneric
    }

    fun glyphFor(type: GameType): String = when (type) {
        GameType.TRUCO -> "♣"
        GameType.MAGIC -> "M"
        GameType.POKEMON -> "P"
        GameType.YUGIOH -> "Y"
        GameType.DIGIMON -> "D"
        GameType.LORCANA -> "L"
        GameType.ONEPIECE -> "OP"
        GameType.CHINCHON -> "CH"
        GameType.BURAKO -> "BK"
        GameType.DARTS -> "🎯"
        GameType.DND -> "🎲"
        GameType.GENERALA -> "⚄"
        GameType.BOWLING -> "🎳"
        GameType.UNO -> "U"
        GameType.WARHAMMER -> "W"
        GameType.POKER -> "♠"
        GameType.SWU -> "SW"
        GameType.ESCOBA -> "15"
        GameType.MUS -> "♦"
        GameType.CONTADOR_SIMPLE -> "±"
        GameType.MANOS_Y_PUNTOS -> "♠"
    }

    @StringRes
    fun labelRes(type: GameType): Int = when (type) {
        GameType.MANOS_Y_PUNTOS -> R.string.game_manos_title
        GameType.CONTADOR_SIMPLE -> R.string.game_contador_title
        GameType.TRUCO -> R.string.game_truco_title
        GameType.MAGIC -> R.string.game_magic_short
        GameType.POKEMON -> R.string.game_pokemon_title
        GameType.YUGIOH -> R.string.game_yugioh_title
        GameType.DIGIMON -> R.string.game_digimon_title
        GameType.LORCANA -> R.string.game_lorcana_title
        GameType.ONEPIECE -> R.string.game_onepiece_title
        GameType.CHINCHON -> R.string.game_chinchon_title
        GameType.BURAKO -> R.string.game_burako_title
        GameType.DARTS -> R.string.game_darts_title
        GameType.DND -> R.string.game_dnd_title
        GameType.GENERALA -> R.string.game_generala_title
        GameType.BOWLING -> R.string.game_bowling_title
        GameType.UNO -> R.string.game_uno_title
        GameType.WARHAMMER -> R.string.game_warhammer_title
        GameType.POKER -> R.string.game_poker_title
        GameType.SWU -> R.string.game_swu_title
        GameType.ESCOBA -> R.string.game_escoba_title
        GameType.MUS -> R.string.game_mus_title
    }
}
