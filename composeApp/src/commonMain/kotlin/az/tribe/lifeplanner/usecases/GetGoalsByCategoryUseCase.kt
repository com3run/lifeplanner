package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalCategory
import az.tribe.lifeplanner.domain.GoalRepository

class GetGoalsByCategoryUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(category: GoalCategory): List<Goal> {
        return repository.getAllGoals().filter { it.category == category }
    }
}