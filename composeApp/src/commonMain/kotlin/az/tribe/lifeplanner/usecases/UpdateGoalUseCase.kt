package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalRepository

class UpdateGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal) {
        repository.updateGoal(goal)
    }
}