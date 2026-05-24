package az.tribe.lifeplanner.ui.balance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.ui.components.GlassCard
import kotlin.math.abs

@Composable
fun LifeBalanceSegmentBar(
    areaScores: List<LifeAreaScore>,
    modifier: Modifier = Modifier
) {
    if (areaScores.isEmpty()) return
    val isDark = true

    // Highest score in the center, lowest on the edges
    val scoreSorted = areaScores.sortedByDescending { it.score }
    val arranged = centerOutArrange(scoreSorted)

    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animFraction.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val fraction by animFraction.asState()

    // Center segment and its position relative to the geometric center
    val centerIndex = arranged.size / 2
    val centerArea = arranged[centerIndex]
    val centerColor = getAreaColor(centerArea.area, isDark)

    val totalWeight = arranged.sumOf { segmentWeight(it.score, fraction).toDouble() }.toFloat()
    var leftWeight = 0f
    for (i in 0 until centerIndex) leftWeight += segmentWeight(arranged[i].score, fraction)
    leftWeight += segmentWeight(arranged[centerIndex].score, fraction) / 2f
    val arrowFraction = if (totalWeight > 0f) leftWeight / totalWeight else 0.5f
    val offsetFromCenter = arrowFraction - 0.5f     // positive → right of balance, negative → left
    val offsetPercent = (abs(offsetFromCenter) * 100).toInt()

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Life Spectrum",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (offsetPercent < 6) "balanced center"
                    else "${centerArea.area.displayName} · ${offsetPercent}% ${if (offsetFromCenter > 0) "right" else "left"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (offsetPercent < 6) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    else centerColor.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Area emoji row, aligned above each segment
            Row(modifier = Modifier.fillMaxWidth()) {
                arranged.forEach { areaScore ->
                    Box(
                        modifier = Modifier.weight(segmentWeight(areaScore.score, fraction)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            areaScore.area.icon,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(5.dp))

            // Segment bar with score text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    arranged.forEach { areaScore ->
                        val color = getAreaColor(areaScore.area, isDark)
                        Box(
                            modifier = Modifier
                                .weight(segmentWeight(areaScore.score, fraction))
                                .fillMaxHeight()
                                .background(color.copy(alpha = 0.88f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(areaScore.score * fraction).toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White.copy(alpha = 0.9f),
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            // Center indicator, arrow at highest-score segment, reference tick at 50%
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(22.dp)
            ) {
                val barWidth = maxWidth

                // 50% reference tick (geometric center of balance)
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(7.dp)
                        .offset(x = barWidth / 2 - 0.75.dp)
                        .align(Alignment.TopStart)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
                            RoundedCornerShape(1.dp)
                        )
                )

                // Arrow pointing up at center-segment position
                Text(
                    text = "▲",
                    modifier = Modifier
                        .offset(x = barWidth * arrowFraction - 5.dp)
                        .align(Alignment.TopStart),
                    fontSize = 9.sp,
                    color = centerColor.copy(alpha = 0.85f * fraction)
                )

                // Label, centered under arrow
                Text(
                    text = centerArea.area.displayName,
                    modifier = Modifier
                        .offset(x = (barWidth * arrowFraction - 20.dp).coerceIn(0.dp, barWidth - 40.dp))
                        .align(Alignment.BottomStart),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = centerColor.copy(alpha = 0.65f * fraction),
                    softWrap = false
                )
            }

            Spacer(Modifier.height(2.dp))

            // Area name row, aligned below each segment
            Row(modifier = Modifier.fillMaxWidth()) {
                arranged.forEach { areaScore ->
                    val color = getAreaColor(areaScore.area, isDark)
                    Text(
                        text = areaScore.area.displayName,
                        modifier = Modifier.weight(segmentWeight(areaScore.score, fraction)),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = color.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// Segments animate from equal width (fraction=0) to score-proportional (fraction=1)
private fun segmentWeight(score: Int, fraction: Float): Float =
    (score.toFloat() * fraction).coerceAtLeast(MIN_WEIGHT) + (MIN_WEIGHT * (1f - fraction))

private const val MIN_WEIGHT = 8f

// Spread highest-score areas to center, lowest to edges
// For sorted = [s1,s2,s3,s4,s5,s6]: result = [s6,s4,s2,s1,s3,s5]
private fun centerOutArrange(sorted: List<LifeAreaScore>): List<LifeAreaScore> {
    val n = sorted.size
    val result = arrayOfNulls<LifeAreaScore>(n)
    sorted.forEachIndexed { rank, item ->
        val pos = if (rank % 2 == 0) n / 2 + rank / 2 else n / 2 - 1 - rank / 2
        result[pos] = item
    }
    return result.filterNotNull()
}
