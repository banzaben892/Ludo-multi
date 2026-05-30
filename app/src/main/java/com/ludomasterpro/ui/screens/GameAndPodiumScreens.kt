package com.ludomasterpro.ui.screens

// ═══════════════════════════════════════════════════════════════
//  GameScreen + PodiumScreen — Jetpack Compose
// ═══════════════════════════════════════════════════════════════

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.engine.*
import com.ludomasterpro.ui.components.BoardCanvas
import com.ludomasterpro.ui.components.DiceView
import com.ludomasterpro.ui.theme.LudoColors

// ═══════════════════════════════════════════════════════════════
//  GAME SCREEN
// ═══════════════════════════════════════════════════════════════
@Composable
fun GameScreen(
    state:       GameState,
    onRollResult: (Int) -> Unit,
    onPieceClick: (String) -> Unit,
    onQuit:      () -> Unit
) {
    val config    = LocalConfiguration.current
    val landscape = config.screenWidthDp > config.screenHeightDp
    val player    = state.currentPlayer
    val pColor    = player?.color?.composeColor() ?: LudoColors.Primary
    val canRoll   = player?.type == PlayerType.HUMAN
                    && !state.waitingForChoice
                    && !state.animating

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LudoColors.BgDeep, LudoColors.BgDark)))
    ) {
        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BoardCanvas(
                    players          = state.players,
                    playablePieceIds = if (state.waitingForChoice) state.playablePieceIds
                                       else if (state.playablePieceIds.size == 1 && !state.animating)
                                            state.playablePieceIds else emptyList(),
                    boardSize        = (config.screenHeightDp - 16).dp,
                    onPieceClick     = onPieceClick
                )
                GameSidePanel(
                    state    = state,
                    pColor   = pColor,
                    canRoll  = canRoll,
                    onRoll   = onRollResult,
                    onQuit   = onQuit
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val boardDp = (config.screenWidthDp - 16).dp
                BoardCanvas(
                    players          = state.players,
                    playablePieceIds = if (state.waitingForChoice) state.playablePieceIds
                                       else if (state.playablePieceIds.size == 1 && !state.animating)
                                            state.playablePieceIds else emptyList(),
                    boardSize        = boardDp,
                    onPieceClick     = onPieceClick
                )
                GameSidePanel(
                    state    = state,
                    pColor   = pColor,
                    canRoll  = canRoll,
                    onRoll   = onRollResult,
                    onQuit   = onQuit,
                    compact  = true
                )
            }
        }
    }
}

@Composable
private fun GameSidePanel(
    state:   GameState,
    pColor:  Color,
    canRoll: Boolean,
    onRoll:  (Int) -> Unit,
    onQuit:  () -> Unit,
    compact: Boolean = false
) {
    val player = state.currentPlayer
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Badge joueur actuel ──────────────────────────────
        if (player != null) {
            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = pColor.copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, pColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(player.color.emoji, fontSize = 26.sp)
                    Column {
                        Text(player.name, fontFamily = FontFamily.Monospace,
                             fontWeight = FontWeight.Bold, fontSize = 13.sp, color = pColor)
                        Text(
                            "${if (player.type == PlayerType.AI) "IA · ${player.aiLevel}" else "Humain"} · Tour ${state.totalTurns + 1}",
                            fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                            color = LudoColors.TextSub
                        )
                    }
                }
            }
        }

        // ── Message ─────────────────────────────────────────
        if (state.message.isNotEmpty()) {
            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = LudoColors.BgCard,
                border = BorderStroke(1.dp, LudoColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    state.message,
                    modifier   = Modifier.padding(10.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp,
                    color      = LudoColors.Primary,
                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // ── Dé ──────────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(12.dp),
            color  = LudoColors.BgCard,
            border = BorderStroke(1.dp, LudoColors.Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp),
                   horizontalAlignment = Alignment.CenterHorizontally) {
                DiceView(
                    currentValue = state.dice,
                    playerColor  = pColor,
                    canRoll      = canRoll,
                    onRollResult = onRoll
                )
            }
        }

        // ── Scores ──────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(12.dp),
            color  = LudoColors.BgCard,
            border = BorderStroke(1.dp, LudoColors.Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp),
                   verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SCORES", fontFamily = FontFamily.Monospace,
                     fontSize = 10.sp, color = LudoColors.TextSub, letterSpacing = 1.sp)
                for (pl in state.players) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${pl.color.emoji} ${pl.name.take(8)}",
                             fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                             color = pl.color.composeColor(), modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(4) { i ->
                                Box(modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (i < pl.score) pl.color.composeColor()
                                                else LudoColors.Border))
                            }
                        }
                    }
                }
            }
        }

        // ── Historique ──────────────────────────────────────
        if (!compact) {
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = Color(0xFF08080F),
                border   = BorderStroke(1.dp, LudoColors.Border),
                modifier = Modifier.fillMaxWidth().height(140.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("HISTORIQUE", fontFamily = FontFamily.Monospace,
                         fontSize = 10.sp, color = LudoColors.TextSub,
                         letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                        items(state.history.reversed()) { h ->
                            Text("${h.color.emoji} ${h.action}",
                                 fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                                 color = h.color.composeColor().copy(alpha = 0.85f),
                                 lineHeight = 16.sp)
                        }
                    }
                }
            }
        }

        // ── Quitter ─────────────────────────────────────────
        OutlinedButton(
            onClick  = onQuit,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = LudoColors.TextSub),
            border   = BorderStroke(1.dp, LudoColors.Border)
        ) {
            Text("↩ Menu", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
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
            .background(Brush.verticalGradient(
                listOf(LudoColors.BgDeep, LudoColors.BgDark, Color(0xFF130A1A))
            ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Titre ────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text("🏆", fontSize = 60.sp)
            Text("FIN DE PARTIE", fontSize = 24.sp,
                 fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                 color = LudoColors.Primary)
            Text("Partie terminée en $totalTurns tours",
                 fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                 color = LudoColors.TextSub)
            if (isRecord) {
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = LudoColors.Primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LudoColors.Primary)
                ) {
                    Text("🏅 NOUVEAU RECORD !",
                         modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                         fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                         fontWeight = FontWeight.Bold, color = LudoColors.Primary)
                }
            }

            // ── Podium visuel ────────────────────────────────
            val podiumOrder = listOfNotNull(sorted.getOrNull(1), sorted.getOrNull(0), sorted.getOrNull(2))
            val heights     = listOf(100.dp, 140.dp, 70.dp)
            val medals      = listOf("🥈","🥇","🥉")
            val barColors   = listOf(Color(0xFFC0C0C0), LudoColors.Primary, Color(0xFFCD7F32))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment     = Alignment.Bottom,
                modifier              = Modifier.fillMaxWidth().height(220.dp)
            ) {
                podiumOrder.forEachIndexed { vi, pl ->
                    val targetH by animateDpAsState(
                        targetValue   = heights[vi],
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                                               stiffness = Spring.StiffnessLow),
                        label = "barH$vi"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.width(90.dp)
                    ) {
                        Text(pl.color.emoji, fontSize = 22.sp)
                        Text(pl.name.take(9), fontFamily = FontFamily.Monospace,
                             fontSize = 11.sp, fontWeight = FontWeight.Bold,
                             color = pl.color.composeColor())
                        Text(medals[vi], fontSize = 20.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(targetH)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barColors[vi].copy(alpha = 0.8f)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text("#${pl.rank}", modifier = Modifier.padding(top = 6.dp),
                                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                                 fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // ── Stats ────────────────────────────────────────
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = LudoColors.BgCard,
                border = BorderStroke(1.dp, LudoColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp),
                       verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 STATISTIQUES", fontFamily = FontFamily.Monospace,
                         fontSize = 11.sp, fontWeight = FontWeight.Bold,
                         color = LudoColors.TextSub, letterSpacing = 1.sp)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Joueur","Pions","Capt.","Subies").forEach { h ->
                            Text(h, modifier = Modifier.weight(1f),
                                 fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                                 color = LudoColors.TextDim,
                                 textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    HorizontalDivider(color = LudoColors.Border)
                    sorted.forEach { pl ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("${pl.color.emoji} ${pl.name.take(7)}",
                                 modifier = Modifier.weight(1f),
                                 fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                                 color = pl.color.composeColor())
                            listOf("${pl.piecesArrived}/4", "${pl.captures}", "${pl.capturesSuffered}")
                                .forEach { v ->
                                    Text(v, modifier = Modifier.weight(1f),
                                         fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                                         color = LudoColors.TextMain,
                                         textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                        }
                    }
                }
            }

            // ── Boutons ──────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = onReplay,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = LudoColors.Primary,
                        contentColor   = LudoColors.BgDark
                    )
                ) {
                    Text("🔄  Rejouer", fontFamily = FontFamily.Monospace,
                         fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick  = onMenu,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = LudoColors.TextMain),
                    border   = BorderStroke(1.dp, LudoColors.Border)
                ) {
                    Text("🏠  Menu", fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
