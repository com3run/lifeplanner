@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import az.tribe.lifeplanner.domain.enum.AmbientSound

actual class AmbientSoundPlayer {
    actual fun play(sound: AmbientSound) {}
    actual fun stop() {}
    actual fun release() {}
}

@Composable
actual fun rememberAmbientSoundPlayer(): AmbientSoundPlayer = remember { AmbientSoundPlayer() }
