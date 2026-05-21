package az.tribe.lifeplanner.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.signInAsGuest
import az.tribe.lifeplanner.data.analytics.Analytics

@Composable
fun WelcomeScreen(
    onComplete: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = org.koin.compose.viewmodel.koinViewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        Analytics.onboardingStarted()
    }

    var hasNavigated by remember { mutableStateOf(false) }
    var showSignInSheet by remember { mutableStateOf(false) }
    var isLoadingGuest by remember { mutableStateOf(false) }

    // Navigate when auth completes (guest or full sign-in)
    LaunchedEffect(authState) {
        if (!hasNavigated && (authState is AuthState.Authenticated || authState is AuthState.Guest)) {
            hasNavigated = true
            onComplete()
        }
        if (authState is AuthState.Error || authState is AuthState.Unauthenticated) {
            isLoadingGuest = false
        }
    }

    // Animated gradient shift for a living background feel
    val infiniteTransition = rememberInfiniteTransition(label = "welcomeBg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D2B),
                            Color(0xFF1A1A4E).copy(alpha = 0.6f + gradientShift * 0.4f),
                            Color(0xFF2D1B69).copy(alpha = 0.5f + (1f - gradientShift) * 0.3f),
                            Color(0xFF0D0D2B)
                        )
                    )
                )
        )

        // Overlay darkening toward bottom for button legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.75f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Typewriter headline — vertically centered, above buttons
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TypewriterHeadline()
        }

        // Auth buttons — pinned to bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Primary CTA — Get Started (guest)
            Button(
                onClick = {
                    if (!isLoadingGuest) {
                        isLoadingGuest = true
                        authViewModel.signInAsGuest()
                    }
                },
                enabled = !isLoadingGuest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                if (isLoadingGuest) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp).padding(horizontal = 4.dp)
                    )
                } else {
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Secondary — Sign In
            OutlinedButton(
                onClick = {
                    Analytics.signInStarted()
                    showSignInSheet = true
                },
                enabled = !isLoadingGuest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    "Sign In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }

    // Sign-in bottom sheet
    if (showSignInSheet) {
        AuthBottomSheet(
            isSignUp = false,
            authViewModel = authViewModel,
            authState = authState,
            onDismiss = { showSignInSheet = false },
            onSuccess = { showSignInSheet = false }
        )
    }
}
