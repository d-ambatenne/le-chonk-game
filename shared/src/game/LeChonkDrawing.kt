package dev.dambatenne.lechonk.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/** Fraction of the screen height at which the flat ground starts. */
internal const val GroundFraction = 0.8f

/** Background color of the sky above the ground. */
internal val SkyColor = Color(0xFFB3E5FC)

/** Top color of the sky gradient. */
internal val SkyTopColor = Color(0xFF4FC3F7)

/** Bottom color of the sky gradient, just above the ground. */
internal val SkyBottomColor = Color(0xFFE1F5FE)

private val CloudColor = Color(0xFFFFFFFF)
private val MountainColor = Color(0xFF78909C)
private val MountainSnowColor = Color(0xFFECEFF1)
private val HillColor = Color(0xFFA1887F)
private val HillDarkColor = Color(0xFF8D6E63)
private val TreeColor = Color(0xFF2E7D32)
private val TreeTrunkColor = Color(0xFF5D4037)

private val GroundColor = Color(0xFF8D6E63)
private val GroundEdgeColor = Color(0xFFA1887F)
private val ObstacleColor = Color(0xFF90A4AE)
private val ObstacleEdgeColor = Color(0xFF607D8B)
private val HeartColor = Color(0xFFE53935)
private val HeartLostColor = Color(0xFFCFD8DC)

/**
 * Draws the parallax background: the sky gradient, then the clouds,
 * mountains, hills and trees on their own layers, from back to front. Each
 * layer scrolls at its own fraction of the distance Le Chonk has run, so
 * the layers move at different speeds and the scene reads as depth.
 */
fun DrawScope.drawParallaxBackground(background: ParallaxBackground, distanceRun: Float) {
    drawSkyGradient()
    val offsets = background.scrollOffsets(distanceRun)
    for ((layer, offset) in background.layers.zip(offsets)) {
        when (layer.name) {
            "clouds" -> drawClouds(offset, background.trackLength)
            "mountains" -> drawMountains(offset, background.trackLength)
            "hills" -> drawHills(offset, background.trackLength)
            "trees" -> drawTrees(offset, background.trackLength)
        }
    }
}

/** Draws the sky: a vertical gradient from [SkyTopColor] to [SkyBottomColor]. */
private fun DrawScope.drawSkyGradient() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(SkyTopColor, SkyBottomColor),
            startY = 0f,
            endY = size.height * GroundFraction,
        ),
        topLeft = Offset(0f, 0f),
        size = Size(size.width, size.height),
    )
}

/**
 * Draws the clouds layer: soft white ellipses drifting slowly across the
 * sky. The pattern repeats every [period] track units, so the layer is
 * drawn twice, offset by one period, to cover the screen seamlessly.
 */
private fun DrawScope.drawClouds(scrollOffset: Float, period: Float) {
    val unit = size.width / period
    val cloudHeight = size.height * 0.08f
    val cloudWidth = cloudHeight * 2.2f
    val y = size.height * 0.18f
    // Cloud centers in one period of the pattern, in track units.
    val centers = listOf(period * 0.15f, period * 0.55f, period * 0.85f)
    for (shift in listOf(0f, period)) {
        for (center in centers) {
            val centerX = ((center - scrollOffset) * unit).mod(size.width + cloudWidth) - cloudWidth
            drawOval(
                color = CloudColor,
                topLeft = Offset(centerX - cloudWidth / 2f, y - cloudHeight / 2f),
                size = Size(cloudWidth, cloudHeight),
            )
            drawOval(
                color = CloudColor,
                topLeft = Offset(centerX - cloudWidth * 0.3f, y - cloudHeight * 0.9f),
                size = Size(cloudWidth * 0.6f, cloudHeight * 0.8f),
            )
        }
    }
}

/**
 * Draws the mountains layer: grey triangles with snow caps, sitting on the
 * horizon just above the ground. The pattern repeats every [period] track
 * units, so the layer is drawn twice, offset by one period.
 */
private fun DrawScope.drawMountains(scrollOffset: Float, period: Float) {
    val unit = size.width / period
    val baseY = size.height * GroundFraction
    val mountainHeight = size.height * 0.32f
    val mountainWidth = mountainHeight * 1.6f
    // Mountain left edges in one period of the pattern, in track units.
    val leftEdges = listOf(period * 0.05f, period * 0.4f, period * 0.75f)
    for (shift in listOf(0f, period)) {
        for (left in leftEdges) {
            val leftX = ((left - scrollOffset + shift) * unit).mod(size.width + mountainWidth) - mountainWidth
            val peakX = leftX + mountainWidth / 2f
            drawPath(
                path = Path().apply {
                    moveTo(leftX, baseY)
                    lineTo(peakX, baseY - mountainHeight)
                    lineTo(leftX + mountainWidth, baseY)
                    close()
                },
                color = MountainColor,
            )
            // Snow cap: a smaller triangle at the peak.
            val capHeight = mountainHeight * 0.25f
            drawPath(
                path = Path().apply {
                    moveTo(peakX - mountainWidth * 0.18f, baseY - mountainHeight + capHeight)
                    lineTo(peakX, baseY - mountainHeight)
                    lineTo(peakX + mountainWidth * 0.18f, baseY - mountainHeight + capHeight)
                    close()
                },
                color = MountainSnowColor,
            )
        }
    }
}

/**
 * Draws the hills layer: rounded mounds in two tones of brown, in front of
 * the mountains and behind the trees. The pattern repeats every [period]
 * track units, so the layer is drawn twice, offset by one period.
 */
private fun DrawScope.drawHills(scrollOffset: Float, period: Float) {
    val unit = size.width / period
    val baseY = size.height * GroundFraction
    val hillHeight = size.height * 0.14f
    val hillWidth = hillHeight * 3f
    // Hill left edges in one period of the pattern, in track units.
    val leftEdges = listOf(period * 0.0f, period * 0.5f)
    for (shift in listOf(0f, period)) {
        for ((index, left) in leftEdges.withIndex()) {
            val leftX = ((left - scrollOffset + shift) * unit).mod(size.width + hillWidth) - hillWidth
            drawArc(
                color = if (index % 2 == 0) HillColor else HillDarkColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(leftX, baseY - hillHeight),
                size = Size(hillWidth, hillHeight * 2f),
            )
        }
    }
}

/**
 * Draws the trees layer: green triangles on brown trunks, standing on the
 * ground in front of the hills. The pattern repeats every [period] track
 * units, so the layer is drawn twice, offset by one period.
 */
private fun DrawScope.drawTrees(scrollOffset: Float, period: Float) {
    val unit = size.width / period
    val groundY = size.height * GroundFraction
    val treeHeight = size.height * 0.16f
    val treeWidth = treeHeight * 0.7f
    val trunkHeight = treeHeight * 0.3f
    // Tree left edges in one period of the pattern, in track units.
    val leftEdges = listOf(period * 0.1f, period * 0.35f, period * 0.6f, period * 0.85f)
    for (shift in listOf(0f, period)) {
        for (left in leftEdges) {
            val leftX = ((left - scrollOffset + shift) * unit).mod(size.width + treeWidth) - treeWidth
            val centerX = leftX + treeWidth / 2f
            // Trunk.
            drawRect(
                color = TreeTrunkColor,
                topLeft = Offset(centerX - treeWidth * 0.1f, groundY - trunkHeight),
                size = Size(treeWidth * 0.2f, trunkHeight),
            )
            // Canopy: three stacked triangles.
            for (tier in 0 until 3) {
                val tierHeight = treeHeight * 0.45f
                val tierWidth = treeWidth * (1f - tier * 0.25f)
                val tierTop = groundY - trunkHeight - treeHeight * 0.75f + tier * tierHeight * 0.5f
                drawPath(
                    path = Path().apply {
                        moveTo(centerX - tierWidth / 2f, tierTop + tierHeight)
                        lineTo(centerX, tierTop)
                        lineTo(centerX + tierWidth / 2f, tierTop + tierHeight)
                        close()
                    },
                    color = TreeColor,
                )
            }
        }
    }
}

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

/**
 * Draws Le Chonk, the chonky orange striped cat, at his current position in
 * the game, using his sprite set: the jump pose in the air, the run cycle
 * on the ground, the idle frames when the game is over.
 */
fun DrawScope.drawLeChonk(game: LeChonkRunner, sprites: LeChonkSprites) {
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

    // The sprite is drawn so that Le Chonk's collision box (leChonkWidth
    // track units wide, standing on the ground) matches the cat: the sprite
    // is as wide as the collision box and its feet land on the ground line.
    val spriteWidth = game.leChonkWidth * unit
    val spriteHeight = spriteWidth * SpriteAspectRatio
    val centerX = game.x * unit
    val topLeft = Offset(centerX - spriteWidth / 2f, feetY - spriteHeight)

    drawSprite(sprites.spriteFor(game), topLeft, Size(spriteWidth, spriteHeight))
}

/**
 * The height of a Le Chonk sprite relative to its width. The sprites are
 * roughly 76-91 pixels wide and 104-106 pixels tall, so a chonky cat is
 * about 1.3 times taller than wide.
 */
internal const val SpriteAspectRatio = 1.3f

/**
 * Draws a sprite painter into [topLeft]..[topLeft]+[size], keeping the
 * pixel art crisp: the image is sampled with nearest-neighbour filtering
 * instead of being smoothed.
 */
private fun DrawScope.drawSprite(painter: Painter, topLeft: Offset, size: Size) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.width <= 0f || intrinsic.height <= 0f) return
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.apply {
            save()
            translate(topLeft.x, topLeft.y)
            scale(size.width / intrinsic.width, size.height / intrinsic.height)
            with(painter) { draw(intrinsic) }
            restore()
        }
    }
}

