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
    // Cleared zones start folded; re-opening one is a deliberate act, and it survives leaving the
    // screen so re-reading a lesson doesn't collapse the trail under you on the way back.
    var expandedZones by rememberSaveable { mutableStateOf(emptySet<String>()) }

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

            // Zones you have finished fold away to a single line. Walking past cleared ground to
            // reach the trail you are actually on is the main thing wrong with a long map.
            state.collections.forEach { cui ->
                if (cui.isComplete) {
                    item(key = "zone_${cui.collection.id}") {
                        ClearedZoneRow(
                            cui = cui,
                            expanded = expandedZones.contains(cui.collection.id),
                            onToggle = {
                                expandedZones = if (expandedZones.contains(cui.collection.id)) {
                                    expandedZones - cui.collection.id
                                } else {
                                    expandedZones + cui.collection.id
                                }
                            },
                        )
                    }
                    if (expandedZones.contains(cui.collection.id)) {
                        item(key = "trail_${cui.collection.id}") {
                            ZoneTrail(cui = cui, readIds = state.readIds, onOpen = onOpen)
                        }
                    }
                } else {
                    item(key = "zone_${cui.collection.id}") {
                        ZoneHeader(cui)
                    }
                    item(key = "trail_${cui.collection.id}") {
                        ZoneTrail(cui = cui, readIds = state.readIds, onOpen = onOpen)
                    }
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
