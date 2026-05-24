package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.repository.GoalHistoryRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil

/**
 * Pillar 4 — builds calibration samples from completed goals. Actual completion is inferred
 * from the goal's status→COMPLETED entry in GoalHistory (we deliberately didn't add a
 * completedAt column); goals without a predictedDueDate or a logged completion are skipped.
 */
class CalibrationProvider(
    private val goalRepository: GoalRepository,
    private val goalHistoryRepository: GoalHistoryRepository,
    private val engine: CalibrationEngine
) {
    suspend fun calibration(): Calibration? {
        val completed = runCatching { goalRepository.getCompletedGoals() }.getOrDefault(emptyList())
        val samples = completed.mapNotNull { goal ->
            val predicted = goal.predictedDueDate ?: return@mapNotNull null
            val start = goal.createdAt.date
            val completion = completionDateOf(goal.id) ?: return@mapNotNull null
            CalibrationEngine.Sample(
                predictedDays = start.daysUntil(predicted),
                actualDays = start.daysUntil(completion)
            )
        }
        return engine.calibrate(samples)
    }

    private suspend fun completionDateOf(goalId: String): LocalDate? {
        val history = runCatching { goalHistoryRepository.getHistoryForGoal(goalId) }.getOrDefault(emptyList())
        val completedChange = history
            .filter { it.field == "status" && it.newValue.contains("COMPLETED", ignoreCase = true) }
            .maxByOrNull { it.changedAt } // ISO timestamps sort lexicographically == chronologically
            ?: return null
        return parseDate(completedChange.changedAt)
    }

    private fun parseDate(s: String): LocalDate? =
        runCatching { LocalDateTime.parse(s).date }
            .getOrElse { runCatching { LocalDate.parse(s.take(10)) }.getOrNull() }
}
