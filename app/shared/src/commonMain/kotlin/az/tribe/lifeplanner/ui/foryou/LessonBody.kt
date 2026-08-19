package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * A lesson, read where it was offered: the paragraphs, the one thing to try, the source, and the
 * button that finishes it.
 *
 * Shared by the feed card and the path card so a lesson looks the same wherever the reader met it.
 * Two copies of this drifted apart the moment one of them gained a button.
 */
@Composable
internal fun LessonBody(
    lesson: KnowledgeBit,
    accent: Color,
    onComplete: (() -> Unit)?,
    completeLabel: String = "Got it",
) {
    val c = MaterialTheme.modernColors
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        lesson.detail.forEach { paragraph ->
            Text(paragraph, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
        if (lesson.takeaway.isNotBlank()) {
            Surface(
                color = accent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
            ) {
                Text(
                    "Try it: ${lesson.takeaway}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textPrimary,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
            }
        }
        lesson.source?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
        }
        if (onComplete != null) {
            AppButton(
                text = completeLabel,
                onClick = onComplete,
                variant = AppButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
