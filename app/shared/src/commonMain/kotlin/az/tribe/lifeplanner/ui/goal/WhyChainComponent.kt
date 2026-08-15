package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Pillar 1, renders the "Why-Chain" ladder for a goal: the [LifeValue] it serves
 * at the top (the why), down through the goal and its milestones (the what). When
 * the goal has no value linked, the top node becomes a gentle "What's this for?"
 * nudge (a prompt, not a blocker) that opens the value picker.
 *
 * The milestones node carries the goal's actual progress (a thin bar and the next step) and the
 * card closes with the coach's read on where the goal stands, as a paragraph with a byline. Those
 * used to be a hero banner stat and a card of their own; here the chain tells the whole story in
 * one place: why, what, how far, and what a person would say about it.
 */
@Composable
fun WhyChainComponent(
    valueTitle: String?,
    goalTitle: String,
    milestoneCount: Int = 0,
    onValueClick: () -> Unit,
    /**
     * The area's current score out of ten, when the user has one on record.
     *
     * This is the whole reason the why moved from a free-text value to a wheel area. "Serves Craft
     * & Career" is decoration; "Friends, 4/10, your lowest area" tells the user which of their
     * goals actually matters this week. Null before the wheel has been filled in.
     */
    areaScore: Double? = null,
    /** "your lowest" / "among your lowest" when this area is at the bottom, else null. */
    lowestNote: String? = null,
    milestonesDone: Int = 0,
    /** Title of the first unfinished step, shown under the milestones count. */
    nextStep: String? = null,
    /** The coach's read on where the goal stands (see CoachGoalRead), the card's closing text. */
    coachRead: String? = null,
    coachName: String? = null,
    coachTitle: String? = null,
    onChat: (() -> Unit)? = null,
    /** Why the goal was set, from creation. Static, so it sits behind a tap. */
    reasoning: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        // Self-pads like every other card on the goal detail list (GoalDescriptionCard,
        // ModernMilestonesCard); without this it is the one block running edge to edge.
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.modernColors.cardBackground
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "WHY-CHAIN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.modernColors.textSecondary
            )
            Spacer(Modifier.height(10.dp))

            if (valueTitle != null) {
                ChainNode(
                    label = "Life area",
                    title = valueTitle,
                    emphasized = true,
                    onClick = onValueClick,
                    trailing = areaScore?.let { score ->
                        val shown = if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()
                        if (lowestNote != null) "$shown/10 · $lowestNote" else "$shown/10"
                    },
                )
            } else {
                OrphanNudgeNode(onClick = onValueClick)
            }

            ChainConnector()
            ChainNode(label = "Goal", title = goalTitle, emphasized = false, onClick = null)

            if (milestoneCount > 0) {
                ChainConnector()
                ChainNode(
                    label = "Milestones",
                    title = when {
                        milestonesDone > 0 -> "$milestonesDone of $milestoneCount done"
                        milestoneCount == 1 -> "1 step"
                        else -> "$milestoneCount steps"
                    },
                    emphasized = false,
                    onClick = null,
                    trailing = nextStep?.let { "Next: $it" },
                    progress = if (milestonesDone > 0) milestonesDone / milestoneCount.toFloat() else null,
                )
            }

            if (coachRead != null || coachName != null || reasoning != null) {
                Spacer(Modifier.height(14.dp))
                if (coachRead != null) {
                    Text(
                        coachRead,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.modernColors.textPrimary,
                    )
                }
                var expanded by remember { mutableStateOf(false) }
                if (reasoning != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (expanded) "Why you set it" else "Why you set it · tap",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.modernColors.primary,
                        modifier = Modifier.clickable { expanded = !expanded },
                    )
                    if (expanded) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.modernColors.textSecondary,
                        )
                    }
                }
                if (coachName != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            listOfNotNull(coachName, coachTitle).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.modernColors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        if (onChat != null) {
                            Text(
                                "Chat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.modernColors.primary,
                                modifier = Modifier.clickable(onClick = onChat).padding(4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainNode(
    label: String,
    title: String,
    emphasized: Boolean,
    onClick: (() -> Unit)?,
    trailing: String? = null,
    progress: Float? = null,
) {
    val bg = if (emphasized) MaterialTheme.modernColors.primaryContainer else MaterialTheme.modernColors.surfaceVariant
    val titleColor = if (emphasized) MaterialTheme.modernColors.onPrimaryContainer else MaterialTheme.modernColors.textPrimary
    val labelColor = if (emphasized) MaterialTheme.modernColors.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.modernColors.textSecondary

    var rowModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(bg)
    if (onClick != null) rowModifier = rowModifier.clickable(onClick = onClick)

    Row(
        modifier = rowModifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = labelColor)
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor
            )
            if (trailing != null) {
                Text(
                    trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                )
            }
            if (progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.modernColors.primary,
                    trackColor = MaterialTheme.modernColors.primary.copy(alpha = 0.15f),
                )
            }
        }
        if (onClick != null) {
            Text(
                "Change",
                style = MaterialTheme.typography.labelMedium,
                color = if (emphasized) MaterialTheme.modernColors.onPrimaryContainer else MaterialTheme.modernColors.primary
            )
        }
    }
}

@Composable
private fun OrphanNudgeNode(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.modernColors.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "What's this for?",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.modernColors.textPrimary
            )
            Text(
                "Link this goal to a life value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.modernColors.textSecondary
            )
        }
        Text("Link", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.modernColors.primary)
    }
}

@Composable
private fun ChainConnector() {
    Box(
        modifier = Modifier
            .padding(start = 22.dp, top = 4.dp, bottom = 4.dp)
            .width(2.dp)
            .height(16.dp)
            .background(MaterialTheme.modernColors.divider)
    )
}
