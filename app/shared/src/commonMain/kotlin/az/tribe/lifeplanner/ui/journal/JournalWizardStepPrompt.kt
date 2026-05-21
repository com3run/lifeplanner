package az.tribe.lifeplanner.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.JournalPrompts
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Quotes
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun PromptSelectionStep(
    mood: Mood,
    selectedPrompt: String?,
    onPromptSelected: (String) -> Unit
) {
    val recommendedPrompts = remember(mood) { JournalPrompts.getPromptsForMood(mood) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Recommended section
        item {
            Text(
                text = "Recommended for you",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        items(recommendedPrompts) { prompt ->
            PromptCard(
                prompt = prompt,
                isSelected = prompt == selectedPrompt,
                isRecommended = true,
                onClick = { onPromptSelected(prompt) }
            )
        }

        // All Prompts section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Prompts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Daily Reflection
        item {
            PromptCategoryHeader(emoji = "\uD83C\uDF05", title = "Daily Reflection")
        }
        items(JournalPrompts.dailyReflection.filter { it !in recommendedPrompts }) { prompt ->
            PromptCard(
                prompt = prompt,
                isSelected = prompt == selectedPrompt,
                isRecommended = false,
                onClick = { onPromptSelected(prompt) }
            )
        }

        // Goal Reflection
        item {
            PromptCategoryHeader(emoji = "\uD83C\uDFAF", title = "Goal Reflection")
        }
        items(JournalPrompts.goalReflection.filter { it !in recommendedPrompts }) { prompt ->
            PromptCard(
                prompt = prompt,
                isSelected = prompt == selectedPrompt,
                isRecommended = false,
                onClick = { onPromptSelected(prompt) }
            )
        }

        // Mood Exploration
        item {
            PromptCategoryHeader(emoji = "\uD83D\uDCAD", title = "Mood Exploration")
        }
        items(JournalPrompts.moodExploration.filter { it !in recommendedPrompts }) { prompt ->
            PromptCard(
                prompt = prompt,
                isSelected = prompt == selectedPrompt,
                isRecommended = false,
                onClick = { onPromptSelected(prompt) }
            )
        }

        // Weekly Review
        item {
            PromptCategoryHeader(emoji = "\uD83D\uDCCA", title = "Weekly Review")
        }
        items(JournalPrompts.weeklyReview) { prompt ->
            PromptCard(
                prompt = prompt,
                isSelected = prompt == selectedPrompt,
                isRecommended = false,
                onClick = { onPromptSelected(prompt) }
            )
        }

        // Write my own option
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { onPromptSelected("Free write") },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.PencilSimple,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Write my own",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Start with a blank page",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
internal fun PromptCategoryHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PromptCard(
    prompt: String,
    isSelected: Boolean,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isRecommended -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isRecommended) PhosphorIcons.Regular.Sparkle else PhosphorIcons.Regular.Quotes,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    PhosphorIcons.Regular.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
