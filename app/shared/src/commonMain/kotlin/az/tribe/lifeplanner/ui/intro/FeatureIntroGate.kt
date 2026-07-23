package az.tribe.lifeplanner.ui.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.koin.compose.koinInject

/**
 * Puts a feature's intro in front of the first tap that opens it.
 *
 * Screens call [open] instead of navigating directly. The first time, the intro explains the
 * feature and navigation waits for the user to continue; after that, [open] navigates immediately.
 * Gating lives here rather than in a ViewModel because it is purely about what the user has been
 * shown, and because every screen with a feature entry point needs the same behavior.
 */
@Stable
class FeatureIntroGate internal constructor(private val store: IntroSeenStore) {

    internal var pending by mutableStateOf<Pending?>(null)
        private set

    internal data class Pending(val intro: FeatureIntro, val accent: Color, val proceed: () -> Unit)

    /**
     * Open the feature behind [introId], explaining it first if the user has not met it yet.
     * [accent] tints the sheet so it visually continues whatever the user tapped.
     */
    fun open(introId: String?, accent: Color, proceed: () -> Unit) {
        val intro = store.introToShow(introId)
        if (intro == null) proceed() else pending = Pending(intro, accent, proceed)
    }

    /** Backing out. Deliberately does not mark the intro seen, so a curious tap costs nothing. */
    internal fun dismiss() {
        pending = null
    }

    internal fun continueToFeature() {
        val p = pending ?: return
        store.markSeen(p.intro.id)
        pending = null
        p.proceed()
    }
}

@Composable
fun rememberFeatureIntroGate(store: IntroSeenStore = koinInject()): FeatureIntroGate =
    remember(store) { FeatureIntroGate(store) }

/** Renders whichever intro the gate is holding. Place once per screen, alongside the content. */
@Composable
fun FeatureIntroHost(gate: FeatureIntroGate) {
    gate.pending?.let { p ->
        FeatureIntroSheet(
            intro = p.intro,
            accent = p.accent,
            onDismiss = gate::dismiss,
            onContinue = gate::continueToFeature,
        )
    }
}
