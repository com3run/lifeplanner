package az.tribe.lifeplanner.ui.components

import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.domain.GoalCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


fun Instant.formatAsReadableDate(): String {
    return try {
        val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.time.hour.toString().padStart(2, '0')
        val minute = localDateTime.time.minute.toString().padStart(2, '0')
        val day = localDateTime.date.dayOfMonth.toString().padStart(2, '0')
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
        GoalCategory.FINANCIAL -> Color(0xFFFF3B3B)      // Vivid Red
        GoalCategory.CAREER -> Color(0xFF00BFA5)         // Teal Mint
        GoalCategory.EMOTIONAL -> Color(0xFFFFEB3B)      // Banana Yellow
        GoalCategory.FAMILY -> Color(0xFFB388FF)         // Light Purple
        GoalCategory.PHYSICAL -> Color(0xFF00C853)       // Strong Green
        GoalCategory.SPIRITUAL -> Color(0xFF7C4DFF)      // Deep Violet
        GoalCategory.SOCIAL -> Color(0xFFB388FF)      // Deep Violet
        else -> Color(0xFFB388FF)
    }
}

