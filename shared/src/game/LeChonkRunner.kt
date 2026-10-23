package dev.dambatenne.lechonk.game

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The Le Chonk Runner game state.
 *
 * Le Chonk runs to the right on his own; tapping the screen makes him jump.
 * The world is measured in track units: [trackLength] units span the screen
 * from the left edge to the right edge, and Le Chonk wraps around to the left
 * edge when he runs off the right one.
 */
@Stable
class LeChonkRunner(
    val trackLength: Float = 1000f,
    val runSpeed: Float = 240f,
    val jumpVelocity: Float = 1000f,
    val gravity: Float = 2000f,
) {
    /** Total distance run so far, in track units. Also the score. */
    var distance by mutableStateOf(0f)
        private set

    /** Height above the ground, in track units. Zero means standing on the ground. */
    var heightAboveGround by mutableStateOf(0f)
        private set

    private var verticalVelocity by mutableStateOf(0f)

    /** Horizontal position on the track, within `[0, trackLength)`. */
    val x: Float
        get() = distance % trackLength

    /** Whether Le Chonk is off the ground, either rising or falling. */
    val isInAir: Boolean
        get() = heightAboveGround > 0f || verticalVelocity > 0f

    /** Advances the game by [deltaSeconds] of simulated time. */
    fun update(deltaSeconds: Float) {
        if (deltaSeconds <= 0f) return
        distance += runSpeed * deltaSeconds
        if (isInAir) {
            heightAboveGround = (heightAboveGround + verticalVelocity * deltaSeconds).coerceAtLeast(0f)
            verticalVelocity -= gravity * deltaSeconds
            if (heightAboveGround == 0f) {
                verticalVelocity = 0f
            }
        }
    }

    /** Makes Le Chonk jump. Tapping while he is already in the air does nothing. */
    fun jump() {
        if (!isInAir) {
            verticalVelocity = jumpVelocity
        }
    }
}

/**
 * Accessibility description of the game scene drawn on the Canvas.
 *
 * What the player sees (Le Chonk and the flat ground) is always described.
 * Internal state — where Le Chonk is along the track and whether he is in
 * the air — is only described when [includeInternalState] is true, which the
 * UI passes only in debuggable builds.
 */
fun LeChonkRunner.sceneDescription(includeInternalState: Boolean): String {
    val visibleScene = "Le Chonk, the chonky cat runner, on flat ground"
    if (!includeInternalState) return visibleScene
    val position = "at ${(x / trackLength * 100f).toInt()}% along the track"
    return if (isInAir) {
        val height = (heightAboveGround / trackLength * 100f).toInt()
        "$visibleScene. Le Chonk is running to the right, in the air $position, $height% of the track above the ground"
    } else {
        "$visibleScene. Le Chonk is running to the right, on the ground $position"
    }
}
