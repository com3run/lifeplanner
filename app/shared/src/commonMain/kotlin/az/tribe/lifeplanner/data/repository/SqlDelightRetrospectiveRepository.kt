package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.data.mapper.parseLocalDateTime
import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.Badge
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.model.GoalChangeWithTitle
import az.tribe.lifeplanner.domain.model.HabitDayStatus
import az.tribe.lifeplanner.domain.model.HabitDaySummary
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class SqlDelightRetrospectiveRepository(
    private val database: SharedDatabase
) : RetrospectiveRepository {

    override suspend fun getDaySnapshot(date: LocalDate): DaySnapshot {
        val dateStr = date.toString()

        // 1. Habits with check-in status for this date
        val habitsRaw = database.getHabitCheckInsWithHabitForDate(dateStr)
        val habitStatuses = habitsRaw.map { row ->
            HabitDayStatus(
                habitId = row.id,
                title = row.title,
                category = try { GoalCategory.valueOf(row.category) } catch (_: Exception) { GoalCategory.CAREER },
                wasCompleted = row.completed == 1L,
                notes = row.checkInNotes ?: "",
                linkedGoalId = row.linkedGoalId
            )
        }

        val habitSummary = HabitDaySummary(
            totalHabits = habitStatuses.size,
            completedHabits = habitStatuses.count { it.wasCompleted },
            habits = habitStatuses
        )

        // 2. Journal entries for this date (reuse existing query)
        val journalEntries = database.getJournalEntriesByDate(dateStr).map { it.toDomain() }

        // 3. Focus sessions for this date
        val focusSessions = database.getFocusSessionsByDate(dateStr).map { it.toDomain() }

        // 4. Goal changes on this date
        val goalChangesRaw = database.getGoalChangesOnDate(dateStr)
        val goalChanges = goalChangesRaw.map { row ->
            GoalChangeWithTitle(
                id = row.id,
                goalId = row.goalId,
                goalTitle = row.goalTitle ?: "Deleted Goal",
                field = row.fieldName,
                oldValue = row.oldValue,
                newValue = row.newValue,
                changedAt = parseLocalDateTime(row.changedAt)
            )
        }

        // 5. Badges earned on this date
        val badges = database.getBadgesEarnedOnDate(dateStr).map { entity ->
            Badge(
                id = entity.id,
                type = try { BadgeType.valueOf(entity.badgeType) } catch (_: Exception) { BadgeType.FIRST_STEP },
                earnedAt = parseLocalDateTime(entity.earnedAt),
                isNew = entity.isNew == 1L
            )
        }

        return DaySnapshot(
            date = date,
            habitSummary = habitSummary,
            journalEntries = journalEntries,
            focusSessions = focusSessions,
            goalChanges = goalChanges,
            badgesEarned = badges
        )
    }

    override fun observeWeeklySnapshots(): Flow<List<DaySnapshot>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dates = (6 downTo 0).map { today.minus(DatePeriod(days = it)) }
        val startDate = dates.first().toString()
        val endDate = dates.last().toString()

        return combine(
            database.observeAllHabits(),
            database.observeCheckInsInRange(startDate, endDate),
            database.observeAllJournalEntries(),
            database.observeAllFocusSessions()
        ) { _, _, _, _ -> Unit }
            .map { dates.map { date -> getDaySnapshot(date) } }
    }

    override suspend fun getDayRecap(date: LocalDate): String? {
        return database.getDayRecap(date.toString())?.recap
    }

    override suspend fun saveDayRecap(date: LocalDate, recap: String) {
        database.insertOrReplaceDayRecap(
            date = date.toString(),
            recap = recap,
            generatedAt = Clock.System.now().toString()
        )
    }

    override suspend fun getDatesWithActivity(start: LocalDate, end: LocalDate): Set<LocalDate> {
        val startStr = start.toString()
        val endStr = end.toString()

        return database.getDatesWithActivity(
            checkInStart = startStr, checkInEnd = endStr,
            journalStart = startStr, journalEnd = endStr,
            focusStart = startStr, focusEnd = endStr,
            historyStart = startStr, historyEnd = endStr,
            badgeStart = startStr, badgeEnd = endStr
        ).mapNotNull { dateStr ->
            try { LocalDate.parse(dateStr) } catch (_: Exception) { null }
        }.toSet()
    }
}
