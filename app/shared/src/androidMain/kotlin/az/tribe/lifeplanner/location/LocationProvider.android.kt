@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "DEPRECATION", "MissingPermission")

package az.tribe.lifeplanner.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import az.tribe.lifeplanner.domain.model.Coordinates
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.mp.KoinPlatform
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume

actual class LocationProvider {

    private val context: Context get() = KoinPlatform.getKoin().get()

    actual suspend fun currentCoordinates(): Coordinates? = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        // Prefer a recent last-known fix (instant); otherwise ask for a single fresh one.
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        val lastKnown = providers
            .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }

        val location = lastKnown ?: withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) { awaitCurrent(lm) }
        location?.let { Coordinates(it.latitude, it.longitude) }
    }

    actual suspend fun placeName(coordinates: Coordinates): String? = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            results?.firstOrNull()?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
        }.getOrElse {
            Logger.w("LocationProvider") { "reverse geocode failed: ${it.message}" }
            null
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private suspend fun awaitCurrent(lm: LocationManager): Location? =
        suspendCancellableCoroutine { cont ->
            val provider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }
            if (provider == null) {
                if (cont.isActive) cont.resume(null)
                return@suspendCancellableCoroutine
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal()
                    val executor = Executor { it.run() }
                    lm.getCurrentLocation(provider, signal, executor) { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    cont.invokeOnCancellation { signal.cancel() }
                } else {
                    lm.requestSingleUpdate(provider, { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }, context.mainLooper)
                }
            } catch (e: Exception) {
                Logger.w("LocationProvider") { "current location failed: ${e.message}" }
                if (cont.isActive) cont.resume(null)
            }
        }

    private companion object {
        const val FRESH_FIX_TIMEOUT_MS = 8_000L
    }
}
