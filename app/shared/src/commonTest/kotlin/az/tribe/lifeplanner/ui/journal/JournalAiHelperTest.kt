package az.tribe.lifeplanner.ui.journal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JournalAiHelperTest {

    @Test
    fun `parses full response including all extracted signal`() {
        val json = """
            {
              "title": "A Quiet Reset",
              "content": "Today I stepped back and breathed.",
              "tags": ["calm", "reset"],
              "detectedDecision": {
                "question": "Should I keep the 5am gym habit or move it later?",
                "optionsConsidered": ["Keep 5am", "Move to evening", "Drop it"],
                "leaning": "Move to evening",
                "reasoning": "Mornings have been brutal and I keep skipping."
              },
              "emotionalSignals": ["Relieved after skipping the gym", "Guilty about the missed run"],
              "memoryCandidates": ["Struggles with early mornings", "Values consistency over intensity"]
            }
        """.trimIndent()

        val result = parseAiJournalResult(json)
        assertTrue(result != null)
        assertEquals("A Quiet Reset", result.title)
        assertEquals(listOf("calm", "reset"), result.tags)

        val decision = result.detectedDecision
        assertTrue(decision != null)
        assertEquals("Should I keep the 5am gym habit or move it later?", decision.question)
        assertEquals(listOf("Keep 5am", "Move to evening", "Drop it"), decision.optionsConsidered)
        assertEquals("Move to evening", decision.leaning)
        assertEquals("Mornings have been brutal and I keep skipping.", decision.reasoning)

        assertEquals(2, result.emotionalSignals.size)
        assertEquals(2, result.memoryCandidates.size)
    }

    @Test
    fun `legacy response with only title content tags still parses`() {
        val json = """
            {"title": "Just A Day", "content": "Nothing special happened.", "tags": ["daily"]}
        """.trimIndent()

        val result = parseAiJournalResult(json)
        assertTrue(result != null)
        assertEquals("Just A Day", result.title)
        assertNull(result.detectedDecision)
        assertTrue(result.emotionalSignals.isEmpty())
        assertTrue(result.memoryCandidates.isEmpty())
    }

    @Test
    fun `decision with blank question is treated as no decision`() {
        val json = """
            {
              "title": "T", "content": "C", "tags": [],
              "detectedDecision": {"question": "", "optionsConsidered": ["a", "b"]}
            }
        """.trimIndent()

        val result = parseAiJournalResult(json)
        assertTrue(result != null)
        assertNull(result.detectedDecision)
    }

    @Test
    fun `blank entries inside signal arrays are dropped`() {
        val json = """
            {
              "title": "T", "content": "C", "tags": ["x", "", "y"],
              "emotionalSignals": ["real", "  ", ""],
              "memoryCandidates": [""]
            }
        """.trimIndent()

        val result = parseAiJournalResult(json)
        assertTrue(result != null)
        // tags keep legacy (unfiltered) behaviour; only the new signal arrays drop blanks.
        assertEquals(listOf("x", "", "y"), result.tags)
        assertEquals(listOf("real"), result.emotionalSignals)
        assertTrue(result.memoryCandidates.isEmpty())
    }

    @Test
    fun `missing title returns null`() {
        val result = parseAiJournalResult("""{"content": "C", "tags": []}""")
        assertNull(result)
    }

    @Test
    fun `malformed json returns null rather than throwing`() {
        assertNull(parseAiJournalResult("not json at all"))
        assertNull(parseAiJournalResult(""))
    }

    @Test
    fun `partial decision keeps question and defaults the rest`() {
        val json = """
            {
              "title": "T", "content": "C", "tags": [],
              "detectedDecision": {"question": "Stay or go?"}
            }
        """.trimIndent()

        val result = parseAiJournalResult(json)
        val decision = result?.detectedDecision
        assertTrue(decision != null)
        assertEquals("Stay or go?", decision.question)
        assertTrue(decision.optionsConsidered.isEmpty())
        assertEquals("", decision.leaning)
        assertEquals("", decision.reasoning)
    }
}
