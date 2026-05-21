package az.tribe.lifeplanner.ui.onboarding

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency

internal fun buildHabitSuggestions(category: GoalCategory): List<Pair<OnboardingHabitItem, Boolean>> {
    val suggestions = when (category) {
        GoalCategory.CAREER -> listOf(
            OnboardingHabitItem("Practice a skill", "30 min daily learning or practice", "📚", HabitFrequency.DAILY),
            OnboardingHabitItem("Weekly networking", "Reach out to one new person", "🤝", HabitFrequency.WEEKLY),
            OnboardingHabitItem("Read industry news", "15 min morning reading", "📰", HabitFrequency.DAILY)
        )
        GoalCategory.MONEY -> listOf(
            OnboardingHabitItem("Track expenses", "Log daily spending", "💰", HabitFrequency.DAILY),
            OnboardingHabitItem("Weekly budget review", "Review your finances every Sunday", "📊", HabitFrequency.WEEKLY),
            OnboardingHabitItem("Save first", "Set aside savings before spending", "🏦", HabitFrequency.DAILY)
        )
        GoalCategory.BODY -> listOf(
            OnboardingHabitItem("Morning workout", "30 min exercise to start the day", "🏃", HabitFrequency.DAILY),
            OnboardingHabitItem("Drink water", "8 glasses of water daily", "💧", HabitFrequency.DAILY),
            OnboardingHabitItem("Early bedtime", "Sleep by 10:30pm", "😴", HabitFrequency.DAILY)
        )
        GoalCategory.PEOPLE -> listOf(
            OnboardingHabitItem("Reach out to someone", "Text or call a friend or family member", "📱", HabitFrequency.DAILY),
            OnboardingHabitItem("Weekly social time", "Plan something with people you care about", "👥", HabitFrequency.WEEKLY),
            OnboardingHabitItem("Active listening practice", "Be fully present in conversations", "👂", HabitFrequency.DAILY)
        )
        GoalCategory.WELLBEING -> listOf(
            OnboardingHabitItem("Morning meditation", "10 min mindfulness to start the day", "🧘", HabitFrequency.DAILY),
            OnboardingHabitItem("Daily journaling", "Write 3 thoughts or reflections", "✍️", HabitFrequency.DAILY),
            OnboardingHabitItem("Gratitude practice", "Name 3 things you're grateful for", "🙏", HabitFrequency.DAILY)
        )
        GoalCategory.PURPOSE -> listOf(
            OnboardingHabitItem("Deep work session", "90 min of focused work on what matters", "🎯", HabitFrequency.DAILY),
            OnboardingHabitItem("Weekly reflection", "Review your week against your values", "🔍", HabitFrequency.WEEKLY),
            OnboardingHabitItem("Learn something new", "Read or listen to something inspiring", "💡", HabitFrequency.DAILY)
        )
        GoalCategory.FAMILY -> listOf(
            OnboardingHabitItem("Quality family time", "Dedicated undivided time with family", "🏡", HabitFrequency.DAILY),
            OnboardingHabitItem("Family dinner", "Eat together without screens", "🍽️", HabitFrequency.DAILY),
            OnboardingHabitItem("Check in with loved ones", "A quick message or call to family", "❤️", HabitFrequency.DAILY)
        )
    }
    return suggestions.map { it to true }
}
