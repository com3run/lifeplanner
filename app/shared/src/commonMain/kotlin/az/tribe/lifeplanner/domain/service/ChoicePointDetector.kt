package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.domain.model.ChoicePointTrigger
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Pillar 3 — pure detector that surfaces a [ChoicePoint] on the three "silent drift"
 * triggers instead of letting them roll over: a broken habit streak, a stalled goal,
 * and a passed deadline. Deterministic over its inputs → fully unit-testable.
 */
class ChoicePointDetector(
    private val stalledDays: Int = 14,
    private val stalledProgressBelow: Long = 25
) {
    fun detect(
        today: LocalDate,
        goals: List<Goal>,
        habits: List<Pair<Habit, Boolean>>   // habit + doneToday
    ): List<ChoicePoint> {
        val result = mutableListOf<ChoicePoint>()

        habits.forEach { (habit, doneToday) ->
            if (!habit.isActive || doneToday) return@forEach
            val last = habit.lastCompletedDate ?: return@forEach
            val missedDays = last.daysUntil(today)
            if (missedDays >= 2) { // at least one full missed day → streak broken
                result.add(
                    ChoicePoint(
                        trigger = ChoicePointTrigger.HABIT_STREAK_BREAK,
                        title = "“${habit.title}” streak broke",
                        prompt = "Last done $missedDays days ago. Keep going, or let it go?",
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
                        prompt = "Due ${-daysToDue} day${if (daysToDue == -1) "" else "s"} ago. Reschedule, shrink, or drop?",
                        subjectTitle = goal.title,
                        relatedGoalId = goal.id
                    )
                )

                (goal.progress ?: 0L) < stalledProgressBelow &&
                    goal.createdAt.date.daysUntil(today) >= stalledDays -> result.add(
                    ChoicePoint(
                        trigger = ChoicePointTrigger.GOAL_STALLED,
                        title = "“${goal.title}” has stalled",
                        prompt = "Little progress in a while. Recommit, shrink, or drop?",
                        subjectTitle = goal.title,
                        relatedGoalId = goal.id
                    )
                )
            }
        }
        return result
    }
}
