package az.tribe.lifeplanner.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Envelope
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.User
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*

// --- Sign-Up Form ---

@Composable
internal fun SignUpForm(
    email: String,
    password: String,
    displayName: String,
    passwordVisible: Boolean,
    attempted: Boolean,
    authState: AuthState,
    nameError: () -> String?,
    emailError: () -> String?,
    passwordError: () -> String?,
    generalError: () -> String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToSignIn: () -> Unit
) {
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val isCurrentlyGuest = authState is AuthState.Guest

    val nameErr = nameError()
    val emailErr = emailError()
    val passErr = passwordError()

    OutlinedTextField(
        value = displayName,
        onValueChange = { onDisplayNameChange(it) },
        label = { Text("Display Name") },
        leadingIcon = { Icon(PhosphorIcons.Regular.User, contentDescription = null) },
        supportingText = nameErr?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        isError = nameErr != null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { onEmailChange(it) },
        label = { Text("Email") },
        leadingIcon = { Icon(PhosphorIcons.Regular.Envelope, contentDescription = null) },
        supportingText = emailErr?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        isError = emailErr != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
        modifier = Modifier.fillMaxWidth().focusRequester(emailFocus),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { onPasswordChange(it) },
        label = { Text("Password") },
        leadingIcon = { Icon(PhosphorIcons.Regular.Lock, contentDescription = null) },
        supportingText = if (passErr != null) {
            { Text(passErr, color = MaterialTheme.colorScheme.error) }
        } else if (!attempted) {
            { Text("Minimum 6 characters", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        isError = passErr != null,
        trailingIcon = {
            IconButton(onClick = { onPasswordVisibleToggle() }) {
                Icon(
                    if (passwordVisible) PhosphorIcons.Regular.Eye else PhosphorIcons.Regular.EyeSlash,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { onSubmit() },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = authState !is AuthState.Loading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
            Text(
                if (isCurrentlyGuest) "Link Account" else "Create Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }
    }

    generalError()?.let { error ->
        Spacer(Modifier.height(12.dp))
        InlineErrorBanner(error)
    }

    Spacer(Modifier.height(12.dp))
    TextButton(onClick = onSwitchToSignIn) {
        Text(
            "Already have an account? Sign in",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- Email Verification (OTP) Form ---

@Composable
internal fun EmailVerificationForm(
    pendingEmail: String,
    authState: AuthState,
    generalError: () -> String?,
    authViewModel: AuthViewModel,
    hideKeyboard: () -> Unit,
    onServerFieldErrorClear: () -> Unit
) {
    var verifyCode by remember { mutableStateOf("") }

    // Start polling for verification done on another device (keyed on email to avoid duplicate loops)
    LaunchedEffect(pendingEmail) {
        authViewModel.startVerificationPolling()
    }

    // Auto-submit when 6 digits entered
    LaunchedEffect(verifyCode) {
        if (verifyCode.length == 6) {
            hideKeyboard()
            authViewModel.verifySignupOtp(pendingEmail, verifyCode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF4CAF50).copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Check your inbox!",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "We sent an email to $pendingEmail",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    // Option 1: Tap the link
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Tap the link in the email to verify instantly",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(16.dp))

    // Divider with "or"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
        Text(
            "  or enter the code  ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }

    Spacer(Modifier.height(16.dp))

    // Option 2: Enter the 6-digit code
    OutlinedTextField(
        value = verifyCode,
        onValueChange = { v ->
            if (v.length <= 6 && v.all { it.isDigit() }) verifyCode = v
        },
        label = { Text("6-digit code") },
        placeholder = { Text("000000") },
        leadingIcon = { Icon(PhosphorIcons.Regular.ShieldCheck, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            hideKeyboard()
            if (verifyCode.length == 6) {
                authViewModel.verifySignupOtp(pendingEmail, verifyCode)
            }
        }),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    generalError()?.let { error ->
        Spacer(Modifier.height(12.dp))
        InlineErrorBanner(error)
    }

    Spacer(Modifier.height(12.dp))

    TextButton(
        onClick = {
            verifyCode = ""
            onServerFieldErrorClear()
            authViewModel.resendVerificationEmail(pendingEmail)
        }
    ) {
        Text(
            "Didn't get it? Resend",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- Magic Link Sign-In Form ---

@Composable
internal fun MagicLinkForm(
    email: String,
    otpCode: String,
    showOtpInput: Boolean,
    magicLinkSent: Boolean,
    authState: AuthState,
    emailError: () -> String?,
    generalError: () -> String?,
    onEmailChange: (String) -> Unit,
    onOtpCodeChange: (String) -> Unit,
    onShowOtpInputChange: (Boolean) -> Unit,
    onSubmitMagicLink: () -> Unit,
    hideKeyboard: () -> Unit,
    authViewModel: AuthViewModel,
    onSwitchToPassword: () -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    if (!magicLinkSent) {
        val magicEmailErr = emailError()

        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it) },
            label = { Text("Email") },
            leadingIcon = { Icon(PhosphorIcons.Regular.Envelope, contentDescription = null) },
            supportingText = magicEmailErr?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            isError = magicEmailErr != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmitMagicLink() }),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSubmitMagicLink() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = authState !is AuthState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Icon(PhosphorIcons.Regular.Envelope, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Send Magic Link", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        generalError()?.let { error ->
            Spacer(Modifier.height(12.dp))
            InlineErrorBanner(error)
        }
    } else {
        // Magic link sent, show OTP entry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                "Magic link sent to $email!\nCheck your inbox.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        if (!showOtpInput) {
            TextButton(onClick = { onShowOtpInputChange(true) }) {
                Text("Enter code manually", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) onOtpCodeChange(it) },
                label = { Text("6-digit code") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = {
                    hideKeyboard()
                    if (email.isNotBlank() && otpCode.length == 6) {
                        authViewModel.verifyOtp(email, otpCode)
                    }
                }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    hideKeyboard()
                    if (email.isNotBlank() && otpCode.length == 6) {
                        authViewModel.verifyOtp(email, otpCode)
                    }
                },
                enabled = otpCode.length == 6 && authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Verify Code", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        TextButton(onClick = {
            authViewModel.clearMagicLinkState()
            onOtpCodeChange("")
            onShowOtpInputChange(false)
        }) {
            Text("Send a new link", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Spacer(Modifier.height(8.dp))

    TextButton(onClick = onSwitchToPassword) {
        Text("Use password instead",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
    }

    TextButton(onClick = onSwitchToSignUp) {
        Text(
            "New here? Create account",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- Password Sign-In Form ---

@Composable
internal fun PasswordSignInForm(
    email: String,
    password: String,
    passwordVisible: Boolean,
    authState: AuthState,
    emailError: () -> String?,
    passwordError: () -> String?,
    generalError: () -> String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToMagicLink: () -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    val signInPasswordFocus = remember { FocusRequester() }

    val signInEmailErr = emailError()
    val signInPassErr = passwordError()

    OutlinedTextField(
        value = email,
        onValueChange = { onEmailChange(it) },
        label = { Text("Email") },
        leadingIcon = { Icon(PhosphorIcons.Regular.Envelope, contentDescription = null) },
        supportingText = signInEmailErr?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        isError = signInEmailErr != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { signInPasswordFocus.requestFocus() }),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { onPasswordChange(it) },
        label = { Text("Password") },
        leadingIcon = { Icon(PhosphorIcons.Regular.Lock, contentDescription = null) },
        supportingText = signInPassErr?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        isError = signInPassErr != null,
        trailingIcon = {
            IconButton(onClick = { onPasswordVisibleToggle() }) {
                Icon(
                    if (passwordVisible) PhosphorIcons.Regular.Eye else PhosphorIcons.Regular.EyeSlash,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        modifier = Modifier.fillMaxWidth().focusRequester(signInPasswordFocus),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { onSubmit() },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = authState !is AuthState.Loading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        } else {
            Text("Sign In", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }

    generalError()?.let { error ->
        Spacer(Modifier.height(12.dp))
        InlineErrorBanner(error)
    }

    Spacer(Modifier.height(8.dp))

    TextButton(onClick = onSwitchToMagicLink) {
        Text("Use magic link instead",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
    }

    TextButton(onClick = onSwitchToSignUp) {
        Text(
            "New here? Create account",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
