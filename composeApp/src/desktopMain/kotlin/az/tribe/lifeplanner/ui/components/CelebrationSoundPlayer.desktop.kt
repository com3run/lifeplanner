@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class CelebrationSoundPlayer {
    actual fun play(type: CelebrationType) {}
    actual fun release() {}
}

@Composable
actual fun rememberCelebrationSoundPlayer(): CelebrationSoundPlayer = remember { CelebrationSoundPlayer() }
