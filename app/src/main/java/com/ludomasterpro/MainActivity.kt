package com.ludomasterpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ludomasterpro.engine.GamePhase
import com.ludomasterpro.engine.GameViewModel
import com.ludomasterpro.ui.screens.GameScreen
import com.ludomasterpro.ui.screens.MenuScreen
import com.ludomasterpro.ui.screens.PodiumScreen
import com.ludomasterpro.ui.theme.LudoMasterTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LudoMasterTheme {
                LudoApp(viewModel)
            }
        }
    }
}

@Composable
fun LudoApp(viewModel: GameViewModel) {
    val state      by viewModel.state.collectAsStateWithLifecycle()
    val configs    by viewModel.configs.collectAsStateWithLifecycle()
    val nbPlayers  by viewModel.nbPlayers.collectAsStateWithLifecycle()
    val bestScores by viewModel.bestScores.collectAsStateWithLifecycle()
    var showQuit   by remember { mutableStateOf(false) }

    when (state.phase) {
        GamePhase.MENU -> MenuScreen(
            nbPlayers  = nbPlayers,
            configs    = configs,
            bestScores = bestScores,
            onNbChange = viewModel::setNbPlayers,
            onConfig   = viewModel::updateConfig,
            onStart    = viewModel::startGame
        )
        GamePhase.PLAYING -> {
            GameScreen(
                state        = state,
                onRollResult = viewModel::applyDiceResult,
                onPieceClick = viewModel::selectPiece,
                onApplyMove  = viewModel::applyMove,
                onQuit       = { showQuit = true }
            )
            if (showQuit) {
                AlertDialog(
                    onDismissRequest = { showQuit = false },
                    title   = { Text("Quitter ?") },
                    text    = { Text("La partie en cours sera perdue.") },
                    confirmButton = {
                        TextButton(onClick = { showQuit = false; viewModel.goToMenu() }) {
                            Text("Quitter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuit = false }) { Text("Continuer") }
                    }
                )
            }
        }
        GamePhase.FINISHED -> PodiumScreen(
            players    = state.players,
            totalTurns = state.totalTurns,
            bestScores = bestScores,
            onReplay   = viewModel::replayGame,
            onMenu     = viewModel::goToMenu
        )
    }
}
