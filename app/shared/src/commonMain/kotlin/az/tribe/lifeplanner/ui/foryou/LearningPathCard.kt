package az.tribe.lifeplanner.ui.foryou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.LearningMomentum
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * The learn session as something the user is in the middle of, on the tab named after the middle
 * of things.
 *
 * The mechanics here are the ordinary ones games have used for decades, and they work because they
 * are honest rather than because they are clever: show the track and how far along it you are, name
 * the next thing so there is a reason to return, and say what finishing pays before it is earned
 * rather than after. Every number is read off the user's real position (see [LearningMomentum]).
 *
 * The one deliberate flourish is the current segment, which breathes. A static bar says "here is
 * your progress"; a bar with a live edge says "this is still going", which is the difference
 * between a record and an invitation.
 */
@Composable
fun LearningPathCard(
    state: LearningMomentum.State,
    lesson: KnowledgeBit?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onComplete: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val accent = state.badge?.let { Color(it.color) } ?: c.primary

    Surface(
        modifier = Modifier.fillMaxWidth().bouncyClickable(onClick = onToggle),
        color = accent.copy(alpha = 0.10f),
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
                Surface(color = accent.copy(alpha = 0.18f), shape = CircleShape) {
                    Text(
                        state.pathEmoji,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        state.pathTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Lesson ${state.position} of ${state.total} · ${state.readMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                // The reward is stated up front. Announcing it only after the fact makes it a
                // surprise; naming it first makes it a reason.
                Surface(color = accent.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "+${state.xp} XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            PathTrack(read = state.read, current = state.position - 1, total = state.total, accent = accent)

            Text(
                state.lessonTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Text(state.line, style = MaterialTheme.typography.bodySmall, color = accent)

            AnimatedVisibility(
                visible = expanded && lesson != null,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(180)),
            ) {
                lesson?.let {
                    LessonBody(
                        lesson = it,
                        accent = accent,
                        onComplete = onComplete,
                        // Naming the reward on the button too, because this is the moment it is paid.
                        completeLabel = "Finish lesson · +${state.xp} XP",
                    )
                }
            }

            if (!expanded) {
                state.upNextTitle?.let { next ->
                    Text(
                        if (state.upNextStartsNewPath) "Then a new path opens: $next" else "Up next: $next",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AppButton(
                    text = if (state.read == 0) "Start reading" else "Continue",
                    onClick = onToggle,
                    variant = AppButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * One segment per lesson: solid behind you, breathing where you are, faint ahead.
 *
 * A path long enough to need scrolling would make the segments too thin to read, so past twelve the
 * track falls back to a single proportional bar. Better one honest bar than sixteen slivers.
 */
@Composable
private fun PathTrack(read: Int, current: Int, total: Int, accent: Color) {
    val c = MaterialTheme.modernColors
    val pulse by rememberInfiniteTransition(label = "pathPulse").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "pathPulseAlpha",
    )

    if (total > 12) {
        val fraction by animateFloatAsState(
            targetValue = (read.toFloat() / total).coerceIn(0f, 1f),
            animationSpec = tween(420),
            label = "pathFraction",
        )
        Box(
            Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                .background(c.textTertiary.copy(alpha = 0.25f)),
        ) {
            Box(Modifier.fillMaxWidth(fraction).height(6.dp).background(accent))
        }
        return
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { i ->
            val done = i < read
            val isCurrent = i == current
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .alpha(if (isCurrent) pulse else 1f)
                    .background(
                        when {
                            done -> accent
                            isCurrent -> accent
                            else -> c.textTertiary.copy(alpha = 0.22f)
                        },
                    ),
            )
        }
    }
}
