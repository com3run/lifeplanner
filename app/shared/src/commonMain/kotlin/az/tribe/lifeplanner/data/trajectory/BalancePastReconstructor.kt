package az.tribe.lifeplanner.data.trajectory

import az.tribe.lifeplanner.domain.model.TrajectoryPoint
import az.tribe.lifeplanner.domain.repository.HabitRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Reconstructs a real "how it's gone" curve for the trajectory graph from the user's actual habit
 * check-in history, no fabricated data. Each past week becomes a consistency score (completed
 * check-ins vs. the possible maximum), and the shape is scaled so the most recent week lands exactly
 * on the true current balance score, giving a continuous line into the projections.
 *
 * (A persisted daily balance snapshot could make the older weeks exact balance scores rather than a
 * consistency proxy; that's a natural future upgrade. For now this is real behavioural data.)
 */
class BalancePastReconstructor(
    private val habitRepository: HabitRepository,
) {
    suspend fun weeklyPast(weeks: Int, anchorScore: Float): List<TrajectoryPoint> {
        if (weeks <= 0) return listOf(TrajectoryPoint(0, anchorScore))
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)
        val habits = runCatching { habitRepository.getAllHabits() }.getOrDefault(emptyList())
        if (habits.isEmpty()) return listOf(TrajectoryPoint(0, anchorScore))

        val start = today.minus(DatePeriod(days = weeks * 7 - 1))
        val counts = IntArray(weeks)
        for (h in habits) {
            val checkIns = runCatching { habitRepository.getCheckInsInRange(h.id, start, today) }
                .getOrDefault(emptyList())
            for (ci in checkIns) {
                if (!ci.completed) continue
                val daysAgo = (today.toEpochDays() - ci.date.toEpochDays()).toInt()
                if (daysAgo < 0) continue
                val weekIdx = weeks - 1 - (daysAgo / 7)
                if (weekIdx in 0 until weeks) counts[weekIdx]++
            }
        }

        val maxPerWeek = (habits.size * 7).coerceAtLeast(1)
        val raw = FloatArray(weeks) { i -> (counts[i].toFloat() / maxPerWeek) * 100f }

        // Scale the whole shape so the latest week equals the real current balance (continuity),
        // clamping the factor so a near-zero recent week can't blow the curve up.
        val last = raw.last()
        val factor = if (last > 1f) (anchorScore / last).coerceIn(0.25f, 4f) else 1f

        return (0 until weeks).map { i ->
            val weekOffset = -(weeks - 1 - i)
            val score = if (weekOffset == 0) anchorScore else (raw[i] * factor).coerceIn(0f, 100f)
            TrajectoryPoint(weekOffset, score)
        }
    }
}
