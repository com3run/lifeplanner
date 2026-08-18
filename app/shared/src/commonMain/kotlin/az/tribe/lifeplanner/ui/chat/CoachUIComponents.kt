package az.tribe.lifeplanner.ui.chat

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CustomCoach
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.UsersThree

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoachLockedScreen(
    currentLevel: Int,
    currentXp: Int,
    xpNeeded: Int,
    requiredLevel: Int = 3,
    totalXpRequired: Int = 450,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Personal Coach") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C4DFF).copy(alpha = 0.15f), Color(0xFF00BFA5).copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(PhosphorIcons.Regular.Brain, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("Coach AI Unlocks at Level $requiredLevel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Complete a few objectives to earn XP and unlock your personal AI coach.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Level $currentLevel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Level $requiredLevel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    val progress = (currentXp.toFloat() / totalXpRequired.toFloat()).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.outlineVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(progress).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00BFA5)))))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("$currentXp / $totalXpRequired XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("${xpNeeded.coerceAtLeast(0)} XP to go", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF6B35))
        }
    }
}

@Composable
internal fun CoachStoryIntro(
    coach: CoachPersona?,
    modifier: Modifier = Modifier,
    onStartChat: () -> Unit
) {
    val coachName   = coach?.name ?: "Luna"
    val coachEmoji  = coach?.emoji ?: "✨"
    val coachTitle  = coach?.title ?: "Life Coach"
    val bio         = coach?.profile?.bio ?: "Your personal guide on this journey."
    val funFact     = coach?.profile?.funFact ?: ""
    val specialties = coach?.specialties ?: emptyList()

    val bg = remember(coach) {
        try { Color(("FF" + (coach?.avatar?.backgroundColor ?: "#6366F1").removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }
    val ac = remember(coach) {
        try { Color(("FF" + (coach?.avatar?.accentColor ?: "#818CF8").removePrefix("#")).toLong(16)) }
        catch (_: Exception) { Color(0xFF818CF8) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coach_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "coach_scale"
    )

    Box(modifier = modifier.background(Brush.verticalGradient(listOf(bg.copy(alpha = 0.12f), Color.Transparent, Color.Transparent)))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    .background(Brush.linearGradient(listOf(bg, ac)), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = coach?.imageUrl
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(coachEmoji, style = MaterialTheme.typography.displaySmall)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Meet $coachName", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text(coachTitle, style = MaterialTheme.typography.bodyMedium, color = bg)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "\u201C$bio\u201D",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (specialties.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth()) {
                    specialties.take(3).forEach { specialty ->
                        Box(modifier = Modifier.background(bg.copy(alpha = 0.15f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(specialty, style = MaterialTheme.typography.labelSmall, color = bg)
                        }
                    }
                }
            }

            if (funFact.isNotBlank()) {
                Text("\u2728 $funFact", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onStartChat,
                modifier = Modifier.fillMaxWidth(0.75f),
                colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = Color.White),
                shape = RoundedCornerShape(50)
            ) {
                Text("Start chatting \u2192", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun CoachSelectorStrip(
    coaches: List<CoachPersona>,
    customCoaches: List<CustomCoach>,
    selectedCoachId: String?,
    isCouncilMode: Boolean,
    onSelectCoach: (String) -> Unit,
    onSelectCouncil: () -> Unit
) {
    val councilGradient = Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00BFA5)))

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = CoachPersona.COUNCIL_ID) {
            CoachStripAvatar(gradient = councilGradient, emoji = null, isSelected = isCouncilMode, onClick = onSelectCouncil) {
                Icon(PhosphorIcons.Regular.UsersThree, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        items(coaches, key = { it.id }) { coach ->
            val bg = try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) } catch (_: Exception) { Color(0xFF7C4DFF) }
            val ac = try { Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16)) } catch (_: Exception) { Color(0xFF00BFA5) }
            CoachStripAvatar(gradient = Brush.linearGradient(listOf(bg, ac)), emoji = coach.emoji, imageUrl = coach.imageUrl, isSelected = selectedCoachId == coach.id && !isCouncilMode, onClick = { onSelectCoach(coach.id) })
        }
        items(customCoaches, key = { it.id }) { coach ->
            val bg = try { Color(("FF" + coach.iconBackgroundColor.removePrefix("#")).toLong(16)) } catch (_: Exception) { Color(0xFF888888) }
            val ac = try { Color(("FF" + coach.iconAccentColor.removePrefix("#")).toLong(16)) } catch (_: Exception) { Color(0xFFAAAAAA) }
            CoachStripAvatar(gradient = Brush.linearGradient(listOf(bg, ac)), emoji = coach.icon, isSelected = selectedCoachId == coach.id && !isCouncilMode, onClick = { onSelectCoach(coach.id) })
        }
    }
}

@Composable
internal fun CoachStripAvatar(
    gradient: Brush,
    emoji: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    imageUrl: String? = null,
    iconContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(gradient, CircleShape)
            .then(if (isSelected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            iconContent != null -> iconContent()
            imageUrl != null -> AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
            emoji != null -> Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Whose chat this is, and nothing else.
 *
 * For a returning user with an empty conversation: [CoachStoryIntro] has already been shown once
 * and repeating it makes it furniture, but an empty screen with a text box does not say who is
 * about to answer. This fades the character in and stops there.
 */
@Composable
internal fun CoachQuietIntro(coach: CoachPersona?, modifier: Modifier = Modifier) {
    val entrance = remember(coach?.id) { Animatable(0f) }
    LaunchedEffect(coach?.id) {
        entrance.snapTo(0f)
        entrance.animateTo(1f, animationSpec = tween(durationMillis = 450))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 16f
            },
        ) {
            val bg = remember(coach?.id) {
                try { Color(("FF" + (coach?.avatar?.backgroundColor ?: "#6366F1").removePrefix("#")).toLong(16)) }
                catch (_: Exception) { Color(0xFF6366F1) }
            }
            Box(
                Modifier.size(72.dp).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center,
            ) {
                if (coach?.imageUrl != null) {
                    AsyncImage(
                        model = coach.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(coach?.emoji ?: "\u2728", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                coach?.name ?: "Luna",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                coach?.title ?: "Life Coach",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
