package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ActionOption
import az.tribe.lifeplanner.domain.model.ActionOptionType
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.PossibilityContext
import az.tribe.lifeplanner.domain.model.TimeOfDay
import az.tribe.lifeplanner.domain.model.TuningDial
import kotlinx.datetime.daysUntil

/**
 * Pillar 2, a pure, on-device ranker (no AI, no I/O). Turns a [PossibilityContext]
 * into up to 5 ranked [ActionOption]s, each with a one-line fit reason. Deterministic,
 * so it is fully covered by unit tests.
 *
 * Pillar 7 (TRI-65): when the context carries a [PossibilityContext.profile], the ranking adapts to
 * the user's wiring, but only for dials that are reliable (enough behaviour observed):
 * - high DELAY_DISCOUNTING (impatient) surfaces quick wins and near payoffs, de-prioritises far ones;
 * - low NOVELTY_SALIENCE (routine-preferring) lifts streak habits over new things;
 * - high PUNISHMENT_SENSITIVITY stops pushing overdue items and softens the copy.
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

    /** A dial value in 0f..1f only when the inference is reliable; null means "do not adapt yet". */
    private fun PossibilityContext.reliableDial(dial: TuningDial): Float? =
        profile?.dial(dial)?.takeIf { it.isReliable }?.value

    private fun habitOptions(ctx: PossibilityContext): List<ActionOption> =
        ctx.pendingHabits.map { habit ->
            var score = 45.0
            if (ctx.timeOfDay == TimeOfDay.MORNING) score += 15
            if ((ctx.energy ?: 3) <= 2) score += 12 // low energy → easy win
            score += minOf(habit.currentStreak, 10).toDouble()
            var reason = when {
                habit.currentStreak > 0 -> "Keep your ${habit.currentStreak}-day streak alive"
                ctx.timeOfDay == TimeOfDay.MORNING -> "A morning habit to start strong"
                else -> "A quick win you can do right now"
            }

            // Impatient users value the immediate payoff a habit gives.
            ctx.reliableDial(TuningDial.DELAY_DISCOUNTING)?.let { v ->
                val k = (v - 0.5f) * 2 // -1..1, positive = impatient
                score += k * 12
                if (k > 0.35f) reason = "A quick win with a payoff right now"
            }
            // Routine-preferring users want continuity; lift their established streaks.
            ctx.reliableDial(TuningDial.NOVELTY_SALIENCE)?.let { v ->
                if (v < 0.5f && habit.currentStreak > 0) {
                    score += (0.5f - v) * 2 * 16
                    reason = "Your routine, keep the rhythm going"
                }
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

            // Novelty seekers are drawn to fresh, concrete next steps.
            ctx.reliableDial(TuningDial.NOVELTY_SALIENCE)?.let { v ->
                if (v > 0.5f) score += (v - 0.5f) * 2 * 12
            }
            // Impatient users discount far-off goal payoffs.
            ctx.reliableDial(TuningDial.DELAY_DISCOUNTING)?.let { v ->
                val k = (v - 0.5f) * 2
                if (k > 0 && ctx.now.date.daysUntil(goal.dueDate) > 7) score -= k * 10
            }

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
            var score = 35.0 + dueBoost(goal, ctx)
            val daysLeft = ctx.now.date.daysUntil(goal.dueDate)
            var reason = when {
                daysLeft < 0 -> "Overdue, a small step gets it moving"
                daysLeft <= 7 -> "Due in $daysLeft day${if (daysLeft == 1) "" else "s"}, make progress"
                else -> "Hasn’t moved lately, nudge it forward"
            }

            // Punishment-sensitive users should not be pushed about misses; de-emphasise + soften.
            ctx.reliableDial(TuningDial.PUNISHMENT_SENSITIVITY)?.let { v ->
                if (v > 0.5f && daysLeft < 0) {
                    score -= (v - 0.5f) * 2 * 22
                    reason = "A small step forward, whenever you are ready"
                }
            }
            // Impatient users discount goals whose payoff is far away.
            ctx.reliableDial(TuningDial.DELAY_DISCOUNTING)?.let { v ->
                val k = (v - 0.5f) * 2
                if (k > 0 && daysLeft > 7) score -= k * 10
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
