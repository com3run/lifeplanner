package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.BalanceTrend
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Life balance, on the front door. One row per area with a filled bar, the overall score, and a
 * line naming whichever area is furthest behind.
 *
 * This replaces the separate Life Balance screen. That screen held its numbers behind a tap most
 * people never made, so the signal it produced never reached anyone; here it sits in the feed the
 * user already reads. Renders nothing until there is a report to show.
 */
@Composable
fun BalanceStripCard(report: LifeBalanceReport?, modifier: Modifier = Modifier) {
    if (report == null || report.areaScores.isEmpty()) return
    val c = MaterialTheme.modernColors
    val weakest = report.areaScores.minByOrNull { it.score }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "BALANCE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    letterSpacing = 1.2.sp,
                    color = c.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${report.overallScore}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = scoreColor(report.overallScore, c.primary),
                )
            }

            report.areaScores.sortedBy { it.score }.forEach { area ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        area.area.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.width(72.dp),
                        maxLines = 1,
                    )
                    val fill by animateFloatAsState(
                        targetValue = (area.score / 100f).coerceIn(0f, 1f),
                        label = "balanceBar_${area.area.name}",
                    )
                    Box(
                        Modifier.weight(1f).height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(c.primary.copy(alpha = 0.10f)),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(fill).height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(scoreColor(area.score, c.primary)),
                        )
                    }
                    Spacer(Modifier.width(LifePlannerDesign.Spacing.xs))
                    Text(
                        "${area.score}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.textSecondary,
                        modifier = Modifier.width(24.dp),
                    )
                }
            }

            if (weakest != null) {
                Text(
                    weakestLine(weakest.area.displayName, weakest.score, weakest.trend),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
            }
        }
    }
}

/** Red below 40, amber below 70, otherwise the app's own accent. */
private fun scoreColor(score: Int, accent: Color): Color = when {
    score < 40 -> Color(0xFFE85D5D)
    score < 70 -> Color(0xFFE8A13A)
    else -> accent
}

private fun weakestLine(area: String, score: Int, trend: BalanceTrend): String = when {
    trend == BalanceTrend.IMPROVING -> "$area is your lowest at $score, but it's climbing."
    trend == BalanceTrend.DECLINING -> "$area is slipping at $score. Worth a look."
    else -> "$area is your lowest at $score."
}
