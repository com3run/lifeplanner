package az.tribe.lifeplanner.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.modernColors
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.EnvelopeOpen

@Composable
internal fun EmailVerificationPendingSection(
    pendingEmail: String,
    password: String,
    authState: AuthState,
    authViewModel: AuthViewModel,
    paddingModifier: Modifier,
) {
    // Poll for verification done on another device (keyed on email to avoid duplicate loops)
    androidx.compose.runtime.LaunchedEffect(pendingEmail) {
        authViewModel.startVerificationPolling()
    }

    Column(
        modifier = paddingModifier
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.EnvelopeOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.modernColors.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "We've sent a verification link to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = pendingEmail,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.modernColors.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Click the link in the email to activate your account, then come back and sign in.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // "I've Verified — Sign Me In" button
        Button(
            onClick = {
                if (password.isNotBlank()) {
                    authViewModel.signInWithEmail(pendingEmail, password)
                } else {
                    // If password was cleared, go back to sign-in form
                    authViewModel.refreshAuthState()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.modernColors.primary
            )
        ) {
            Text("I've Verified — Sign Me In", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(12.dp))

        // Resend email button
        OutlinedButton(
            onClick = { authViewModel.resendVerificationEmail(pendingEmail) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.modernColors.primary
            )
        ) {
            Text("Resend Verification Email", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(12.dp))

        // Use different email
        TextButton(
            onClick = {
                // Go back to the sign-up form
                authViewModel.refreshAuthState()
            }
        ) {
            Text(
                "Use a Different Email",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun DataLossWarningDialog(
    email: String,
    password: String,
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace Guest Data?") },
        text = {
            Text(
                "Signing in to an existing account will replace your guest data. " +
                "If you want to keep your current data, go back and choose \"Sign Up\" to link your guest account instead."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    authViewModel.signInWithEmail(email, password)
                }
            ) {
                Text("Sign In Anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun PasswordResetDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel,
) {
    var resetEmail by remember { mutableStateOf(initialEmail) }
    var resetSent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            resetSent = false
        },
        title = { Text("Reset Password") },
        text = {
            Column {
                if (!resetSent) {
                    Text("Enter your email address and we'll send you a link to reset your password.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text("Password reset email sent! Check your inbox for a link to reset your password.")
                }
            }
        },
        confirmButton = {
            if (!resetSent) {
                TextButton(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            authViewModel.sendPasswordResetEmail(resetEmail)
                            resetSent = true
                        }
                    },
                    enabled = resetEmail.isNotBlank()
                ) {
                    Text("Send Reset Link")
                }
            } else {
                TextButton(onClick = {
                    onDismiss()
                    resetSent = false
                }) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (!resetSent) {
                TextButton(onClick = {
                    onDismiss()
                    resetSent = false
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
