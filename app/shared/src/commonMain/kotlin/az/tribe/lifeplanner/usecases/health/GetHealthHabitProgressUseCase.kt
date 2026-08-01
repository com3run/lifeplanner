package az.tribe.lifeplanner.usecases.health

import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Today's progress for habits wired to a health metric, so the home feed can show
 * "6,240 / 8,000" rather than an untickable checkbox the user has no way to action.
 *
 * The thresholds mirror [AutoCompleteHealthHabitsUseCase] exactly — same sum-vs-max rule per
 * metric, same defaults — because these two disagreeing would mean a row reading "almost there"
 * next to a habit the app has already ticked off, or the reverse.
 */
class GetHealthHabitProgressUseCase(
    private val habitRepository: HabitRepository,
    private val healthRepository: HealthRepository,
) {

    /**
     * @param current today's value for the metric (steps summed, sleep taken at its longest).
     * @param target the bar to clear, or null for habits that just want *a* reading today.
     * @param done whether the habit is already checked in, however that happened.
     */
    data class Progress(
        val habitId: String,
        val title: String,
        val metricType: HealthMetricType,
        val current: Double,
        val target: Double?,
        val done: Boolean,
    ) {
        /** 0f..1f, or null when there is no bar to fill (weight, heart rate). */
        val fraction: Float?
            get() = target?.takeIf { it > 0.0 }?.let { (current / it).coerceIn(0.0, 1.0).toFloat() }
    }

    suspend operator fun invoke(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): List<Progress> {
        val linked = runCatching { habitRepository.getAllHabits() }
            .getOrElse { return emptyList() }
            .filter { it.isActive && it.healthMetricType != null }
        if (linked.isEmpty()) return emptyList()

        return linked.mapNotNull { habit ->
            val metricType = habit.healthMetricType ?: return@mapNotNull null
            runCatching {
                val metrics = healthRepository.getMetricsInRange(metricType, date, date)
                val current = when (metricType) {
                    HealthMetricType.STEPS -> metrics.sumOf { it.value }
                    HealthMetricType.SLEEP -> metrics.maxOfOrNull { it.value } ?: 0.0
                    HealthMetricType.WEIGHT, HealthMetricType.HEART_RATE ->
                        metrics.lastOrNull()?.value ?: 0.0
                }
                val target = when (metricType) {
                    HealthMetricType.STEPS -> habit.healthTarget
                        ?: AutoCompleteHealthHabitsUseCase.DEFAULT_STEPS_TARGET
                    HealthMetricType.SLEEP -> habit.healthTarget
                        ?: AutoCompleteHealthHabitsUseCase.DEFAULT_SLEEP_HOURS
                    // Tracking habits: any reading today counts, so there is no bar to fill.
                    HealthMetricType.WEIGHT, HealthMetricType.HEART_RATE -> null
                }
                Progress(
                    habitId = habit.id,
                    title = habit.title,
                    metricType = metricType,
                    current = current,
                    target = target,
                    done = habitRepository.getCheckInByHabitAndDate(habit.id, date) != null,
                )
            }.onFailure {
                Logger.w(TAG) { "progress failed for '${habit.title}': ${it.message}" }
            }.getOrNull()
        }
    }

    private companion object {
        const val TAG = "GetHealthHabitProgress"
    }
}
