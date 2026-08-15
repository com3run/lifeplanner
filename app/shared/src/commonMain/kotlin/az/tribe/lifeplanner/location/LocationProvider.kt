@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.location

import az.tribe.lifeplanner.domain.model.Coordinates

/**
 * Platform access to the device's coarse location and a human place name for it. Coarse is enough
 * for weather; callers must ensure permission is granted first (see `rememberLocationPermission`).
 * Implementations return null rather than throwing when location is unavailable.
 */
expect class LocationProvider() {
    /** Best available coarse location, or null if none/denied. */
    suspend fun currentCoordinates(): Coordinates?

    /** A short place name (city/locality) for [coordinates], or null if it can't be resolved. */
    suspend fun placeName(coordinates: Coordinates): String?
}
