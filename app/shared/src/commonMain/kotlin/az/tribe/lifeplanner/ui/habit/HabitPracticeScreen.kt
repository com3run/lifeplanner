package az.tribe.lifeplanner.ui.habit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.focus.FocusProgressRing
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The practice ground. A habit measured in time or reps is something you *do*, so this gives it a
 * place to happen: a countdown for "Plank 30 sec", a tap target for "50 pushups". Finishing checks
 * the habit in, so there is no second trip to the habit list.
 *
 * Reuses [FocusProgressRing] rather than the whole Focus flow — same visual language, sized to the
 * habit, without Focus's session setup which already knows none of this.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitPracticeScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    viewModel: HabitPracticeViewModel = koinViewModel { parametersOf(habitId) },
) {
    val state by viewModel.state.collectAsState()
    val c = MaterialTheme.modernColors

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background),
                title = {
                    Text(
                        state.habit?.title ?: "Practice",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = c.textPrimary,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = c.textPrimary)
                    }
                },
                actions = {
                    if (state.mode == HabitTrackMode.DURATION && !state.done) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(
                                PhosphorIcons.Regular.ArrowCounterClockwise,
                                contentDescription = "Reset",
                                tint = c.textSecondary,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .padding(horizontal = LifePlannerDesign.Padding.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                state.loading -> Unit
                state.mode == HabitTrackMode.DURATION -> DurationPractice(state, viewModel)
                state.mode == HabitTrackMode.COUNT -> CountPractice(state, viewModel)
                else -> SinglePractice(state, viewModel)
            }
        }
    }
}

@Composable
private fun DurationPractice(state: HabitPracticeViewModel.State, viewModel: HabitPracticeViewModel) {
    val c = MaterialTheme.modernColors
    FocusProgressRing(
        progress = state.progress,
        remainingSeconds = state.remainingSeconds,
        isRunning = state.isRunning,
        elapsedSeconds = state.totalSeconds - state.remainingSeconds,
        size = 260.dp,
    )
    Spacer(Modifier.height(LifePlannerDesign.Spacing.xl))
    Text(
        text = if (state.done) "Done. Nice hold." else practiceHint(state),
        style = MaterialTheme.typography.bodyMedium,
        color = c.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(LifePlannerDesign.Spacing.lg))
    if (!state.done) {
        AppButton(
            text = if (state.isRunning) "Pause" else if (state.remainingSeconds < state.totalSeconds) "Resume" else "Start",
            onClick = { if (state.isRunning) viewModel.pause() else viewModel.start() },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(LifePlannerDesign.Spacing.xs))
        AppButton(
            text = "Mark done",
            onClick = { viewModel.complete() },
            variant = AppButtonVariant.TERTIARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CountPractice(state: HabitPracticeViewModel.State, viewModel: HabitPracticeViewModel) {
    val c = MaterialTheme.modernColors
    // The tap target *is* the control: a big circle that fills as reps land.
    val fill by animateFloatAsState(state.progress, label = "repProgress")
    Box(
        modifier = Modifier.size(260.dp).clip(CircleShape)
            .background(c.primary.copy(alpha = 0.10f + 0.25f * fill))
            .bouncyClickable(enabled = !state.done) { viewModel.addRep() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${state.reps}",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Text(
                "of ${state.targetReps}${state.habit?.unit?.let { " $it" } ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                color = c.textSecondary,
            )
        }
    }
    Spacer(Modifier.height(LifePlannerDesign.Spacing.xl))
    Text(
        text = if (state.done) "All ${state.targetReps} done. Nice work." else "Tap the circle for each one",
        style = MaterialTheme.typography.bodyMedium,
        color = c.textSecondary,
        textAlign = TextAlign.Center,
    )
}

/**
 * A habit with no number attached has nothing to count or time, so practice is just doing it and
 * saying so. Shown rather than blocking, since the caller may route here from any habit.
 */
@Composable
private fun SinglePractice(state: HabitPracticeViewModel.State, viewModel: HabitPracticeViewModel) {
    val c = MaterialTheme.modernColors
    Text(
        text = if (state.done) "Done for today." else "This one is a simple check.",
        style = MaterialTheme.typography.titleMedium,
        color = c.textPrimary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(LifePlannerDesign.Spacing.lg))
    if (!state.done) {
        AppButton(text = "Mark done", onClick = { viewModel.complete() }, modifier = Modifier.fillMaxWidth())
    }
}

private fun practiceHint(state: HabitPracticeViewModel.State): String = when {
    state.isRunning -> "Hold steady"
    state.remainingSeconds < state.totalSeconds -> "Paused"
    else -> "Ready when you are"
}
