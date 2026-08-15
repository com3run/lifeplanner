package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.testutil.FakeGoalRepository
import az.tribe.lifeplanner.testutil.FakeLifeValueRepository
import az.tribe.lifeplanner.testutil.testGoal
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoLinkGoalValuesUseCaseTest {

    private fun value(id: String, title: String, description: String = "") =
        LifeValue(id = id, title = title, description = description)

    private fun fixture(
        goals: List<az.tribe.lifeplanner.domain.model.Goal>,
        values: List<LifeValue>,
    ): Triple<AutoLinkGoalValuesUseCase, FakeGoalRepository, FakeLifeValueRepository> {
        val goalRepo = FakeGoalRepository().apply { setGoals(goals) }
        val valueRepo = FakeLifeValueRepository().apply { setValues(values) }
        return Triple(AutoLinkGoalValuesUseCase(goalRepo, valueRepo), goalRepo, valueRepo)
    }

    @Test
    fun linksUnambiguousKeywordMatch() = runTest {
        val (useCase, goals, _) = fixture(
            goals = listOf(testGoal(id = "g1", title = "Run a half marathon", description = "Build fitness")),
            values = listOf(
                value("v-fit", "Fitness", "health strength running"),
                value("v-money", "Wealth", "savings finance"),
            ),
        )
        val count = useCase()
        assertEquals(1, count)
        assertEquals("v-fit", goals.getGoalById("g1")?.valueId)
    }

    @Test
    fun leavesTiedMatchesForTheUserToDecide() = runTest {
        val (useCase, goals, _) = fixture(
            goals = listOf(testGoal(id = "g1", title = "Balance", description = "growth")),
            values = listOf(
                value("v-a", "Alpha", "growth"),
                value("v-b", "Beta", "growth"),
            ),
        )
        assertEquals(0, useCase())
        assertNull(goals.getGoalById("g1")?.valueId)
    }

    @Test
    fun neverOverwritesAnExistingLink() = runTest {
        val (useCase, goals, _) = fixture(
            goals = listOf(testGoal(id = "g1", title = "Run a half marathon").copy(valueId = "manual")),
            values = listOf(value("v-fit", "Fitness", "running")),
        )
        assertEquals(0, useCase())
        assertEquals("manual", goals.getGoalById("g1")?.valueId)
    }

    @Test
    fun skipsGoalsWithNoConfidentMatch() = runTest {
        val (useCase, goals, _) = fixture(
            goals = listOf(testGoal(id = "g1", title = "Xyzzy", description = "")),
            values = listOf(value("v-fit", "Fitness", "running strength")),
        )
        assertEquals(0, useCase())
        assertNull(goals.getGoalById("g1")?.valueId)
    }

    @Test
    fun noValuesMeansNoLinking() = runTest {
        val (useCase, goals, _) = fixture(
            goals = listOf(testGoal(id = "g1", title = "Run a half marathon")),
            values = emptyList(),
        )
        assertEquals(0, useCase())
        assertNull(goals.getGoalById("g1")?.valueId)
    }
}
