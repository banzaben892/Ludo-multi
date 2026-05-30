package com.ludomasterpro.ui.components

// ═══════════════════════════════════════════════════════════════
//  LUDO MASTER PRO — BoardCanvas (Jetpack Compose Canvas)
// ═══════════════════════════════════════════════════════════════

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.engine.*
import com.ludomasterpro.engine.BoardConstants.SAFE_CELLS
import com.ludomasterpro.ui.theme.LudoColors

@Composable
fun BoardCanvas(
    players:          List<Player>,
    playablePieceIds: List<String>,
    boardSize:        Dp = 340.dp,
    onPieceClick:     (String) -> Unit
) {
    val density = LocalDensity.current
    val sizePx  = with(density) { boardSize.toPx() }
    val cell    = sizePx / 15f

    // Animation de pulsation pour les pions jouables
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    val textMeasurer = rememberTextMeasurer()
    val allPieces    = players.flatMap { it.pieces }

    // Map pour accès rapide couleur → valeurs
    fun colorOf(c: PieceColor) = when(c) {
        PieceColor.RED    -> LudoColors.Red
        PieceColor.BLUE   -> LudoColors.Blue
        PieceColor.GREEN  -> LudoColors.Green
        PieceColor.YELLOW -> LudoColors.Yellow
    }
    fun colorDarkOf(c: PieceColor) = when(c) {
        PieceColor.RED    -> LudoColors.RedDark
        PieceColor.BLUE   -> LudoColors.BlueDark
        PieceColor.GREEN  -> LudoColors.GreenDark
        PieceColor.YELLOW -> LudoColors.YellowDark
    }

    fun cellTL(r: Int, c: Int) = Offset(c * cell, r * cell)
    fun cellCenter(r: Int, c: Int) = Offset(c * cell + cell / 2f, r * cell + cell / 2f)

    Canvas(
        modifier = Modifier
            .size(boardSize)
            .pointerInput(playablePieceIds) {
                detectTapGestures { tapOffset ->
                    // Trouver quel pion a été tapé
                    for (piece in allPieces) {
                        if (piece.id !in playablePieceIds) continue
                        val (r, c) = piece.gridCell()
                        val offsets = if (piece.isAtBase) arrayOf(
                            Offset(-cell*0.22f, -cell*0.22f), Offset(cell*0.22f, -cell*0.22f),
                            Offset(-cell*0.22f,  cell*0.22f), Offset(cell*0.22f,  cell*0.22f)
                        ) else arrayOf(
                            Offset(-cell*0.18f, -cell*0.18f), Offset(cell*0.18f, -cell*0.18f),
                            Offset(-cell*0.18f,  cell*0.18f), Offset(cell*0.18f,  cell*0.18f)
                        )
                        val (dx, dy) = offsets[piece.index]
                        val center = cellCenter(r, c) + Offset(dx, dy)
                        val radius = cell * 0.28f * if (piece.id in playablePieceIds) pulse else 1f
                        if ((tapOffset - center).getDistance() <= radius + 10f) {
                            onPieceClick(piece.id)
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        // ── Fond ────────────────────────────────────────────────
        drawRect(color = LudoColors.BgBoard, size = Size(sizePx, sizePx))

        // ── Zones de maison ─────────────────────────────────────
        val homeZones = listOf(
            PieceColor.YELLOW to (0 to 0),
            PieceColor.BLUE   to (0 to 9),
            PieceColor.RED    to (9 to 0),
            PieceColor.GREEN  to (9 to 9),
        )
        for ((coul, rc) in homeZones) {
            val (r0, c0) = rc
            val x = c0 * cell; val y = r0 * cell
            val s = 6 * cell
            // Fond de zone
            drawRect(color = colorDarkOf(coul), topLeft = Offset(x, y), size = Size(s, s))
            // Cercle intérieur
            val m = 10f
            drawCircle(
                color  = colorOf(coul),
                radius = s / 2f - m,
                center = Offset(x + s/2f, y + s/2f)
            )
            drawCircle(
                color       = colorDarkOf(coul),
                radius      = s / 2f - m,
                center      = Offset(x + s/2f, y + s/2f),
                style       = Stroke(width = 3f)
            )
        }

        // ── Chemin principal ─────────────────────────────────────
        for ((i, rc) in BoardConstants.PATH.withIndex()) {
            val (r, c) = rc
            val tl = cellTL(r, c)
            var fill = LudoColors.PathCell
            // Coloration cases de départ
            for ((coul, idx) in BoardConstants.HOME_ENTRY) {
                if (i == idx) fill = colorOf(coul)
            }
            drawRect(color = fill, topLeft = tl, size = Size(cell, cell))
            drawRect(
                color = Color(0xFF0A1A40),
                topLeft = tl, size = Size(cell, cell),
                style = Stroke(width = 0.8f)
            )
            // Étoile cases sûres
            if (i in SAFE_CELLS) {
                val cc = cellCenter(r, c)
                drawCircle(color = LudoColors.Primary.copy(alpha = 0.25f), radius = cell * 0.35f, center = cc)
                val starText = textMeasurer.measure(
                    AnnotatedString("★"),
                    style = TextStyle(fontSize = (cell * 0.4f).sp, color = LudoColors.Primary)
                )
                drawText(starText, topLeft = Offset(cc.x - starText.size.width/2f, cc.y - starText.size.height/2f))
            }
        }

        // ── Couloirs finaux ──────────────────────────────────────
        for ((coul, cells) in BoardConstants.CORRIDORS) {
            for ((r, c) in cells) {
                drawRect(
                    color    = colorOf(coul),
                    topLeft  = cellTL(r, c),
                    size     = Size(cell, cell)
                )
                drawRect(
                    color    = Color(0xFF0A1A40),
                    topLeft  = cellTL(r, c),
                    size     = Size(cell, cell),
                    style    = Stroke(width = 0.8f)
                )
            }
        }

        // ── Centre (étoile 4 triangles + cercle doré) ────────────
        val (cr, cc) = BoardConstants.CENTER
        val cx0 = cc * cell; val cy0 = cr * cell
        val mid = Offset(cx0 + cell/2f, cy0 + cell/2f)
        // 4 triangles colorés
        val triDefs = listOf(
            PieceColor.RED    to listOf(mid, Offset(cx0, cy0+cell), Offset(cx0+cell, cy0+cell)),
            PieceColor.BLUE   to listOf(mid, Offset(cx0, cy0),       Offset(cx0, cy0+cell)),
            PieceColor.GREEN  to listOf(mid, Offset(cx0, cy0),       Offset(cx0+cell, cy0)),
            PieceColor.YELLOW to listOf(mid, Offset(cx0+cell, cy0),  Offset(cx0+cell, cy0+cell)),
        )
        for ((coul, pts) in triDefs) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(pts[0].x, pts[0].y)
                lineTo(pts[1].x, pts[1].y)
                lineTo(pts[2].x, pts[2].y)
                close()
            }
            drawPath(path, color = colorOf(coul))
        }
        drawCircle(color = LudoColors.Primary, radius = cell * 0.38f, center = mid)
        drawCircle(
            color  = LudoColors.PrimaryDim,
            radius = cell * 0.38f, center = mid,
            style  = Stroke(width = 1.5f)
        )
        // Étoile au centre
        val starMeas = textMeasurer.measure(
            AnnotatedString("★"),
            style = TextStyle(fontSize = (cell * 0.38f).sp,
                color = LudoColors.BgDark, fontWeight = FontWeight.Bold)
        )
        drawText(starMeas, topLeft = Offset(mid.x - starMeas.size.width/2f, mid.y - starMeas.size.height/2f))

        // ── Pions ────────────────────────────────────────────────
        val pieceOffsets = arrayOf(
            Offset(-cell*0.18f, -cell*0.18f), Offset(cell*0.18f, -cell*0.18f),
            Offset(-cell*0.18f,  cell*0.18f), Offset(cell*0.18f,  cell*0.18f)
        )
        val homeOffsets = arrayOf(
            Offset(-cell*0.22f, -cell*0.22f), Offset(cell*0.22f, -cell*0.22f),
            Offset(-cell*0.22f,  cell*0.22f), Offset(cell*0.22f,  cell*0.22f)
        )

        for (piece in allPieces) {
            val (r, c) = piece.gridCell()
            val offArr = if (piece.isAtBase) homeOffsets else pieceOffsets
            val (dx, dy) = offArr[piece.index]
            val center = cellCenter(r, c) + Offset(dx, dy)
            val isPlayable = piece.id in playablePieceIds
            val radius = cell * 0.28f * if (isPlayable) pulse else 1f

            // Halo pulsant
            if (isPlayable) {
                drawCircle(
                    color  = LudoColors.Primary.copy(alpha = 0.30f),
                    radius = radius + cell * 0.14f,
                    center = center
                )
            }

            // Corps du pion
            drawCircle(color = colorOf(piece.color), radius = radius, center = center)
            drawCircle(
                color  = if (isPlayable) LudoColors.Primary else colorDarkOf(piece.color),
                radius = radius, center = center,
                style  = Stroke(width = if (isPlayable) 2.5f else 1.5f)
            )

            // Numéro du pion
            val numColor = if (piece.color == PieceColor.YELLOW) Color.Black else Color.White
            val numMeas = textMeasurer.measure(
                AnnotatedString("${piece.index + 1}"),
                style = TextStyle(
                    fontSize   = (radius * 0.82f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color      = numColor
                )
            )
            drawText(numMeas,
                topLeft = Offset(center.x - numMeas.size.width/2f,
                                 center.y - numMeas.size.height/2f))
        }
    }
}
