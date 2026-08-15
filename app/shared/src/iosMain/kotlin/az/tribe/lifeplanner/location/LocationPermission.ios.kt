@file:OptIn(ExperimentalForeignApi::class)

package az.tribe.lifeplanner.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

private fun authStateOf(manager: CLLocationManager): LocationPermissionState =
    when (manager.authorizationStatus) {
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionState.GRANTED
        else -> LocationPermissionState.DENIED
    }

@Composable
actual fun rememberLocationPermission(): LocationPermissionHandle {
    val manager = remember { CLLocationManager() }
    val state = remember { mutableStateOf(authStateOf(manager)) }

    // The delegate is remembered rather than built inside DisposableEffect, because
    // CLLocationManager.delegate is a *weak* reference, as Objective-C delegates are. Constructed
    // in the effect block, the only strong reference died when that block returned, the delegate
    // was collected, and locationManagerDidChangeAuthorization never fired. Granting the permission
    // then changed nothing on screen: the weather card sat on "Enable" until the screen was left
    // and re-entered, which re-read the status directly and made it look like it had worked all
    // along.
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                state.value = authStateOf(manager)
            }
        }
    }

    DisposableEffect(manager, delegate) {
        manager.delegate = delegate
        // Re-read on attach as well. The status can change while this screen is away (the user
        // granting it in Settings), and any change between construction and attaching the delegate
        // would otherwise be missed entirely.
        state.value = authStateOf(manager)
        onDispose { manager.delegate = null }
    }

    return LocationPermissionHandle(
        state = state.value,
        request = { manager.requestWhenInUseAuthorization() },
    )
}
