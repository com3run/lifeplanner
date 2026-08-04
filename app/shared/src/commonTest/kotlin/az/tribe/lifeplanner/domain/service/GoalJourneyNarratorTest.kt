package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.testutil.testGoal
import az.tribe.lifeplanner.testutil.testMilestone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoalJourneyNarratorTest {

    private fun milestones(done: Int, total: Int) = (1..total).map {
        testMilestone(id = "m$it", title = "Step $it", isCompleted = it <= done)
    }

    @Test
    fun noMilestonesInvitesWithoutDemanding() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = emptyList()))
        assertEquals("Chapter 1", j.chapterLabel)
        assertNull(j.teaser)
        assertFalse(j.isComplete)
        // A goal without milestones is allowed to stay that way. The copy offers them as an option
        // ("when you know what they are") rather than describing the goal as missing a map.
        assertTrue("milestones" in j.story)
        assertFalse("does not" in j.story)
    }

    @Test
    fun aPracticeGoalIsNeverToldItIsMissingSomething() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = emptyList()), isPractice = true)

        assertNull(j.teaser)
        assertFalse(j.isComplete)
        // The habits are the goal. Telling this user to go add milestones invents a problem: there
        // is nothing to tick off and that is the correct shape for the goal, not a gap in it.
        assertFalse("milestone" in j.story.lowercase())
        assertTrue("keep doing" in j.story)
    }

    @Test
    fun notStartedIsChapterOneAndTeasesFirstMilestone() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = milestones(done = 0, total = 4)))
        assertEquals("Chapter 1 of 4", j.chapterLabel)
        assertTrue(j.teaser!!.contains("Step 1"))
        assertFalse(j.isFinalStep)
    }

    @Test
    fun inProgressCountsChaptersAndTeasesNextIncomplete() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = milestones(done = 2, total = 4)))
        assertEquals("Chapter 3 of 4", j.chapterLabel)
        assertTrue(j.teaser!!.contains("Step 3"))
        assertFalse(j.isFinalStep)
    }

    @Test
    fun lastRemainingMilestoneGetsTheAffectionateEnding() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = milestones(done = 3, total = 4)))
        assertEquals("The last chapter", j.chapterLabel)
        assertTrue(j.isFinalStep)
        assertTrue(j.teaser!!.contains("Step 4"))
        assertTrue("ending" in j.teaser!!)
    }

    @Test
    fun allMilestonesDoneReadsAsFinishedStory() {
        val j = GoalJourneyNarrator.narrate(testGoal(milestones = milestones(done = 4, total = 4)))
        assertTrue(j.isComplete)
        assertNull(j.teaser)
        assertTrue("story you finished" in j.story)
    }

    @Test
    fun completedStatusWinsEvenWithOpenMilestones() {
        val j = GoalJourneyNarrator.narrate(
            testGoal(status = GoalStatus.COMPLETED, milestones = milestones(done = 1, total = 3))
        )
        assertTrue(j.isComplete)
        assertFalse(j.isFinalStep)
    }

    @Test
    fun narrationIsStablePerGoal() {
        val goal = testGoal(id = "stable-goal", milestones = milestones(done = 1, total = 3))
        assertEquals(GoalJourneyNarrator.narrate(goal), GoalJourneyNarrator.narrate(goal))
    }
}
