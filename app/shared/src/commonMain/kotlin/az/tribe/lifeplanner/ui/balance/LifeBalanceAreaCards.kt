package az.tribe.lifeplanner.ui.balance

import az.tribe.lifeplanner.ui.theme.LocalIsDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.BalanceTrend
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Flag

// ─── Area Grid ────────────────────────────────────────────────────────────────

@Composable
internal fun AreaGrid(areaScores: List<LifeAreaScore>, modifier: Modifier = Modifier) {
    val sorted = areaScores.sortedByDescending { it.score }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sorted.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { areaScore ->
                    AreaGridCard(areaScore = areaScore, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun AreaGridCard(areaScore: LifeAreaScore, modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val color = getAreaColor(areaScore.area, isDark)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored top accent stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                    )
            )

            Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                // Mini arc ring, top-right
                HeartProgress(
                    score = areaScore.score,
                    color = color,
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.TopEnd)
                )

                Column(modifier = Modifier.fillMaxWidth().padding(end = 50.dp)) {
                    Text(areaScore.area.icon, fontSize = 26.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        areaScore.area.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${areaScore.score}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        lineHeight = 28.sp
                    )
                    val scoreLabel = when {
                        areaScore.score >= 70 -> "Good"
                        areaScore.score >= 50 -> "Fair"
                        areaScore.score >= 30 -> "Low"
                        else -> "Critical"
                    }
                    Text(
                        scoreLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${areaScore.goalCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowsClockwise,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${areaScore.habitCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (areaScore.trend != BalanceTrend.STABLE) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = if (areaScore.trend == BalanceTrend.IMPROVING)
                                    PhosphorIcons.Regular.CaretUp else PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = if (areaScore.trend == BalanceTrend.IMPROVING)
                                    Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HeartProgress(score: Int, color: Color, modifier: Modifier = Modifier) {
    val fillLevel by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "heart_fill"
    )
    Canvas(modifier = modifier) {
        val path = heartPath(size)
        drawPath(path, color.copy(alpha = 0.13f))
        clipPath(path) {
            val fillTop = size.height * (1f - fillLevel)
            drawRect(
                color = color,
                topLeft = Offset(0f, fillTop),
                size = Size(size.width, size.height - fillTop)
            )
        }
        drawPath(path, color, style = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round))
    }
}

private fun heartPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w / 2f, h * 0.27f)
        cubicTo(w / 2f - w * 0.15f, 0f, 0f, h * 0.08f, 0f, h * 0.38f)
        cubicTo(0f, h * 0.65f, w / 2f - w * 0.1f, h * 0.82f, w / 2f, h)
        cubicTo(w / 2f + w * 0.1f, h * 0.82f, w, h * 0.65f, w, h * 0.38f)
        cubicTo(w, h * 0.08f, w / 2f + w * 0.15f, 0f, w / 2f, h * 0.27f)
        close()
    }
}
