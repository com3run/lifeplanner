package az.tribe.lifeplanner.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.ui.components.GlassCard
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Footprints
import kotlinx.datetime.LocalDate

@Composable
fun TodayPulseCard(
    today: LocalDate,
    habitsCompleted: Int,
    totalHabits: Int,
    todayFocusMinutes: Int,
    todaySteps: Long?,
    healthConnected: Boolean,
    onHabitsClick: () -> Unit,
    onFocusClick: () -> Unit,
    onStepsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val monthName = today.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

    val completionFraction = if (totalHabits > 0) habitsCompleted.toFloat() / totalHabits else 0f
    val animBar = remember(habitsCompleted, totalHabits) { Animatable(0f) }
    LaunchedEffect(habitsCompleted, totalHabits) {
        animBar.animateTo(completionFraction, tween(800, easing = FastOutSlowInEasing))
    }

    val allDone = totalHabits > 0 && habitsCompleted == totalHabits
    val stepsText = when {
        !healthConnected || todaySteps == null -> "—"
        todaySteps >= 1000L -> "${todaySteps / 1000}.${(todaySteps % 1000) / 100}K"
        else -> todaySteps.toString()
    }
    val focusText = when {
        todayFocusMinutes >= 60 -> "${todayFocusMinutes / 60}h ${todayFocusMinutes % 60}m"
        todayFocusMinutes > 0 -> "${todayFocusMinutes}m"
        else -> "—"
    }

    GlassCard(modifier = modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$dayName, $monthName ${today.dayOfMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (allDone) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF28C76F).copy(alpha = 0.12f)) {
                        Text(
                            "All habits done!",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF28C76F),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PulseTile(
                    value = if (totalHabits > 0) "$habitsCompleted/$totalHabits" else "—",
                    label = "Habits",
                    accentColor = Color(0xFF28C76F),
                    onClick = onHabitsClick,
                    modifier = Modifier.weight(1f)
                )
                PulseTile(
                    value = focusText,
                    label = "Focus",
                    accentColor = Color(0xFFFF6B35),
                    onClick = onFocusClick,
                    modifier = Modifier.weight(1f)
                )
                PulseTile(
                    value = stepsText,
                    label = "Steps",
                    accentColor = Color(0xFF00CFE8),
                    onClick = onStepsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            if (totalHabits > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Habit completion",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                        Text(
                            "${(completionFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF28C76F)
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF28C76F).copy(alpha = 0.10f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animBar.value.coerceIn(0f, 1f))
                                .height(5.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF28C76F), Color(0xFF00CFE8))),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            if (!healthConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onStepsClick),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Footprints,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Connect Health to track steps, sleep & heart rate",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        PhosphorIcons.Regular.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PulseTile(
    value: String,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
