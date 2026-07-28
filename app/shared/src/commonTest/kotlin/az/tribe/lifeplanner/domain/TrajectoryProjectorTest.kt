package az.tribe.lifeplanner.domain

import az.tribe.lifeplanner.domain.model.TrajectoryPoint
import az.tribe.lifeplanner.domain.service.TrajectoryProjector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrajectoryProjectorTest {

    @Test
    fun currentPaceContinuesTheRecentTrend() {
        // Rising 2 points/week over the past, anchored at 60 now.
        val past = listOf(TrajectoryPoint(-2, 56f), TrajectoryPoint(-1, 58f), TrajectoryPoint(0, 60f))
        val s = TrajectoryProjector.project(past, weeksAhead = 4, improvement = 0f, idealScore = 85f)
        // ~+2/week for 4 weeks from 60 -> ~68.
        assertEquals(68f, s.currentPace.last().score, 0.5f)
    }

    @Test
    fun fullEffortReachesIdealByTheHorizon() {
        val past = listOf(TrajectoryPoint(0, 60f)) // flat, single point
        val s = TrajectoryProjector.project(past, weeksAhead = 10, improvement = 1f, idealScore = 90f)
        assertEquals(90, s.projectedEndScore)
    }

    @Test
    fun moreEffortProjectsHigherThanCurrentPace() {
        val past = listOf(TrajectoryPoint(-1, 50f), TrajectoryPoint(0, 50f))
        val low = TrajectoryProjector.project(past, 8, improvement = 0.2f, idealScore = 90f)
        val high = TrajectoryProjector.project(past, 8, improvement = 0.8f, idealScore = 90f)
        assertTrue(high.projectedEndScore > low.projectedEndScore)
    }

    @Test
    fun scoresClampToOneHundred() {
        val past = listOf(TrajectoryPoint(0, 95f))
        val s = TrajectoryProjector.project(past, 12, improvement = 1f, idealScore = 100f)
        assertTrue(s.couldBe.all { it.score <= 100f })
    }

    @Test
    fun handlesEmptyPastGracefully() {
        val s = TrajectoryProjector.project(emptyList(), 6, improvement = 0.5f, idealScore = 85f)
        assertTrue(s.currentPace.isNotEmpty() && s.couldBe.isNotEmpty())
    }
}
