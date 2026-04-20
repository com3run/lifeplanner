package az.tribe.lifeplanner.util

import androidx.compose.runtime.Composable

@Composable
actual fun InAppUpdateEffect(enabled: Boolean) {
    // No-op on Meta Quest — Google Play In-App Update not available
}
