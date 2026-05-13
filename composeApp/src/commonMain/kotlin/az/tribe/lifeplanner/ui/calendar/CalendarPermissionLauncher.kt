@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
data class CalendarPermissionHandle(
    val state: CalendarPermissionState,
    val request: () -> Unit
)

/**
 * Returns current calendar permission state and a launcher function that requests access.
 * On Android: READ_CALENDAR + WRITE_CALENDAR runtime permissions.
 * On iOS: EventKit full-access authorization via EKEventStore.
 */
@Composable
expect fun rememberCalendarPermission(): CalendarPermissionHandle
