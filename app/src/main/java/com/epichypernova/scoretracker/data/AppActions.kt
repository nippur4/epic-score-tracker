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

    /** Unlocks an avatar (after a rewarded ad). */
    fun unlockAvatar(s: AppState, id: Int): AppState =
        s.copy(unlockedAvatars = s.unlockedAvatars + id)

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

    // ---------- Settings ----------

    fun setLanguage(s: AppState, language: String): AppState =
        s.copy(settings = s.settings.copy(language = language))

    fun setSoundVolume(s: AppState, volume: Float): AppState =
        s.copy(settings = s.settings.copy(soundVolume = volume.coerceIn(0f, 1f)))
}
