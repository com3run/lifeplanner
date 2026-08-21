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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.domain.service.DecisionScorecard
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.ui.components.InlineEmptyState
import az.tribe.lifeplanner.ui.theme.modernColors
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.illus_empty_decisions
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
    reviewViewModel: MetacognitiveReviewViewModel = koinViewModel(),
) {
    val decisions by viewModel.decisions.collectAsState()
    val scorecard by viewModel.scorecard.collectAsState()
    val choicePoints by viewModel.choicePoints.collectAsState()
    val pendingDecisions by viewModel.pendingDecisions.collectAsState()
    var activeChoicePoint by remember { mutableStateOf<ChoicePoint?>(null) }
    var activePendingDecision by remember { mutableStateOf<Decision?>(null) }

    // Reviewing your reasoning is part of keeping a decision journal, not a separate destination,
    // so it lives here as a tab instead of its own row on the You page.
    var currentTab by rememberSaveable { mutableStateOf(0) }
    val toReview = decisions.filter { it.outcomeQuality == null }

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
            item {
                DecisionTabRow(
                    selectedTab = currentTab,
                    reviewCount = toReview.size,
                    onTabSelected = { currentTab = it },
                )
            }

            // The track record belongs on the log, not on the review tab: the review tab is where
            // you answer for one call, this is where you see what all of them add up to.
            if (currentTab == 0) {
                item { ScorecardCard(scorecard) }
            }

            if (currentTab == 1) {
                item {
                    Text(
                        "Grade your reasoning, not the outcome. A sound decision can turn out badly " +
                            "(and a flawed one can get lucky), what matters long-term is the process.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.modernColors.textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (toReview.isEmpty()) {
                    item {
                        Text(
                            if (decisions.isEmpty())
                                "No decisions logged yet. Resolve a Choice Point and it'll appear here to review."
                            else
                                "Every decision is reviewed. Nothing waiting on you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.modernColors.textSecondary
                        )
                    }
                } else {
                    items(toReview, key = { it.id }) { d ->
                        DecisionGradeCard(d, onGrade = { q -> reviewViewModel.grade(d, q) })
                    }
                }
            } else {
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
                        InlineEmptyState(
                            illustration = Res.drawable.illus_empty_decisions,
                            title = "No decisions yet",
                            subtitle = "Choices you make at choice points land here, what you picked and why.",
                        )
                    }
                } else {
                    items(decisions, key = { it.id }) { d -> DecisionCard(d, onClick = { onDecisionClick(d.id) }) }
                }
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

/** Pill tabs matching the Artifact hub's row. The count makes the review backlog visible up front. */
@Composable
private fun DecisionTabRow(selectedTab: Int, reviewCount: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Journal", if (reviewCount > 0) "Review · $reviewCount" else "Review")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * The track record. Process hit rate leads, because that is the score worth chasing: it credits a
 * sound call that went badly and refuses to credit a lucky one. Calibration sits underneath as the
 * honest mirror, and both stay hidden until something has actually been reviewed rather than
 * showing a demoralising 0%.
 */
@Composable
internal fun ScorecardCard(card: DecisionScorecard) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
        color = c.primaryContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "TRACK RECORD",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = c.onPrimaryContainer
            )

            val hitRate = card.processHitRate
            if (hitRate == null) {
                Text(
                    if (card.logged == 0) "No calls logged yet." else "Review a call to start your record.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onPrimaryContainer
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "$hitRate%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = c.onPrimaryContainer
                    )
                    Text(
                        "sound thinking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                card.calibrationGap?.let { gap ->
                    val verdict = when {
                        gap > 10 -> "overconfident by $gap points"
                        gap < -10 -> "you sell yourself short by ${-gap} points"
                        else -> "well calibrated"
                    }
                    Text(
                        "Said ${card.averageConfidence}% \u00b7 right ${card.actualSuccessRate}% \u00b7 $verdict",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onPrimaryContainer
                    )
                }
            }

            Text(
                buildString {
                    append("${card.logged} logged")
                    append(" \u00b7 ${card.reviewed} reviewed")
                    if (card.awaitingReview > 0) append(" \u00b7 ${card.awaitingReview} still owed an answer")
                },
                style = MaterialTheme.typography.bodySmall,
                color = c.onPrimaryContainer
            )
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
