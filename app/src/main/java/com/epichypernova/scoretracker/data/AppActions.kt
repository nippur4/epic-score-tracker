package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.Cell
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.GenericRules
import com.epichypernova.scoretracker.data.model.HistoryEntry
import com.epichypernova.scoretracker.data.model.MagicGame
import com.epichypernova.scoretracker.data.model.MagicMode
import com.epichypernova.scoretracker.data.model.MagicPlayer
import com.epichypernova.scoretracker.data.model.Round
import com.epichypernova.scoretracker.data.model.SavedConfig
import com.epichypernova.scoretracker.data.model.Settings
import com.epichypernova.scoretracker.data.model.TrucoEvent
import com.epichypernova.scoretracker.data.model.TrucoMatch
import com.epichypernova.scoretracker.data.model.TrucoSide
import com.epichypernova.scoretracker.data.model.User
import java.util.UUID

/**
 * Pure state transitions. Each function returns a new [AppState]; callers pass these to
 * [Repository.update]. Keeping game logic here makes it reusable and unit-testable.
 */
object AppActions {

    fun newId(prefix: String): String = prefix + "_" + UUID.randomUUID().toString().take(8)

    // ---------- Players ----------

    fun addUser(s: AppState, name: String, color: Long, favorite: Boolean): AppState {
        val user = User(newId("u"), name.trim(), color, favorite)
        return s.copy(users = s.users + user)
    }

    fun updateUser(s: AppState, updated: User): AppState =
        s.copy(users = s.users.map { if (it.id == updated.id) updated else it })

    /** Deletes a user but keeps their finished games in history. */
    fun deleteUser(s: AppState, id: String): AppState =
        s.copy(users = s.users.filterNot { it.id == id })

    /** Users ordered for game setup: favorites first, then by name. */
    fun playersForSetup(s: AppState): List<User> =
        s.users.sortedWith(compareByDescending<User> { it.favorite }.thenBy { it.name.lowercase() })

    // ---------- Saved configs ----------

    fun saveConfig(
        s: AppState,
        name: String,
        gameType: GameType,
        playerIds: List<String>,
        rules: GenericRules,
    ): AppState {
        val cfg = SavedConfig(newId("cfg"), name.trim(), gameType, playerIds, rules)
        return s.copy(savedConfigs = s.savedConfigs + cfg)
    }

    fun deleteConfig(s: AppState, id: String): AppState =
        s.copy(savedConfigs = s.savedConfigs.filterNot { it.id == id })

    // ---------- Generic game ----------

    fun startGeneric(
        s: AppState,
        gameType: GameType,
        name: String?,
        playerIds: List<String>,
        rules: GenericRules,
    ): AppState {
        val game = CurrentGame(
            id = newId("g"),
            gameType = gameType,
            name = name,
            playerIds = playerIds,
            rules = rules,
            rounds = emptyList(),
            startedAt = System.currentTimeMillis(),
        )
        return s.copy(currentGame = game)
    }

    /** Adds a new round or replaces an existing one (edit). Cells keyed by playerId. */
    fun saveRound(s: AppState, roundIndex: Int, points: Map<String, Int>, bids: Map<String, Int?>): AppState {
        val game = s.currentGame ?: return s
        val cells = game.playerIds.map { pid -> Cell(pid, points[pid] ?: 0, bids[pid]) }
        val rounds = game.rounds.toMutableList()
        val existing = rounds.indexOfFirst { it.index == roundIndex }
        val round = Round(roundIndex, cells)
        if (existing >= 0) rounds[existing] = round else rounds.add(round)
        rounds.sortBy { it.index }
        return s.copy(currentGame = game.copy(rounds = rounds))
    }

    /** Finalises the current generic game: writes a history entry and clears it. */
    fun finishGeneric(s: AppState): AppState {
        val game = s.currentGame ?: return s
        val totals = Derivations.totals(game)
        val winners = Derivations.leaders(game)
        val nameOf = { id: String -> s.users.firstOrNull { it.id == id }?.name ?: "?" }
        val entry = HistoryEntry(
            id = newId("h"),
            gameType = game.gameType,
            playerIds = game.playerIds,
            playerNames = game.playerIds.map(nameOf),
            winnerNames = winners.map(nameOf),
            summary = (game.name ?: "Partida") + " · " +
                winners.joinToString("/") { nameOf(it) } + " " +
                (winners.firstOrNull()?.let { totals[it] } ?: 0) + " · a " + game.rules.targetScore,
            finishedAt = System.currentTimeMillis(),
        )
        val bumpedUsers = s.users.map {
            if (it.id in game.playerIds) it.copy(gamesPlayed = it.gamesPlayed + 1) else it
        }
        return s.copy(
            users = bumpedUsers,
            currentGame = null,
            history = listOf(entry) + s.history,
        )
    }

    fun discardCurrentGeneric(s: AppState): AppState = s.copy(currentGame = null)

    // ---------- Truco ----------

    private const val TRUCO_TARGET = 30

    fun trucoStart(s: AppState): AppState = s.copy(trucoMatch = TrucoMatch())

    fun trucoEnsure(s: AppState): AppState =
        if (s.trucoMatch == null) s.copy(trucoMatch = TrucoMatch()) else s

    /** Adds points to a side. On reaching 30 the side wins the match; porotos reset. */
    fun trucoAdd(s: AppState, us: Boolean, amount: Int): AppState {
        val m = s.trucoMatch ?: TrucoMatch()
        val history = m.history + TrucoEvent(m.us, m.them)
        var usSide = m.us
        var themSide = m.them
        if (us) {
            val p = usSide.points + amount
            usSide = if (p >= TRUCO_TARGET) usSide.copy(points = 0, gamesWon = usSide.gamesWon + 1)
            else usSide.copy(points = p)
            if (p >= TRUCO_TARGET) themSide = themSide.copy(points = 0)
        } else {
            val p = themSide.points + amount
            themSide = if (p >= TRUCO_TARGET) themSide.copy(points = 0, gamesWon = themSide.gamesWon + 1)
            else themSide.copy(points = p)
            if (p >= TRUCO_TARGET) usSide = usSide.copy(points = 0)
        }
        return s.copy(trucoMatch = m.copy(us = usSide, them = themSide, history = history))
    }

    fun trucoRemove(s: AppState, us: Boolean): AppState {
        val m = s.trucoMatch ?: return s
        val history = m.history + TrucoEvent(m.us, m.them)
        return if (us) {
            s.copy(trucoMatch = m.copy(us = m.us.copy(points = (m.us.points - 1).coerceAtLeast(0)), history = history))
        } else {
            s.copy(trucoMatch = m.copy(them = m.them.copy(points = (m.them.points - 1).coerceAtLeast(0)), history = history))
        }
    }

    fun trucoUndo(s: AppState): AppState {
        val m = s.trucoMatch ?: return s
        val last = m.history.lastOrNull() ?: return s
        return s.copy(trucoMatch = m.copy(us = last.us, them = last.them, history = m.history.dropLast(1)))
    }

    /** Resets porotos but keeps the match (partidos) score. */
    fun trucoNewMatch(s: AppState): AppState {
        val m = s.trucoMatch ?: return s
        return s.copy(trucoMatch = m.copy(us = m.us.copy(points = 0), them = m.them.copy(points = 0), history = emptyList()))
    }

    // ---------- Magic ----------

    fun magicStart(s: AppState, mode: MagicMode): AppState {
        val life = if (mode == MagicMode.COMMANDER) 40 else 20
        val palette = listOf(0xFFA18AF5, 0xFF55E6A5, 0xFF2FD3F0, 0xFFFF6FA8)
        val count = if (mode == MagicMode.COMMANDER) 4 else 2
        val names = if (mode == MagicMode.COMMANDER) listOf("Vos", "Rival 1", "Rival 2", "Rival 3")
        else listOf("Vos", "Rival")
        val players = (0 until count).map { i ->
            MagicPlayer(name = names[i], color = palette[i], life = life)
        }
        return s.copy(magicGame = MagicGame(mode, players, life))
    }

    fun magicEnsure(s: AppState, mode: MagicMode): AppState =
        if (s.magicGame?.mode == mode) s else magicStart(s, mode)

    private fun MagicGame.mapPlayer(index: Int, f: (MagicPlayer) -> MagicPlayer): MagicGame =
        copy(players = players.mapIndexed { i, p -> if (i == index) recomputeEliminated(f(p)) else p })

    private fun recomputeEliminated(p: MagicPlayer): MagicPlayer =
        p.copy(eliminated = p.life <= 0 || p.poison >= 10)

    fun magicLife(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) { it.copy(life = it.life + delta) })
    }

    fun magicPoison(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) { it.copy(poison = (it.poison + delta).coerceAtLeast(0)) })
    }

    fun magicEnergy(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) { it.copy(energy = (it.energy + delta).coerceAtLeast(0)) })
    }

    fun magicCommanderDamage(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) {
            val cd = (it.commanderDamage + delta).coerceAtLeast(0)
            // commander damage also chips life when increasing
            it.copy(commanderDamage = cd, life = if (delta > 0) it.life - delta else it.life)
        })
    }

    fun magicReset(s: AppState): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.copy(players = g.players.map {
            it.copy(life = g.startingLife, poison = 0, energy = 0, commanderDamage = 0, eliminated = false)
        }))
    }

    // ---------- Settings ----------

    fun setLanguage(s: AppState, language: String): AppState =
        s.copy(settings = s.settings.copy(language = language))
}
