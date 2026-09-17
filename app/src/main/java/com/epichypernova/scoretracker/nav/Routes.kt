package com.epichypernova.scoretracker.nav

/** Central place for navigation route strings. */
object Routes {
    const val MENU = "menu"                 // Juegos tab (list/grid toggle inside)
    const val HISTORY = "history"           // Historial tab
    const val PLAYERS = "players"           // Jugadores tab
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

    fun genericSetup(gameType: String) = "generic_setup/$gameType"
    fun editPlayer(userId: String) = "edit_player/$userId"  // "new" to create
}
