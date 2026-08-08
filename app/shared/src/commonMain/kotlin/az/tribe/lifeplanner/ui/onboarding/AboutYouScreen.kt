package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.DotScale
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Everything setup still needs, on one screen.
 *
 * The old flow asked the same things as a conversation: thirteen screens, one question each, a
 * coach typing at you between them. It reads as friendly and behaves as an interrogation — you
 * cannot see how many are left, cannot answer them out of order, cannot answer two and leave, and
 * cannot tell what any of it is for until it is over.
 *
 * This is the same information as a form you can see the end of. Answer none of it, answer one, or
 * answer all of it and leave. Nothing is required, nothing blocks the button, and the button says
 * what happens rather than "Continue".
 */
@Composable
fun AboutYouScreen(
    name: String,
    age: Int?,
    stress: Int?,
    sleep: Int?,
    onName: (String) -> Unit,
    onAge: (Int?) -> Unit,
    onStress: (Int) -> Unit,
    onSleep: (Int) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
) {
    val c = MaterialTheme.modernColors

    LazyColumn(
        // Insets handled here rather than by whoever routes to it. The first version took a
        // topPadding the nav graph never passed, so the title rendered underneath the clock.
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = LifePlannerDesign.Padding.screenHorizontal,
            end = LifePlannerDesign.Padding.screenHorizontal,
            top = LifePlannerDesign.Spacing.md,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs)) {
                Text(
                    "A bit about you",
                    style = MaterialTheme.typography.headlineSmall
                        .copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                )
                Text(
                    // Says what it is for and that it is optional, in the place the user decides
                    // whether to bother.
                    "All optional. It only changes how your coach talks to you, and you can come "
                        + "back to it whenever.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }

        item {
            SetupCard("What should we call you?") {
                OutlinedTextField(
                    value = name,
                    onValueChange = onName,
                    placeholder = { Text("Your first name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                )
            }
        }

        item {
            SetupCard("How old are you?") {
                OutlinedTextField(
                    value = age?.toString().orEmpty(),
                    // Digits only, and an empty field means "not saying" rather than zero.
                    onValueChange = { text ->
                        val digits = text.filter { it.isDigit() }.take(3)
                        onAge(digits.toIntOrNull()?.takeIf { it in 1..120 })
                    },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                )
            }
        }

        item {
            SetupCard("How stressed have you been?", value = stress, lowLabel = "Calm", highLabel = "Frazzled") {
                DotScale(
                    score = stress?.toDouble(),
                    onRate = { onStress(it.toInt()) },
                    // Inverted on purpose: a high stress score is the bad end, so the ramp has to
                    // run the other way or a 9 would come up reassuringly green.
                    color = { az.tribe.lifeplanner.ui.wheel.scoreColorForStress(it) },
                )
            }
        }

        item {
            SetupCard("How well are you sleeping?", value = sleep, lowLabel = "Badly", highLabel = "Well") {
                DotScale(
                    score = sleep?.toDouble(),
                    onRate = { onSleep(it.toInt()) },
                    color = { az.tribe.lifeplanner.ui.wheel.scoreColor(it) },
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs)) {
                AppButton(
                    text = "Save",
                    onClick = onDone,
                    variant = AppButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = "Not now",
                    onClick = onSkip,
                    variant = AppButtonVariant.TERTIARY,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SetupCard(
    title: String,
    value: Int? = null,
    lowLabel: String? = null,
    highLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val c = MaterialTheme.modernColors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = c.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                )
                if (value != null) {
                    Text(
                        "$value",
                        style = MaterialTheme.typography.bodyLarge
                            .copy(fontWeight = FontWeight.Bold),
                        color = c.textPrimary,
                    )
                }
            }
            content()
            // The ends of a scale need naming, or a 7 is just a number the user has to guess at.
            if (lowLabel != null && highLabel != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(lowLabel, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                    Text(highLabel, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
                }
            }
        }
    }
}
