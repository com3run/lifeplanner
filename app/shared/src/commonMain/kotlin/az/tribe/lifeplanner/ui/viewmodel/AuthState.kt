package az.tribe.lifeplanner.ui.viewmodel

import az.tribe.lifeplanner.domain.model.User

/**
 * Authentication state
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Guest(val user: User) : AuthState()
    /** Email signup succeeded but the user must verify their email before signing in. */
    data class EmailVerificationPending(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
