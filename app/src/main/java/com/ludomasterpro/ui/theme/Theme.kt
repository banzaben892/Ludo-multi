package com.ludomasterpro.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ──────────────────────────────────────────────────
object LudoColors {
    val BgDeep       = Color(0xFF080810)
    val BgDark       = Color(0xFF0D0D1A)
    val BgCard       = Color(0xFF13132A)
    val BgBoard      = Color(0xFF0A2240)
    val Primary      = Color(0xFFFFD700)
    val PrimaryDim   = Color(0xFFE6C200)
    val Accent       = Color(0xFFE94560)
    val TextMain     = Color(0xFFFFFFFF)
    val TextSub      = Color(0xFF888899)
    val TextDim      = Color(0xFF444466)
    val Border       = Color(0xFF222244)
    val SafeCell     = Color(0xFF1A3A6A)
    val PathCell     = Color(0xFF1A3A6A)

    val Red          = Color(0xFFE53935)
    val RedDark      = Color(0xFFB71C1C)
    val Blue         = Color(0xFF1E88E5)
    val BlueDark     = Color(0xFF0D47A1)
    val Green        = Color(0xFF43A047)
    val GreenDark    = Color(0xFF1B5E20)
    val Yellow       = Color(0xFFFDD835)
    val YellowDark   = Color(0xFFF9A825)
}

private val DarkColorScheme = darkColorScheme(
    primary          = LudoColors.Primary,
    onPrimary        = LudoColors.BgDark,
    secondary        = LudoColors.Accent,
    background       = LudoColors.BgDark,
    surface          = LudoColors.BgCard,
    onBackground     = LudoColors.TextMain,
    onSurface        = LudoColors.TextMain,
    outline          = LudoColors.Border,
)

@Composable
fun LudoMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = LudoTypography,
        content     = content
    )
}

val LudoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        color      = LudoColors.Primary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize   = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize   = 11.sp,
        color      = LudoColors.TextSub
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize   = 10.sp,
        color      = LudoColors.TextDim
    )
)
