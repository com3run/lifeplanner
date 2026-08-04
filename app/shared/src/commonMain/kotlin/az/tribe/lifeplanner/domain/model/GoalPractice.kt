package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDate

/**
 * A goal being lived rather than ticked off.
 *
 * Some goals are a list of things to finish. Others are a practice you keep: the habits are the
 * goal, and there is no checklist to complete. "Explore Spirituality" is not five milestones, it is
 * showing up each week. Presenting that as `0/0 Milestones` tells the user their goal is unfinished
 * when nothing is missing.
 *
 * A goal becomes a practice when habits are linked to it, and this is what we can say about it.
 */
data class GoalPractice(
    val habits: List<Habit>,
    /** Days since the practice began, counting today. 1 on the first day, never below 1. */
    val dayNumber: Int,
    /** The window we measure against. See [PracticeWindow.DAYS_TO_AUTOMATIC]. */
    val windowDays: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val checkIns: Int,
) {
    /** 0..1 through the window, capped: past the window the practice is established, not overdue. */
    val windowProgress: Float
        get() = (dayNumber.toFloat() / windowDays).coerceIn(0f, 1f)

    /** Past the window there is nothing left to count down to, so we stop counting. */
    val isEstablished: Boolean get() = dayNumber >= windowDays
}

object PracticeWindow {

    /**
     * The median days to automaticity from Lally et al. (University College London, 2009).
     *
     * The same study found a range of 18 to 254 days, which is why nothing built on this number
     * should read as a deadline. It is a reasonable horizon to show, not a promise, and the copy
     * around it says so. The app already cites this figure in [KnowledgeLibrary] and the daily
     * recap; keep them consistent if it ever changes.
     */
    const val DAYS_TO_AUTOMATIC: Int = 66

    /**
     * Builds the practice for a goal, or null when no habits are linked and the goal is an ordinary
     * checklist after all.
     *
     * The practice starts with the oldest linked habit, since that is when the user actually began.
     */
    fun of(habits: List<Habit>, today: LocalDate): GoalPractice? {
        val active = habits.filter { it.isActive }
        if (active.isEmpty()) return null

        val began = active.minOf { it.createdAt.date }
        // Counting today, and never negative: a habit created with a future date is bad data, not a
        // practice that has not started.
        val dayNumber = (today.toEpochDays() - began.toEpochDays() + 1).toInt().coerceAtLeast(1)

        return GoalPractice(
            habits = active,
            dayNumber = dayNumber,
            windowDays = DAYS_TO_AUTOMATIC,
            currentStreak = active.maxOf { it.currentStreak },
            longestStreak = active.maxOf { it.longestStreak },
            checkIns = active.sumOf { it.totalCompletions },
        )
    }
}
