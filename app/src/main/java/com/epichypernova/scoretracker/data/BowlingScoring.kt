package com.epichypernova.scoretracker.data

/** One of the ten frames as derived from a player's roll list. [score] is the running total, null while pending bonus rolls. */
data class BowlingFrame(val rolls: List<Int>, val score: Int?, val complete: Boolean)

object BowlingScoring {
    /** Splits a flat roll list into exactly 10 frames, applying strike/spare bonuses. */
    fun frames(rolls: List<Int>): List<BowlingFrame> {
        val out = ArrayList<BowlingFrame>(10)
        var i = 0
        var total = 0
        for (f in 1..10) {
            if (i >= rolls.size) { out += BowlingFrame(emptyList(), null, false); continue }
            val a = rolls[i]
            if (f == 10) {
                val fr = rolls.subList(i, minOf(rolls.size, i + 3))
                val complete = fr.size == 3 || (fr.size == 2 && fr[0] + fr[1] < 10)
                val score = if (complete) total + fr.sum() else null
                if (score != null) total = score
                out += BowlingFrame(fr, score, complete)
                i += fr.size
            } else if (a == 10) {
                val b1 = rolls.getOrNull(i + 1); val b2 = rolls.getOrNull(i + 2)
                val score = if (b1 != null && b2 != null) total + 10 + b1 + b2 else null
                if (score != null) total = score
                out += BowlingFrame(listOf(10), score, true)
                i += 1
            } else if (i + 1 < rolls.size) {
                val b = rolls[i + 1]
                val score = if (a + b == 10) rolls.getOrNull(i + 2)?.let { total + 10 + it } else total + a + b
                if (score != null) total = score
                out += BowlingFrame(listOf(a, b), score, true)
                i += 2
            } else {
                out += BowlingFrame(listOf(a), null, false)
                i += 1
            }
        }
        return out
    }

    fun isComplete(rolls: List<Int>): Boolean = frames(rolls).all { it.complete }

    fun total(rolls: List<Int>): Int = frames(rolls).lastOrNull { it.score != null }?.score ?: 0

    /** Index (0-based) of the frame the next roll belongs to, or 10 when the game is over. */
    fun currentFrame(rolls: List<Int>): Int = frames(rolls).indexOfFirst { !it.complete }.let { if (it < 0) 10 else it }

    /** Pins still standing for the next roll (the max the player can knock down). */
    fun pinsStanding(rolls: List<Int>): Int {
        val fs = frames(rolls)
        val idx = currentFrame(rolls)
        if (idx >= 10) return 0
        val fr = fs[idx].rolls
        if (idx < 9) return if (fr.isEmpty()) 10 else 10 - fr[0]
        // 10th frame: pins reset after a strike or a spare
        return when (fr.size) {
            0 -> 10
            1 -> if (fr[0] == 10) 10 else 10 - fr[0]
            else -> if (fr[0] == 10 && fr[1] != 10) 10 - fr[1] else 10
        }
    }
}
