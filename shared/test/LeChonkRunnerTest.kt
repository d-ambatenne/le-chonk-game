package dev.dambatenne.lechonk.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LeChonkRunnerTest {
    @Test
    fun `le chonk runs to the right on his own`() {
        val game = LeChonkRunner()
        val startX = game.x
        game.update(1f)
        assertTrue(game.x > startX, "Le Chonk should move right automatically")
        assertEquals(game.runSpeed, game.distance, 0.001f)
    }

    @Test
    fun `le chonk wraps around to the left edge after running off the right edge`() {
        val game = LeChonkRunner(trackLength = 100f, runSpeed = 60f)
        game.update(2f)
        assertEquals(20f, game.x, 0.001f)
    }

    @Test
    fun `tapping makes le chonk leave the ground`() {
        val game = LeChonkRunner()
        assertFalse(game.isInAir)
        game.jump()
        assertTrue(game.isInAir)
    }

    @Test
    fun `le chonk lands back on the ground after a jump`() {
        val game = LeChonkRunner()
        game.jump()
        var frames = 0
        while (game.isInAir && frames < 10_000) {
            game.update(1f / 60f)
            frames++
        }
        assertFalse(game.isInAir, "Le Chonk should land back on the ground")
        assertEquals(0f, game.heightAboveGround, 0.0001f)
        assertTrue(frames > 10, "A jump should last more than a single frame")
    }

    @Test
    fun `a jump lifts le chonk well above the ground`() {
        val game = LeChonkRunner()
        game.jump()
        var maxHeight = 0f
        var frames = 0
        while (game.isInAir && frames < 10_000) {
            game.update(1f / 60f)
            maxHeight = maxOf(maxHeight, game.heightAboveGround)
            frames++
        }
        assertTrue(maxHeight > game.trackLength * 0.2f, "A jump should be clearly visible")
    }

    @Test
    fun `tapping again while in the air does not change the jump`() {
        val game = LeChonkRunner()
        game.jump()
        game.update(0.1f)
        game.jump() // ignored: already in the air
        game.update(0.1f)

        val reference = LeChonkRunner()
        reference.jump()
        reference.update(0.1f)
        reference.update(0.1f)

        assertEquals(reference.heightAboveGround, game.heightAboveGround, 0.0001f)
        assertEquals(reference.distance, game.distance, 0.0001f)
    }

    @Test
    fun `updating with a non-positive delta does nothing`() {
        val game = LeChonkRunner()
        game.update(0f)
        game.update(-1f)
        assertEquals(0f, game.distance, 0.0001f)
        assertFalse(game.isInAir)
    }

    @Test
    fun `the score increases over time while the game runs`() {
        val game = LeChonkRunner()
        assertEquals(0, game.score)
        game.update(1f)
        val scoreAfterOneSecond = game.score
        assertTrue(scoreAfterOneSecond > 0, "The score should increase while the game runs")
        game.update(1f)
        assertTrue(game.score > scoreAfterOneSecond, "The score should keep increasing over time")
    }

    @Test
    fun `the score counts one point per scoreUnitsPerPoint track units`() {
        val game = LeChonkRunner(runSpeed = 100f, scoreUnitsPerPoint = 10f)
        game.update(1f) // 100 track units run
        assertEquals(10, game.score)
    }

    @Test
    fun `the scene description always describes what is drawn`() {
        val game = LeChonkRunner()
        val description = game.sceneDescription(includeInternalState = false)
        assertTrue(description.contains("Le Chonk"))
        assertTrue(description.contains("ground"))
    }

    @Test
    fun `the scene description reports internal state only when asked`() {
        val game = LeChonkRunner(trackLength = 100f, runSpeed = 10f)
        game.update(2f) // 20% along the track
        val debugDescription = game.sceneDescription(includeInternalState = true)
        assertTrue(debugDescription.contains("running to the right"))
        assertTrue(debugDescription.contains("on the ground"))
        assertTrue(debugDescription.contains("20%"))

        game.jump()
        val airborneDescription = game.sceneDescription(includeInternalState = true)
        assertTrue(airborneDescription.contains("in the air"))

        val releaseDescription = game.sceneDescription(includeInternalState = false)
        assertFalse(releaseDescription.contains("in the air"))
        assertFalse(releaseDescription.contains("20%"))
    }
}
