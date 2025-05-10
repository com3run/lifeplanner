package az.tribe.lifeplanner.infrastructure

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
                dueDate = goal.dueDate
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
                    dueDate = goal.dueDate
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
                id = goal.id
            )
        }
    }
}