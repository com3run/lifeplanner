package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import com.adamglin.phosphoricons.regular.Play
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.components.StatTile
import az.tribe.lifeplanner.ui.components.StateView
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.gradient
import az.tribe.lifeplanner.ui.theme.gradientColors
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Fire
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Pencil
import com.adamglin.phosphoricons.regular.Prohibit
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Trophy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt
import kotlin.time.Clock
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back

/**
 * D7, the redesigned **Habit Detail**. Category-gradient hero + consistency ring, the signature
 * one-tap today check-in, streak stat tiles, a five-week consistency heatmap, the goal it supports
 * (Pillar-1 "why" chain), and Edit reusing the existing bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onBackClick: () -> Unit,
    onOpenLesson: (String) -> Unit = {},
    onPractice: (String) -> Unit = {},
    viewModel: HabitDetailViewModel = koinViewModel { parametersOf(habitId) },
) {
    val habit by viewModel.habit.collectAsStateWithLifecycle()
    val doneToday by viewModel.doneToday.collectAsStateWithLifecycle()
    val goalTitle by viewModel.linkedGoalTitle.collectAsStateWithLifecycle()
    val completedDates by viewModel.completedDates.collectAsStateWithLifecycle()
    val rate by viewModel.completionRate.collectAsStateWithLifecycle()
    val lessons by viewModel.relatedLessons.collectAsStateWithLifecycle()
    val c = MaterialTheme.modernColors

    var showEdit by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.xpEvent.collect { xp -> if (xp > 0) snackbarHostState.showSnackbar("+$xp XP") }
    }

    Scaffold(
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Habit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
    ) { padding ->
        val h = habit
        if (h == null) {
            StateView(
                title = "Habit not found",
                message = "It may have been deleted.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }
        val isQuit = h.type == HabitType.QUIT

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
                HabitPaperHeader(habit = h, ratePercent = rate)
            }

            // Signature interaction: one-tap today check-in
            item {
                TodayCheckInCard(
                    done = doneToday,
                    isQuit = isQuit,
                    accent = c.success,
                    onToggle = viewModel::toggleToday,
                )
            }

            // Streak stats
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                    StatTile(
                        value = "${h.currentStreak}",
                        label = if (isQuit) "Days clean" else "Current",
                        accent = if (isQuit) c.success else c.warning,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(value = "${h.longestStreak}", label = "Best", accent = c.secondary, modifier = Modifier.weight(1f))
                    StatTile(value = "${h.totalCompletions}", label = "Total", accent = c.primary, modifier = Modifier.weight(1f))
                }
            }

            // Consistency heatmap (last 5 weeks)
            item {
                ConsistencyCard(completedDates = completedDates, accent = h.category.gradientColors().first())
            }

            // Supports, the goal this habit serves (Pillar 1 "why")
            goalTitle?.let { gt ->
                item {
                    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
                        Row(Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent), horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            IconChip(PhosphorIcons.Regular.Flag, tint = c.secondary)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Supports $gt", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                                Text("Every check-in moves this goal forward.", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                        }
                    }
                }
            }

            // Why this works: lessons matched to what this habit is about, so the knowledge shows up
            // next to the practice instead of waiting to be found in the Learn hub.
            if (lessons.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                        Text(
                            "Why this works",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = c.textPrimary,
                        )
                        lessons.forEach { lesson ->
                            RelatedLessonRow(lesson = lesson, onClick = { onOpenLesson(lesson.id) })
                        }
                    }
                }
            }

            if (h.description.isNotBlank()) {
                item {
                    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
                        Text(h.description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(LifePlannerDesign.Padding.cardContent))
                    }
                }
            }

            // A timed or counted habit is something you *do*, so offer somewhere to do it.
            // A plain check-off habit has nothing to run, so it gets no practice button.
            item {
                habit?.takeIf { it.trackMode != HabitTrackMode.SINGLE }?.let { h ->
                    AppButton(
                        text = if (h.trackMode == HabitTrackMode.DURATION) "Start practice" else "Count reps",
                        onClick = { onPractice(h.id) },
                        leadingIcon = PhosphorIcons.Regular.Play,
                        modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                    )
                }
            }

            item {
                AppButton(
                    text = "Edit habit",
                    onClick = { showEdit = true },
                    variant = AppButtonVariant.SECONDARY,
                    leadingIcon = PhosphorIcons.Regular.Pencil,
                    modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                )
            }
        }
    }

    if (showEdit) {
        habit?.let { h ->
            EditHabitBottomSheet(
                habit = h,
                onDismiss = { showEdit = false },
                onConfirm = { updated ->
                    viewModel.updateHabit(updated)
                    showEdit = false
                },
            )
        }
    }
}

/**
 * The habit written like a document rather than announced like a poster, matching the goal
 * detail's paper header: category overline, a title that wraps as long as it needs to, a thin
 * consistency line, and one quiet meta line. The streak is deliberately absent; the stat tiles
 * directly below own those numbers, and the old hero repeating them was the page saying
 * everything twice.
 */
@Composable
internal fun HabitPaperHeader(
    habit: Habit,
    /** 0..100, the last 30 days of check-ins. */
    ratePercent: Float,
    modifier: Modifier = Modifier,
) {
    val accent = habit.category.gradientColors().first()
    val isQuit = habit.type == HabitType.QUIT

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "${habit.category.displayName} · ${habit.type.displayName}".uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )

        Text(
            text = habit.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (ratePercent > 0f) {
            LinearProgressIndicator(
                progress = { (ratePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )
        }

        Text(
            text = when {
                habit.totalCompletions == 0 && isQuit ->
                    "${habit.frequency.displayName} · day one starts now"
                habit.totalCompletions == 0 ->
                    "${habit.frequency.displayName} · start today"
                else ->
                    "${habit.frequency.displayName} · ${ratePercent.roundToInt()}% of the last 30 days"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TodayCheckInCard(done: Boolean, isQuit: Boolean, accent: Color, onToggle: () -> Unit) {
    val c = MaterialTheme.modernColors
    val label = when {
        done && isQuit -> "Resisted today"
        done -> "Done today"
        isQuit -> "Resisted today?"
        else -> "Check in for today"
    }
    val sub = if (done) "Tap to undo" else "One tap to keep the streak alive"
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onToggle),
        color = if (done) accent.copy(alpha = 0.12f) else c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when {
                    done -> PhosphorIcons.Regular.CheckCircle
                    isQuit -> PhosphorIcons.Regular.Prohibit
                    else -> PhosphorIcons.Regular.Circle
                },
                contentDescription = null,
                tint = if (done) accent else c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.large),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (done) accent else c.textPrimary)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun ConsistencyCard(completedDates: Set<LocalDate>, accent: Color) {
    val c = MaterialTheme.modernColors
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    // Monday five weeks back, so columns read Mon..Sun.
    val start = remember(today) {
        var d = today
        while (d.dayOfWeek.ordinal != 0) d = d.plus(-1, DateTimeUnit.DAY)
        d.plus(-4 * 7, DateTimeUnit.DAY)
    }
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent), verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
            Text("Last 5 weeks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayLabels.forEach { lbl ->
                    Text(lbl, style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            repeat(5) { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(7) { day ->
                        val date = start.plus(week * 7 + day, DateTimeUnit.DAY)
                        val isFuture = date > today
                        val isDone = date in completedDates
                        val color = when {
                            isFuture -> Color.Transparent
                            isDone -> accent
                            else -> c.textTertiary.copy(alpha = 0.12f)
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(color),
                        )
                    }
                }
            }
            Spacer(Modifier.size(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(c.textTertiary.copy(alpha = 0.12f)))
                Text("missed", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                Spacer(Modifier.size(8.dp))
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(accent))
                Text("done", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            }
        }
    }
}

/**
 * One lesson in the habit's "Why this works" shelf: emoji, title, and how long it takes to read.
 * Tapping opens the full lesson in the existing Learn detail screen.
 */
@Composable
private fun RelatedLessonRow(lesson: KnowledgeBit, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(lesson.emoji, style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                )
                Text(
                    "${lesson.readMin} min read",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textTertiary,
                )
            }
        }
    }
}
