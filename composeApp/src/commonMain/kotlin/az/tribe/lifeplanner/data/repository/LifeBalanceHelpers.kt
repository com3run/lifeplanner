package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.BalanceRating
import az.tribe.lifeplanner.domain.model.BalanceTrend
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.LifeArea
import az.tribe.lifeplanner.domain.model.LifeAreaScore

// ─── LifeArea → GoalCategory mapping ─────────────────────────────────────────

internal fun LifeArea.toGoalCategory(): GoalCategory = when (this) {
    LifeArea.CAREER -> GoalCategory.CAREER
    LifeArea.MONEY -> GoalCategory.MONEY
    LifeArea.BODY -> GoalCategory.BODY
    LifeArea.PEOPLE -> GoalCategory.PEOPLE
    LifeArea.WELLBEING -> GoalCategory.WELLBEING
    LifeArea.PURPOSE -> GoalCategory.PURPOSE
}

// ─── Score calculations ───────────────────────────────────────────────────────

internal fun calculateRecentActivityScore(
    goals: List<az.tribe.lifeplanner.domain.model.Goal>,
    habits: List<Habit>
): Int {
    var score = 0

    // Points for active goals
    score += goals.count { it.status == GoalStatus.IN_PROGRESS } * 10

    // Points for recent goal completions
    score += goals.count { it.status == GoalStatus.COMPLETED } * 15

    // Points for habits with streaks
    habits.forEach { habit ->
        score += when {
            habit.currentStreak >= 30 -> 20
            habit.currentStreak >= 14 -> 15
            habit.currentStreak >= 7 -> 10
            habit.currentStreak >= 3 -> 5
            else -> 0
        }
    }

    return score.coerceIn(0, 100)
}

internal fun calculateAreaScore(
    totalGoals: Int,
    completedGoals: Int,
    activeGoals: Int,
    habitCount: Int,
    habitCompletionRate: Float,
    recentActivityScore: Int
): Int {
    // Base score from goal engagement (0-40 points)
    val goalEngagementScore = when {
        totalGoals == 0 -> 10 // No goals = low engagement
        else -> {
            val completionRatio = completedGoals.toFloat() / totalGoals
            val activeRatio = activeGoals.toFloat() / totalGoals
            ((completionRatio * 20) + (activeRatio * 20)).toInt()
        }
    }

    // Habit consistency score (0-30 points)
    val habitScore = when {
        habitCount == 0 -> 5 // No habits = low consistency
        else -> (habitCompletionRate * 30).toInt()
    }

    // Recent activity score (0-30 points)
    val activityScore = (recentActivityScore * 0.3).toInt()

    return (goalEngagementScore + habitScore + activityScore).coerceIn(0, 100)
}

internal fun calculateBalanceRating(areaScores: List<LifeAreaScore>): BalanceRating {
    val overallScore = areaScores.map { it.score }.average()
    val variance = calculateVariance(areaScores.map { it.score })
    val minScore = areaScores.minOfOrNull { it.score } ?: 0

    return when {
        overallScore >= 70 && variance < 200 && minScore >= 50 -> BalanceRating.EXCELLENT
        overallScore >= 55 && variance < 400 && minScore >= 35 -> BalanceRating.GOOD
        overallScore >= 40 && minScore >= 20 -> BalanceRating.MODERATE
        minScore >= 10 -> BalanceRating.NEEDS_ATTENTION
        else -> BalanceRating.CRITICAL
    }
}

internal fun calculateVariance(scores: List<Int>): Double {
    if (scores.isEmpty()) return 0.0
    val mean = scores.average()
    return scores.map { (it - mean) * (it - mean) }.average()
}

// ─── Suggestion tables ────────────────────────────────────────────────────────

internal fun getSuggestedGoal(area: LifeArea): String = when (area) {
    LifeArea.CAREER -> "Complete a professional certification"
    LifeArea.MONEY -> "Build a 3-month emergency fund"
    LifeArea.BODY -> "Exercise 3 times per week for a month"
    LifeArea.PEOPLE -> "Reconnect with 5 old friends"
    LifeArea.WELLBEING -> "Practice daily mindfulness for 30 days"
    LifeArea.PURPOSE -> "Establish a daily meditation practice"
}

internal fun getSuggestedHabit(area: LifeArea): String = when (area) {
    LifeArea.CAREER -> "Dedicate 30 minutes daily to skill development"
    LifeArea.MONEY -> "Track daily expenses"
    LifeArea.BODY -> "10-minute morning stretch"
    LifeArea.PEOPLE -> "Reach out to one friend daily"
    LifeArea.WELLBEING -> "5-minute gratitude journaling"
    LifeArea.PURPOSE -> "10-minute morning meditation"
}
