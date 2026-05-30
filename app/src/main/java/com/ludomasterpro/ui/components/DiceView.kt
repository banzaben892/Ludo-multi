package com.ludomasterpro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.ui.theme.LudoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

val DICE_FACES = listOf("⚀","⚁","⚂","⚃","⚄","⚅")

@Composable
fun DiceView(
    currentValue:  Int,
    playerColor:   Color,
    canRoll:       Boolean,
    onRollResult:  (Int) -> Unit
) {
    val haptic  = LocalHapticFeedback.current
    val scope   = rememberCoroutineScope()
    var display by remember { mutableIntStateOf(currentValue.coerceIn(1, 6)) }
    var rolling by remember { mutableStateOf(false) }

    // Animation scale / rotation
    val scale   by animateFloatAsState(
        targetValue    = if (rolling) 1.1f else 1f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label          = "diceScale"
    )
    val rotation by animateFloatAsState(
        targetValue   = if (rolling) 15f else 0f,
        animationSpec = tween(80, easing = FastOutSlowInEasing),
        label         = "diceRot"
    )

    fun roll() {
        if (!canRoll || rolling) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        rolling = true
        scope.launch {
            repeat(12) {
                display = Random.nextInt(1, 7)
                delay(55 + it * 8L)
            }
            val result = Random.nextInt(1, 7)
            display = result
            rolling = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRollResult(result)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Face du dé ────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
                .rotate(rotation)
                .clip(RoundedCornerShape(14.dp))
                .background(if (canRoll) LudoColors.BgCard else Color(0xFF0A0A18))
                .border(
                    width = if (canRoll) 2.dp else 1.dp,
                    color = if (canRoll) playerColor else LudoColors.Border,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(enabled = canRoll && !rolling) { roll() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = DICE_FACES[display - 1],
                fontSize = 42.sp
            )
        }

        // ── Valeur numérique ──────────────────────────────────
        if (currentValue > 0 && !rolling) {
            Text(
                text       = "$currentValue",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color      = playerColor
            )
        }

        // ── Bouton lancer ─────────────────────────────────────
        Button(
            onClick  = { roll() },
            enabled  = canRoll && !rolling,
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (canRoll) playerColor else LudoColors.Border,
                contentColor   = LudoColors.BgDark,
                disabledContainerColor = LudoColors.Border,
                disabledContentColor   = LudoColors.TextDim
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text       = if (rolling) "…" else "🎲  Lancer",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        }
    }
}
