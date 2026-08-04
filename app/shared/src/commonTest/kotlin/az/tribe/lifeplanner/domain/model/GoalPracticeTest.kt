package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoalPracticeTest {

    private val today = LocalDate(2026, 8, 4)

    private fun habit(
        created: LocalDate,
        streak: Int = 0,
        longest: Int = 0,
        completions: Int = 0,
        active: Boolean = true,
    ) = Habit(
        id = "h-$created-$streak",
        title = "t",
        category = GoalCategory.PURPOSE,
        frequency = HabitFrequency.DAILY,
        currentStreak = streak,
        longestStreak = longest,
        totalCompletions = completions,
        isActive = active,
        createdAt = LocalDateTime(created.year, created.month, created.day, 9, 0),
    )

    @Test
    fun `a goal with no habits is not a practice`() {
        // It is an ordinary checklist goal and should keep being presented as one.
        assertNull(PracticeWindow.of(emptyList(), today))
    }

    @Test
    fun `the practice starts on the day the first habit did`() {
        val practice = PracticeWindow.of(
            listOf(habit(LocalDate(2026, 8, 1)), habit(LocalDate(2026, 7, 20))),
            today,
        )

        // Oldest habit wins: that is when the user actually started, whatever was added since.
        assertEquals(16, practice?.dayNumber)
    }

    @Test
    fun `the first day is day one, not day zero`() {
        val practice = PracticeWindow.of(listOf(habit(today)), today)

        // Someone who starts today is on day 1 of their practice. Day 0 reads as not started.
        assertEquals(1, practice?.dayNumber)
    }

    @Test
    fun `a habit dated in the future does not produce a practice that has not begun`() {
        val practice = PracticeWindow.of(listOf(habit(LocalDate(2026, 9, 1))), today)

        // Bad data, from a clock change or an import. Showing "day -28" is worse than showing day 1.
        assertEquals(1, practice?.dayNumber)
    }

    @Test
    fun `inactive habits do not count`() {
        val practice = PracticeWindow.of(
            listOf(habit(LocalDate(2026, 1, 1), active = false), habit(LocalDate(2026, 8, 1))),
            today,
        )

        // An archived habit should not backdate the practice to January.
        assertEquals(4, practice?.dayNumber)
    }

    @Test
    fun `a goal whose habits are all inactive is no longer a practice`() {
        assertNull(PracticeWindow.of(listOf(habit(today, active = false)), today))
    }

    @Test
    fun `the streak shown is the best one running, not the sum`() {
        val practice = PracticeWindow.of(
            listOf(habit(today, streak = 3, longest = 9), habit(today, streak = 7, longest = 12)),
            today,
        )

        // Two habits at 3 and 7 days is a 7-day streak, not a 10-day one.
        assertEquals(7, practice?.currentStreak)
        assertEquals(12, practice?.longestStreak)
    }

    @Test
    fun `check-ins add up across the habits`() {
        val practice = PracticeWindow.of(
            listOf(habit(today, completions = 4), habit(today, completions = 6)),
            today,
        )

        assertEquals(10, practice?.checkIns)
    }

    @Test
    fun `progress through the window never overflows`() {
        val practice = PracticeWindow.of(listOf(habit(LocalDate(2025, 1, 1))), today)

        // A year-old practice is not 560% of the way through. It is established, and the bar is
        // full rather than broken.
        assertEquals(1f, practice?.windowProgress)
        assertTrue(practice?.isEstablished == true)
    }

    @Test
    fun `a practice inside the window is not yet established`() {
        val practice = PracticeWindow.of(listOf(habit(LocalDate(2026, 7, 20))), today)

        assertTrue(practice?.isEstablished == false)
    }

    @Test
    fun `the window is the researched figure, not the folk one`() {
        // 21 days is the myth the app explicitly debunks elsewhere. If this ever becomes 21 the
        // rest of the app starts contradicting itself.
        assertEquals(66, PracticeWindow.DAYS_TO_AUTOMATIC)
    }
}
