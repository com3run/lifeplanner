package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ActionOptionType
import az.tribe.lifeplanner.domain.model.MilestoneCandidate
import az.tribe.lifeplanner.domain.model.PossibilityContext
import az.tribe.lifeplanner.domain.model.TimeOfDay
import az.tribe.lifeplanner.testutil.testGoal
import az.tribe.lifeplanner.testutil.testHabit
import az.tribe.lifeplanner.testutil.testMilestone
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PossibilityEngineTest {

    private val engine = PossibilityEngine()
    private val now = LocalDateTime(2026, 3, 6, 9, 0, 0) // a Friday morning
    private val today: LocalDate = now.date

    private fun context(
        timeOfDay: TimeOfDay = TimeOfDay.MORNING,
        energy: Int? = null,
        freeMinutes: Int? = null,
        pendingHabits: List<az.tribe.lifeplanner.domain.model.Habit> = emptyList(),
        openMilestones: List<MilestoneCandidate> = emptyList(),
        dueOrStalledGoals: List<az.tribe.lifeplanner.domain.model.Goal> = emptyList(),
    ) = PossibilityContext(
        now = now,
        timeOfDay = timeOfDay,
        energy = energy,
        stressLevel = null,
        sleepQuality = null,
        freeMinutes = freeMinutes,
        pendingHabits = pendingHabits,
        openMilestones = openMilestones,
        dueOrStalledGoals = dueOrStalledGoals,
    )

    @Test
    fun `low-energy morning ranks a streak habit first`() {
        val options = engine.rank(
            context(
                timeOfDay = TimeOfDay.MORNING,
                energy = 2,
                pendingHabits = listOf(testHabit(id = "h1", title = "Meditate", currentStreak = 5)),
                openMilestones = listOf(
                    MilestoneCandidate(testGoal(id = "g1"), testMilestone(id = "m1", title = "Outline"))
                ),
            )
        )
        assertEquals(ActionOptionType.HABIT, options.first().type)
        assertEquals("h1", options.first().refId)
        assertTrue(options.first().fitReason.contains("streak"))
    }

    @Test
    fun `high energy promotes a milestone to a focus session`() {
        val options = engine.rank(
            context(
                timeOfDay = TimeOfDay.AFTERNOON,
                energy = 5,
                openMilestones = listOf(
                    MilestoneCandidate(testGoal(id = "g1"), testMilestone(id = "m1", title = "Draft chapter"))
                ),
            )
        )
        val focus = options.single { it.refId == "m1" }
        assertEquals(ActionOptionType.FOCUS, focus.type)
        assertTrue(focus.title.startsWith("Focus on"))
    }

    @Test
    fun `milestone fit reason includes free minutes and energy when present`() {
        val options = engine.rank(
            context(
                energy = 5,
                freeMinutes = 45,
                timeOfDay = TimeOfDay.AFTERNOON,
                openMilestones = listOf(
                    MilestoneCandidate(testGoal(id = "g1"), testMilestone(id = "m1", title = "Draft"))
                ),
            )
        )
        val reason = options.single { it.refId == "m1" }.fitReason
        assertTrue(reason.contains("45 min free"), "expected free minutes in: $reason")
        assertTrue(reason.contains("high energy"), "expected energy in: $reason")
    }

    @Test
    fun `overdue goal outranks a far-future goal`() {
        val overdue = testGoal(id = "overdue", title = "Taxes", dueDate = today.plus(DatePeriod(days = -5)))
        val far = testGoal(id = "far", title = "Someday", dueDate = today.plus(DatePeriod(months = 6)))
        val options = engine.rank(context(dueOrStalledGoals = listOf(far, overdue)))

        assertEquals("overdue", options.first().refId)
        assertTrue(options.first().fitReason.contains("Overdue"))
    }

    @Test
    fun `due-soon goal reason mentions the day count`() {
        val soon = testGoal(id = "soon", dueDate = today.plus(DatePeriod(days = 3)))
        val option = engine.rank(context(dueOrStalledGoals = listOf(soon))).single()
        assertTrue(option.fitReason.contains("Due in 3 days"), "was: ${option.fitReason}")
    }

    @Test
    fun `returns at most five options`() {
        val habits = (1..8).map { testHabit(id = "h$it", title = "Habit $it") }
        assertTrue(engine.rank(context(pendingHabits = habits)).size <= 5)
    }

    @Test
    fun `empty context yields no options`() {
        assertTrue(engine.rank(context()).isEmpty())
    }
}
