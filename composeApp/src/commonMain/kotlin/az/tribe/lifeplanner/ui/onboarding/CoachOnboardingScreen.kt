package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.signInAsGuest
import az.tribe.lifeplanner.util.PlatformBackHandler
import com.russhwolf.settings.Settings
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoachOnboardingScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: CoachOnboardingViewModel = koinViewModel()
) {
    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.authState.collectAsState()
    val settings: Settings = koinInject()

    val phase by viewModel.phase.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showSignInSheet by remember { mutableStateOf(false) }
    var isLoadingGuest by remember { mutableStateOf(false) }

    // When auth resolves:
    // - If the user explicitly tapped "Get Started" (isLoadingGuest was true), reset any stale
    //   onboarding state and proceed from the beginning — never skip to Home.
    // - If auth resolved on its own (session restore / sign-in sheet), skip onboarding if complete.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated, is AuthState.Guest -> {
                val freshGuestAuth = isLoadingGuest
                isLoadingGuest = false
                if (freshGuestAuth) {
                    viewModel.resetForNewSession()
                } else if (CoachOnboardingViewModel.isComplete(settings)) {
                    onComplete()
                }
            }
            is AuthState.Error, is AuthState.Unauthenticated -> isLoadingGuest = false
            else -> {}
        }
    }

    // ── Auth gate — first screen users see ────────────────────────────────────
    if (authState is AuthState.Unauthenticated || authState is AuthState.Error) {
        AuthGate(
            isLoading = isLoadingGuest,
            onGetStarted = {
                Analytics.signUpStarted("guest")
                isLoadingGuest = true
                authViewModel.signInAsGuest()
            },
            onSignIn = { showSignInSheet = true }
        )
        if (showSignInSheet) {
            AuthBottomSheet(
                isSignUp = false,
                authViewModel = authViewModel,
                authState = authState,
                onDismiss = { showSignInSheet = false },
                onSuccess = { showSignInSheet = false }
            )
        }
        return
    }

    // Loading state between auth phases
    if (authState is AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ── Normal onboarding flow (authenticated) ────────────────────────────────
    val luna = CoachPersona.getById("luna_general")
    val specialist = runCatching { CoachPersona.getById(viewModel.specialistCoachId) }.getOrElse { luna }
    val activeCoach = if (phase >= OnboardingPhase.SPECIALIST_INTRO) specialist else luna

    val completeness = viewModel.overallCompleteness()
    val isFirstPhase = phase == OnboardingPhase.LUNA_INTRO

    PlatformBackHandler(enabled = !isFirstPhase) {
        viewModel.back()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        OnboardingProgressHeader(
            completeness = completeness,
            coachName = activeCoach.name,
            showBack = !isFirstPhase,
            onBack = { viewModel.back() }
        )

        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 3 } + fadeOut())
            },
            modifier = Modifier.weight(1f)
        ) { currentPhase ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoachBubble(coach = activeCoach, message = phaseMessage(currentPhase, viewModel))
                Spacer(modifier = Modifier.height(24.dp))
                PhaseContent(
                    phase = currentPhase,
                    viewModel = viewModel,
                    onAdvance = {
                        if (currentPhase == OnboardingPhase.COMPLETE) {
                            viewModel.completeOnboarding(onComplete)
                        } else {
                            viewModel.advance()
                        }
                    },
                    isSaving = isSaving
                )
            }
        }
    }
}

@Composable
private fun AuthGate(
    isLoading: Boolean,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Life Planner",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Goals. Habits. Life.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { if (!isLoading) onGetStarted() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = onSignIn,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Sign In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun OnboardingProgressHeader(
    completeness: Float,
    coachName: String,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(end = 24.dp, top = 12.dp, bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.padding(start = 24.dp))
            }
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(completeness * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { completeness },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
