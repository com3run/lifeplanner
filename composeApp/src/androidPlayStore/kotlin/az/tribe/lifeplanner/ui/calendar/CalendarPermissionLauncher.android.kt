package az.tribe.lifeplanner.ui.calendar

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
actual fun rememberCalendarPermission(): CalendarPermissionHandle {
    val context = LocalContext.current

    fun currentState(): CalendarPermissionState =
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
            CalendarPermissionState.GRANTED
        else
            CalendarPermissionState.DENIED

    var state by remember { mutableStateOf(currentState()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        state = if (permissions[Manifest.permission.READ_CALENDAR] == true)
            CalendarPermissionState.GRANTED
        else
            CalendarPermissionState.DENIED
    }

    return CalendarPermissionHandle(
        state = state,
        request = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        }
    )
}
