package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import coil3.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChatCircle

/**
 * Why this goal exists, in the coach's voice.
 *
 * Replaces two cards that were saying the same thing in different places: the AI reasoning near the
 * top, and a full coach profile at the bottom of the page. The profile introduced the coach from
 * scratch on every visit to every goal in that category — name, job title, tagline, a fun fact —
 * which reads as a business card stapled next to the goal rather than a coach involved in it. Once
 * you have met Morgan, being introduced to Morgan is furniture.
 *
 * So the coach becomes the byline on the thing they actually have to say, and the introduction
 * collapses to an avatar and a name. Everything else about them is a tap away rather than in the
 * way.
 */
@Composable
internal fun CoachWhyCard(
    coach: CoachPersona,
    /** The coach's read on where the goal stands today. See [CoachGoalRead]. */
    read: String,
    /** Why the goal was set, from when it was created. Static, and second to the live read. */
    reasoning: String?,
    onMeetCoach: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = try {
        Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val accentColor = try {
        Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.tertiary
    }

    var expanded by remember { mutableStateOf(false) }

    GlassCard(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = reasoning != null) { expanded = !expanded }
                .padding(LifePlannerDesign.Padding.standard)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(bgColor, accentColor.copy(alpha = 0.7f))
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (coach.imageUrl != null) {
                        AsyncImage(
                            model = coach.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Text(coach.emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        // The heading is where the goal stands, not the coach's name. The coach is
                        // who is telling you.
                        "Where this stands",
                        style = MaterialTheme.typography.titleMedium
                            .copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${coach.name} · ${coach.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = bgColor,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = bgColor.copy(alpha = 0.12f),
                    modifier = Modifier.clickable(onClick = onMeetCoach),
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            PhosphorIcons.Regular.ChatCircle,
                            contentDescription = null,
                            tint = bgColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            "Chat",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = bgColor,
                        )
                    }
                }
            }

            // The live read leads, because it is the part that is different today.
            Text(
                read,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // The reasoning from when the goal was written is worth keeping, but it never changes,
            // so it sits behind a tap rather than competing with what is true now.
            if (reasoning != null) {
                Text(
                    if (expanded) "Why you set it" else "Why you set it · tap",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = bgColor,
                )
                if (expanded) {
                    Text(
                        reasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
