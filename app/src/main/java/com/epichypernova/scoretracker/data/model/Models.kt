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
    DIGIMON,
    LORCANA,
    ONEPIECE,
    CHINCHON,
    BURAKO,
    DARTS,
    DND,
    GENERALA,
    BOWLING,
    UNO,
    WARHAMMER,
    POKER,
    SWU,
    ESCOBA,
    MUS;

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
    val avatarId: Int? = null, // 1..30 → drawable avatar_N; null = colored initial
)

/** Rules for a generic (hands & points) game. */
@Serializable
data class GenericRules(
    val targetScore: Int = 100,
    val lowWins: Boolean = true,
    val fixedHands: Int? = null,     // null = open-ended
    val bidsEnabled: Boolean = false,
    val pointsPerHit: Int = 1,       // "basas": points earned per acierto
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

/**
 * One player's entry within a round.
 * - Simple games: [points] is typed directly.
 * - "Basas" games: [bid] is declared first, then [hits] (aciertos) and [extra] are entered;
 *   [points] = hits * rules.pointsPerHit + extra.
 * A round whose cells have [hits] == null (while bids are enabled) is "points-pending".
 */
@Serializable
data class Cell(
    val playerId: String,
    val points: Int,
    val bid: Int? = null,
    val hits: Int? = null,
    val extra: Int? = null,
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
    val target: Int = 30,              // 15 (solo malas) or 30 (malas + buenas)
    val chooseTarget: Boolean = false, // true → ask 15/30 before the next partido
    val history: List<TrucoEvent> = emptyList(),
)

// ---- Magic ----

@Serializable
data class MagicPlayer(
    val name: String,
    val color: Long,
    val life: Int,
    val poison: Int = 0,
    val energy: Int = 0,
    val experience: Int = 0,          // commander only
    val eliminated: Boolean = false,
)

@Serializable
data class MagicGame(
    val commander: Boolean,
    val players: List<MagicPlayer>,
    val startingLife: Int,
    val finished: Boolean = false,
)

// ---- Yu-Gi-Oh ----

@Serializable
data class YuGiOhPlayer(
    val name: String,
    val color: Long,
    val life: Int,
    val eliminated: Boolean = false,
)

@Serializable
data class YuGiOhGame(
    val players: List<YuGiOhPlayer>,
    val startingLife: Int,        // 8000 (standard) / 4000 (classic) / custom
    val step: Int = 100,          // amount applied by the inline − / + buttons
    val finished: Boolean = false,
)

// ---- Pokémon TCG ----

@Serializable
data class PokemonPlayer(
    val name: String,
    val color: Long,
    val prizes: Int = 6,      // prize cards still to take; reaching 0 = win
    val damage: Int = 0,      // damage counters on the active Pokémon (multiples of 10)
    val won: Boolean = false,
)

@Serializable
data class PokemonGame(
    val players: List<PokemonPlayer>,
    val startingPrizes: Int = 6,
    val damageStep: Int = 10,
    val finished: Boolean = false,
)

// ---- Digimon TCG ----

@Serializable
data class DigimonPlayer(
    val name: String,
    val color: Long,
    val security: Int = 5,       // security stack; taking a hit at 0 = defeat
    val defeated: Boolean = false,
)

@Serializable
data class DigimonGame(
    val players: List<DigimonPlayer>,     // always 2 (1v1)
    val startingSecurity: Int = 5,
    val memory: Int = 0,                  // shared memory gauge, -10..+10 (− = top player, + = bottom)
    val finished: Boolean = false,
)

// ---- Disney Lorcana ----

@Serializable
data class LorcanaPlayer(
    val name: String,
    val color: Long,
    val lore: Int = 0,       // reaching targetLore wins
    val won: Boolean = false,
)

@Serializable
data class LorcanaGame(
    val players: List<LorcanaPlayer>,
    val targetLore: Int = 20,
    val finished: Boolean = false,
)

// ---- One Piece Card Game ----

@Serializable
data class OnePiecePlayer(
    val name: String,
    val color: Long,
    val life: Int = 5,        // life cards; taking a hit at 0 = defeat
    val don: Int = 0,         // active DON!! (0..10)
    val defeated: Boolean = false,
)

@Serializable
data class OnePieceGame(
    val players: List<OnePiecePlayer>,     // always 2 (1v1)
    val startingLife: Int = 5,
    val finished: Boolean = false,
)

// ---- Chinchón ----

@Serializable
data class ChinchonPlayer(
    val name: String,
    val color: Long,
    val score: Int = 0,
)

@Serializable
data class ChinchonGame(
    val players: List<ChinchonPlayer>,
    val target: Int = 100,        // reaching/exceeding it ends the game; lowest score wins
    val finished: Boolean = false,
)

// ---- Burako ----

@Serializable
data class BurakoTeam(
    val name: String,
    val color: Long,
    val score: Int = 0,
)

@Serializable
data class BurakoGame(
    val teams: List<BurakoTeam>,  // always 2
    val target: Int = 2000,       // first to reach it wins (highest)
    val finished: Boolean = false,
)

// ---- Darts 501 ----

@Serializable
data class DartsPlayer(
    val name: String,
    val color: Long,
    val remaining: Int = 501,
)

@Serializable
data class DartsGame(
    val players: List<DartsPlayer>,
    val startScore: Int = 501,
    val finished: Boolean = false,
)

// ---- Dungeons & Dragons (party stat tracker) ----

@Serializable
data class DndCharacter(
    val name: String,
    val color: Long,
    val hp: Int = 20,
    val maxHp: Int = 20,
    val tempHp: Int = 0,          // temporary HP; absorbed before real HP
    val ac: Int = 10,             // armor class
    val initiative: Int = 0,
    val deathSuccess: Int = 0,    // 0..3, only meaningful while hp == 0
    val deathFail: Int = 0,       // 0..3; reaching 3 = dead
    val inspiration: Boolean = false,
)

@Serializable
data class DndGame(
    val characters: List<DndCharacter>,
    val round: Int = 1,
    val sortByInitiative: Boolean = false,
    val finished: Boolean = false,
)

// ---- Generala ----

@Serializable
enum class GeneralaCat(val fixed: Int, val servida: Int) {
    UNO(0, 0), DOS(0, 0), TRES(0, 0), CUATRO(0, 0), CINCO(0, 0), SEIS(0, 0),
    ESCALERA(20, 25), FULL(30, 35), POKER(40, 45), GENERALA(50, 50), DOBLE(100, 100);

    val isNumber: Boolean get() = ordinal <= 5
    val face: Int get() = ordinal + 1   // die face for number categories
}

@Serializable
data class GeneralaPlayer(
    val name: String,
    val color: Long,
    val scores: Map<GeneralaCat, Int> = emptyMap(),   // 0 = tachado
)

@Serializable
data class GeneralaGame(
    val players: List<GeneralaPlayer>,
    val finished: Boolean = false,
)

// ---- Bowling ----

@Serializable
data class BowlingPlayer(
    val name: String,
    val color: Long,
    val rolls: List<Int> = emptyList(),   // pins knocked down per roll, in order
)

@Serializable
data class BowlingGame(
    val players: List<BowlingPlayer>,
    val turn: Int = 0,
    val rollLog: List<Int> = emptyList(), // player index per roll, for undo
    val finished: Boolean = false,
)

// ---- Uno ----

@Serializable
data class UnoPlayer(
    val name: String,
    val color: Long,
    val score: Int = 0,
)

@Serializable
data class UnoGame(
    val players: List<UnoPlayer>,
    val target: Int = 500,
    val rounds: Int = 0,
    val finished: Boolean = false,
)

// ---- Warhammer 40k ----

@Serializable
data class WarhammerPlayer(
    val name: String,
    val color: Long,
    val primary: Int = 0,
    val secondary: Int = 0,
    val cp: Int = 0,
)

@Serializable
data class WarhammerGame(
    val players: List<WarhammerPlayer>,   // always 2
    val round: Int = 1,                   // battle round 1..5
    val finished: Boolean = false,
)

// ---- Poker (blind timer) ----

@Serializable
data class PokerLevel(val small: Int, val big: Int)

@Serializable
data class PokerGame(
    val levels: List<PokerLevel>,
    val level: Int = 0,
    val levelMinutes: Int = 15,
    val running: Boolean = false,
    val endAt: Long = 0L,                 // wall-clock end of the current level while running
    val remainingMs: Long = 15 * 60_000L, // remaining time while paused
    val playersLeft: Int = 8,
    val finished: Boolean = false,
)

// ---- Star Wars Unlimited ----

@Serializable
data class SwuPlayer(
    val name: String,
    val color: Long,
    val hp: Int = 30,            // base HP remaining
    val defeated: Boolean = false,
)

@Serializable
data class SwuGame(
    val players: List<SwuPlayer>,   // always 2
    val startingHp: Int = 30,
    val initiative: Int = 0,        // index of the player holding the initiative token
    val finished: Boolean = false,
)

// ---- Escoba de 15 ----

@Serializable
data class EscobaPlayer(
    val name: String,
    val color: Long,
    val score: Int = 0,
    val escobas: Int = 0,          // escobas in the current hand (added on round close)
)

@Serializable
data class EscobaGame(
    val players: List<EscobaPlayer>,
    val target: Int = 15,
    val finished: Boolean = false,
)

// ---- Mus ----

@Serializable
data class MusTeam(
    val name: String,
    val color: Long,
    val piedras: Int = 0,     // 0..40; 5 piedras = 1 amarrako
    val juegos: Int = 0,
    val vacas: Int = 0,
)

@Serializable
data class MusGame(
    val teams: List<MusTeam>,               // always 2
    val juegosPerVaca: Int = 3,
    val history: List<List<MusTeam>> = emptyList(),  // snapshots for undo
    val finished: Boolean = false,
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

// ---- End-of-game result (shown on the winner screen) ----

@Serializable
data class ResultLine(
    val name: String,
    val value: String,
    val color: Long,
    val winner: Boolean = false,
)

@Serializable
data class GameResult(
    val gameType: GameType,
    val title: String,
    val winners: List<String>,
    val lines: List<ResultLine>,
    val summary: String,
)

// ---- Settings ----

@Serializable
data class Settings(
    val language: String = "es",   // "es" | "en"
    val soundVolume: Float = 1f,   // 0f..1f
)

/** Root persisted state — serialized as one JSON blob in DataStore. */
@Serializable
data class AppState(
    val users: List<User> = emptyList(),
    val savedConfigs: List<SavedConfig> = emptyList(),
    val favoriteGames: Set<GameType> = emptySet(),
    val collapsedSections: Set<String> = emptySet(),   // menu section keys the user folded
    val currentGame: CurrentGame? = null,
    val trucoMatch: TrucoMatch? = null,
    val magicGame: MagicGame? = null,
    val yugiohGame: YuGiOhGame? = null,
    val pokemonGame: PokemonGame? = null,
    val digimonGame: DigimonGame? = null,
    val lorcanaGame: LorcanaGame? = null,
    val onePieceGame: OnePieceGame? = null,
    val chinchonGame: ChinchonGame? = null,
    val burakoGame: BurakoGame? = null,
    val dartsGame: DartsGame? = null,
    val dndGame: DndGame? = null,
    val generalaGame: GeneralaGame? = null,
    val bowlingGame: BowlingGame? = null,
    val unoGame: UnoGame? = null,
    val warhammerGame: WarhammerGame? = null,
    val pokerGame: PokerGame? = null,
    val swuGame: SwuGame? = null,
    val escobaGame: EscobaGame? = null,
    val musGame: MusGame? = null,
    val history: List<HistoryEntry> = emptyList(),
    val settings: Settings = Settings(),
    val pendingResult: GameResult? = null,
    val unlockedAvatars: Set<Int> = setOf(1, 2, 12, 24),
    val avatarAdProgress: Map<Int, Int> = emptyMap(),  // avatarId → ads watched so far toward unlock
    val adBaseline: Int = 0,           // history.size when the last interstitial was shown
)
