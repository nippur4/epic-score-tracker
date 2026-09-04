package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.CurrentGame

/**
 * Pure derivations over a [CurrentGame]. Mirrors the logic in the design prototype's
 * Component class (totals, leader, bar percentage, finished state).
 */
object Derivations {

    /** Total accumulated points per playerId. */
    fun totals(game: CurrentGame): Map<String, Int> {
        val acc = game.playerIds.associateWith { 0 }.toMutableMap()
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

    /** Whether the game has reached an end condition. */
    fun isFinished(game: CurrentGame): Boolean {
        val fixed = game.rules.fixedHands
        if (fixed != null && game.rounds.size >= fixed) return true
        val t = totals(game).values
        if (t.isNotEmpty() && t.max() >= game.rules.targetScore) return true
        return false
    }
}
