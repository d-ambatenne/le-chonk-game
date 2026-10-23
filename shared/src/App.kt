package dev.dambatenne.lechonk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dambatenne.lechonk.game.LeChonkRunner
import dev.dambatenne.lechonk.game.SkyColor
import dev.dambatenne.lechonk.game.drawGround
import dev.dambatenne.lechonk.game.drawLeChonk
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

@Composable
private fun LeChonkGameScreen() {
    val game = remember { LeChonkRunner() }

    // The game loop: advance the game on every frame.
    LaunchedEffect(game) {
        var lastFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                val deltaSeconds =
                    if (lastFrameNanos == 0L) 0f
                    else (frameNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameNanos
                game.update(deltaSeconds.coerceIn(0f, 0.1f))
            }
        }
    }

    // Internal state (position along the track, in the air) is only
    // described in debuggable builds.
    val sceneDescription = game.sceneDescription(includeInternalState = isDebuggable())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(game) { detectTapGestures { game.jump() } }
            .background(SkyColor)
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
            drawGround()
            drawLeChonk(game)
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Le Chonk Runner",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Distance: ${game.distance.toInt()} m",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Tap anywhere to jump",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
