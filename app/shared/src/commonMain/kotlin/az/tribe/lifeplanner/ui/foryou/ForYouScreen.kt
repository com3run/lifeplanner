package az.tribe.lifeplanner.ui.foryou

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.FeedKind
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.components.ProgressRing
import az.tribe.lifeplanner.ui.intro.FeatureIntroHost
import az.tribe.lifeplanner.ui.intro.rememberFeatureIntroGate
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.gradientColors
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Fire
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Sparkle
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * The new home: a "For You" feed. A calm gradient header (greeting + level/streak ring), filter
 * chips (All / Right now / Reflect / Learn), then a ranked stream of cards the app fills for you,
 * the single best next action, insights about you, who you are becoming, and leveled knowledge.
 * Reflect cards deep-link into the full You screens so those functions live on the front door.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouScreen(
    onOpenRoute: (String) -> Unit,
    viewModel: ForYouViewModel = koinViewModel(),
) {
    val feed by viewModel.feed.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val c = MaterialTheme.modernColors

    var filter by remember { mutableStateOf<FeedSection?>(null) }
    val introGate = rememberFeatureIntroGate()
    val visible = remember(feed, filter) {
        val f = filter
        if (f == null) feed else feed.filter { it.kind.section() == f }
    }
    val grouped = remember(visible) { visible.groupBy { it.kind.section() } }

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("For You", fontWeight = FontWeight.Bold) },
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
            item { Header(progress) }
            item { FilterRow(selected = filter, onSelect = { filter = it }) }

            if (feed.isEmpty()) {
                item {
                    Hint(
                        if (isLoading) "Gathering today for you..."
                        else "Add a goal or a habit and your feed fills up with insights, nudges, and ideas.",
                    )
                }
            } else {
                FeedSection.entries.forEach { sec ->
                    val cards = grouped[sec].orEmpty()
                    if (cards.isNotEmpty()) {
                        item(key = "h_${sec.name}") { SectionLabel(sec.label) }
                        items(cards, key = { it.id }) { fi ->
                            val accent = accentFor(fi)
                            FeedCard(
                                item = fi,
                                accent = accent,
                                onAction = { fi.actionHabitId?.let(viewModel::checkInHabit) },
                                onOpen = {
                                    val route = fi.route ?: return@FeedCard
                                    // A feature the user has never met explains itself first.
                                    introGate.open(fi.introId, accent) { onOpenRoute(route) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    FeatureIntroHost(introGate)
}

@Composable
private fun accentFor(item: FeedItem): Color {
    val c = MaterialTheme.modernColors
    return when (item.kind) {
        FeedKind.DO_NEXT -> item.category?.gradientColors()?.firstOrNull() ?: c.primary
        FeedKind.INSIGHT -> c.secondary
        FeedKind.BECOMING -> c.success
        FeedKind.PATTERN -> c.accent
        FeedKind.MOMENTUM -> c.warning
        FeedKind.KNOWLEDGE -> c.primary
        FeedKind.POSSIBILITY -> c.secondary
    }
}

@Composable
private fun Header(progress: UserProgress?) {
    val p = progress
    GradientHero(
        eyebrow = today(),
        title = greeting(),
        subtitle = when {
            p == null -> "Here is your day, your way."
            p.currentStreak > 0 -> "${p.currentStreak} day streak, level ${p.currentLevel} ${p.title}."
            else -> "Level ${p.currentLevel} ${p.title}. Let's build today."
        },
        trailing = if (p == null) null else {
            {
                ProgressRing(
                    progress = p.levelProgress.coerceIn(0f, 1f), diameter = 64.dp, strokeWidth = 7.dp,
                    color = Color.White, trackColor = Color.White.copy(alpha = 0.3f),
                ) {
                    Text("Lv ${p.currentLevel}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        },
    )
}

@Composable
private fun FilterRow(selected: FeedSection?, onSelect: (FeedSection?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
        FilterChip("All", selected == null) { onSelect(null) }
        FeedSection.entries.forEach { sec ->
            FilterChip(sec.label, selected == sec) { onSelect(sec) }
        }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.bouncyClickable(onClick = onClick),
        color = if (active) c.primary else c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (active) Color.White else c.textSecondary,
            modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = LifePlannerDesign.Spacing.xs),
        )
    }
}

@Composable
private fun FeedCard(item: FeedItem, accent: Color, onAction: () -> Unit, onOpen: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(enabled = item.route != null, onClick = onOpen),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.Top) {
                LeadingVisual(item, accent)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
                    Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
                    Text(item.body, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
                if (item.route != null && item.actionLabel == null) {
                    Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
                }
            }
            if (item.actionLabel != null) {
                AppButton(
                    text = item.actionLabel,
                    onClick = onAction,
                    variant = AppButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LeadingVisual(item: FeedItem, accent: Color) {
    val emoji = item.emoji
    if (item.kind == FeedKind.KNOWLEDGE && emoji != null) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium), modifier = Modifier.fillMaxSize()) {}
            Text(emoji, style = MaterialTheme.typography.titleLarge)
        }
    } else {
        val icon = when (item.kind) {
            FeedKind.DO_NEXT -> PhosphorIcons.Regular.Lightning
            FeedKind.INSIGHT -> PhosphorIcons.Regular.Brain
            FeedKind.BECOMING -> PhosphorIcons.Regular.Sparkle
            FeedKind.PATTERN -> PhosphorIcons.Regular.ClockCounterClockwise
            FeedKind.MOMENTUM -> PhosphorIcons.Regular.Fire
            FeedKind.KNOWLEDGE -> PhosphorIcons.Regular.Sparkle
            FeedKind.POSSIBILITY -> PhosphorIcons.Regular.Sparkle
        }
        IconChip(icon, tint = accent)
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

private fun greeting(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
}

private fun today(): String {
    val d = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${d.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${d.dayOfMonth}"
}
