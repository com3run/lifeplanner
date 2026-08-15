package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.Mood
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DaySnapshot(
    val date: LocalDate,
    val habitSummary: HabitDaySummary,
    val journalEntries: List<JournalEntry>,
    val focusSessions: List<FocusSession>,
    val goalChanges: List<GoalChangeWithTitle>,
    val badgesEarned: List<Badge>
) {
    // Null on a tie as well as on an empty day: one happy and one anxious entry has no dominant
    // mood, and picking whichever grouped first narrated the user a feeling they never led with.
    val dominantMood: Mood?
        get() {
            val counts = journalEntries.groupingBy { it.mood }.eachCount()
            val top = counts.maxByOrNull { it.value } ?: return null
            return top.key.takeIf { counts.count { c -> c.value == top.value } == 1 }
        }

    val totalFocusMinutes: Int
        get() = focusSessions.sumOf { it.actualDurationSeconds } / 60

    val xpEarnedOnDay: Int
        get() = focusSessions.sumOf { it.xpEarned }

    val hasAnyActivity: Boolean
        get() = habitSummary.completedHabits > 0 ||
                journalEntries.isNotEmpty() ||
                focusSessions.isNotEmpty() ||
                goalChanges.isNotEmpty() ||
                badgesEarned.isNotEmpty()
}

data class HabitDaySummary(
    val totalHabits: Int,
    val completedHabits: Int,
    val habits: List<HabitDayStatus>
)

data class HabitDayStatus(
    val habitId: String,
    val title: String,
    val category: GoalCategory,
    val wasCompleted: Boolean,
    val notes: String,
    val linkedGoalId: String? = null
)

data class GoalChangeWithTitle(
    val id: String,
    val goalId: String,
    val goalTitle: String,
    val field: String,
    val oldValue: String?,
    val newValue: String?,
    val changedAt: LocalDateTime
)
