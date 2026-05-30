package com.ludomasterpro.engine

// ═══════════════════════════════════════════════════════════════
//  LUDO MASTER PRO — Game Engine (Kotlin pur, sans Android)
// ═══════════════════════════════════════════════════════════════

import kotlin.random.Random

// ─── Enums ────────────────────────────────────────────────────
enum class PieceColor(val hex: String, val hexDark: String, val label: String) {
    RED   ("#E53935", "#B71C1C", "Rouge"),
    BLUE  ("#1E88E5", "#0D47A1", "Bleu"),
    GREEN ("#43A047", "#1B5E20", "Vert"),
    YELLOW("#FDD835", "#F9A825", "Jaune");

    val emoji get() = when(this) {
        RED -> "🔴"; BLUE -> "🔵"; GREEN -> "🟢"; YELLOW -> "🟡"
    }
    val textDark get() = this == YELLOW
}

enum class PlayerType { HUMAN, AI }
enum class AiLevel    { EASY, NORMAL, EXPERT }
enum class GamePhase  { MENU, PLAYING, FINISHED }

// ─── Constantes du plateau ────────────────────────────────────
object BoardConstants {
    /** 52 cases du chemin principal sous forme (row, col) */
    val PATH: List<Pair<Int,Int>> = listOf(
        14 to 6, 13 to 6, 12 to 6, 11 to 6, 10 to 6, 9 to 6,
        8 to 5,  8 to 4,  8 to 3,  8 to 2,  8 to 1,  8 to 0,
        7 to 0,  6 to 0,
        6 to 1,  6 to 2,  6 to 3,  6 to 4,  6 to 5,
        5 to 6,  4 to 6,  3 to 6,  2 to 6,  1 to 6,  0 to 6,
        0 to 7,  0 to 8,
        1 to 8,  2 to 8,  3 to 8,  4 to 8,  5 to 8,
        6 to 9,  6 to 10, 6 to 11, 6 to 12, 6 to 13, 6 to 14,
        7 to 14, 8 to 14,
        8 to 13, 8 to 12, 8 to 11, 8 to 10, 8 to 9,
        9 to 8,  10 to 8, 11 to 8, 12 to 8, 13 to 8, 14 to 8,
        14 to 7, 14 to 6
    ).take(52)

    /** Couloirs finaux (6 cases avant le centre) */
    val CORRIDORS = mapOf(
        PieceColor.RED    to listOf(13 to 7, 12 to 7, 11 to 7, 10 to 7, 9 to 7, 8 to 7),
        PieceColor.BLUE   to listOf(7 to 1,  7 to 2,  7 to 3,  7 to 4,  7 to 5, 7 to 6),
        PieceColor.GREEN  to listOf(1 to 7,  2 to 7,  3 to 7,  4 to 7,  5 to 7, 6 to 7),
        PieceColor.YELLOW to listOf(7 to 13, 7 to 12, 7 to 11, 7 to 10, 7 to 9, 7 to 8),
    )
    val CENTER = 7 to 7

    /** Cases de maison (base départ) */
    val HOME_CELLS = mapOf(
        PieceColor.RED    to listOf(11 to 1, 11 to 2, 12 to 1, 12 to 2),
        PieceColor.BLUE   to listOf(1 to 11, 2 to 11, 1 to 12, 2 to 12),
        PieceColor.GREEN  to listOf(11 to 11, 11 to 12, 12 to 11, 12 to 12),
        PieceColor.YELLOW to listOf(1 to 1,  1 to 2,  2 to 1,  2 to 2),
    )

    /** Index de sortie de base sur la piste principale */
    val HOME_ENTRY = mapOf(
        PieceColor.RED to 0, PieceColor.BLUE to 13,
        PieceColor.GREEN to 26, PieceColor.YELLOW to 39
    )
    /** Dernière case avant d'entrer dans le couloir */
    val FINAL_ENTRY = mapOf(
        PieceColor.RED to 50, PieceColor.BLUE to 11,
        PieceColor.GREEN to 24, PieceColor.YELLOW to 37
    )
    /** Première case du couloir final (index logique) */
    val FINAL_START = mapOf(
        PieceColor.RED to 52, PieceColor.BLUE to 58,
        PieceColor.GREEN to 64, PieceColor.YELLOW to 70
    )
    /** Dernière case du couloir final */
    val FINAL_END = mapOf(
        PieceColor.RED to 57, PieceColor.BLUE to 63,
        PieceColor.GREEN to 69, PieceColor.YELLOW to 75
    )
    /** Cases protégées (rosettes) */
    val SAFE_CELLS = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    const val POS_BASE    = -1
    const val POS_ARRIVED = 100
}

// ─── Data classes ─────────────────────────────────────────────
data class Piece(
    val id: String,           // "RED_0" etc.
    val color: PieceColor,
    val index: Int,           // 0-3
    val pos: Int = BoardConstants.POS_BASE
) {
    val isAtBase    get() = pos == BoardConstants.POS_BASE
    val hasArrived  get() = pos == BoardConstants.POS_ARRIVED

    fun gridCell(): Pair<Int,Int> = with(BoardConstants) {
        when {
            isAtBase   -> HOME_CELLS[color]!![index]
            hasArrived -> CENTER
            pos >= FINAL_START[color]!! -> CORRIDORS[color]!![pos - FINAL_START[color]!!]
            else -> PATH[pos % 52]
        }
    }
}

data class Player(
    val id: String,
    val name: String,
    val color: PieceColor,
    val type: PlayerType,
    val aiLevel: AiLevel = AiLevel.NORMAL,
    val pieces: List<Piece> = List(4) { i -> Piece("${color.name}_$i", color, i) },
    val score: Int = 0,
    val captures: Int = 0,
    val capturesSuffered: Int = 0,
    val rank: Int? = null
) {
    val hasWon get() = pieces.all { it.hasArrived }
    val piecesArrived get() = pieces.count { it.hasArrived }
}

data class HistoryEntry(
    val turn: Int,
    val playerName: String,
    val color: PieceColor,
    val dice: Int,
    val action: String
)

data class GameState(
    val phase: GamePhase = GamePhase.MENU,
    val players: List<Player> = emptyList(),
    val currentTurn: Int = 0,
    val dice: Int = 0,
    val totalTurns: Int = 0,
    val history: List<HistoryEntry> = emptyList(),
    val ranking: List<Player> = emptyList(),
    val playablePieceIds: List<String> = emptyList(),
    val waitingForChoice: Boolean = false,
    val animating: Boolean = false,
    val message: String = ""
) {
    val currentPlayer get() = players.getOrNull(currentTurn % players.size.coerceAtLeast(1))
}

// ─── Logique de jeu ───────────────────────────────────────────
object LudoRules {

    fun rollDice(): Int = Random.nextInt(1, 7)

    /**
     * Calcule la nouvelle position logique d'un pion avec un dé donné.
     * Retourne null si le mouvement est impossible.
     */
    fun computeNewPos(piece: Piece, dice: Int): Int? = with(BoardConstants) {
        val c   = piece.color
        val fs  = FINAL_START[c]!!
        val fe  = FINAL_END[c]!!
        val ent = FINAL_ENTRY[c]!!
        val pos = piece.pos

        when {
            pos >= fs -> {
                val np = pos + dice
                when {
                    np < fe  -> np
                    np == fe -> np
                    np == fe + 1 -> POS_ARRIVED
                    else -> null
                }
            }
            else -> {
                val dist = ((ent - pos) + 52) % 52
                if (dice > dist) {
                    val rem = dice - dist - 1
                    val np  = fs + rem
                    when {
                        np <= fe -> np
                        np == fe + 1 -> POS_ARRIVED
                        else -> null
                    }
                } else {
                    (pos + dice) % 52
                }
            }
        }
    }

    /**
     * Retourne la liste des positions intermédiaires pour l'animation.
     */
    fun computeAnimPath(piece: Piece, dice: Int): List<Int> = with(BoardConstants) {
        if (piece.isAtBase) return listOf(HOME_ENTRY[piece.color]!!)

        val c   = piece.color
        val fs  = FINAL_START[c]!!
        val fe  = FINAL_END[c]!!
        val ent = FINAL_ENTRY[c]!!
        val pos = piece.pos

        buildList {
            if (pos >= fs) {
                for (i in 1..dice) {
                    val np = pos + i
                    if (np <= fe) add(np)
                    else { add(POS_ARRIVED); break }
                }
            } else {
                val dist = ((ent - pos) + 52) % 52
                if (dice > dist) {
                    for (i in 1..dist) add((pos + i) % 52)
                    val rem = dice - dist - 1
                    for (j in 0..rem) {
                        val np = fs + j
                        if (np <= fe) add(np)
                        else { add(POS_ARRIVED); break }
                    }
                } else {
                    for (i in 1..dice) add((pos + i) % 52)
                }
            }
        }
    }

    /** Pions jouables pour un joueur et un dé donnés. */
    fun getPlayablePieces(player: Player, dice: Int): List<Piece> =
        player.pieces.filter { p ->
            when {
                p.hasArrived -> false
                p.isAtBase   -> dice == 6
                else         -> computeNewPos(p, dice) != null
            }
        }

    /**
     * Applique un mouvement : retourne le nouvel état du jeu.
     */
    fun applyMove(state: GameState, pieceId: String, finalPos: Int): GameState {
        val playerIdx = state.currentTurn % state.players.size
        var captured  = false
        var capturedPlayerIdx = -1

        val players = state.players.mapIndexed { pi, pl ->
            if (pi == playerIdx) {
                val pieces = pl.pieces.map { p ->
                    if (p.id == pieceId) p.copy(pos = finalPos) else p
                }
                val score = pieces.count { it.hasArrived }
                pl.copy(pieces = pieces, score = score)
            } else {
                // Vérifier capture
                val pieces = pl.pieces.map { p ->
                    val isSafe = finalPos >= BoardConstants.FINAL_START[pl.color]!!
                        || finalPos in BoardConstants.SAFE_CELLS
                        || finalPos < 0
                    if (p.pos == finalPos && !isSafe && finalPos != BoardConstants.POS_ARRIVED) {
                        captured = true
                        capturedPlayerIdx = pi
                        p.copy(pos = BoardConstants.POS_BASE)
                    } else p
                }
                if (captured && capturedPlayerIdx == pi)
                    pl.copy(pieces = pieces, capturesSuffered = pl.capturesSuffered + 1)
                else pl
            }
        }.toMutableList()

        // Mettre à jour les captures du joueur actif
        if (captured) {
            val ap = players[playerIdx]
            players[playerIdx] = ap.copy(captures = ap.captures + 1)
        }

        // Vérifier victoire
        var ranking  = state.ranking.toMutableList()
        var phase    = state.phase
        val updatedPlayer = players[playerIdx]

        if (updatedPlayer.hasWon && updatedPlayer.rank == null) {
            val rank = ranking.size + 1
            players[playerIdx] = updatedPlayer.copy(rank = rank)
            ranking.add(players[playerIdx])
            if (ranking.size >= state.players.size) phase = GamePhase.FINISHED
        }

        val replayTurn = state.dice == 6 && phase != GamePhase.FINISHED
        val nextTurn   = if (replayTurn) state.currentTurn else state.currentTurn + 1

        val captureMsg = if (captured) "💥 Capture !" else ""
        val arrivedMsg = if (finalPos == BoardConstants.POS_ARRIVED) "🏆 Pion arrivé !" else ""
        val replayMsg  = if (replayTurn) "🎲 6 ! ${updatedPlayer.name} rejoue !" else ""
        val msg = listOf(captureMsg, arrivedMsg, replayMsg).filter { it.isNotEmpty() }.joinToString(" ")

        return state.copy(
            players         = players,
            currentTurn     = nextTurn,
            ranking         = ranking,
            phase           = phase,
            animating       = false,
            waitingForChoice= false,
            playablePieceIds= emptyList(),
            totalTurns      = if (replayTurn) state.totalTurns else state.totalTurns + 1,
            message         = msg
        )
    }

    // ── IA ──────────────────────────────────────────────────────
    fun aiChoose(player: Player, playable: List<Piece>, allPlayers: List<Player>, dice: Int): Piece {
        if (player.aiLevel == AiLevel.EASY || playable.size == 1) return playable.random()

        data class Score(val piece: Piece, val value: Int)

        val scores = playable.map { p ->
            var s = 0
            if (p.isAtBase) {
                s = 10
            } else {
                val np = computeNewPos(p, dice) ?: return@map Score(p, -9999)
                if (np == BoardConstants.POS_ARRIVED) return@map Score(p, 10000)

                s += if (np >= BoardConstants.FINAL_START[p.color]!!) np + 200 else np

                // Bonus capture
                for (other in allPlayers) {
                    if (other.color == player.color) continue
                    for (op in other.pieces) {
                        if (op.pos == np && np !in BoardConstants.SAFE_CELLS) {
                            s += if (player.aiLevel == AiLevel.EXPERT) 600 else 400
                        }
                    }
                }

                // Expert : pénalité danger
                if (player.aiLevel == AiLevel.EXPERT && np !in BoardConstants.SAFE_CELLS) {
                    for (other in allPlayers) {
                        if (other.color == player.color) continue
                        for (op in other.pieces) {
                            if (!op.isAtBase && !op.hasArrived) {
                                if (kotlin.math.abs(op.pos - np) in 1..6) s -= 70
                            }
                        }
                    }
                }
            }
            Score(p, s)
        }
        return scores.maxByOrNull { it.value }?.piece ?: playable.first()
    }
}
