package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.roundToHalf
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.domain.repository.WheelRepository
import az.tribe.lifeplanner.domain.service.AreaSignals
import az.tribe.lifeplanner.domain.service.GlobalSignals
import az.tribe.lifeplanner.domain.service.WheelScorePredictor
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.clearWheelScoreLocal
import az.tribe.lifeplanner.infrastructure.observeWheelScores
import az.tribe.lifeplanner.infrastructure.setWheelScoreLocal
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Builds the wheel by predicting from live app data, then letting the user's own scores win.
 *
 * Signal gathering is deliberately best-effort: one repository failing should cost that area its
 * confidence, not take the whole wheel down with it.
 */
class WheelRepositoryImpl(
    private val db: SharedDatabase,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val journalRepository: JournalRepository,
    private val healthRepository: HealthRepository,
    private val abilityRepository: AbilityRepository,
    private val knowledgeReadCount: suspend () -> Int,
    private val syncManager: SyncManager,
) : WheelRepository {

    private val log = Logger.withTag("WheelRepository")

    override fun observeWheel(): Flow<WheelReport> =
        db.observeWheelScores().map { entities ->
            build(entities.associate { it.id to it.score })
        }

    override suspend fun getWheel(): WheelReport = observeWheel().first()

    override suspend fun setScore(area: WheelArea, score: Double, note: String?) {
        db.setWheelScoreLocal(area.name, score.roundToHalf(), note)
        syncManager.requestSync()
    }

    override suspend fun clearScore(area: WheelArea) {
        db.clearWheelScoreLocal(area.name)
        syncManager.requestSync()
    }

    private suspend fun build(userScores: Map<String, Double>): WheelReport {
        val scores = WheelScorePredictor.predict(
            signalsByArea = gatherAreaSignals(),
            global = gatherGlobalSignals(),
            userScores = userScores.mapNotNull { (id, score) ->
                // An unknown id means an area we renamed or dropped; ignore rather than crash.
                WheelArea.entries.firstOrNull { it.name == id }?.let { it to score }
            }.toMap(),
        )

        return WheelReport(
            id = "wheel",
            scores = scores,
            generatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        )
    }

    private suspend fun gatherAreaSignals(): Map<WheelArea, AreaSignals> {
        val goals = runCatching { goalRepository.getAllGoals() }
            .onFailure { log.w { "goals unavailable: ${it.message}" } }
            .getOrDefault(emptyList())
        val habits = runCatching { habitRepository.getAllHabits() }
            .onFailure { log.w { "habits unavailable: ${it.message}" } }
            .getOrDefault(emptyList())

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Completion rate is a per-habit query, so cache it once rather than per area. Areas share
        // categories (Mission and Growth are both CAREER), which would otherwise re-query.
        val completionRates = habits.associate { habit ->
            habit.id to runCatching { habitRepository.getHabitCompletionRate(habit.id, days = 30) }
                .getOrDefault(0f)
        }

        return WheelArea.segments().associateWith { area ->
            if (area.categories.isEmpty()) return@associateWith AreaSignals()

            val areaGoals = goals.filter { it.category in area.categories }
            val areaHabits = habits.filter { it.isActive && it.category in area.categories }
            val rates = areaHabits.mapNotNull { completionRates[it.id] }

            AreaSignals(
                activeGoals = areaGoals.count { it.status == GoalStatus.IN_PROGRESS },
                completedGoals = areaGoals.count { it.status == GoalStatus.COMPLETED },
                plannedGoals = areaGoals.count { it.status != GoalStatus.COMPLETED && it.dueDate >= today },
                habits = areaHabits.size,
                habitCompletionRate = rates.takeIf { it.isNotEmpty() }
                    ?.let { r -> r.map { it.toDouble() }.average() } ?: 0.0,
                longestStreak = areaHabits.maxOfOrNull { it.currentStreak } ?: 0,
            )
        }
    }

    private suspend fun gatherGlobalSignals(): GlobalSignals {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monthAgo = today.minusDaysSafe(30)
        val weekAgo = today.minusDaysSafe(7)

        val entries = runCatching { journalRepository.getEntriesInRange(monthAgo, today) }
            .onFailure { log.w { "journal unavailable: ${it.message}" } }
            .getOrDefault(emptyList())
        val moods = entries.map { it.mood.score.toDouble() }

        val steps = runCatching {
            healthRepository.getMetricsInRange(HealthMetricType.STEPS, weekAgo, today)
                .map { it.value }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toInt()
        }.getOrNull()

        val sleep = runCatching {
            healthRepository.getMetricsInRange(HealthMetricType.SLEEP, weekAgo, today)
                .map { it.value }
                .takeIf { it.isNotEmpty() }
                ?.average()
        }.getOrNull()

        val abilities = runCatching { abilityRepository.observeAllAbilities().first().size }
            .onFailure { log.w { "abilities unavailable: ${it.message}" } }
            .getOrDefault(0)

        val lessons = runCatching { knowledgeReadCount() }
            .onFailure { log.w { "lesson reads unavailable: ${it.message}" } }
            .getOrDefault(0)

        return GlobalSignals(
            averageMood = moods.takeIf { it.isNotEmpty() }?.average(),
            journalEntriesThisMonth = entries.size,
            averageDailySteps = steps,
            averageSleepHours = sleep,
            lessonsReadThisMonth = lessons,
            abilitiesInProgress = abilities,
            focusSessionsThisMonth = 0,
        )
    }
}

/** kotlinx-datetime has no clamped minus, and a date before the epoch would be nonsense here. */
private fun LocalDate.minusDaysSafe(days: Int): LocalDate =
    LocalDate.fromEpochDays((this.toEpochDays() - days).coerceAtLeast(0L))
