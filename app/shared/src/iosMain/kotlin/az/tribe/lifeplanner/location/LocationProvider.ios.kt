@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalForeignApi::class)

package az.tribe.lifeplanner.location

import az.tribe.lifeplanner.domain.model.Coordinates
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

actual class LocationProvider {

    private val manager = CLLocationManager()

    actual suspend fun currentCoordinates(): Coordinates? {
        // The system keeps a recent cached fix once authorized; coarse is plenty for weather.
        val location = manager.location ?: return null
        return location.coordinate.useContents { Coordinates(latitude, longitude) }
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
}
