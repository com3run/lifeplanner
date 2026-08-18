package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.UserSituation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoachMissingSlotsTest {

    private val orchestrator = CoachOrchestrator()

    private fun coach(id: String) = CoachPersona.getById(id)

    /** Every coach that has a slice of its own, i.e. every one that can ask for anything. */
    private val everyCoach = listOf(
        "alex_career", "morgan_finance", "kai_fitness",
        "sam_social", "river_wellness", "jamie_family",
    ).map(CoachPersona::getById)

    @Test
    fun `a coach is told what it does not know`() {
        val context = orchestrator.buildSituationContext(UserSituation(), coach("morgan_finance"))

        // These were sign-up questions. Now the coach carries them and asks in its own territory,
        // where the user arrived on purpose.
        assertTrue("NOT YET KNOWN" in context, context)
        assertTrue("income range" in context, context)
    }

    @Test
    fun `it is told to ask one at a time and never to lead with it`() {
        val context = orchestrator.buildSituationContext(UserSituation(), coach("kai_fitness"))

        // Without this the coach opens with a checklist, which is the questionnaire again wearing
        // a chat bubble.
        // One question, not a checklist — that part has to hold whatever the wording.
        assertTrue("ONE short question" in context, context)
        // And it must stop if the user deflects.
        assertTrue("let it go" in context, context)

        // The first version hedged: "only when it genuinely helps", "never open with it", "drop it
        // if the user does not want to say" — three discouragements to one weak invitation. Across
        // real conversations with two coaches it never asked once, including where the answer
        // plainly depended on the answer. If that phrasing comes back, so does the silence.
        assertFalse("only when it genuinely helps" in context, context)
    }

    @Test
    fun `nothing is asked for once it is known`() {
        val known = UserSituation(
            money = MoneySlice(
                incomeBand = az.tribe.lifeplanner.domain.model.IncomeBand.BAND_30_60K,
                savingsHabit = az.tribe.lifeplanner.domain.model.SavingsHabit.CONSISTENT,
                financialGoal = "Build six months of runway",
                confidence = 0.7f,
            ),
        )

        val context = orchestrator.buildSituationContext(known, coach("morgan_finance"))

        assertFalse("NOT YET KNOWN" in context, context)
        assertFalse("income range" in context, context)
    }

    @Test
    fun `debt is never asked for by any coach`() {
        // Dropped deliberately. Nothing read it but prompt padding, and it is a lot to ask of
        // someone who has been in the app for ninety seconds.
        everyCoach.forEach { c ->
            val context = orchestrator.buildSituationContext(UserSituation(), c)
            assertFalse("debt" in context.lowercase(), "${c.id} asks about debt: $context")
        }
    }

    @Test
    fun `relationship status is never asked for by any coach`() {
        everyCoach.forEach { c ->
            val context = orchestrator.buildSituationContext(UserSituation(), c)
            assertFalse(
                "relationship status" in context.lowercase(),
                "${c.id} asks about relationship status: $context",
            )
        }
    }

    @Test
    fun `what is already known is still stated, alongside what is not`() {
        val partial = UserSituation(
            meta = MetaSlice(name = "Kamran", stressLevel = 4, confidence = 0.7f),
        )

        val context = orchestrator.buildSituationContext(partial, coach("morgan_finance"))

        // Both halves matter: the coach must not re-ask the name, and must know money is still open.
        assertTrue("Kamran" in context, context)
        assertTrue("do NOT re-ask" in context, context)
        assertTrue("NOT YET KNOWN" in context, context)
    }

    @Test
    fun `a coach with nothing known and nothing to ask says nothing at all`() {
        // Luna has no slice of her own, so on an empty profile there is no context worth spending
        // prompt tokens on.
        val context = orchestrator.buildSituationContext(UserSituation(), coach = null)

        assertTrue(context.isEmpty(), context)
    }
}
