package az.tribe.lifeplanner.ui.utils

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

private val MONTHS_SHORT = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)
private val MONTHS_LONG = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)
private val DAYS_OF_WEEK = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
)

/** "Apr 16" if current year, "Apr 16, 2025" if another year */
fun LocalDate.formatHuman(): String {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val month = MONTHS_SHORT[month.number - 1]
    return if (year == currentYear) "$month $day" else "$month $day, $year"
}

/** "Wednesday, April 16" if current year, "Wednesday, April 16, 2025" if another year */
fun LocalDate.formatHumanDetailed(): String {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val dayName = DAYS_OF_WEEK[dayOfWeek.ordinal % 7]
    val month = MONTHS_LONG[month.number - 1]
    return if (year == currentYear) "$dayName, $month $day" else "$dayName, $month $day, $year"
}
