@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.widget

actual class WidgetDataSyncService {
    actual suspend fun syncWidgetData(dashboardData: WidgetDashboardData, habits: List<WidgetHabitData>) {}
    actual suspend fun refreshWidgets() {}
    actual fun getPendingCheckIns(): List<String> = emptyList()
    actual fun clearPendingCheckIns() {}
    actual fun removePendingCheckIn(habitId: String) {}
}
