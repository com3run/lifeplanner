package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.koinInject
import com.russhwolf.settings.Settings
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.FeedKind
import az.tribe.lifeplanner.domain.model.HourlyBrief
import az.tribe.lifeplanner.domain.model.TodayWeather
import az.tribe.lifeplanner.domain.model.WeatherCondition
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
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
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
    var showWeatherSheet by remember { mutableStateOf(false) }

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
            // The header is now weather-forward when we have it: one understandable widget instead of
            // a big greeting. Falls back to the greeting hero when weather isn't available yet.
            val loadedWeather = (weatherState as? TodayWeatherViewModel.State.Loaded)?.weather
            item { TodayHero(progress = progress, weather = loadedWeather, onExpand = { showWeatherSheet = true }) }

            // Hourly detail lives inside the tap-to-expand weather popup, not as a second feed card.
            if (weatherState is TodayWeatherViewModel.State.NeedsPermission) {
                item(key = "weather_prompt") { WeatherPromptCard(onEnable = locationPermission.request) }
            }

            // A calming beat: a breath or two, right in the flow, that "grows" as you complete it.
            item(key = "breath") { BreathingCard() }

            // Today's plan, planner-style: tick items off right here instead of jumping to Goals.
            // Always shown (with an empty state) so it never silently disappears once you're caught up.
            item(key = "plan_header") { SectionHeader(label = "Today's plan", onSeeAll = null) }
            if (plan.isEmpty()) {
                item(key = "plan_empty") { Hint("You're all caught up for today. Steps due today show up here.") }
            } else {
                items(plan, key = { "plan_${it.milestoneId}" }) { p ->
                    PlanRow(item = p, onComplete = { viewModel.completePlanItem(p.milestoneId) })
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

    val sheetWeather = (weatherState as? TodayWeatherViewModel.State.Loaded)?.weather
    if (showWeatherSheet && sheetWeather != null) {
        WeatherDetailSheet(weather = sheetWeather, onDismiss = { showWeatherSheet = false })
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
    // Level/streak live on the You tab; keep the greeting hero calm when weather isn't available.
    GradientHero(
        eyebrow = today(),
        title = greeting(),
        subtitle = when {
            p == null -> "Here is your day, your way."
            p.currentStreak > 0 -> "${p.currentStreak} day streak. Let's build on it."
            else -> "Let's build today."
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

/**
 * The Today header. When weather is available it becomes one compact widget, place + conditions as
 * the headline (no oversized greeting), the alert or high/low + streak as the subtitle, and the
 * level ring kept on the right. Falls back to the plain greeting hero otherwise.
 */
@Composable
private fun TodayHero(progress: UserProgress?, weather: TodayWeather?, onExpand: () -> Unit) {
    if (weather == null) {
        Header(progress)
        return
    }
    val subtitle = weather.alert ?: buildString {
        append("H ${weather.highC}°  L ${weather.lowC}°")
        if (progress != null && progress.currentStreak > 0) append(" · ${progress.currentStreak} day streak")
    }
    val place = weather.placeName
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    // Level lives on the You tab; the Today hero is a clean weather widget whose background reflects
    // the day's mood (condition + time), and taps open the full details.
    Box(Modifier.clickable(onClick = onExpand)) {
        GradientHero(
            eyebrow = if (place != null) "${today()} · $place" else today(),
            title = "${weather.condition.emoji}  ${weather.temperatureC}°  ${weather.condition.label}",
            subtitle = subtitle,
            gradient = heroMoodGradient(weather.condition, hour),
            trailing = {
                Icon(PhosphorIcons.Regular.CaretDown, contentDescription = "Weather details", tint = Color.White.copy(alpha = 0.85f))
            },
        )
    }
}

/**
 * A deterministic "day mood" gradient for the hero, dynamic through the day but predictable: the same
 * condition + time band always yields the same look. Night and rain read cool and calm; clear days
 * read bright; sunset reads warm.
 */
private fun heroMoodGradient(condition: WeatherCondition, hour: Int): Brush {
    val night = hour < 6 || hour >= 20
    val evening = hour in 17..19
    val rainy = condition == WeatherCondition.RAIN || condition == WeatherCondition.HEAVY_RAIN ||
        condition == WeatherCondition.SHOWERS || condition == WeatherCondition.DRIZZLE
    val cloudy = condition == WeatherCondition.CLOUDY || condition == WeatherCondition.PARTLY_CLOUDY || condition == WeatherCondition.FOG
    val colors = when {
        night -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))            // deep indigo night
        condition == WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF2E3B4E), Color(0xFF465A73))
        rainy -> listOf(Color(0xFF41556B), Color(0xFF5B7089))            // calm slate
        evening -> listOf(Color(0xFFF6836B), Color(0xFF9B5DE5))          // sunset coral → violet
        cloudy -> listOf(Color(0xFF5E6E86), Color(0xFF8090A8))           // muted grey-blue
        else -> listOf(Color(0xFF3B82F6), Color(0xFF6366F1))             // bright clear day
    }
    return Brush.linearGradient(colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherDetailSheet(weather: TodayWeather, onDismiss: () -> Unit) {
    val c = MaterialTheme.modernColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val nowHour = now.hour
    val nowLabel = timeLabel(now.hour, now.minute)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = c.background) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = LifePlannerDesign.Padding.screenHorizontal).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            // Current
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(weather.condition.emoji, style = MaterialTheme.typography.displaySmall)
                Column(Modifier.weight(1f)) {
                    Text("${weather.temperatureC}°  ${weather.condition.label}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
                    Text(
                        buildString {
                            weather.placeName?.let { append(it); append(" · ") }
                            append("as of $nowLabel")
                        },
                        style = MaterialTheme.typography.bodyMedium, color = c.textSecondary,
                    )
                }
            }
            weather.alert?.let {
                Surface(color = c.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(it, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = c.primary, modifier = Modifier.fillMaxWidth().padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = LifePlannerDesign.Spacing.xs))
                }
            }

            // Metric grid
            val d = weather.details
            val metrics = buildList {
                add("High / Low" to "${weather.highC}° / ${weather.lowC}°")
                d?.feelsLikeC?.let { add("Feels like" to "$it°") }
                d?.humidityPct?.let { add("Humidity" to "$it%") }
                d?.windSpeedKmh?.let { add("Wind" to "$it km/h") }
                d?.rainChanceMaxPct?.let { add("Rain chance" to "$it%") }
                d?.uvIndexMax?.let { add("UV index" to "$it") }
                d?.sunrise?.let { add("Sunrise" to it) }
                d?.sunset?.let { add("Sunset" to it) }
            }
            metrics.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                    row.forEach { (label, value) -> MetricTile(label, value, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (weather.hourly.isNotEmpty()) {
                Text("Weather by the hour", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                    weather.hourly.forEach { h -> HourCell(h, isNow = h.hour == nowHour) }
                }
            }

            Text("As of $nowLabel · Source: ${weather.source}", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.modernColors
    Surface(modifier = modifier, color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium)) {
        Column(Modifier.padding(LifePlannerDesign.Padding.cardContent), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        }
    }
}

private const val BREATH_GOAL = 3

/**
 * A tiny in-app "grow" moment: take a breath or two a day. The card expands into a breathing circle
 * that grows on the inhale and shrinks on the exhale; finishing counts toward a small daily goal
 * (persisted per day) and earns a little XP, like completing a micro-habit while you're already here.
 */
@Composable
private fun BreathingCard() {
    val settings: Settings = koinInject()
    val gamification: GamificationRepository = koinInject()
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.modernColors
    val dateKey = remember { "breaths_" + Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() }
    var count by remember { mutableStateOf(settings.getInt(dateKey, 0)) }
    var active by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = count < BREATH_GOAL || active) {
        Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
            if (active) {
                BreathingAnimation(
                    onDone = {
                        val next = (count + 1).coerceAtMost(BREATH_GOAL)
                        count = next
                        settings.putInt(dateKey, next)
                        scope.launch { runCatching { gamification.awardXp(2) } }
                        active = false
                    },
                    onCancel = { active = false },
                )
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🫧", style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Take a breath", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                        Text(
                            if (count == 0) "A moment to reset before you dive in." else "$count of $BREATH_GOAL today. One more?",
                            style = MaterialTheme.typography.bodySmall, color = c.textSecondary,
                        )
                    }
                    AppButton(text = "Breathe", onClick = { active = true }, variant = AppButtonVariant.SECONDARY)
                }
            }
        }
    }
}

@Composable
private fun BreathingAnimation(onDone: () -> Unit, onCancel: () -> Unit) {
    val c = MaterialTheme.modernColors
    val total = 3
    var inhaling by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (inhaling) 1f else 0.55f,
        animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
        label = "breathScale",
    )
    LaunchedEffect(Unit) {
        for (i in 0 until total) {
            inhaling = true; delay(3800)
            inhaling = false; delay(3800)
            completed = i + 1
        }
        onDone()
    }
    Column(
        Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(150.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(c.primary.copy(alpha = 0.55f), c.primary.copy(alpha = 0.12f)))),
            )
            Text(if (inhaling) "In" else "Out", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
        }
        Text(if (inhaling) "Breathe in…" else "Breathe out…", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Text("Breath ${(completed + 1).coerceAtMost(total)} of $total", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        TextButton(onClick = onCancel) { Text("Done for now") }
    }
}

@Composable
private fun HourCell(h: HourlyBrief, isNow: Boolean = false) {
    val c = MaterialTheme.modernColors
    val wet = h.precipitationProbability in 1..100
    val heavyNow = isNow && wet && h.precipitationProbability >= 50
    // The current hour is highlighted so "what's happening now" is easy to catch at a glance.
    val bg = when {
        heavyNow -> Color(0xFFE53935).copy(alpha = 0.18f) // rain right now: warm alert tint
        isNow -> c.primary.copy(alpha = 0.16f)
        else -> Color.Transparent
    }
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            if (isNow) "Now" else clockShort(h.hour),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal),
            color = if (isNow) c.primary else c.textSecondary,
        )
        Text(h.condition.emoji, style = MaterialTheme.typography.titleMedium)
        Text("${h.temperatureC}°", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = if (isNow) c.primary else c.textPrimary)
        Text(
            if (wet) "${h.precipitationProbability}%" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = if (wet && h.precipitationProbability >= 50) c.primary else c.textTertiary,
        )
    }
}

private fun clockShort(hour24: Int): String {
    val h = ((hour24 % 24) + 24) % 24
    val period = if (h < 12) "AM" else "PM"
    val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
    return "$h12$period"
}

/** Device-local time like "1:57 PM". */
private fun timeLabel(hour24: Int, minute: Int): String {
    val h = ((hour24 % 24) + 24) % 24
    val period = if (h < 12) "AM" else "PM"
    val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
    val mm = minute.toString().padStart(2, '0')
    return "$h12:$mm $period"
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

/** Planner-style row: a tappable check circle ticks the milestone done in place (no navigation). */
@Composable
private fun PlanRow(item: PlanItem, onComplete: () -> Unit) {
    val c = MaterialTheme.modernColors
    val overdueColor = Color(0xFFE53935)
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onComplete),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Circle,
                contentDescription = "Mark done",
                tint = if (item.overdue) overdueColor else c.textTertiary,
                modifier = Modifier.size(LifePlannerDesign.IconSize.large),
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
