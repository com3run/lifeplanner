package az.tribe.lifeplanner.usecases.habit

import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.HabitNumericParser
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings

/**
 * One-time backfill: habits created before track modes existed are all single-check
 * (targetCount 1, no unit) even when their title clearly names a count, e.g.
 * "Drink 8 glasses of water". Re-parses titles and upgrades those habits so the card
 * shows real progress (0 of 8 glasses) instead of one tap. Runs once, guarded by a
 * Settings flag; the flag is only set after a full successful pass so a crash retries
 * next launch (updates are idempotent).
 */
class BackfillHabitTargetsUseCase(
    private val habitRepository: HabitRepository,
    private val settings: Settings,
) {
    suspend operator fun invoke() {
        if (settings.getBoolean(KEY_DONE, false)) return
        val habits = runCatching { habitRepository.getAllHabits() }.getOrElse { return }

        var upgraded = 0
        for (habit in habits) {
            if (habit.targetCount > 1 || habit.unit != null) continue
            val parsed = HabitNumericParser.parse(habit.title)
                ?: habit.description.takeIf { it.isNotBlank() }?.let { HabitNumericParser.parse(it) }
                ?: continue
            val (count, unit) = parsed
            // "Walk 5000 steps" belongs to health sync, not 5000 manual taps; hours become minutes.
            if (unit == "steps") continue
            val (targetCount, normalizedUnit) = if (unit == "hrs") count * 60 to "min" else count to unit
            if (targetCount !in 2..MAX_SANE_TARGET) continue

            runCatching {
                habitRepository.updateHabit(habit.copy(targetCount = targetCount, unit = normalizedUnit))
                upgraded++
            }.onFailure {
                Logger.w(TAG) { "backfill failed for '${habit.title}': ${it.message}" }
                return
            }
        }

        settings.putBoolean(KEY_DONE, true)
        if (upgraded > 0) Logger.i(TAG) { "upgraded $upgraded habit(s) to count/minutes targets" }
    }

    companion object {
        private const val KEY_DONE = "habit_target_backfill_done"
        private const val MAX_SANE_TARGET = 180
        private const val TAG = "BackfillHabitTargets"
    }
}
