package az.tribe.lifeplanner.domain


data class GoalAnalytics(
    val totalGoals: Int,
    val notStarted: Int,
    val inProgress: Int,
    val completed: Int,
    val averageCompletionDays: Int
)