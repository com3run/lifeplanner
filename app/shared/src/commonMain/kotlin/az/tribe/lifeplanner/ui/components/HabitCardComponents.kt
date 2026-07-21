package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Fire
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun HabitSwipeBackgroundNew(
    swipeProgress: Float,
    isSwipingLeft: Boolean,
    isSwipingRight: Boolean,
    isInDeleteZone: Boolean,
    isInEditZone: Boolean,
    isCompletedToday: Boolean,
    modifier: Modifier = Modifier
) {
    // Colors for different states
    val editColor = Color(0xFF2196F3) // Blue for edit
    val deleteColor = Color(0xFFF44336) // Red for delete
    val checkInColor = if (isCompletedToday) Color(0xFFFF9800) else Color(0xFF4CAF50)

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isInDeleteZone -> deleteColor
            isInEditZone -> editColor
            isSwipingLeft && swipeProgress > 0.1f -> editColor.copy(alpha = 0.7f)
            isSwipingRight && swipeProgress > 0.1f -> checkInColor
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "backgroundColor"
    )

    val icon = when {
        isInDeleteZone -> PhosphorIcons.Regular.Trash
        isSwipingLeft -> PhosphorIcons.Regular.PencilSimple
        isSwipingRight && isCompletedToday -> PhosphorIcons.Regular.ArrowCounterClockwise
        isSwipingRight -> PhosphorIcons.Regular.CheckCircle
        else -> null
    }

    val actionText = when {
        isInDeleteZone -> "Delete"
        isInEditZone -> "Edit"
        isSwipingLeft && swipeProgress > 0.1f -> "Edit"
        isSwipingRight && swipeProgress > EDIT_THRESHOLD -> if (isCompletedToday) "Undo" else "Check in"
        else -> null
    }

    val alignment = if (isSwipingLeft) Alignment.CenterEnd else Alignment.CenterStart

    val iconScale by animateFloatAsState(
        targetValue = when {
            isInDeleteZone -> 1.2f
            swipeProgress > 0.15f -> 1f
            else -> 0.8f
        },
        animationSpec = tween(150),
        label = "iconScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.medium))
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (swipeProgress > 0.1f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isSwipingLeft) Arrangement.End else Arrangement.Start,
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
            ) {
                if (!isSwipingLeft) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = actionText,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                actionText?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isSwipingLeft) {
                    Spacer(modifier = Modifier.width(8.dp))
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = actionText,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Burst animation that plays inside the card on check-in:
 * 3 expanding rings + 8 particles flying outward from the check circle position,
 * plus a brief green flash across the card.
 */
@Composable
internal fun CheckInBurstOverlay(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit
) {
    val green = Color(0xFF4CAF50)

    val ring1Scale = remember { Animatable(0f) }
    val ring2Scale = remember { Animatable(0f) }
    val ring3Scale = remember { Animatable(0f) }
    val ring1Alpha = remember { Animatable(0.7f) }
    val ring2Alpha = remember { Animatable(0.5f) }
    val ring3Alpha = remember { Animatable(0.35f) }
    val particleProgress = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0.14f) }

    LaunchedEffect(Unit) {
        launch { ring1Scale.animateTo(3f, tween(580, easing = FastOutSlowInEasing)) }
        launch { delay(80);  ring1Alpha.animateTo(0f, tween(500)) }
        launch { delay(110); ring2Scale.animateTo(3.6f, tween(680, easing = FastOutSlowInEasing)) }
        launch { delay(240); ring2Alpha.animateTo(0f, tween(560)) }
        launch { delay(220); ring3Scale.animateTo(4.2f, tween(780, easing = FastOutSlowInEasing)) }
        launch { delay(380); ring3Alpha.animateTo(0f, tween(620)) }
        launch { particleProgress.animateTo(1f, tween(720, easing = FastOutSlowInEasing)) }
        flashAlpha.animateTo(0f, tween(380))
        delay(680)
        onComplete()
    }

    // Read in composable scope so Canvas captures reactive snapshots
    val r1 = ring1Scale.value; val a1 = ring1Alpha.value
    val r2 = ring2Scale.value; val a2 = ring2Alpha.value
    val r3 = ring3Scale.value; val a3 = ring3Alpha.value
    val pp = particleProgress.value
    val fa = flashAlpha.value

    Box(modifier = modifier) {
        // Brief green flash overlay
        if (fa > 0.01f) {
            Box(modifier = Modifier.matchParentSize().background(green.copy(alpha = fa)))
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            // Check circle sits at top-right of card
            val cx = size.width - 34.dp.toPx()
            val cy = 38.dp.toPx()
            val baseR = 14.dp.toPx()

            // 3 expanding rings
            listOf(
                Triple(r1, a1, 2.dp.toPx()),
                Triple(r2, a2, 1.5.dp.toPx()),
                Triple(r3, a3, 1.dp.toPx()),
            ).forEach { (scale, alpha, stroke) ->
                if (alpha > 0.01f) {
                    drawCircle(
                        color = green.copy(alpha = alpha),
                        radius = baseR * scale,
                        center = Offset(cx, cy),
                        style = Stroke(width = stroke)
                    )
                }
            }

            // 8 particles flying outward
            if (pp > 0f) {
                repeat(8) { i ->
                    val angleRad = (i * 45.0) * PI / 180.0
                    val distance = baseR * 3.5f * pp
                    val px = cx + cos(angleRad).toFloat() * distance
                    val py = cy + sin(angleRad).toFloat() * distance
                    val dotAlpha = (1f - pp).coerceIn(0f, 1f)
                    val dotRadius = 3.dp.toPx() * (1f - pp * 0.4f)
                    drawCircle(
                        color = green.copy(alpha = dotAlpha),
                        radius = dotRadius,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

/**
 * Category icon badge, shows the category symbol.
 * When completed, shows a green background with check overlay.
 */
@Composable
internal fun CategoryIconBadge(
    category: GoalCategory,
    isCompleted: Boolean
) {
    val categoryColor = category.backgroundColor()

    // Done reads as a calm, muted gray, not a celebratory green. The whole card is meant to
    // recede once completed, not compete for attention.
    val doneGray = MaterialTheme.colorScheme.onSurfaceVariant

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) doneGray.copy(alpha = 0.14f) else categoryColor.copy(alpha = 0.12f),
        animationSpec = tween(300),
        label = "iconBgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isCompleted) doneGray else categoryColor,
        animationSpec = tween(300),
        label = "iconTint"
    )

    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = category.getIcon(),
                    contentDescription = category.name,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Tappable circle for check-in.
 * Uncompleted: outline ring + ghost check icon, clearly "tap to complete".
 * Completed: solid green filled circle with animated checkmark.
 */
@Composable
internal fun CheckInCircle(
    isCompleted: Boolean,
    onClick: (() -> Unit)? = null
) {
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = if (isCompleted) keyframes {
            durationMillis = 300
            0f at 0
            1.2f at 150
            1f at 300
        } else tween(150),
        label = "checkScale"
    )

    // Muted gray "done", not green. The check still reads clearly; it just does not shout.
    val fillColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.Transparent,
        animationSpec = tween(200),
        label = "fillColor"
    )

    val ringColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(fillColor)
                .then(
                    if (!isCompleted) Modifier.border(1.5.dp, ringColor, CircleShape)
                    else Modifier
                )
        ) {
            if (!isCompleted) {
                // Empty ring, no check icon so it clearly reads as "not done yet"
            } else {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(150)) + scaleIn(tween(150))
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Completed",
                        // Contrast against the gray fill in both light and dark.
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(checkScale)
                    )
                }
            }
        }
    }
}

/**
 * Mini weekly completion dots, 7 dots for Mon through Sun.
 * Completed days are filled with the category color, future/incomplete days are hollow.
 */
@Composable
internal fun WeeklyDots(
    completions: List<Boolean>,
    categoryColor: Color
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        completions.forEachIndexed { index, completed ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) categoryColor
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                )
                if (index < dayLabels.size) {
                    Text(
                        text = dayLabels[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.75f
                    )
                }
            }
        }
    }
}

@Composable
internal fun StreakBadge(
    streak: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.Fire,
            contentDescription = null,
            tint = Color(0xFFFF6B35),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "${streak}d",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFFFF6B35)
        )
    }
}

@Composable
internal fun HabitTypePill(type: HabitType) {
    val color = if (type == HabitType.BUILD) Color(0xFF4CAF50) else Color(0xFFF44336)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
internal fun FrequencyChip(frequency: HabitFrequency) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = frequency.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
