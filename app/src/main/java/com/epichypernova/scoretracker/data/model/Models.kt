package com.epichypernova.scoretracker.data.model

import kotlinx.serialization.Serializable

/** The kinds of games the app can track. */
@Serializable
enum class GameType {
    MANOS_Y_PUNTOS,   // generic: hands and points (basas, chinchón, generala, escoba)
    CONTADOR_SIMPLE,  // generic: simple +/- counter, no hands
    TRUCO,
    MAGIC,
    POKEMON,
    YUGIOH,
    DIGIMON;

    val isGeneric: Boolean get() = this == MANOS_Y_PUNTOS || this == CONTADOR_SIMPLE
}

/** A local device player used to build generic games. */
@Serializable
data class User(
    val id: String,
    val name: String,
    val color: Long,          // ARGB as Long, e.g. 0xFF2FD3F0
    val favorite: Boolean = false,
    val gamesPlayed: Int = 0,
)

/** Rules for a generic (hands & points) game. */
@Serializable
data class GenericRules(
    val targetScore: Int = 100,
    val lowWins: Boolean = true,
    val fixedHands: Int? = null,     // null = open-ended
    val bidsEnabled: Boolean = false,
)

/** A saved reusable configuration for a generic game. */
@Serializable
data class SavedConfig(
    val id: String,
    val name: String,
    val gameType: GameType,
    val playerIds: List<String>,
    val rules: GenericRules,
)

/** One player's entry within a round. */
@Serializable
data class Cell(
    val playerId: String,
    val points: Int,
    val bid: Int? = null,
)

/** One played hand/round. */
@Serializable
data class Round(
    val index: Int,
    val cells: List<Cell>,
)

/** The generic game in progress. */
@Serializable
data class CurrentGame(
    val id: String,
    val gameType: GameType,
    val name: String? = null,          // optional display title, e.g. "Chinchón"
    val playerIds: List<String>,
    val rules: GenericRules,
    val rounds: List<Round> = emptyList(),
    val startedAt: Long,
)

// ---- Truco ----

@Serializable
data class TrucoSide(
    val points: Int = 0,
    val gamesWon: Int = 0,
)

/** A single reversible score event, for undo. */
@Serializable
data class TrucoEvent(
    val us: TrucoSide,
    val them: TrucoSide,
)

@Serializable
data class TrucoMatch(
    val us: TrucoSide = TrucoSide(),
    val them: TrucoSide = TrucoSide(),
    val history: List<TrucoEvent> = emptyList(),
)

// ---- Magic ----

@Serializable
enum class MagicMode { ONE_V_ONE, COMMANDER }

@Serializable
data class MagicPlayer(
    val name: String,
    val color: Long,
    val life: Int,
    val poison: Int = 0,
    val energy: Int = 0,
    val commanderDamage: Int = 0,
    val eliminated: Boolean = false,
)

@Serializable
data class MagicGame(
    val mode: MagicMode,
    val players: List<MagicPlayer>,
    val startingLife: Int,
)

// ---- History ----

@Serializable
data class HistoryEntry(
    val id: String,
    val gameType: GameType,
    val playerIds: List<String>,      // may reference deleted users (kept for the record)
    val playerNames: List<String>,    // snapshot of names at finish time
    val winnerNames: List<String>,
    val summary: String,
    val finishedAt: Long,
)

// ---- Settings ----

@Serializable
data class Settings(
    val language: String = "es",   // "es" | "en"
)

/** Root persisted state — serialized as one JSON blob in DataStore. */
@Serializable
data class AppState(
    val users: List<User> = emptyList(),
    val savedConfigs: List<SavedConfig> = emptyList(),
    val currentGame: CurrentGame? = null,
    val trucoMatch: TrucoMatch? = null,
    val magicGame: MagicGame? = null,
    val history: List<HistoryEntry> = emptyList(),
    val settings: Settings = Settings(),
)
