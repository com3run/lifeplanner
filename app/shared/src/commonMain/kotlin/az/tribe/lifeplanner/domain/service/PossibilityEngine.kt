package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ActionOption
import az.tribe.lifeplanner.domain.model.ActionOptionType
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.PossibilityContext
import az.tribe.lifeplanner.domain.model.TimeOfDay
import kotlinx.datetime.daysUntil

/**
 * Pillar 2, a pure, on-device ranker (no AI, no I/O). Turns a [PossibilityContext]
 * into up to 5 ranked [ActionOption]s, each with a one-line fit reason. Deterministic,
 * so it is fully covered by unit tests.
 */
class PossibilityEngine {

    fun rank(context: PossibilityContext, limit: Int = 5): List<ActionOption> {
        val candidates = buildList {
            addAll(habitOptions(context))
            addAll(milestoneOptions(context))
            addAll(goalOptions(context))
        }
        return candidates
            .sortedByDescending { it.score }
            .distinctBy { it.type to it.refId }
            .take(limit.coerceIn(1, 5))
    }

    private fun habitOptions(ctx: PossibilityContext): List<ActionOption> =
        ctx.pendingHabits.map { habit ->
            var score = 45.0
            if (ctx.timeOfDay == TimeOfDay.MORNING) score += 15
            if ((ctx.energy ?: 3) <= 2) score += 12 // low energy → easy win
            score += minOf(habit.currentStreak, 10).toDouble()
            val reason = when {
                habit.currentStreak > 0 -> "Keep your ${habit.currentStreak}-day streak alive"
                ctx.timeOfDay == TimeOfDay.MORNING -> "A morning habit to start strong"
                else -> "A quick win you can do right now"
            }
            ActionOption(
                type = ActionOptionType.HABIT,
                refId = habit.id,
                title = habit.title,
                fitReason = reason,
                category = habit.category,
                score = score
            )
        }

    private fun milestoneOptions(ctx: PossibilityContext): List<ActionOption> =
        ctx.openMilestones.map { (goal, milestone) ->
            val highEnergy = (ctx.energy ?: 3) >= 4
            val deepFocus = highEnergy && ctx.timeOfDay != TimeOfDay.NIGHT
            var score = 40.0 + dueBoost(goal, ctx)
            if (highEnergy) score += 15

            val parts = mutableListOf<String>()
            ctx.freeMinutes?.let { parts.add("$it min free") }
            energyWord(ctx.energy)?.let { parts.add(it) }
            val prefix = if (parts.isNotEmpty()) parts.joinToString(", ") + ", " else ""
            val due = dueClause(goal, ctx)
            val reason = prefix + "milestone “${milestone.title}” fits" + (due?.let { " ($it)" } ?: "")

            ActionOption(
                type = if (deepFocus) ActionOptionType.FOCUS else ActionOptionType.MILESTONE,
                refId = milestone.id,
                title = if (deepFocus) "Focus on “${milestone.title}”" else milestone.title,
                fitReason = reason,
                goalId = goal.id,
                category = goal.category,
                score = score
            )
        }

    private fun goalOptions(ctx: PossibilityContext): List<ActionOption> =
        ctx.dueOrStalledGoals.map { goal ->
            val score = 35.0 + dueBoost(goal, ctx)
            val daysLeft = ctx.now.date.daysUntil(goal.dueDate)
            val reason = when {
                daysLeft < 0 -> "Overdue, a small step gets it moving"
                daysLeft <= 7 -> "Due in $daysLeft day${if (daysLeft == 1) "" else "s"}, make progress"
                else -> "Hasn’t moved lately, nudge it forward"
            }
            ActionOption(
                type = ActionOptionType.GOAL,
                refId = goal.id,
                title = goal.title,
                fitReason = reason,
                goalId = goal.id,
                category = goal.category,
                score = score
            )
        }

    private fun dueBoost(goal: Goal, ctx: PossibilityContext): Double {
        val d = ctx.now.date.daysUntil(goal.dueDate)
        return when {
            d < 0 -> 30.0   // overdue
            d == 0 -> 28.0  // due today
            d <= 3 -> 20.0
            d <= 7 -> 12.0
            else -> 0.0
        }
    }

    private fun dueClause(goal: Goal, ctx: PossibilityContext): String? {
        val d = ctx.now.date.daysUntil(goal.dueDate)
        return when {
            d < 0 -> "overdue"
            d == 0 -> "due today"
            d <= 7 -> "due in $d day${if (d == 1) "" else "s"}"
            else -> null
        }
    }

    private fun energyWord(energy: Int?): String? = when {
        energy == null -> null
        energy >= 4 -> "high energy"
        energy == 3 -> "steady energy"
        else -> "low energy"
    }
}
