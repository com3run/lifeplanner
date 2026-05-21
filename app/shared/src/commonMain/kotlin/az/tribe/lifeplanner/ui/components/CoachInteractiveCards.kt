package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Question
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.ui.chat.ChatGoalQuestionnaire

@Composable
internal fun JournalPreviewCard(
    suggestion: CoachSuggestion.CreateJournalEntry,
    onAdd: () -> Unit,
    isExecuting: Boolean,
    isAdded: Boolean = false
) {
    val journalColor = Color(0xFF9C6ADE)
    val journalGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF9C6ADE), Color(0xFF6A82FB))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(journalGradient))

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(journalGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = PhosphorIcons.Regular.PencilSimple, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(text = "New Journal Entry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (suggestion.mood != null) {
                            Text(text = suggestion.mood, style = MaterialTheme.typography.labelMedium, color = journalColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text(text = suggestion.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (suggestion.content.isNotBlank()) {
                    Text(text = suggestion.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                GradientActionButton(text = if (isAdded) "Added" else "Add Entry", gradient = journalGradient, onClick = onAdd, isExecuting = isExecuting, isAdded = isAdded)
            }
        }
    }
}

@Composable
internal fun QuestionCard(
    suggestion: CoachSuggestion.AskQuestion,
    onOptionSelected: (String) -> Unit,
    isExecuting: Boolean,
    isAnswered: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp).background(
                    Brush.horizontalGradient(listOf(Color(0xFF7A5AF8), Color(0xFF9C6ADE)))
                )
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = suggestion.question,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestion.options.forEach { option ->
                        Surface(
                            onClick = { onOptionSelected(option.label) },
                            enabled = !isExecuting && !isAnswered,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = option.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                                    option.description?.let { desc ->
                                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Icon(imageVector = PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SuggestionActionButton(
    suggestion: CoachSuggestion,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isCompleted: Boolean = false
) {
    val (icon, color) = when (suggestion) {
        is CoachSuggestion.CheckInHabit -> PhosphorIcons.Regular.Check to Color(0xFF4CAF50)
        else -> PhosphorIcons.Regular.Question to MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (isCompleted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = if (isCompleted) PhosphorIcons.Regular.CheckCircle else icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isCompleted) MaterialTheme.colorScheme.tertiary else color)
            Text(text = if (isCompleted) "Done" else suggestion.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = if (isCompleted) MaterialTheme.colorScheme.tertiary else color)
        }
    }
}

@Composable
fun GoalQuestionnaireCard(
    questionnaire: ChatGoalQuestionnaire,
    onAnswerQuestion: (index: Int, answer: String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAnswered = questionnaire.answers.isNotEmpty() &&
            questionnaire.answers.all { it.isNotEmpty() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(
                        Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00BCD4))), CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Column {
                    Text("Let me personalise your goal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Answer a few quick questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (questionnaire.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text("Generating questions…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (questionnaire.loadError || questionnaire.questions.isEmpty()) {
                Text(
                    "Couldn't generate questions. Check your connection and try again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Text(text = "Select all that apply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))

                questionnaire.questions.forEachIndexed { idx, q ->
                    if (idx > 0) Spacer(modifier = Modifier.height(14.dp))
                    val selectedAnswers = questionnaire.answers.getOrElse(idx) { emptyList() }

                    Text(text = "${idx + 1}. ${q.text}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        q.options.forEach { option ->
                            val isSelected = option in selectedAnswers
                            val isNoneOption = option.startsWith("None of the above", ignoreCase = true)
                            Surface(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onAnswerQuestion(idx, option) },
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isSelected && isNoneOption -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = when {
                                        isSelected && isNoneOption -> MaterialTheme.colorScheme.outline
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (isSelected) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
                                        contentDescription = null,
                                        tint = when {
                                            isSelected && isNoneOption -> MaterialTheme.colorScheme.outline
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isSelected && isNoneOption -> MaterialTheme.colorScheme.onSurfaceVariant
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                if (questionnaire.questions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onSubmit,
                        enabled = allAnswered,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate my goal →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
