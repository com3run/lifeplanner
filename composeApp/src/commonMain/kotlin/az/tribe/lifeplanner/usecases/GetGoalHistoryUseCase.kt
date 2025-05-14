package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.GoalHistoryRepository
import az.tribe.lifeplanner.domain.GoalChange

class GetGoalHistoryUseCase(private val repository: GoalHistoryRepository) {
    suspend operator fun invoke(goalId: String): List<GoalChange> {
        return repository.getHistoryForGoal(goalId)
    }
}