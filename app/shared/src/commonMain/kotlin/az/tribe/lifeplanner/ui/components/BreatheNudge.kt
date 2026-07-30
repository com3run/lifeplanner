package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.X
import kotlinx.coroutines.delay
import kotlin.time.Clock

private const val NUDGE_VISIBLE_MS = 9_000L // how long the awareness pill lingers before it fades on its own
private const val TICK_MS = 5_000L

/**
 * A gentle, awareness-only "take a breath" nudge for long, heads-down sessions. Drop it into any
 * screen as the last child of a fill-size container (so it overlays content); it tracks wall-clock
 * time on the screen and, at each of [thresholdsMinutes], floats a soft pill at the bottom. Tapping
 * it runs one level-appropriate guided breath over a dim scrim; ignoring it lets the pill fade away.
 *
 * It never blocks, never nags (each threshold fires once, then it goes quiet), and awards the same
 * mindfulness XP a feed-card breath would. Reused across screens; piloted in the coach chat.
 */
@Composable
fun BreatheAwarenessNudge(
    thresholdsMinutes: List<Int> = listOf(3, 5),
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.modernColors
    var fired by remember { mutableStateOf(0) }
    var showNudge by remember { mutableStateOf(false) }
    var breathing by remember { mutableStateOf(false) }

    val thresholds = remember(thresholdsMinutes) { thresholdsMinutes.sorted() }

    // Wall-clock tick: survives brief backgrounding, fires each threshold once, then stops.
    LaunchedEffect(thresholds) {
        val start = Clock.System.now()
        while (fired < thresholds.size) {
            delay(TICK_MS)
            val elapsed = (Clock.System.now() - start).inWholeSeconds
            val nextAt = thresholds[fired].toLong() * 60L
            if (elapsed >= nextAt) {
                fired += 1
                if (!breathing) showNudge = true
            }
        }
    }

    // The pill is a whisper, not a modal: if the user doesn't engage, it fades on its own.
    LaunchedEffect(showNudge) {
        if (showNudge) {
            delay(NUDGE_VISIBLE_MS)
            showNudge = false
        }
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        if (breathing) {
            // A calm scrim that swallows taps behind the guided breath.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                    GuidedBreathSession(onClose = { breathing = false })
                }
            }
        } else {
            AnimatedVisibility(
                visible = showNudge,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 96.dp, start = LifePlannerDesign.Spacing.md, end = LifePlannerDesign.Spacing.md)
                        .bouncyClickable { showNudge = false; breathing = true },
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full),
                    color = c.cardBackground,
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        Modifier.padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🌿", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Take a breath?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = c.textPrimary,
                        )
                        Icon(
                            PhosphorIcons.Regular.X,
                            contentDescription = "Dismiss",
                            tint = c.textTertiary,
                            modifier = Modifier
                                .size(28.dp)
                                .bouncyClickable { showNudge = false }
                                .padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}
