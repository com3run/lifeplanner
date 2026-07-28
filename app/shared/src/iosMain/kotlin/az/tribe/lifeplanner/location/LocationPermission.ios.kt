@file:OptIn(ExperimentalForeignApi::class)

package az.tribe.lifeplanner.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

@Composable
actual fun rememberLocationPermission(): LocationPermissionHandle {
    val manager = remember { CLLocationManager() }

    fun currentState(): LocationPermissionState =
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionState.GRANTED
            else -> LocationPermissionState.DENIED
        }

    var state by remember { mutableStateOf(currentState()) }

    // Authorization changes arrive on the delegate; reflect them into Compose state.
    DisposableEffect(manager) {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                state = currentState()
            }
        }
        manager.delegate = delegate
        onDispose { manager.delegate = null }
    }

    return LocationPermissionHandle(
        state = state,
        request = { manager.requestWhenInUseAuthorization() },
    )
}
