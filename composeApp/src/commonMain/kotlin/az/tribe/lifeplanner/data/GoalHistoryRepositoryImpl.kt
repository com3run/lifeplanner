package az.tribe.lifeplanner.data

import az.tribe.lifeplanner.domain.GoalChange
import az.tribe.lifeplanner.domain.GoalHistoryRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase

class GoalHistoryRepositoryImpl(
    private val localGoalStore: SharedDatabase
) : GoalHistoryRepository {

    override suspend fun insertChange(
        id: String,
        goalId: String,
        field: String,
        oldValue: String?,
        newValue: String,
        changedAt: String
    ) {
        localGoalStore.insertGoalHistory(
            id = id,
            goalId = goalId,
            field = field,
            oldValue = oldValue,
            newValue = newValue,
            changedAt = changedAt
        )
    }
    override suspend fun getHistoryForGoal(goalId: String): List<GoalChange> {
        return localGoalStore.getGoalHistory(goalId)
    }
}