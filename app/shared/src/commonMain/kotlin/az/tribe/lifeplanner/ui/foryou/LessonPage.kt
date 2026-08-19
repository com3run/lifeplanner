package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.service.LearningStream
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.CheckCircle

/**
 * One lesson, whole, as a stop in the scroll.
 *
 * The library used to be a teaser and a tap: a one-line card that opened a reader page, which meant
 * the decision to read came before any of the reading. Here the lesson is simply there, so the
 * scroll itself is the reading, and the only thing left to decide is whether to tick it.
 *
 * A lesson already read stays in the stream and says so. Hiding it would make the stream shorter
 * every time the reader used it, which is a strange thing to do to someone who is enjoying it.
 */
@Composable
fun LessonPage(
    entry: LearningStream.Entry,
    read: Boolean,
    onComplete: () -> Unit,
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
                        entry.lesson.emoji,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (read) "READ AGAIN · ${entry.pathTitle.uppercase()}" else entry.pathTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Lesson ${entry.position} of ${entry.total} · ${entry.lesson.readMin} min",
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
            }

            Text(
                entry.lesson.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )

            // The body carries the paragraphs, the one thing to try, and the source. The tick only
            // appears while the lesson is unread: a lesson cannot be finished twice, and offering
            // to pay the XP again would be a lie about what the button does.
            LessonBody(
                lesson = entry.lesson,
                accent = accent,
                onComplete = if (read) null else onComplete,
                completeLabel = "Got it · +${XpRewards.LESSON_READ} XP",
            )
        }
    }
}

/**
 * The bottom of the stream: a quiet line while more is coming, and an honest one when the library
 * is finished. "You have read everything" is a better ending than a spinner that never resolves.
 */
@Composable
fun StreamFooter(atEnd: Boolean) {
    val c = MaterialTheme.modernColors
    Text(
        if (atEnd) "That is every lesson unlocked at your level. More open as you level up." else "Loading more...",
        style = MaterialTheme.typography.bodySmall,
        color = c.textTertiary,
        modifier = Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.md),
    )
}
