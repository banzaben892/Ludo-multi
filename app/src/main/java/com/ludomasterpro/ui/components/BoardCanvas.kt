package com.ludomasterpro.ui.components

// ═══════════════════════════════════════════════════════════════
//  LUDO MASTER PRO — BoardCanvas redesign (fidèle à la ref UI)
//  Plateau : zones maison arrondies, cases blanches, centre triangles
//  Pions   : cercle plein + anneau intérieur blanc
//  Stats   : nom + % affiché dans chaque zone
// ═══════════════════════════════════════════════════════════════

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludomasterpro.engine.*
import com.ludomasterpro.engine.BoardConstants.SAFE_CELLS

@Composable
fun BoardCanvas(
    players:          List<Player>,
    playablePieceIds: List<String>,
    boardSize:        Dp = 340.dp,
    onPieceClick:     (String) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val allPieces    = players.flatMap { it.pieces }

    // Pulsation des pions jouables
    val pulse by rememberInfiniteTransition(label = "p").animateFloat(
        initialValue  = 0.88f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            tween(550, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulse"
    )

    // Helpers couleur
    fun colorOf(c: PieceColor): Color = when (c) {
        PieceColor.RED    -> Color(0xFFE53935)
        PieceColor.BLUE   -> Color(0xFF1E88E5)
        PieceColor.GREEN  -> Color(0xFF43A047)
        PieceColor.YELLOW -> Color(0xFFFDD835)
    }
    fun colorLight(c: PieceColor): Color = when (c) {
        PieceColor.RED    -> Color(0xFFEF9A9A)
        PieceColor.BLUE   -> Color(0xFF90CAF9)
        PieceColor.GREEN  -> Color(0xFFA5D6A7)
        PieceColor.YELLOW -> Color(0xFFFFF59D)
    }
    fun colorDark(c: PieceColor): Color = when (c) {
        PieceColor.RED    -> Color(0xFFB71C1C)
        PieceColor.BLUE   -> Color(0xFF0D47A1)
        PieceColor.GREEN  -> Color(0xFF1B5E20)
        PieceColor.YELLOW -> Color(0xFFF57F17)
    }

    Canvas(
        modifier = Modifier
            .size(boardSize)
            .pointerInput(playablePieceIds) {
                detectTapGestures { tap ->
                    val cell = size.width / 15f
                    for (piece in allPieces) {
                        if (piece.id !in playablePieceIds) continue
                        val (r, c) = piece.gridCell()
                        val offsets = pieceOffsets(piece, cell)
                        val center  = Offset(c * cell + cell / 2f + offsets.first,
                                             r * cell + cell / 2f + offsets.second)
                        val rad = cell * 0.3f * pulse
                        if ((tap - center).getDistance() <= rad + 12f) {
                            onPieceClick(piece.id); return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        val W    = size.width
        val cell = W / 15f

        fun cx(c: Int) = c * cell + cell / 2f
        fun cy(r: Int) = r * cell + cell / 2f
        fun tl(r: Int, c: Int) = Offset(c * cell, r * cell)
        fun sz() = Size(cell, cell)

        // ════════════════════════════════════════════════════
        //  1. FOND GÉNÉRAL
        // ════════════════════════════════════════════════════
        drawRect(Color(0xFFE8EAF6), size = size)  // fond violet très clair

        // ════════════════════════════════════════════════════
        //  2. ZONES DE MAISON (6×6 coins)
        // ════════════════════════════════════════════════════
        val homeCorners = listOf(
            PieceColor.GREEN  to Offset(0f, 0f),
            PieceColor.RED    to Offset(9 * cell, 0f),
            PieceColor.YELLOW to Offset(0f, 9 * cell),
            PieceColor.BLUE   to Offset(9 * cell, 9 * cell),
        )
        for ((coul, origin) in homeCorners) {
            val s = 6 * cell
            val base = colorOf(coul)
            val dark = colorDark(coul)

            // Fond zone plein
            drawRoundRect(
                color       = base,
                topLeft     = origin,
                size        = Size(s, s),
                cornerRadius = CornerRadius(cell * 0.3f)
            )
            // Ombre intérieure simulée (bordure sombre)
            drawRoundRect(
                color       = dark.copy(alpha = 0.35f),
                topLeft     = origin,
                size        = Size(s, s),
                cornerRadius = CornerRadius(cell * 0.3f),
                style       = Stroke(width = cell * 0.12f)
            )

            // Carré intérieur arrondi (fond plus clair pour les pions)
            val m  = cell * 0.55f
            val is2 = s - m * 2f
            drawRoundRect(
                color       = colorLight(coul).copy(alpha = 0.55f),
                topLeft     = origin + Offset(m, m),
                size        = Size(is2, is2),
                cornerRadius = CornerRadius(cell * 0.25f)
            )
            drawRoundRect(
                color       = dark.copy(alpha = 0.25f),
                topLeft     = origin + Offset(m, m),
                size        = Size(is2, is2),
                cornerRadius = CornerRadius(cell * 0.25f),
                style       = Stroke(width = cell * 0.06f)
            )

            // Nom + % joueur dans la zone
            val playerForZone = players.firstOrNull { it.color == coul }
            if (playerForZone != null) {
                val pct = "${(playerForZone.score * 25)}%"
                val nameLabel = playerForZone.name.uppercase()
                // Position selon coin
                val textR = when (coul) {
                    PieceColor.GREEN  -> 5.5f
                    PieceColor.RED    -> 5.5f
                    PieceColor.YELLOW -> 9.5f
                    PieceColor.BLUE   -> 9.5f
                }
                val textC = when (coul) {
                    PieceColor.GREEN  -> 0f; PieceColor.RED    -> 9f
                    PieceColor.YELLOW -> 0f; PieceColor.BLUE   -> 9f
                }
                val ty = origin.y + (if (coul == PieceColor.GREEN || coul == PieceColor.RED) s - cell * 0.7f else cell * 0.4f)
                val tx = origin.x + s / 2f

                val nameMeas = textMeasurer.measure(
                    AnnotatedString(nameLabel),
                    style = TextStyle(fontSize = (cell * 0.38f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White)
                )
                drawText(nameMeas, topLeft = Offset(tx - nameMeas.size.width / 2f,
                    if (coul == PieceColor.GREEN || coul == PieceColor.RED) ty - nameMeas.size.height - 2f else ty))

                val pctMeas = textMeasurer.measure(
                    AnnotatedString(pct),
                    style = TextStyle(fontSize = (cell * 0.32f).sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White.copy(alpha = 0.9f))
                )
                drawText(pctMeas, topLeft = Offset(tx - pctMeas.size.width / 2f,
                    if (coul == PieceColor.GREEN || coul == PieceColor.RED) ty else ty + nameMeas.size.height + 2f))
            }
        }

        // ════════════════════════════════════════════════════
        //  3. CHEMIN PRINCIPAL (cases blanches / colorées)
        // ════════════════════════════════════════════════════
        for ((i, rc) in BoardConstants.PATH.withIndex()) {
            val (r, c) = rc
            val pos = tl(r, c)

            // Couleur de base : blanc cassé, ou couleur de départ
            var fill = Color(0xFFF5F5F5)
            for ((coul, idx) in BoardConstants.HOME_ENTRY) {
                if (i == idx) fill = colorOf(coul)
            }
            // Couloir final
            for ((coul, cidx) in BoardConstants.FINAL_START) {
                // les cases 52-57, 58-63… correspondent aux couloirs
            }

            drawRoundRect(
                color        = fill,
                topLeft      = pos + Offset(1.5f, 1.5f),
                size         = Size(cell - 3f, cell - 3f),
                cornerRadius = CornerRadius(cell * 0.12f)
            )
            // Bordure légère
            drawRoundRect(
                color        = Color(0xFFBBBBCC),
                topLeft      = pos + Offset(1.5f, 1.5f),
                size         = Size(cell - 3f, cell - 3f),
                cornerRadius = CornerRadius(cell * 0.12f),
                style        = Stroke(1f)
            )

            // Étoile safe
            if (i in SAFE_CELLS) {
                val cc = Offset(cx(c), cy(r))
                val starMeas = textMeasurer.measure(
                    AnnotatedString("★"),
                    style = TextStyle(fontSize = (cell * 0.5f).sp,
                        color = if (fill == Color(0xFFF5F5F5)) Color(0xFFFFD700) else Color.White,
                        fontWeight = FontWeight.Bold)
                )
                drawText(starMeas, topLeft = Offset(cc.x - starMeas.size.width / 2f,
                    cc.y - starMeas.size.height / 2f))
            }
        }

        // ════════════════════════════════════════════════════
        //  4. COULOIRS FINAUX (colorés)
        // ════════════════════════════════════════════════════
        for ((coul, cells) in BoardConstants.CORRIDORS) {
            for ((r, c) in cells) {
                drawRoundRect(
                    color        = colorOf(coul),
                    topLeft      = tl(r, c) + Offset(1.5f, 1.5f),
                    size         = Size(cell - 3f, cell - 3f),
                    cornerRadius = CornerRadius(cell * 0.12f)
                )
            }
        }

        // ════════════════════════════════════════════════════
        //  5. CENTRE — 4 triangles colorés + cercle doré
        // ════════════════════════════════════════════════════
        val (cr, cc2) = BoardConstants.CENTER
        val mid = Offset(cx(cc2), cy(cr))
        val hs  = cell / 2f   // demi-cellule

        // Triangle Rouge (haut)
        drawPath(Path().apply {
            moveTo(mid.x, mid.y)
            lineTo(mid.x - hs, mid.y - hs)
            lineTo(mid.x + hs, mid.y - hs)
            close()
        }, color = colorOf(PieceColor.RED))

        // Triangle Bleu (droite)
        drawPath(Path().apply {
            moveTo(mid.x, mid.y)
            lineTo(mid.x + hs, mid.y - hs)
            lineTo(mid.x + hs, mid.y + hs)
            close()
        }, color = colorOf(PieceColor.BLUE))

        // Triangle Jaune (bas)
        drawPath(Path().apply {
            moveTo(mid.x, mid.y)
            lineTo(mid.x - hs, mid.y + hs)
            lineTo(mid.x + hs, mid.y + hs)
            close()
        }, color = colorOf(PieceColor.YELLOW))

        // Triangle Vert (gauche)
        drawPath(Path().apply {
            moveTo(mid.x, mid.y)
            lineTo(mid.x - hs, mid.y - hs)
            lineTo(mid.x - hs, mid.y + hs)
            close()
        }, color = colorOf(PieceColor.GREEN))

        // Cercle central blanc
        drawCircle(Color.White, radius = cell * 0.32f, center = mid)
        drawCircle(Color(0xFFE0E0E0), radius = cell * 0.32f, center = mid,
            style = Stroke(cell * 0.05f))

        // ════════════════════════════════════════════════════
        //  6. PIONS (cercle plein + anneau blanc intérieur)
        // ════════════════════════════════════════════════════
        val pieceOff = arrayOf(
            Offset(-cell * 0.20f, -cell * 0.20f),
            Offset( cell * 0.20f, -cell * 0.20f),
            Offset(-cell * 0.20f,  cell * 0.20f),
            Offset( cell * 0.20f,  cell * 0.20f),
        )
        val homeOff = arrayOf(
            Offset(-cell * 0.23f, -cell * 0.23f),
            Offset( cell * 0.23f, -cell * 0.23f),
            Offset(-cell * 0.23f,  cell * 0.23f),
            Offset( cell * 0.23f,  cell * 0.23f),
        )

        for (piece in allPieces) {
            val (r, c) = piece.gridCell()
            val off    = if (piece.isAtBase) homeOff[piece.index] else pieceOff[piece.index]
            val center = Offset(cx(c) + off.x, cy(r) + off.y)
            val isPlay = piece.id in playablePieceIds
            val rad    = cell * 0.29f * if (isPlay) pulse else 1f
            val base   = colorOf(piece.color)
            val dark   = colorDark(piece.color)

            // Ombre portée
            drawCircle(dark.copy(alpha = 0.4f), radius = rad + 2f,
                center = center + Offset(2f, 2f))

            // Corps principal avec dégradé simulé (cercle foncé + cercle clair superposé)
            drawCircle(dark, radius = rad, center = center)
            drawCircle(
                brush  = Brush.radialGradient(
                    colors  = listOf(base.copy(alpha = 0.95f), dark),
                    center  = center - Offset(rad * 0.2f, rad * 0.2f),
                    radius  = rad
                ),
                radius = rad * 0.95f,
                center = center
            )

            // Anneau blanc intérieur (signature visuelle de la ref)
            drawCircle(
                color  = Color.White.copy(alpha = 0.85f),
                radius = rad * 0.62f,
                center = center,
                style  = Stroke(width = rad * 0.22f)
            )

            // Halo jouable
            if (isPlay) {
                drawCircle(Color(0xFFFFD700).copy(alpha = 0.35f),
                    radius = rad + cell * 0.15f, center = center)
                drawCircle(Color(0xFFFFD700), radius = rad,
                    center = center, style = Stroke(width = 2.5f))
            }
        }
    }
}

private fun pieceOffsets(piece: Piece, cell: Float): Pair<Float, Float> {
    val off = if (piece.isAtBase) arrayOf(
        -cell * 0.23f to -cell * 0.23f,
         cell * 0.23f to -cell * 0.23f,
        -cell * 0.23f to  cell * 0.23f,
         cell * 0.23f to  cell * 0.23f,
    ) else arrayOf(
        -cell * 0.20f to -cell * 0.20f,
         cell * 0.20f to -cell * 0.20f,
        -cell * 0.20f to  cell * 0.20f,
         cell * 0.20f to  cell * 0.20f,
    )
    return off[piece.index]
}
