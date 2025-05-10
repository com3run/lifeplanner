package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalRepository
import az.tribe.lifeplanner.domain.GoalTimeline

class GetGoalsByTimelineUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(timeline: GoalTimeline): List<Goal> {
        return repository.getGoalsByTimeline(timeline)
    }
}