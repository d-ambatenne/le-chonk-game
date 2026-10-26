package dev.dambatenne.lechonk.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LeChonkRunnerObstaclesTest {
    @Test
    fun `obstacles spawn at the right edge of the screen`() {
        val game = LeChonkRunner(obstacleSpawnInterval = 1f)
        assertTrue(game.obstacles.isEmpty())
        game.update(1f)
        assertEquals(1, game.obstacles.size)
        assertEquals(game.trackLength, game.obstacles[0].x, 0.001f)
    }

    @Test
    fun `obstacles scroll left toward le chonk`() {
        val game = LeChonkRunner(obstacleSpawnInterval = 1f)
        game.update(1f) // spawns one obstacle at the right edge
        val startX = game.obstacles[0].x
        game.update(0.5f)
        assertEquals(startX - game.runSpeed * 0.5f, game.obstacles[0].x, 0.001f)
    }

    @Test
    fun `obstacles spawn periodically`() {
        val game = LeChonkRunner(obstacleSpawnInterval = 1f)
        game.update(1f)
        assertEquals(1, game.obstacles.size)
        game.update(1f)
        assertEquals(2, game.obstacles.size)
    }

    @Test
    fun `obstacles are removed after scrolling past the left edge`() {
        val game = LeChonkRunner(obstacleSpawnInterval = 1000f)
        game.update(1000f) // spawns one obstacle at the right edge
        assertEquals(1, game.obstacles.size)
        var frames = 0
        while (game.obstacles.isNotEmpty() && frames < 10_000) {
            game.update(1f / 60f)
            frames++
        }
        assertTrue(game.obstacles.isEmpty(), "Obstacles should be removed past the left edge")
    }

    @Test
    fun `colliding with an obstacle removes one heart and starts the hit flash`() {
        val game = LeChonkRunner()
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f)
        assertEquals(game.maxHearts - 1, game.hearts)
        assertEquals(1, game.hits)
        assertTrue(game.isFlashing)
    }

    @Test
    fun `le chonk cannot be hit again while the hit flash lasts`() {
        val game = LeChonkRunner()
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f)
        assertEquals(1, game.hits)
        game.update(1f / 60f) // still flashing: the same obstacle cannot hit twice
        assertEquals(1, game.hits)
        assertEquals(game.maxHearts - 1, game.hearts)
    }

    @Test
    fun `the hit flash ends after hitFlashSeconds`() {
        val game = LeChonkRunner(hitFlashSeconds = 0.1f)
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f)
        assertTrue(game.isFlashing)
        game.obstacles.clear()
        var frames = 0
        while (game.isFlashing && frames < 1000) {
            game.update(1f / 60f)
            frames++
        }
        assertFalse(game.isFlashing)
        assertTrue(frames > 3, "The hit flash should last several frames")
    }

    @Test
    fun `jumping over an obstacle causes no collision and no heart loss`() {
        val game = LeChonkRunner()
        // An obstacle 300 track units ahead; jump in time to clear it.
        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x + 300f))
        repeat(10) { game.update(1f / 60f) }
        game.jump()
        var frames = 0
        while (game.obstacles.isNotEmpty() && frames < 10_000) {
            game.update(1f / 60f)
            frames++
        }
        assertEquals(game.maxHearts, game.hearts)
        assertEquals(0, game.hits)
        assertEquals(1, game.jumps)
    }

    @Test
    fun `hearts never drop below zero`() {
        val game = LeChonkRunner(maxHearts = 2, hitFlashSeconds = 0.01f)
        repeat(5) {
            game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
            game.update(1f / 60f)
            game.obstacles.clear()
        }
        assertEquals(0, game.hearts)
    }

    @Test
    fun `the scene description reports the hearts in every build`() {
        val game = LeChonkRunner()
        assertTrue(game.sceneDescription(includeInternalState = false).contains("5 of 5 hearts"))

        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f)

        assertTrue(game.sceneDescription(includeInternalState = false).contains("4 of 5 hearts"))
        assertTrue(game.sceneDescription(includeInternalState = true).contains("4 of 5 hearts"))
    }

    @Test
    fun `the scene description reports obstacle positions, jumps and hits only in debug builds`() {
        val game = LeChonkRunner(obstacleSpawnInterval = 1f)
        game.update(1f) // one obstacle at the right edge
        game.jump()

        val debugDescription = game.sceneDescription(includeInternalState = true)
        assertTrue(debugDescription.contains("Obstacles at 100%"))
        assertTrue(debugDescription.contains("jumps: 1"))

        val releaseDescription = game.sceneDescription(includeInternalState = false)
        assertFalse(releaseDescription.contains("Obstacles at"))
        assertFalse(releaseDescription.contains("jumps:"))

        game.obstacles.add(Obstacle(game.obstacleWidth, game.obstacleHeight, startX = game.x))
        game.update(1f / 60f)

        val debugAfterHit = game.sceneDescription(includeInternalState = true)
        assertTrue(debugAfterHit.contains("hits: 1"))
        assertTrue(debugAfterHit.contains("flashing"))
        assertFalse(game.sceneDescription(includeInternalState = false).contains("hits:"))
    }
}
