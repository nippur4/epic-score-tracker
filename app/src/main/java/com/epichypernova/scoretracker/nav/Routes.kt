package com.epichypernova.scoretracker.nav

import com.epichypernova.scoretracker.data.model.GameType

/** Central place for navigation route strings. */
object Routes {
    const val MENU = "menu"                 // Juegos tab (list/grid toggle inside)
    const val HISTORY = "history"           // Historial tab
    const val PLAYERS = "players"           // Jugadores tab
    const val DICE = "dice"                 // Dados tab
    const val SETTINGS = "settings"

    const val GENERIC_SETUP = "generic_setup/{gameType}"
    const val GENERIC_TABLE = "generic_table"
    const val END_GAME = "end_game"
    const val TRUCO = "truco"
    const val MAGIC = "magic"
    const val YUGIOH = "yugioh"
    const val POKEMON = "pokemon"
    const val DIGIMON = "digimon"
    const val LORCANA = "lorcana"
    const val ONEPIECE = "onepiece"
    const val CHINCHON = "chinchon"
    const val BURAKO = "burako"
    const val DARTS = "darts"
    const val DND = "dnd"
    const val GENERALA = "generala"
    const val BOWLING = "bowling"
    const val UNO = "uno"
    const val WARHAMMER = "warhammer"
    const val POKER = "poker"
    const val SWU = "swu"
    const val ESCOBA = "escoba"
    const val MUS = "mus"
    const val EDIT_PLAYER = "edit_player/{userId}"

    /** The board route for a game type; the generics share the table. */
    fun forGame(type: GameType): String = when (type) {
        GameType.MANOS_Y_PUNTOS, GameType.CONTADOR_SIMPLE -> GENERIC_TABLE
        GameType.TRUCO -> TRUCO
        GameType.MAGIC -> MAGIC
        GameType.YUGIOH -> YUGIOH
        GameType.POKEMON -> POKEMON
        GameType.DIGIMON -> DIGIMON
        GameType.LORCANA -> LORCANA
        GameType.ONEPIECE -> ONEPIECE
        GameType.CHINCHON -> CHINCHON
        GameType.BURAKO -> BURAKO
        GameType.DARTS -> DARTS
        GameType.DND -> DND
        GameType.GENERALA -> GENERALA
        GameType.BOWLING -> BOWLING
        GameType.UNO -> UNO
        GameType.WARHAMMER -> WARHAMMER
        GameType.POKER -> POKER
        GameType.SWU -> SWU
        GameType.ESCOBA -> ESCOBA
        GameType.MUS -> MUS
    }

    fun genericSetup(gameType: String) = "generic_setup/$gameType"
    fun editPlayer(userId: String) = "edit_player/$userId"  // "new" to create
}
