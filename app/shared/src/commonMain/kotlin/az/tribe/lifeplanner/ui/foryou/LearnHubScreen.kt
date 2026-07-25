package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
            item(key = "progress") { ProgressHeader(state.readCount, state.totalUnlocked) }

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
private fun ProgressHeader(read: Int, total: Int) {
    val c = MaterialTheme.modernColors
    Surface(Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Icon(PhosphorIcons.Regular.BookOpen, contentDescription = null, tint = c.primary, modifier = Modifier.size(LifePlannerDesign.IconSize.medium))
                Text("Your learning", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
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
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                LinearProgressIndicator(
                    progress = { cui.progress },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (cui.isComplete) c.success else c.primary,
                    trackColor = c.primary.copy(alpha = 0.15f),
                )
                Text(
                    if (cui.isComplete) "Done" else "${cui.readCount}/${cui.total}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (cui.isComplete) c.success else c.textSecondary,
                )
            }

            cui.lessons.forEach { bit ->
                LessonRow(
                    title = bit.title,
                    readMin = bit.readMin,
                    read = bit.id in readIds,
                    isNext = bit.id == cui.nextUnreadId,
                    onClick = { onOpen(bit.id) },
                )
            }
            if (cui.lockedCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                    Icon(PhosphorIcons.Regular.Lock, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
                    Text(
                        "${cui.lockedCount} more unlock as you level up",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(title: String, readMin: Int, read: Boolean, isNext: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Row(
        Modifier.fillMaxWidth().bouncyClickable(onClick = onClick).padding(vertical = LifePlannerDesign.Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (read) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.CaretRight,
            contentDescription = null,
            tint = if (read) c.success else if (isNext) c.primary else c.textTertiary,
            modifier = Modifier.size(LifePlannerDesign.IconSize.small),
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal),
            color = if (read) c.textSecondary else c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text("$readMin min", style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
    }
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
