package az.tribe.lifeplanner.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.ChatMessage
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.domain.model.MessageRole
import az.tribe.lifeplanner.ui.components.CoachSuggestionButtons
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Sparkle

@Composable
fun StreamingMessageBubble(text: String) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val styledText = parseMarkdownToAnnotatedString(text, textColor)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                PhosphorIcons.Regular.Sparkle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .animateContentSize(),
            shape = RoundedCornerShape(
                topStart = 4.dp, topEnd = 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp
            ),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = styledText,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    suggestions: List<CoachSuggestion> = emptyList(),
    onExecuteSuggestion: (CoachSuggestion) -> Unit = {},
    isExecutingAction: Boolean = false,
    executedSuggestionIds: Set<String> = emptySet(),
    onAnswerQuestion: (String) -> Unit = {},
    onCopyMessage: (String) -> Unit = {},
    goalQuestionnaire: ChatGoalQuestionnaire? = null,
    onAnswerGoalQuestion: (index: Int, answer: String) -> Unit = { _, _ -> },
    onSubmitQuestionnaire: () -> Unit = {}
) {
    val isUser = message.role == MessageRole.USER

    val (coachName, messageText) = remember(message.content) {
        val content = message.content
        if (!isUser && content.contains(": ")) {
            val colonIndex = content.indexOf(": ")
            val potentialCoach = content.substring(0, colonIndex)
            if (potentialCoach.length <= 10 && potentialCoach.first().isUpperCase() && !potentialCoach.contains(" ")) {
                Pair(potentialCoach, content.substring(colonIndex + 2))
            } else {
                Pair(null, content)
            }
        } else {
            Pair(null, content)
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Column(modifier = Modifier.widthIn(max = 280.dp)) {
                if (coachName != null && !isUser) {
                    Text(
                        text = coachName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                val bubbleTextColor = if (isUser) MaterialTheme.colorScheme.onPrimary
                                      else MaterialTheme.colorScheme.onSurfaceVariant
                val styledContent = parseMarkdownToAnnotatedString(messageText, bubbleTextColor)

                var showCopyButton by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .animateContentSize()
                        .then(
                            if (!isUser) Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { showCopyButton = !showCopyButton }
                            ) else Modifier
                        ),
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 16.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp, bottomEnd = 16.dp
                    ),
                    color = if (isUser) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = styledContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bubbleTextColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (!isUser) {
                    val cleanContent = remember(messageText) { stripMarkdown(messageText) }

                    if (showCopyButton) {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(3000)
                            showCopyButton = false
                        }
                    }

                    AnimatedVisibility(
                        visible = showCopyButton,
                        enter = fadeIn() + slideInVertically { -it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 }
                    ) {
                        Surface(
                            onClick = {
                                onCopyMessage(cleanContent)
                                showCopyButton = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Copy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Copy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isUser && suggestions.isNotEmpty()) {
            CoachSuggestionButtons(
                suggestions = suggestions,
                onExecute = onExecuteSuggestion,
                isExecuting = isExecutingAction,
                executedSuggestionIds = executedSuggestionIds,
                onAnswerQuestion = onAnswerQuestion,
                goalQuestionnaire = goalQuestionnaire,
                onAnswerGoalQuestion = onAnswerGoalQuestion,
                onSubmitQuestionnaire = onSubmitQuestionnaire,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

// ── Markdown utilities ─────────────────────────────────────────────────────────

internal fun stripMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace(Regex("__(.+?)__"), "$1")
        .replace(Regex("_(.+?)_"), "$1")
        .replace(Regex("~~(.+?)~~"), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .replace(Regex("^[-*]\\s+", RegexOption.MULTILINE), "• ")
        .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1")
        .trim()
}

@Composable
internal fun parseMarkdownToAnnotatedString(
    text: String,
    baseColor: Color
): androidx.compose.ui.text.AnnotatedString {
    val boldStyle = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    val codeStyle = androidx.compose.ui.text.SpanStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        background = baseColor.copy(alpha = 0.08f)
    )
    val headerStyle = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)

    return androidx.compose.ui.text.buildAnnotatedString {
        val cleaned = text
            .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1")
            .trim()

        val lines = cleaned.split("\n")
        lines.forEachIndexed { lineIndex, rawLine ->
            var line = rawLine
            val headerMatch = Regex("^(#{1,3})\\s+(.*)").find(line)
            if (headerMatch != null) {
                line = headerMatch.groupValues[2]
                pushStyle(headerStyle)
                appendInlineMarkdown(line, boldStyle, italicStyle, codeStyle)
                pop()
            } else if (line.matches(Regex("^\\s*[-*]\\s+.*"))) {
                val content = line.replace(Regex("^\\s*[-*]\\s+"), "")
                append("• ")
                appendInlineMarkdown(content, boldStyle, italicStyle, codeStyle)
            } else if (line.matches(Regex("^\\s*\\d+\\.\\s+.*"))) {
                val match = Regex("^\\s*(\\d+\\.)\\s+(.*)").find(line)
                if (match != null) {
                    append("${match.groupValues[1]} ")
                    appendInlineMarkdown(match.groupValues[2], boldStyle, italicStyle, codeStyle)
                } else {
                    appendInlineMarkdown(line, boldStyle, italicStyle, codeStyle)
                }
            } else {
                appendInlineMarkdown(line, boldStyle, italicStyle, codeStyle)
            }
            if (lineIndex < lines.size - 1) append("\n")
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineMarkdown(
    text: String,
    boldStyle: androidx.compose.ui.text.SpanStyle,
    italicStyle: androidx.compose.ui.text.SpanStyle,
    codeStyle: androidx.compose.ui.text.SpanStyle
) {
    val pattern = Regex("(\\*\\*(.+?)\\*\\*|\\*(.+?)\\*|`(.+?)`)")
    var lastIndex = 0
    pattern.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[2].isNotEmpty() -> { pushStyle(boldStyle); append(match.groupValues[2]); pop() }
            match.groupValues[3].isNotEmpty() -> { pushStyle(italicStyle); append(match.groupValues[3]); pop() }
            match.groupValues[4].isNotEmpty() -> { pushStyle(codeStyle); append(match.groupValues[4]); pop() }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}
