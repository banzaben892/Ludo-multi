package com.ludomasterpro.ui.screens

// ═══════════════════════════════════════════════════════════════
//  GameScreen + PodiumScreen — fidèles à la référence UI
//  - Fond violet sombre
//  - En-tête : avatar joueur actif (encadré couleur) + stats ✕ ☠
//  - Plateau centré
//  - Zone dé en bas avec stats + bouton doré
//  - Barre de navigation verte en bas
// ═══════════════════════════════════════════════════════════════

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.engine.*
import com.ludomasterpro.ui.components.BoardCanvas
import com.ludomasterpro.ui.components.DiceAreaRow

// ─── Couleurs helper ────────────────────────────────────────
fun PieceColor.toColor() = when (this) {
    PieceColor.RED    -> Color(0xFFE53935)
    PieceColor.BLUE   -> Color(0xFF1E88E5)
    PieceColor.GREEN  -> Color(0xFF43A047)
    PieceColor.YELLOW -> Color(0xFFFDD835)
}

// ═══════════════════════════════════════════════════════════════
//  GAME SCREEN
// ═══════════════════════════════════════════════════════════════
@Composable
fun GameScreen(
    state:        GameState,
    onRollResult: (Int) -> Unit,
    onPieceClick: (String) -> Unit,
    onApplyMove:  (String, Int) -> Unit,
    onQuit:       () -> Unit
) {
    val cfg      = LocalConfiguration.current
    val player   = state.currentPlayer
    val pColor   = player?.color?.toColor() ?: Color(0xFFFFD700)
    val canRoll  = player?.type == PlayerType.HUMAN
                   && !state.waitingForChoice
                   && !state.animating

    // Fond violet sombre (identique à la ref)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2F6B))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── HEADER : avatar joueur actif + stats ──────────
            if (player != null) {
                PlayerHeader(player = player, allPlayers = state.players)
            }

            // ── PLATEAU ───────────────────────────────────────
            val boardDp = (cfg.screenWidthDp - 16).coerceAtMost(440).dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Message si besoin
                    if (state.message.isNotEmpty()) {
                        Text(
                            state.message,
                            color      = Color(0xFFFFD700),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    BoardCanvas(
                        players          = state.players,
                        playablePieceIds = when {
                            state.waitingForChoice -> state.playablePieceIds
                            state.playablePieceIds.size == 1 && !state.animating -> state.playablePieceIds
                            else -> emptyList()
                        },
                        boardSize    = boardDp,
                        onPieceClick = onPieceClick
                    )
                }
            }

            // ── ZONE DÉ + STATS ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                DiceAreaRow(
                    currentValue    = state.dice,
                    playerCaptures  = player?.captures ?: 0,
                    playerSuffered  = player?.capturesSuffered ?: 0,
                    canRoll         = canRoll,
                    onRollResult    = onRollResult
                )
            }

            // ── BARRE NAVIGATION VERTE ─────────────────────────
            BottomNavBar(
                title    = "Solo\nClassique",
                onBack   = onQuit,
                onRun    = { /* mode rapide */ },
                onSettings = { /* paramètres */ }
            )
        }
    }
}

// ── En-tête joueur actif ─────────────────────────────────────
@Composable
fun PlayerHeader(player: Player, allPlayers: List<Player>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Stats ✕/☠ du joueur opposant affiché en haut à droite
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${player.captures}", color = Color.White,
                     fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("✕", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${player.capturesSuffered}", color = Color.White,
                     fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("☠", color = Color.White, fontSize = 13.sp)
            }
        }

        // Avatar encadré de la couleur du joueur
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF3D3F7A))
                .border(
                    width = 3.dp,
                    color = player.color.toColor(),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text     = when (player.type) { PlayerType.HUMAN -> "😊"; else -> "🤖" },
                fontSize = 30.sp
            )
        }
    }
}

// ── Barre de navigation verte ────────────────────────────────
@Composable
fun BottomNavBar(
    title:      String,
    onBack:     () -> Unit,
    onRun:      () -> Unit,
    onSettings: () -> Unit
) {
    val greenBtn  = Color(0xFF4CAF50)
    val greenDark = Color(0xFF388E3C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2057))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // ← Retour
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(greenBtn, greenDark)))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("◀", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Titre central
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF9E9EC8),
                textAlign  = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        // Boutons droite
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // 🏃 Mode rapide
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(greenBtn, greenDark)))
                    .clickable { onRun() },
                contentAlignment = Alignment.Center
            ) {
                Text("🏃", fontSize = 22.sp)
            }
            // ⚙️ Paramètres
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(greenBtn, greenDark)))
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 22.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  PODIUM SCREEN
// ═══════════════════════════════════════════════════════════════
@Composable
fun PodiumScreen(
    players:    List<Player>,
    totalTurns: Int,
    bestScores: Map<String, Int>,
    onReplay:   () -> Unit,
    onMenu:     () -> Unit
) {
    val sorted  = players.sortedBy { it.rank ?: 99 }
    val winner  = sorted.firstOrNull()
    val isRecord = winner != null &&
        bestScores[winner.name].let { it == null || totalTurns <= it }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2F6B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Text("🏆", fontSize = 64.sp)
            Text("FIN DE PARTIE",
                 fontSize   = 24.sp, fontWeight = FontWeight.Bold,
                 fontFamily = FontFamily.SansSerif, color = Color(0xFFFFD700))
            Text("$totalTurns tours",
                 fontSize = 13.sp, color = Color(0xFF9E9EC8))

            if (isRecord) {
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = Color(0x33FFD700),
                    border = BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text("🏅 NOUVEAU RECORD !",
                         modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                         fontWeight = FontWeight.Bold, fontSize = 13.sp,
                         color      = Color(0xFFFFD700))
                }
            }

            // Podium
            val podOrder = listOfNotNull(sorted.getOrNull(1), sorted.getOrNull(0), sorted.getOrNull(2))
            val heights  = listOf(100.dp, 140.dp, 70.dp)
            val medals   = listOf("🥈","🥇","🥉")
            val barC     = listOf(Color(0xFFC0C0C0), Color(0xFFFFD700), Color(0xFFCD7F32))

            Row(
                modifier              = Modifier.fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment     = Alignment.Bottom
            ) {
                podOrder.forEachIndexed { vi, pl ->
                    val h by animateDpAsState(heights[vi],
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "h")
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                           modifier = Modifier.width(88.dp)) {
                        Text(pl.color.emoji, fontSize = 22.sp)
                        Text(pl.name.take(9), fontFamily = FontFamily.SansSerif,
                             fontSize = 11.sp, fontWeight = FontWeight.Bold,
                             color = pl.color.toColor())
                        Text(medals[vi], fontSize = 22.sp)
                        Box(
                            Modifier.fillMaxWidth().height(h)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barC[vi].copy(alpha = 0.85f)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text("#${pl.rank}", modifier = Modifier.padding(top = 6.dp),
                                 fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                 color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Stats
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF3D3F7A),
                    border = BorderStroke(1.dp, Color(0xFF555588)),
                    modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 STATISTIQUES", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                         color = Color(0xFF9E9EC8), letterSpacing = 1.sp)
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Joueur","Pions","✕","☠").forEach { h ->
                            Text(h, Modifier.weight(1f), fontSize = 10.sp,
                                 color = Color(0xFF6666AA), textAlign = TextAlign.Center)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF555588))
                    sorted.forEach { pl ->
                        Row(Modifier.fillMaxWidth()) {
                            Text("${pl.color.emoji} ${pl.name.take(7)}",
                                 Modifier.weight(1f), fontSize = 10.sp,
                                 color = pl.color.toColor())
                            listOf("${pl.piecesArrived}/4","${pl.captures}","${pl.capturesSuffered}")
                                .forEach { v ->
                                    Text(v, Modifier.weight(1f), fontSize = 10.sp,
                                         color = Color.White, textAlign = TextAlign.Center)
                                }
                        }
                    }
                }
            }

            // Boutons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReplay, Modifier.weight(1f).height(52.dp),
                       shape  = RoundedCornerShape(12.dp),
                       colors = ButtonDefaults.buttonColors(
                           containerColor = Color(0xFFFFD700), contentColor = Color(0xFF0D0D1A))) {
                    Text("🔄  Rejouer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(onClick = onMenu, Modifier.weight(1f).height(52.dp),
                               shape  = RoundedCornerShape(12.dp),
                               border = BorderStroke(1.dp, Color(0xFF555588)),
                               colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text("🏠  Menu", fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
