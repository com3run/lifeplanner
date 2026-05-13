@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.ui.calendar

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCalendarPermission(): CalendarPermissionHandle =
    CalendarPermissionHandle(state = CalendarPermissionState.NOT_AVAILABLE, request = {})
