package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.domain.model.CausalInsight
import az.tribe.lifeplanner.domain.model.DayMetrics
import az.tribe.lifeplanner.domain.repository.FocusRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import co.touchlab.kermit.Logger
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock

/**
 * Pillar 4, aggregates the user's recent history from the repos into [DayMetrics] and runs
 * the pure [CausalInsightEngine] over it. Energy has no per-day time series today (only a
 * UserSituation snapshot) so it's left null; the engine simply skips energy relationships.
 */
class CausalInsightProvider(
    private val habitRepository: HabitRepository,
    private val journalRepository: JournalRepository,
    private val focusRepository: FocusRepository,
    private val healthRepository: HealthRepository,
    private val engine: CausalInsightEngine
) {
    suspend fun insights(windowDays: Int = 90): List<CausalInsight> =
        engine.analyze(buildDays(windowDays))

    suspend fun buildDays(windowDays: Int = 90): List<DayMetrics> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = today.minus(DatePeriod(days = windowDays))

        val habitsByDay = safe { habitRepository.getAllCheckInsInRange(start, today) }
            .filter { it.completed }
            .groupBy { it.date }
            .mapValues { (_, v) -> v.size }

        val moodByDay = safe { journalRepository.getEntriesInRange(start, today) }
            .groupBy { it.date }
            .mapValues { (_, v) -> v.map { it.mood.score }.average().roundToInt() }

        val focusByDay = safe { focusRepository.getCompletedSessions() }
            .filter { it.startedAt.date in start..today }
            .groupBy { it.startedAt.date }
            .mapValues { (_, v) -> v.sumOf { it.actualMinutes } }

        val sleepByDay = safe { healthRepository.getMetricsInRange(HealthMetricType.SLEEP, start, today) }
            .groupBy { it.date }
            .mapValues { (_, v) -> v.map { m -> m.value }.average() }

        val dates = (habitsByDay.keys + moodByDay.keys + focusByDay.keys + sleepByDay.keys).sorted()
        return dates.map { d ->
            DayMetrics(
                date = d,
                habitsCompleted = habitsByDay[d],
                mood = moodByDay[d],
                focusMinutes = focusByDay[d],
                sleepHours = sleepByDay[d],
                energy = null,
                goalProgressDelta = null
            )
        }
    }

    private suspend fun <T> safe(block: suspend () -> List<T>): List<T> =
        runCatching { block() }.getOrElse {
            Logger.w("CausalInsightProvider") { "signal fetch failed: ${it.message}" }
            emptyList()
        }
}
