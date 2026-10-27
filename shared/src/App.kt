package dev.dambatenne.lechonk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dambatenne.lechonk.game.LeChonkRunner
import dev.dambatenne.lechonk.game.LeChonkSprites
import dev.dambatenne.lechonk.game.ParallaxBackground
import dev.dambatenne.lechonk.game.drawGround
import dev.dambatenne.lechonk.game.drawHearts
import dev.dambatenne.lechonk.game.drawLeChonk
import dev.dambatenne.lechonk.game.drawObstacles
import dev.dambatenne.lechonk.game.drawParallaxBackground
import dev.dambatenne.lechonk.game.sceneDescription
import kotlinx.coroutines.isActive
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    MaterialTheme {
        LeChonkGameScreen()
    }
}

/**
 * The Le Chonk Runner screen.
 *
 * The whole game lives in this shared module: the game loop ([GameLoop]),
 * the tap-to-jump input, the Canvas rendering and the accessibility
 * description of the drawn scene.
 */
@Composable
private fun LeChonkGameScreen() {
    val game = remember { LeChonkRunner() }
    // The parallax background: sky gradient, clouds, mountains, hills and
    // trees on layers that scroll at different speeds as Le Chonk runs.
    val background = remember { ParallaxBackground(trackLength = game.trackLength) }
    GameLoop(game)
    Box {
        GameScene(game, background)
        if (game.isGameOver) {
            GameOverScreen(game)
        }
    }
}

/**
 * The game loop: advances the game on every animation frame, so Le Chonk
 * runs to the right on his own, without any input.
 */
@Composable
private fun GameLoop(game: LeChonkRunner) {
    LaunchedEffect(game) {
        var previousFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                val deltaSeconds =
                    if (previousFrameNanos == 0L) 0f
                    else (frameNanos - previousFrameNanos) / 1_000_000_000f
                previousFrameNanos = frameNanos
                // Clamp the step so one long frame (e.g. after a pause)
                // cannot teleport Le Chonk.
                game.update(deltaSeconds.coerceIn(0f, 0.1f))
            }
        }
    }
}

/** The drawn scene: the parallax background, the flat ground, the obstacles, Le Chonk and the hearts, plus the on-screen status. */
@Composable
private fun GameScene(game: LeChonkRunner, background: ParallaxBackground) {
    // Le Chonk's sprite set: the idle frames, the run cycle and the jump
    // pose, loaded once from the compose resources.
    val sprites = LeChonkSprites.rememberLeChonkSprites()
    // The Canvas draws art, not text, so it carries a content description in
    // every build. Internal state (track position, airborne, obstacle
    // positions, jumps and hits) is only described in debuggable builds.
    val sceneDescription = game.sceneDescription(
        includeInternalState = isDebuggable(),
        background = background,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Tap anywhere on the screen to jump.
            .pointerInput(game) { detectTapGestures { game.jump() } }
            .safeContentPadding(),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics(mergeDescendants = false) {
                    role = Role.Image
                    contentDescription = sceneDescription
                },
        ) {
            drawParallaxBackground(background, game.distance)
            drawGround()
            drawObstacles(game)
            drawLeChonk(game, sprites)
            drawHearts(game)
        }
        ScoreBoard(game)
    }
}

/** The title, the score and the hint, as real text (not drawn on a Canvas). */
@Composable
private fun BoxScope.ScoreBoard(game: LeChonkRunner) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            // The top padding leaves room for the hearts drawn at the top of the screen.
            .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Le Chonk Runner",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Score: ${game.score}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Tap anywhere to jump",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * The game-over screen, shown when Le Chonk runs out of hearts: the final
 * score and a restart button that starts a new game. The title, the score
 * and the button are real text, so the whole screen is readable in the
 * accessibility tree.
 */
@Composable
private fun GameOverScreen(game: LeChonkRunner) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "Final score: ${game.score}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            Button(onClick = { game.restart() }) {
                Text("Restart")
            }
        }
    }
}
