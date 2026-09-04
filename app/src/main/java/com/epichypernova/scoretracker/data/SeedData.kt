package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.Cell
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.GenericRules
import com.epichypernova.scoretracker.data.model.HistoryEntry
import com.epichypernova.scoretracker.data.model.Round
import com.epichypernova.scoretracker.data.model.User

/**
 * Sample data seeded on first launch so the app matches the design's example content
 * (players Sofi/Nacho/Vale/Tomi, a Chinchón game in progress, some finished history).
 */
object SeedData {

    private const val CYAN = 0xFF2FD3F0
    private const val MINT = 0xFF55E6A5
    private const val PINK = 0xFFFF6FA8
    private const val VIOLET = 0xFFA18AF5
    private const val BLUE = 0xFF3B7BF7
    private const val ROSE = 0xFFF27BA9

    fun initial(): AppState {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        val sofi = User("u_sofi", "Sofi", CYAN, favorite = true, gamesPlayed = 24)
        val nacho = User("u_nacho", "Nacho", MINT, favorite = true, gamesPlayed = 19)
        val vale = User("u_vale", "Vale", PINK, gamesPlayed = 31)
        val tomi = User("u_tomi", "Tomi", VIOLET, gamesPlayed = 12)
        val lu = User("u_lu", "Lu", BLUE, gamesPlayed = 7)

        val players = listOf(sofi, nacho, vale, tomi, lu)

        // A Chinchón game in progress: 6 rounds played, hand 7 is next.
        val perRound = mapOf(
            sofi.id to listOf(10, 8, 12, 6, 9, 7),   // 52
            nacho.id to listOf(7, 9, 6, 11, 8, 7),    // 48
            vale.id to listOf(6, 4, 8, 5, 7, 6),      // 36  (leader, low wins)
            tomi.id to listOf(12, 10, 9, 13, 8, 9),   // 61
        )
        val orderedIds = listOf(sofi.id, nacho.id, vale.id, tomi.id)
        val rounds = (0 until 6).map { r ->
            Round(
                index = r,
                cells = orderedIds.map { pid -> Cell(pid, perRound.getValue(pid)[r]) },
            )
        }
        val current = CurrentGame(
            id = "g_current",
            gameType = GameType.MANOS_Y_PUNTOS,
            name = "Chinchón",
            playerIds = orderedIds,
            rules = GenericRules(targetScore = 100, lowWins = true),
            rounds = rounds,
            startedAt = now - 40 * 60 * 1000L,
        )

        val history = listOf(
            HistoryEntry(
                id = "h1",
                gameType = GameType.TRUCO,
                playerIds = emptyList(),
                playerNames = listOf("Nosotros", "Ellos"),
                winnerNames = listOf("Nosotros"),
                summary = "2 a 1 · a 30",
                finishedAt = now - 1 * day,
            ),
            HistoryEntry(
                id = "h2",
                gameType = GameType.MANOS_Y_PUNTOS,
                playerIds = listOf(sofi.id, nacho.id, vale.id),
                playerNames = listOf("Sofi", "Nacho", "Vale"),
                winnerNames = listOf("Sofi"),
                summary = "Chinchón · Sofi 78 · a 100",
                finishedAt = now - 2 * day,
            ),
            HistoryEntry(
                id = "h3",
                gameType = GameType.MAGIC,
                playerIds = emptyList(),
                playerNames = listOf("Vos", "Rival"),
                winnerNames = listOf("Vos"),
                summary = "Commander · 40 vidas",
                finishedAt = now - 3 * day,
            ),
            HistoryEntry(
                id = "h4",
                gameType = GameType.MANOS_Y_PUNTOS,
                playerIds = listOf(tomi.id, lu.id, vale.id, nacho.id),
                playerNames = listOf("Tomi", "Lu", "Vale", "Nacho"),
                winnerNames = listOf("Lu"),
                summary = "Generala · Lu",
                finishedAt = now - 9 * day,
            ),
        )

        return AppState(
            users = players,
            currentGame = current,
            history = history,
        )
    }
}
