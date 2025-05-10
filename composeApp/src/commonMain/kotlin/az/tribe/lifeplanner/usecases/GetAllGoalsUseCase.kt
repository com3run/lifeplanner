package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalRepository

class GetAllGoalsUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(): List<Goal> {
        return repository.getAllGoals()
    }
}