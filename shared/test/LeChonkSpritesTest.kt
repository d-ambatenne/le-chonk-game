package dev.dambatenne.lechonk.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LeChonkSpritesTest {
    @Test
    fun `le chonk runs on the ground by default`() {
        val game = LeChonkRunner()
        assertEquals(LeChonkPose.Run, game.pose)
    }

    @Test
    fun `le chonk is in his jump pose while in the air`() {
        val game = LeChonkRunner()
        game.jump()
        assertEquals(LeChonkPose.Jump, game.pose)
    }

    @Test
    fun `le chonk is back in his run pose after landing`() {
        val game = LeChonkRunner()
        game.jump()
        var frames = 0
        while (game.isInAir && frames < 10_000) {
            game.update(1f / 60f)
            frames++
        }
        assertEquals(LeChonkPose.Run, game.pose)
    }

    @Test
    fun `le chonk stands in his idle pose when the game is over`() {
        val game = LeChonkRunner(maxHearts = 1)
        // Run into an obstacle until the last heart is gone.
        var frames = 0
        while (!game.isGameOver && frames < 100_000) {
            game.update(1f / 60f)
            frames++
        }
        assertTrue(game.isGameOver)
        assertEquals(LeChonkPose.Idle, game.pose)
    }

    @Test
    fun `the run cycle advances with the distance run`() {
        val game = LeChonkRunner(trackLength = 100_000f, runSpeed = 100f)
        val seen = mutableSetOf<Int>()
        repeat(200) {
            game.update(1f / 60f)
            seen.add((game.distance * RunFramesPerTrackUnit).toInt() % 8)
        }
        assertTrue(seen.size > 1, "The run cycle should advance while Le Chonk runs")
    }

    @Test
    fun `the run cycle wraps around after a full cycle`() {
        val game = LeChonkRunner(trackLength = 100_000f, runSpeed = 100f)
        val frames = 8
        val distancePerCycle = frames / RunFramesPerTrackUnit
        game.update(distancePerCycle / 100f)
        val frameAfterOneCycle = (game.distance * RunFramesPerTrackUnit).toInt() % frames
        assertEquals(0, frameAfterOneCycle)
    }

    @Test
    fun `the scene description names the pose in debug builds`() {
        val game = LeChonkRunner(trackLength = 100f, runSpeed = 10f)
        game.update(2f)
        assertTrue(game.sceneDescription(includeInternalState = true).contains("run pose"))

        game.jump()
        assertTrue(game.sceneDescription(includeInternalState = true).contains("jump pose"))
    }

    @Test
    fun `the scene description describes the cat as orange and striped in every build`() {
        val game = LeChonkRunner()
        val description = game.sceneDescription(includeInternalState = false)
        assertTrue(description.contains("orange"))
        assertTrue(description.contains("striped"))
    }

    @Test
    fun `the scene description hides the pose in release builds`() {
        val game = LeChonkRunner(trackLength = 100f, runSpeed = 10f)
        game.update(2f)
        val releaseDescription = game.sceneDescription(includeInternalState = false)
        assertNotEquals(true, releaseDescription.contains("run pose"))
    }
}
