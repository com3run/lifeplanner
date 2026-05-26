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
import az.tribe.lifeplanner.domain.model.Possibility
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.StateView
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
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
    viewModel: PossibilityModeViewModel = koinViewModel { parametersOf(goalId) },
) {
    val goal by viewModel.goal.collectAsState()
    val possibilities by viewModel.possibilities.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionDone by viewModel.actionDone.collectAsState()
    val c = MaterialTheme.modernColors
    val snackbar = remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(actionDone) {
        actionDone?.let {
            snackbar.showSnackbar(it)
            viewModel.clearActionDone()
        }
    }

    val selected = remember(possibilities, selectedIds) { possibilities.filter { it.id in selectedIds } }

    Scaffold(
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbar) },
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
            item {
                GradientHero(
                    eyebrow = "WHEN YOU'RE STUCK",
                    title = "Possibility Mode",
                    subtitle = goal?.let { "Widen the options for \"${it.title}\"" }
                        ?: "Generate many options, then choose.",
                )
            }

            when {
                isGenerating -> item { GeneratingRow() }
                error != null -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                        StateView(title = "Nothing yet", message = error ?: "", modifier = Modifier.fillMaxWidth())
                        AppButton(text = "Try again", onClick = viewModel::generate, modifier = Modifier.fillMaxWidth())
                    }
                }
                else -> {
                    item {
                        Text(
                            "Pick the ones worth trying",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = c.textPrimary,
                            modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                        )
                    }
                    items(possibilities, key = { it.id }) { p ->
                        PossibilityCard(p, selected = p.id in selectedIds, onToggle = { viewModel.toggleSelect(p.id) })
                    }
                    if (selected.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                                Text(
                                    "${selected.size} selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary,
                                    modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                                )
                                AppButton(
                                    text = if (selected.size == 1) "Make it a goal" else "Make ${selected.size} new goals",
                                    onClick = { selected.forEach(viewModel::makeGoal) },
                                    variant = AppButtonVariant.PRIMARY,
                                    leadingIcon = PhosphorIcons.Regular.Flag,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                AppButton(
                                    text = "Add as steps to this goal",
                                    onClick = { selected.forEach(viewModel::addStep) },
                                    variant = AppButtonVariant.SECONDARY,
                                    leadingIcon = PhosphorIcons.Regular.Plus,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                AppButton(
                                    text = "Log as a decision",
                                    onClick = viewModel::logAsDecision,
                                    variant = AppButtonVariant.SECONDARY,
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
            Text("Widening the options...", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
}

@Composable
private fun PossibilityCard(p: Possibility, selected: Boolean, onToggle: () -> Unit) {
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
