package az.tribe.lifeplanner.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ChatMessage
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.domain.model.MessageRole
import az.tribe.lifeplanner.ui.components.OfflineBanner
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.PaperPlaneRight
import com.adamglin.phosphoricons.regular.Plus

@Composable
fun ChatContent(
    messages: List<ChatMessage>,
    isSending: Boolean,
    isStreaming: Boolean = false,
    streamingText: String? = null,
    isExecutingAction: Boolean,
    onSendMessage: (String) -> Unit,
    onExecuteSuggestion: (CoachSuggestion) -> Unit,
    executedSuggestionIds: Set<String> = emptySet(),
    isCouncilMode: Boolean = false,
    isOffline: Boolean = false,
    coach: CoachPersona? = null,
    goalQuestionnaire: ChatGoalQuestionnaire? = null,
    onAnswerGoalQuestion: (index: Int, answer: String) -> Unit = { _, _ -> },
    onSubmitQuestionnaire: () -> Unit = {},
    onCopyMessage: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    var revealedMessageCount by remember { mutableStateOf(messages.size) }

    LaunchedEffect(messages.size, isCouncilMode) {
        if (isCouncilMode && messages.size > revealedMessageCount) {
            val newMessages = messages.drop(revealedMessageCount)
            for ((index, _) in newMessages.withIndex()) {
                revealedMessageCount = revealedMessageCount + 1
                if (index < newMessages.size - 1) {
                    kotlinx.coroutines.delay((500..1000).random().toLong())
                }
            }
        } else if (messages.size < revealedMessageCount) {
            revealedMessageCount = messages.size
        } else if (!isCouncilMode) {
            revealedMessageCount = messages.size
        }
    }

    LaunchedEffect(revealedMessageCount) {
        if (revealedMessageCount > 0) listState.animateScrollToItem(0)
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val keyboardDismissConnection = remember(keyboardController) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                return Offset.Zero
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Once per coach, ever. The card is an introduction — name, tagline, a fun fact — which is
        // worth reading the first time you meet someone and is furniture every time after. It used
        // to reappear on every visit to an empty chat, because "dismissed" only lived as long as
        // the composable did.
        val chatSettings: Settings = koinInject()
        val introKey = coach?.id?.let { "coach_intro_seen_$it" }
        var storyDismissed by remember(introKey) {
            mutableStateOf(introKey != null && chatSettings.getBoolean(introKey, false))
        }

        val showStory = messages.isEmpty() && !storyDismissed
        // Marked on display rather than on the button: backing out without tapping Start chatting
        // still means you have met them.
        LaunchedEffect(showStory, introKey) {
            if (showStory && introKey != null) chatSettings.putBoolean(introKey, true)
        }

        if (showStory) {
            CoachStoryIntro(
                coach = coach,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onStartChat = {
                    storyDismissed = true
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            )
        } else if (messages.isEmpty()) {
            // Been here before, nothing said yet. The full introduction would be furniture, but a
            // blank screen does not tell you whose chat you are in either.
            CoachQuietIntro(coach = coach, modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
            val visibleMessages = if (isCouncilMode) messages.take(revealedMessageCount) else messages
            val reversedMessages = remember(visibleMessages) { visibleMessages.reversed() }

            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(keyboardDismissConnection),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isStreaming && !streamingText.isNullOrEmpty()) {
                    item(key = "streaming_bubble") {
                        StreamingMessageBubble(text = streamingText)
                    }
                }

                items(reversedMessages, key = { it.id }) { message ->
                    val suggestions = remember(message.metadata) {
                        if (message.role == MessageRole.ASSISTANT) {
                            message.metadata?.coachSuggestions ?: emptyList()
                        } else emptyList()
                    }
                    MessageBubble(
                        message = message,
                        suggestions = suggestions,
                        onExecuteSuggestion = onExecuteSuggestion,
                        isExecutingAction = isExecutingAction,
                        executedSuggestionIds = executedSuggestionIds,
                        onAnswerQuestion = onSendMessage,
                        onCopyMessage = onCopyMessage,
                        goalQuestionnaire = goalQuestionnaire,
                        onAnswerGoalQuestion = onAnswerGoalQuestion,
                        onSubmitQuestionnaire = onSubmitQuestionnaire
                    )
                }
            }

            LaunchedEffect(messages.lastOrNull()?.id, isStreaming, streamingText) {
                listState.animateScrollToItem(0)
            }

            val lastMessage = messages.lastOrNull()
            LaunchedEffect(lastMessage?.id) {
                if (lastMessage != null &&
                    lastMessage.role == MessageRole.ASSISTANT &&
                    !lastMessage.metadata?.coachSuggestions.isNullOrEmpty()
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            }
        }

        OfflineBanner(isOffline = isOffline)

        ChatInputField(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    onSendMessage(inputText)
                    inputText = ""
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            },
            isSending = isSending,
            isStreaming = isStreaming,
            isOffline = isOffline,
            coachName = coach?.name ?: "Luna",
            focusRequester = focusRequester
        )
    }
}

@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    isStreaming: Boolean = false,
    isOffline: Boolean = false,
    coachName: String = "Luna",
    focusRequester: FocusRequester? = null
) {
    val isDisabled = isSending || isOffline
    val isThinking = isSending || isStreaming
    val hasText = value.isNotBlank()

    val thinkingPhrases = remember {
        listOf(
            "Thinking...", "Reflecting...", "Considering your goals...",
            "Finding the best approach...", "Crafting a response..."
        )
    }
    var phraseIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isThinking) {
        if (isThinking) {
            phraseIndex = 0
            while (true) {
                kotlinx.coroutines.delay(2200)
                phraseIndex = (phraseIndex + 1) % thinkingPhrases.size
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    var wasSending by remember { mutableStateOf(false) }
    LaunchedEffect(isSending) {
        if (isSending) {
            wasSending = true
        } else if (wasSending && focusRequester != null) {
            wasSending = false
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topEnd = 28.dp, topStart = 28.dp))
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    if (isThinking) {
                        AnimatedContent(
                            targetState = thinkingPhrases[phraseIndex],
                            transitionSpec = {
                                (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                                (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
                            },
                            label = "placeholder_phrase"
                        ) { phrase ->
                            Text(
                                text = phrase,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = if (isOffline) "You're offline" else "Message $coachName...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (!isDisabled) onSend() }),
                    maxLines = 4,
                    enabled = !isOffline
                )
            }

            IconButton(
                onClick = { if (hasText && !isDisabled) onSend() },
                enabled = !isOffline,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (hasText && !isDisabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    androidx.compose.animation.Crossfade(targetState = hasText) { showSend ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (showSend) {
                                Icon(
                                    PhosphorIcons.Regular.PaperPlaneRight,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    PhosphorIcons.Regular.Plus,
                                    contentDescription = "More",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
