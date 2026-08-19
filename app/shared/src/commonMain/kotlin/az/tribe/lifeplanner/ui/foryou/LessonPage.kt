package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.X

/**
 * The lesson at the stop you just tapped, opened where you are standing on the map.
 *
 * Reading used to mean leaving: the trail was here and the words were on another screen, so every
 * lesson cost you your place. Opening it under the node keeps the map on screen, and closing it
 * puts you back exactly where you were rather than at the top of a page.
 */
@Composable
fun LessonPage(
    lesson: KnowledgeBit,
    pathTitle: String,
    position: Int,
    total: Int,
    read: Boolean,
    onComplete: () -> Unit,
    onClose: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val accent = if (read) c.success else c.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = c.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(color = accent.copy(alpha = 0.14f), shape = CircleShape) {
                    Text(
                        lesson.emoji,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        pathTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Stop $position of $total · ${lesson.readMin} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                if (read) {
                    Icon(
                        PhosphorIcons.Fill.CheckCircle,
                        contentDescription = "Read",
                        tint = accent,
                        modifier = Modifier.size(LifePlannerDesign.IconSize.medium),
                    )
                }
                Icon(
                    PhosphorIcons.Regular.X,
                    contentDescription = "Close lesson",
                    tint = c.textTertiary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClickable(onClick = onClose)
                        .padding(4.dp)
                        .size(LifePlannerDesign.IconSize.small),
                )
            }

            Text(
                lesson.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )

            // The tick only appears while the lesson is unread: a lesson cannot be finished twice,
            // and offering to pay the XP again would be a lie about what the button does.
            LessonBody(
                lesson = lesson,
                accent = accent,
                onComplete = if (read) null else onComplete,
                completeLabel = "Got it · +${XpRewards.LESSON_READ} XP",
            )
        }
    }
}

/**
 * The stop, opened over the map. The trail stays visible behind it, so finishing a lesson and
 * watching the node light up happen in the same place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonSheet(
    lesson: KnowledgeBit,
    pathTitle: String,
    position: Int,
    total: Int,
    read: Boolean,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                .padding(bottom = 32.dp),
        ) {
            LessonPage(
                lesson = lesson,
                pathTitle = pathTitle,
                position = position,
                total = total,
                read = read,
                onComplete = onComplete,
                onClose = onDismiss,
            )
        }
    }
}

/**
 * The bottom of the map: a quiet line while the next zone is coming, and an honest one when the
 * ground runs out. "That is every zone" is a better ending than a spinner that never resolves.
 */
@Composable
fun StreamFooter(atEnd: Boolean) {
    val c = MaterialTheme.modernColors
    Text(
        if (atEnd) "That is the whole map at your level. New ground opens as you level up." else "Unrolling the next zone...",
        style = MaterialTheme.typography.bodySmall,
        color = c.textTertiary,
        modifier = Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.md),
    )
}
