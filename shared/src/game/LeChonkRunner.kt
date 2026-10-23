package dev.dambatenne.lechonk.game

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Le Chonk Runner: the game state, the auto-run movement and the jump
 * physics, all measured in world units on a horizontal track.
 *
 * The track is [trackLength] units long and wraps around the screen: Le
 * Chonk's on-screen position is [x], the distance he has run so far taken
 * modulo the track length. He runs to the right on his own at [runSpeed]
 * units per second. [jump] is the tap input: it launches him straight up at
 * [jumpVelocity] units per second, and [gravity] pulls him back down until
 * he lands on the ground.
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
) {
    /** Distance run so far, in track units. Also the score. */
    var distance by mutableStateOf(0f)
        private set

    /** Height above the ground, in track units. Zero means standing on the ground. */
    var heightAboveGround by mutableStateOf(0f)
        private set

    /** Vertical speed while airborne, in track units per second. Positive is up. */
    private var verticalVelocity by mutableStateOf(0f)

    /** Position along the track, wrapped into `[0, trackLength)`. */
    val x: Float
        get() = distance % trackLength

    /** Whether Le Chonk is off the ground, either rising or falling. */
    val isInAir: Boolean
        get() = heightAboveGround > 0f || verticalVelocity > 0f

    /**
     * Advances the game by [deltaSeconds] of simulated time: Le Chonk runs
     * to the right, and while he is airborne gravity acts on his jump until
     * he lands back on the ground.
     */
    fun update(deltaSeconds: Float) {
        if (deltaSeconds <= 0f) return
        runToTheRight(deltaSeconds)
        if (isInAir) {
            applyGravity(deltaSeconds)
        }
    }

    /**
     * The tap input: jump. Tapping while already in the air does nothing, so
     * a jump cannot be restarted mid-flight.
     */
    fun jump() {
        if (!isInAir) {
            verticalVelocity = jumpVelocity
        }
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
}

/**
 * Accessibility description of the scene drawn on the Canvas.
 *
 * What the player sees — Le Chonk and the flat ground — is described in every
 * build. Internal state (where Le Chonk is along the track, whether he is in
 * the air) is only described when [includeInternalState] is true, which the
 * UI passes only in debuggable builds.
 */
fun LeChonkRunner.sceneDescription(includeInternalState: Boolean): String {
    val drawnScene = "Le Chonk, the chonky cat runner, on flat ground"
    if (!includeInternalState) return drawnScene

    val position = "at ${(x / trackLength * 100f).toInt()}% along the track"
    return if (isInAir) {
        val height = (heightAboveGround / trackLength * 100f).toInt()
        "$drawnScene. Le Chonk is running to the right, in the air $position, " +
            "$height% of the track above the ground"
    } else {
        "$drawnScene. Le Chonk is running to the right, on the ground $position"
    }
}
