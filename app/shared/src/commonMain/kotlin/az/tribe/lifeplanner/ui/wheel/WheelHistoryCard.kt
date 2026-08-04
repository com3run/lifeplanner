package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ComparisonPeriod
import az.tribe.lifeplanner.domain.model.WheelComparison
import az.tribe.lifeplanner.domain.model.WheelDelta
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * What moved since a past wheel.
 *
 * The empty states carry most of the weight here. "Nothing to compare against yet" and "nothing
 * moved" produce the same blank list but mean opposite things, and a new user will see the first
 * one for a week, so it has to explain itself rather than look broken.
 */
@Composable
fun WheelHistoryCard(
    comparison: WheelComparison?,
    period: ComparisonPeriod,
    snapshotCount: Int,
    isLoading: Boolean,
    onPeriodChange: (ComparisonPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            Text("What moved", style = MaterialTheme.typography.titleSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                ComparisonPeriod.entries.forEach { option ->
                    FilterChip(
                        selected = option == period,
                        onClick = { onPeriodChange(option) },
                        label = { Text(option.displayName) },
                    )
                }
            }

            when {
                isLoading -> Text(
                    text = "Reading your history…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                comparison == null -> Text(
                    // Said in days rather than "no data", because the user has not done anything
                    // wrong and there is nothing for them to fix. It just has not been long enough.
                    text = when (snapshotCount) {
                        0 -> "Nothing recorded yet. Set a score and we will start keeping track."
                        1 -> "Today is the first wheel on record. Come back tomorrow and this will " +
                            "start showing what changed."
                        else -> "No wheel from far enough back to compare ${option(period)}. " +
                            "$snapshotCount ${dayWord(snapshotCount)} on record so far."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                !comparison.hasMovement -> Text(
                    text = "Nothing moved ${option(period)}. Measured against ${comparison.previousDate}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> {
                    val overall = comparison.overallChange
                    Text(
                        text = buildString {
                            append(
                                when {
                                    overall > 0 -> "Your wheel is up ${formatScore(overall)}"
                                    overall < 0 -> "Your wheel is down ${formatScore(-overall)}"
                                    else -> "Your wheel is level overall"
                                }
                            )
                            append(", measured against ${comparison.previousDate}.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    comparison.risen.forEach { DeltaRow(it) }
                    comparison.fallen.forEach { DeltaRow(it) }
                }
            }
        }
    }
}

@Composable
private fun DeltaRow(delta: WheelDelta) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(areaColor(delta.area))
        )
        Text(
            text = delta.area.displayName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = LifePlannerDesign.Spacing.xs).weight(1f),
        )
        Text(
            text = "${formatScore(delta.from)} → ${formatScore(delta.to)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // The arrow carries the direction as well as the colour, so this still reads for anyone
        // who cannot separate the two greens and reds.
        Text(
            text = if (delta.rose) "  ↑ ${formatScore(delta.change)}" else "  ↓ ${formatScore(-delta.change)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (delta.rose) RoseColor else FellColor,
        )
    }
}

private fun option(period: ComparisonPeriod) = period.displayName

private fun dayWord(count: Int) = if (count == 1) "day" else "days"

private val RoseColor = Color(0xFF2AAF6E)
private val FellColor = Color(0xFFD9536B)
