package az.tribe.lifeplanner.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.gradientColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.X
import kotlinx.coroutines.delay

@Composable
internal fun FocusActiveContent(
    focusViewModel: FocusViewModel
) {
    val timerState by focusViewModel.timerState.collectAsState()
    val remainingSeconds by focusViewModel.remainingSeconds.collectAsState()
    val progress by focusViewModel.progress.collectAsState()
    val selectedGoal by focusViewModel.selectedGoal.collectAsState()
    val selectedMilestone by focusViewModel.selectedMilestone.collectAsState()
    val elapsedSeconds by focusViewModel.elapsedSeconds.collectAsState()
    val isFreeFlow by focusViewModel.isFreeFlow.collectAsState()
    val selectedFocusTheme by focusViewModel.selectedFocusTheme.collectAsState()

    val isRunning = timerState == TimerState.RUNNING
    val gradientColors = selectedGoal?.category?.gradientColors()
        ?: listOf(Color(0xFFFF6B35), Color(0xFFFFA726))

    var showControls by remember { mutableStateOf(true) }
    var tapCounter by remember { mutableStateOf(0) }

    LaunchedEffect(tapCounter, isRunning) {
        if (isRunning) {
            showControls = true
            delay(5000)
            showControls = false
        } else {
            showControls = true
        }
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(400),
        label = "controlsAlpha"
    )

    val motivationalTexts = remember {
        listOf(
            "Stay focused, you're doing great!",
            "Deep work builds extraordinary results.",
            "One minute at a time.",
            "Your future self will thank you.",
            "Progress, not perfection.",
            "This is where growth happens.",
            "Stay in the zone.",
            "You've got this!"
        )
    }
    val messageIndex = (elapsedSeconds / 300) % motivationalTexts.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapCounter++ }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FocusProgressRing(
                progress = progress,
                remainingSeconds = remainingSeconds,
                isRunning = isRunning,
                elapsedSeconds = elapsedSeconds,
                isFreeFlow = isFreeFlow,
                gradientColors = gradientColors,
                theme = selectedFocusTheme,
                size = 240.dp
            )

            Spacer(Modifier.height(20.dp))

            Text(
                motivationalTexts[messageIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f * controlsAlpha),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .alpha(controlsAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            selectedGoal?.let { goal ->
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            selectedMilestone?.let { milestone ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        milestone.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    bottom = 48.dp,
                    start = LifePlannerDesign.Padding.screenHorizontal,
                    end = LifePlannerDesign.Padding.screenHorizontal
                )
                .alpha(controlsAlpha),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { focusViewModel.cancelTimer() },
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = showControls
            ) {
                Icon(PhosphorIcons.Regular.X, "Cancel", modifier = Modifier.size(20.dp))
            }

            Button(
                onClick = {
                    if (isRunning) focusViewModel.pauseTimer()
                    else focusViewModel.resumeTimer()
                },
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = gradientColors.first()),
                enabled = showControls
            ) {
                Icon(
                    if (isRunning) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                    if (isRunning) "Pause" else "Resume",
                    modifier = Modifier.size(32.dp)
                )
            }

            if (isFreeFlow) {
                FilledTonalButton(
                    onClick = { focusViewModel.completeFreeFlowSession() },
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    enabled = showControls
                ) {
                    Icon(PhosphorIcons.Regular.Check, "Done", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            } else {
                FilledTonalButton(
                    onClick = { focusViewModel.addFiveMinutes() },
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = showControls
                ) {
                    Text("+5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
