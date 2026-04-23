package az.tribe.lifeplanner.ui.coach

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.CoachPost
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.X

@Composable
internal fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

// ─── Horizontal story cover card ─────────────────────────────────────

@Composable
internal fun StoryCard(
    post: CoachPost,
    coachEmoji: String,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val categoryLabel = when (post.category) {
        "story" -> "Story"
        "tip" -> "Quick Tip"
        "reflection" -> "Reflection"
        "motivation" -> "Motivation"
        else -> "Post"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgColor, accentColor.copy(alpha = 0.7f))
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            // Decorative emoji watermark
            Text(
                text = post.emoji,
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .alpha(0.15f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = categoryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(coachEmoji, fontSize = 12.sp)
                        Text(
                            text = "${post.readTimeMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Full-screen manga-style story reader ────────────────────────────

@Composable
internal fun StoryReader(
    post: CoachPost,
    coachEmoji: String,
    coachName: String,
    bgColor: Color,
    accentColor: Color,
    onDismiss: () -> Unit,
    coachImageUrl: String? = null
) {
    // Split content into paragraphs (panels)
    val panels = remember(post) {
        post.content.split("\n\n").filter { it.isNotBlank() }
    }
    var currentPanel by remember { mutableStateOf(0) }
    val totalPanels = panels.size

    val panelAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "panelFade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40 && currentPanel < totalPanels - 1) {
                        currentPanel++
                    } else if (dragAmount > 40 && currentPanel > 0) {
                        currentPanel--
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Tap right half → next, left half → previous
                    if (offset.x > size.width / 2) {
                        if (currentPanel < totalPanels - 1) currentPanel++
                        else onDismiss()
                    } else {
                        if (currentPanel > 0) currentPanel--
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar: close button + progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress segments
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(totalPanels) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (index <= currentPanel) accentColor
                                    else Color.White.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(PhosphorIcons.Regular.X, "Close", modifier = Modifier.size(20.dp))
                }
            }

            // Title card (first panel gets the title)
            if (currentPanel == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(post.emoji, fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "by $coachName",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Speech bubble panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .alpha(panelAlpha)
            ) {
                // Speech bubble
                val bubbleColor = accentColor.copy(alpha = 0.12f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Rounded bubble background
                            drawRoundRect(
                                color = bubbleColor,
                                cornerRadius = CornerRadius(20.dp.toPx()),
                                size = size
                            )
                            // Small triangle pointer at bottom-left
                            val trianglePath = Path().apply {
                                moveTo(40.dp.toPx(), size.height)
                                lineTo(28.dp.toPx(), size.height + 12.dp.toPx())
                                lineTo(56.dp.toPx(), size.height)
                                close()
                            }
                            drawPath(trianglePath, color = bubbleColor)
                        }
                        .padding(20.dp)
                ) {
                    Text(
                        text = panels[currentPanel],
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 26.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Coach avatar + panel indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Coach avatar
                if (coachImageUrl != null) {
                    AsyncImage(
                        model = coachImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = bgColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(coachEmoji, fontSize = 22.sp)
                        }
                    }
                }
                Column {
                    Text(
                        text = coachName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${currentPanel + 1} of $totalPanels",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Tap hint
                Text(
                    text = if (currentPanel < totalPanels - 1) "Tap to continue" else "Tap to close",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Personality tag chip ─────────────────────────────────────────────────────

@Composable
internal fun PersonalityTag(trait: String, bgColor: Color, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = trait.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = bgColor
        )
    }
}

// ─── Specialties section ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SpecialtiesSection(
    specialties: List<String>,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Specialties",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            specialties.forEach { specialty ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        specialty,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = bgColor
                    )
                }
            }
        }
    }
}

// ─── Bio quote block ──────────────────────────────────────────────────────────

@Composable
internal fun QuoteBlock(text: String, accentColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "About",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(start = 20.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
        ) {
            Text(
                "“",
                style = MaterialTheme.typography.displayMedium,
                color = accentColor.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

// ─── Fun fact bubble ──────────────────────────────────────────────────────────

@Composable
internal fun FunFactBubble(text: String, accentColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Fun Fact",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(accentColor.copy(alpha = 0.1f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", fontSize = 22.sp)
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
