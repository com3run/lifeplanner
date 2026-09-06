package az.tribe.lifeplanner.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Envelope
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.User
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.modernColors
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()
    val magicLinkSent by authViewModel.magicLinkSent.collectAsState()

    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var showDataLossWarning by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var showOtpInput by remember { mutableStateOf(false) }

    // Track if we've already navigated to prevent double navigation
    var hasNavigated by remember { mutableStateOf(false) }

    // Skip the initial auth state so we don't bounce back if already Authenticated
    var hasSeenInitialState by remember { mutableStateOf(false) }

    // Detect if user is currently a guest
    val isCurrentlyGuest = authState is AuthState.Guest

    // Handle auth state changes, navigate on success (only on transitions, not initial state)
    LaunchedEffect(authState) {
        if (!hasSeenInitialState) {
            hasSeenInitialState = true
            return@LaunchedEffect
        }
        if (!hasNavigated) {
            when (authState) {
                is AuthState.Authenticated, is AuthState.Guest -> {
                    hasNavigated = true
                    onSignInSuccess()
                }
                else -> {}
            }
        }
    }

    // Snackbar for success messages (e.g., "Verification email resent")
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearSuccessMessage()
        }
    }

    // Derive title and subtitle based on state
    val titleText = when {
        isCurrentlyGuest && isSignUp -> "Link Your Account"
        isSignUp -> "Create Your Account"
        else -> "Welcome Back"
    }
    val subtitleText = when {
        isCurrentlyGuest && isSignUp -> "Your existing data will be preserved and synced to your new account"
        isSignUp -> "Sign up to start planning your life"
        else -> "Sign in to continue your journey"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            authState is AuthState.EmailVerificationPending -> "Verify Email"
                            isCurrentlyGuest && isSignUp -> "Link Account"
                            isSignUp -> "Create Account"
                            else -> "Sign In"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.modernColors.background
    ) { paddingValues ->

        // ── Email Verification Pending Screen ──
        if (authState is AuthState.EmailVerificationPending) {
            val pendingEmail = (authState as AuthState.EmailVerificationPending).email
            EmailVerificationPendingSection(
                pendingEmail = pendingEmail,
                password = password,
                authState = authState,
                authViewModel = authViewModel,
                paddingModifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            return@Scaffold
        }

        // ── Main Sign In / Sign Up Form ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Icon(
                imageVector = PhosphorIcons.Regular.User,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.modernColors.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Display Name (sign up only)
            if (isSignUp) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(PhosphorIcons.Regular.User, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.modernColors.primary,
                        focusedLabelColor = MaterialTheme.modernColors.primary,
                        focusedLeadingIconColor = MaterialTheme.modernColors.primary,
                        cursorColor = MaterialTheme.modernColors.primary
                    )
                )
                Spacer(Modifier.height(16.dp))
            }

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(PhosphorIcons.Regular.Envelope, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.modernColors.primary,
                    focusedLabelColor = MaterialTheme.modernColors.primary,
                    focusedLeadingIconColor = MaterialTheme.modernColors.primary,
                    cursorColor = MaterialTheme.modernColors.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(PhosphorIcons.Regular.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) PhosphorIcons.Regular.Eye else PhosphorIcons.Regular.EyeSlash,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.modernColors.primary,
                    focusedLabelColor = MaterialTheme.modernColors.primary,
                    focusedLeadingIconColor = MaterialTheme.modernColors.primary,
                    cursorColor = MaterialTheme.modernColors.primary
                )
            )

            // Forgot Password (sign in only)
            if (!isSignUp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showResetPasswordDialog = true }) {
                        Text("Forgot Password?", color = MaterialTheme.modernColors.primary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Primary action button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isSignUp) {
                        if (displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            if (isCurrentlyGuest) {
                                authViewModel.linkGuestAccount(email, password, displayName)
                            } else {
                                authViewModel.signUpWithEmail(email, password, displayName)
                            }
                        }
                    } else {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            if (isCurrentlyGuest) {
                                showDataLossWarning = true
                            } else {
                                authViewModel.signInWithEmail(email, password)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = authState !is AuthState.Loading &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        (!isSignUp || displayName.isNotBlank()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.modernColors.primary
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        when {
                            isCurrentlyGuest && isSignUp -> "Link Account"
                            isSignUp -> "Sign Up"
                            else -> "Sign In"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Magic link option (sign-in mode only)
            if (!isSignUp) {
                Spacer(Modifier.height(12.dp))

                if (!magicLinkSent) {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            if (email.isNotBlank()) authViewModel.sendMagicLink(email)
                        },
                        enabled = email.isNotBlank() && authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.modernColors.primary
                        )
                    ) {
                        Text("Sign in with Magic Link", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    // Magic link sent, show success + OTP entry
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Magic link sent! Check your email.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (!showOtpInput) {
                        TextButton(
                            onClick = { showOtpInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter code manually")
                        }
                    } else {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("6-digit code") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.modernColors.primary,
                                focusedLabelColor = MaterialTheme.modernColors.primary,
                                cursorColor = MaterialTheme.modernColors.primary
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && otpCode.length == 6) {
                                    authViewModel.verifyOtp(email, otpCode)
                                }
                            },
                            enabled = otpCode.length == 6 && authState !is AuthState.Loading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.modernColors.primary
                            )
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Verify Code", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    TextButton(
                        onClick = {
                            authViewModel.clearMagicLinkState()
                            otpCode = ""
                            showOtpInput = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Send a new link",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Guest option (only if NOT already a guest)
            if (!isCurrentlyGuest) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "OR",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { authViewModel.signInAsGuest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.modernColors.primary
                    )
                ) {
                    Text("Continue as Guest", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Toggle Sign In / Sign Up
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isSignUp) "Already have an account?" else "Don't have an account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    isSignUp = !isSignUp
                    if (authState is AuthState.Error) {
                        authViewModel.refreshAuthState()
                    }
                }) {
                    Text(
                        if (isSignUp) "Sign In" else "Sign Up",
                        color = MaterialTheme.modernColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error message
            if (authState is AuthState.Error) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Data Loss Warning Dialog
    if (showDataLossWarning) {
        DataLossWarningDialog(
            email = email,
            password = password,
            onDismiss = { showDataLossWarning = false },
            authViewModel = authViewModel,
        )
    }

    // Password Reset Dialog
    if (showResetPasswordDialog) {
        PasswordResetDialog(
            initialEmail = email,
            onDismiss = { showResetPasswordDialog = false },
            authViewModel = authViewModel,
        )
    }
}
