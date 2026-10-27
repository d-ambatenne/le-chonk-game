package dev.dambatenne.lechonk.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParallaxBackgroundTest {
    @Test
    fun `the background has the layers from back to front`() {
        val background = ParallaxBackground()
        assertEquals(
            listOf("sky", "clouds", "mountains", "hills", "trees"),
            background.layers.map { it.name },
        )
    }

    @Test
    fun `back layers scroll slower than front layers`() {
        val background = ParallaxBackground()
        val factors = background.layers.map { it.speedFactor }
        for (index in 1 until factors.size) {
            assertTrue(
                factors[index] > factors[index - 1],
                "Layer ${background.layers[index].name} should scroll faster than " +
                    "${background.layers[index - 1].name}",
            )
        }
    }

    @Test
    fun `the sky never scrolls`() {
        val background = ParallaxBackground()
        val sky = background.layers.first()
        assertEquals(0f, sky.speedFactor)
        assertEquals(0f, background.scrollOffset(sky, distanceRun = 500f))
    }

    @Test
    fun `each layer scrolls at its own fraction of the distance run`() {
        val background = ParallaxBackground(trackLength = 1000f)
        val distance = 400f
        for (layer in background.layers) {
            assertEquals(
                distance * layer.speedFactor,
                background.scrollOffset(layer, distance),
                0.0001f,
                "Layer ${layer.name} should scroll at ${layer.speedFactor} of the distance",
            )
        }
    }

    @Test
    fun `the layers scroll at different speeds when the player moves`() {
        val background = ParallaxBackground(trackLength = 1000f)
        val before = background.scrollOffsets(distanceRun = 0f)
        val after = background.scrollOffsets(distanceRun = 100f)
        val deltas = before.zip(after) { old, new -> new - old }
        // Every layer except the sky moves, and no two layers move the same amount.
        assertEquals(0f, deltas.first(), 0.0001f)
        val movingLayers = deltas.drop(1)
        assertTrue(movingLayers.all { it > 0f }, "Every layer but the sky should scroll")
        assertEquals(
            movingLayers.size,
            movingLayers.toSet().size,
            "Each layer should scroll at a different speed",
        )
    }

    @Test
    fun `the scroll offset wraps around the layer period`() {
        val background = ParallaxBackground(trackLength = 100f)
        val trees = background.layers.last()
        val offset = background.scrollOffset(trees, distanceRun = 250f)
        assertTrue(offset in 0f..<trees.period, "The offset should wrap into one period")
        assertEquals(25f, offset, 0.0001f)
    }

    @Test
    fun `the scene description describes the parallax background in every build`() {
        val game = LeChonkRunner()
        val description = game.sceneDescription(includeInternalState = false)
        assertTrue(description.contains("sky gradient"))
        assertTrue(description.contains("clouds"))
        assertTrue(description.contains("mountains"))
        assertTrue(description.contains("hills"))
        assertTrue(description.contains("trees"))
        assertTrue(description.contains("scroll at different speeds"))
    }

    @Test
    fun `the scene description reports the layer speeds only in debug builds`() {
        val game = LeChonkRunner()
        val debugDescription = game.sceneDescription(includeInternalState = true)
        assertTrue(debugDescription.contains("clouds scrolling at 10% of the run speed"))
        assertTrue(debugDescription.contains("trees scrolling at 90% of the run speed"))

        val releaseDescription = game.sceneDescription(includeInternalState = false)
        assertTrue(!releaseDescription.contains("scrolling at"))
    }
}
