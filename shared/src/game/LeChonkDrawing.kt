package dev.dambatenne.lechonk.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

/** Fraction of the screen height at which the flat ground sits. */
internal const val GroundFraction = 0.8f

internal val SkyColor = Color(0xFFB3E5FC)
private val GroundColor = Color(0xFF8D6E63)
private val GroundHighlightColor = Color(0xFFA1887F)
private val ChonkBodyColor = Color(0xFFFFB74D)
private val ChonkEarColor = Color(0xFFFFB74D)
private val ChonkLegColor = Color(0xFFFFA726)
private val ChonkTailColor = Color(0xFFFFA726)
private val EyeWhiteColor = Color(0xFFFFFFFF)
private val EyePupilColor = Color(0xFF212121)
private val NoseColor = Color(0xFFE57373)
private val MouthColor = Color(0xFF6D4C41)

/** Draws the flat ground across the bottom of the screen. */
fun DrawScope.drawGround() {
    val groundY = size.height * GroundFraction
    drawRect(
        color = GroundColor,
        topLeft = Offset(0f, groundY),
        size = Size(size.width, size.height - groundY),
    )
    drawRect(
        color = GroundHighlightColor,
        topLeft = Offset(0f, groundY),
        size = Size(size.width, 8f),
    )
}

/** Draws Le Chonk, the chonky cat, at his current position in the game. */
fun DrawScope.drawLeChonk(game: LeChonkRunner) {
    val scale = size.width / game.trackLength
    val groundY = size.height * GroundFraction
    val feetY = groundY - game.heightAboveGround * scale

    val bodyHeight = size.height * 0.16f
    val bodyWidth = bodyHeight * 1.5f
    val centerX = game.x * scale
    val bodyLeft = centerX - bodyWidth / 2f
    val bodyRight = centerX + bodyWidth / 2f
    val bodyTop = feetY - bodyHeight
    val bodyBottom = feetY

    // Tail trailing behind him.
    drawPath(
        path = Path().apply {
            moveTo(bodyLeft + bodyWidth * 0.05f, bodyTop + bodyHeight * 0.55f)
            cubicTo(
                bodyLeft - bodyWidth * 0.28f, bodyTop + bodyHeight * 0.50f,
                bodyLeft - bodyWidth * 0.32f, bodyTop + bodyHeight * 0.18f,
                bodyLeft - bodyWidth * 0.14f, bodyTop + bodyHeight * 0.06f,
            )
        },
        color = ChonkTailColor,
        style = Stroke(width = bodyHeight * 0.10f, cap = StrokeCap.Round),
    )

    // Legs, swinging as he runs.
    val legWidth = bodyWidth * 0.18f
    val legHeight = bodyHeight * 0.20f
    val swing = sin(game.distance * 0.08f) * bodyWidth * 0.10f
    drawRoundRect(
        color = ChonkLegColor,
        topLeft = Offset(bodyLeft + bodyWidth * 0.24f - legWidth / 2f + swing, bodyBottom - legHeight),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(legWidth / 2f),
    )
    drawRoundRect(
        color = ChonkLegColor,
        topLeft = Offset(bodyRight - bodyWidth * 0.24f - legWidth / 2f - swing, bodyBottom - legHeight),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(legWidth / 2f),
    )

    // Body.
    drawRoundRect(
        color = ChonkBodyColor,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyHeight * 0.45f),
    )

    // Ears.
    val earHeight = bodyHeight * 0.30f
    drawPath(
        path = Path().apply {
            moveTo(bodyLeft + bodyWidth * 0.18f, bodyTop + bodyHeight * 0.10f)
            lineTo(bodyLeft + bodyWidth * 0.10f, bodyTop - earHeight * 0.55f)
            lineTo(bodyLeft + bodyWidth * 0.32f, bodyTop + bodyHeight * 0.02f)
            close()
        },
        color = ChonkEarColor,
    )
    drawPath(
        path = Path().apply {
            moveTo(bodyRight - bodyWidth * 0.18f, bodyTop + bodyHeight * 0.10f)
            lineTo(bodyRight - bodyWidth * 0.10f, bodyTop - earHeight * 0.55f)
            lineTo(bodyRight - bodyWidth * 0.32f, bodyTop + bodyHeight * 0.02f)
            close()
        },
        color = ChonkEarColor,
    )

    // Eyes.
    val eyeRadius = bodyHeight * 0.085f
    val eyeY = bodyTop + bodyHeight * 0.40f
    val leftEyeX = bodyLeft + bodyWidth * 0.34f
    val rightEyeX = bodyRight - bodyWidth * 0.34f
    drawCircle(color = EyeWhiteColor, radius = eyeRadius * 1.15f, center = Offset(leftEyeX, eyeY))
    drawCircle(color = EyeWhiteColor, radius = eyeRadius * 1.15f, center = Offset(rightEyeX, eyeY))
    drawCircle(color = EyePupilColor, radius = eyeRadius * 0.55f, center = Offset(leftEyeX, eyeY))
    drawCircle(color = EyePupilColor, radius = eyeRadius * 0.55f, center = Offset(rightEyeX, eyeY))

    // Nose and mouth.
    val noseY = bodyTop + bodyHeight * 0.58f
    drawPath(
        path = Path().apply {
            moveTo(centerX - bodyWidth * 0.05f, noseY)
            lineTo(centerX + bodyWidth * 0.05f, noseY)
            lineTo(centerX, noseY + bodyHeight * 0.07f)
            close()
        },
        color = NoseColor,
    )
    drawArc(
        color = MouthColor,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(centerX - bodyWidth * 0.10f, noseY + bodyHeight * 0.02f),
        size = Size(bodyWidth * 0.20f, bodyHeight * 0.12f),
        style = Stroke(width = bodyHeight * 0.025f, cap = StrokeCap.Round),
    )
}
