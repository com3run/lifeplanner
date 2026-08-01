package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.Briefcase
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.Barbell
import com.adamglin.phosphoricons.regular.Users
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Flower
import com.adamglin.phosphoricons.regular.House
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import com.adamglin.phosphoricons.regular.Play
import az.tribe.lifeplanner.domain.service.targetSeconds
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.ui.habit.HabitWithStatus
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// Haptic feedback
private var lastThresholdState: ThresholdState = ThresholdState.NONE

private enum class ThresholdState {
    NONE, EDIT_ZONE, DELETE_ZONE, CHECK_IN_ZONE
}

// Swipe thresholds
internal const val EDIT_THRESHOLD = 0.25f  // 25% for edit
private const val DELETE_THRESHOLD = 0.45f  // 45% for delete

/**
 * Swipeable wrapper for HabitCard
 * - Swipe right (start-to-end): Check-in / Toggle completion
 * - Swipe left (end-to-start):
 *   - Light swipe (25-45%): Edit
 *   - Heavy swipe (>45%): Delete
 */
@Composable
fun SwipeableHabitCard(
    habitWithStatus: HabitWithStatus,
    onCheckIn: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onFocusClick: (() -> Unit)? = null,
    onIncrement: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onPractice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isCompletedToday = habitWithStatus.isCompletedToday
    val haptic = rememberHapticManager()

    var offsetX by remember { mutableStateOf(0f) }
    var cardWidth by remember { mutableStateOf(1f) }

    // Track threshold state for haptic feedback
    var currentThresholdState by remember { mutableStateOf(ThresholdState.NONE) }

    // Calculate swipe progress (0 to 1)
    val swipeProgress = (offsetX.absoluteValue / cardWidth).coerceIn(0f, 1f)
    val isSwipingLeft = offsetX < 0
    val isSwipingRight = offsetX > 0

    // Determine action based on threshold
    val isInDeleteZone = isSwipingLeft && swipeProgress >= DELETE_THRESHOLD
    val isInEditZone = isSwipingLeft && swipeProgress >= EDIT_THRESHOLD && swipeProgress < DELETE_THRESHOLD
    val isInCheckInZone = isSwipingRight && swipeProgress >= EDIT_THRESHOLD

    // Haptic feedback when crossing thresholds
    val newThresholdState = when {
        isInDeleteZone -> ThresholdState.DELETE_ZONE
        isInEditZone -> ThresholdState.EDIT_ZONE
        isInCheckInZone -> ThresholdState.CHECK_IN_ZONE
        else -> ThresholdState.NONE
    }

    LaunchedEffect(newThresholdState) {
        if (newThresholdState != currentThresholdState && newThresholdState != ThresholdState.NONE) {
            when (newThresholdState) {
                ThresholdState.DELETE_ZONE -> haptic.swipeDeleteZone()
                ThresholdState.EDIT_ZONE -> haptic.swipeActionZone()
                ThresholdState.CHECK_IN_ZONE -> haptic.swipeActionZone()
                else -> {}
            }
        }
        currentThresholdState = newThresholdState
    }

    // Animated offset for smooth return
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(
            durationMillis = if (offsetX == 0f) 200 else 0
        ),
        label = "offsetAnimation"
    )

    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(-cardWidth * 0.6f, cardWidth * 0.4f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { cardWidth = it.width.toFloat() }
    ) {
        // Background layer - uses matchParentSize to match card height
        HabitSwipeBackgroundNew(
            swipeProgress = swipeProgress,
            isSwipingLeft = isSwipingLeft,
            isSwipingRight = isSwipingRight,
            isInDeleteZone = isInDeleteZone,
            isInEditZone = isInEditZone,
            isCompletedToday = isCompletedToday,
            modifier = Modifier.matchParentSize()
        )

        // Foreground card
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        when {
                            isInDeleteZone -> {
                                haptic.warning()
                                showDeleteDialog = true
                            }
                            isInEditZone -> {
                                haptic.click()
                                onEdit()
                            }
                            isInCheckInZone -> {
                                haptic.success()
                                onCheckIn()
                            }
                        }
                        offsetX = 0f
                        currentThresholdState = ThresholdState.NONE
                    }
                )
        ) {
            HabitCard(
                habitWithStatus = habitWithStatus,
                onCheckIn = onCheckIn,
                onFocusClick = onFocusClick,
                onIncrement = onIncrement,
                onCardClick = onCardClick,
                onPractice = onPractice
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Trash,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Delete Habit?") },
            text = { Text("Are you sure you want to delete \"${habitWithStatus.habit.title}\"? This will remove all check-in history. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onCheckIn: (() -> Unit)? = null,
    onFocusClick: (() -> Unit)? = null,
    onIncrement: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onPractice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStatus.habit
    val isCompletedToday = habitWithStatus.isCompletedToday
    val trackMode = habit.trackMode
    val isCountBased = trackMode == HabitTrackMode.COUNT
    val todayCount = habitWithStatus.todayCount
    val categoryColor = habit.category.backgroundColor()

    val haptic = rememberHapticManager()
    val soundPlayer = rememberCelebrationSoundPlayer()
    var showBurst by remember { mutableStateOf(false) }
    val previouslyCompleted = remember { mutableStateOf(isCompletedToday) }

    LaunchedEffect(isCompletedToday) {
        val wasCompleted = previouslyCompleted.value
        previouslyCompleted.value = isCompletedToday
        if (isCompletedToday && !wasCompleted) {
            showBurst = true
            haptic.success()
            soundPlayer.play(CelebrationType.STREAK_MILESTONE)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .then(if (onCardClick != null) Modifier.clickable(onClick = onCardClick) else Modifier),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompletedToday) LifePlannerDesign.Elevation.none else LifePlannerDesign.Elevation.low
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // No green wash on completion. The strikethrough title + gray check are enough;
                // the card should quietly recede, not light up.

                .padding(LifePlannerDesign.Padding.standard)
        ) {
            // One clean row: icon, title + one compact subtitle, (+1 for count), check.
            // Minimised per Kamran: dropped the weekly dots, frequency chip, focus button,
            // description and progress bar so the card is just "see it, tick it".
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(
                    icon = habitIcon(habit.title, habit.category),
                    isCompleted = isCompletedToday
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isCompletedToday)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // A single subtitle. Count habits carry their progress on the pill now, so this
                    // line is free for the streak; duration habits use it to state how long.
                    if (!isCompletedToday) {
                        when {
                            trackMode == HabitTrackMode.DURATION -> Text(
                                text = durationLabel(habit),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = categoryColor,
                            )
                            habit.currentStreak > 0 -> Text(
                                text = "🔥 ${habit.currentStreak} day${if (habit.currentStreak > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Progress pill for count habits: shows where you are, and tapping adds one.
                // "2/8" tells you more than "+1" ever did, and it replaces the old subtitle line.
                if (isCountBased && !isCompletedToday && onIncrement != null) {
                    Surface(
                        onClick = onIncrement,
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$todayCount/${habit.targetCount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // One icon, not a button: the card stays "see it, tick it", but a habit you have
                // to actually perform gets a way in. Hidden once done, and for habits with
                // nothing to run.
                if (onPractice != null && !isCompletedToday && trackMode != HabitTrackMode.SINGLE) {
                    IconButton(onClick = onPractice, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Play,
                            contentDescription = "Practice",
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                CheckInCircle(
                    isCompleted = isCompletedToday,
                    onClick = onCheckIn
                )
            }
        }
    }

    if (showBurst) {
        CheckInBurstOverlay(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.medium)),
            onComplete = { showBurst = false }
        )
    }
    } // end Box
}


fun GoalCategory.getIcon(): ImageVector {
    return when (this) {
        GoalCategory.CAREER -> PhosphorIcons.Regular.Briefcase
        GoalCategory.MONEY -> PhosphorIcons.Regular.Bank
        GoalCategory.BODY -> PhosphorIcons.Regular.Barbell
        GoalCategory.PEOPLE -> PhosphorIcons.Regular.Users
        GoalCategory.WELLBEING -> PhosphorIcons.Regular.Heart
        GoalCategory.PURPOSE -> PhosphorIcons.Regular.Flower
        GoalCategory.FAMILY -> PhosphorIcons.Regular.House
    }
}

/** "30 sec" / "3 min" / "2 hrs" — a duration habit stated in the unit the user wrote it in. */
private fun durationLabel(habit: az.tribe.lifeplanner.domain.model.Habit): String {
    val seconds = habit.targetSeconds ?: return ""
    return when {
        seconds < 60 -> "$seconds sec"
        seconds % 3600 == 0 -> "${seconds / 3600} hr${if (seconds > 3600) "s" else ""}"
        else -> "${seconds / 60} min"
    }
}
