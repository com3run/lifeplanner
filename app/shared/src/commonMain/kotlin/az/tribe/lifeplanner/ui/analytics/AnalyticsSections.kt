package az.tribe.lifeplanner.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.GoalAnalytics
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Barbell
import com.adamglin.phosphoricons.regular.ChartLine
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Target
import com.adamglin.phosphoricons.regular.ThumbsUp
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.Trophy

@Composable
internal fun HeroStatsSection(analytics: GoalAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Trophy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Your Journey",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroStatCard(
                        title = "Total Goals",
                        value = analytics.totalGoals.toString(),
                        icon = PhosphorIcons.Regular.Flag,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                    HeroStatCard(
                        title = "Success Rate",
                        value = "${(analytics.completionRate * 100).toInt()}%",
                        icon = PhosphorIcons.Regular.TrendUp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProgressOverviewSection(analytics: GoalAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ChartLine,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Progress Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressStatCard(
                    title = "Active",
                    value = analytics.activeGoals.toString(),
                    icon = PhosphorIcons.Regular.Play,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                ProgressStatCard(
                    title = "Completed",
                    value = analytics.completedGoals.toString(),
                    icon = PhosphorIcons.Regular.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                ProgressStatCard(
                    title = "Due Soon",
                    value = analytics.upcomingDeadlines.toString(),
                    icon = PhosphorIcons.Regular.Clock,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun CategoryBreakdownSection(analytics: GoalAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.SquaresFour,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (analytics.goalsByCategory.isEmpty()) {
                EmptyStateCard(message = "No categories found")
            } else {
                analytics.goalsByCategory.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, count) ->
                        ModernCategoryItem(
                            category = category,
                            count = count,
                            total = analytics.totalGoals,
                            averageProgress = analytics.averageProgressPerCategory[category] ?: 0f
                        )
                    }
            }
        }
    }
}

@Composable
internal fun TimelineDistributionSection(analytics: GoalAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Clock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Timeline Distribution",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                analytics.goalsByTimeline.entries.forEach { (timeline, count) ->
                    ModernTimelineCard(
                        timeline = timeline,
                        count = count,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun PerformanceInsightsSection(analytics: GoalAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Smart Insights",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Most active category insight. Only when one category actually leads: with a goal
            // in each of three categories, "Career leads with 1 goals" is a tie dressed up as a
            // ranking (same tie rule as the wheel).
            analytics.goalsByCategory.maxByOrNull { it.value }
                ?.takeIf { top -> analytics.goalsByCategory.count { it.value == top.value } == 1 }
                ?.let { (category, count) ->
                    ModernInsightCard(
                        icon = PhosphorIcons.Regular.Trophy,
                        title = "Top Category",
                        description = "${category.name.lowercase().replaceFirstChar { it.uppercase() }} " +
                            "leads with $count ${if (count == 1) "goal" else "goals"}",
                        color = Color(0xFF10B981)
                    )
                }

            // Completion rate insight
            val completionPercentage = (analytics.completionRate * 100).toInt()
            val insightIcon = when {
                completionPercentage >= 80 -> PhosphorIcons.Regular.CheckCircle
                completionPercentage >= 60 -> PhosphorIcons.Regular.ThumbsUp
                completionPercentage >= 40 -> PhosphorIcons.Regular.Barbell
                else -> PhosphorIcons.Regular.Target
            }
            val insightTitle = when {
                completionPercentage >= 80 -> "Excellent!"
                completionPercentage >= 60 -> "Good Progress"
                completionPercentage >= 40 -> "Keep Going"
                else -> "Focus Time"
            }
            val insightDescription = when {
                completionPercentage >= 80 -> "Amazing $completionPercentage% completion rate!"
                completionPercentage >= 60 -> "$completionPercentage% completion rate - keep it up!"
                completionPercentage >= 40 -> "$completionPercentage% completion - you can do better!"
                else -> "Focus on completing existing goals first"
            }
            val insightColor = when {
                completionPercentage >= 80 -> Color(0xFF10B981)
                completionPercentage >= 60 -> Color(0xFF3B82F6)
                completionPercentage >= 40 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            ModernInsightCard(
                icon = insightIcon,
                title = insightTitle,
                description = insightDescription,
                color = insightColor
            )

            // Upcoming deadlines insight
            if (analytics.upcomingDeadlines > 0) {
                ModernInsightCard(
                    icon = PhosphorIcons.Regular.Clock,
                    title = "Due Soon",
                    description = "${analytics.upcomingDeadlines} goals need your attention soon",
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}
