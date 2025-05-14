package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.domain.GoalChange

import az.tribe.lifeplanner.database.LifePlannerDB
import az.tribe.lifeplanner.di.DatabaseDriverFactory
import az.tribe.lifeplanner.database.GoalEntity

class SharedDatabase(
    private val driverProvider: DatabaseDriverFactory,
) {
    private var database: LifePlannerDB? = null

    private suspend fun initDatabase() {
        if (database == null) {
            database = LifePlannerDB.invoke(
                driver = driverProvider.createDriver()
            )
        }
    }

    suspend operator fun <R> invoke(block: suspend (LifePlannerDB) -> R): R {
        initDatabase()
        return block(database!!)
    }

    // --- GoalEntity accessors ---

    suspend fun getAllGoals(): List<GoalEntity> {
        return this { db -> db.lifePlannerDBQueries.selectAll().executeAsList() }
    }

    suspend fun deleteAllGoals(){
        this { db ->
            db.lifePlannerDBQueries.deleteAllGoals()
        }
    }

    suspend fun insertGoal(goal: GoalEntity) {
        this { db ->
            db.lifePlannerDBQueries.insertGoal(
                id = goal.id,
                category = goal.category,
                title = goal.title,
                description = goal.description,
                status = goal.status,
                timeline = goal.timeline,
                dueDate = goal.dueDate,
                progress = goal.progress
            )
        }
    }

    suspend fun insertGoals(goals: List<GoalEntity>) {
        this { db ->
            goals.forEach { goal ->
                db.lifePlannerDBQueries.insertGoal(
                    id = goal.id,
                    category = goal.category,
                    title = goal.title,
                    description = goal.description,
                    status = goal.status,
                    timeline = goal.timeline,
                    dueDate = goal.dueDate,
                    progress = goal.progress
                )
            }
        }
    }

    suspend fun getGoalsByTimeline(timeline: String): List<GoalEntity> {
        return this { db ->
            db.lifePlannerDBQueries.selectGoalsByTimeline(timeline).executeAsList()
        }
    }

    suspend fun getGoalsByCategory(category: String): List<GoalEntity> {
        return this { db ->
            db.lifePlannerDBQueries.selectGoalsByCategory(category).executeAsList()
        }
    }

    suspend fun deleteGoalById(id: String) {
        this { db ->
            db.lifePlannerDBQueries.deleteGoalById(id)
        }
    }

    suspend fun updateGoal(goal: GoalEntity) {
        this { db ->
            db.lifePlannerDBQueries.updateGoal(
                category = goal.category,
                title = goal.title,
                description = goal.description,
                status = goal.status,
                timeline = goal.timeline,
                dueDate = goal.dueDate,
                progress = goal.progress,
                id = goal.id
            )
        }
    }

    suspend fun insertGoalHistory(
        id: String,
        goalId: String,
        field: String,
        oldValue: String?,
        newValue: String,
        changedAt: String
    ) {
        this { db ->
            db.lifePlannerDBQueries.insertGoalHistory(
                id = id,
                goalId = goalId,
                field_ = field,
                oldValue = oldValue,
                newValue = newValue,
                changedAt = changedAt,
            )
        }
    }

    suspend fun getGoalHistory(goalId: String): List<GoalChange> {
        return this { db ->
            db.lifePlannerDBQueries.getGoalHistory(goalId).executeAsList().map {
                GoalChange(
                    id = it.id,
                    goalId = it.goalId,
                    field = it.field_,
                    oldValue = it.oldValue,
                    newValue = it.newValue?:"unknown",
                    changedAt = it.changedAt
                )
            }
        }
    }

    suspend fun updateGoalProgress(id: String, progress: Long) {
        this { db ->
            db.lifePlannerDBQueries.updateGoalProgress(progress = progress, id = id)
        }
    }
}