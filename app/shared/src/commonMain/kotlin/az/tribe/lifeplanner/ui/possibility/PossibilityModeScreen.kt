package az.tribe.lifeplanner.ui.possibility

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Possibility
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.StateView
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ChatCircleText
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Scales
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Pillar 6 — Possibility Mode. The app's one divergent surface: when a goal is stuck, widen the
 * options (AI expands, never decides), then converge by picking the ones worth trying and turning
 * them into a goal, a step, or a logged decision.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PossibilityModeScreen(
    goalId: String,
    onBackClick: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onOpenDecision: (String) -> Unit,
    onTalkToCoach: (String, String) -> Unit,
    viewModel: PossibilityModeViewModel = koinViewModel { parametersOf(goalId) },
) {
    val goal by viewModel.goal.collectAsState()
    val possibilities by viewModel.possibilities.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isEnhancing by viewModel.isEnhancing.collectAsState()
    val error by viewModel.error.collectAsState()
    val nav by viewModel.nav.collectAsState()
    val c = MaterialTheme.modernColors

    // Actions move the user somewhere real instead of just flashing a toast.
    androidx.compose.runtime.LaunchedEffect(nav) {
        when (val n = nav) {
            is PossibilityNav.OpenGoal -> onOpenGoal(n.goalId)
            is PossibilityNav.OpenDecision -> onOpenDecision(n.decisionId)
            is PossibilityNav.TalkToCoach -> onTalkToCoach(n.coachId, n.message)
            PossibilityNav.Back -> onBackClick()
            null -> Unit
        }
        if (nav != null) viewModel.consumeNav()
    }

    val selected = remember(possibilities, selectedIds) { possibilities.filter { it.id in selectedIds } }

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Possibility Mode", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.xs,
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            // The goal's own name used to be shouted from a gradient poster here. Paper rules,
            // same as the detail screens: overline, a title that wraps, one quiet line of intent.
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "WHEN YOU'RE STUCK",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = c.primary,
                    )
                    Text(
                        goal?.title ?: "Get unstuck",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                    Text(
                        "Widen the options, then pick what to try or talk it through.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }

            when {
                error != null && possibilities.isEmpty() -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                        StateView(title = "Nothing yet", message = error ?: "", modifier = Modifier.fillMaxWidth())
                        AppButton(text = "Try again", onClick = viewModel::generate, modifier = Modifier.fillMaxWidth())
                    }
                }
                else -> {
                    if (isEnhancing) item { GeneratingRow() }
                    item {
                        Text(
                            "Pick the ones worth trying",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = c.textPrimary,
                            modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                        )
                    }
                    if (!isEnhancing && possibilities.isNotEmpty() && possibilities.all { it.isLocal }) {
                        item {
                            Text(
                                "AI could not be reached just now, so these were drawn from the goal itself.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textTertiary,
                            )
                        }
                    }
                    items(possibilities, key = { it.id }) { p ->
                        PossibilityCard(p, selected = p.id in selectedIds, onToggle = { viewModel.toggleSelect(p.id) })
                    }
                    if (possibilities.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                                if (selected.isNotEmpty()) {
                                    Text(
                                        "${selected.size} selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textSecondary,
                                        modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                                    )
                                }
                                // The coach hand-off is always available: pick a few first for context,
                                // or just talk it through. Chat opens and the persona reacts to this goal.
                                AppButton(
                                    text = "Talk it through with your coach",
                                    onClick = viewModel::talkToCoach,
                                    variant = AppButtonVariant.PRIMARY,
                                    leadingIcon = PhosphorIcons.Regular.ChatCircleText,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                if (selected.isNotEmpty()) {
                                    AppButton(
                                        text = if (selected.size == 1) "Add as a step to this goal" else "Add as steps to this goal",
                                        onClick = viewModel::addStepsFromSelection,
                                        variant = AppButtonVariant.SECONDARY,
                                        leadingIcon = PhosphorIcons.Regular.Plus,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    AppButton(
                                        text = if (selected.size == 1) "Make it a goal" else "Make ${selected.size} new goals",
                                        onClick = viewModel::makeGoalsFromSelection,
                                        variant = AppButtonVariant.SECONDARY,
                                        leadingIcon = PhosphorIcons.Regular.Flag,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    AppButton(
                                        text = "Log as a decision",
                                        onClick = viewModel::logSelectionAsDecision,
                                        variant = AppButtonVariant.TERTIARY,
                                        leadingIcon = PhosphorIcons.Regular.Scales,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingRow() {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = c.primary)
            Text("Finding more ideas for you...", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
}

// Internal so the JVM preview harness (PreviewScreenshots) can render it with fixture data.
@Composable
internal fun PossibilityCard(p: Possibility, selected: Boolean, onToggle: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onToggle),
        color = if (selected) c.primary.copy(alpha = 0.10f) else c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = if (selected) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
                contentDescription = if (selected) "Selected" else "Select",
                tint = if (selected) c.primary else c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.medium),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    p.permutation.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = c.secondary,
                )
                Text(p.text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
                if (p.rationale.isNotBlank()) {
                    Text(p.rationale, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
            }
        }
    }
}
