package az.tribe.lifeplanner.ui.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import az.tribe.lifeplanner.ui.components.CelebrationOverlay
import az.tribe.lifeplanner.ui.components.CelebrationType
import az.tribe.lifeplanner.ui.components.KeepScreenOn
import az.tribe.lifeplanner.ui.components.rememberAmbientSoundPlayer
import az.tribe.lifeplanner.ui.components.rememberCelebrationSoundPlayer
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    goalId: String? = null,
    milestoneId: String? = null,
    focusViewModel: FocusViewModel = koinViewModel()
) {
    val timerState by focusViewModel.timerState.collectAsState()
    val selectedAmbientSound by focusViewModel.selectedAmbientSound.collectAsState()
    val selectedFocusTheme by focusViewModel.selectedFocusTheme.collectAsState()
    val selectedMood by focusViewModel.selectedMood.collectAsState()
    val soundPlayer = rememberCelebrationSoundPlayer()
    val hapticManager = rememberHapticManager()
    val ambientSoundPlayer = rememberAmbientSoundPlayer()

    LaunchedEffect(goalId, milestoneId) {
        if (goalId != null && milestoneId != null) {
            focusViewModel.preSelectGoalAndMilestone(goalId, milestoneId)
        } else if (goalId == null && milestoneId == null) {
            focusViewModel.setTimerMode(true)
        }
    }

    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(timerState) {
        if (timerState == TimerState.COMPLETED) {
            hapticManager.celebration()
            soundPlayer.play(CelebrationType.GOAL_COMPLETED)
            showCelebration = true
        }
    }

    LaunchedEffect(timerState, selectedAmbientSound) {
        when (timerState) {
            TimerState.RUNNING -> ambientSoundPlayer.play(selectedAmbientSound)
            TimerState.PAUSED -> ambientSoundPlayer.stop()
            TimerState.COMPLETED, TimerState.CANCELLED -> ambientSoundPlayer.stop()
            TimerState.IDLE -> ambientSoundPlayer.stop()
        }
    }

    val isActive = timerState == TimerState.RUNNING || timerState == TimerState.PAUSED

    KeepScreenOn(isActive)

    Scaffold(
        topBar = {
            if (timerState == TimerState.IDLE) {
                TopAppBar(
                    title = { Text("Focus Timer", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(PhosphorIcons.Regular.ArrowLeft, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        containerColor = if (isActive) Color.Transparent else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isActive) {
                FocusAnimatedBackground(
                    theme = selectedFocusTheme,
                    mood = selectedMood,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AnimatedContent(
                    targetState = timerState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "focusContent"
                ) { state ->
                    when (state) {
                        TimerState.IDLE -> FocusSetupContent(
                            focusViewModel = focusViewModel,
                            onStartFocus = { focusViewModel.startTimer() }
                        )
                        TimerState.RUNNING, TimerState.PAUSED -> FocusActiveContent(
                            focusViewModel = focusViewModel
                        )
                        TimerState.COMPLETED -> FocusCompleteContent(
                            focusViewModel = focusViewModel,
                            onDone = onNavigateBack,
                            onStartAnother = { focusViewModel.resetToSetup() }
                        )
                        TimerState.CANCELLED -> FocusCompleteContent(
                            focusViewModel = focusViewModel,
                            onDone = onNavigateBack,
                            onStartAnother = { focusViewModel.resetToSetup() },
                            wasCancelled = true
                        )
                    }
                }

                if (timerState == TimerState.COMPLETED) {
                    CelebrationOverlay(
                        type = CelebrationType.GOAL_COMPLETED,
                        message = "Focus Complete!",
                        isVisible = showCelebration,
                        onDismiss = { showCelebration = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusSetupContent(
    focusViewModel: FocusViewModel,
    onStartFocus: () -> Unit
) {
    val isFreeFlow by focusViewModel.isFreeFlow.collectAsState()
    if (isFreeFlow) {
        FocusFreeFlowSetup(focusViewModel, onStartFocus)
    } else {
        FocusTimedSetup(focusViewModel, onStartFocus)
    }
}
