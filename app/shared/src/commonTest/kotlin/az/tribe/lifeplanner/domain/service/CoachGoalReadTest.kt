package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoachGoalReadTest {

    private fun snap(
        status: GoalStatus = GoalStatus.IN_PROGRESS,
        total: Int = 4,
        done: Int = 0,
        next: String? = "Open a savings account",
        daysUntilDue: Int = 30,
        ageDays: Int = 3,
        reflections: Int = 0,
        practiceDay: Int? = null,
        practiceStreak: Int? = null,
        areaIsLowest: Boolean = false,
    ) = GoalSnapshot(
        status = status,
        milestonesTotal = total,
        milestonesDone = done,
        nextStep = next,
        daysUntilDue = daysUntilDue,
        ageDays = ageDays,
        reflections = reflections,
        practiceDay = practiceDay,
        practiceStreak = practiceStreak,
        areaName = "Money",
        areaIsLowest = areaIsLowest,
    )

    @Test
    fun `the read changes when the goal changes`() {
        // The whole point. The old card said one sentence forever, so it could not be wrong and
        // could not be useful either.
        val fresh = CoachGoalRead.read("Morgan", snap())
        val moving = CoachGoalRead.read("Morgan", snap(done = 2))
        val nearlyDone = CoachGoalRead.read("Morgan", snap(done = 3, next = "Reach 3 months saved"))
        val overdue = CoachGoalRead.read("Morgan", snap(daysUntilDue = -12))

        assertTrue(setOf(fresh, moving, nearlyDone, overdue).size == 4, "states produced duplicate copy")
    }

    @Test
    fun `progress is stated with the actual numbers and the actual next step`() {
        val read = CoachGoalRead.read("Morgan", snap(total = 5, done = 2, next = "Save first month"))

        assertTrue("2 of 5" in read, read)
        assertTrue("Save first month" in read, read)
    }

    @Test
    fun `an overdue goal names the days and the open steps`() {
        val read = CoachGoalRead.read("Morgan", snap(total = 4, done = 1, daysUntilDue = -12))

        assertTrue("12 day" in read, read)
        assertTrue("3 step" in read, read)
        // The honest options, not a cheer. A date that has passed is information, not a failing.
        assertTrue("Move the date" in read, read)
    }

    @Test
    fun `a stalled goal says so plainly and shrinks the ask`() {
        val read = CoachGoalRead.read("Morgan", snap(done = 0, ageDays = 40))

        assertTrue("weeks" in read, read)
        // Someone who has not touched a goal in six weeks is not helped by enthusiasm, and the
        // failure they are most likely experiencing is a first step that is too big.
        assertTrue("too big" in read, read)
        assertFalse("!" in read, "a stalled goal was cheered at: $read")
    }

    @Test
    fun `a finished plan on an open goal points that out`() {
        val read = CoachGoalRead.read("Morgan", snap(total = 4, done = 4, next = null))

        assertTrue("finished" in read, read)
    }

    @Test
    fun `the last step is named`() {
        val read = CoachGoalRead.read("Morgan", snap(total = 4, done = 3, next = "Reach 3 months saved"))

        assertTrue("One step left" in read, read)
        assertTrue("Reach 3 months saved" in read, read)
    }

    @Test
    fun `a practice reads on its streak, not on steps it does not have`() {
        val running = CoachGoalRead.read("Morgan", snap(total = 0, next = null, practiceDay = 30, practiceStreak = 12))
        val broken = CoachGoalRead.read("Morgan", snap(total = 0, next = null, practiceDay = 30, practiceStreak = 0))

        assertTrue("12 days running" in running, running)
        // A broken streak is the moment a person is most likely to quit, so it gets the kindest
        // true thing rather than a scolding about the number being zero.
        assertTrue("counts for exactly as much" in broken, broken)
        assertFalse("step" in running.lowercase(), "a practice goal was read as a checklist: $running")
    }

    @Test
    fun `a goal with no plan is not told off for it`() {
        val read = CoachGoalRead.read("Morgan", snap(total = 0, done = 0, next = null, ageDays = 40))

        // Consistent with the rest of the app: milestones are optional. This offers a step and
        // explicitly blesses leaving it as an intention.
        assertTrue("allowed" in read, read)
        assertFalse("should" in read.lowercase(), "an optional plan was made to sound mandatory: $read")
    }

    @Test
    fun `the lowest area is mentioned, and only when it is the lowest`() {
        val lowest = CoachGoalRead.read("Morgan", snap(areaIsLowest = true))
        val ordinary = CoachGoalRead.read("Morgan", snap(areaIsLowest = false))

        assertTrue("lowest area" in lowest, lowest)
        // Naming a healthy area on every goal would be noise, and noise is what this replaced.
        assertFalse("Money" in ordinary, ordinary)
    }

    @Test
    fun `a completed goal looks back rather than forward`() {
        val withJournal = CoachGoalRead.read("Morgan", snap(status = GoalStatus.COMPLETED, done = 4, total = 4, reflections = 3))
        val without = CoachGoalRead.read("Morgan", snap(status = GoalStatus.COMPLETED, done = 4, total = 4))

        assertTrue("3 times" in withJournal, withJournal)
        assertTrue("Done" in without, without)
        assertFalse("Next" in withJournal, withJournal)
    }

    @Test
    fun `singulars and plurals are not mangled`() {
        val oneDay = CoachGoalRead.read("Morgan", snap(total = 2, done = 1, daysUntilDue = -1))

        assertTrue("1 day ago" in oneDay, oneDay)
        assertTrue("1 step is still open" in oneDay, oneDay)
    }

    @Test
    fun `no state produces an empty or truncated read`() {
        val states = listOf(
            snap(), snap(done = 2), snap(done = 4, next = null), snap(daysUntilDue = -5),
            snap(ageDays = 60, done = 0), snap(total = 0, next = null),
            snap(practiceDay = 1, practiceStreak = 1, total = 0, next = null),
            snap(status = GoalStatus.COMPLETED, done = 4),
            snap(status = GoalStatus.NOT_STARTED, ageDays = 1),
        )

        states.forEach { s ->
            val read = CoachGoalRead.read("Morgan", s)
            assertTrue(read.length > 30, "read too short to be worth showing: $read")
            assertTrue(read.trim().endsWith(".") || read.trim().endsWith("?"), "unfinished: $read")
            // A null next step must never reach the user as the word "null".
            assertFalse("null" in read, "a missing value leaked into the copy: $read")
        }
    }
}
