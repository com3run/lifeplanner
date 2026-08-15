@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalForeignApi::class)

package az.tribe.lifeplanner.location

import az.tribe.lifeplanner.domain.model.Coordinates
import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationProvider {

    private val manager = CLLocationManager()

    /**
     * Strong reference to the one-shot delegate. CLLocationManager holds its delegate weakly, so
     * without this the callback can be collected before the fix arrives and we would hang until
     * the timeout.
     */
    private var pendingDelegate: CLLocationManagerDelegateProtocol? = null

    actual suspend fun currentCoordinates(): Coordinates? {
        // The system keeps a recent cached fix once authorized; coarse is plenty for weather.
        manager.location?.let { cached ->
            return cached.coordinate.useContents { Coordinates(latitude, longitude) }
        }
        // No cached fix yet: on a fresh boot, or right after the user first grants permission,
        // `location` is nil until something actually asks. Request a single fix rather than
        // reporting "no location" and leaving the weather banner stuck on its Enable prompt.
        return withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) { awaitSingleFix() }
    }

    /** One-shot `requestLocation()`, bridged to a suspend call. Null if it fails or is denied. */
    private suspend fun awaitSingleFix(): Coordinates? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    val coordinates = location?.coordinate?.useContents {
                        Coordinates(latitude, longitude)
                    }
                    if (cont.isActive) cont.resume(coordinates)
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    Logger.w("LocationProvider") { "requestLocation failed: ${didFailWithError.localizedDescription}" }
                    if (cont.isActive) cont.resume(null)
                }
            }
            pendingDelegate = delegate
            manager.delegate = delegate
            manager.requestLocation()

            cont.invokeOnCancellation {
                manager.delegate = null
                pendingDelegate = null
            }
        }
    }

    actual suspend fun placeName(coordinates: Coordinates): String? =
        suspendCancellableCoroutine { cont ->
            val geocoder = CLGeocoder()
            val location = CLLocation(latitude = coordinates.latitude, longitude = coordinates.longitude)
            geocoder.reverseGeocodeLocation(location) { placemarks, _ ->
                val name = (placemarks?.firstOrNull() as? CLPlacemark)?.let { it.locality ?: it.administrativeArea }
                if (cont.isActive) cont.resume(name)
            }
            cont.invokeOnCancellation { geocoder.cancelGeocode() }
        }

    private companion object {
        // Matches the Android provider's fresh-fix budget so both platforms give up together.
        const val FRESH_FIX_TIMEOUT_MS = 8_000L
    }
}
