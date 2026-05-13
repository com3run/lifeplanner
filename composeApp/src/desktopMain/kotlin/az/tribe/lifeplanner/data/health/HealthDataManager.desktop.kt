@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.health

import kotlinx.datetime.LocalDate

actual class HealthDataManager {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun hasPermissions(): Boolean = false
    actual suspend fun readTodaySteps(): Long? = null
    actual suspend fun readStepsForDateRange(start: LocalDate, end: LocalDate): List<HealthDataPoint> = emptyList()
    actual suspend fun readRecentWeight(days: Int): List<HealthDataPoint> = emptyList()
    actual suspend fun readHeartRate(days: Int): List<HealthDataPoint> = emptyList()
    actual suspend fun readSleep(days: Int): List<HealthDataPoint> = emptyList()
}
