package az.tribe.lifeplanner.domain

data class GoalChange(
    val id: String,
    val goalId: String,
    val field: String,
    val oldValue: String?,
    val newValue: String,
    val changedAt: String
)