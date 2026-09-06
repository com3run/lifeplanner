package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.StepDuration
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Play
import kotlinx.coroutines.delay
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_start_timer

/**
 * A step written as a length of time ("Hold crow pose 30 seconds") is a timer wearing a tick box:
 * the user has to do the thing, watch a clock elsewhere, then come back and tick. This runs it and
 * ticks the step off at zero.
 *
 * It lives on its own so a step reads the same wherever it is shown. It appears both in the day's
 * plan and, when it is the thing to be doing right now, in the card at the top of the screen, and a
 * countdown that exists in one place and not the other is a step that quietly gets harder to do
 * depending on where you happened to find it.
 *
 * The running state is held by the caller rather than inside the button, because the row around it
 * also has something to say while the clock is going.
 */
@Stable
internal class StepTimerState(val totalSeconds: Int) {
    var remaining by mutableStateOf<Int?>(null)
        private set

    val running: Boolean get() = remaining != null

    fun start() {
        remaining = totalSeconds
    }

    fun cancel() {
        remaining = null
    }

    internal fun tickTo(seconds: Int?) {
        remaining = seconds
    }
}

/**
 * @param stepKey resets the countdown when the row is recycled onto a different step.
 * @param onComplete called once, at zero. A cancelled timer never completes: someone who stopped
 *   early did not do the thing, and an app that ticks it anyway is lying to them about their week.
 */
@Composable
internal fun rememberStepTimer(
    stepKey: String,
    totalSeconds: Int,
    onComplete: () -> Unit,
): StepTimerState {
    val haptic = rememberHapticManager()
    val complete by rememberUpdatedState(onComplete)
    val state = remember(stepKey, totalSeconds) { StepTimerState(totalSeconds) }

    LaunchedEffect(state, state.running) {
        while (state.running) {
            delay(1000)
            val left = (state.remaining ?: return@LaunchedEffect) - 1
            if (left <= 0) {
                state.tickTo(null)
                haptic.success()
                complete()
            } else {
                state.tickTo(left)
                // A tick each second makes it feel like something is happening in your hand rather
                // than only on screen.
                haptic.click()
            }
        }
    }
    return state
}

/**
 * Play, then a countdown ring in the same 40dp the tick box occupied.
 *
 * Tapping mid-run cancels rather than completing.
 */
@Composable
internal fun StepTimerControl(state: StepTimerState, accent: Color) {
    val remaining = state.remaining
    // One continuous sweep over the whole duration, not a new 950ms tween per tick. Chasing the
    // per-second state made the arc advance in visible steps; time does not pass in steps.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(remaining != null) {
        if (remaining == null) {
            progress.snapTo(0f)
        } else {
            progress.snapTo(1f - (remaining.toFloat() / state.totalSeconds))
            progress.animateTo(
                1f,
                tween(durationMillis = remaining * 1000, easing = LinearEasing),
            )
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.12f))
            .bouncyClickable { if (state.running) state.cancel() else state.start() },
        contentAlignment = Alignment.Center,
    ) {
        if (remaining != null) {
            Box(
                Modifier.fillMaxSize().drawBehind {
                    drawArc(
                        color = accent,
                        startAngle = -90f,
                        sweepAngle = progress.value * 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            )
            Text(
                StepDuration.format(remaining),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
        } else {
            Icon(
                PhosphorIcons.Fill.Play,
                contentDescription = stringResource(Res.string.cd_start_timer, StepDuration.format(state.totalSeconds)),
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
