package dev.dambatenne.lechonk.game

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Le Chonk Runner: the game state, the auto-run movement, the jump
 * physics, the scrolling obstacles and the hearts, all measured in world
 * units on a horizontal track.
 *
 * The track is [trackLength] units long and wraps around the screen: Le
 * Chonk's on-screen position is [x], the distance he has run so far taken
 * modulo the track length. He runs to the right on his own at [runSpeed]
 * units per second. [jump] is the tap input: it launches him straight up at
 * [jumpVelocity] units per second, and [gravity] pulls him back down until
 * he lands on the ground.
 *
 * Obstacles spawn at the right edge of the screen, one every
 * [obstacleSpawnInterval] seconds, and scroll left toward Le Chonk at
 * [runSpeed] units per second. When an obstacle overlaps Le Chonk while his
 * feet are below the obstacle's top, he loses one of his [maxHearts] hearts
 * and flashes for [hitFlashSeconds] seconds; while the flash lasts he
 * cannot be hit again, so one obstacle costs at most one heart. Jumping
 * above an obstacle's top clears it without a collision.
 *
 * The score grows over time while the game runs: [score] counts one point
 * per [scoreUnitsPerPoint] track units run. When the hearts reach zero the
 * game is over ([isGameOver]): the score stops growing, Le Chonk cannot
 * jump anymore, and [restart] starts a new game.
 */
@Stable
class LeChonkRunner(
    /** Length of the track in world units; one track length spans the screen. */
    val trackLength: Float = 1000f,
    /** Auto-run speed to the right, in track units per second. */
    val runSpeed: Float = 240f,
    /** Upward speed of a jump, in track units per second. */
    val jumpVelocity: Float = 1000f,
    /** Downward acceleration while airborne, in track units per second squared. */
    val gravity: Float = 2000f,
    /** Hearts Le Chonk starts with; one is lost per obstacle collision. */
    val maxHearts: Int = 5,
    /** Width of Le Chonk's collision box, in track units. */
    val leChonkWidth: Float = 120f,
    /** Width of an obstacle, in track units. */
    val obstacleWidth: Float = 80f,
    /** Height of an obstacle, in track units; jumping above it clears it. */
    val obstacleHeight: Float = 140f,
    /** Seconds between obstacle spawns at the right edge of the screen. */
    val obstacleSpawnInterval: Float = 2.5f,
    /** How long the hit flash (and the invulnerability) lasts, in seconds. */
    val hitFlashSeconds: Float = 0.5f,
    /** Track units that make up one score point. */
    val scoreUnitsPerPoint: Float = 10f,
) {
    /** Distance run so far, in track units. Also the score. */
    var distance by mutableStateOf(0f)
        private set

    /**
     * The score, in points: one point per [scoreUnitsPerPoint] track units
     * run. It increases over time while the game runs and stops when the
     * game is over.
     */
    val score: Int
        get() = (distance / scoreUnitsPerPoint).toInt()

    /** Height above the ground, in track units. Zero means standing on the ground. */
    var heightAboveGround by mutableStateOf(0f)
        private set

    /** Vertical speed while airborne, in track units per second. Positive is up. */
    private var verticalVelocity by mutableStateOf(0f)

    /** Hearts left. One heart is lost each time Le Chonk collides with an obstacle. */
    var hearts by mutableStateOf(maxHearts)
        private set

    /** Whether the game is over: Le Chonk has run out of hearts. */
    val isGameOver: Boolean
        get() = hearts <= 0

    /** Obstacles currently on the screen, scrolling left toward Le Chonk. */
    val obstacles: SnapshotStateList<Obstacle> = mutableStateListOf()

    /** How many times Le Chonk has jumped. */
    var jumps by mutableStateOf(0)
        private set

    /** How many times an obstacle has hit Le Chonk. */
    var hits by mutableStateOf(0)
        private set

    /** Remaining seconds of the hit flash after the latest collision. */
    var hitFlashRemaining by mutableStateOf(0f)
        private set

    /** Position along the track, wrapped into `[0, trackLength)`. */
    val x: Float
        get() = distance % trackLength

    /** Whether Le Chonk is off the ground, either rising or falling. */
    val isInAir: Boolean
        get() = heightAboveGround > 0f || verticalVelocity > 0f

    /** Whether Le Chonk is flashing after a hit; he cannot be hit again while flashing. */
    val isFlashing: Boolean
        get() = hitFlashRemaining > 0f

    /** Time until the next obstacle spawns at the right edge, in seconds. */
    private var timeToNextObstacle = obstacleSpawnInterval

    /**
     * Advances the game by [deltaSeconds] of simulated time: Le Chonk runs
     * to the right, gravity acts on his jump until he lands, obstacles
     * scroll left, new obstacles spawn at the right edge, collisions remove
     * hearts and the hit flash ticks down. Does nothing once the game is
     * over, so the final score stays put.
     */
    fun update(deltaSeconds: Float) {
        if (deltaSeconds <= 0f || isGameOver) return
        runToTheRight(deltaSeconds)
        if (isInAir) {
            applyGravity(deltaSeconds)
        }
        scrollObstacles(deltaSeconds)
        spawnObstacles(deltaSeconds)
        collideWithObstacles()
        if (isFlashing) {
            hitFlashRemaining = (hitFlashRemaining - deltaSeconds).coerceAtLeast(0f)
        }
    }

    /**
     * The tap input: jump. Tapping while already in the air does nothing, so
     * a jump cannot be restarted mid-flight. Tapping does nothing once the
     * game is over.
     */
    fun jump() {
        if (!isInAir && !isGameOver) {
            verticalVelocity = jumpVelocity
            jumps++
        }
    }

    /**
     * Starts a new game: the distance, the score, the hearts, the obstacles
     * and the jump and hit counters all go back to their starting values.
     */
    fun restart() {
        distance = 0f
        heightAboveGround = 0f
        verticalVelocity = 0f
        hearts = maxHearts
        obstacles.clear()
        jumps = 0
        hits = 0
        hitFlashRemaining = 0f
        timeToNextObstacle = obstacleSpawnInterval
    }

    /** Auto-run: Le Chonk always moves to the right, without any input. */
    private fun runToTheRight(deltaSeconds: Float) {
        distance += runSpeed * deltaSeconds
    }

    /** Jump physics: move by the vertical speed, then let gravity act. */
    private fun applyGravity(deltaSeconds: Float) {
        heightAboveGround = (heightAboveGround + verticalVelocity * deltaSeconds)
            .coerceAtLeast(0f)
        verticalVelocity -= gravity * deltaSeconds
        if (heightAboveGround == 0f) {
            // Back on the ground: the jump is over.
            verticalVelocity = 0f
        }
    }

    /** Obstacles scroll left toward Le Chonk at the auto-run speed. */
    private fun scrollObstacles(deltaSeconds: Float) {
        for (obstacle in obstacles) {
            obstacle.x -= runSpeed * deltaSeconds
        }
        // Obstacles that scrolled fully past the left edge are gone.
        obstacles.removeAll { it.x + it.width < 0f }
    }

    /** Spawns an obstacle at the right edge of the screen every [obstacleSpawnInterval] seconds. */
    private fun spawnObstacles(deltaSeconds: Float) {
        timeToNextObstacle -= deltaSeconds
        if (timeToNextObstacle <= 0f) {
            obstacles.add(
                Obstacle(
                    width = obstacleWidth,
                    height = obstacleHeight,
                    startX = trackLength,
                ),
            )
            timeToNextObstacle += obstacleSpawnInterval
        }
    }

    /**
     * Collision: an obstacle that overlaps Le Chonk while his feet are below
     * the obstacle's top costs one heart and starts the hit flash. While the
     * flash lasts Le Chonk is invulnerable, so one obstacle costs at most one
     * heart. Jumping above the obstacle's top avoids the collision entirely.
     */
    private fun collideWithObstacles() {
        if (isFlashing) return
        val leChonkLeft = x - leChonkWidth / 2f
        val leChonkRight = x + leChonkWidth / 2f
        for (obstacle in obstacles) {
            val overlaps = obstacle.x < leChonkRight && obstacle.x + obstacle.width > leChonkLeft
            if (overlaps && heightAboveGround < obstacle.height) {
                registerHit()
                return
            }
        }
    }

    /** A collision: lose one heart, count the hit and start the hit flash. */
    private fun registerHit() {
        hearts = (hearts - 1).coerceAtLeast(0)
        hits++
        hitFlashRemaining = hitFlashSeconds
    }
}

/**
 * An obstacle on the track: a block of [width] by [height] track units whose
 * left edge sits at [x] (0 is the left edge of the screen,
 * [LeChonkRunner.trackLength] the right edge). Obstacles scroll left toward
 * Le Chonk and sit on the ground.
 */
@Stable
class Obstacle(
    /** Width of the obstacle, in track units. */
    val width: Float,
    /** Height of the obstacle, in track units. */
    val height: Float,
    /** Left edge of the obstacle when it spawns, in track units. */
    startX: Float,
) {
    /** Left edge of the obstacle along the track, in track units. */
    var x by mutableStateOf(startX)
}

/**
 * Accessibility description of the scene drawn on the Canvas.
 *
 * What the player sees — Le Chonk, the flat ground, the hearts, the
 * obstacles and whether the game is over — is described in every build.
 * Internal state (where Le Chonk is along the track, whether he is in the
 * air, where the obstacles are, how many jumps and hits happened, whether
 * he is flashing) is only described when [includeInternalState] is true,
 * which the UI passes only in debuggable builds.
 */
fun LeChonkRunner.sceneDescription(includeInternalState: Boolean): String {
    val drawnScene = buildString {
        append("Le Chonk, the chonky cat runner, on flat ground")
        append(", $hearts of $maxHearts hearts at the top of the screen")
        if (obstacles.isNotEmpty()) {
            append(", ${obstacles.size} ${if (obstacles.size == 1) "obstacle" else "obstacles"} on the track")
        }
        if (isGameOver) {
            append(", game over")
        }
    }
    if (!includeInternalState) return drawnScene

    val position = "at ${(x / trackLength * 100f).toInt()}% along the track"
    val runner = if (isInAir) {
        val height = (heightAboveGround / trackLength * 100f).toInt()
        "Le Chonk is running to the right, in the air $position, " +
            "$height% of the track above the ground"
    } else {
        "Le Chonk is running to the right, on the ground $position"
    }
    val obstaclesDescription = if (obstacles.isEmpty()) {
        "No obstacles on the track"
    } else {
        val positions = obstacles.joinToString(", ") { "${(it.x / trackLength * 100f).toInt()}%" }
        "Obstacles at $positions along the track"
    }
    val flash = if (isFlashing) " Le Chonk is flashing after a hit." else ""
    return "$drawnScene. $runner. $obstaclesDescription. jumps: $jumps, hits: $hits.$flash"
}
