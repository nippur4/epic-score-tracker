package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.Cell
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.data.model.GameResult
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.GenericRules
import com.epichypernova.scoretracker.data.model.ResultLine
import com.epichypernova.scoretracker.data.model.HistoryEntry
import com.epichypernova.scoretracker.data.model.MagicGame
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

    fun addUser(s: AppState, name: String, color: Long, favorite: Boolean, avatarId: Int? = null): AppState {
        val user = User(newId("u"), name.trim(), color, favorite, avatarId = avatarId)
        return s.copy(users = s.users + user)
    }

    fun updateUser(s: AppState, updated: User): AppState =
        s.copy(users = s.users.map { if (it.id == updated.id) updated else it })

    /** Deletes a user but keeps their finished games in history. */
    fun deleteUser(s: AppState, id: String): AppState =
        s.copy(users = s.users.filterNot { it.id == id })

    /** Unlocks an avatar (after a rewarded ad). */
    fun unlockAvatar(s: AppState, id: Int): AppState =
        s.copy(unlockedAvatars = s.unlockedAvatars + id)

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

    /** Starts a fresh generic game from a saved configuration. */
    fun startFromConfig(s: AppState, config: SavedConfig): AppState =
        startGeneric(s, config.gameType, config.name, config.playerIds, config.rules)

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

    private fun putRound(game: CurrentGame, round: Round): CurrentGame {
        val rounds = game.rounds.toMutableList()
        val existing = rounds.indexOfFirst { it.index == round.index }
        if (existing >= 0) rounds[existing] = round else rounds.add(round)
        rounds.sortBy { it.index }
        return game.copy(rounds = rounds)
    }

    /** Simple games: the user types the points directly for each player. */
    fun savePoints(s: AppState, roundIndex: Int, points: Map<String, Int>): AppState {
        val game = s.currentGame ?: return s
        val cells = game.playerIds.map { pid -> Cell(pid, points[pid] ?: 0) }
        return s.copy(currentGame = putRound(game, Round(roundIndex, cells)))
    }

    /** "Basas" phase 1: declare bids. Creates/updates a points-pending round (hits == null). */
    fun saveBids(s: AppState, roundIndex: Int, bids: Map<String, Int?>): AppState {
        val game = s.currentGame ?: return s
        val existing = game.rounds.firstOrNull { it.index == roundIndex }
        val cells = game.playerIds.map { pid ->
            val prev = existing?.cells?.firstOrNull { it.playerId == pid }
            Cell(pid, points = prev?.points ?: 0, bid = bids[pid], hits = prev?.hits, extra = prev?.extra)
        }
        return s.copy(currentGame = putRound(game, Round(roundIndex, cells)))
    }

    /**
     * "Basas" phase 2: enter manos ganadas (hits) + extra. You score [pointsPerHit] only if your
     * declared bid matches exactly (bid == hits) — that's one "acierto" — plus the extra points.
     */
    fun saveHandPoints(s: AppState, roundIndex: Int, hits: Map<String, Int>, extra: Map<String, Int>): AppState {
        val game = s.currentGame ?: return s
        val pph = game.rules.pointsPerHit
        val existing = game.rounds.firstOrNull { it.index == roundIndex }
        val cells = game.playerIds.map { pid ->
            val h = hits[pid] ?: 0
            val e = extra[pid] ?: 0
            val prevBid = existing?.cells?.firstOrNull { it.playerId == pid }?.bid
            val acierto = prevBid != null && prevBid == h
            Cell(pid, points = (if (acierto) pph else 0) + e, bid = prevBid, hits = h, extra = e)
        }
        return s.copy(currentGame = putRound(game, Round(roundIndex, cells)))
    }

    /** Finalises the current generic game: builds the result, records history, clears the game. */
    fun finishGeneric(s: AppState): AppState {
        val game = s.currentGame ?: return s
        val totals = Derivations.totals(game)
        val winnerIds = Derivations.leaders(game)
        val nameOf = { id: String -> s.users.firstOrNull { it.id == id }?.name ?: "?" }
        val colorOf = { id: String -> s.users.firstOrNull { it.id == id }?.color ?: 0xFF2FD3F0 }
        val ranking = game.playerIds.sortedBy { (totals[it] ?: 0) * (if (game.rules.lowWins) 1 else -1) }
        val lines = ranking.map { id -> ResultLine(nameOf(id), (totals[id] ?: 0).toString(), colorOf(id), id in winnerIds) }
        val winnerNames = winnerIds.map(nameOf)
        val title = game.name ?: "Partida"
        val summary = title + " · " + winnerNames.joinToString("/") + " · a " + game.rules.targetScore
        val entry = HistoryEntry(
            id = newId("h"),
            gameType = game.gameType,
            playerIds = game.playerIds,
            playerNames = game.playerIds.map(nameOf),
            winnerNames = winnerNames,
            summary = summary,
            finishedAt = System.currentTimeMillis(),
        )
        val bumpedUsers = s.users.map {
            if (it.id in game.playerIds) it.copy(gamesPlayed = it.gamesPlayed + 1) else it
        }
        return s.copy(
            users = bumpedUsers,
            currentGame = null,
            history = listOf(entry) + s.history,
            pendingResult = GameResult(game.gameType, title, winnerNames, lines, summary),
        )
    }

    /** Dismisses the winner screen. */
    fun clearResult(s: AppState): AppState = s.copy(pendingResult = null)

    // ---------- Truco ----------

    fun trucoStart(s: AppState, target: Int = 30): AppState = s.copy(trucoMatch = TrucoMatch(target = target))

    /** Adds points to a side. On reaching the target the side wins the partido; shows the winner. */
    fun trucoAdd(s: AppState, us: Boolean, amount: Int): AppState {
        val m = s.trucoMatch ?: return s
        val target = m.target
        val history = m.history + TrucoEvent(m.us, m.them)
        var usSide = m.us
        var themSide = m.them
        var winnerUs: Boolean? = null
        if (us) {
            val p = usSide.points + amount
            if (p >= target) { usSide = usSide.copy(points = 0, gamesWon = usSide.gamesWon + 1); themSide = themSide.copy(points = 0); winnerUs = true }
            else usSide = usSide.copy(points = p)
        } else {
            val p = themSide.points + amount
            if (p >= target) { themSide = themSide.copy(points = 0, gamesWon = themSide.gamesWon + 1); usSide = usSide.copy(points = 0); winnerUs = false }
            else themSide = themSide.copy(points = p)
        }
        var ns = s.copy(trucoMatch = m.copy(us = usSide, them = themSide, history = history))
        if (winnerUs != null) {
            val usName = "Nosotros"; val themName = "Ellos"
            val wName = if (winnerUs) usName else themName
            val lines = listOf(
                ResultLine(usName, usSide.gamesWon.toString(), 0xFF2FD3F0, winnerUs),
                ResultLine(themName, themSide.gamesWon.toString(), 0xFFE24BD6, !winnerUs),
            )
            val summary = "$usName ${usSide.gamesWon} - ${themSide.gamesWon} $themName"
            val entry = HistoryEntry(newId("h"), GameType.TRUCO, emptyList(), listOf(usName, themName), listOf(wName), summary, System.currentTimeMillis())
            ns = ns.copy(history = listOf(entry) + ns.history, pendingResult = GameResult(GameType.TRUCO, "Truco", listOf(wName), lines, summary))
        }
        return ns
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

    private val magicPalette = listOf(0xFFA18AF5, 0xFF55E6A5, 0xFF2FD3F0, 0xFFFF6FA8)
    private val magicNames = listOf("Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4")

    fun magicStart(s: AppState, count: Int, commander: Boolean, startingLife: Int? = null): AppState {
        val life = startingLife ?: if (commander) 40 else 20
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i -> MagicPlayer(name = magicNames[i], color = magicPalette[i], life = life) }
        return s.copy(magicGame = MagicGame(commander, players, life))
    }

    fun magicEnsureExists(s: AppState): AppState =
        if (s.magicGame != null) s else magicStart(s, count = 2, commander = false)

    /** Reconfigure player count / commander, preserving existing players where possible. */
    fun magicConfigure(s: AppState, count: Int, commander: Boolean): AppState {
        val g = s.magicGame ?: return magicStart(s, count, commander)
        val life = if (commander) 40 else 20
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i ->
            val prev = g.players.getOrNull(i)
            MagicPlayer(
                name = prev?.name ?: magicNames[i],
                color = prev?.color ?: magicPalette[i],
                life = life,
            )
        }
        return s.copy(magicGame = MagicGame(commander, players, life))
    }

    fun magicSetLife(s: AppState, startingLife: Int): AppState {
        val g = s.magicGame ?: return s
        val life = startingLife.coerceAtLeast(1)
        return s.copy(magicGame = g.copy(startingLife = life, finished = false, players = g.players.map {
            it.copy(life = life, poison = 0, energy = 0, experience = 0, eliminated = false)
        }))
    }

    fun magicSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    private fun MagicGame.mapPlayer(index: Int, f: (MagicPlayer) -> MagicPlayer): MagicGame =
        copy(players = players.mapIndexed { i, p -> if (i == index) recomputeEliminated(f(p)) else p })

    private fun recomputeEliminated(p: MagicPlayer): MagicPlayer =
        p.copy(eliminated = p.life <= 0 || p.poison >= 10)

    /** When only one player is left standing, mark finished, record history and show the winner. */
    private fun checkMagicEnd(s: AppState): AppState {
        val g = s.magicGame ?: return s
        if (g.finished || g.players.size <= 1) return s
        val alive = g.players.filter { !it.eliminated }
        if (alive.size > 1 || g.players.none { it.eliminated }) return s
        val winner = alive.firstOrNull()
        val wName = winner?.name ?: "—"
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it == winner) }
        val label = if (g.commander) "Commander" else "Magic ${g.players.size}p"
        val summary = "$label · $wName"
        val entry = HistoryEntry(newId("h"), GameType.MAGIC, emptyList(), g.players.map { it.name }, listOfNotNull(winner?.name), summary, System.currentTimeMillis())
        return s.copy(
            magicGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.MAGIC, "Magic", listOf(wName), lines, summary),
        )
    }

    fun magicLife(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return checkMagicEnd(s.copy(magicGame = g.mapPlayer(index) { it.copy(life = it.life + delta) }))
    }

    fun magicPoison(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return checkMagicEnd(s.copy(magicGame = g.mapPlayer(index) { it.copy(poison = (it.poison + delta).coerceAtLeast(0)) }))
    }

    fun magicEnergy(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) { it.copy(energy = (it.energy + delta).coerceAtLeast(0)) })
    }

    fun magicExperience(s: AppState, index: Int, delta: Int): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.mapPlayer(index) { it.copy(experience = (it.experience + delta).coerceAtLeast(0)) })
    }

    fun magicReset(s: AppState): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.copy(finished = false, players = g.players.map {
            it.copy(life = g.startingLife, poison = 0, energy = 0, experience = 0, eliminated = false)
        }))
    }

    // ---------- Settings ----------

    fun setLanguage(s: AppState, language: String): AppState =
        s.copy(settings = s.settings.copy(language = language))
}
