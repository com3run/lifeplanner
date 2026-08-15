package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.model.PermutationKind
import az.tribe.lifeplanner.domain.service.LocalPossibilityFallback
import az.tribe.lifeplanner.testutil.FakeAiProxyService
import az.tribe.lifeplanner.testutil.testGoal
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pillar 6 (TRI-44): verifies the divergence step parses the AI's response robustly. The AI call
 * itself is faked; these lock down the JSON handling so a stray model response never crashes the UI,
 * and a proxy outage degrades to the on-device fallback instead of an empty screen.
 */
class GeneratePossibilitiesUseCaseTest {

    private val ai = FakeAiProxyService()
    private val useCase = GeneratePossibilitiesUseCase(ai, LocalPossibilityFallback())
    private val goal = testGoal(id = "g1", title = "Run a marathon")

    @Test
    fun `parses a clean JSON array into possibilities`() = runTest {
        ai.responseToReturn = """
            [
              {"text":"Pair your morning walk with a training podcast","permutation":"RECOMBINE","rationale":"stacks two habits"},
              {"text":"Train like a chef does mise en place","permutation":"ANALOGY","rationale":"borrow from cooking"}
            ]
        """.trimIndent()
        val result = useCase(goal)
        assertEquals(2, result.size)
        assertEquals(PermutationKind.RECOMBINE, result[0].permutation)
        assertEquals(PermutationKind.ANALOGY, result[1].permutation)
        assertTrue(result[0].text.isNotBlank())
        assertTrue(result[0].id.isNotBlank())
    }

    @Test
    fun `strips markdown code fences`() = runTest {
        ai.responseToReturn = "```json\n[{\"text\":\"Shrink it to 5 minutes a day\",\"permutation\":\"SHRINK\",\"rationale\":\"lower the bar\"}]\n```"
        val result = useCase(goal)
        assertEquals(1, result.size)
        assertEquals(PermutationKind.SHRINK, result.single().permutation)
    }

    @Test
    fun `an unknown permutation label falls back to RECOMBINE`() = runTest {
        ai.responseToReturn = """[{"text":"Try something new","permutation":"WHATEVER","rationale":"y"}]"""
        assertEquals(PermutationKind.RECOMBINE, useCase(goal).single().permutation)
    }

    @Test
    fun `a non-JSON response falls back to local options rather than an empty screen`() = runTest {
        ai.responseToReturn = "Sorry, I can't help with that right now."
        val result = useCase(goal)
        assertEquals(PermutationKind.entries.size, result.size)
        assertTrue(result.all { it.isLocal })
    }

    @Test
    fun `an AI failure falls back to local options`() = runTest {
        ai.errorToThrow = RuntimeException("proxy down")
        val result = useCase(goal)
        assertEquals(PermutationKind.entries.size, result.size)
        assertTrue(result.all { it.isLocal })
    }

    @Test
    fun `AI-parsed options are not marked local`() = runTest {
        ai.responseToReturn = """[{"text":"A real option","permutation":"INVERT","rationale":"y"}]"""
        assertTrue(useCase(goal).none { it.isLocal })
    }

    @Test
    fun `blank-text items are dropped`() = runTest {
        ai.responseToReturn = """[{"text":"","permutation":"RECOMBINE","rationale":"x"},{"text":"A real option","permutation":"INVERT","rationale":"y"}]"""
        val result = useCase(goal)
        assertEquals(1, result.size)
        assertEquals(PermutationKind.INVERT, result.single().permutation)
    }
}
