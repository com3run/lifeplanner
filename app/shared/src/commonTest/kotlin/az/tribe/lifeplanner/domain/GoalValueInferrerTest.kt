package az.tribe.lifeplanner.domain

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.service.GoalValueInferrer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GoalValueInferrerTest {

    private fun value(id: String, title: String, description: String = "", active: Boolean = true) =
        LifeValue(id = id, title = title, description = description, isActive = active)

    @Test
    fun picksTheValueWhoseWordAppearsInTheGoal() {
        val id = GoalValueInferrer.infer(
            category = GoalCategory.BODY,
            title = "Run a half marathon",
            description = "Build fitness and strength",
            values = listOf(value("v-fit", "Fitness", "health strength"), value("v-money", "Wealth", "savings")),
        )
        assertEquals("v-fit", id)
    }

    @Test
    fun usesCategoryAffinityWhenTheGoalTextDoesNotNameTheValue() {
        // "Save for a house" names no value word, but "Financial Security" belongs to MONEY.
        val id = GoalValueInferrer.infer(
            category = GoalCategory.MONEY,
            title = "Save for a house",
            description = "Put money aside every month",
            values = listOf(value("v-sec", "Financial Security", "wealth savings"), value("v-adv", "Adventure", "travel")),
        )
        assertEquals("v-sec", id)
    }

    @Test
    fun returnsNullOnAmbiguousTie() {
        val id = GoalValueInferrer.infer(
            category = GoalCategory.CAREER,
            title = "Balance",
            description = "growth",
            values = listOf(value("v-a", "Alpha", "growth"), value("v-b", "Beta", "growth")),
        )
        assertNull(id)
    }

    @Test
    fun returnsNullWhenNothingMatches() {
        val id = GoalValueInferrer.infer(
            category = GoalCategory.CAREER,
            title = "Xyzzy",
            description = "",
            values = listOf(value("v-fit", "Fitness", "running strength")),
        )
        assertNull(id)
    }

    @Test
    fun returnsNullWhenNoValuesExist() {
        assertNull(GoalValueInferrer.infer(GoalCategory.BODY, "Run a marathon", "", emptyList()))
    }

    @Test
    fun ignoresInactiveValues() {
        val id = GoalValueInferrer.infer(
            category = GoalCategory.BODY,
            title = "Improve fitness",
            description = "",
            values = listOf(value("v-fit", "Fitness", "", active = false)),
        )
        assertNull(id)
    }
}
