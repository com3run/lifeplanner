package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Story
import az.tribe.lifeplanner.ui.components.StoryReaderStore
import az.tribe.lifeplanner.ui.components.parseStoryColor
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

private const val SEEN_COACH_POSTS_KEY = "seen_coach_posts"

private val COACH_TIMESTAMPS = mapOf(
    "coach_tip_luna_balance"    to "2h ago",
    "coach_tip_alex_career"     to "Yesterday",
    "coach_tip_morgan_money"    to "2d ago",
    "coach_tip_kai_fitness"     to "3d ago",
    "coach_tip_sam_social"      to "4d ago",
    "coach_tip_river_wellness"  to "5d ago"
)

@Composable
fun CoachPostFeed(
    stories: List<Story>,
    onOpenReader: (initialIndex: Int) -> Unit,
    onStoryAction: (ctaAction: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stories.isEmpty()) return

    val settings: Settings = koinInject()

    var seenIds by remember {
        val saved = settings.getStringOrNull(SEEN_COACH_POSTS_KEY) ?: ""
        mutableStateOf(if (saved.isBlank()) emptySet() else saved.split(",").toSet())
    }

    val sortedStories = remember(stories, seenIds) {
        stories.sortedBy { if (it.id in seenIds) 1 else 0 }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            start = LifePlannerDesign.Padding.screenHorizontal,
            end = LifePlannerDesign.Padding.screenHorizontal
        )
    ) {
        sortedStories.forEachIndexed { index, story ->
            item(key = story.id) {
                val isSeen = story.id in seenIds
                CoachPostCard(
                    story = story,
                    isSeen = isSeen,
                    timestamp = COACH_TIMESTAMPS[story.id] ?: "This week",
                    onClick = {
                        StoryReaderStore.stories = sortedStories
                        StoryReaderStore.initialIndex = index
                        StoryReaderStore.seenIds = seenIds
                        StoryReaderStore.onMarkSeen = { storyId ->
                            val updated = seenIds + storyId
                            seenIds = updated
                            settings.putString(SEEN_COACH_POSTS_KEY, updated.joinToString(","))
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
private fun CoachPostCard(
    story: Story,
    isSeen: Boolean,
    timestamp: String,
    onClick: () -> Unit
) {
    val startColor = parseStoryColor(story.gradientStart)
    val endColor = parseStoryColor(story.gradientEnd)
    val alpha = if (isSeen) 0.45f else 1f

    Box(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
    ) {
        // Left accent strip
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(
                    if (isSeen)
                        Brush.verticalGradient(listOf(Color.Gray.copy(0.25f), Color.Gray.copy(0.25f)))
                    else
                        Brush.verticalGradient(listOf(startColor, endColor))
                )
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Coach identity row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Coach avatar circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    startColor.copy(alpha = if (isSeen) 0.25f else 0.35f),
                                    endColor.copy(alpha = if (isSeen) 0.25f else 0.35f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(story.emoji, fontSize = 16.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        story.label ?: "Coach",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                    Text(
                        timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.6f)
                    )
                }

                // Unread dot
                if (!isSeen) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(startColor)
                    )
                }
            }

            // Post title
            Text(
                story.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Post preview
            Text(
                story.subtitle.lines().first().take(80),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

