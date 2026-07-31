package az.tribe.lifeplanner.ui.goals

import az.tribe.lifeplanner.core.FeatureFlags

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.components.ProgressRing
import az.tribe.lifeplanner.ui.components.StateView
import az.tribe.lifeplanner.ui.goal.CoachMilestonesCard
import az.tribe.lifeplanner.ui.goal.GoalJourneyCard
import az.tribe.lifeplanner.ui.intro.FeatureIntroCatalog
import az.tribe.lifeplanner.ui.intro.FeatureIntroHost
import az.tribe.lifeplanner.ui.intro.rememberFeatureIntroGate
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.gradient
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Pencil
import com.adamglin.phosphoricons.regular.Sparkle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

/**
 * D7, the redesigned **Goal Detail**. Category-gradient hero + progress ring, the real Why-Chain
 * (the value this goal serves, Pillar 1), checkable milestones, and Edit reusing the existing flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onBackClick: () -> Unit,
    onEdit: () -> Unit,
    onExplorePossibilities: () -> Unit = {},
    onOpenCoach: (String) -> Unit = {},
    viewModel: GoalDetailViewModel = koinViewModel { parametersOf(goalId) },
) {
    val goal by viewModel.goal.collectAsState()
    val valueTitle by viewModel.valueTitle.collectAsState()
    val suggestedValue by viewModel.suggestedValue.collectAsState()
    val c = MaterialTheme.modernColors
    val introGate = rememberFeatureIntroGate()
    FeatureIntroHost(introGate)

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Goal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
    ) { padding ->
        val g = goal
        if (g == null) {
            StateView(
                title = "Goal not found",
                message = "It may have been deleted.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }
        val rate = g.completionRate.coerceIn(0f, 1f)
        val done = g.milestones.count { it.isCompleted }

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
                    eyebrow = g.category.displayName,
                    title = g.title,
                    subtitle = "${(rate * 100).roundToInt()}% complete · Due ${g.dueDate}",
                    gradient = g.category.gradient(),
                    trailing = {
                        ProgressRing(
                            progress = rate, diameter = 64.dp, strokeWidth = 7.dp,
                            color = Color.White, trackColor = Color.White.copy(alpha = 0.3f),
                        ) {
                            Text("${(rate * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    },
                )
            }

            // Why-Chain (Pillar 1): name the value, and since the coach exists because of that
            // value, offer to chat about it right here instead of just stating it.
            item {
                val coach = CoachPersona.getByCategory(g.category)
                Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
                    Column(
                        Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)
                    ) {
                        val suggestion = suggestedValue
                        // Three states, none of them a nag: linked (its why), a confident
                        // suggestion (accept in one tap), or category+coach framing as the why.
                        val headline = when {
                            valueTitle != null -> "Toward $valueTitle"
                            suggestion != null -> "Serves \"${suggestion.title}\"?"
                            else -> "Part of your ${g.category.displayName} journey"
                        }
                        val body = when {
                            valueTitle != null -> "This goal serves \"$valueTitle\". ${coach.name} coaches this, keep the why alive together."
                            suggestion != null -> "This looks like it serves your value \"${suggestion.title}\". Link it as your why, or change it in Edit."
                            else -> "${coach.name} coaches this area. Add a life value in Edit whenever you want to name the deeper why."
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            IconChip(PhosphorIcons.Regular.Compass, tint = c.secondary)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(headline, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                                Text(body, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                        }
                        when {
                            valueTitle != null -> AppButton(
                                text = "Talk it through with ${coach.name}",
                                onClick = { onOpenCoach(coach.id) },
                                variant = AppButtonVariant.SECONDARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                            suggestion != null -> AppButton(
                                text = "Yes, this is my why",
                                onClick = { viewModel.linkValue(suggestion.id) },
                                variant = AppButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Goal as a journal: local narrative of where the user is on this journey.
            item { GoalJourneyCard(goal = g, horizontalPadding = 0.dp) }

            if (g.description.isNotBlank()) {
                item {
                    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
                        Text(g.description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(LifePlannerDesign.Padding.cardContent))
                    }
                }
            }

            item {
                Text(
                    if (g.milestones.isEmpty()) "Milestones" else "Milestones · $done/${g.milestones.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                    modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
                )
            }
            items(g.milestones, key = { it.id }) { m ->
                MilestoneRow(m, onToggle = { viewModel.toggleMilestone(m.id, !m.isCompleted) })
            }

            // The coach's draft, always here rather than only on an empty goal: a plan is easier to
            // edit than to invent, so the next step is always one tap (or one reword) away.
            item(key = "coach_milestones") {
                CoachMilestonesCard(
                    goalTitle = g.title,
                    category = g.category,
                    description = g.description,
                    existingTitles = g.milestones.map { it.title },
                    onAdd = { viewModel.addMilestone(it) },
                )
            }

            // Pillar 6: the divergent way out when an in-progress goal stalls.
            if (FeatureFlags.PILLAR_POSSIBILITY && rate < 1f) {
                item {
                    AppButton(
                        text = "Feeling stuck? Explore possibilities",
                        onClick = { introGate.open(FeatureIntroCatalog.POSSIBILITY, c.primary) { onExplorePossibilities() } },
                        variant = AppButtonVariant.PRIMARY,
                        leadingIcon = PhosphorIcons.Regular.Sparkle,
                        modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                    )
                }
            }

            item {
                AppButton(
                    text = "Edit goal",
                    onClick = onEdit,
                    variant = AppButtonVariant.SECONDARY,
                    leadingIcon = PhosphorIcons.Regular.Pencil,
                    modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun MilestoneRow(m: Milestone, onToggle: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onToggle),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (m.isCompleted) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
                contentDescription = if (m.isCompleted) "Done" else "Mark done",
                tint = if (m.isCompleted) c.success else c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.medium),
            )
            Text(
                m.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (m.isCompleted) c.textTertiary else c.textPrimary,
                textDecoration = if (m.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
