package az.tribe.lifeplanner.ui.intro

import com.russhwolf.settings.Settings

/**
 * Remembers which feature intros the user has already seen. An intro is a first-touch explanation,
 * so showing it twice turns an introduction into nagging.
 */
interface IntroSeenStore {
    fun hasSeen(introId: String): Boolean
    fun markSeen(introId: String)
}

/**
 * The intro to show before opening a feature, or null to open it directly. Null covers both "this
 * card has no intro" and "already explained", so the explanation happens exactly once and never
 * stands between the user and a feature they already know.
 */
fun IntroSeenStore.introToShow(introId: String?): FeatureIntro? =
    FeatureIntroCatalog[introId]?.takeUnless { hasSeen(it.id) }

/**
 * Settings-backed store. Deliberately best-effort: an intro is a courtesy, and a storage failure
 * must never block the user from reaching the feature behind it. A failed read reports "seen", so
 * the worst case is a missing explanation rather than a sheet that reappears on every tap.
 */
class SettingsIntroSeenStore(
    private val settings: Settings = Settings(),
) : IntroSeenStore {

    override fun hasSeen(introId: String): Boolean =
        runCatching { settings.getBoolean(key(introId), false) }.getOrDefault(true)

    override fun markSeen(introId: String) {
        runCatching { settings.putBoolean(key(introId), true) }
    }

    private fun key(introId: String) = "seen_$introId"
}
