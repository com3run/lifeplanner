package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDateTime

enum class TimeOfDay {
    MORNING, MIDDAY, AFTERNOON, EVENING, NIGHT;

    companion object {
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
            in 5..10 -> MORNING
            in 11..13 -> MIDDAY
            in 14..17 -> AFTERNOON
            in 18..21 -> EVENING
            else -> NIGHT
        }
    }
}

/** A goal milestone paired with its parent goal, so the engine can score it in context. */
data class MilestoneCandidate(
    val goal: Goal,
    val milestone: Milestone
)

/**
 * Pillar 2 — an ephemeral snapshot of "right now" that the
 * [az.tribe.lifeplanner.domain.service.PossibilityEngine] ranks over. Pure value
 * object: it carries `now` and pre-fetched candidates so the engine needs no clock or I/O.
 */
data class PossibilityContext(
    val now: LocalDateTime,
    val timeOfDay: TimeOfDay,
    val energy: Int?,            // BodySlice.energyRating (~1..5), null if unknown
    val stressLevel: Int?,       // MetaSlice.stressLevel
    val sleepQuality: Int?,      // MetaSlice.sleepQuality
    val freeMinutes: Int?,       // open calendar-slot estimate; null when unknown (no calendar API yet)
    val pendingHabits: List<Habit> = emptyList(),         // active habits not yet done today
    val openMilestones: List<MilestoneCandidate> = emptyList(),
    val dueOrStalledGoals: List<Goal> = emptyList()       // due/stalled goals with no open milestone
)
