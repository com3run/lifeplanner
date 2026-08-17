package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.UserContext
import az.tribe.lifeplanner.domain.model.UserSituation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * What the model is actually sent.
 *
 * [CoachMissingSlotsTest] proves [CoachOrchestrator.buildSituationContext] produces the asking
 * instruction. That turned out not to be the same thing as the model receiving it: across four real
 * conversations with two coaches, none of them ever asked. Testing the unit and assuming the
 * assembly is where the time went, so this tests the assembly.
 */
class CoachPromptAssemblyTest {

    private val orchestrator = CoachOrchestrator()

    private fun prompt(
        situation: UserSituation?,
        coach: CoachPersona? = CoachPersona.getById("kai_fitness"),
    ) = buildStreamingSystemPrompt(
        userContext = UserContext(
            userName = "Kamran",
            totalGoals = 0,
            completedGoals = 0,
            activeGoals = 0,
            currentStreak = 0,
            totalXp = 0,
            level = 1,
            recentMilestones = emptyList(),
            upcomingDeadlines = emptyList(),
            habitCompletionRate = 0f,
            journalEntryCount = 0,
            primaryCategories = emptyList(),
        ),
        coach = coach,
        conversationHistory = emptyList(),
        customCoach = null,
        personaOverride = null,
        situation = situation,
        orchestrator = orchestrator,
    )

    @Test
    fun `the asking instruction survives into the assembled prompt`() {
        val assembled = prompt(UserSituation())

        assertTrue("NOT YET KNOWN" in assembled, assembled)
        assertTrue("activity level" in assembled, assembled)
    }

    @Test
    fun `a user with no stored situation still gets it`() {
        // The case the whole feature exists for. Passing null here skipped the situation block
        // entirely — including the instruction — so a brand new user, the only kind with nothing
        // known, was the one kind of user the coach would never ask.
        val assembled = prompt(null)

        assertTrue("NOT YET KNOWN" in assembled, assembled)
    }

    @Test
    fun `nothing later in the prompt forbids asking a question`() {
        val assembled = prompt(UserSituation())

        // The response format is appended after the situation block and gets the last word. If it
        // ever says "do not ask questions" or caps replies below a question's length, the coach
        // will comply with that and the instruction above becomes decoration.
        val tail = assembled.substringAfter("NOT YET KNOWN")
        assertFalse("do not ask" in tail.lowercase(), tail)
        assertFalse("no questions" in tail.lowercase(), tail)
    }

    @Test
    fun `a coach that knows everything is not told to ask`() {
        val known = UserSituation(
            body = az.tribe.lifeplanner.domain.model.BodySlice(
                activityLevel = az.tribe.lifeplanner.domain.model.ActivityLevel.MODERATE,
                sleepHours = 7f,
                energyRating = 6,
                confidence = 0.7f,
            ),
        )

        assertFalse("NOT YET KNOWN" in prompt(known), prompt(known))
    }
}
