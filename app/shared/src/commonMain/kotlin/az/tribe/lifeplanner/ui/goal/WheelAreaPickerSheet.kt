package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelScore
import az.tribe.lifeplanner.domain.service.GoalWheelAreaInferrer
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check

/**
 * Which part of your life this goal is for.
 *
 * Replaces the free-text value picker as the goal's why. The old list was whatever the user typed
 * at onboarding, so the same goal could be filed under wording that meant nothing six months later.
 * These are the nine wheel segments, and each one carries the score the user gave it, which is the
 * point: choosing an area here is choosing which number on your wheel this goal is meant to move.
 *
 * Joy is absent by design. It is a reading of the whole wheel rather than a slice with goals of its
 * own, so there is no number a goal could move by being tagged to it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelAreaPickerSheet(
    selected: WheelArea?,
    scores: List<WheelScore>,
    onSelect: (WheelArea) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val segments = scores.filter { it.area.isWheelSegment }
    // Two areas can tie for last. Calling one of them "your lowest" because it happens to sort
    // first claims a precision the numbers do not have. And when every area ties there is no
    // lowest at all — nine rows each marked "among your lowest" is the app inventing a problem
    // (same tie rule as the wheel's headline).
    val allTied = segments.isNotEmpty() && segments.all { it.score == segments.first().score }
    val lowestScore = if (allTied) null else segments.minOfOrNull { it.score }
    val lowestIsShared = lowestScore != null && segments.count { it.score == lowestScore } > 1

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // Everything scrolls together, header included. A LazyColumn nested under a Column inside
        // the sheet took its height from what was left over, which clipped the last area off the
        // bottom edge — visible, but with nothing left to tap. Money was unselectable.
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            item {
                Column(Modifier.padding(bottom = LifePlannerDesign.Spacing.sm)) {
                    Text(
                        "What is this goal for?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = c.textPrimary,
                    )
                    Text(
                        "Pick the part of your life it is meant to move. You can change it whenever.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                    )
                }
            }

            items(GoalWheelAreaInferrer.selectable.size) { index ->
                    val area = GoalWheelAreaInferrer.selectable[index]
                    val score = scores.firstOrNull { it.area == area }?.score
                    val isSelected = area == selected

                    Surface(
                        modifier = Modifier.fillMaxWidth().bouncyClickable { onSelect(area) },
                        color = if (isSelected) c.primaryContainer else c.surfaceVariant,
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(area.emoji, style = MaterialTheme.typography.titleMedium)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    area.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                        .copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) c.onPrimaryContainer else c.textPrimary,
                                )
                                if (score != null) {
                                    val shown =
                                        if (score % 1.0 == 0.0) score.toInt().toString()
                                        else score.toString()
                                    Text(
                                        // Naming the weakest area here turns the picker into a
                                        // reason to choose rather than a list to get past.
                                        when {
                                            score != lowestScore -> "$shown/10"
                                            lowestIsShared -> "$shown/10 · among your lowest"
                                            else -> "$shown/10 · your lowest right now"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) {
                                            c.onPrimaryContainer.copy(alpha = 0.8f)
                                        } else {
                                            c.textSecondary
                                        },
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    PhosphorIcons.Regular.Check,
                                    contentDescription = "Selected",
                                    tint = c.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
        }
    }
}
