package dev.dambatenne.lechonk.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LeChonkRunnerGameOverTest {
    /** A game where Le Chonk has just lost his last heart. */
    private fun gameOverGame(): LeChonkRunner {
        val game = LeChonkRunner(maxHearts = 1, hitFlashSeconds = 0.01f)
        game.update(1f) // run and score for a second
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f) // collide with the obstacle: the last heart is lost
        return game
    }

    @Test
    fun `the game is not over while le chonk has hearts left`() {
        val game = LeChonkRunner()
        assertFalse(game.isGameOver)
        game.update(10f)
        assertFalse(game.isGameOver)
    }

    @Test
    fun `the game is over when the hearts reach zero`() {
        val game = gameOverGame()
        assertEquals(0, game.hearts)
        assertTrue(game.isGameOver)
    }

    @Test
    fun `the score stops increasing when the game is over`() {
        val game = gameOverGame()
        val finalScore = game.score
        assertTrue(finalScore > 0, "The game should have scored before it ended")
        game.update(10f)
        assertEquals(finalScore, game.score)
    }

    @Test
    fun `le chonk cannot jump when the game is over`() {
        val game = gameOverGame()
        game.jump()
        assertFalse(game.isInAir)
        assertEquals(0, game.jumps)
    }

    @Test
    fun `restart starts a new game`() {
        val game = LeChonkRunner(maxHearts = 2, hitFlashSeconds = 0.01f)
        game.update(1f) // the score and the distance grow
        game.jump()
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f) // one hit: one heart lost
        assertTrue(game.score > 0)
        assertEquals(1, game.hearts)
        assertEquals(1, game.jumps)
        assertEquals(1, game.hits)
        assertFalse(game.obstacles.isEmpty())

        game.restart()

        assertEquals(0, game.score)
        assertEquals(0f, game.distance, 0.0001f)
        assertEquals(game.maxHearts, game.hearts)
        assertEquals(0, game.jumps)
        assertEquals(0, game.hits)
        assertTrue(game.obstacles.isEmpty())
        assertFalse(game.isInAir)
        assertFalse(game.isFlashing)
        assertFalse(game.isGameOver)

        // The new game runs and scores again.
        game.update(1f)
        assertTrue(game.score > 0)
    }

    @Test
    fun `restart after a game over starts a new game`() {
        val game = gameOverGame()
        assertTrue(game.isGameOver)

        game.restart()

        assertFalse(game.isGameOver)
        assertEquals(game.maxHearts, game.hearts)
        assertEquals(0, game.score)
        game.update(1f)
        assertTrue(game.score > 0)
    }

    @Test
    fun `the scene description reports the game over in every build`() {
        val game = gameOverGame()
        assertTrue(game.sceneDescription(includeInternalState = false).contains("game over"))
        assertTrue(game.sceneDescription(includeInternalState = true).contains("game over"))
    }
}
