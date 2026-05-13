package az.tribe.lifeplanner.ui.balance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import az.tribe.lifeplanner.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val COLOR_BALANCE = Color(0xFF6366F1)
private val COLOR_HABITS  = Color(0xFF28C76F)
private val COLOR_GOALS   = Color(0xFFFF9800)

@Composable
internal fun LifeProgressRingsCard(
    report: LifeBalanceReport,
    habitsCompleted: Int,
    totalHabits: Int,
    modifier: Modifier = Modifier,
) {
    val balanceFraction = report.overallScore / 100f
    val habitFraction   = if (totalHabits > 0) habitsCompleted.toFloat() / totalHabits else 0f
    val avgHabitRate    = if (report.areaScores.isNotEmpty())
        report.areaScores.map { it.habitCompletionRate }.average().toFloat() else 0f

    val animBalance = remember { Animatable(0f) }
    val animHabit   = remember { Animatable(0f) }
    val animAvg     = remember { Animatable(0f) }

    LaunchedEffect(balanceFraction) {
        animBalance.animateTo(balanceFraction, tween(1300, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(habitFraction) {
        delay(180)
        animHabit.animateTo(habitFraction, tween(1100, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(avgHabitRate) {
        delay(360)
        animAvg.animateTo(avgHabitRate, tween(900, easing = FastOutSlowInEasing))
    }

    val strongest = report.strongestAreas.firstOrNull()
    val weakest   = report.weakestAreas.firstOrNull()
    val insight = when {
        report.overallScore >= 80 ->
            "You're thriving — all areas of your life are in motion."
        strongest != null && weakest != null ->
            "${strongest.displayName} is your strongest area right now. ${weakest.displayName} could use a little more love this week."
        weakest != null ->
            "You've been consistent. Let's give ${weakest.displayName} some attention next."
        else -> "Keep building momentum across your life areas."
    }

    GlassCard(modifier = modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Concentric rings ──────────────────────────────────────────────
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 9.dp.toPx()
                        val gap    = 5.dp.toPx()

                        val rOuter  = size.minDimension / 2f - stroke / 2f
                        val rMiddle = rOuter  - stroke - gap
                        val rInner  = rMiddle - stroke - gap

                        fun arcOffset(r: Float) = Offset(center.x - r, center.y - r)
                        fun arcSize(r: Float)   = Size(r * 2, r * 2)

                        // Outer track + progress
                        drawArc(COLOR_BALANCE.copy(alpha = 0.13f), -90f, 360f, false,
                            arcOffset(rOuter), arcSize(rOuter), style = Stroke(stroke, cap = StrokeCap.Round))
                        if (animBalance.value > 0.01f)
                            drawArc(COLOR_BALANCE, -90f, 360f * animBalance.value, false,
                                arcOffset(rOuter), arcSize(rOuter), style = Stroke(stroke, cap = StrokeCap.Round))

                        // Middle track + progress
                        drawArc(COLOR_HABITS.copy(alpha = 0.13f), -90f, 360f, false,
                            arcOffset(rMiddle), arcSize(rMiddle), style = Stroke(stroke, cap = StrokeCap.Round))
                        if (animHabit.value > 0.01f)
                            drawArc(COLOR_HABITS, -90f, 360f * animHabit.value, false,
                                arcOffset(rMiddle), arcSize(rMiddle), style = Stroke(stroke, cap = StrokeCap.Round))

                        // Inner track + progress
                        drawArc(COLOR_GOALS.copy(alpha = 0.13f), -90f, 360f, false,
                            arcOffset(rInner), arcSize(rInner), style = Stroke(stroke, cap = StrokeCap.Round))
                        if (animAvg.value > 0.01f)
                            drawArc(COLOR_GOALS, -90f, 360f * animAvg.value, false,
                                arcOffset(rInner), arcSize(rInner), style = Stroke(stroke, cap = StrokeCap.Round))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${report.overallScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = COLOR_BALANCE,
                            fontSize = 22.sp
                        )
                        Text(
                            "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            fontSize = 9.sp
                        )
                    }
                }

                // ── Ring legend ───────────────────────────────────────────────────
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RingLegendRow(
                        color = COLOR_BALANCE,
                        label = "Life balance",
                        value = "${report.overallScore}%"
                    )
                    RingLegendRow(
                        color = COLOR_HABITS,
                        label = "Habits today",
                        value = if (totalHabits > 0) "$habitsCompleted / $totalHabits" else "—"
                    )
                    RingLegendRow(
                        color = COLOR_GOALS,
                        label = "Habit rate",
                        value = "${(avgHabitRate * 100).roundToInt()}%"
                    )
                }
            }

            // ── Personalized insight ──────────────────────────────────────────────
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                insight,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RingLegendRow(color: Color, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

fun generateWeeklyInsight(report: LifeBalanceReport?): String? {
    if (report == null) return null
    val strongest = report.strongestAreas.take(2).joinToString(" & ") { it.displayName }
    val weakest   = report.weakestAreas.firstOrNull()?.displayName
    return when {
        report.overallScore >= 75 && strongest.isNotBlank() ->
            "You're showing up for $strongest. Keep that energy going."
        weakest != null && strongest.isNotBlank() ->
            "Consistent in $strongest this week — $weakest still has room to grow."
        weakest != null ->
            "$weakest is the area to focus on next. Small steps count."
        else -> null
    }
}
