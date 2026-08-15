package az.tribe.lifeplanner.ui.intro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeatureIntroCatalogTest {

    @Test
    fun everyCascadeStepHasAnIntro() {
        listOf(FeatureIntroCatalog.VISION, FeatureIntroCatalog.QUEST, FeatureIntroCatalog.WEEKLY_REVIEW)
            .forEach { assertNotNull(FeatureIntroCatalog[it], "missing intro for $it") }
    }

    @Test
    fun unknownAndNullKeysResolveToNothing() {
        assertNull(FeatureIntroCatalog["intro_does_not_exist"])
        assertNull(FeatureIntroCatalog[null])
    }

    @Test
    fun everyIntroExplainsWhatItAsksOfTheUser() {
        // The transparency line is the point of the layer, not decoration: a feature that wants
        // time, answers, or data says so before the user commits.
        FeatureIntroCatalog.ids.forEach { id ->
            val intro = FeatureIntroCatalog[id]!!
            assertTrue(intro.asks.isNotBlank(), "$id has no asks line")
            assertTrue(intro.whatItIs.isNotBlank(), "$id does not say what it is")
            assertTrue(intro.ctaLabel.isNotBlank(), "$id has no call to action")
        }
    }

    @Test
    fun everyIntroOffersTwoOrThreeBenefits() {
        FeatureIntroCatalog.ids.forEach { id ->
            val benefits = FeatureIntroCatalog[id]!!.benefits
            assertTrue(benefits.size in 2..3, "$id has ${benefits.size} benefits, expected 2 or 3")
            assertTrue(benefits.none { it.text.isBlank() }, "$id has an empty benefit row")
        }
    }

    @Test
    fun copyAvoidsDashesTheOwnerRejects() {
        FeatureIntroCatalog.ids.forEach { id ->
            val intro = FeatureIntroCatalog[id]!!
            val text = listOf(intro.title, intro.whatItIs, intro.asks, intro.ctaLabel) +
                intro.benefits.map { it.text }
            text.forEach { line ->
                assertTrue('—' !in line && '–' !in line, "$id uses an em or en dash: $line")
            }
        }
    }

    @Test
    fun everyYouTabFeatureHasAnIntro() {
        listOf(
            FeatureIntroCatalog.WEEKLY_REVIEW,
            FeatureIntroCatalog.DECISION_JOURNAL,
            FeatureIntroCatalog.DECISION_REVIEW,
            FeatureIntroCatalog.MY_PATTERNS,
        ).forEach { assertNotNull(FeatureIntroCatalog[it], "missing intro for $it") }
    }

    @Test
    fun idsAreUniqueAndStable() {
        assertEquals(7, FeatureIntroCatalog.ids.size)
        assertTrue(FeatureIntroCatalog.ids.all { it.startsWith("intro_") })
    }
}
