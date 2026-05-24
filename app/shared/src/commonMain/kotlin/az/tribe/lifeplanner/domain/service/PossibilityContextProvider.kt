package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.MilestoneCandidate
import az.tribe.lifeplanner.domain.model.PossibilityContext
import az.tribe.lifeplanner.domain.model.TimeOfDay
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Pillar 2 — aggregates current context (time, energy/sleep/stress, what's pending /
 * due / stalled) into a [PossibilityContext] for the [PossibilityEngine] to rank.
 *
 * NOTE: "open calendar slot" (`freeMinutes`) is left null — there is no calendar
 * free/busy API yet (only a permission launcher). That's a follow-up; the engine
 * degrades gracefully when it's null.
 */
class PossibilityContextProvider(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val userSituationRepository: UserSituationRepository,
) {
    suspend fun currentContext(
        now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): PossibilityContext {
        val today = now.date

        val situation = runCatching { userSituationRepository.observe().firstOrNull() }.getOrNull()

        val pendingHabits = runCatching { habitRepository.observeHabitsWithTodayStatus().firstOrNull() }
            .getOrNull()
            .orEmpty()
            .filter { (habit, doneToday) -> habit.isActive && !doneToday }
            .map { it.first }

        val activeGoals = runCatching { goalRepository.getActiveGoals() }.getOrDefault(emptyList())

        val openMilestones = activeGoals.flatMap { goal ->
            goal.milestones.filter { !it.isCompleted }.map { MilestoneCandidate(goal, it) }
        }
        val goalsWithOpenMilestone = openMilestones.mapTo(mutableSetOf()) { it.goal.id }

        // Due within a week (incl. overdue) OR stalled (>2 weeks old, <25% progress),
        // excluding goals already represented by an open milestone.
        val dueOrStalledGoals = activeGoals.filter { goal ->
            if (goal.id in goalsWithOpenMilestone) return@filter false
            val due = today.daysUntil(goal.dueDate) <= 7
            val stalled = (goal.progress ?: 0L) < 25L && goal.createdAt.date.daysUntil(today) > 14
            due || stalled
        }

        return PossibilityContext(
            now = now,
            timeOfDay = TimeOfDay.fromHour(now.hour),
            energy = situation?.body?.energyRating,
            stressLevel = situation?.meta?.stressLevel,
            sleepQuality = situation?.meta?.sleepQuality,
            freeMinutes = null,
            pendingHabits = pendingHabits,
            openMilestones = openMilestones,
            dueOrStalledGoals = dueOrStalledGoals,
        )
    }
}
