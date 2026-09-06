package az.tribe.lifeplanner.ui.causal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CausalInsight
import az.tribe.lifeplanner.domain.model.InsightConfidence
import az.tribe.lifeplanner.domain.model.InsightKind
import az.tribe.lifeplanner.domain.service.Calibration
import az.tribe.lifeplanner.ui.theme.modernColors
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.illus_state_waiting
import org.jetbrains.compose.resources.painterResource
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back

/**
 * Pillar 4, Causal Insights: the user's own causal model, computed on-device. Shows the
 * predicted-vs-actual calibration stat, correlations, and amplification-spiral warnings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CausalInsightsScreen(
    onBackClick: () -> Unit,
    viewModel: CausalInsightsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.isPremium -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                UpsellCard()
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 84.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.calibration?.let { item { CalibrationCard(it) } }

                if (state.insights.isEmpty() && state.calibration == null) {
                    item { EmptyState() }
                } else if (state.insights.isNotEmpty()) {
                    item { SectionHeader("What your data shows") }
                    items(state.insights, key = { it.statement }) { InsightCard(it) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.modernColors.textSecondary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun CalibrationCard(c: Calibration) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.modernColors.primaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "ESTIMATION CALIBRATION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.modernColors.onPrimaryContainer
            )
            Text(
                c.statement,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.modernColors.onPrimaryContainer
            )
            Text(
                "based on ${c.sampleSize} completed goal${if (c.sampleSize == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.modernColors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InsightCard(insight: CausalInsight) {
    val isSpiral = insight.kind == InsightKind.AMPLIFICATION_SPIRAL
    val bg = if (isSpiral) MaterialTheme.modernColors.warningContainer else MaterialTheme.modernColors.cardBackground
    val fg = if (isSpiral) MaterialTheme.modernColors.onWarningContainer else MaterialTheme.modernColors.textPrimary
    Surface(modifier = Modifier.fillMaxWidth(), color = bg, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(insight.statement, style = MaterialTheme.typography.bodyLarge, color = fg)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ConfidenceChip(insight.confidence)
                Text(
                    "${insight.sampleSize} data points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.modernColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ConfidenceChip(confidence: InsightConfidence) {
    val (label, color) = when (confidence) {
        InsightConfidence.HIGH -> "High confidence" to MaterialTheme.modernColors.success
        InsightConfidence.MODERATE -> "Moderate" to MaterialTheme.modernColors.primary
        InsightConfidence.LOW -> "Low, early signal" to MaterialTheme.modernColors.textSecondary
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.18f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.illus_state_waiting),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
        Text(
            "Not enough history yet. Keep logging habits, mood, focus, and sleep, once there's " +
                "enough, your personal patterns and estimation calibration show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.modernColors.textSecondary
        )
    }
}

@Composable
private fun UpsellCard() {
    Surface(color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Causal Insights is a premium feature",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.modernColors.textPrimary
            )
            Text(
                "Discover what actually drives your progress, correlations across sleep, mood, " +
                    "focus and habits, plus how accurate your time estimates are. All computed " +
                    "privately on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.modernColors.textSecondary
            )
        }
    }
}
