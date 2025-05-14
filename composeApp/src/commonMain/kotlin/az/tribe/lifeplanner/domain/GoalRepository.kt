package az.tribe.lifeplanner.domain

interface GoalRepository {
    suspend fun getAllGoals(): List<Goal>
    suspend fun insertGoal(goal: Goal)
    suspend fun insertGoals(goals: List<Goal>) // ← Add this
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoalById(id: String)
    suspend fun deleteAllGoals()
    suspend fun getGoalsByTimeline(timeline: GoalTimeline): List<Goal>
    suspend fun updateProgress(id: String, progress: Int)
    suspend fun getAnalytics(): GoalAnalytics
}