package az.tribe.lifeplanner.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberLocationPermission(): LocationPermissionHandle {
    val context = LocalContext.current

    fun currentState(): LocationPermissionState {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return if (coarse || fine) LocationPermissionState.GRANTED else LocationPermissionState.DENIED
    }

    var state by remember { mutableStateOf(currentState()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        state = if (granted) LocationPermissionState.GRANTED else LocationPermissionState.DENIED
    }

    return LocationPermissionHandle(
        state = state,
        request = { launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)) },
    )
}
