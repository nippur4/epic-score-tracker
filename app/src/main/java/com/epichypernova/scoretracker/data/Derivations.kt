package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.CurrentGame
import com.epichypernova.scoretracker.data.model.GameType

/**
 * Pure derivations over a [CurrentGame]. Mirrors the logic in the design prototype's
 * Component class (totals, leader, bar percentage, finished state).
 */
object Derivations {

    /** Total accumulated points per playerId, starting from what each one joined with. */
    fun totals(game: CurrentGame): Map<String, Int> {
        val acc = game.playerIds.associateWith { game.startScores[it] ?: 0 }.toMutableMap()
        for (round in game.rounds) {
            for (cell in round.cells) {
                acc[cell.playerId] = (acc[cell.playerId] ?: 0) + cell.points
            }
        }
        return acc
    }

    /** The best (winning) total value given the low/high rule. */
    fun bestTotal(game: CurrentGame): Int {
        val t = totals(game).values
        if (t.isEmpty()) return 0
        return if (game.rules.lowWins) t.min() else t.max()
    }

    /** PlayerIds currently in the lead (may be several on a tie). */
    fun leaders(game: CurrentGame): List<String> {
        val t = totals(game)
        if (t.isEmpty()) return emptyList()
        val best = bestTotal(game)
        return t.filter { it.value == best }.keys.toList()
    }

    /**
     * Progress bar fill fraction for a player: (target − total) / target, clamped to [0,1].
     * In "gana el menor", a fuller bar means closer to winning.
     */
    fun barFraction(game: CurrentGame, playerId: String): Float {
        val target = game.rules.targetScore.takeIf { it > 0 } ?: return 0f
        val total = totals(game)[playerId] ?: 0
        return ((target - total).toFloat() / target).coerceIn(0f, 1f)
    }

    /** 1-based number of the hand currently being played. */
    fun currentRoundNumber(game: CurrentGame): Int = game.rounds.size + 1

    /**
     * Games with a board already going, so the menu can offer to pick them up again.
     * Truco, burako, mus and warhammer build their board the moment you open them, so for
     * those "in play" means something was actually scored.
     */
    fun gamesInPlay(s: AppState): List<GameType> = buildList {
        s.currentGame?.let { add(it.gameType) }
        if (s.trucoMatch?.let { it.us.points + it.them.points + it.us.gamesWon + it.them.gamesWon > 0 } == true) add(GameType.TRUCO)
        if (s.magicGame?.finished == false) add(GameType.MAGIC)
        if (s.yugiohGame?.finished == false) add(GameType.YUGIOH)
        if (s.pokemonGame?.finished == false) add(GameType.POKEMON)
        if (s.digimonGame?.finished == false) add(GameType.DIGIMON)
        if (s.lorcanaGame?.finished == false) add(GameType.LORCANA)
        if (s.onePieceGame?.finished == false) add(GameType.ONEPIECE)
        if (s.chinchonGame?.finished == false) add(GameType.CHINCHON)
        if (s.burakoGame?.let { !it.finished && it.teams.any { t -> t.score > 0 } } == true) add(GameType.BURAKO)
        if (s.dartsGame?.finished == false) add(GameType.DARTS)
        if (s.dndGame?.finished == false) add(GameType.DND)
        if (s.generalaGame?.finished == false) add(GameType.GENERALA)
        if (s.bowlingGame?.finished == false) add(GameType.BOWLING)
        if (s.unoGame?.finished == false) add(GameType.UNO)
        if (s.warhammerGame?.let { g -> !g.finished && (g.round > 1 || g.players.any { it.primary + it.secondary + it.cp > 0 }) } == true) add(GameType.WARHAMMER)
        if (s.pokerGame?.finished == false) add(GameType.POKER)
        if (s.swuGame?.finished == false) add(GameType.SWU)
        if (s.escobaGame?.finished == false) add(GameType.ESCOBA)
        if (s.musGame?.let { !it.finished && it.teams.any { t -> t.piedras + t.juegos + t.vacas > 0 } } == true) add(GameType.MUS)
    }

    /** Whether the game has reached an end condition. */
    fun isFinished(game: CurrentGame): Boolean {
        val fixed = game.rules.fixedHands
        if (fixed != null && game.rounds.size >= fixed) return true
        val t = totals(game).values
        if (t.isNotEmpty() && t.max() >= game.rules.targetScore) return true
        return false
    }
}
