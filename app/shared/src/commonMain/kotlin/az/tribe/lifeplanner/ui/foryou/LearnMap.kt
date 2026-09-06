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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_read

/**
 * The map itself: a zone's heading, its winding trail, and the stops on it.
 *
 * Lifted out of the hub screen when the Present tab started drawing the same map. One copy of it,
 * because two trails that drift apart are two different games, and the user is playing one.
 */

@Composable
internal fun ZoneHeader(cui: CollectionUi) {
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

/** A finished zone, folded to one line with its badge. Tap to walk it again. */
@Composable
internal fun ClearedZoneRow(cui: CollectionUi, expanded: Boolean, onToggle: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onToggle),
        color = c.successContainer,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = LifePlannerDesign.Padding.cardContent, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🏅", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    cui.collection.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = c.onSuccessContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Cleared · all ${cui.total} lessons",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSuccessContainer,
                )
            }
            Text(
                if (expanded) "Hide" else "Revisit",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = c.onSuccessContainer,
            )
        }
    }
}

internal enum class NodeState { READ, NEXT, UNREAD, LOCKED, REWARD_EARNED, REWARD_LOCKED }

internal data class TrailStop(
    val label: String,
    val emoji: String,
    val state: NodeState,
    val done: Boolean,
    val lessonId: String?,
)

/** Lessons in order, then the locked ground still ahead, then the trophy at the end of the zone. */
internal fun buildTrail(cui: CollectionUi, readIds: Set<String>): List<TrailStop> = buildList {
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

internal val NODE_SIZE = 64.dp
internal val ROW_HEIGHT = 128.dp
internal val TRAIL_AMPLITUDE = 76.dp

/**
 * How far off centre each stop sits, as a fraction of [TRAIL_AMPLITUDE]. A full sine period over
 * eight stops, so the trail curves rather than zig-zagging.
 */
internal val SWAY = listOf(0f, 0.7f, 1f, 0.7f, 0f, -0.7f, -1f, -0.7f)

internal fun swayAt(index: Int): Float = SWAY[index % SWAY.size]

/** The winding trail for one zone: a drawn path with the stops sitting on it. */
@Composable
internal fun ZoneTrail(cui: CollectionUi, readIds: Set<String>, onOpen: (String) -> Unit) {
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
internal fun TrailNode(stop: TrailStop, modifier: Modifier = Modifier, onOpen: (String) -> Unit) {
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
                            contentDescription = stringResource(Res.string.cd_read),
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

/** The illustration for each zone (falls back to the generic learning art). */
internal fun pathIllustration(collectionId: String): DrawableResource = when (collectionId) {
    "col_habits" -> Res.drawable.illus_learn_habits
    "col_motivation" -> Res.drawable.illus_learn_motivation
    "col_mind" -> Res.drawable.illus_learn_focus
    else -> Res.drawable.illus_learn_hero
}

