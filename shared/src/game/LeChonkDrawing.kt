package dev.dambatenne.lechonk.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** Fraction of the screen height at which the flat ground starts. */
internal const val GroundFraction = 0.8f

/** Background color of the sky above the ground. */
internal val SkyColor = Color(0xFFB3E5FC)

private val GroundColor = Color(0xFF8D6E63)
private val GroundEdgeColor = Color(0xFFA1887F)
private val ObstacleColor = Color(0xFF90A4AE)
private val ObstacleEdgeColor = Color(0xFF607D8B)
private val HeartColor = Color(0xFFE53935)
private val HeartLostColor = Color(0xFFCFD8DC)
private val FurColor = Color(0xFFFFCC80)
private val FurDarkColor = Color(0xFFFFB74D)
private val EyeColor = Color(0xFF3E2723)
private val NoseColor = Color(0xFFE57373)
private val WhiskerColor = Color(0xFFFFFFFF)

/** Draws the flat ground across the bottom of the screen. */
fun DrawScope.drawGround() {
    val groundTop = size.height * GroundFraction
    drawRect(
        color = GroundColor,
        topLeft = Offset(0f, groundTop),
        size = Size(size.width, size.height - groundTop),
    )
    drawRect(
        color = GroundEdgeColor,
        topLeft = Offset(0f, groundTop),
        size = Size(size.width, 6f),
    )
}

/** Draws the obstacles scrolling left toward Le Chonk, sitting on the ground. */
fun DrawScope.drawObstacles(game: LeChonkRunner) {
    // One track unit is this many pixels wide.
    val unit = size.width / game.trackLength
    val groundY = size.height * GroundFraction
    for (obstacle in game.obstacles) {
        val width = obstacle.width * unit
        val height = obstacle.height * unit
        val topLeft = Offset(obstacle.x * unit, groundY - height)
        drawRoundRect(
            color = ObstacleColor,
            topLeft = topLeft,
            size = Size(width, height),
            cornerRadius = CornerRadius(width * 0.25f),
        )
        drawRoundRect(
            color = ObstacleEdgeColor,
            topLeft = topLeft,
            size = Size(width, height),
            cornerRadius = CornerRadius(width * 0.25f),
            style = Stroke(width = unit * 2f),
        )
    }
}

/** Draws the hearts at the top of the screen: one filled heart per heart left. */
fun DrawScope.drawHearts(game: LeChonkRunner) {
    val heartSize = 28.dp.toPx()
    val gap = 8.dp.toPx()
    val rowWidth = game.maxHearts * heartSize + (game.maxHearts - 1) * gap
    val firstCenterX = (size.width - rowWidth) / 2f + heartSize / 2f
    val centerY = 12.dp.toPx() + heartSize / 2f
    for (heart in 0 until game.maxHearts) {
        drawPath(
            path = heartPath(firstCenterX + heart * (heartSize + gap), centerY, heartSize),
            color = if (heart < game.hearts) HeartColor else HeartLostColor,
        )
    }
}

/** A heart shape centered on ([centerX], [centerY]), [size] units tall. */
private fun heartPath(centerX: Float, centerY: Float, size: Float): Path {
    // The classic 24x24 heart icon, scaled to [size] and centered on the heart.
    val scale = size / 24f
    fun iconX(x: Float) = centerX + (x - 12f) * scale
    fun iconY(y: Float) = centerY + (y - 12f) * scale
    return Path().apply {
        moveTo(iconX(12f), iconY(21.35f))
        lineTo(iconX(10.55f), iconY(20.03f))
        cubicTo(iconX(5.4f), iconY(15.36f), iconX(2f), iconY(12.28f), iconX(2f), iconY(8.5f))
        cubicTo(iconX(2f), iconY(5.42f), iconX(4.42f), iconY(3f), iconX(7.5f), iconY(3f))
        cubicTo(iconX(9.24f), iconY(3f), iconX(10.91f), iconY(3.81f), iconX(12f), iconY(5.09f))
        cubicTo(iconX(13.09f), iconY(3.81f), iconX(14.76f), iconY(3f), iconX(16.5f), iconY(3f))
        cubicTo(iconX(19.58f), iconY(3f), iconX(22f), iconY(5.42f), iconX(22f), iconY(8.5f))
        cubicTo(iconX(22f), iconY(12.28f), iconX(18.6f), iconY(15.36f), iconX(13.45f), iconY(20.03f))
        close()
    }
}

/** Draws Le Chonk, the chonky cat, at his current position in the game. */
fun DrawScope.drawLeChonk(game: LeChonkRunner) {
    if (game.isFlashing) {
        // Hit flash: blink for as long as the flash lasts.
        val elapsed = game.hitFlashSeconds - game.hitFlashRemaining
        if ((elapsed * 10f).toInt() % 2 == 1) return
    }

    // One track unit is this many pixels wide; the same scale converts the
    // jump height from track units to pixels.
    val unit = size.width / game.trackLength
    val groundY = size.height * GroundFraction
    val feetY = groundY - game.heightAboveGround * unit

    val bodyHeight = size.height * 0.16f
    val bodyWidth = bodyHeight * 1.4f
    val legHeight = bodyHeight * 0.18f
    val centerX = game.x * unit

    val bodyLeft = centerX - bodyWidth / 2f
    val bodyRight = centerX + bodyWidth / 2f
    val bodyBottom = feetY - legHeight
    val bodyTop = bodyBottom - bodyHeight

    // Tail, curling up behind him.
    drawPath(
        path = Path().apply {
            moveTo(bodyLeft + bodyWidth * 0.08f, bodyTop + bodyHeight * 0.72f)
            quadraticTo(
                bodyLeft - bodyWidth * 0.38f, bodyTop + bodyHeight * 0.62f,
                bodyLeft - bodyWidth * 0.30f, bodyTop + bodyHeight * 0.22f,
            )
        },
        color = FurDarkColor,
        style = Stroke(width = bodyHeight * 0.12f, cap = StrokeCap.Round),
    )

    // Body: a chonky round rectangle.
    drawRoundRect(
        color = FurColor,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyHeight * 0.5f),
    )

    // Ears.
    val earHeight = bodyHeight * 0.34f
    drawPath(
        path = Path().apply {
            moveTo(bodyLeft + bodyWidth * 0.22f, bodyTop + bodyHeight * 0.10f)
            lineTo(bodyLeft + bodyWidth * 0.12f, bodyTop - earHeight * 0.55f)
            lineTo(bodyLeft + bodyWidth * 0.36f, bodyTop + bodyHeight * 0.02f)
            close()
        },
        color = FurDarkColor,
    )
    drawPath(
        path = Path().apply {
            moveTo(bodyRight - bodyWidth * 0.22f, bodyTop + bodyHeight * 0.10f)
            lineTo(bodyRight - bodyWidth * 0.12f, bodyTop - earHeight * 0.55f)
            lineTo(bodyRight - bodyWidth * 0.36f, bodyTop + bodyHeight * 0.02f)
            close()
        },
        color = FurDarkColor,
    )

    // Eyes.
    val eyeRadius = bodyHeight * 0.085f
    val eyeY = bodyTop + bodyHeight * 0.40f
    drawCircle(
        color = EyeColor,
        radius = eyeRadius,
        center = Offset(bodyLeft + bodyWidth * 0.32f, eyeY),
    )
    drawCircle(
        color = EyeColor,
        radius = eyeRadius,
        center = Offset(bodyRight - bodyWidth * 0.32f, eyeY),
    )

    // Nose.
    val noseY = bodyTop + bodyHeight * 0.60f
    drawPath(
        path = Path().apply {
            moveTo(centerX - bodyWidth * 0.06f, noseY)
            lineTo(centerX + bodyWidth * 0.06f, noseY)
            lineTo(centerX, noseY + bodyHeight * 0.08f)
            close()
        },
        color = NoseColor,
    )

    // Whiskers.
    val whiskerY = noseY + bodyHeight * 0.03f
    val whiskerInset = bodyWidth * 0.10f
    val whiskerOutset = bodyWidth * 0.30f
    val whiskerWidth = bodyHeight * 0.02f
    drawLine(
        color = WhiskerColor,
        start = Offset(bodyLeft + whiskerInset, whiskerY),
        end = Offset(bodyLeft - whiskerOutset, whiskerY - bodyHeight * 0.04f),
        strokeWidth = whiskerWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = WhiskerColor,
        start = Offset(bodyLeft + whiskerInset, whiskerY + bodyHeight * 0.05f),
        end = Offset(bodyLeft - whiskerOutset, whiskerY + bodyHeight * 0.05f),
        strokeWidth = whiskerWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = WhiskerColor,
        start = Offset(bodyRight - whiskerInset, whiskerY),
        end = Offset(bodyRight + whiskerOutset, whiskerY - bodyHeight * 0.04f),
        strokeWidth = whiskerWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = WhiskerColor,
        start = Offset(bodyRight - whiskerInset, whiskerY + bodyHeight * 0.05f),
        end = Offset(bodyRight + whiskerOutset, whiskerY + bodyHeight * 0.05f),
        strokeWidth = whiskerWidth,
        cap = StrokeCap.Round,
    )

    // Legs, swinging as he runs. They reach from the body down to his feet
    // on the ground, so the running motion stays visible.
    val legWidth = bodyWidth * 0.16f
    val swing = sin(game.distance * 0.08f) * bodyWidth * 0.12f
    drawRoundRect(
        color = FurDarkColor,
        topLeft = Offset(bodyLeft + bodyWidth * 0.28f - legWidth / 2f + swing, bodyBottom),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(legWidth / 2f),
    )
    drawRoundRect(
        color = FurDarkColor,
        topLeft = Offset(bodyRight - bodyWidth * 0.28f - legWidth / 2f - swing, bodyBottom),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(legWidth / 2f),
    )
}
