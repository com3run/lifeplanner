package az.tribe.lifeplanner.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import com.github.takahirom.roborazzi.captureRoboImage

/**
 * Shared snapshot helpers for the JVM preview harness. Each feature keeps its own
 * `<Feature>ScreenPreviews` class next to [PreviewScreenshots] and calls these, so adding a
 * preview never edits another feature's file.
 *
 * PNGs land in `app/shared/build/previews/`. Run one class with
 * `./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.previews.<Class>"`.
 */

/** Renders a whole screen edge to edge, for composables that own their own Scaffold. */
fun ComposeContentTestRule.snapScreen(name: String, darkTheme: Boolean = true, content: @Composable () -> Unit) {
    mainClock.autoAdvance = false
    setContent {
        LifePlannerTheme(darkTheme = darkTheme) { content() }
    }
    // Advance past entrance animations to a stable frame; infinite transitions stay frozen
    // because autoAdvance is off.
    mainClock.advanceTimeBy(800)
    onRoot().captureRoboImage("build/previews/$name.png")
}

/** Renders a component inside a padded surface, for cards and headers rather than screens. */
fun ComposeContentTestRule.snapComponent(name: String, darkTheme: Boolean = true, content: @Composable () -> Unit) {
    mainClock.autoAdvance = false
    setContent {
        LifePlannerTheme(darkTheme = darkTheme) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(Modifier.padding(16.dp)) { content() }
            }
        }
    }
    mainClock.advanceTimeBy(800)
    onRoot().captureRoboImage("build/previews/$name.png")
}
