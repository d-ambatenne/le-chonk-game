package dev.dambatenne.lechonk.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import dev.dambatenne.lechonk.resources.Res
import dev.dambatenne.lechonk.resources.lechonk_idle_1
import dev.dambatenne.lechonk.resources.lechonk_idle_2
import dev.dambatenne.lechonk.resources.lechonk_jump
import dev.dambatenne.lechonk.resources.lechonk_run_1
import dev.dambatenne.lechonk.resources.lechonk_run_2
import dev.dambatenne.lechonk.resources.lechonk_run_3
import dev.dambatenne.lechonk.resources.lechonk_run_4
import dev.dambatenne.lechonk.resources.lechonk_run_5
import dev.dambatenne.lechonk.resources.lechonk_run_6
import dev.dambatenne.lechonk.resources.lechonk_run_7
import dev.dambatenne.lechonk.resources.lechonk_run_8
import org.jetbrains.compose.resources.painterResource

/**
 * The sprite set of Le Chonk, the chonky orange striped cat.
 *
 * The sprites are the CC0 "Orange Fat Cat" pack by megupets (see
 * `composeResources/drawable/LECHONK_SPRITES.md` for the source and the
 * license): two idle frames, an eight-frame run cycle taken from the
 * pack's walk animation, and one jump pose taken from the apex of the
 * pack's jump animation.
 *
 * Which sprite is shown is derived from the game state: the jump pose
 * while Le Chonk is in the air, the run cycle while he runs on the
 * ground, and the idle frames when the game is over (he stands still,
 * out of hearts).
 */
@Stable
class LeChonkSprites(
    /** The two idle frames, played as a slow breathing animation. */
    val idle: List<Painter>,
    /** The run cycle frames, played while Le Chonk runs on the ground. */
    val run: List<Painter>,
    /** The jump pose, shown while Le Chonk is in the air. */
    val jump: Painter,
) {
    /**
     * The sprite for the current game state.
     *
     * In the air the jump pose is shown. On the ground the run cycle
     * advances with the distance run, so the legs move at the pace of the
     * auto-run. When the game is over Le Chonk stands still and the idle
     * frames play instead.
     */
    fun spriteFor(game: LeChonkRunner): Painter = when (game.pose) {
        LeChonkPose.Jump -> jump
        LeChonkPose.Idle -> idle[frameAt(game.distance * IdleFramesPerTrackUnit, idle.size)]
        LeChonkPose.Run -> run[frameAt(game.distance * RunFramesPerTrackUnit, run.size)]
    }

    /** The animation frame for [progress], wrapped into `[0, frameCount)`. */
    private fun frameAt(progress: Float, frameCount: Int): Int =
        (progress.toInt() % frameCount).coerceIn(0, frameCount - 1)

    companion object {
        /**
         * Loads the sprite set from the compose resources. Remembered so the
         * painters are decoded once per composition.
         */
        @Composable
        fun rememberLeChonkSprites(): LeChonkSprites {
            val idle1 = painterResource(Res.drawable.lechonk_idle_1)
            val idle2 = painterResource(Res.drawable.lechonk_idle_2)
            val run1 = painterResource(Res.drawable.lechonk_run_1)
            val run2 = painterResource(Res.drawable.lechonk_run_2)
            val run3 = painterResource(Res.drawable.lechonk_run_3)
            val run4 = painterResource(Res.drawable.lechonk_run_4)
            val run5 = painterResource(Res.drawable.lechonk_run_5)
            val run6 = painterResource(Res.drawable.lechonk_run_6)
            val run7 = painterResource(Res.drawable.lechonk_run_7)
            val run8 = painterResource(Res.drawable.lechonk_run_8)
            val jump = painterResource(Res.drawable.lechonk_jump)
            return remember(idle1, idle2, run1, run2, run3, run4, run5, run6, run7, run8, jump) {
                LeChonkSprites(
                    idle = listOf(idle1, idle2),
                    run = listOf(run1, run2, run3, run4, run5, run6, run7, run8),
                    jump = jump,
                )
            }
        }
    }
}

/**
 * Run cycle frames shown per track unit run. The auto-run speed is 240
 * track units per second, so at 4 frames per track unit the legs move at
 * roughly 16 steps per second: a brisk run.
 */
internal const val RunFramesPerTrackUnit = 0.04f

/**
 * Idle frames shown per track unit run while the game is over. Le Chonk
 * stands still then, so the breathing animation is slow.
 */
internal const val IdleFramesPerTrackUnit = 0.002f
