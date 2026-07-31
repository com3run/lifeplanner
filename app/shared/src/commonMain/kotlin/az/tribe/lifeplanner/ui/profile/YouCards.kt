package az.tribe.lifeplanner.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.ProgressRing
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.LifePlannerGradients
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretRight

/**
 * One thing asking for the user's attention. Deliberately loud relative to the rest of the tab, and
 * absent entirely when nothing qualifies, so its presence is the signal.
 */
@Composable
internal fun AttentionCard(item: YouAttention, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onClick),
        color = c.warningContainer,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.standard),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = c.onWarningContainer,
                )
                Text(item.body, style = MaterialTheme.typography.bodySmall, color = c.onWarningContainer)
                Text(
                    item.actionLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = c.onWarningContainer,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                PhosphorIcons.Regular.CaretRight,
                contentDescription = null,
                tint = c.onWarningContainer,
                modifier = Modifier.size(LifePlannerDesign.IconSize.small),
            )
        }
    }
}

/**
 * Life balance as an answer rather than a link: the overall score in a ring, who's carrying it, and
 * who isn't. Tapping opens the full report.
 */
@Composable
internal fun BalanceGlanceCard(
    overall: Int,
    areaScores: List<LifeAreaScore>,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val strongest = areaScores.maxByOrNull { it.score }
    val weakest = areaScores.minByOrNull { it.score }

    GlassCard(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onClick),
        cornerRadius = LifePlannerDesign.CornerRadius.large,
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(4.dp).background(LifePlannerGradients.primary))
            Column(Modifier.padding(LifePlannerDesign.Padding.standard)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProgressRing(
                        progress = overall / 100f,
                        diameter = 62.dp,
                        strokeWidth = 6.dp,
                        color = c.primary,
                    ) {
                        Text(
                            "$overall",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Life balance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        if (strongest != null) {
                            Text(
                                "${strongest.area.icon} ${strongest.area.displayName} is carrying it at ${strongest.score}",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                            )
                        }
                        if (weakest != null && weakest.area != strongest?.area) {
                            Text(
                                "${weakest.area.icon} ${weakest.area.displayName} is lowest at ${weakest.score}",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                            )
                        }
                    }
                    Icon(
                        PhosphorIcons.Regular.CaretRight,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(LifePlannerDesign.IconSize.small),
                    )
                }

                if (areaScores.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        areaScores.forEach { area ->
                            AreaSpark(area, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/** One area as a labelled bar, so the shape of the whole life reads at a glance. */
@Composable
private fun AreaSpark(area: LifeAreaScore, modifier: Modifier = Modifier) {
    val c = MaterialTheme.modernColors
    val tint = when {
        area.score >= 70 -> c.success
        area.score >= 40 -> c.primary
        else -> c.warning
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(c.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxWidth((area.score / 100f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(area.area.icon, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * A live finding stated in plain language. When there isn't enough data yet it degrades to an
 * honest invitation rather than disappearing, so the feature stays discoverable.
 */
@Composable
internal fun FindingCard(
    icon: ImageVector,
    accent: Color,
    headline: String,
    detail: String,
    footnote: String? = null,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onClick),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.standard),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    headline,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                )
                Text(detail, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                if (footnote != null) {
                    Text(
                        footnote,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Icon(
                PhosphorIcons.Regular.CaretRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.small),
            )
        }
    }
}
