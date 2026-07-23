package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.PermutationKind
import az.tribe.lifeplanner.testutil.testGoal
import az.tribe.lifeplanner.testutil.testMilestone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalPossibilityFallbackTest {

    private val fallback = LocalPossibilityFallback()

    @Test
    fun `one option per permutation`() {
        val result = fallback(testGoal(title = "Run a half marathon"))
        assertEquals(PermutationKind.entries.size, result.size)
        assertEquals(PermutationKind.entries.toSet(), result.map { it.permutation }.toSet())
    }

    @Test
    fun `every option is marked local with text and a rationale`() {
        val result = fallback(testGoal())
        assertTrue(result.all { it.isLocal })
        assertTrue(result.all { it.text.isNotBlank() })
        assertTrue(result.all { it.rationale.isNotBlank() })
    }

    @Test
    fun `shrink uses the next incomplete milestone`() {
        val result = fallback(
            testGoal(
                milestones = listOf(
                    testMilestone(id = "m1", title = "Run 5K without stopping", isCompleted = true),
                    testMilestone(id = "m2", title = "Finish a 10K race", isCompleted = false),
                ),
            ),
        )
        val shrink = result.first { it.permutation == PermutationKind.SHRINK }
        assertTrue("Finish a 10K race" in shrink.text)
    }

    @Test
    fun `shrink falls back to the goal title without milestones`() {
        val shrink = fallback(testGoal(title = "Run a half marathon"))
            .first { it.permutation == PermutationKind.SHRINK }
        assertTrue("Run a half marathon" in shrink.text)
    }

    @Test
    fun `the analogy follows the goal's life area`() {
        val body = fallback(testGoal(category = GoalCategory.BODY)).first { it.permutation == PermutationKind.ANALOGY }
        val family = fallback(testGoal(category = GoalCategory.FAMILY)).first { it.permutation == PermutationKind.ANALOGY }
        assertTrue("musician" in body.text)
        assertTrue("standing date" in family.text)
    }
}
