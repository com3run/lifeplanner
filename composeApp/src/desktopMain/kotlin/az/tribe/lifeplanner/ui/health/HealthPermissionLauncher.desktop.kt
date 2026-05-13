@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.ui.health

import androidx.compose.runtime.Composable

@Composable
actual fun rememberHealthPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit = {}
