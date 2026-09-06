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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.ui.ObserveAsEvents
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.focus.FocusProgressRing
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.ArrowLeft
import kotlinx.datetime.LocalDateTime
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_reset
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The practice ground. A habit measured in time or reps is something you *do*, so this gives it a
 * place to happen: a countdown for "Plank 30 sec", a tap target for "50 pushups". Finishing checks
 * the habit in, so there is no second trip to the habit list.
 *
 * The Root owns the ViewModel; [HabitPracticeScreen] renders state and forwards actions.
 */
@Composable
fun HabitPracticeRoot(
    habitId: String,
    onNavigateBack: () -> Unit,
    viewModel: HabitPracticeViewModel = koinViewModel { parametersOf(habitId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HabitPracticeEvent.NavigateBack -> onNavigateBack()
            is HabitPracticeEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve())
        }
    }

    HabitPracticeScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

/**
 * Reuses [FocusProgressRing] rather than the whole Focus flow, same visual language, sized to the
 * habit, without Focus's session setup which already knows none of this.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitPracticeScreen(
    state: HabitPracticeState,
    onAction: (HabitPracticeAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val c = MaterialTheme.modernColors

    Scaffold(
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    IconButton(onClick = { onAction(HabitPracticeAction.OnBackClick) }) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = c.textPrimary)
                    }
                },
                actions = {
                    if (state.mode == HabitTrackMode.DURATION && !state.done) {
                        IconButton(onClick = { onAction(HabitPracticeAction.OnResetClick) }) {
                            Icon(
                                PhosphorIcons.Regular.ArrowCounterClockwise,
                                contentDescription = stringResource(Res.string.cd_reset),
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
                state.isLoading -> Unit
                state.mode == HabitTrackMode.DURATION -> DurationPractice(state, onAction)
                state.mode == HabitTrackMode.COUNT -> CountPractice(state, onAction)
                else -> SinglePractice(state, onAction)
            }
        }
    }
}

@Composable
private fun DurationPractice(state: HabitPracticeState, onAction: (HabitPracticeAction) -> Unit) {
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
            onClick = { onAction(if (state.isRunning) HabitPracticeAction.OnPauseClick else HabitPracticeAction.OnStartClick) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(LifePlannerDesign.Spacing.xs))
        AppButton(
            text = "Mark done",
            onClick = { onAction(HabitPracticeAction.OnCompleteClick) },
            variant = AppButtonVariant.TERTIARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CountPractice(state: HabitPracticeState, onAction: (HabitPracticeAction) -> Unit) {
    val c = MaterialTheme.modernColors
    // The tap target *is* the control: a big circle that fills as reps land.
    val fill by animateFloatAsState(state.progress, label = "repProgress")
    Box(
        modifier = Modifier.size(260.dp).clip(CircleShape)
            .background(c.primary.copy(alpha = 0.10f + 0.25f * fill))
            .bouncyClickable(enabled = !state.done) { onAction(HabitPracticeAction.OnAddRepClick) },
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
private fun SinglePractice(state: HabitPracticeState, onAction: (HabitPracticeAction) -> Unit) {
    val c = MaterialTheme.modernColors
    Text(
        text = if (state.done) "Done for today." else "This one is a simple check.",
        style = MaterialTheme.typography.titleMedium,
        color = c.textPrimary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(LifePlannerDesign.Spacing.lg))
    if (!state.done) {
        AppButton(text = "Mark done", onClick = { onAction(HabitPracticeAction.OnCompleteClick) }, modifier = Modifier.fillMaxWidth())
    }
}

private fun practiceHint(state: HabitPracticeState): String = when {
    state.isRunning -> "Hold steady"
    state.remainingSeconds < state.totalSeconds -> "Paused"
    else -> "Ready when you are"
}

@Preview
@Composable
private fun HabitPracticeScreenPreview() {
    LifePlannerTheme(darkTheme = true) {
        HabitPracticeScreen(state = habitPracticePreviewState(), onAction = {})
    }
}

/** A counted habit halfway through: the state a tester most wants to see. */
internal fun habitPracticePreviewState(): HabitPracticeState = HabitPracticeState(
    habit = Habit(
        id = "habit-pushups",
        title = "50 pushups",
        category = GoalCategory.BODY,
        frequency = HabitFrequency.DAILY,
        targetCount = 50,
        unit = "reps",
        createdAt = LocalDateTime(2026, 5, 1, 7, 0),
    ),
    mode = HabitTrackMode.COUNT,
    reps = 27,
    targetReps = 50,
    isLoading = false,
)
