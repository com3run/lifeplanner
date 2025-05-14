package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.GoalAnalytics
import az.tribe.lifeplanner.domain.GoalRepository

class GetGoalAnalyticsUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(): GoalAnalytics {
        return repository.getAnalytics()
    }
}