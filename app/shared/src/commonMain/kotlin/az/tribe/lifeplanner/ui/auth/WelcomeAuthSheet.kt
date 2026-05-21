package az.tribe.lifeplanner.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*
import az.tribe.lifeplanner.util.GetCredentialEffect
import az.tribe.lifeplanner.util.SaveCredentialEffect
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AuthBottomSheet(
    isSignUp: Boolean,
    authViewModel: AuthViewModel,
    authState: AuthState,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val magicLinkSent by authViewModel.magicLinkSent.collectAsState()

    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // Allow toggling between sign-up and sign-in within the same sheet
    var showSignUp by remember { mutableStateOf(isSignUp) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usePasswordMode by remember { mutableStateOf(true) }
    var otpCode by remember { mutableStateOf("") }
    var showOtpInput by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    // Credential autofill state
    val isVerifyingState = authState is AuthState.EmailVerificationPending
    var triggerGetCredential by remember { mutableStateOf(false) }
    var triggerSaveCredential by remember { mutableStateOf(false) }
    var credSaveEmail by remember { mutableStateOf("") }
    var credSavePassword by remember { mutableStateOf("") }

    // Android: show saved-password picker when sign-in password form opens.
    // iOS: no-op — system QuickType bar handles autofill natively via KeyboardType.Password.
    val showSignInWithPassword = !showSignUp && usePasswordMode && !isVerifyingState
    LaunchedEffect(showSignInWithPassword) {
        if (showSignInWithPassword) {
            delay(450) // Wait for sheet open animation to finish
            triggerGetCredential = true
        }
    }

    // Android: fill fields from saved credential + auto-submit (one-tap sign-in).
    GetCredentialEffect(
        trigger = triggerGetCredential,
        onCredentialReceived = { savedEmail, savedPassword ->
            email = savedEmail
            password = savedPassword
            attempted = true
            // Auto-submit immediately after autofill
            if (savedEmail.isNotBlank() && savedPassword.isNotBlank()) {
                authViewModel.signInWithEmail(savedEmail.trim(), savedPassword)
            }
        },
        onComplete = { triggerGetCredential = false }
    )

    // Android: save credential after successful password sign-in or sign-up.
    // iOS: no-op — iCloud Keychain save prompt is shown by the system automatically.
    SaveCredentialEffect(
        email = credSaveEmail,
        password = credSavePassword,
        trigger = triggerSaveCredential,
        onComplete = { triggerSaveCredential = false }
    )

    // Server error: "email"/"password"/"general" → message
    var serverFieldError by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun clearErrors() { attempted = false; serverFieldError = null }
    fun onEmailChange(value: String) {
        email = value
        if (serverFieldError?.first == "email" || serverFieldError?.first == "general") serverFieldError = null
    }
    fun onPasswordChange(value: String) {
        password = value
        if (serverFieldError?.first == "password" || serverFieldError?.first == "general") serverFieldError = null
    }
    fun onDisplayNameChange(value: String) {
        displayName = value
        if (serverFieldError?.first == "name") serverFieldError = null
    }

    // Validation helpers
    val emailRegex = remember { Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") }
    fun nameError(): String? = when {
        !attempted -> null
        displayName.isBlank() -> "Name is required"
        displayName.length < 2 -> "At least 2 characters"
        serverFieldError?.first == "name" -> serverFieldError?.second
        else -> null
    }
    fun emailError(): String? = when {
        !attempted -> null
        email.isBlank() -> "Email is required"
        !emailRegex.matches(email.trim()) -> "Enter a valid email"
        serverFieldError?.first == "email" -> serverFieldError?.second
        else -> null
    }
    fun passwordError(): String? = when {
        !attempted -> null
        password.isBlank() -> "Password is required"
        password.length < 6 -> "At least 6 characters"
        serverFieldError?.first == "password" -> serverFieldError?.second
        else -> null
    }
    fun generalError(): String? = if (serverFieldError?.first == "general") serverFieldError?.second else null
    fun isFormValid(includesName: Boolean): Boolean {
        val emailOk = email.isNotBlank() && emailRegex.matches(email.trim())
        val passOk = password.isNotBlank() && password.length >= 6
        val nameOk = !includesName || (displayName.isNotBlank() && displayName.length >= 2)
        return emailOk && passOk && nameOk
    }

    // Track which error we already handled so stale errors don't re-trigger on recomposition
    var lastHandledError by remember { mutableStateOf<String?>(null) }

    // Map server errors to fields inline — no snackbar
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            val errorMsg = (authState as AuthState.Error).message
            // Skip if we already handled this exact error (e.g. sheet reopened with stale state)
            if (errorMsg == lastHandledError) return@LaunchedEffect
            lastHandledError = errorMsg

            keyboardController?.hide()
            attempted = true
            val msg = errorMsg.lowercase()
            when {
                "email" in msg && ("invalid" in msg || "format" in msg || "not found" in msg || "already" in msg) ->
                    serverFieldError = "email" to errorMsg
                "password" in msg && ("weak" in msg || "short" in msg || "incorrect" in msg || "wrong" in msg || "invalid" in msg) ->
                    serverFieldError = "password" to errorMsg
                else -> serverFieldError = "general" to errorMsg
            }
        } else {
            // Reset tracked error when state changes away from Error
            lastHandledError = null
        }
    }

    // Watch for successful auth — only navigate when a real auth transition happens.
    // Track the initial user ID so we don't auto-close when the sheet opens on an
    // already-authenticated screen (e.g. Guest opening sign-in from Profile).
    val initialUserId = remember {
        when (authState) {
            is AuthState.Authenticated -> (authState as AuthState.Authenticated).user.id
            is AuthState.Guest -> (authState as AuthState.Guest).user.id
            else -> null
        }
    }
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                val newId = (authState as AuthState.Authenticated).user.id
                if (newId != initialUserId) {
                    // Save credential for password-based sign-in/sign-up
                    if (usePasswordMode && email.isNotBlank() && password.isNotBlank()) {
                        credSaveEmail = email
                        credSavePassword = password
                        triggerSaveCredential = true
                    }
                    onSuccess()
                }
            }
            is AuthState.Guest -> {
                val newId = (authState as AuthState.Guest).user.id
                if (newId != initialUserId) onSuccess()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            authViewModel.clearMagicLinkState()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isVerifying = authState is AuthState.EmailVerificationPending

            Text(
                text = when {
                    isVerifying -> "Verify Your Email"
                    showSignUp -> "Create Account"
                    else -> "Welcome Back"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when {
                    isVerifying -> "Enter the 6-digit code we sent to your email"
                    showSignUp -> "Sign up to start planning your life"
                    !usePasswordMode -> "We'll send a magic link to your email"
                    else -> "Sign in with your password"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            when {
                showSignUp && !isVerifying -> SignUpForm(
                    email = email,
                    password = password,
                    displayName = displayName,
                    passwordVisible = passwordVisible,
                    attempted = attempted,
                    authState = authState,
                    nameError = ::nameError,
                    emailError = ::emailError,
                    passwordError = ::passwordError,
                    generalError = ::generalError,
                    onEmailChange = ::onEmailChange,
                    onPasswordChange = ::onPasswordChange,
                    onDisplayNameChange = ::onDisplayNameChange,
                    onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                    onSubmit = {
                        attempted = true
                        hideKeyboard()
                        if (isFormValid(includesName = true)) {
                            if (authState is AuthState.Guest) {
                                authViewModel.linkGuestAccount(email.trim(), password, displayName.trim())
                            } else {
                                authViewModel.signUpWithEmail(email.trim(), password, displayName.trim())
                            }
                        }
                    },
                    onSwitchToSignIn = {
                        showSignUp = false
                        clearErrors()
                        email = ""; password = ""; displayName = ""
                    }
                )

                isVerifying -> EmailVerificationForm(
                    pendingEmail = (authState as AuthState.EmailVerificationPending).email,
                    authState = authState,
                    generalError = ::generalError,
                    authViewModel = authViewModel,
                    hideKeyboard = ::hideKeyboard,
                    onServerFieldErrorClear = { serverFieldError = null }
                )

                !usePasswordMode -> MagicLinkForm(
                    email = email,
                    otpCode = otpCode,
                    showOtpInput = showOtpInput,
                    magicLinkSent = magicLinkSent,
                    authState = authState,
                    emailError = ::emailError,
                    generalError = ::generalError,
                    onEmailChange = ::onEmailChange,
                    onOtpCodeChange = { otpCode = it },
                    onShowOtpInputChange = { showOtpInput = it },
                    onSubmitMagicLink = {
                        attempted = true
                        hideKeyboard()
                        if (email.isNotBlank() && emailRegex.matches(email.trim())) {
                            authViewModel.sendMagicLink(email.trim())
                        }
                    },
                    hideKeyboard = ::hideKeyboard,
                    authViewModel = authViewModel,
                    onSwitchToPassword = { usePasswordMode = true; clearErrors() },
                    onSwitchToSignUp = {
                        showSignUp = true
                        clearErrors()
                        email = ""; password = ""
                        authViewModel.clearMagicLinkState()
                    }
                )

                else -> PasswordSignInForm(
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    authState = authState,
                    emailError = ::emailError,
                    passwordError = ::passwordError,
                    generalError = ::generalError,
                    onEmailChange = ::onEmailChange,
                    onPasswordChange = ::onPasswordChange,
                    onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                    onSubmit = {
                        attempted = true
                        hideKeyboard()
                        if (email.isNotBlank() && emailRegex.matches(email.trim()) && password.isNotBlank()) {
                            authViewModel.signInWithEmail(email.trim(), password)
                        }
                    },
                    onSwitchToMagicLink = {
                        usePasswordMode = false
                        clearErrors()
                        authViewModel.clearMagicLinkState()
                    },
                    onSwitchToSignUp = {
                        showSignUp = true
                        clearErrors()
                        usePasswordMode = false
                        email = ""; password = ""
                        authViewModel.clearMagicLinkState()
                    }
                )
            }
        }
    }
}
