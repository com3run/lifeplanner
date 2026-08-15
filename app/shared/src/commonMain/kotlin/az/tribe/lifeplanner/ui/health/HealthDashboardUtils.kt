package az.tribe.lifeplanner.ui.health

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

internal fun dayLabel(date: LocalDate): String {
    return when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
        else -> ""
    }
}

internal fun isToday(date: LocalDate): Boolean {
    val today = kotlinx.datetime.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return date == today
}

internal fun sleepColor(hours: Double): Color = when {
    hours < 6.0 -> Color(0xFFEF5350)
    hours < 7.0 -> Color(0xFFFFA726)
    hours <= 9.0 -> Color(0xFF66BB6A)
    else -> Color(0xFF42A5F5)
}

internal fun heartRateZoneColor(bpm: Double): Color = when {
    bpm < 60.0 -> Color(0xFF42A5F5)
    bpm <= 100.0 -> Color(0xFF66BB6A)
    else -> Color(0xFFFFA726)
}

internal fun formatCompact(value: Double): String {
    val longVal = value.toLong()
    return when {
        longVal >= 10_000 -> {
            val k = longVal / 1000.0
            val rounded = (k * 10).roundToInt() / 10.0
            if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()}K"
            else "${rounded}K"
        }
        else -> longVal.toString()
    }
}

/** 12345 -> "12,345" (multiplatform; String.format is JVM-only). */
internal fun formatThousands(value: Long): String {
    val digits = value.toString()
    val sign = if (digits.startsWith("-")) "-" else ""
    val body = digits.removePrefix("-")
    val grouped = body.reversed().chunked(3).joinToString(",").reversed()
    return sign + grouped
}

internal fun formatSleepDuration(hours: Double): String {
    val h = hours.toInt()
    val m = ((hours - h) * 60).roundToInt()
    return "${h}h ${m}m"
}
