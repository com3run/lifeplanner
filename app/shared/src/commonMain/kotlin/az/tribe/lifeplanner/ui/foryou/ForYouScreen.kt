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
import az.tribe.lifeplanner.domain.model.TodayWeather
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.components.ProgressRing
import az.tribe.lifeplanner.ui.intro.FeatureIntroHost
import az.tribe.lifeplanner.location.LocationPermissionState
import az.tribe.lifeplanner.location.rememberLocationPermission
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.today.PlanItem
import az.tribe.lifeplanner.ui.today.TodayWeatherViewModel
import az.tribe.lifeplanner.ui.intro.rememberFeatureIntroGate
import androidx.compose.runtime.LaunchedEffect
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
import com.adamglin.phosphoricons.regular.Flag
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
    val plan by viewModel.todayPlan.collectAsState()
    val c = MaterialTheme.modernColors

    // Today's weather (Open-Meteo). The permission lives in Compose; feed its state to the VM.
    val weatherViewModel: TodayWeatherViewModel = koinViewModel()
    val weatherState by weatherViewModel.state.collectAsState()
    val locationPermission = rememberLocationPermission()
    LaunchedEffect(locationPermission.state) {
        weatherViewModel.onPermissionState(locationPermission.state == LocationPermissionState.GRANTED)
    }

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

            // Today's context: local weather + a plan-relevant heads-up (incoming rain, heat…).
            when (val w = weatherState) {
                is TodayWeatherViewModel.State.Loaded -> item(key = "weather") { WeatherCard(w.weather) }
                TodayWeatherViewModel.State.NeedsPermission ->
                    item(key = "weather_prompt") { WeatherPromptCard(onEnable = locationPermission.request) }
                else -> Unit // Idle / Loading / Unavailable: stay quiet
            }

            // Today's plan, the planner lives on the front door now: a compact, always-visible strip
            // of what's scheduled for today, above the filterable feed.
            if (plan.isNotEmpty()) {
                item(key = "plan_header") {
                    SectionHeader(label = "Today's plan", onSeeAll = { onOpenRoute(Screen.Goals.route) })
                }
                items(plan, key = { "plan_${it.goalId}|${it.title}" }) { p ->
                    PlanRow(item = p, onClick = { onOpenRoute("goal_detail_redesign/${p.goalId}") })
                }
            }

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
                        item(key = "h_${sec.name}") {
                            SectionHeader(
                                label = sec.label,
                                onSeeAll = if (sec == FeedSection.LEARN) {
                                    { onOpenRoute(Screen.LearnHub.route) }
                                } else null,
                            )
                        }
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
private fun WeatherCard(w: TodayWeather) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(w.condition.emoji, style = MaterialTheme.typography.headlineMedium)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        buildString {
                            w.placeName?.let { append(it); append(" · ") }
                            append("${w.temperatureC}°")
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = c.textPrimary,
                        maxLines = 1,
                    )
                    Text(
                        "${w.condition.label} · H ${w.highC}°  L ${w.lowC}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }
            w.alert?.let { alert ->
                Surface(color = c.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        alert,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = c.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = LifePlannerDesign.Spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherPromptCard(onEnable: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🌤️", style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("See today's weather here", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                Text("Turn on location to plan around conditions like incoming rain.", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            AppButton(text = "Enable", onClick = onEnable, variant = AppButtonVariant.SECONDARY)
        }
    }
}

@Composable
private fun PlanRow(item: PlanItem, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    val overdueColor = Color(0xFFE53935)
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
            IconChip(
                icon = PhosphorIcons.Regular.Flag,
                tint = if (item.overdue) overdueColor else c.primary,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary, maxLines = 1)
                Text(item.goalTitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 1)
            }
            DueChip(overdue = item.overdue)
        }
    }
}

@Composable
private fun DueChip(overdue: Boolean) {
    val c = MaterialTheme.modernColors
    val accent = if (overdue) Color(0xFFE53935) else c.primary
    Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
        Text(
            if (overdue) "Overdue" else "Today",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SectionHeader(label: String, onSeeAll: (() -> Unit)?) {
    val c = MaterialTheme.modernColors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = c.textPrimary,
        )
        if (onSeeAll != null) {
            Row(
                modifier = Modifier.bouncyClickable(onClick = onSeeAll),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "See all",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = c.primary,
                )
                Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.primary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
            }
        }
    }
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
