package az.tribe.lifeplanner.ui.components

import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.model.GoalAnalytics
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


fun Instant.formatAsReadableDate(): String {
    return try {
        val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.time.hour.toString().padStart(2, '0')
        val minute = localDateTime.time.minute.toString().padStart(2, '0')
        val day = localDateTime.date.day.toString().padStart(2, '0')
        val month = localDateTime.date.month.name.lowercase().replaceFirstChar(Char::uppercase)
        val year = localDateTime.date.year
        "$hour:$minute $day $month $year"
    } catch (e: Exception) {
        this.toString()
    }
}

// Utility function to map each GoalCategory to a bold, high-contrast Neo-Brutalist color
fun GoalCategory?.backgroundColor(): Color {
    return when (this) {
        GoalCategory.MONEY -> Color(0xFFFF3B3B)
        GoalCategory.CAREER -> Color(0xFF00BFA5)
        GoalCategory.WELLBEING -> Color(0xFFFFEB3B)
        GoalCategory.BODY -> Color(0xFF00C853)
        GoalCategory.PURPOSE -> Color(0xFF7C4DFF)
        GoalCategory.PEOPLE -> Color(0xFFB388FF)
        else -> Color(0xFFB388FF)
    }
}


fun GoalAnalytics.getCompletionRatePercentage(): Int = (completionRate * 100).toInt()

fun GoalAnalytics.getMostActiveCategory(): GoalCategory? =
    goalsByCategory.maxByOrNull { it.value }?.key

fun GoalAnalytics.getMostUsedTimeline(): GoalTimeline? =
    goalsByTimeline.maxByOrNull { it.value }?.key

fun GoalAnalytics.getCategoryWithHighestProgress(): GoalCategory? =
    averageProgressPerCategory.maxByOrNull { it.value }?.key

