package az.tribe.lifeplanner.ui.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.HealthMetric
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Minus
import com.adamglin.phosphoricons.bold.TrendDown
import com.adamglin.phosphoricons.bold.TrendUp
import kotlinx.datetime.number
import kotlin.math.roundToInt

@Composable
internal fun WeightDetailView(data: List<HealthMetric>) {
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

    val minWeight = data.minOf { it.value }
    val maxWeight = data.maxOf { it.value }
    val lineColor = Color(0xFF7E57C2)

    // Trend calculation
    val trendText = if (data.size >= 2) {
        val first = data.first().value
        val last = data.last().value
        val diff = last - first
        val rounded = (diff * 10).roundToInt() / 10.0
        when {
            rounded < -0.05 -> {
                val display = ((-rounded) * 10).roundToInt() / 10.0
                "-$display kg this week"
            }
            rounded > 0.05 -> {
                val display = (rounded * 10).roundToInt() / 10.0
                "+$display kg this week"
            }
            else -> "Stable this week"
        }
    } else {
        "Stable this week"
    }

    val trendIcon = if (data.size >= 2) {
        val diff = data.last().value - data.first().value
        when {
            diff < -0.05 -> PhosphorIcons.Bold.TrendDown
            diff > 0.05 -> PhosphorIcons.Bold.TrendUp
            else -> PhosphorIcons.Bold.Minus
        }
    } else {
        PhosphorIcons.Bold.Minus
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Trend header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                trendIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = lineColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trendText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = lineColor
            )
        }

        if (data.size == 1) {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                val w = data[0].value
                val display = (w * 10).roundToInt() / 10.0
                Text(
                    text = "$display kg",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = lineColor
                )
            }
        } else {
            val range = (maxWeight - minWeight).coerceAtLeast(0.1)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val paddingY = 16.dp.toPx()
                val chartHeight = size.height - paddingY * 2
                val stepX = size.width / (data.size - 1)

                fun yPos(value: Double): Float =
                    paddingY + chartHeight - ((value - minWeight) / range * chartHeight).toFloat()

                // Gradient fill
                val fillPath = Path().apply {
                    moveTo(0f, size.height)
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
                        colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.0f))
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

                // Dots
                data.forEachIndexed { index, metric ->
                    val x = index * stepX
                    val y = yPos(metric.value)
                    drawCircle(color = lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(x, y))
                }
            }
        }

        // X-axis day labels
        if (data.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { metric ->
                    Text(
                        text = "${metric.date.day}/${metric.date.month.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Min / Max
        val minDisplay = (minWeight * 10).roundToInt() / 10.0
        val maxDisplay = (maxWeight * 10).roundToInt() / 10.0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: $minDisplay kg",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Max: $maxDisplay kg",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
