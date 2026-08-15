package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * A small, deterministic "goal tune-up" engine. Given the user's goals and today, it surfaces a few
 * concrete, one-tap optimizations, wrap up what's basically done, reschedule what slipped, finish the
 * near-complete, revisit the stalled, so the plan stays honest with the calendar instead of quietly
 * rotting. Pure logic: no time, storage, or framework deps, so it's easy to test and reason about.
 */
object GoalOptimizer {

    /** How far out an overdue goal is pushed when the user taps "Reschedule". */
    const val RESCHEDULE_DAYS = 14
    private const val STALE_AGE_DAYS = 21
    private const val STALE_PROGRESS = 0.25f
    private const val ALMOST_PROGRESS = 0.75f

    enum class Kind { READY_TO_COMPLETE, RESCHEDULE_OVERDUE, FINISH_ALMOST, REFOCUS_STALE }

    data class Suggestion(
        val kind: Kind,
        val goalId: String,
        val goalTitle: String,
        val message: String,
        val actionLabel: String,
    )

    /**
     * Up to [limit] suggestions, most useful first. At most one per goal (its highest-priority issue).
     * Priority order matches [Kind]'s declaration order.
     */
    fun suggestions(goals: List<Goal>, today: LocalDate, limit: Int = 2): List<Suggestion> =
        goals.asSequence()
            .filter { !it.isArchived && it.status != GoalStatus.COMPLETED }
            .mapNotNull { forGoal(it, today) }
            .sortedWith(compareBy({ it.kind.ordinal }, { it.goalTitle }))
            .take(limit)
            .toList()

    private fun forGoal(goal: Goal, today: LocalDate): Suggestion? {
        val total = goal.milestones.size
        val done = goal.milestones.count { it.isCompleted }
        val progress = goal.completionRate
        val overdueDays = goal.dueDate.daysUntil(today) // > 0 once the due date has passed
        val ageDays = goal.createdAt.date.daysUntil(today)

        return when {
            // Everything's checked off but it's still marked open, offer to close it out.
            (total > 0 && done == total) || progress >= 1f -> Suggestion(
                Kind.READY_TO_COMPLETE, goal.id, goal.title,
                "Every step of \"${goal.title}\" is done. Mark it complete?",
                "Complete",
            )
            // Slipped past its date, offer a realistic new one instead of leaving it red.
            overdueDays > 0 -> Suggestion(
                Kind.RESCHEDULE_OVERDUE, goal.id, goal.title,
                "\"${goal.title}\" is ${overdueDays}d overdue. Push it $RESCHEDULE_DAYS days out?",
                "Reschedule",
            )
            // Nearly there, a small nudge to land it.
            (total >= 2 && done == total - 1) || progress >= ALMOST_PROGRESS -> Suggestion(
                Kind.FINISH_ALMOST, goal.id, goal.title,
                "\"${goal.title}\" is almost there. One last push?",
                "Finish",
            )
            // Old and barely moved, maybe it needs a rethink (or to go).
            progress < STALE_PROGRESS && ageDays > STALE_AGE_DAYS -> Suggestion(
                Kind.REFOCUS_STALE, goal.id, goal.title,
                "\"${goal.title}\" hasn't moved in weeks. Still worth it?",
                "Review",
            )
            else -> null
        }
    }
}
