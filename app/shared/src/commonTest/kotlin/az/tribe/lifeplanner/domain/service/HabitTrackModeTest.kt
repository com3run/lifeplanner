package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.model.Habit
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HabitTrackModeTest {

    private fun habit(
        title: String = "Habit",
        targetCount: Int = 1,
        unit: String? = null,
    ) = Habit(
        id = "h",
        title = title,
        category = GoalCategory.BODY,
        frequency = HabitFrequency.DAILY,
        targetCount = targetCount,
        unit = unit,
        createdAt = LocalDateTime(2026, 1, 1, 0, 0),
    )

    @Test
    fun `plain habit is single`() {
        assertEquals(HabitTrackMode.SINGLE, habit().trackMode)
    }

    @Test
    fun `numeric target without a time unit is count`() {
        assertEquals(HabitTrackMode.COUNT, habit(targetCount = 8, unit = "glasses").trackMode)
        assertEquals(HabitTrackMode.COUNT, habit(targetCount = 20, unit = "pages").trackMode)
        assertEquals(HabitTrackMode.COUNT, habit(targetCount = 3, unit = "times").trackMode)
    }

    @Test
    fun `minutes make a habit duration, not a ten-tap count`() {
        // "Meditate 10min" is parsed to targetCount 10 + unit "min" by HabitNumericParser.
        // It must not ask the user for ten taps.
        assertEquals(HabitTrackMode.DURATION, habit(targetCount = 10, unit = "min").trackMode)
    }

    @Test
    fun `time units are recognised in every spelling the parser can emit`() {
        for (u in listOf("min", "mins", "minute", "minutes", "hr", "hrs", "hour", "hours", "MIN", " Min ")) {
            assertEquals(HabitTrackMode.DURATION, habit(targetCount = 30, unit = u).trackMode, "unit=$u")
        }
    }

    @Test
    fun `a single-minute habit is still duration`() {
        assertEquals(HabitTrackMode.DURATION, habit(targetCount = 1, unit = "min").trackMode)
    }

    @Test
    fun `targetMinutes is only set for duration habits`() {
        assertEquals(25, habit(targetCount = 25, unit = "min").targetMinutes)
        assertNull(habit(targetCount = 8, unit = "glasses").targetMinutes)
        assertNull(habit().targetMinutes)
    }

    @Test
    fun `isTimeUnit rejects countable units and null`() {
        assertEquals(false, isTimeUnit(null))
        assertEquals(false, isTimeUnit("glasses"))
        assertEquals(false, isTimeUnit("km"))
    }
}
