package az.tribe.lifeplanner.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CoachPost
import az.tribe.lifeplanner.domain.repository.CoachPostRepository
import az.tribe.lifeplanner.ui.components.SyncStatusIndicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachProfileScreen(
    coachId: String,
    onNavigateBack: () -> Unit,
    onStartChat: ((coachId: String) -> Unit)? = null,
    coachPostRepository: CoachPostRepository = koinInject(),
    syncManager: SyncManager = koinInject()
) {
    val coach = remember(coachId) { CoachPersona.getById(coachId) }
    val scope = rememberCoroutineScope()
    val postsFlow = remember { MutableStateFlow<List<CoachPost>>(emptyList()) }
    val posts by postsFlow.collectAsState()
    var selectedPost by remember { mutableStateOf<CoachPost?>(null) }

    LaunchedEffect(coachId) {
        Analytics.coachProfileViewed(coachId)
        postsFlow.value = coachPostRepository.getPostsForCoach(coachId)
    }

    val bgColor = try {
        Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val accentColor = try {
        Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.tertiary
    }

    val headerGradient = Brush.verticalGradient(
        colors = listOf(bgColor, accentColor, MaterialTheme.colorScheme.surface)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient header with avatar, name, title, category, sync
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top row: back button + sync indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    PhosphorIcons.Regular.ArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            SyncStatusIndicator(
                                syncStatus = syncManager.syncStatus,
                                onRetryClick = { scope.launch { syncManager.performFullSync() } },
                                compact = true,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        // Avatar circle
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            shadowElevation = 8.dp
                        ) {
                            if (coach.imageUrl != null) {
                                AsyncImage(
                                    model = coach.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = coach.emoji,
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = coach.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = coach.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = coach.category.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val isAvailable = remember { coach.isAvailableNow() }
                        val localTime = remember { coach.localTimeText() }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isAvailable) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                            )
                            Text(
                                text = if (isAvailable)
                                    "${coach.countryFlag} ${coach.city} · $localTime"
                                else
                                    "${coach.countryFlag} ${coach.city} · $localTime · Away",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (onStartChat != null) {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { onStartChat(coachId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    PhosphorIcons.Regular.ChatCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Chat with ${coach.name}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            // Bio
            item {
                Spacer(Modifier.height(8.dp))
                SectionCard(title = "About") {
                    Text(
                        text = coach.profile.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Specialties
            item {
                SectionCard(title = "Specialties") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        coach.specialties.forEach { specialty ->
                            AssistChip(
                                onClick = {},
                                label = { Text(specialty) }
                            )
                        }
                    }
                }
            }

            // Personality
            item {
                SectionCard(title = "Personality") {
                    Text(
                        text = coach.personality,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fun fact
            item {
                SectionCard(title = "Fun Fact") {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = coach.profile.funFact,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stories — horizontal manga-style cards
            if (posts.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                item {
                    Text(
                        text = "${coach.name}'s Stories",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.width(4.dp))
                        posts.forEach { post ->
                            StoryCard(
                                post = post,
                                coachEmoji = coach.emoji,
                                bgColor = bgColor,
                                accentColor = accentColor,
                                onClick = { selectedPost = post }
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }

        // Full-screen story reader overlay
        selectedPost?.let { post ->
            StoryReader(
                post = post,
                coachEmoji = coach.emoji,
                coachImageUrl = coach.imageUrl,
                coachName = coach.name,
                bgColor = bgColor,
                accentColor = accentColor,
                onDismiss = { selectedPost = null }
            )
        }
    }
}
