package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.BalanceInsight
import az.tribe.lifeplanner.domain.model.BalanceRating
import az.tribe.lifeplanner.domain.model.BalanceRecommendation
import az.tribe.lifeplanner.domain.model.BalanceRecommendationAction
import az.tribe.lifeplanner.domain.model.InsightPriority
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle

// ─── Hero Score Card ──────────────────────────────────────────────────────────

@Composable
internal fun HeroScoreCard(report: LifeBalanceReport) {
    val gradientColors = when {
        report.overallScore >= 70 -> listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
        report.overallScore >= 50 -> listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1E88E5))
        report.overallScore >= 30 -> listOf(Color(0xFFBF360C), Color(0xFFE64A19), Color(0xFFFF7043))
        else -> listOf(Color(0xFF7B1FA2), Color(0xFF8E24AA), Color(0xFFAB47BC))
    }

    val ratingColor = when (report.balanceRating) {
        BalanceRating.EXCELLENT -> Color(0xFF4CAF50)
        BalanceRating.GOOD -> Color(0xFF8BC34A)
        BalanceRating.MODERATE -> Color(0xFFFFC107)
        BalanceRating.NEEDS_ATTENTION -> Color(0xFFFF9800)
        BalanceRating.CRITICAL -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.04f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .background(Color.White.copy(alpha = 0.04f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "YOUR LIFE SCORE",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${report.overallScore}",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 80.sp
                )
                Text(
                    " /100",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            val isDark = true
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                report.areaScores.forEach { areaScore ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        HeartProgress(
                            score = areaScore.score,
                            color = getAreaColor(areaScore.area, isDark = false),
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            areaScore.area.displayName.take(3),
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ratingColor.copy(alpha = 0.25f)
            ) {
                Text(
                    report.balanceRating.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                report.balanceRating.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Insight Card ─────────────────────────────────────────────────────────────

@Composable
internal fun InsightCard(
    insight: BalanceInsight,
    onGetAdvice: (BalanceInsight) -> Unit
) {
    val isDark = true

    // Vivid accent colors for the left bar, always punchy regardless of theme
    val priorityBarColor = when (insight.priority) {
        InsightPriority.HIGH -> Color(0xFFF44336)
        InsightPriority.MEDIUM -> Color(0xFFFFC107)
        InsightPriority.LOW -> Color(0xFF4CAF50)
    }
    // Accessible colors for chip text and chip background in dark mode (WCAG AA ≥ 4.5:1)
    val priorityColor = when (insight.priority) {
        InsightPriority.HIGH -> if (isDark) Color(0xFFEF9A9A) else Color(0xFFF44336)
        InsightPriority.MEDIUM -> if (isDark) Color(0xFFFFE082) else Color(0xFFFFC107)
        InsightPriority.LOW -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
    }

    val priorityLabel = when (insight.priority) {
        InsightPriority.HIGH -> "HIGH"
        InsightPriority.MEDIUM -> "MEDIUM"
        InsightPriority.LOW -> "LOW"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left priority accent bar, always vivid for maximum signalling
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        priorityBarColor,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        insight.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                    ) {
                        Text(
                            priorityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (insight.relatedAreas.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        insight.relatedAreas.forEach { area ->
                            val chipColor = getAreaColor(area, isDark)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = chipColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                            ) {
                                Text(
                                    "${area.icon} ${area.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = chipColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onGetAdvice(insight) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Sparkle,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("Ask a Coach", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ─── Recommendation Card ──────────────────────────────────────────────────────

@Composable
internal fun RecommendationCard(
    recommendation: BalanceRecommendation,
    isPreGenerating: Boolean,
    isCreated: Boolean,
    onCreateGoal: () -> Unit,
    onCreateHabit: () -> Unit
) {
    val isDark = true
    val areaColor = getAreaColor(recommendation.targetArea, isDark)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = areaColor.copy(alpha = if (isDark) 0.14f else 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(areaColor.copy(alpha = if (isDark) 0.28f else 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(recommendation.targetArea.icon, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when (recommendation.actionType) {
                BalanceRecommendationAction.CREATE_GOAL -> {
                    val goal = recommendation.preGeneratedGoal

                    if (goal != null) {
                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    goal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    goal.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (goal.milestones.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    PhosphorIcons.Regular.Flag,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(11.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    "${goal.milestones.size} milestones",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            when (goal.timeline) {
                                                az.tribe.lifeplanner.domain.enum.GoalTimeline.SHORT_TERM -> "30 days"
                                                az.tribe.lifeplanner.domain.enum.GoalTimeline.MID_TERM -> "90 days"
                                                az.tribe.lifeplanner.domain.enum.GoalTimeline.LONG_TERM -> "1 year"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = onCreateGoal,
                            enabled = !isCreated,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (isCreated) {
                                ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                                    disabledContentColor = Color(0xFF4CAF50)
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            if (isCreated) {
                                Icon(PhosphorIcons.Regular.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Added to Goals")
                            } else {
                                Icon(PhosphorIcons.Regular.Plus, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add to My Goals")
                            }
                        }

                    } else if (isPreGenerating) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Crafting a smart goal for you...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (recommendation.suggestedGoal != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PhosphorIcons.Regular.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                recommendation.suggestedGoal,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                BalanceRecommendationAction.CREATE_HABIT -> {
                    if (recommendation.suggestedHabit != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(PhosphorIcons.Regular.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    recommendation.suggestedHabit,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = onCreateHabit,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(PhosphorIcons.Regular.Plus, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Create", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
