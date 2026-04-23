package az.tribe.lifeplanner.ui.coach

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CoachPost
import az.tribe.lifeplanner.domain.repository.CoachPostRepository
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import coil3.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Lock
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ChatCircle
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoachProfileScreen(
    coachId: String,
    onNavigateBack: () -> Unit,
    onStartChat: ((coachId: String) -> Unit)? = null,
    coachPostRepository: CoachPostRepository = koinInject()
) {
    val coach = remember(coachId) { CoachPersona.getById(coachId) }
    val postsFlow = remember { MutableStateFlow<List<CoachPost>>(emptyList()) }
    val posts by postsFlow.collectAsState()
    var selectedPost by remember { mutableStateOf<CoachPost?>(null) }
    var showAuthSheet by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.authState.collectAsState()

    val gamificationViewModel: GamificationViewModel = koinViewModel()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val level = userProgress?.currentLevel ?: 1
    val chatUnlocked = coachId == CoachPersona.LUNA_ID || level >= 3

    val listState = rememberLazyListState()
    // Measured at runtime so parallax works for any screen width at 9:16 ratio.
    var heroHeightPx by remember { mutableStateOf(0f) }
    // Image is 40% taller than box → factor = 0.4 / 1.0 = 0.4.
    // At max scroll (heroHeightPx): translationY = -heroHeightPx * 0.4 → image bottom at heroHeightPx*1.4 - heroHeightPx*0.4 = heroHeightPx. No gap.
    val parallaxFactor = 0.4f

    val heroScrollOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset.toFloat()
                .takeIf { listState.firstVisibleItemIndex == 0 } ?: 0f
        }
    }

    // Toolbar fades in over the last 40% of the hero scroll, then stays visible
    val toolbarAlpha by remember {
        derivedStateOf {
            if (heroHeightPx <= 0f) return@derivedStateOf 0f
            if (listState.firstVisibleItemIndex > 0) 1f
            else ((heroScrollOffset / heroHeightPx - 0.6f) / 0.4f).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(coachId) {
        Analytics.coachProfileViewed(coachId)
        postsFlow.value = coachPostRepository.getPostsForCoach(coachId)
    }

    val bgColor = remember(coach) {
        try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }
    val accentColor = remember(coach) {
        try { Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF818CF8) }
    }

    val isAvailable = remember { coach.isAvailableNow() }
    val localTime = remember { coach.localTimeText() }
    val personalityTraits = remember(coach) {
        coach.personality.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coachPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "dotPulse"
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            // ── Parallax hero ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clipToBounds()
                        .onSizeChanged { if (it.height > 0) heroHeightPx = it.height.toFloat() }
                ) {
                    if (coach.imageUrl != null) {
                        AsyncImage(
                            model = coach.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(bgColor, accentColor))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(coach.emoji, fontSize = 88.sp)
                        }
                    }

                    // Deep bottom gradient for legible text
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.08f),
                                    0.35f to Color.Transparent,
                                    0.7f to Color.Black.copy(alpha = 0.45f),
                                    1.0f to Color.Black.copy(alpha = 0.82f)
                                )
                            )
                        )
                    )

                    // Back button
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 12.dp, top = 10.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft, "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Coach identity at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 48.dp)
                    ) {
                        Text(
                            coach.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            coach.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pulsing availability dot with glow ring
                            Box(contentAlignment = Alignment.Center) {
                                if (isAvailable) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Color(0xFF4CAF50).copy(alpha = (1f - pulseAlpha) * 0.5f)
                                            )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isAvailable) Color(0xFF4CAF50)
                                            else Color(0xFF9E9E9E)
                                        )
                                )
                            }
                            Text(
                                if (isAvailable) "${coach.countryFlag} ${coach.city} · $localTime"
                                else "${coach.countryFlag} ${coach.city} · Away",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // ── Personality traits ─────────────────────────────────────────────
            if (personalityTraits.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        personalityTraits.forEach { trait ->
                            PersonalityTag(trait, bgColor, accentColor)
                        }
                    }
                }
            }

            // ── About / Bio ────────────────────────────────────────────────────
            item {
                QuoteBlock(
                    text = coach.profile.bio,
                    accentColor = accentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ── Specialties ────────────────────────────────────────────────────
            item {
                SpecialtiesSection(
                    specialties = coach.specialties,
                    bgColor = bgColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // ── Fun Fact ───────────────────────────────────────────────────────
            item {
                FunFactBubble(
                    text = coach.profile.funFact,
                    accentColor = accentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ── Stories ────────────────────────────────────────────────────────
            if (posts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "${coach.name}'s Stories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.width(4.dp))
                        posts.forEach { post ->
                            StoryCard(post, coach.emoji, bgColor, accentColor) { selectedPost = post }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        // ── Sticky Start Chat button ───────────────────────────────────────────
        if (onStartChat != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        when {
                            authState is AuthState.Guest -> showAuthSheet = true
                            !chatUnlocked -> { /* locked — button shows level req, tap is no-op */ }
                            else -> onStartChat(coachId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (chatUnlocked) bgColor else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (chatUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (chatUnlocked) {
                        Icon(PhosphorIcons.Regular.ChatCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Start Chat with ${coach.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(PhosphorIcons.Fill.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Unlocks at Level 3",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ── Collapsing toolbar ────────────────────────────────────────────────
        if (toolbarAlpha > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .graphicsLayer { alpha = toolbarAlpha },
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        coach.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ── Story reader overlay ───────────────────────────────────────────────
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

        // ── Auth gate ──────────────────────────────────────────────────────────
        if (showAuthSheet) {
            AuthBottomSheet(
                isSignUp = authState is AuthState.Guest,
                authViewModel = authViewModel,
                authState = authState,
                onDismiss = { showAuthSheet = false },
                onSuccess = {
                    showAuthSheet = false
                    onStartChat?.invoke(coachId)
                }
            )
        }
    }
}
