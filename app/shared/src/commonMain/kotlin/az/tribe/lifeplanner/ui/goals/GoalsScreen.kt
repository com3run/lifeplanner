package az.tribe.lifeplanner.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.components.StateView
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.backgroundColor
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Plus
import org.koin.compose.viewmodel.koinViewModel

/**
 * D7 — the redesigned **Goals** canvas (D2): your commitments, active first, each laddered to a
 * reason. Premium gradient hero + per-category color accents, built on D3 tokens + D4 [AppButton].
 * The value/Why-Chain tag is a Pillar 1 seam (see [GoalsViewModel]); category is the tag for now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBackClick: () -> Unit,
    onNewGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val c = MaterialTheme.modernColors

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Goals", fontWeight = FontWeight.Bold) },
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
                    eyebrow = "Your commitments",
                    title = "Goals",
                    subtitle = summary(state),
                    trailing = {
                        IconButton(onClick = onNewGoal) {
                            Icon(PhosphorIcons.Regular.Plus, contentDescription = "New goal", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(LifePlannerDesign.IconSize.large))
                        }
                    },
                )
            }

            if (state.active.isEmpty() && state.completed.isEmpty()) {
                item {
                    StateView(
                        title = "Nothing here yet",
                        message = "A goal is something you're working toward — start with one that matters to you.",
                        icon = PhosphorIcons.Regular.Flag,
                        actionLabel = "Create your first goal",
                        onAction = onNewGoal,
                        modifier = Modifier.padding(top = LifePlannerDesign.Spacing.lg),
                    )
                }
            }

            if (state.active.isNotEmpty()) {
                item { SectionLabel("Active · ${state.active.size}") }
                items(state.active, key = { it.id }) { g -> GoalCard(g, onClick = { onOpenGoal(g.id) }) }
            }
            if (state.completed.isNotEmpty()) {
                item { SectionLabel("Completed · ${state.completed.size}") }
                items(state.completed, key = { it.id }) { g -> GoalCard(g, onClick = { onOpenGoal(g.id) }, dimmed = true) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.modernColors.textPrimary,
        modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
    )
}

@Composable
private fun GoalCard(goal: Goal, onClick: () -> Unit, dimmed: Boolean = false) {
    val c = MaterialTheme.modernColors
    val accent = goal.category.backgroundColor()
    val rate = goal.completionRate.coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onClick),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconChip(PhosphorIcons.Regular.Flag, tint = accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs)) {
                Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.category.displayName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = accent)
                    Text("· Due ${goal.dueDate}", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                }
                Text(
                    goal.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (dimmed) c.textTertiary else c.textPrimary,
                    maxLines = 2,
                )
                Box(
                    Modifier.fillMaxWidth().height(LifePlannerDesign.ComponentSize.progressBarHeight)
                        .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.full)).background(c.surfaceVariant),
                ) {
                    if (rate > 0f) {
                        Box(Modifier.fillMaxWidth(rate).height(LifePlannerDesign.ComponentSize.progressBarHeight).clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.full)).background(accent))
                    }
                }
            }
        }
    }
}

private fun summary(state: GoalsState): String = when {
    state.active.isEmpty() && state.completed.isEmpty() -> "Nothing yet — let's start one."
    state.active.isEmpty() -> "${state.completed.size} done. Time for a new one?"
    else -> "${state.active.size} active · ${state.completed.size} done"
}
