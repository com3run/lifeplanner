@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

enum class LocationPermissionState { GRANTED, DENIED }

@Stable
data class LocationPermissionHandle(
    val state: LocationPermissionState,
    val request: () -> Unit,
)

/**
 * Current coarse-location permission state plus a launcher to request it.
 * Android: ACCESS_COARSE_LOCATION runtime permission. iOS: CLLocationManager when-in-use auth.
 */
@Composable
expect fun rememberLocationPermission(): LocationPermissionHandle
