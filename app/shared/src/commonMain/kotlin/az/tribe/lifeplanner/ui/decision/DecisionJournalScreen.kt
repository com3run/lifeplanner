package az.tribe.lifeplanner.ui.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pillar 3, Decision Journal. Surfaces any pending [ChoicePoint]s at the top (tap to
 * re-choose via [ChoicePointBottomSheet]), then lists logged [Decision]s with reasoning
 * and outcome status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionJournalScreen(
    onBackClick: () -> Unit,
    onDecisionClick: (String) -> Unit,
    viewModel: DecisionViewModel = koinViewModel(),
) {
    val decisions by viewModel.decisions.collectAsState()
    val choicePoints by viewModel.choicePoints.collectAsState()
    val pendingDecisions by viewModel.pendingDecisions.collectAsState()
    var activeChoicePoint by remember { mutableStateOf<ChoicePoint?>(null) }
    var activePendingDecision by remember { mutableStateOf<Decision?>(null) }

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Decisions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (choicePoints.isNotEmpty()) {
                item { SectionHeader("Needs a decision") }
                items(choicePoints, key = { it.trigger.name + (it.relatedGoalId ?: it.relatedHabitId ?: it.title) }) { cp ->
                    ChoicePointCard(cp, onClick = { activeChoicePoint = cp })
                }
            }

            if (pendingDecisions.isNotEmpty()) {
                item { SectionHeader("From your journal") }
                items(pendingDecisions, key = { it.id }) { d ->
                    PendingDecisionCard(d, onClick = { activePendingDecision = d })
                }
            }

            item { SectionHeader("Decision journal") }
            if (decisions.isEmpty()) {
                item {
                    Text(
                        "Decisions you make at choice points will show up here, what you chose, and why.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.modernColors.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(decisions, key = { it.id }) { d -> DecisionCard(d, onClick = { onDecisionClick(d.id) }) }
            }
        }
    }

    activeChoicePoint?.let { cp ->
        ChoicePointBottomSheet(
            choicePoint = cp,
            onResolve = { action, reasoning -> viewModel.resolve(cp, action, reasoning) },
            onDismiss = { activeChoicePoint = null }
        )
    }

    activePendingDecision?.let { d ->
        PendingDecisionSheet(
            decision = d,
            onConfirm = { chosenOption, reasoning -> viewModel.confirm(d, chosenOption, reasoning) },
            onDismissDecision = { viewModel.dismiss(d) },
            onDismiss = { activePendingDecision = null }
        )
    }
}

@Composable
private fun PendingDecisionCard(d: Decision, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = MaterialTheme.modernColors.secondaryContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(d.question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.onSecondaryContainer, maxLines = 2)
            Text("Tap to log or dismiss", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.onSecondaryContainer)
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
private fun ChoicePointCard(cp: ChoicePoint, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = MaterialTheme.modernColors.warningContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(cp.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.onWarningContainer)
            Text(cp.prompt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.onWarningContainer)
        }
    }
}

@Composable
private fun DecisionCard(d: Decision, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = MaterialTheme.modernColors.cardBackground,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(d.question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary, maxLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.modernColors.primaryContainer) {
                    Text(
                        d.chosenOption,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.modernColors.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                val status = if (d.outcomeQuality != null) "Reviewed" else "Awaiting outcome"
                Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
            }
            if (d.reasoning.isNotBlank()) {
                Text(d.reasoning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.textSecondary, maxLines = 2)
            }
        }
    }
}
