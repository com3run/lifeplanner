package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * The wheel as it appears on Today: the shape, the face, and one honest line.
 *
 * Replaces the row of linear bars that used to sit here. Those bars showed the old *engagement*
 * score, so the feed and the wheel screen were answering the same question with different numbers
 * one tap apart. One instrument, shown twice at different sizes, is the point.
 *
 * The wheel is not interactive here. Dragging a slice this small would be fiddly and, worse, would
 * let someone rewrite their life scores by accident while scrolling a feed. Tapping opens the full
 * screen, where changing a score is a deliberate act.
 */
@Composable
fun WheelStripCard(
    report: WheelReport?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Draw it big, with its labels, as a thing to look at rather than a status line.
     *
     * The wheel is the one view in the app that shows a whole life at once, and at 88dp beside two
     * lines of text it was reading as a progress chip. Given room it does the job it was built for.
     */
    prominent: Boolean = false,
) {
    if (report == null || report.segments.isEmpty()) return

    if (prominent) {
        WheelProminentCard(report = report, onOpen = onOpen, modifier = modifier)
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContentLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                WheelCanvas(
                    scores = report.scores,
                    onAreaTap = { onOpen() },
                    compact = true,
                    modifier = Modifier.size(88.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = LifePlannerDesign.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WheelFace(
                        score = report.overall,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = formatScore(report.overall),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = LifePlannerDesign.Spacing.xs),
                    )
                    Text(
                        text = " / 10",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = wheelMood(report.overall),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // What to say next depends on how much we actually know. Naming the weakest area
                // when most of the wheel is a guess would be asserting something we cannot support.
                val unconfirmed = report.unconfirmed.size
                val detail = when {
                    unconfirmed >= report.segments.size ->
                        "Tap to tell us where you are."
                    unconfirmed > 0 ->
                        "$unconfirmed still to set."
                    report.spread >= 3.0 ->
                        // soleLowest: "furthest behind" is a claim about one area; when several
                        // tie for last the honest reading is that the low end is shared.
                        report.soleLowest?.let { "${it.area.displayName} is furthest behind." }
                            ?: "A few areas are equally behind."
                    else -> "Fairly even across the board."
                }
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * The wheel given the space to be the wheel: full size, labelled, with the face and the number
 * under it rather than crammed alongside.
 */
@Composable
private fun WheelProminentCard(
    report: WheelReport,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContentLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            // Labels on, because at this size the point is being able to read which slice is which
            // without opening anything.
            WheelCanvas(
                scores = report.scores,
                onAreaTap = { onOpen() },
                compact = false,
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelFace(
                    score = report.overall,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatScore(report.overall),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = LifePlannerDesign.Spacing.xs),
                )
                Text(
                    text = " / 10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = wheelMood(report.overall),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val unconfirmed = report.unconfirmed.size
            val detail = when {
                unconfirmed >= report.segments.size -> "Tap to tell us where you are."
                unconfirmed > 0 -> "$unconfirmed still to set."
                report.spread >= 3.0 ->
                    // soleLowest, because "furthest behind" is a claim about one area; when
                    // several tie for last the honest reading is that the low end is shared.
                    report.soleLowest?.let { "${it.area.displayName} is furthest behind." }
                        ?: "A few areas are equally behind."
                else -> "Fairly even across the board."
            }
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
