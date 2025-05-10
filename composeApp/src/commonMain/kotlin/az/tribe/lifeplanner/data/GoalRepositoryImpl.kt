package az.tribe.lifeplanner.data

import az.tribe.lifeplanner.domain.*
import az.tribe.lifeplanner.database.GoalEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import kotlinx.datetime.LocalDate

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

    override suspend fun deleteGoalById(id: String) {
        localGoalStore.deleteGoalById(id)
    }

    override suspend fun deleteAllGoals() {
        localGoalStore.deleteAllGoals()
    }

    private fun GoalEntity.toDomain(): Goal {
        return Goal(
            id = id,
            category = GoalCategory.valueOf(category),
            title = title,
            description = description,
            status = GoalStatus.valueOf(status),
            timeline = GoalTimeline.valueOf(timeline),
            dueDate = LocalDate.parse(dueDate),
            steps = emptyList() // Optional: implement if steps are persisted
        )
    }

    private fun Goal.toEntity(): GoalEntity {
        return GoalEntity(
            id = id,
            category = category.name,
            title = title,
            description = description,
            status = status.name,
            timeline = timeline.name,
            dueDate = dueDate.toString()
        )
    }
}