package az.tribe.lifeplanner.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.InsightPriority
import az.tribe.lifeplanner.ui.balance.InsightCard
import az.tribe.lifeplanner.ui.balance.LifeBalanceSegmentBar
import az.tribe.lifeplanner.ui.balance.LifeBalanceViewModel
import az.tribe.lifeplanner.ui.balance.RecommendationCard
import az.tribe.lifeplanner.ui.components.GlassCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LifeBalanceSummaryCard(
    onViewFullReport: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToAddHabit: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LifeBalanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Section header ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Life Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Full report →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onViewFullReport)
            )
        }

        // ── Life Spectrum segment bar ─────────────────────────────────
        when {
            uiState.isLoading && uiState.report == null -> {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
            uiState.report != null -> {
                LifeBalanceSegmentBar(
                    areaScores = uiState.report!!.areaScores,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Tap 'Full report' to analyse your balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        val report = uiState.report ?: return@Column

        // ── Top HIGH insight ─────────────────────────────────────────
        val topInsight = report.aiInsights
            .filter { it.priority == InsightPriority.HIGH }
            .firstOrNull()
            ?: report.aiInsights.firstOrNull()

        if (topInsight != null) {
            Text(
                "Key Insight",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            InsightCard(
                insight = topInsight,
                onGetAdvice = { onNavigateToChat() }
            )
        }

        // ── Top recommendation ────────────────────────────────────────
        val topRec = report.recommendations.firstOrNull()
        if (topRec != null) {
            Text(
                "Suggested Action",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RecommendationCard(
                recommendation = topRec,
                isPreGenerating = uiState.isPreGenerating,
                isCreated = uiState.createdGoalIds.contains(topRec.targetArea.name),
                onCreateGoal = { viewModel.createGoalFromRecommendation(topRec) },
                onCreateHabit = { onNavigateToAddHabit() }
            )
        }
    }
}

