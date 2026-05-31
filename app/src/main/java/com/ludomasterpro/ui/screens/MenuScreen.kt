package com.ludomasterpro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.engine.*
import com.ludomasterpro.ui.theme.LudoColors
import androidx.compose.animation.animateColorAsState
fun PieceColor.composeColor() = Color(android.graphics.Color.parseColor(hex))
fun PieceColor.composeDarkColor() = Color(android.graphics.Color.parseColor(hexDark))

@Composable
fun MenuScreen(
    nbPlayers:    Int,
    configs:      List<PlayerConfig>,
    bestScores:   Map<String, Int>,
    onNbChange:   (Int) -> Unit,
    onConfig:     (Int, PlayerConfig) -> Unit,
    onStart:      () -> Unit
) {
    // Animation titre
    val titleColor by rememberInfiniteTransition(label = "title").animateColor(
        initialValue  = LudoColors.Primary,
        targetValue   = Color(0xFFFF6B6B),
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "titleColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(LudoColors.BgDeep, LudoColors.BgDark, LudoColors.BgDeep)
            ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Titre ──────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                   modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text("🎲 LUDO MASTER 🎲", fontSize = 26.sp,
                     fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                     color = titleColor)
                Text("— Pro Edition —", fontSize = 12.sp,
                     fontFamily = FontFamily.Monospace, color = LudoColors.TextSub)
            }

            // ── Nombre de joueurs ──────────────────────────────
            LudoCard(title = "Nombre de joueurs") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (n in 2..4) {
                        val active = n == nbPlayers
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) LudoColors.Primary.copy(alpha=0.15f)
                                            else Color.Transparent)
                                .border(1.5.dp,
                                        if (active) LudoColors.Primary else LudoColors.Border,
                                        RoundedCornerShape(10.dp))
                                .clickable { onNbChange(n) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$n", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                 fontFamily = FontFamily.Monospace,
                                 color = if (active) LudoColors.Primary else LudoColors.TextSub)
                        }
                    }
                }
            }

            // ── Config joueurs ─────────────────────────────────
            LudoCard(title = "Configuration des joueurs") {
                val colors = PieceColor.entries
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until nbPlayers) {
                        val cfg   = configs[i]
                        val coul  = colors[i]
                        PlayerConfigRow(
                            color   = coul,
                            config  = cfg,
                            onChange = { onConfig(i, it) }
                        )
                        if (i < nbPlayers - 1)
                            HorizontalDivider(color = LudoColors.Border, thickness = 0.5.dp)
                    }
                }
            }

            // ── Records ────────────────────────────────────────
            if (bestScores.isNotEmpty()) {
                LudoCard(title = "🏅 Records") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        bestScores.entries
                            .sortedBy { it.value }.take(5)
                            .forEach { (name, turns) ->
                                Text("🥇 $name — $turns tours",
                                     fontFamily = FontFamily.Monospace,
                                     fontSize = 12.sp,
                                     color = LudoColors.Primary)
                            }
                    }
                }
            }

            // ── Règles rapides ─────────────────────────────────
            LudoCard {
                Text(
                    text = "🎯 Faites arriver vos 4 pions au centre\n" +
                           "🎲 Obtenez 6 pour sortir un pion & rejouer\n" +
                           "★ Cases étoile = protection des captures\n" +
                           "💥 Atterrir sur un adversaire → renvoie en base",
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp,
                    color      = LudoColors.TextSub,
                    lineHeight = 20.sp
                )
            }

            // ── Bouton JOUER ───────────────────────────────────
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LudoColors.Primary,
                    contentColor   = LudoColors.BgDark
                )
            ) {
                Text("▶   JOUER", fontSize = 18.sp,
                     fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Ligne de config d'un joueur ──────────────────────────────
@Composable
fun PlayerConfigRow(
    color:    PieceColor,
    config:   PlayerConfig,
    onChange: (PlayerConfig) -> Unit
) {
    val pColor = color.composeColor()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Couleur + Nom
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${color.emoji} ${color.label}",
                 fontFamily = FontFamily.Monospace,
                 fontWeight = FontWeight.Bold,
                 fontSize   = 13.sp,
                 color      = pColor,
                 modifier   = Modifier.width(90.dp))
            OutlinedTextField(
                value          = config.name,
                onValueChange  = { onChange(config.copy(name = it)) },
                singleLine     = true,
                modifier       = Modifier.weight(1f),
                textStyle      = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = LudoColors.TextMain
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = pColor,
                    unfocusedBorderColor = pColor.copy(alpha = 0.4f),
                    cursorColor          = pColor
                )
            )
        }

        // IA toggle + niveau
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("IA", fontFamily = FontFamily.Monospace,
                 fontSize = 12.sp, color = LudoColors.TextSub)
            Switch(
                checked  = config.type == PlayerType.AI,
                onCheckedChange = { onChange(config.copy(type = if (it) PlayerType.AI else PlayerType.HUMAN)) },
                colors   = SwitchDefaults.colors(
                    checkedThumbColor   = pColor,
                    checkedTrackColor   = color.composeDarkColor(),
                    uncheckedThumbColor = LudoColors.TextDim,
                    uncheckedTrackColor = LudoColors.Border
                )
            )
            if (config.type == PlayerType.AI) {
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(AiLevel.EASY to "😊", AiLevel.NORMAL to "🤖", AiLevel.EXPERT to "🧠")
                        .forEach { (lvl, emoji) ->
                            val active = config.aiLevel == lvl
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) pColor.copy(alpha=0.2f) else Color.Transparent)
                                    .border(1.dp,
                                            if (active) pColor else LudoColors.Border,
                                            RoundedCornerShape(8.dp))
                                    .clickable { onChange(config.copy(aiLevel = lvl)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 16.sp)
                            }
                        }
                }
            }
        }
    }
}

// ── Card réutilisable ─────────────────────────────────────────
@Composable
fun LudoCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = LudoColors.BgCard,
        border = BorderStroke(1.dp, LudoColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp),
               verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (title != null) {
                Text(title.uppercase(),
                     fontFamily = FontFamily.Monospace,
                     fontSize   = 11.sp,
                     fontWeight = FontWeight.Bold,
                     color      = LudoColors.TextSub,
                     letterSpacing = 1.sp)
            }
            content()
        }
    }
}
