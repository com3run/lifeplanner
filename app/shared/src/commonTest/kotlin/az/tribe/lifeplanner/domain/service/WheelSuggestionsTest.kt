package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.WheelArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WheelSuggestionsTest {

    @Test
    fun `every covered area has copy at every urgency`() {
        WheelSuggestions.covered.forEach { area ->
            NudgeUrgency.entries.forEach { urgency ->
                assertNotNull(
                    WheelSuggestions.forArea(area, urgency),
                    "$area has nothing to say at $urgency",
                )
            }
        }
    }

    @Test
    fun `no two areas share a suggestion`() {
        // Distinct per area first. Rotation wraps when an urgency has fewer options than the
        // sample range, so counting raw hits would flag an area for repeating itself, which is
        // rotation working rather than duplicated copy.
        val byArea = WheelSuggestions.covered.associateWith { area ->
            NudgeUrgency.entries.flatMap { urgency ->
                (0..4).mapNotNull { WheelSuggestions.forArea(area, urgency, it)?.action }
            }.toSet()
        }

        // The failure mode for authored sets like this is the same three tips with the nouns
        // swapped, so what matters is a line turning up under two different areas.
        val seen = mutableMapOf<String, WheelArea>()
        byArea.forEach { (area, actions) ->
            actions.forEach { action ->
                val previous = seen.put(action, area)
                assertTrue(
                    previous == null,
                    "\"$action\" appears under both $previous and $area",
                )
            }
        }
    }

    @Test
    fun `an area with no copy yet stays silent rather than inventing something`() {
        (WheelArea.entries - WheelSuggestions.covered).forEach { area ->
            NudgeUrgency.entries.forEach { urgency ->
                assertNull(
                    WheelSuggestions.forArea(area, urgency),
                    "$area returned copy that was never written for it",
                )
            }
        }
    }

    @Test
    fun `rotation cycles rather than repeating or running off the end`() {
        val seen = (0..5).map { WheelSuggestions.forArea(WheelArea.FRIENDS, NudgeUrgency.GENTLE, it)?.action }

        assertEquals(3, seen.take(3).toSet().size, "three consecutive days gave the same advice")
        // Day 3 comes back around to day 0 rather than throwing or falling off the list.
        assertEquals(seen[0], seen[3])
    }

    @Test
    fun `a negative rotation is handled, since callers pass date arithmetic`() {
        val suggestion = WheelSuggestions.forArea(WheelArea.FRIENDS, NudgeUrgency.GENTLE, -1)

        assertNotNull(suggestion)
    }

    @Test
    fun `every suggestion is an action and a reason, not a slogan`() {
        WheelSuggestions.covered.forEach { area ->
            NudgeUrgency.entries.forEach { urgency ->
                (0..2).forEach { rotation ->
                    val s = WheelSuggestions.forArea(area, urgency, rotation) ?: return@forEach
                    assertTrue(s.action.isNotBlank(), "$area/$urgency/$rotation has no action")
                    assertTrue(s.because.isNotBlank(), "$area/$urgency/$rotation has no reason")
                    // Long enough to be specific. "Reach out" is what this guards against.
                    assertTrue(s.action.length > 24, "action too vague to act on: ${s.action}")
                    assertEquals(area, s.area)
                    assertEquals(urgency, s.urgency)
                }
            }
        }
    }

    @Test
    fun `Money never names an amount or tells anyone where to put it`() {
        val money = NudgeUrgency.entries.flatMap { urgency ->
            (0..2).mapNotNull { WheelSuggestions.forArea(WheelArea.MONEY, urgency, it) }
        }

        assertTrue(money.isNotEmpty(), "no Money copy found, so this asserts nothing")
        money.forEach { suggestion ->
            val text = (suggestion.action + " " + suggestion.because).lowercase()
            // Naming a figure or a percentage assumes slack the app cannot see, and reads as an
            // insult to anyone without it.
            assertTrue(text.none { c -> c.isDigit() }, "names a figure: " + suggestion.action)
            assertTrue(!text.contains("%"), "names a percentage: " + suggestion.action)
            // Where to put money so it grows is regulated advice and none of our business.
            listOf("invest", "stocks", "shares", "crypto", "portfolio").forEach { word ->
                assertTrue(!text.contains(word), "strays into investment advice: " + suggestion.action)
            }
        }
    }

    @Test
    fun `Romance never assumes there is a partner`() {
        val romance = NudgeUrgency.entries.flatMap { urgency ->
            (0..2).mapNotNull { WheelSuggestions.forArea(WheelArea.ROMANCE, urgency, it) }
        }

        assertTrue(romance.isNotEmpty(), "no Romance copy found, so this asserts nothing")
        romance.forEach { suggestion ->
            val text = (suggestion.action + " " + suggestion.because).lowercase()
            // The rubric explicitly blesses a contented single life. Copy that assumes a partner,
            // or treats single as the problem to solve, tells that person the app is not listening.
            listOf("your partner", "date night", "romantic", "relationship you", "find someone")
                .forEach { phrase ->
                    assertTrue(!text.contains(phrase), "assumes coupling: " + suggestion.action)
                }
        }
    }

    @Test
    fun `Mental points outward when it is serious, and never sounds like treatment`() {
        val serious = (0..2).mapNotNull {
            WheelSuggestions.forArea(WheelArea.MENTAL, NudgeUrgency.SERIOUS, it)
        }

        assertTrue(serious.isNotEmpty(), "no serious Mental copy found, so this asserts nothing")

        // At this end the true thing matters more than the encouraging one. Someone whose head has
        // been a bad place for weeks is not fixed by a walk, and implying otherwise is how an app
        // loses the person it most wanted to help.
        val pointsOutward = serious.any { s ->
            val t = (s.action + " " + s.because).lowercase()
            listOf("doctor", "therapist", "professional").any { t.contains(it) }
        }
        assertTrue(pointsOutward, "serious Mental copy never suggests getting help")

        // And it must not read as treatment itself. Diagnostic language invites people to
        // substitute a tip card for the real thing.
        WheelSuggestions.covered.forEach { area ->
            NudgeUrgency.entries.forEach { urgency ->
                (0..2).mapNotNull { WheelSuggestions.forArea(area, urgency, it) }.forEach { s ->
                    val t = (s.action + " " + s.because).lowercase()
                    // Deliberately narrow. An earlier version banned "cure" and fired on "not
                    // cured by it" in a line about skimming, which is ordinary English: a guard
                    // that cries wolf gets deleted by whoever hits it next.
                    listOf("depression", "anxiety disorder", "diagnos", "symptoms of", "treat your")
                        .forEach { word ->
                            assertTrue(!t.contains(word), "sounds clinical: " + s.action)
                        }
                }
            }
        }
    }

    @Test
    fun `the serious copy does not ask for more than one small thing`() {
        val serious = WheelSuggestions.covered.flatMap { area ->
            (0..2).mapNotNull { WheelSuggestions.forArea(area, NudgeUrgency.SERIOUS, it) }
        }

        // Someone at a 3 is being asked for the smallest possible step, so nothing here should
        // read as a project. Multiple sentences of instruction is the tell.
        serious.forEach {
            assertTrue(
                it.action.count { c -> c == '.' } <= 2,
                "serious advice should be one step, was: ${it.action}",
            )
        }
    }
}
