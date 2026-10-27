package dev.dambatenne.lechonk.game

import androidx.compose.runtime.Stable

/**
 * The parallax background of Le Chonk Runner: a sky gradient with clouds,
 * mountains, hills and trees on separate layers, from back to front.
 *
 * Every layer scrolls at its own fraction of the distance Le Chonk has run,
 * so the background layers move at different speeds when he moves and the
 * scene reads as depth: the sky gradient stays put, the clouds drift
 * slowly, the mountains a bit faster, the hills faster still and the trees
 * at nearly the full run speed, right behind Le Chonk.
 *
 * The layers are pure functions of the distance run: [scrollOffset] of a
 * layer is the distance run times that layer's [speedFactor], wrapped into
 * one pattern period so the layer can repeat seamlessly.
 */
@Stable
class ParallaxBackground(
    /** Length of the track in world units; one track length spans the screen. */
    val trackLength: Float = 1000f,
) {
    /**
     * The background layers, from back to front. Each layer's [speedFactor]
     * is the fraction of the run speed it scrolls at: 0 for the sky, which
     * never moves, up to nearly 1 for the trees, which move almost as fast
     * as Le Chonk himself.
     */
    val layers: List<ParallaxLayer> = listOf(
        ParallaxLayer(
            name = "sky",
            speedFactor = 0f,
            period = trackLength,
        ),
        ParallaxLayer(
            name = "clouds",
            speedFactor = 0.1f,
            period = trackLength,
        ),
        ParallaxLayer(
            name = "mountains",
            speedFactor = 0.3f,
            period = trackLength,
        ),
        ParallaxLayer(
            name = "hills",
            speedFactor = 0.6f,
            period = trackLength,
        ),
        ParallaxLayer(
            name = "trees",
            speedFactor = 0.9f,
            period = trackLength,
        ),
    )

    /**
     * The horizontal scroll offset of [layer], in track units, for the given
     * distance run: how far the layer has scrolled left. Wrapped into
     * `[0, period)` so a repeating layer pattern can be drawn seamlessly.
     */
    fun scrollOffset(layer: ParallaxLayer, distanceRun: Float): Float =
        (distanceRun * layer.speedFactor).mod(layer.period)

    /** The scroll offset of every layer, from back to front, for [distanceRun]. */
    fun scrollOffsets(distanceRun: Float): List<Float> =
        layers.map { scrollOffset(it, distanceRun) }
}

/**
 * One layer of the parallax background: a named decoration ([name], used in
 * accessibility descriptions) that scrolls at [speedFactor] times the run
 * speed and repeats every [period] track units.
 */
@Stable
class ParallaxLayer(
    /** The layer's name: sky, clouds, mountains, hills or trees. */
    val name: String,
    /** Fraction of the run speed the layer scrolls at; 0 means it never moves. */
    val speedFactor: Float,
    /** Track units after which the layer's pattern repeats. */
    val period: Float,
)
