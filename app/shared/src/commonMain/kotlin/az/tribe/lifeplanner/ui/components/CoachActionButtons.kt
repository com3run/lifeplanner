package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.ui.chat.ChatGoalQuestionnaire
import kotlin.math.absoluteValue

/**
 * Displays coach suggestions as preview cards for goals/habits
 * and simple buttons for other actions.
 * Uses horizontal pager for multiple goal/habit suggestions.
 */
@Composable
fun CoachSuggestionButtons(
    suggestions: List<CoachSuggestion>,
    onExecute: (CoachSuggestion) -> Unit,
    isExecuting: Boolean = false,
    executedSuggestionIds: Set<String> = emptySet(),
    onAnswerQuestion: (String) -> Unit = {},
    goalQuestionnaire: ChatGoalQuestionnaire? = null,
    onAnswerGoalQuestion: (index: Int, answer: String) -> Unit = { _, _ -> },
    onSubmitQuestionnaire: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    val cardSuggestions = suggestions.filter {
        it is CoachSuggestion.CreateGoal || it is CoachSuggestion.CreateHabit
    }
    val otherSuggestions = suggestions.filter {
        it !is CoachSuggestion.CreateGoal && it !is CoachSuggestion.CreateHabit
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (cardSuggestions.isNotEmpty()) {
            if (cardSuggestions.size > 1) {
                SuggestionsPager(
                    suggestions = cardSuggestions,
                    executedSuggestionIds = executedSuggestionIds,
                    isExecuting = isExecuting,
                    onExecute = onExecute
                )
            } else {
                val suggestion = cardSuggestions.first()
                val isAdded = executedSuggestionIds.contains(suggestion.id)
                when (suggestion) {
                    is CoachSuggestion.CreateGoal -> {
                        val activeQuestionnaire = goalQuestionnaire?.takeIf {
                            it.forSuggestionId == suggestion.id && !it.submitted
                        }
                        if (activeQuestionnaire != null) {
                            GoalQuestionnaireCard(
                                questionnaire = activeQuestionnaire,
                                onAnswerQuestion = onAnswerGoalQuestion,
                                onSubmit = onSubmitQuestionnaire
                            )
                        } else {
                            GoalPreviewCard(
                                suggestion = suggestion,
                                onAdd = { onExecute(suggestion) },
                                isExecuting = isExecuting,
                                isAdded = isAdded
                            )
                        }
                    }
                    is CoachSuggestion.CreateHabit -> {
                        HabitPreviewCard(
                            suggestion = suggestion,
                            onAdd = { onExecute(suggestion) },
                            isExecuting = isExecuting,
                            isAdded = isAdded
                        )
                    }
                    else -> {}
                }
            }
        }

        otherSuggestions.forEach { suggestion ->
            val isAdded = executedSuggestionIds.contains(suggestion.id)
            when (suggestion) {
                is CoachSuggestion.CreateJournalEntry -> {
                    JournalPreviewCard(
                        suggestion = suggestion,
                        onAdd = { onExecute(suggestion) },
                        isExecuting = isExecuting,
                        isAdded = isAdded
                    )
                }
                is CoachSuggestion.CheckInHabit -> {
                    SuggestionActionButton(
                        suggestion = suggestion,
                        onClick = { onExecute(suggestion) },
                        enabled = !isExecuting && !isAdded,
                        isCompleted = isAdded
                    )
                }
                is CoachSuggestion.AskQuestion -> {
                    QuestionCard(
                        suggestion = suggestion,
                        onOptionSelected = { optionLabel ->
                            onAnswerQuestion(optionLabel)
                            onExecute(suggestion)
                        },
                        isExecuting = isExecuting,
                        isAnswered = isAdded
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun SuggestionsPager(
    suggestions: List<CoachSuggestion>,
    executedSuggestionIds: Set<String>,
    isExecuting: Boolean,
    onExecute: (CoachSuggestion) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { suggestions.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val suggestion = suggestions[page]
            val isAdded = executedSuggestionIds.contains(suggestion.id)
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val scale = lerp(0.9f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(0.5f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                    }
                    .fillMaxWidth()
            ) {
                when (suggestion) {
                    is CoachSuggestion.CreateGoal -> GoalPreviewCard(
                        suggestion = suggestion,
                        onAdd = { onExecute(suggestion) },
                        isExecuting = isExecuting,
                        isAdded = isAdded
                    )
                    is CoachSuggestion.CreateHabit -> HabitPreviewCard(
                        suggestion = suggestion,
                        onAdd = { onExecute(suggestion) },
                        isExecuting = isExecuting,
                        isAdded = isAdded
                    )
                    else -> {}
                }
            }
        }

        if (suggestions.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(suggestions.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}
