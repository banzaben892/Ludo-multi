package com.ludomasterpro.engine

// ═══════════════════════════════════════════════════════════════
//  LUDO MASTER PRO — GameViewModel (MVVM + Coroutines)
// ═══════════════════════════════════════════════════════════════

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ludo_prefs")

// ── Config d'un joueur dans le menu ──────────────────────────
data class PlayerConfig(
    val name:    String,
    val type:    PlayerType,
    val aiLevel: AiLevel
)

private val defaultConfigs = listOf(
    PlayerConfig("Rouge",  PlayerType.HUMAN,  AiLevel.NORMAL),
    PlayerConfig("Bleu",   PlayerType.AI,     AiLevel.NORMAL),
    PlayerConfig("Vert",   PlayerType.AI,     AiLevel.NORMAL),
    PlayerConfig("Jaune",  PlayerType.AI,     AiLevel.EASY),
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // ── StateFlows exposés à l'UI ─────────────────────────────
    private val _state      = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _configs    = MutableStateFlow(defaultConfigs)
    val configs: StateFlow<List<PlayerConfig>> = _configs.asStateFlow()

    private val _nbPlayers  = MutableStateFlow(2)
    val nbPlayers: StateFlow<Int> = _nbPlayers.asStateFlow()

    private val _bestScores = MutableStateFlow<Map<String,Int>>(emptyMap())
    val bestScores: StateFlow<Map<String,Int>> = _bestScores.asStateFlow()

    // Animation path en cours
    private var animPath: List<Int> = emptyList()
    private var animPieceId: String = ""

    init {
        loadBestScores()
    }

    // ── Persistance des records ───────────────────────────────
    private fun loadBestScores() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.data.collect { prefs ->
                val map = prefs.asMap()
                    .entries
                    .filter { it.key.name.startsWith("score_") }
                    .associate { it.key.name.removePrefix("score_") to (it.value as Int) }
                _bestScores.value = map
            }
        }
    }

    private fun saveScore(playerName: String, turns: Int) {
        viewModelScope.launch {
            val current = _bestScores.value[playerName]
            if (current == null || turns < current) {
                val key = intPreferencesKey("score_$playerName")
                getApplication<Application>().dataStore.edit { it[key] = turns }
            }
        }
    }

    // ── Configuration ─────────────────────────────────────────
    fun setNbPlayers(n: Int) { _nbPlayers.value = n }

    fun updateConfig(index: Int, config: PlayerConfig) {
        _configs.value = _configs.value.toMutableList().also { it[index] = config }
    }

    // ── Démarrer une partie ───────────────────────────────────
    fun startGame() {
        val colors = PieceColor.entries
        val players = (0 until _nbPlayers.value).map { i ->
            val cfg = _configs.value[i]
            Player(
                id      = colors[i].name,
                name    = cfg.name.ifBlank { colors[i].label },
                color   = colors[i],
                type    = cfg.type,
                aiLevel = cfg.aiLevel
            )
        }
        _state.value = GameState(
            phase   = GamePhase.PLAYING,
            players = players
        )
        viewModelScope.launch {
            delay(400)
            triggerNextTurn()
        }
    }

    // ── Tour suivant ──────────────────────────────────────────
    private fun triggerNextTurn() {
        val s = _state.value
        val player = s.currentPlayer ?: return
        if (player.hasWon) {
            advanceTurnSkipWinners()
            return
        }
        _state.value = s.copy(message = if (player.type == PlayerType.HUMAN) "Lancez le dé !" else "")
        if (player.type == PlayerType.AI) {
            viewModelScope.launch {
                delay(700)
                performAiRoll()
            }
        }
    }

    private fun advanceTurnSkipWinners() {
        val s = _state.value
        var turn = s.currentTurn + 1
        repeat(s.players.size) {
            val p = s.players[turn % s.players.size]
            if (!p.hasWon) return@repeat
            turn++
        }
        _state.value = s.copy(currentTurn = turn)
        triggerNextTurn()
    }

    // ── Lancer le dé (humain) ─────────────────────────────────
    fun humanRoll() {
        val s = _state.value
        if (s.animating || s.waitingForChoice) return
        val player = s.currentPlayer ?: return
        if (player.type != PlayerType.HUMAN) return
    }

    fun applyDiceResult(dice: Int) {
        val s = _state.value
        val player = s.currentPlayer ?: return
        val playable = LudoRules.getPlayablePieces(player, dice)
        val hist = HistoryEntry(s.totalTurns, player.name, player.color, dice, "Dé: $dice")

        if (playable.isEmpty()) {
            _state.value = s.copy(
                dice    = dice,
                history = (s.history + hist + HistoryEntry(s.totalTurns, player.name, player.color, dice, "✗ Aucun coup")).takeLast(60),
                message = "Aucun mouvement possible."
            )
            viewModelScope.launch {
                delay(1200)
                finishTurn()
            }
            return
        }

        if (playable.size == 1 || player.type == PlayerType.AI) {
            val chosen = if (player.type == PlayerType.AI)
                LudoRules.aiChoose(player, playable, s.players, dice)
            else playable.first()

            _state.value = s.copy(
                dice            = dice,
                history         = (s.history + hist).takeLast(60),
                playablePieceIds= listOf(chosen.id),
                animating       = true
            )
            startPieceAnimation(chosen, dice)
        } else {
            _state.value = s.copy(
                dice             = dice,
                history          = (s.history + hist).takeLast(60),
                playablePieceIds = playable.map { it.id },
                waitingForChoice = true,
                message          = "Choisissez un pion"
            )
        }
    }

    // ── Choix d'un pion (humain) ──────────────────────────────
    fun selectPiece(pieceId: String) {
        val s = _state.value
        if (!s.waitingForChoice || pieceId !in s.playablePieceIds) return
        val player = s.currentPlayer ?: return
        val piece  = player.pieces.find { it.id == pieceId } ?: return
        _state.value = s.copy(animating = true, waitingForChoice = false)
        startPieceAnimation(piece, s.dice)
    }

    // ── Animation ─────────────────────────────────────────────
    private fun startPieceAnimation(piece: Piece, dice: Int) {
        animPath    = LudoRules.computeAnimPath(piece, dice)
        animPieceId = piece.id

        if (animPath.isEmpty()) {
            val finalPos = LudoRules.computeNewPos(piece, dice) ?: piece.pos
            finalizePieceMove(piece.id, finalPos)
            return
        }
        animateStep(piece.id, animPath, 0)
    }

    private fun animateStep(pieceId: String, path: List<Int>, step: Int) {
        viewModelScope.launch {
            delay(130)
            if (step >= path.size) {
                finalizePieceMove(pieceId, path.last())
                return@launch
            }
            val s = _state.value
            val players = s.players.map { pl ->
                val pieces = pl.pieces.map { p ->
                    if (p.id == pieceId) p.copy(pos = path[step]) else p
                }
                pl.copy(pieces = pieces)
            }
            _state.value = s.copy(players = players)
            animateStep(pieceId, path, step + 1)
        }
    }

    private fun finalizePieceMove(pieceId: String, finalPos: Int) {
        val newState = LudoRules.applyMove(_state.value, pieceId, finalPos)
        _state.value = newState

        if (newState.phase == GamePhase.FINISHED) {
            val winner = newState.ranking.firstOrNull()
            if (winner != null) saveScore(winner.name, newState.totalTurns)
            return
        }

        viewModelScope.launch {
            delay(350)
            val s = _state.value
            val player = s.currentPlayer ?: return@launch

            if (player.type == PlayerType.AI) {
                delay(if (s.dice == 6) 700 else 500)
                performAiRoll()
            } else {
                if (s.dice == 6) {
                    _state.value = s.copy(message = "🎲 6 ! Relancez !")
                } else {
                    finishTurn()
                }
            }
        }
    }

    private fun finishTurn() {
        val s = _state.value
        _state.value = s.copy(currentTurn = s.currentTurn + 1, dice = 0)
        triggerNextTurn()
    }

    private fun performAiRoll() {
        val dice = LudoRules.rollDice()
        applyDiceResult(dice)
    }

    // ── Retour menu ───────────────────────────────────────────
    fun goToMenu() {
        _state.value = GameState()
    }

    fun replayGame() { startGame() }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES APPLYMOVE POUR L'UI (CORRECTIONS)
    // ═══════════════════════════════════════════════════════════
    
    // Méthode qui accepte un objet Move (déjà existante)
    fun applyMove(move: Any) {
        when (move) {
            is Move -> {
                when (move.type) {
                    MoveType.MOVE_TOKEN -> {
                        val pieceId = move.tokenId ?: return
                        val newPos = move.newPosition ?: return
                        val newState = LudoRules.applyMove(_state.value, pieceId, newPos)
                        _state.value = newState
                    }
                    MoveType.KILL_TOKEN, MoveType.REACH_GOAL -> {
                        // Déjà géré par LudoRules.applyMove
                    }
                }
                
                if (_state.value.phase == GamePhase.FINISHED) {
                    val winner = _state.value.ranking.firstOrNull()
                    if (winner != null) saveScore(winner.name, _state.value.totalTurns)
                    return
                }
                
                viewModelScope.launch {
                    delay(350)
                    val s = _state.value
                    if (s.dice != 6) {
                        finishTurn()
                    }
                }
            }
        }
    }
    
    // Méthode qui accepte deux paramètres (pieceId et newPos) pour l'UI
    fun applyMove(pieceId: String, newPos: Int) {
        val move = Move(
            type = MoveType.MOVE_TOKEN,
            tokenId = pieceId,
            newPosition = newPos
        )
        applyMove(move)
    }
}

// ═══════════════════════════════════════════════════════════════
//  CLASSES DE SUPPORT POUR LES MOUVEMENTS
// ═══════════════════════════════════════════════════════════════

data class Move(
    val type: MoveType,
    val tokenId: String? = null,
    val newPosition: Int? = null,
    val targetTokenId: String? = null
)

enum class MoveType {
    MOVE_TOKEN, KILL_TOKEN, REACH_GOAL
}
