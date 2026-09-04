package com.epichypernova.scoretracker.nav

/** Central place for navigation route strings. */
object Routes {
    const val MENU = "menu"                 // Juegos tab (list/grid toggle inside)
    const val HISTORY = "history"           // Historial tab
    const val PLAYERS = "players"           // Jugadores tab
    const val SETTINGS = "settings"

    const val GENERIC_SETUP = "generic_setup/{gameType}"
    const val GENERIC_TABLE = "generic_table"
    const val TRUCO = "truco"
    const val MAGIC_1V1 = "magic_1v1"
    const val MAGIC_COMMANDER = "magic_commander"
    const val EDIT_PLAYER = "edit_player/{userId}"

    fun genericSetup(gameType: String) = "generic_setup/$gameType"
    fun editPlayer(userId: String) = "edit_player/$userId"  // "new" to create
}
