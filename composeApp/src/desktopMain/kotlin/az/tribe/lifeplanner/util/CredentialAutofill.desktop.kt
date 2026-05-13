package az.tribe.lifeplanner.util

import androidx.compose.runtime.Composable

@Composable
actual fun SaveCredentialEffect(email: String, password: String, trigger: Boolean, onComplete: () -> Unit) {
    if (trigger) onComplete()
}

@Composable
actual fun GetCredentialEffect(trigger: Boolean, onCredentialReceived: (email: String, password: String) -> Unit, onComplete: () -> Unit) {
    if (trigger) onComplete()
}
