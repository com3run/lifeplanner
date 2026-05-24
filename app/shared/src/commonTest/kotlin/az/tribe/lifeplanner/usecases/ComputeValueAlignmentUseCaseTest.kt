package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.testutil.testGoal
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ComputeValueAlignmentUseCaseTest {

    private val dt = LocalDateTime(2026, 1, 1, 9, 0)
    private fun decision(id: String, goalId: String?) =
        Decision(id = id, question = "Q", chosenOption = "keep", decidedAt = dt, relatedGoalId = goalId)

    @Test
    fun `counts completed goals and decisions per value`() {
        val v1 = LifeValue(id = "v1", title = "Health")
        val v2 = LifeValue(id = "v2", title = "Career")
        val goals = listOf(
            testGoal().copy(id = "g1", valueId = "v1", status = GoalStatus.COMPLETED),
            testGoal().copy(id = "g2", valueId = "v1", status = GoalStatus.COMPLETED),
            testGoal().copy(id = "g3", valueId = "v2", status = GoalStatus.COMPLETED),
            testGoal().copy(id = "g4", valueId = "v1", status = GoalStatus.IN_PROGRESS), // not completed
        )
        // d1 → g1 (v1), d2 → g3 (v2), d3 → no goal
        val decisions = listOf(decision("d1", "g1"), decision("d2", "g3"), decision("d3", null))

        val result = computeValueAlignment(listOf(v1, v2), goals, decisions)

        val r1 = result.first { it.valueId == "v1" }
        assertEquals(2, r1.completedGoalCount)
        assertEquals(1, r1.decisionCount)
        assertEquals(2.0 / 3.0, r1.goalShare, 0.001) // 2 of 3 value-tagged completed goals

        val r2 = result.first { it.valueId == "v2" }
        assertEquals(1, r2.completedGoalCount)
        assertEquals(1, r2.decisionCount)
        assertEquals(1.0 / 3.0, r2.goalShare, 0.001)
    }

    @Test
    fun `value with no goals reports zero without dividing by zero`() {
        val result = computeValueAlignment(listOf(LifeValue(id = "v9", title = "Unused")), emptyList(), emptyList())
        assertEquals(0, result.single().completedGoalCount)
        assertEquals(0.0, result.single().goalShare, 0.001)
    }

    @Test
    fun `sorted by completed goal count descending`() {
        val v1 = LifeValue(id = "v1", title = "A")
        val v2 = LifeValue(id = "v2", title = "B")
        val goals = listOf(
            testGoal().copy(id = "g1", valueId = "v2", status = GoalStatus.COMPLETED),
            testGoal().copy(id = "g2", valueId = "v2", status = GoalStatus.COMPLETED),
            testGoal().copy(id = "g3", valueId = "v1", status = GoalStatus.COMPLETED),
        )
        val result = computeValueAlignment(listOf(v1, v2), goals, emptyList())
        assertEquals("v2", result.first().valueId) // 2 completed goals → first
    }
}
