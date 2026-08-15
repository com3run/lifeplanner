package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.onboarding.WheelRatingStep
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * The one-time offer to replace our guesses with the user's own numbers.
 *
 * Everyone who signed up before the wheel moved into registration is looking at a wheel we invented
 * for them, and nothing on screen says so. This says so, once, and offers the same nine questions
 * new users get.
 *
 * It is honest about whose numbers are on screen rather than selling a feature, because the whole
 * value of the wheel is that the user believes it is about them.
 */
@Composable
fun WheelSetupPromptCard(
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            Text(
                "This wheel is our guess",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "Every score here was predicted from your goals and habits. Nothing on it is yours "
                    + "yet. Nine taps fixes that, and everything the app says about your life gets "
                    + "more honest for it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            ) {
                AppButton(
                    text = "Rate mine",
                    onClick = onStart,
                    variant = AppButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Not now",
                    onClick = onDismiss,
                    variant = AppButtonVariant.TERTIARY,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * The nine questions, in a sheet, reusing exactly what sign-up asks.
 *
 * Ratings are held locally and committed on done, so a half-finished pass that the user backs out
 * of does not leave their wheel part ours and part theirs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelSetupSheet(
    initial: Map<WheelArea, Double>,
    onDone: (Map<WheelArea, Double>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var ratings by remember { mutableStateOf(initial) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 40.dp,
            ),
        ) {
            item {
                Column(Modifier.padding(bottom = LifePlannerDesign.Spacing.sm)) {
                    Text(
                        "How is each part going?",
                        style = MaterialTheme.typography.titleLarge
                            .copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Rough is fine. You can change any of it whenever.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                WheelRatingStep(
                    ratings = ratings,
                    onRate = { area, score -> ratings = ratings + (area to score) },
                    onContinue = { onDone(ratings) },
                    onSkip = onDismiss,
                )
            }
        }
    }
}
