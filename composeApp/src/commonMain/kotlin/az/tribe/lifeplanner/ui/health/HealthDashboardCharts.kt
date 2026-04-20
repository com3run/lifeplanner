package az.tribe.lifeplanner.ui.health

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.HealthMetric
import kotlin.math.roundToInt

@Composable
internal fun StepsDetailView(data: List<HealthMetric>, stepsGoal: Long = 10_000L) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxSteps = data.maxOf { it.value }.coerceAtLeast(stepsGoal.toDouble())

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.forEach { metric ->
            val fraction = (metric.value / maxSteps).toFloat().coerceIn(0f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(600)
            )
            val metGoal = metric.value >= stepsGoal
            val barColor = if (metGoal) Color(0xFF4CAF50) else Color(0xFFA5D6A7)
            val todayHighlight = isToday(metric.date)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (todayHighlight) Modifier.background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dayLabel(metric.date)} ${metric.date.day}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (todayHighlight) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(52.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(barColor)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatCompact(metric.value),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary row
        val dailyAvg = data.sumOf { it.value } / data.size
        val weeklyTotal = data.sumOf { it.value }.toLong()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Daily Average",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCompact(dailyAvg),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Weekly Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCompact(weeklyTotal.toDouble()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun HeartRateDetailView(data: List<HealthMetric>) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minBpm = data.minOf { it.value }
    val maxBpm = data.maxOf { it.value }
    val avgBpm = data.sumOf { it.value } / data.size

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Top stats: Min / Avg / Max
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${minBpm.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF42A5F5)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${avgBpm.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF66BB6A)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Max",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${maxBpm.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA726)
                )
            }
        }

        // Canvas line chart with cubic Bezier
        val range = (maxBpm - minBpm).coerceAtLeast(1.0)
        val lineColor = Color(0xFFE57373)

        if (data.size == 1) {
            // Single data point — just show the value centered
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${data[0].value.roundToInt()} bpm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = heartRateZoneColor(data[0].value)
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val paddingY = 16.dp.toPx()
                val chartHeight = size.height - paddingY * 2
                val stepX = size.width / (data.size - 1)

                fun yPos(value: Double): Float =
                    paddingY + chartHeight - ((value - minBpm) / range * chartHeight).toFloat()

                // Gradient fill under curve
                val fillPath = Path().apply {
                    moveTo(0f, size.height)
                    // First point
                    lineTo(0f, yPos(data[0].value))
                    for (i in 0 until data.size - 1) {
                        val x0 = i * stepX
                        val x1 = (i + 1) * stepX
                        val y0 = yPos(data[i].value)
                        val y1 = yPos(data[i + 1].value)
                        val cx = (x0 + x1) / 2f
                        cubicTo(cx, y0, cx, y1, x1, y1)
                    }
                    lineTo((data.size - 1) * stepX, size.height)
                    close()
                }
                drawPath(
                    fillPath,
                    Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.0f))
                    )
                )

                // Smooth line
                val linePath = Path().apply {
                    moveTo(0f, yPos(data[0].value))
                    for (i in 0 until data.size - 1) {
                        val x0 = i * stepX
                        val x1 = (i + 1) * stepX
                        val y0 = yPos(data[i].value)
                        val y1 = yPos(data[i + 1].value)
                        val cx = (x0 + x1) / 2f
                        cubicTo(cx, y0, cx, y1, x1, y1)
                    }
                }
                drawPath(linePath, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                // Dots colored by zone
                data.forEachIndexed { index, metric ->
                    val x = index * stepX
                    val y = yPos(metric.value)
                    val zoneColor = heartRateZoneColor(metric.value)
                    drawCircle(color = zoneColor, radius = 5.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(x, y))
                }
            }
        }

        // X-axis day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { metric ->
                Text(
                    text = dayLabel(metric.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Zone legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZoneDot(color = Color(0xFF42A5F5), label = "<60 Low")
            ZoneDot(color = Color(0xFF66BB6A), label = "60-100 Normal")
            ZoneDot(color = Color(0xFFFFA726), label = ">100 High")
        }
    }
}

@Composable
internal fun SleepDetailView(data: List<HealthMetric>, sleepGoal: Double = 8.0) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxHours = data.maxOf { it.value }.coerceAtLeast(sleepGoal)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.forEach { metric ->
            val fraction = (metric.value / maxHours).toFloat().coerceIn(0f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(600)
            )
            val color = sleepColor(metric.value)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dayLabel(metric.date)} ${metric.date.day}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(52.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(color)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatSleepDuration(metric.value),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary
        val weeklyAvg = data.sumOf { it.value } / data.size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Weekly Avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatSleepDuration(weeklyAvg),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sleepGoal.toInt()}h goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (weeklyAvg >= sleepGoal) "Meeting goal" else "Below goal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (weeklyAvg >= sleepGoal) Color(0xFF66BB6A) else Color(0xFFFFA726)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Color legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZoneDot(color = Color(0xFFEF5350), label = "<6h")
            ZoneDot(color = Color(0xFFFFA726), label = "6-7h")
            ZoneDot(color = Color(0xFF66BB6A), label = "7-9h")
            ZoneDot(color = Color(0xFF42A5F5), label = ">9h")
        }
    }
}
