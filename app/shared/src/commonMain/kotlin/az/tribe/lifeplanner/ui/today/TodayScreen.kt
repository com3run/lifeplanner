package az.tribe.lifeplanner.ui.today

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.LifePlannerGradients
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Lightning
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * D7 — the redesigned **Today** agency surface (D2): a calm "top stories for your life" front door
 * answering "what can I do right now?". Built on D3 tokens, the D4 [AppButton], and the premium
 * [GradientHero]/[IconChip] blocks so it matches the app's gradient/depth craft bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onBackClick: () -> Unit,
    viewModel: TodayViewModel = koinViewModel(),
) {
    val habits by viewModel.habitsToday.collectAsState()
    val possibilities by viewModel.possibilities.collectAsState()
    val c = MaterialTheme.modernColors
    val doneCount = habits.count { it.doneToday }

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Today", fontWeight = FontWeight.Bold) },
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
                    eyebrow = today(),
                    title = greeting(),
                    subtitle = if (habits.isEmpty()) "What matters to you right now is your call."
                    else "$doneCount of ${habits.size} habits done today — your call on the rest.",
                )
            }

            item { SectionLabel("Right now you could…") }
            if (possibilities.isEmpty()) {
                item { Hint("Add a goal or a habit and your next best move shows up here.") }
            } else {
                items(possibilities, key = { it.kind.name + it.targetId }) { p ->
                    PossibilityCard(
                        title = p.title,
                        reason = p.reason,
                        isHabit = p.kind == Possibility.Kind.HABIT,
                        onAction = { if (p.kind == Possibility.Kind.HABIT) viewModel.checkInHabit(p.targetId) },
                    )
                }
            }

            item { SectionLabel("Today's habits") }
            if (habits.isEmpty()) {
                item { Hint("No habits yet — small, repeatable actions go here.") }
            } else {
                items(habits, key = { it.habit.id }) { h ->
                    HabitRow(title = h.habit.title, done = h.doneToday, onToggle = { viewModel.checkInHabit(h.habit.id) })
                }
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
private fun Hint(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.modernColors.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.modernColors.textSecondary,
            modifier = Modifier.padding(LifePlannerDesign.Padding.cardContent),
        )
    }
}

@Composable
private fun PossibilityCard(title: String, reason: String, isHabit: Boolean, onAction: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconChip(
                icon = if (isHabit) PhosphorIcons.Regular.Lightning else PhosphorIcons.Regular.Flag,
                tint = if (isHabit) c.success else c.primary,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary, maxLines = 1)
                Text(reason, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2)
            }
            if (isHabit) AppButton(text = "Do it", onClick = onAction, variant = AppButtonVariant.SECONDARY)
        }
    }
}

@Composable
private fun HabitRow(title: String, done: Boolean, onToggle: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !done, onClick = onToggle),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (done) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
                contentDescription = if (done) "Done" else "Mark done",
                tint = if (done) c.success else c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.large),
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (done) FontWeight.Normal else FontWeight.Medium),
                color = if (done) c.textTertiary else c.textPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun greeting(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Tonight"
    }
}

private fun today(): String {
    val d = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${d.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${d.dayOfMonth}"
}
