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
        GameEntry(GameType.DIGIMON, "D", Palette.GameDigimon, R.string.game_digimon_title, R.string.game_digimon_sub, available = false),
    )

    fun tintFor(type: GameType): Color = when (type) {
        GameType.MAGIC -> Palette.GameMagic
        GameType.POKEMON -> Palette.GamePokemon
        GameType.YUGIOH -> Palette.GameYugioh
        GameType.DIGIMON -> Palette.GameDigimon
        else -> Palette.GameGeneric
    }

    fun glyphFor(type: GameType): String = when (type) {
        GameType.TRUCO -> "♣"
        GameType.MAGIC -> "M"
        GameType.POKEMON -> "P"
        GameType.YUGIOH -> "Y"
        GameType.DIGIMON -> "D"
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
    }
}
