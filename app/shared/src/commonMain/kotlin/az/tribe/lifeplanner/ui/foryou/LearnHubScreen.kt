package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Lock
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Learn hub. Your progress across the whole library, a personalized "Recommended for you" pick,
 * and every learning path with its own progress and a Continue jump to the next unread lesson.
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
            item(key = "progress") {
                ProgressHeader(state.level, state.levelTitle, state.totalXp, state.readCount, state.totalUnlocked)
            }

            if (state.recommended.isNotEmpty()) {
                item(key = "rec_label") { SectionLabel("Recommended for you") }
                items(state.recommended, key = { "rec_${it.id}" }) { bit ->
                    LessonCard(bit, read = bit.id in state.readIds, onOpen = { onOpen(bit.id) })
                }
            }

            if (state.collections.isNotEmpty()) {
                item(key = "paths_label") { SectionLabel("Learning paths") }
                items(state.collections, key = { it.collection.id }) { cui ->
                    CollectionCard(cui, readIds = state.readIds, onOpen = onOpen)
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(level: Int, levelTitle: String, totalXp: Int, read: Int, total: Int) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Icon(PhosphorIcons.Regular.BookOpen, contentDescription = null, tint = c.primary, modifier = Modifier.size(LifePlannerDesign.IconSize.medium))
                    Column {
                        Text("Your learning", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
                        if (levelTitle.isNotBlank()) {
                            Text("Level $level · $levelTitle", style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                        }
                    }
                }
                Surface(color = c.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.full)) {
                    Text(
                        "$totalXp XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.primary,
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Spacing.sm, vertical = 4.dp),
                    )
                }
            }
            Text(
                if (total == 0) "New lessons unlock as you level up." else "You've read $read of $total lessons.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else read.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = c.primary,
                trackColor = c.primary.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun LessonCard(bit: KnowledgeBit, read: Boolean, onOpen: () -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onOpen),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBox(bit.emoji, c.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(bit.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
                Text("${bit.readMin} min read", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            }
            if (read) {
                Icon(PhosphorIcons.Regular.CheckCircle, contentDescription = "Read", tint = c.success, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
            } else {
                Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
            }
        }
    }
}

@Composable
private fun CollectionCard(cui: CollectionUi, readIds: Set<String>, onOpen: (String) -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.Top) {
                Text(cui.collection.emoji, style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(cui.collection.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
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

            val nodes = remember(cui, readIds) { buildPathNodes(cui, readIds) }
            nodes.forEachIndexed { i, node ->
                PathRow(
                    node = node,
                    isFirst = i == 0,
                    isLast = i == nodes.lastIndex,
                    filledAbove = i > 0 && nodes[i - 1].done,
                    filledBelow = node.done,
                    onOpen = onOpen,
                )
            }
        }
    }
}

private enum class NodeState { READ, NEXT, UNREAD, LOCKED, REWARD_EARNED, REWARD_LOCKED }

private data class PathNodeData(
    val title: String,
    val subtitle: String?,
    val state: NodeState,
    val done: Boolean,
    val lessonId: String?,
)

/** Turns a collection into the ordered nodes of its path: lessons, a locked tail, then the badge. */
private fun buildPathNodes(cui: CollectionUi, readIds: Set<String>): List<PathNodeData> {
    val list = mutableListOf<PathNodeData>()
    cui.lessons.forEach { b ->
        val read = b.id in readIds
        val state = when {
            read -> NodeState.READ
            b.id == cui.nextUnreadId -> NodeState.NEXT
            else -> NodeState.UNREAD
        }
        list += PathNodeData(b.title, "${b.readMin} min read", state, read, b.id)
    }
    if (cui.lockedCount > 0) {
        list += PathNodeData("${cui.lockedCount} more unlock as you level up", null, NodeState.LOCKED, false, null)
    }
    list += PathNodeData(
        title = if (cui.isComplete) "Path complete" else "Finish to earn this path's badge",
        subtitle = if (cui.isComplete) "Badge earned" else null,
        state = if (cui.isComplete) NodeState.REWARD_EARNED else NodeState.REWARD_LOCKED,
        done = cui.isComplete,
        lessonId = null,
    )
    return list
}

@Composable
private fun PathRow(
    node: PathNodeData,
    isFirst: Boolean,
    isLast: Boolean,
    filledAbove: Boolean,
    filledBelow: Boolean,
    onOpen: (String) -> Unit,
) {
    val c = MaterialTheme.modernColors
    val rowMod = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        .let { if (node.lessonId != null) it.bouncyClickable { onOpen(node.lessonId) } else it }
    Row(rowMod, horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.fillMaxHeight().width(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Connector(visible = !isFirst, filled = filledAbove)
            NodeCircle(node.state)
            Connector(visible = !isLast, filled = filledBelow)
        }
        Column(Modifier.weight(1f).padding(vertical = LifePlannerDesign.Spacing.sm), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                node.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (node.state == NodeState.NEXT) FontWeight.Bold else FontWeight.Normal),
                color = when (node.state) {
                    NodeState.READ -> c.textSecondary
                    NodeState.LOCKED, NodeState.REWARD_LOCKED -> c.textTertiary
                    else -> c.textPrimary
                },
            )
            node.subtitle?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            }
        }
        when {
            node.state == NodeState.NEXT ->
                Text("Continue", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = c.primary)
            node.lessonId != null ->
                Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
        }
    }
}

@Composable
private fun ColumnScope.Connector(visible: Boolean, filled: Boolean) {
    val c = MaterialTheme.modernColors
    Box(
        Modifier.width(2.dp).weight(1f).background(
            when {
                !visible -> Color.Transparent
                filled -> c.primary
                else -> c.textTertiary.copy(alpha = 0.25f)
            }
        )
    )
}

@Composable
private fun NodeCircle(state: NodeState) {
    val c = MaterialTheme.modernColors
    val d = 28.dp
    when (state) {
        NodeState.READ -> SolidNode(d, c.success) {
            Icon(PhosphorIcons.Regular.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        NodeState.NEXT -> SolidNode(d, c.primary) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
        }
        NodeState.UNREAD -> Box(
            Modifier.size(d).clip(CircleShape).border(BorderStroke(2.dp, c.textTertiary.copy(alpha = 0.5f)), CircleShape)
        )
        NodeState.LOCKED -> SolidNode(d, c.textTertiary.copy(alpha = 0.2f)) {
            Icon(PhosphorIcons.Regular.Lock, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(14.dp))
        }
        NodeState.REWARD_EARNED -> SolidNode(d, c.primary) { Text("🏅", style = MaterialTheme.typography.labelLarge) }
        NodeState.REWARD_LOCKED -> SolidNode(d, c.textTertiary.copy(alpha = 0.2f)) { Text("🏅", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun SolidNode(size: Dp, color: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun EmojiBox(emoji: String, accent: androidx.compose.ui.graphics.Color) {
    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium), modifier = Modifier.fillMaxSize()) {}
        Text(emoji, style = MaterialTheme.typography.titleLarge)
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
