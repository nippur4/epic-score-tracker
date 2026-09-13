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
import com.epichypernova.scoretracker.data.model.DigimonGame
import com.epichypernova.scoretracker.data.model.DigimonPlayer
import com.epichypernova.scoretracker.data.model.BurakoGame
import com.epichypernova.scoretracker.data.model.BurakoTeam
import com.epichypernova.scoretracker.data.model.ChinchonGame
import com.epichypernova.scoretracker.data.model.ChinchonPlayer
import com.epichypernova.scoretracker.data.model.DartsGame
import com.epichypernova.scoretracker.data.model.DartsPlayer
import com.epichypernova.scoretracker.data.model.LorcanaGame
import com.epichypernova.scoretracker.data.model.LorcanaPlayer
import com.epichypernova.scoretracker.data.model.OnePieceGame
import com.epichypernova.scoretracker.data.model.OnePiecePlayer
import com.epichypernova.scoretracker.data.model.PokemonGame
import com.epichypernova.scoretracker.data.model.PokemonPlayer
import com.epichypernova.scoretracker.data.model.Round
import com.epichypernova.scoretracker.data.model.SavedConfig
import com.epichypernova.scoretracker.data.model.Settings
import com.epichypernova.scoretracker.data.model.TrucoEvent
import com.epichypernova.scoretracker.data.model.TrucoMatch
import com.epichypernova.scoretracker.data.model.TrucoSide
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.data.model.YuGiOhGame
import com.epichypernova.scoretracker.data.model.YuGiOhPlayer
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

    /** Adds a pre-built user (used when the caller needs to know the id, e.g. to auto-select it). */
    fun addExistingUser(s: AppState, user: User): AppState = s.copy(users = s.users + user)

    fun updateUser(s: AppState, updated: User): AppState =
        s.copy(users = s.users.map { if (it.id == updated.id) updated else it })

    /** Deletes a user but keeps their finished games in history. */
    fun deleteUser(s: AppState, id: String): AppState =
        s.copy(users = s.users.filterNot { it.id == id })

    /** How many rewarded ads an avatar costs to unlock. Avatar 30 is the premium one (5). */
    fun adsRequiredForAvatar(id: Int): Int = if (id == 30) 5 else 1

    /**
     * Registers one watched rewarded ad toward unlocking [id]. Unlocks once the required
     * number of ads has been reached; otherwise advances (and persists) the progress.
     */
    fun watchAvatarAd(s: AppState, id: Int): AppState {
        if (id in s.unlockedAvatars) return s
        val progress = (s.avatarAdProgress[id] ?: 0) + 1
        return if (progress >= adsRequiredForAvatar(id)) {
            s.copy(unlockedAvatars = s.unlockedAvatars + id, avatarAdProgress = s.avatarAdProgress - id)
        } else {
            s.copy(avatarAdProgress = s.avatarAdProgress + (id to progress))
        }
    }

    /** Whether an interstitial is due (every 7 finished games, before starting a new one). */
    fun adDue(s: AppState): Boolean = s.history.size - s.adBaseline >= 7

    fun markAdShown(s: AppState): AppState = s.copy(adBaseline = s.history.size)

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

    /** Toggles whether a game type is one of the user's favorites (shown at the top of the menu). */
    fun toggleFavoriteGame(s: AppState, gameType: GameType): AppState =
        s.copy(favoriteGames = if (gameType in s.favoriteGames) s.favoriteGames - gameType else s.favoriteGames + gameType)

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

    /** Picks the target for the next partido, preserving the partidos marker. */
    fun trucoChooseTarget(s: AppState, target: Int): AppState {
        val m = s.trucoMatch ?: return trucoStart(s, target)
        return s.copy(trucoMatch = m.copy(target = target, chooseTarget = false, us = m.us.copy(points = 0), them = m.them.copy(points = 0), history = emptyList()))
    }

    /** Ends the whole truco session, records it and shows the winner. */
    fun trucoFinish(s: AppState): AppState {
        val m = s.trucoMatch ?: return s
        val usName = "Nosotros"; val themName = "Ellos"
        val winners = when {
            m.us.gamesWon > m.them.gamesWon -> listOf(usName)
            m.them.gamesWon > m.us.gamesWon -> listOf(themName)
            else -> listOf(usName, themName)
        }
        val lines = listOf(
            ResultLine(usName, m.us.gamesWon.toString(), 0xFF2FD3F0, usName in winners),
            ResultLine(themName, m.them.gamesWon.toString(), 0xFFE24BD6, themName in winners),
        )
        val summary = "$usName ${m.us.gamesWon} - ${m.them.gamesWon} $themName"
        val entry = HistoryEntry(newId("h"), GameType.TRUCO, emptyList(), listOf(usName, themName), winners, summary, System.currentTimeMillis())
        return s.copy(trucoMatch = null, history = listOf(entry) + s.history, pendingResult = GameResult(GameType.TRUCO, "Truco", winners, lines, summary))
    }

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
            ns = ns.copy(
                trucoMatch = ns.trucoMatch?.copy(chooseTarget = true),
                history = listOf(entry) + ns.history,
                pendingResult = GameResult(GameType.TRUCO, "Truco", listOf(wName), lines, summary),
            )
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

    /** Resets porotos and re-asks the target, keeping the partidos marker. */
    fun trucoNewMatch(s: AppState): AppState {
        val m = s.trucoMatch ?: return s
        return s.copy(trucoMatch = m.copy(us = m.us.copy(points = 0), them = m.them.copy(points = 0), chooseTarget = true, history = emptyList()))
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

    /** Ends the magic game now: winner = most life (alive preferred); records and shows result. */
    fun magicFinish(s: AppState): AppState {
        val g = s.magicGame ?: return s
        val pool = g.players.filter { !it.eliminated }.ifEmpty { g.players }
        val best = pool.maxByOrNull { it.life }
        val winners = pool.filter { it.life == best?.life }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it in winners) }
        val label = if (g.commander) "Commander" else "Magic ${g.players.size}p"
        val summary = "$label · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.MAGIC, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            magicGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.MAGIC, "Magic", wNames, lines, summary),
        )
    }

    fun magicReset(s: AppState): AppState {
        val g = s.magicGame ?: return s
        return s.copy(magicGame = g.copy(finished = false, players = g.players.map {
            it.copy(life = g.startingLife, poison = 0, energy = 0, experience = 0, eliminated = false)
        }))
    }

    // ---------- Yu-Gi-Oh ----------

    private val yugiohPalette = listOf(0xFFFF6FA8, 0xFF3B7BF7, 0xFF55E6A5, 0xFFA18AF5)
    private val yugiohNames = listOf("Duelista 1", "Duelista 2", "Duelista 3", "Duelista 4")

    fun yugiohStart(s: AppState, count: Int, startingLife: Int = 8000): AppState {
        val life = startingLife.coerceAtLeast(1)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i -> YuGiOhPlayer(name = yugiohNames[i], color = yugiohPalette[i], life = life) }
        return s.copy(yugiohGame = YuGiOhGame(players = players, startingLife = life))
    }

    fun yugiohEnsureExists(s: AppState): AppState =
        if (s.yugiohGame != null) s else yugiohStart(s, count = 2)

    /** Reconfigure player count, preserving existing players (name/color/life) where possible. */
    fun yugiohConfigure(s: AppState, count: Int): AppState {
        val g = s.yugiohGame ?: return yugiohStart(s, count)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i ->
            val prev = g.players.getOrNull(i)
            YuGiOhPlayer(
                name = prev?.name ?: yugiohNames[i],
                color = prev?.color ?: yugiohPalette[i],
                life = prev?.life ?: g.startingLife,
                eliminated = prev?.eliminated ?: false,
            )
        }
        return s.copy(yugiohGame = g.copy(players = players))
    }

    /** Sets a new starting life and resets every duelist to it. */
    fun yugiohSetLife(s: AppState, startingLife: Int): AppState {
        val g = s.yugiohGame ?: return s
        val life = startingLife.coerceAtLeast(1)
        return s.copy(yugiohGame = g.copy(startingLife = life, finished = false, players = g.players.map {
            it.copy(life = life, eliminated = false)
        }))
    }

    fun yugiohSetStep(s: AppState, step: Int): AppState {
        val g = s.yugiohGame ?: return s
        return s.copy(yugiohGame = g.copy(step = step.coerceAtLeast(1)))
    }

    fun yugiohSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.yugiohGame ?: return s
        return s.copy(yugiohGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    private fun YuGiOhGame.mapPlayer(index: Int, f: (YuGiOhPlayer) -> YuGiOhPlayer): YuGiOhGame =
        copy(players = players.mapIndexed { i, p -> if (i == index) recomputeYugiohEliminated(f(p)) else p })

    private fun recomputeYugiohEliminated(p: YuGiOhPlayer): YuGiOhPlayer =
        p.copy(eliminated = p.life <= 0)

    /** Applies a life delta (LP never goes below 0). */
    fun yugiohLife(s: AppState, index: Int, delta: Int): AppState {
        val g = s.yugiohGame ?: return s
        return checkYugiohEnd(s.copy(yugiohGame = g.mapPlayer(index) { it.copy(life = (it.life + delta).coerceAtLeast(0)) }))
    }

    /** Sets a duelist's LP to an exact value (from the calculator). */
    fun yugiohSetExact(s: AppState, index: Int, value: Int): AppState {
        val g = s.yugiohGame ?: return s
        return checkYugiohEnd(s.copy(yugiohGame = g.mapPlayer(index) { it.copy(life = value.coerceAtLeast(0)) }))
    }

    /** When only one duelist is left standing, mark finished, record history and show the winner. */
    private fun checkYugiohEnd(s: AppState): AppState {
        val g = s.yugiohGame ?: return s
        if (g.finished || g.players.size <= 1) return s
        val alive = g.players.filter { !it.eliminated }
        if (alive.size > 1 || g.players.none { it.eliminated }) return s
        val winner = alive.firstOrNull()
        val wName = winner?.name ?: "—"
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it == winner) }
        val summary = "Yu-Gi-Oh! ${g.players.size}p · $wName"
        val entry = HistoryEntry(newId("h"), GameType.YUGIOH, emptyList(), g.players.map { it.name }, listOfNotNull(winner?.name), summary, System.currentTimeMillis())
        return s.copy(
            yugiohGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.YUGIOH, "Yu-Gi-Oh!", listOf(wName), lines, summary),
        )
    }

    /** Ends the duel now: winner = most LP (alive preferred); records and shows result. */
    fun yugiohFinish(s: AppState): AppState {
        val g = s.yugiohGame ?: return s
        val pool = g.players.filter { !it.eliminated }.ifEmpty { g.players }
        val best = pool.maxByOrNull { it.life }
        val winners = pool.filter { it.life == best?.life }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it in winners) }
        val summary = "Yu-Gi-Oh! ${g.players.size}p · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.YUGIOH, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            yugiohGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.YUGIOH, "Yu-Gi-Oh!", wNames, lines, summary),
        )
    }

    fun yugiohReset(s: AppState): AppState {
        val g = s.yugiohGame ?: return s
        return s.copy(yugiohGame = g.copy(finished = false, players = g.players.map {
            it.copy(life = g.startingLife, eliminated = false)
        }))
    }

    // ---------- Pokémon TCG ----------

    private val pokemonPalette = listOf(0xFF55E6A5, 0xFFF27BA9, 0xFF4AA3FF, 0xFFF2B33B)
    private val pokemonNames = listOf("Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4")

    fun pokemonStart(s: AppState, count: Int, startingPrizes: Int = 6): AppState {
        val prizes = startingPrizes.coerceIn(1, 6)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i -> PokemonPlayer(name = pokemonNames[i], color = pokemonPalette[i], prizes = prizes) }
        return s.copy(pokemonGame = PokemonGame(players = players, startingPrizes = prizes))
    }

    fun pokemonEnsureExists(s: AppState): AppState =
        if (s.pokemonGame != null) s else pokemonStart(s, count = 2)

    /** Reconfigure player count + starting prizes; resets the game to fresh prizes/damage. */
    fun pokemonConfigure(s: AppState, count: Int, startingPrizes: Int): AppState {
        val g = s.pokemonGame ?: return pokemonStart(s, count, startingPrizes)
        val prizes = startingPrizes.coerceIn(1, 6)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i ->
            val prev = g.players.getOrNull(i)
            PokemonPlayer(
                name = prev?.name ?: pokemonNames[i],
                color = prev?.color ?: pokemonPalette[i],
                prizes = prizes,
            )
        }
        return s.copy(pokemonGame = g.copy(players = players, startingPrizes = prizes, finished = false))
    }

    fun pokemonSetStep(s: AppState, step: Int): AppState {
        val g = s.pokemonGame ?: return s
        return s.copy(pokemonGame = g.copy(damageStep = step.coerceAtLeast(1)))
    }

    fun pokemonSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.pokemonGame ?: return s
        return s.copy(pokemonGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    private fun PokemonGame.mapPlayer(index: Int, f: (PokemonPlayer) -> PokemonPlayer): PokemonGame =
        copy(players = players.mapIndexed { i, p -> if (i == index) f(p) else p })

    /** Take/return a prize card (clamped 0..startingPrizes). Reaching 0 wins the game. */
    fun pokemonPrize(s: AppState, index: Int, delta: Int): AppState {
        val g = s.pokemonGame ?: return s
        val updated = g.mapPlayer(index) {
            val next = (it.prizes + delta).coerceIn(0, g.startingPrizes)
            it.copy(prizes = next, won = next <= 0)
        }
        return checkPokemonEnd(s.copy(pokemonGame = updated))
    }

    /** Adjust damage counters on the active Pokémon (never below 0). */
    fun pokemonDamage(s: AppState, index: Int, delta: Int): AppState {
        val g = s.pokemonGame ?: return s
        return s.copy(pokemonGame = g.mapPlayer(index) { it.copy(damage = (it.damage + delta).coerceAtLeast(0)) })
    }

    fun pokemonResetDamage(s: AppState, index: Int): AppState {
        val g = s.pokemonGame ?: return s
        return s.copy(pokemonGame = g.mapPlayer(index) { it.copy(damage = 0) })
    }

    /** When a player has taken all their prizes, mark finished, record history and show the winner. */
    private fun checkPokemonEnd(s: AppState): AppState {
        val g = s.pokemonGame ?: return s
        if (g.finished) return s
        val winners = g.players.filter { it.prizes <= 0 }
        if (winners.isEmpty()) return s
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, "${g.startingPrizes - it.prizes}/${g.startingPrizes}", it.color, it in winners) }
        val summary = "Pokémon TCG · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.POKEMON, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            pokemonGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.POKEMON, "Pokémon TCG", wNames, lines, summary),
        )
    }

    /** Ends the game now: winner = fewest prizes remaining (closest to victory). */
    fun pokemonFinish(s: AppState): AppState {
        val g = s.pokemonGame ?: return s
        val fewest = g.players.minByOrNull { it.prizes }?.prizes
        val winners = g.players.filter { it.prizes == fewest }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, "${g.startingPrizes - it.prizes}/${g.startingPrizes}", it.color, it in winners) }
        val summary = "Pokémon TCG · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.POKEMON, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            pokemonGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.POKEMON, "Pokémon TCG", wNames, lines, summary),
        )
    }

    fun pokemonReset(s: AppState): AppState {
        val g = s.pokemonGame ?: return s
        return s.copy(pokemonGame = g.copy(finished = false, players = g.players.map {
            it.copy(prizes = g.startingPrizes, damage = 0, won = false)
        }))
    }

    // ---------- Digimon TCG ----------

    private val digimonPalette = listOf(0xFF3B7BF7, 0xFFF2B33B)
    private val digimonNames = listOf("Tamer 1", "Tamer 2")

    fun digimonStart(s: AppState, startingSecurity: Int = 5): AppState {
        val sec = startingSecurity.coerceIn(1, 10)
        val players = (0 until 2).map { i -> DigimonPlayer(name = digimonNames[i], color = digimonPalette[i], security = sec) }
        return s.copy(digimonGame = DigimonGame(players = players, startingSecurity = sec))
    }

    fun digimonEnsureExists(s: AppState): AppState =
        if (s.digimonGame != null) s else digimonStart(s)

    fun digimonSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.digimonGame ?: return s
        return s.copy(digimonGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    /** Sets a new starting security and resets both tamers to it. */
    fun digimonSetSecurity(s: AppState, startingSecurity: Int): AppState {
        val g = s.digimonGame ?: return s
        val sec = startingSecurity.coerceIn(1, 10)
        return s.copy(digimonGame = g.copy(startingSecurity = sec, finished = false, memory = 0, players = g.players.map {
            it.copy(security = sec, defeated = false)
        }))
    }

    /**
     * Adjusts a tamer's security stack. Taking a hit (negative delta) while already at 0
     * is the finishing blow → that tamer is defeated.
     */
    fun digimonSecurity(s: AppState, index: Int, delta: Int): AppState {
        val g = s.digimonGame ?: return s
        val updated = g.copy(players = g.players.mapIndexed { i, p ->
            if (i != index) p else {
                val next = p.security + delta
                if (next < 0) p.copy(security = 0, defeated = true)
                else p.copy(security = next.coerceAtMost(10))
            }
        })
        return checkDigimonEnd(s.copy(digimonGame = updated))
    }

    /** Moves the shared memory gauge (clamped to -10..+10). */
    fun digimonMemory(s: AppState, delta: Int): AppState {
        val g = s.digimonGame ?: return s
        return s.copy(digimonGame = g.copy(memory = (g.memory + delta).coerceIn(-10, 10)))
    }

    fun digimonSetMemory(s: AppState, value: Int): AppState {
        val g = s.digimonGame ?: return s
        return s.copy(digimonGame = g.copy(memory = value.coerceIn(-10, 10)))
    }

    private fun checkDigimonEnd(s: AppState): AppState {
        val g = s.digimonGame ?: return s
        if (g.finished) return s
        val defeated = g.players.filter { it.defeated }
        if (defeated.isEmpty()) return s
        val winner = g.players.firstOrNull { !it.defeated }
        val wName = winner?.name ?: "—"
        val lines = g.players.map { ResultLine(it.name, it.security.toString(), it.color, it == winner) }
        val summary = "Digimon · $wName"
        val entry = HistoryEntry(newId("h"), GameType.DIGIMON, emptyList(), g.players.map { it.name }, listOfNotNull(winner?.name), summary, System.currentTimeMillis())
        return s.copy(
            digimonGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.DIGIMON, "Digimon", listOf(wName), lines, summary),
        )
    }

    /** Ends the duel now: winner = most security remaining. */
    fun digimonFinish(s: AppState): AppState {
        val g = s.digimonGame ?: return s
        val best = g.players.maxByOrNull { it.security }?.security
        val winners = g.players.filter { it.security == best }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.security.toString(), it.color, it in winners) }
        val summary = "Digimon · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.DIGIMON, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            digimonGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.DIGIMON, "Digimon", wNames, lines, summary),
        )
    }

    fun digimonReset(s: AppState): AppState {
        val g = s.digimonGame ?: return s
        return s.copy(digimonGame = g.copy(finished = false, memory = 0, players = g.players.map {
            it.copy(security = g.startingSecurity, defeated = false)
        }))
    }

    // ---------- Disney Lorcana ----------

    private val lorcanaPalette = listOf(0xFFF2B33B, 0xFF4AA3FF, 0xFF55E6A5, 0xFFA18AF5)
    private val lorcanaNames = listOf("Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4")

    fun lorcanaStart(s: AppState, count: Int, targetLore: Int = 20): AppState {
        val target = targetLore.coerceIn(1, 99)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i -> LorcanaPlayer(name = lorcanaNames[i], color = lorcanaPalette[i]) }
        return s.copy(lorcanaGame = LorcanaGame(players = players, targetLore = target))
    }

    fun lorcanaEnsureExists(s: AppState): AppState =
        if (s.lorcanaGame != null) s else lorcanaStart(s, count = 2)

    fun lorcanaConfigure(s: AppState, count: Int, targetLore: Int): AppState {
        val g = s.lorcanaGame ?: return lorcanaStart(s, count, targetLore)
        val target = targetLore.coerceIn(1, 99)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i ->
            val prev = g.players.getOrNull(i)
            LorcanaPlayer(name = prev?.name ?: lorcanaNames[i], color = prev?.color ?: lorcanaPalette[i])
        }
        return s.copy(lorcanaGame = g.copy(players = players, targetLore = target, finished = false))
    }

    fun lorcanaSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.lorcanaGame ?: return s
        return s.copy(lorcanaGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    /** Adjusts a player's lore (0..target). Reaching the target wins the game. */
    fun lorcanaLore(s: AppState, index: Int, delta: Int): AppState {
        val g = s.lorcanaGame ?: return s
        val updated = g.copy(players = g.players.mapIndexed { i, p ->
            if (i != index) p else {
                val next = (p.lore + delta).coerceIn(0, g.targetLore)
                p.copy(lore = next, won = next >= g.targetLore)
            }
        })
        return checkLorcanaEnd(s.copy(lorcanaGame = updated))
    }

    private fun checkLorcanaEnd(s: AppState): AppState {
        val g = s.lorcanaGame ?: return s
        if (g.finished) return s
        val winners = g.players.filter { it.won }
        if (winners.isEmpty()) return s
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, "${it.lore}/${g.targetLore}", it.color, it in winners) }
        val summary = "Lorcana · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.LORCANA, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            lorcanaGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.LORCANA, "Lorcana", wNames, lines, summary),
        )
    }

    /** Ends now: winner = most lore. */
    fun lorcanaFinish(s: AppState): AppState {
        val g = s.lorcanaGame ?: return s
        val best = g.players.maxByOrNull { it.lore }?.lore
        val winners = g.players.filter { it.lore == best }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, "${it.lore}/${g.targetLore}", it.color, it in winners) }
        val summary = "Lorcana · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.LORCANA, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            lorcanaGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.LORCANA, "Lorcana", wNames, lines, summary),
        )
    }

    fun lorcanaReset(s: AppState): AppState {
        val g = s.lorcanaGame ?: return s
        return s.copy(lorcanaGame = g.copy(finished = false, players = g.players.map { it.copy(lore = 0, won = false) }))
    }

    // ---------- One Piece Card Game ----------

    private val onePiecePalette = listOf(0xFFE0492F, 0xFF4AA3FF)
    private val onePieceNames = listOf("Jugador 1", "Jugador 2")

    fun onePieceStart(s: AppState, startingLife: Int = 5): AppState {
        val life = startingLife.coerceIn(1, 10)
        val players = (0 until 2).map { i -> OnePiecePlayer(name = onePieceNames[i], color = onePiecePalette[i], life = life) }
        return s.copy(onePieceGame = OnePieceGame(players = players, startingLife = life))
    }

    fun onePieceEnsureExists(s: AppState): AppState =
        if (s.onePieceGame != null) s else onePieceStart(s)

    fun onePieceSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.onePieceGame ?: return s
        return s.copy(onePieceGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    /** Sets a new starting life and resets both players (life + DON). */
    fun onePieceSetLife(s: AppState, startingLife: Int): AppState {
        val g = s.onePieceGame ?: return s
        val life = startingLife.coerceIn(1, 10)
        return s.copy(onePieceGame = g.copy(startingLife = life, finished = false, players = g.players.map {
            it.copy(life = life, don = 0, defeated = false)
        }))
    }

    /** Adjusts life cards. Taking a hit (negative) while at 0 = defeat. */
    fun onePieceLife(s: AppState, index: Int, delta: Int): AppState {
        val g = s.onePieceGame ?: return s
        val updated = g.copy(players = g.players.mapIndexed { i, p ->
            if (i != index) p else {
                val next = p.life + delta
                if (next < 0) p.copy(life = 0, defeated = true)
                else p.copy(life = next.coerceAtMost(10))
            }
        })
        return checkOnePieceEnd(s.copy(onePieceGame = updated))
    }

    /** Adjusts active DON!! (0..10). */
    fun onePieceDon(s: AppState, index: Int, delta: Int): AppState {
        val g = s.onePieceGame ?: return s
        return s.copy(onePieceGame = g.copy(players = g.players.mapIndexed { i, p ->
            if (i == index) p.copy(don = (p.don + delta).coerceIn(0, 10)) else p
        }))
    }

    private fun checkOnePieceEnd(s: AppState): AppState {
        val g = s.onePieceGame ?: return s
        if (g.finished) return s
        val defeated = g.players.filter { it.defeated }
        if (defeated.isEmpty()) return s
        val winner = g.players.firstOrNull { !it.defeated }
        val wName = winner?.name ?: "—"
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it == winner) }
        val summary = "One Piece · $wName"
        val entry = HistoryEntry(newId("h"), GameType.ONEPIECE, emptyList(), g.players.map { it.name }, listOfNotNull(winner?.name), summary, System.currentTimeMillis())
        return s.copy(
            onePieceGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.ONEPIECE, "One Piece", listOf(wName), lines, summary),
        )
    }

    /** Ends now: winner = most life remaining. */
    fun onePieceFinish(s: AppState): AppState {
        val g = s.onePieceGame ?: return s
        val best = g.players.maxByOrNull { it.life }?.life
        val winners = g.players.filter { it.life == best }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.life.toString(), it.color, it in winners) }
        val summary = "One Piece · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.ONEPIECE, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(
            onePieceGame = g.copy(finished = true),
            history = listOf(entry) + s.history,
            pendingResult = GameResult(GameType.ONEPIECE, "One Piece", wNames, lines, summary),
        )
    }

    fun onePieceReset(s: AppState): AppState {
        val g = s.onePieceGame ?: return s
        return s.copy(onePieceGame = g.copy(finished = false, players = g.players.map {
            it.copy(life = g.startingLife, don = 0, defeated = false)
        }))
    }

    // ---------- Chinchón ----------

    private val chinchonPalette = listOf(0xFF4FB35B, 0xFF2FD3F0, 0xFFF2B33B, 0xFFA18AF5)
    private val chinchonNames = listOf("Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4")

    fun chinchonStart(s: AppState, count: Int, target: Int = 100): AppState {
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i -> ChinchonPlayer(name = chinchonNames[i], color = chinchonPalette[i]) }
        return s.copy(chinchonGame = ChinchonGame(players = players, target = target.coerceAtLeast(1)))
    }

    fun chinchonEnsureExists(s: AppState): AppState =
        if (s.chinchonGame != null) s else chinchonStart(s, count = 2)

    fun chinchonConfigure(s: AppState, count: Int, target: Int): AppState {
        val g = s.chinchonGame ?: return chinchonStart(s, count, target)
        val n = count.coerceIn(2, 4)
        val players = (0 until n).map { i ->
            val prev = g.players.getOrNull(i)
            ChinchonPlayer(name = prev?.name ?: chinchonNames[i], color = prev?.color ?: chinchonPalette[i])
        }
        return s.copy(chinchonGame = g.copy(players = players, target = target.coerceAtLeast(1), finished = false))
    }

    fun chinchonSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.chinchonGame ?: return s
        return s.copy(chinchonGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    /** Adds this round's points to a player (never below 0). Crossing the target ends the game. */
    fun chinchonAdd(s: AppState, index: Int, delta: Int): AppState {
        val g = s.chinchonGame ?: return s
        val updated = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(score = (p.score + delta).coerceAtLeast(0)) else p })
        return checkChinchonEnd(s.copy(chinchonGame = updated))
    }

    private fun checkChinchonEnd(s: AppState): AppState {
        val g = s.chinchonGame ?: return s
        if (g.finished) return s
        if (g.players.none { it.score >= g.target }) return s
        val best = g.players.minByOrNull { it.score }
        val winners = g.players.filter { it.score == best?.score }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.score.toString(), it.color, it in winners) }
        val summary = "Chinchón · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.CHINCHON, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(chinchonGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.CHINCHON, "Chinchón", wNames, lines, summary))
    }

    /** Instant win for a player who closed with chinchón. */
    fun chinchonInstantWin(s: AppState, index: Int): AppState {
        val g = s.chinchonGame ?: return s
        val winner = g.players.getOrNull(index) ?: return s
        val lines = g.players.map { ResultLine(it.name, it.score.toString(), it.color, it == winner) }
        val summary = "Chinchón · ${winner.name}"
        val entry = HistoryEntry(newId("h"), GameType.CHINCHON, emptyList(), g.players.map { it.name }, listOf(winner.name), summary, System.currentTimeMillis())
        return s.copy(chinchonGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.CHINCHON, "Chinchón", listOf(winner.name), lines, summary))
    }

    fun chinchonFinish(s: AppState): AppState {
        val g = s.chinchonGame ?: return s
        val best = g.players.minByOrNull { it.score }
        val winners = g.players.filter { it.score == best?.score }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.score.toString(), it.color, it in winners) }
        val summary = "Chinchón · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.CHINCHON, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(chinchonGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.CHINCHON, "Chinchón", wNames, lines, summary))
    }

    fun chinchonReset(s: AppState): AppState {
        val g = s.chinchonGame ?: return s
        return s.copy(chinchonGame = g.copy(finished = false, players = g.players.map { it.copy(score = 0) }))
    }

    // ---------- Burako ----------

    fun burakoStart(s: AppState, target: Int = 2000): AppState {
        val teams = listOf(
            BurakoTeam("Nosotros", 0xFFF27BA9, 0),
            BurakoTeam("Ellos", 0xFF4AA3FF, 0),
        )
        return s.copy(burakoGame = BurakoGame(teams = teams, target = target.coerceAtLeast(1)))
    }

    fun burakoEnsureExists(s: AppState): AppState =
        if (s.burakoGame != null) s else burakoStart(s)

    fun burakoSetTarget(s: AppState, target: Int): AppState {
        val g = s.burakoGame ?: return s
        return s.copy(burakoGame = g.copy(target = target.coerceAtLeast(1)))
    }

    /** Adds this round's points to a team (never below 0). Reaching the target wins. */
    fun burakoAdd(s: AppState, index: Int, delta: Int): AppState {
        val g = s.burakoGame ?: return s
        val updated = g.copy(teams = g.teams.mapIndexed { i, t -> if (i == index) t.copy(score = (t.score + delta).coerceAtLeast(0)) else t })
        return checkBurakoEnd(s.copy(burakoGame = updated))
    }

    private fun checkBurakoEnd(s: AppState): AppState {
        val g = s.burakoGame ?: return s
        if (g.finished) return s
        if (g.teams.none { it.score >= g.target }) return s
        val best = g.teams.maxByOrNull { it.score }
        val winners = g.teams.filter { it.score == best?.score }
        val wNames = winners.map { it.name }
        val lines = g.teams.map { ResultLine(it.name, it.score.toString(), it.color, it in winners) }
        val summary = "Burako · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.BURAKO, emptyList(), g.teams.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(burakoGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.BURAKO, "Burako", wNames, lines, summary))
    }

    fun burakoFinish(s: AppState): AppState {
        val g = s.burakoGame ?: return s
        val best = g.teams.maxByOrNull { it.score }
        val winners = g.teams.filter { it.score == best?.score }
        val wNames = winners.map { it.name }
        val lines = g.teams.map { ResultLine(it.name, it.score.toString(), it.color, it in winners) }
        val summary = "Burako · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.BURAKO, emptyList(), g.teams.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(burakoGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.BURAKO, "Burako", wNames, lines, summary))
    }

    fun burakoReset(s: AppState): AppState {
        val g = s.burakoGame ?: return s
        return s.copy(burakoGame = g.copy(finished = false, teams = g.teams.map { it.copy(score = 0) }))
    }

    // ---------- Darts 501 ----------

    private val dartsPalette = listOf(0xFFEB5757, 0xFF4AA3FF)
    private val dartsNames = listOf("Jugador 1", "Jugador 2")

    fun dartsStart(s: AppState, startScore: Int = 501): AppState {
        val start = startScore.coerceAtLeast(2)
        val players = (0 until 2).map { i -> DartsPlayer(name = dartsNames[i], color = dartsPalette[i], remaining = start) }
        return s.copy(dartsGame = DartsGame(players = players, startScore = start))
    }

    fun dartsEnsureExists(s: AppState): AppState =
        if (s.dartsGame != null) s else dartsStart(s)

    fun dartsSetStart(s: AppState, startScore: Int): AppState {
        val g = s.dartsGame ?: return s
        val start = startScore.coerceAtLeast(2)
        return s.copy(dartsGame = g.copy(startScore = start, finished = false, players = g.players.map { it.copy(remaining = start) }))
    }

    fun dartsSetColor(s: AppState, index: Int, color: Long): AppState {
        val g = s.dartsGame ?: return s
        return s.copy(dartsGame = g.copy(players = g.players.mapIndexed { i, p -> if (i == index) p.copy(color = color) else p }))
    }

    /**
     * Registers a turn score (0..180). Standard bust rules: if it would leave a negative
     * remainder or exactly 1, the turn is a bust and nothing changes. Reaching exactly 0 wins.
     */
    fun dartsThrow(s: AppState, index: Int, turn: Int): AppState {
        val g = s.dartsGame ?: return s
        val p = g.players.getOrNull(index) ?: return s
        val next = p.remaining - turn.coerceIn(0, 180)
        if (next < 0 || next == 1) return s   // bust: no change
        val updated = g.copy(players = g.players.mapIndexed { i, pl -> if (i == index) pl.copy(remaining = next) else pl })
        return if (next == 0) {
            val winner = updated.players[index]
            val lines = updated.players.map { ResultLine(it.name, it.remaining.toString(), it.color, it == winner) }
            val summary = "Dardos ${g.startScore} · ${winner.name}"
            val entry = HistoryEntry(newId("h"), GameType.DARTS, emptyList(), updated.players.map { it.name }, listOf(winner.name), summary, System.currentTimeMillis())
            s.copy(dartsGame = updated.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.DARTS, "Dardos", listOf(winner.name), lines, summary))
        } else {
            s.copy(dartsGame = updated)
        }
    }

    fun dartsFinish(s: AppState): AppState {
        val g = s.dartsGame ?: return s
        // closest to zero (lowest remaining) wins
        val best = g.players.minByOrNull { it.remaining }
        val winners = g.players.filter { it.remaining == best?.remaining }
        val wNames = winners.map { it.name }
        val lines = g.players.map { ResultLine(it.name, it.remaining.toString(), it.color, it in winners) }
        val summary = "Dardos ${g.startScore} · " + wNames.joinToString("/")
        val entry = HistoryEntry(newId("h"), GameType.DARTS, emptyList(), g.players.map { it.name }, wNames, summary, System.currentTimeMillis())
        return s.copy(dartsGame = g.copy(finished = true), history = listOf(entry) + s.history, pendingResult = GameResult(GameType.DARTS, "Dardos", wNames, lines, summary))
    }

    fun dartsReset(s: AppState): AppState {
        val g = s.dartsGame ?: return s
        return s.copy(dartsGame = g.copy(finished = false, players = g.players.map { it.copy(remaining = g.startScore) }))
    }

    // ---------- Settings ----------

    fun setLanguage(s: AppState, language: String): AppState =
        s.copy(settings = s.settings.copy(language = language))

    fun setSoundVolume(s: AppState, volume: Float): AppState =
        s.copy(settings = s.settings.copy(soundVolume = volume.coerceIn(0f, 1f)))
}
