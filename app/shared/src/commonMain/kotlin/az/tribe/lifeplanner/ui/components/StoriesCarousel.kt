package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Story
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.X
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val SEEN_STORIES_KEY = "seen_stories"

@Composable
fun StoriesCarousel(
    stories: List<Story>,
    onStoryAction: (ctaAction: String?) -> Unit,
    onOpenReader: (initialIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stories.isEmpty()) return

    val settings: Settings = koinInject()

    var seenIds by remember {
        val saved = settings.getStringOrNull(SEEN_STORIES_KEY) ?: ""
        mutableStateOf(if (saved.isBlank()) emptySet() else saved.split(",").toSet())
    }

    // Unseen first, then seen
    val sortedStories = remember(stories, seenIds) {
        stories.sortedBy { if (it.id in seenIds) 1 else 0 }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = LifePlannerDesign.Padding.screenHorizontal,
            end = LifePlannerDesign.Padding.screenHorizontal
        )
    ) {
        sortedStories.forEachIndexed { index, story ->
            item(key = story.id) {
                StoryCircle(
                    story = story,
                    isSeen = story.id in seenIds,
                    onClick = {
                        StoryReaderStore.stories = sortedStories
                        StoryReaderStore.initialIndex = index
                        StoryReaderStore.seenIds = seenIds
                        StoryReaderStore.onMarkSeen = { storyId ->
                            val updated = seenIds + storyId
                            seenIds = updated
                            settings.putString(SEEN_STORIES_KEY, updated.joinToString(","))
                        }
                        StoryReaderStore.onStoryAction = onStoryAction
                        onOpenReader(index)
                    }
                )
            }
        }
    }
}

@Composable
private fun StoryCircle(
    story: Story,
    isSeen: Boolean,
    onClick: () -> Unit
) {
    val startColor = parseStoryColor(story.gradientStart)
    val endColor = parseStoryColor(story.gradientEnd)

    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer ring, gradient if new, grey if seen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isSeen)
                            Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.35f), Color.Gray.copy(alpha = 0.35f)))
                        else
                            Brush.linearGradient(listOf(startColor, endColor))
                    )
            )
            // White gap
            Box(
                modifier = Modifier
                    .size(61.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
            // Inner emoji circle
            Box(
                modifier = Modifier
                    .size(57.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                startColor.copy(alpha = if (isSeen) 0.08f else 0.18f),
                                endColor.copy(alpha = if (isSeen) 0.08f else 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = story.emoji, fontSize = 26.sp)
            }
        }
        if (story.label != null) {
            Text(
                text = story.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isSeen) 0.4f else 0.85f),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun StoryFullReader(
    stories: List<Story>,
    initialIndex: Int,
    seenIds: Set<String>,
    onMarkSeen: (String) -> Unit,
    onStoryAction: (ctaAction: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val story = stories[currentIndex]

    // Mark as seen and reset progress each time we land on a new story
    LaunchedEffect(currentIndex) {
        progress = 0f
        if (stories[currentIndex].id !in seenIds) {
            onMarkSeen(stories[currentIndex].id)
        }
    }

    // Auto-advance: duration scales with subtitle length (250ms/word, clamped 3-7s)
    LaunchedEffect(currentIndex) {
        val wordCount = stories[currentIndex].subtitle.split(" ").size
        val durationMs = (wordCount * 250L).coerceIn(3000L, 7000L)
        val stepMs = 16L
        while (progress < 1f) {
            delay(stepMs)
            if (!isHolding) {
                progress = (progress + stepMs.toFloat() / durationMs).coerceAtMost(1f)
            }
        }
        if (currentIndex < stories.lastIndex) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    val startColor = parseStoryColor(story.gradientStart)
    val endColor = parseStoryColor(story.gradientEnd)

    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures { _, dragAmount ->
                    when {
                        dragAmount < -60 && currentIndex < stories.lastIndex -> { progress = 0f; currentIndex++ }
                        dragAmount > 60 && currentIndex > 0 -> { progress = 0f; currentIndex-- }
                        dragAmount < -60 -> onDismiss()
                    }
                }
            }
            .pointerInput(currentIndex) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    },
                    onTap = { offset ->
                        progress = 0f
                        if (offset.x < size.width / 2) {
                            if (currentIndex > 0) currentIndex--
                        } else {
                            if (currentIndex < stories.lastIndex) currentIndex++
                            else onDismiss()
                        }
                    }
                )
            }
    ) {

        // Gradient background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            startColor.copy(alpha = 0.35f),
                            endColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Progress segments + close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(onSurface.copy(alpha = 0.2f))
                        ) {
                            val fillFraction = when {
                                index < currentIndex -> 1f
                                index == currentIndex -> progress
                                else -> 0f
                            }
                            if (fillFraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fillFraction)
                                        .fillMaxHeight()
                                        .background(Brush.horizontalGradient(listOf(startColor, endColor)))
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = onSurface)
                ) {
                    Icon(PhosphorIcons.Regular.X, "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            // Story content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = story.emoji,
                    fontSize = 80.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = story.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                // Gradient divider
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.horizontalGradient(listOf(startColor, endColor)))
                )

                Text(
                    text = story.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }

            Spacer(Modifier.weight(1f))

            // CTA button + nav hint, sits outside the tap zones, no interference
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (story.ctaText != null) {
                    Button(
                        onClick = { onStoryAction(story.ctaAction) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = startColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = story.ctaText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = when {
                        isHolding -> "Release to continue"
                        currentIndex < stories.lastIndex -> "Hold to pause  ·  tap to skip"
                        else -> "Tap to close"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

internal fun parseStoryColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        Color(
            red = cleaned.substring(0, 2).toInt(16) / 255f,
            green = cleaned.substring(2, 4).toInt(16) / 255f,
            blue = cleaned.substring(4, 6).toInt(16) / 255f
        )
    } catch (_: Exception) {
        Color(0xFF6366F1)
    }
}
