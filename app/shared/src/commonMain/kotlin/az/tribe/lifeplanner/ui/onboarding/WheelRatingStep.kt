package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.ui.wheel.scoreColor
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * Sign-up's opening question: how is each part of your life going, out of ten?
 *
 * This replaces "which areas of life matter most to you right now? Select at least 3" — an abstract
 * question asked from a blank page before the user has seen anything the app does, whose answer told
 * us almost nothing. A tap per area is less work and produces the user's own wheel, so the app has
 * something true to say from the first launch instead of nine invented fives.
 *
 * Every row carries its rubric, because a 7 has to mean roughly the same thing to everyone or the
 * numbers cannot be compared to each other, let alone to next month's.
 *
 * Skipping is allowed and skipping individual rows is allowed. Forcing all nine is how you get
 * people tapping through at random, which is worse data than an honest gap.
 */
@Composable
fun WheelRatingStep(
    ratings: Map<WheelArea, Double>,
    onRate: (WheelArea, Double) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val haptic = rememberHapticManager()
    val areas = WheelArea.segments()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        Text(
            "${ratings.size} of ${areas.size} rated",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        areas.forEach { area ->
            AreaRatingRow(
                area = area,
                score = ratings[area],
                onRate = { haptic.click(); onRate(area, it) },
            )
        }

        AppButton(
            text = if (ratings.isEmpty()) "Continue" else "That's my wheel",
            onClick = onContinue,
            variant = AppButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Skip for now",
            onClick = onSkip,
            variant = AppButtonVariant.TERTIARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AreaRatingRow(area: WheelArea, score: Double?, onRate: (Double) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(area.emoji, style = MaterialTheme.typography.titleMedium)
                Text(
                    area.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp).weight(1f),
                )
                if (score != null) {
                    Text(
                        if (score % 1.0 == 0.0) "${score.toInt()}" else "$score",
                        style = MaterialTheme.typography.titleMedium
                            .copy(fontWeight = FontWeight.Bold),
                        color = scoreColor(score),
                    )
                }
            }

            // A 10 means the same thing for everyone or the number is just a mood.
            Text(
                area.rubric,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (1..10).forEach { value ->
                    val filled = score != null && value <= score
                    // The whole filled run takes the colour of the score chosen, so the bar reads
                    // as one answer rather than ten lit cells, and shifts as you move across it.
                    val color by animateColorAsState(
                        if (filled && score != null) scoreColor(score)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
                        label = "ratingCell",
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onRate(value.toDouble()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        // Only the ends are labelled. Ten numbers in a row is a ruler, not a scale
                        // anyone reads.
                        if (value == 1 || value == 10) {
                            Text(
                                "$value",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (filled) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
