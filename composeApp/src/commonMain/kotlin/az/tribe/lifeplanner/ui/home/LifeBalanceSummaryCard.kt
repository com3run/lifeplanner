package az.tribe.lifeplanner.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.ui.balance.LifeBalanceViewModel
import az.tribe.lifeplanner.ui.balance.getAreaColor
import az.tribe.lifeplanner.ui.components.GlassCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LifeBalanceSummaryCard(
    onViewFullReport: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LifeBalanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Life Balance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.report != null) {
                        Text(
                            "Overall ${uiState.report!!.overallScore}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    "See report →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onViewFullReport)
                )
            }

            Spacer(Modifier.height(14.dp))

            when {
                uiState.isLoading && uiState.report == null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                uiState.report != null -> {
                    AreaBars(
                        areaScores = uiState.report!!.areaScores,
                        isDark = isDark
                    )
                }
                uiState.error != null -> {
                    Text(
                        "Tap 'See report' to analyse your balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                else -> {
                    Text(
                        "Analysing your life areas…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaBars(areaScores: List<LifeAreaScore>, isDark: Boolean) {
    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animFraction.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    val fraction = animFraction.value

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        areaScores.forEach { areaScore ->
            val color = getAreaColor(areaScore.area, isDark)
            AreaBarRow(areaScore = areaScore, color = color, fraction = fraction)
        }
    }
}

@Composable
private fun AreaBarRow(areaScore: LifeAreaScore, color: Color, fraction: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji label
        Text(
            text = areaScore.area.icon,
            fontSize = 14.sp,
            modifier = Modifier.width(22.dp)
        )
        Spacer(Modifier.width(6.dp))
        // Area name
        Text(
            text = areaScore.area.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(88.dp),
            maxLines = 1
        )
        Spacer(Modifier.width(8.dp))
        // Bar track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (areaScore.score / 100f) * fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        // Score
        Text(
            text = "${areaScore.score}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(24.dp)
        )
    }
}
