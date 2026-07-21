package az.tribe.lifeplanner.usecases.health

import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import az.tribe.lifeplanner.notification.notifyNow
import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Checks off habits linked to a health metric once today's data meets their target:
 * STEPS and SLEEP compare against [Habit.healthTarget] (with sensible defaults),
 * WEIGHT and HEART_RATE complete on any reading today (tracking habits).
 * Runs after every health sync; skips habits that already have a check-in today
 * so it never fights a manual check-in or double-awards XP.
 */
class AutoCompleteHealthHabitsUseCase(
    private val habitRepository: HabitRepository,
    private val healthRepository: HealthRepository,
    private val gamificationRepository: GamificationRepository,
) {
    suspend operator fun invoke(): List<Habit> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val linked = runCatching { habitRepository.getAllHabits() }
            .getOrElse { return emptyList() }
            .filter { it.isActive && it.healthMetricType != null }
        if (linked.isEmpty()) return emptyList()

        val completed = mutableListOf<Habit>()
        for (habit in linked) {
            val metricType = habit.healthMetricType ?: continue
            runCatching {
                if (habitRepository.getCheckInByHabitAndDate(habit.id, today) != null) return@runCatching
                val todayMetrics = healthRepository.getMetricsInRange(metricType, today, today)
                val met = when (metricType) {
                    HealthMetricType.STEPS ->
                        todayMetrics.sumOf { it.value } >= (habit.healthTarget ?: DEFAULT_STEPS_TARGET)
                    HealthMetricType.SLEEP ->
                        (todayMetrics.maxOfOrNull { it.value } ?: 0.0) >= (habit.healthTarget ?: DEFAULT_SLEEP_HOURS)
                    HealthMetricType.WEIGHT, HealthMetricType.HEART_RATE ->
                        todayMetrics.isNotEmpty()
                }
                if (met) {
                    habitRepository.checkIn(habit.id, today, notes = AUTO_NOTE)
                    gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
                    completed += habit
                }
            }.onFailure {
                Logger.w(TAG) { "auto-complete failed for '${habit.title}': ${it.message}" }
            }
        }

        if (completed.isNotEmpty()) {
            val message = if (completed.size == 1) {
                "\"${completed.first().title}\" is done, your health data confirmed it. Nice work."
            } else {
                "${completed.size} habits are done, your health data confirmed them. Nice work."
            }
            notifyNow(
                id = "health_auto_complete_$today",
                title = "Checked off automatically",
                message = message
            )
            Logger.i(TAG) { "auto-completed ${completed.size} habit(s) from health data" }
        }
        return completed
    }

    companion object {
        const val AUTO_NOTE = "Auto-completed from health data"
        private const val DEFAULT_STEPS_TARGET = 10_000.0
        private const val DEFAULT_SLEEP_HOURS = 7.0
        private const val TAG = "AutoCompleteHealthHabits"
    }
}
