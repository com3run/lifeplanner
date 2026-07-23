package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.service.GoalJourneyNarrator
import az.tribe.lifeplanner.ui.components.GlassCard
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookOpen

/**
 * "Goal as a journal": a short story about where the user is on this goal, generated
 * locally by [GoalJourneyNarrator]. Reads like a journal page: chapter eyebrow, a few
 * warm sentences reflecting the current state, and a teaser of the next milestone
 * (with a more affectionate tone when the next step is the last one).
 */
@Composable
internal fun GoalJourneyCard(
    goal: Goal,
    modifier: Modifier = Modifier,
    // Screens whose list already insets content pass 0.dp; the legacy screen relies on 16.dp.
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp,
) {
    val journey = remember(goal.id, goal.status, goal.milestones) {
        GoalJourneyNarrator.narrate(goal)
    }
    val accent = when {
        journey.isComplete -> MaterialTheme.colorScheme.tertiary
        journey.isFinalStep -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    GlassCard(modifier = modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = PhosphorIcons.Regular.BookOpen,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "YOUR JOURNEY · ${journey.chapterLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = journey.story,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (journey.teaser != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Column(
                            modifier = Modifier
                                .width(3.dp)
                                .background(accent, RoundedCornerShape(2.dp))
                        ) { Spacer(Modifier.size(3.dp, 36.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = journey.teaserLabel ?: "Next in your story",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accent
                            )
                            Spacer(Modifier.size(2.dp))
                            Text(
                                text = journey.teaser,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
