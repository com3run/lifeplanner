package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * The coach's questions, offered rather than imposed.
 *
 * These used to be thirteen screens standing between registration and the app. The answers are
 * genuinely useful — they are what lets a coach say something specific rather than generic — but
 * nothing about them needs to happen before the user has seen what any of it is for. Asked here,
 * the user can see the app first and decide whether they want the app to know them better.
 *
 * Dismissable, and gone for good once dismissed. An offer that keeps coming back is not an offer.
 */
@Composable
fun CoachSetupCard(
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.modernColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            Text(
                "Want the coaching to fit you?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Text(
                // Honest about the trade rather than selling it: a few minutes, and what it buys.
                "A few questions about your work, your health and what you are dealing with. It "
                    + "takes a couple of minutes and it is the difference between advice that "
                    + "could be for anyone and advice that is for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            ) {
                AppButton(
                    text = "Answer them",
                    onClick = onStart,
                    variant = AppButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "No thanks",
                    onClick = onDismiss,
                    variant = AppButtonVariant.TERTIARY,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
