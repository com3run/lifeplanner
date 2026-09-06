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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.repository.BuiltinCoachStore
import az.tribe.lifeplanner.domain.model.CoachPersona
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
import com.adamglin.phosphoricons.regular.Play
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoachProfileScreen(
    coachId: String,
    onNavigateBack: () -> Unit,
    onStartChat: ((coachId: String) -> Unit)? = null,
    onPlayIntro: ((coachId: String) -> Unit)? = null,
) {
    val storeCoaches by BuiltinCoachStore.coaches.collectAsState()
    val coach = remember(coachId, storeCoaches) { BuiltinCoachStore.getById(coachId) }
    var showAuthSheet by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()
    val gamificationViewModel: GamificationViewModel = koinViewModel()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val level = userProgress?.currentLevel ?: 1
    val chatUnlocked = coachId == CoachPersona.LUNA_ID || level >= 3

    LaunchedEffect(coachId) { Analytics.coachProfileViewed(coachId) }

    val bgColor = remember(coach.id) {
        try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }
    val accentColor = remember(coach.id) {
        try { Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF818CF8) }
    }
    val isAvailable = remember(coach.id) { coach.isAvailableNow() }
    val localTime = remember(coach.id) { coach.localTimeText() }
    val personalityTraits = remember(coach.id) {
        coach.personality.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "dotPulse"
    )

    val hasActions = onStartChat != null || (onPlayIntro != null && coach.clipUrl != null)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (hasActions) 128.dp else 24.dp)
        ) {

            // ── Toolbar ──────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            coach.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (coach.title.isNotBlank()) {
                            Text(
                                coach.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Availability pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isAvailable) Color(0xFF4CAF50).copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isAvailable) {
                                Box(
                                    Modifier.size(14.dp).clip(CircleShape)
                                        .background(Color(0xFF4CAF50).copy(alpha = (1f - pulseAlpha) * 0.4f))
                                )
                            }
                            Box(
                                Modifier.size(7.dp).clip(CircleShape)
                                    .background(if (isAvailable) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                            )
                        }
                        Text(
                            if (isAvailable) "Online" else "Away",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAvailable) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Portrait card ─────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    if (coach.imageUrl != null) {
                        AsyncImage(
                            model = coach.imageUrl,
                            contentDescription = coach.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(Brush.linearGradient(listOf(bgColor, accentColor))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(coach.emoji, fontSize = 80.sp)
                        }
                    }
                    // City / time overlay at the bottom of the portrait
                    if (coach.city.isNotBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.72f))
                                )
                            )
                        )
                        Row(
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (coach.countryFlag.isNotBlank()) Text(coach.countryFlag, fontSize = 16.sp)
                            Text(
                                buildString {
                                    append(coach.city)
                                    if (localTime.isNotBlank()) append(" · $localTime")
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Personality traits (API, skip if empty) ───────────────────────
            if (personalityTraits.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        personalityTraits.forEach { PersonalityTag(it, bgColor, accentColor) }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Bio (API, skip if blank) ──────────────────────────────────────
            if (coach.profile.bio.isNotBlank()) {
                item {
                    QuoteBlock(
                        text = coach.profile.bio,
                        accentColor = accentColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Specialties (API, skip if empty) ─────────────────────────────
            if (coach.specialties.isNotEmpty()) {
                item {
                    SpecialtiesSection(
                        specialties = coach.specialties,
                        bgColor = bgColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Media grid (TribeBot, the main content section) ──────────────
            if (coach.media.isNotEmpty()) {
                item {
                    MediaGridSection(
                        coachName = coach.name,
                        media = coach.media,
                        bgColor = bgColor,
                        accentColor = accentColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Fun Fact (API, skip if blank) ────────────────────────────────
            if (coach.profile.funFact.isNotBlank()) {
                item {
                    FunFactBubble(
                        text = coach.profile.funFact,
                        accentColor = accentColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // ── Sticky action buttons ─────────────────────────────────────────────
        if (hasActions) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onPlayIntro != null && coach.clipUrl != null) {
                        Button(
                            onClick = { onPlayIntro(coachId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(PhosphorIcons.Regular.Play, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Watch ${coach.name}'s Intro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (onStartChat != null) {
                        Button(
                            onClick = {
                                when {
                                    authState is AuthState.Guest -> showAuthSheet = true
                                    !chatUnlocked -> {}
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
                                Icon(PhosphorIcons.Regular.ChatCircle, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Chat with ${coach.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(PhosphorIcons.Fill.Lock, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Unlocks at Level 3", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

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
