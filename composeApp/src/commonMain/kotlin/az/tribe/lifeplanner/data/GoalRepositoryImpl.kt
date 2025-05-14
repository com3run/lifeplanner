package az.tribe.lifeplanner.data

import az.tribe.lifeplanner.domain.*
import az.tribe.lifeplanner.database.GoalEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

class GoalRepositoryImpl(
    private val localGoalStore: SharedDatabase
) : GoalRepository {

    override suspend fun getAllGoals(): List<Goal> {
        return localGoalStore.getAllGoals().map { it.toDomain() }
    }

    override suspend fun insertGoal(goal: Goal) {
        localGoalStore.insertGoal(goal.toEntity())
    }

    override suspend fun insertGoals(goals: List<Goal>) {
        localGoalStore.insertGoals(goals.map { it.toEntity() })
    }

    override suspend fun updateGoal(goal: Goal) {
        localGoalStore.updateGoal(goal.toEntity())
    }

    override suspend fun getGoalsByTimeline(timeline: GoalTimeline): List<Goal> {
        return getAllGoals().filter { it.timeline == timeline }
    }

    override suspend fun updateProgress(id: String, progress: Int) {
        localGoalStore.updateGoalProgress(id, progress.toLong())
    }

    override suspend fun getAnalytics(): GoalAnalytics {
        val goals = localGoalStore.getAllGoals()
        val total = goals.size
        val notStarted = goals.count { it.status == GoalStatus.NOT_STARTED.name }
        val inProgress = goals.count { it.status == GoalStatus.IN_PROGRESS.name }
        val completedGoals = goals.filter { it.status == GoalStatus.COMPLETED.name }

        val averageCompletionDays = completedGoals.mapNotNull { goal ->
            // Assume the goal was created when added to DB and completed on dueDate
            // Ideally, we'd track createdAt and completedAt
            // For now: simulate using dueDate - today
            try {
                val due = LocalDate.parse(goal.dueDate)
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                (due.daysUntil(today)).takeIf { it >= 0 }
            } catch (e: Exception) {
                null
            }
        }.average().toInt() ?: 0

        return GoalAnalytics(
            totalGoals = total,
            notStarted = notStarted,
            inProgress = inProgress,
            completed = completedGoals.size,
            averageCompletionDays = averageCompletionDays
        )
    }

    override suspend fun deleteGoalById(id: String) {
        localGoalStore.deleteGoalById(id)
    }

    override suspend fun deleteAllGoals() {
        localGoalStore.deleteAllGoals()
    }


}