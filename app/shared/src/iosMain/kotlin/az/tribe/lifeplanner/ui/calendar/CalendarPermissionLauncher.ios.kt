package az.tribe.lifeplanner.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusDenied
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKAuthorizationStatusRestricted
import platform.EventKit.EKAuthorizationStatusWriteOnly
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCalendarPermission(): CalendarPermissionHandle {
    val store = remember { EKEventStore() }
    val scope = rememberCoroutineScope()

    val isIOS17Plus = remember {
        NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
            cValue<NSOperatingSystemVersion> {
                majorVersion = 17
                minorVersion = 0
                patchVersion = 0
            }
        )
    }

    fun currentState(): CalendarPermissionState {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        return when (status) {
            EKAuthorizationStatusAuthorized,
            EKAuthorizationStatusFullAccess -> CalendarPermissionState.GRANTED
            EKAuthorizationStatusWriteOnly  -> CalendarPermissionState.GRANTED
            EKAuthorizationStatusDenied     -> CalendarPermissionState.DENIED
            EKAuthorizationStatusRestricted -> CalendarPermissionState.NOT_AVAILABLE
            else                            -> CalendarPermissionState.UNKNOWN
        }
    }

    var state by remember { mutableStateOf(currentState()) }

    return CalendarPermissionHandle(
        state = state,
        request = {
            scope.launch {
                val granted = suspendCancellableCoroutine { cont ->
                    if (isIOS17Plus) {
                        // iOS 17+: use the new full-access API
                        store.requestFullAccessToEventsWithCompletion { g, _ -> cont.resume(g) }
                    } else {
                        // iOS < 17: deprecated but still functional
                        @Suppress("DEPRECATION")
                        store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { g, _ -> cont.resume(g) }
                    }
                }
                state = if (granted) CalendarPermissionState.GRANTED else CalendarPermissionState.DENIED
            }
        }
    )
}
