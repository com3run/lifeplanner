package az.tribe.lifeplanner.usecases.habit

import az.tribe.lifeplanner.domain.enum.HabitCompletionSource
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.trackMode
import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** What a finished session did to one habit. */
data class HabitCredit(
    val habit: Habit,
    val newCount: Int,
    val completed: Boolean,
    val xpAwarded: Int,
)

/**
 * Credits the habits a user linked to an in-app session, so finishing the session inside
 * LifePlanner is the check-in — no second trip to the habit list.
 *
 * A minutes habit takes the minutes the session lasted, so a 25-minute focus block fills a
 * "Meditate 10 min" habit outright. Everything else gains one per session, which is what makes
 * "three breaths a day" work against a habit with a target of three.
 */
class CreditHabitsFromSessionUseCase(
    private val habitRepository: HabitRepository,
    private val awardHabitCompletion: AwardHabitCompletionUseCase,
) {
    suspend operator fun invoke(
        source: HabitCompletionSource,
        minutes: Int = 1,
        date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    ): List<HabitCredit> {
        if (source == HabitCompletionSource.MANUAL) return emptyList()

        val linked = runCatching { habitRepository.getAllHabits() }
            .getOrElse {
                Logger.w(TAG) { "could not read habits: ${it.message}" }
                return emptyList()
            }
            .filter { it.isActive && it.completionSource == source }
        if (linked.isEmpty()) return emptyList()

        return linked.mapNotNull { habit ->
            runCatching { credit(habit, minutes, date) }
                .onFailure { Logger.w(TAG) { "credit failed for '${habit.title}': ${it.message}" } }
                .getOrNull()
        }
    }

    private suspend fun credit(habit: Habit, minutes: Int, date: LocalDate): HabitCredit? {
        // Already done today: nothing to add, and never award twice for the same day.
        val existing = habitRepository.getCheckInByHabitAndDate(habit.id, date)
        if (existing?.completed == true) return null

        val delta = when (habit.trackMode) {
            HabitTrackMode.DURATION -> minutes.coerceAtLeast(1)
            else -> 1
        }
        val checkIn = habitRepository.addCount(habit.id, date, delta)
        val xp = if (checkIn.completed) awardHabitCompletion(habit.id, date) else 0
        return HabitCredit(
            habit = habit,
            newCount = checkIn.count,
            completed = checkIn.completed,
            xpAwarded = xp,
        )
    }

    private companion object {
        const val TAG = "CreditHabitsFromSession"
    }
}
