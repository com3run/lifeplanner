package az.tribe.lifeplanner.domain

interface GoalHistoryRepository {
    suspend fun insertChange(
        id: String,
        goalId: String,
        field: String,
        oldValue: String?,
        newValue: String,
        changedAt: String
    )
    suspend fun getHistoryForGoal(goalId: String): List<GoalChange>
}