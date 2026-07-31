package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.Motion
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Lock
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.illus_learn_empty
import leanlifeplanner.app.shared.generated.resources.illus_learn_focus
import leanlifeplanner.app.shared.generated.resources.illus_learn_habits
import leanlifeplanner.app.shared.generated.resources.illus_learn_hero
import leanlifeplanner.app.shared.generated.resources.illus_learn_motivation
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Learn map. Each collection is a zone, and its lessons are stops on a winding trail you walk
 * down: cleared stops behind you, the next one lit and waiting, locked ground ahead. A list told you
 * what existed; a trail tells you where you are and what comes next.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnHubScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: LearnHubViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val c = MaterialTheme.modernColors

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Learn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            item(key = "banner") {
                MapBanner(
                    level = state.level,
                    levelTitle = state.levelTitle,
                    totalXp = state.totalXp,
                    read = state.readCount,
                    total = state.totalUnlocked,
                    continueTitle = state.continueLessonTitle,
                    continuePath = state.continuePathTitle,
                    isFresh = state.continueIsFresh,
                    onContinue = { state.continueLessonId?.let(onOpen) },
                )
            }

            if (state.recommended.isNotEmpty()) {
                item(key = "picked") {
                    PickedForYou(state.recommended, state.readIds, onOpen)
                }
            }

            state.collections.forEach { cui ->
                item(key = "zone_${cui.collection.id}") {
                    ZoneHeader(cui)
                }
                item(key = "trail_${cui.collection.id}") {
                    ZoneTrail(cui = cui, readIds = state.readIds, onOpen = onOpen)
                }
            }

            if (!state.loading && state.collections.isEmpty()) {
                item(key = "empty") {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.illus_learn_empty),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(180.dp),
                        )
                        Text(
                            "Lessons unlock as you grow",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = c.textPrimary,
                        )
                        Text(
                            "Keep building your goals and habits, and your first learning paths will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ── Banner ──────────────────────────────────────────────────────────────

/** Where you are on the whole map, and one tap back to the trail you were walking. */
@Composable
private fun MapBanner(
    level: Int,
    levelTitle: String,
    totalXp: Int,
    read: Int,
    total: Int,
    continueTitle: String?,
    continuePath: String?,
    isFresh: Boolean,
    onContinue: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.extraLarge))
            .background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))))
            .padding(LifePlannerDesign.Padding.cardContentLarge),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.illus_learn_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(52.dp),
                    )
                    Column {
                        Text(
                            "YOUR JOURNEY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Text(
                            if (total == 0) "Just getting started" else "$read of $total lessons",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        if (levelTitle.isNotBlank()) {
                            Text(
                                "Level $level · $levelTitle",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
                Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full)) {
                    Text(
                        "$totalXp XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = 4.dp),
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(if (total == 0) 0f else (read.toFloat() / total).coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color.White, Color(0xFFFFD479)))),
                )
            }

            if (continueTitle != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onContinue),
                    color = Color.White,
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            if (isFresh) "START HERE" else "PICK UP WHERE YOU LEFT OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = Color(0xFF764BA2),
                        )
                        Text(
                            continueTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2C3345),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (continuePath != null) {
                            Text(
                                continuePath,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6E7A94),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Zones ───────────────────────────────────────────────────────────────

@Composable
private fun ZoneHeader(cui: CollectionUi) {
    val c = MaterialTheme.modernColors
    Row(
        Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(pathIllustration(cui.collection.id)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 72.dp, height = 58.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                cui.collection.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Text(cui.collection.subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Surface(
            color = if (cui.isComplete) c.success.copy(alpha = 0.15f) else c.primary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full),
        ) {
            Text(
                if (cui.isComplete) "Done" else "${cui.readCount}/${cui.total}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (cui.isComplete) c.success else c.primary,
                modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = 4.dp),
            )
        }
    }
}

private enum class NodeState { READ, NEXT, UNREAD, LOCKED, REWARD_EARNED, REWARD_LOCKED }

private data class TrailStop(
    val label: String,
    val emoji: String,
    val state: NodeState,
    val done: Boolean,
    val lessonId: String?,
)

/** Lessons in order, then the locked ground still ahead, then the trophy at the end of the zone. */
private fun buildTrail(cui: CollectionUi, readIds: Set<String>): List<TrailStop> = buildList {
    cui.lessons.forEach { b ->
        val read = b.id in readIds
        add(
            TrailStop(
                label = b.title,
                emoji = b.emoji,
                state = when {
                    read -> NodeState.READ
                    b.id == cui.nextUnreadId -> NodeState.NEXT
                    else -> NodeState.UNREAD
                },
                done = read,
                lessonId = b.id,
            )
        )
    }
    if (cui.lockedCount > 0) {
        add(TrailStop("${cui.lockedCount} more unlock as you level up", "", NodeState.LOCKED, false, null))
    }
    add(
        TrailStop(
            label = if (cui.isComplete) "Zone cleared" else "Clear the zone",
            emoji = "🏅",
            state = if (cui.isComplete) NodeState.REWARD_EARNED else NodeState.REWARD_LOCKED,
            done = cui.isComplete,
            lessonId = null,
        )
    )
}

private val NODE_SIZE = 64.dp
private val ROW_HEIGHT = 128.dp
private val TRAIL_AMPLITUDE = 76.dp

/**
 * How far off centre each stop sits, as a fraction of [TRAIL_AMPLITUDE]. A full sine period over
 * eight stops, so the trail curves rather than zig-zagging.
 */
private val SWAY = listOf(0f, 0.7f, 1f, 0.7f, 0f, -0.7f, -1f, -0.7f)

private fun swayAt(index: Int): Float = SWAY[index % SWAY.size]

/** The winding trail for one zone: a drawn path with the stops sitting on it. */
@Composable
private fun ZoneTrail(cui: CollectionUi, readIds: Set<String>, onOpen: (String) -> Unit) {
    val c = MaterialTheme.modernColors
    val stops = remember(cui, readIds) { buildTrail(cui, readIds) }

    Box(Modifier.fillMaxWidth().height(ROW_HEIGHT * stops.size)) {
        // The trail itself, drawn behind the stops. Segments already walked are lit.
        Canvas(Modifier.fillMaxSize()) {
            val amp = TRAIL_AMPLITUDE.toPx()
            val row = ROW_HEIGHT.toPx()
            val midX = size.width / 2f
            fun center(i: Int) = Offset(midX + swayAt(i) * amp, row * i + row / 2f)

            for (i in 1 until stops.size) {
                val from = center(i - 1)
                val to = center(i)
                // A single control point midway gives a soft S as the sway changes sign.
                val path = Path().apply {
                    moveTo(from.x, from.y)
                    quadraticTo(from.x, (from.y + to.y) / 2f, to.x, to.y)
                }
                drawPath(
                    path = path,
                    color = if (stops[i - 1].done) c.primary else c.textTertiary.copy(alpha = 0.22f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }

        Column(Modifier.fillMaxSize()) {
            stops.forEachIndexed { i, stop ->
                Box(
                    Modifier.fillMaxWidth().height(ROW_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    TrailNode(
                        stop = stop,
                        modifier = Modifier.offset(x = TRAIL_AMPLITUDE * swayAt(i)),
                        onOpen = onOpen,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailNode(stop: TrailStop, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
    val c = MaterialTheme.modernColors
    val isNext = stop.state == NodeState.NEXT

    val pulse = rememberInfiniteTransition()
    val halo by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = Motion.standard),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val fill = when (stop.state) {
        NodeState.READ -> c.success
        NodeState.NEXT -> c.primary
        NodeState.REWARD_EARNED -> Color(0xFFF5A623)
        NodeState.UNREAD -> c.cardBackground
        NodeState.LOCKED, NodeState.REWARD_LOCKED -> c.surfaceVariant
    }

    Column(
        modifier = modifier.width(148.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // The one stop you're meant to take next breathes, so the eye lands on it first.
            if (isNext) {
                Box(
                    Modifier
                        .size(NODE_SIZE)
                        .scale(halo)
                        .clip(CircleShape)
                        .background(c.primary.copy(alpha = 0.22f)),
                )
            }

            val tapMod = if (stop.lessonId != null) {
                Modifier.bouncyClickable { onOpen(stop.lessonId) }
            } else Modifier

            Box(tapMod, contentAlignment = Alignment.TopCenter) {
                // A darker disc peeking out below gives the node a physical, pressable lip.
                Box(
                    Modifier
                        .offset(y = 5.dp)
                        .size(NODE_SIZE)
                        .clip(CircleShape)
                        .background(fill.copy(alpha = 0.45f)),
                )
                Box(
                    Modifier
                        .size(NODE_SIZE)
                        .clip(CircleShape)
                        .background(fill)
                        .then(
                            if (stop.state == NodeState.UNREAD)
                                Modifier.border(2.dp, c.textTertiary.copy(alpha = 0.35f), CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    when (stop.state) {
                        NodeState.LOCKED, NodeState.REWARD_LOCKED -> Icon(
                            PhosphorIcons.Regular.Lock,
                            contentDescription = null,
                            tint = c.textTertiary,
                            modifier = Modifier.size(22.dp),
                        )
                        else -> Text(
                            stop.emoji.ifBlank { "•" },
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = if (stop.state == NodeState.UNREAD) Modifier.alpha(0.55f) else Modifier,
                        )
                    }
                }
                if (stop.state == NodeState.READ) {
                    Box(
                        Modifier
                            .offset(x = 22.dp, y = 44.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(c.success)
                            .border(2.dp, c.background, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Check,
                            contentDescription = "Read",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (isNext) {
            Surface(color = c.primary, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full)) {
                Text(
                    "START",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        // An opaque signpost, so the trail passing behind never runs through the words.
        Surface(color = c.background, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small)) {
            Text(
                stop.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                ),
                color = when (stop.state) {
                    NodeState.READ -> c.textSecondary
                    NodeState.LOCKED, NodeState.REWARD_LOCKED -> c.textTertiary
                    else -> c.textPrimary
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

/**
 * The recommender's picks, which follow the user's habits and goals rather than the trail order.
 * Kept beside the map because a fixed path can't know that this week is about sleep.
 */
@Composable
private fun PickedForYou(lessons: List<KnowledgeBit>, readIds: Set<String>, onOpen: (String) -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        Modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(Modifier.padding(vertical = LifePlannerDesign.Spacing.xs)) {
            Text(
                "PICKED FOR YOU",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = c.textSecondary,
                modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.cardContent, vertical = 6.dp),
            )
            lessons.forEach { bit ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .bouncyClickable { onOpen(bit.id) }
                        .padding(horizontal = LifePlannerDesign.Padding.cardContent, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(bit.emoji, style = MaterialTheme.typography.titleMedium)
                    Text(
                        bit.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (bit.id in readIds) c.textSecondary else c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (bit.id in readIds) {
                        Icon(
                            PhosphorIcons.Regular.Check,
                            contentDescription = "Read",
                            tint = c.success,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/** The illustration for each zone (falls back to the generic learning art). */
private fun pathIllustration(collectionId: String): DrawableResource = when (collectionId) {
    "col_habits" -> Res.drawable.illus_learn_habits
    "col_motivation" -> Res.drawable.illus_learn_motivation
    "col_mind" -> Res.drawable.illus_learn_focus
    else -> Res.drawable.illus_learn_hero
}
