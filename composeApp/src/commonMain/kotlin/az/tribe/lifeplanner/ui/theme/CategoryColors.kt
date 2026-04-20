package az.tribe.lifeplanner.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import az.tribe.lifeplanner.domain.enum.GoalCategory

// Modern category colors and gradients
object CategoryColors {
    // Vibrant base colors
    val CAREER = Color(0xFF4A6FFF)
    val MONEY = Color(0xFF28C76F)
    val BODY = Color(0xFFFF9F43)
    val PEOPLE = Color(0xFF7A5AF8)
    val WELLBEING = Color(0xFF00CFE8)
    val PURPOSE = Color(0xFFEA5455)
    val LEARNING = Color(0xFF28C3D7)
    val OTHER = Color(0xFF9E9FA3)

    // Secondary/gradient colors
    val CAREER_GRADIENT = listOf(Color(0xFF4A6FFF), Color(0xFF2C42B0))
    val MONEY_GRADIENT = listOf(Color(0xFF28C76F), Color(0xFF1AAC59))
    val BODY_GRADIENT = listOf(Color(0xFFFF9F43), Color(0xFFE88B2E))
    val PEOPLE_GRADIENT = listOf(Color(0xFF7A5AF8), Color(0xFF6346E0))
    val WELLBEING_GRADIENT = listOf(Color(0xFF00CFE8), Color(0xFF00A1B5))
    val PURPOSE_GRADIENT = listOf(Color(0xFFEA5455), Color(0xFFD03A3B))
    val LEARNING_GRADIENT = listOf(Color(0xFF28C3D7), Color(0xFF1A9AAB))
    val OTHER_GRADIENT = listOf(Color(0xFF9E9FA3), Color(0xFF75767A))

    // Container/light backgrounds for cards
    val CAREER_CONTAINER = Color(0xFFECF0FF)
    val MONEY_CONTAINER = Color(0xFFE0F7EA)
    val BODY_CONTAINER = Color(0xFFFFF4E6)
    val PEOPLE_CONTAINER = Color(0xFFF1ECFF)
    val WELLBEING_CONTAINER = Color(0xFFE0F9FC)
    val PURPOSE_CONTAINER = Color(0xFFFFEDED)
    val LEARNING_CONTAINER = Color(0xFFDFF7FB)
    val OTHER_CONTAINER = Color(0xFFF0F0F0)
}

// Extension function to get category gradient
fun GoalCategory.gradientColors(): List<Color> {
    return when (this) {
        GoalCategory.CAREER -> CategoryColors.CAREER_GRADIENT
        GoalCategory.MONEY -> CategoryColors.MONEY_GRADIENT
        GoalCategory.BODY -> CategoryColors.BODY_GRADIENT
        GoalCategory.PEOPLE -> CategoryColors.PEOPLE_GRADIENT
        GoalCategory.WELLBEING -> CategoryColors.WELLBEING_GRADIENT
        GoalCategory.PURPOSE -> CategoryColors.PURPOSE_GRADIENT
    }
}

// Extension function to get category container color
fun GoalCategory.containerColor(): Color {
    return when (this) {
        GoalCategory.CAREER -> CategoryColors.CAREER_CONTAINER
        GoalCategory.MONEY -> CategoryColors.MONEY_CONTAINER
        GoalCategory.BODY -> CategoryColors.BODY_CONTAINER
        GoalCategory.PEOPLE -> CategoryColors.PEOPLE_CONTAINER
        GoalCategory.WELLBEING -> CategoryColors.WELLBEING_CONTAINER
        GoalCategory.PURPOSE -> CategoryColors.PURPOSE_CONTAINER
    }
}

// Extension function to get main category color
fun GoalCategory.backgroundColor(): Color {
    return when (this) {
        GoalCategory.CAREER -> CategoryColors.CAREER
        GoalCategory.MONEY -> CategoryColors.MONEY
        GoalCategory.BODY -> CategoryColors.BODY
        GoalCategory.PEOPLE -> CategoryColors.PEOPLE
        GoalCategory.WELLBEING -> CategoryColors.WELLBEING
        GoalCategory.PURPOSE -> CategoryColors.PURPOSE
    }
}

// Create a horizontal gradient brush for the category
@Composable
fun GoalCategory.horizontalGradient(): Brush {
    return Brush.horizontalGradient(
        colors = this.gradientColors()
    )
}

// Create a vertical gradient brush for the category
@Composable
fun GoalCategory.verticalGradient(): Brush {
    return Brush.verticalGradient(
        colors = this.gradientColors()
    )
}

// Get a category by its color (useful for analytics)
fun getCategoryByColor(color: Int): GoalCategory {
    return when (color) {
        CategoryColors.CAREER.toArgb() -> GoalCategory.CAREER
        CategoryColors.MONEY.toArgb() -> GoalCategory.MONEY
        CategoryColors.BODY.toArgb() -> GoalCategory.BODY
        CategoryColors.PEOPLE.toArgb() -> GoalCategory.PEOPLE
        CategoryColors.WELLBEING.toArgb() -> GoalCategory.WELLBEING
        CategoryColors.PURPOSE.toArgb() -> GoalCategory.PURPOSE
        else -> GoalCategory.CAREER
    }
}