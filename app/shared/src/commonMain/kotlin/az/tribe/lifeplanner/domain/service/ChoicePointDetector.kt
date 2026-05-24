package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.domain.model.ChoicePointTrigger
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Pillar 3 — pure detector that surfaces a [ChoicePoint] on the three "silent drift"
 * triggers instead of letting them roll over: a broken habit streak, a stalled goal,
 * and a passed deadline. Deterministic over its inputs → fully unit-testable.
 *
 * Pillar 7 (Innate): an optional [DecisionProfile] tunes the prompts to the user's wiring —
 * a punishment-sensitive user gets fewer, later, gentler prompts (never hammered for a miss),
 * and a risk-averse user is offered the safe option first. With no profile (or an
 * unreliable one), behaviour is unchanged.
 */
class ChoicePointDetector(
    private val stalledDays: Int = 14,
    private val stalledProgressBelow: Long = 25
) {
    fun detect(
        today: LocalDate,
        goals: List<Goal>,
        habits: List<Pair<Habit, Boolean>>,   // habit + doneToday
        profile: DecisionProfile? = null
    ): List<ChoicePoint> {
        // Only reliably-inferred, clearly-high dials change anything — otherwise stay neutral.
        val punishmentHigh = isHigh(profile?.punishmentSensitivity)
        val riskAverse = isHigh(profile?.riskAversion)
        val gentle = punishmentHigh || riskAverse          // softer tone, safe option first
        val streakMissedFloor = if (punishmentHigh) 3 else 2  // give one more grace day
        val stalledFloor = if (punishmentHigh) stalledDays + 7 else stalledDays

        val result = mutableListOf<ChoicePoint>()

        habits.forEach { (habit, doneToday) ->
            if (!habit.isActive || doneToday) return@forEach
            val last = habit.lastCompletedDate ?: return@forEach
            val missedDays = last.daysUntil(today)
            if (missedDays >= streakMissedFloor) { // at least one full missed day → streak broken
                result.add(
                    ChoicePoint(
                        trigger = ChoicePointTrigger.HABIT_STREAK_BREAK,
                        title = "“${habit.title}” streak broke",
                        prompt = if (gentle)
                            "Life happens — last done $missedDays days ago. Pick it back up when you're ready, or set it aside for now?"
                        else
                            "Last done $missedDays days ago. Keep going, or let it go?",
                        subjectTitle = habit.title,
                        relatedHabitId = habit.id
                    )
                )
            }
        }

        goals.forEach { goal ->
            if (goal.status == GoalStatus.COMPLETED) return@forEach
            val daysToDue = today.daysUntil(goal.dueDate)
            when {
                daysToDue < 0 -> result.add(
                    ChoicePoint(
                        trigger = ChoicePointTrigger.DEADLINE_PASSED,
                        title = "“${goal.title}” deadline passed",
                        prompt = "Due ${-daysToDue} day${if (daysToDue == -1) "" else "s"} ago. " +
                            if (gentle) "No rush — give it more time, make it smaller, or set it aside?"
                            else "Reschedule, shrink, or drop?",
                        subjectTitle = goal.title,
                        relatedGoalId = goal.id
                    )
                )

                (goal.progress ?: 0L) < stalledProgressBelow &&
                    goal.createdAt.date.daysUntil(today) >= stalledFloor -> result.add(
                    ChoicePoint(
                        trigger = ChoicePointTrigger.GOAL_STALLED,
                        title = "“${goal.title}” has stalled",
                        prompt = if (gentle)
                            "This one's gone quiet. Ease back in, make it smaller, or set it aside?"
                        else
                            "Little progress in a while. Recommit, shrink, or drop?",
                        subjectTitle = goal.title,
                        relatedGoalId = goal.id
                    )
                )
            }
        }
        return result
    }

    /** A dial counts only when we've inferred it reliably and it sits clearly toward the high pole. */
    private fun isHigh(dial: DialSetting?): Boolean =
        dial != null && dial.isReliable && dial.value >= HIGH_THRESHOLD

    companion object {
        const val HIGH_THRESHOLD = 0.6f
    }
}
