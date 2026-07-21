package az.tribe.lifeplanner.ui.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.HealthMetric
import az.tribe.lifeplanner.ui.components.connect.ConnectStoryContent
import az.tribe.lifeplanner.ui.components.connect.FeatureConnectStory
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Barbell
import com.adamglin.phosphoricons.bold.CaretDown
import com.adamglin.phosphoricons.bold.CaretUp
import com.adamglin.phosphoricons.bold.Footprints
import com.adamglin.phosphoricons.bold.Heartbeat
import com.adamglin.phosphoricons.bold.Minus
import com.adamglin.phosphoricons.bold.Plus
import com.adamglin.phosphoricons.bold.TrendDown
import com.adamglin.phosphoricons.bold.TrendUp
import kotlin.math.roundToInt

internal enum class WeightTrend { UP, DOWN, STABLE }

@Composable
internal fun ExpandableMetricCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    summaryValue: String? = null,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (summaryValue != null && !expanded) {
                        Text(
                            text = summaryValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = iconTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        if (expanded) PhosphorIcons.Bold.CaretUp else PhosphorIcons.Bold.CaretDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
internal fun StepsCard(todaySteps: Long?, stepsGoal: Long) {
    val steps = todaySteps ?: 0L
    val progress = (steps.toFloat() / stepsGoal).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)

                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        PhosphorIcons.Bold.Footprints,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = "Today's Steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = steps.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Goal: $stepsGoal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
internal fun WeightCard(
    latestWeight: Double?,
    weightHistory: List<HealthMetric>,
    onAddWeight: () -> Unit
) {
    val trend = if (weightHistory.size >= 2) {
        val recent = weightHistory.takeLast(2)
        val diff = recent.last().value - recent.first().value
        when {
            diff > 0.1 -> WeightTrend.UP
            diff < -0.1 -> WeightTrend.DOWN
            else -> WeightTrend.STABLE
        }
    } else WeightTrend.STABLE

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    PhosphorIcons.Bold.Barbell,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Weight",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (latestWeight != null) "${((latestWeight * 10).roundToInt() / 10.0)} kg" else "No data",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (latestWeight != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when (trend) {
                                    WeightTrend.UP -> PhosphorIcons.Bold.TrendUp
                                    WeightTrend.DOWN -> PhosphorIcons.Bold.TrendDown
                                    WeightTrend.STABLE -> PhosphorIcons.Bold.Minus
                                },
                                contentDescription = trend.name,
                                modifier = Modifier.size(20.dp),
                                tint = when (trend) {
                                    WeightTrend.UP -> Color(0xFFE57373)
                                    WeightTrend.DOWN -> Color(0xFF81C784)
                                    WeightTrend.STABLE -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onAddWeight) {
                Icon(
                    PhosphorIcons.Bold.Plus,
                    contentDescription = "Add weight",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
internal fun HealthNotAvailableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                PhosphorIcons.Bold.Barbell,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Health Data Not Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Health Connect (Android) or Apple Health (iOS) is not available on this device.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A looping "living" illustration for the health permission prompt: concentric radar rings
 * ripple outward from a gently breathing heartbeat icon. Pure Compose (no Lottie / no assets),
 * so it renders identically on Android and iOS with no extra dependency.
 */
@Composable
internal fun AnimatedHealthPulse(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "health-pulse")
    val period = 2200
    // Three ripple rings, evenly staggered across the period, each expanding and fading.
    val ring1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(period, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1"
    )
    val ring2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(period, delayMillis = period / 3, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring3 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(period, delayMillis = period * 2 / 3, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "ring3"
    )
    val breathe by transition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2f
            listOf(ring1, ring2, ring3).forEach { p ->
                drawCircle(
                    color = tint.copy(alpha = (1f - p) * 0.35f),
                    radius = maxRadius * (0.30f + 0.70f * p),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            // Soft filled core the icon sits on.
            drawCircle(color = tint.copy(alpha = 0.12f), radius = maxRadius * 0.32f)
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { scaleX = breathe; scaleY = breathe }
        )
    }
}

/** The "connect Health" story content, shared by the dedicated screen and the embedded section. */
private val healthConnectStory = ConnectStoryContent(
    eyebrow = "Health",
    title = "Connect your health data",
    story = "LifePlanner reads your steps, sleep, heart rate and weight from Health Connect so your " +
        "day fills in on its own. Your data stays on your device until you choose to sync it.",
    benefits = listOf(
        "Steps count toward your daily goal automatically",
        "See sleep and heart-rate trends over time",
        "Health progress feeds your goals and streaks",
    ),
    ctaLabel = "Grant Access",
    footnote = "On Android, make sure Health Connect is installed from the Play Store.",
)

@Composable
internal fun PermissionDeniedCard(
    onRequestPermissions: () -> Unit,
    connecting: Boolean = false,
) {
    FeatureConnectStory(
        content = healthConnectStory,
        onConnect = onRequestPermissions,
        connecting = connecting,
        hero = {
            AnimatedHealthPulse(
                icon = PhosphorIcons.Bold.Heartbeat,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
internal fun ManualWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight") },
        text = {
            Column {
                Text(
                    text = "Enter your current weight in kilograms",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        isError = false
                    },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a valid weight (e.g., 70.5)") }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightText.toDoubleOrNull()
                    if (weight != null && weight > 0 && weight < 500) {
                        onConfirm(weight)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun ZoneDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
