package az.tribe.lifeplanner.ui.intro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeIntroSeenStore : IntroSeenStore {
    val seen = mutableSetOf<String>()
    override fun hasSeen(introId: String) = introId in seen
    override fun markSeen(introId: String) { seen += introId }
}

class IntroSeenStoreTest {

    @Test
    fun firstTouchOfAFeatureGetsAnIntro() {
        val store = FakeIntroSeenStore()
        val intro = store.introToShow(FeatureIntroCatalog.WEEKLY_REVIEW)
        assertEquals(FeatureIntroCatalog.WEEKLY_REVIEW, intro?.id)
    }

    @Test
    fun anExplainedFeatureOpensDirectly() {
        val store = FakeIntroSeenStore()
        store.markSeen(FeatureIntroCatalog.WEEKLY_REVIEW)
        assertNull(store.introToShow(FeatureIntroCatalog.WEEKLY_REVIEW))
    }

    @Test
    fun seeingOneIntroDoesNotSuppressTheOthers() {
        val store = FakeIntroSeenStore()
        store.markSeen(FeatureIntroCatalog.QUEST)
        assertNull(store.introToShow(FeatureIntroCatalog.QUEST))
        assertEquals(FeatureIntroCatalog.VISION, store.introToShow(FeatureIntroCatalog.VISION)?.id)
    }

    @Test
    fun cardsWithoutAnIntroOpenDirectly() {
        val store = FakeIntroSeenStore()
        assertNull(store.introToShow(null))
        assertNull(store.introToShow("intro_not_in_catalog"))
    }

    @Test
    fun dismissingWithoutContinuingLeavesTheIntroUnseen() {
        // The screen only marks an intro seen on Continue, so a curious tap costs nothing.
        val store = FakeIntroSeenStore()
        store.introToShow(FeatureIntroCatalog.VISION)
        assertEquals(FeatureIntroCatalog.VISION, store.introToShow(FeatureIntroCatalog.VISION)?.id)
    }
}
