package com.ludomasterpro.ui.components

// ═══════════════════════════════════════════════════════════════
//  DiceView — Bouton dé style "fleur dorée" + stats X/💀
// ═══════════════════════════════════════════════════════════════

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════
//  CONSTANTES DES FACES DU DÉ (CORRECTION BUG)
// ═══════════════════════════════════════════════════════════════

private val DICE_FACES = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

// Alternative avec des émojis (si vous préférez) :
// private val DICE_FACES = listOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣")

@Composable
fun DiceAreaRow(
    currentValue:  Int,
    playerCaptures: Int,
    playerSuffered: Int,
    canRoll:       Boolean,
    onRollResult:  (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope  = rememberCoroutineScope()
    var display by remember { mutableIntStateOf(currentValue.coerceIn(1, 6)) }
    var rolling by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (rolling) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "diceScale"
    )
    val rotation by animateFloatAsState(
        targetValue   = if (rolling) 18f else 0f,
        animationSpec = tween(80),
        label         = "diceRot"
    )

    fun roll() {
        if (!canRoll || rolling) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        rolling = true
        scope.launch {
            repeat(14) {
                display = Random.nextInt(1, 7)
                delay(50 + it * 6L)
            }
            val result = Random.nextInt(1, 7)
            display = result
            rolling = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRollResult(result)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Stats gauche : ✗ captures / 💀 subies ────────────
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("✕", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$playerCaptures", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("☠", fontSize = 16.sp, color = Color.White)
                Text("$playerSuffered", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // ── Bouton dé doré arrondi ────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
                .rotate(rotation)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = if (canRoll)
                            listOf(Color(0xFFFFE066), Color(0xFFFFA000))
                        else
                            listOf(Color(0xFF888888), Color(0xFF555555))
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFF8F00))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(enabled = canRoll && !rolling) { roll() },
            contentAlignment = Alignment.Center
        ) {
            // Motif "fleur" / dé — on utilise les unicode des dés
            Text(
                text     = DICE_FACES[display - 1],
                fontSize = 40.sp,
                color    = Color.White
            )
        }

        // ── Indicateur "à toi" ────────────────────────────────
        if (canRoll && !rolling) {
            Text("👉", fontSize = 28.sp)
        }
    }
}
