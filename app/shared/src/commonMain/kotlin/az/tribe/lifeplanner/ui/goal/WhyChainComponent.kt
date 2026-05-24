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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Pillar 1 — renders the "Why-Chain" ladder for a goal: the [LifeValue] it serves
 * at the top (the why), down through the goal and its milestones (the what). When
 * the goal has no value linked, the top node becomes a gentle "What's this for?"
 * nudge (a prompt, not a blocker) that opens the value picker.
 */
@Composable
fun WhyChainComponent(
    valueTitle: String?,
    goalTitle: String,
    milestoneCount: Int = 0,
    onValueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                ChainNode(label = "Value", title = valueTitle, emphasized = true, onClick = onValueClick)
            } else {
                OrphanNudgeNode(onClick = onValueClick)
            }

            ChainConnector()
            ChainNode(label = "Goal", title = goalTitle, emphasized = false, onClick = null)

            if (milestoneCount > 0) {
                ChainConnector()
                ChainNode(
                    label = "Milestones",
                    title = if (milestoneCount == 1) "1 step" else "$milestoneCount steps",
                    emphasized = false,
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun ChainNode(
    label: String,
    title: String,
    emphasized: Boolean,
    onClick: (() -> Unit)?
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
