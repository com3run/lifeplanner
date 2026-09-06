package az.tribe.lifeplanner.data.auth

import az.tribe.lifeplanner.data.network.AuthTokenProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

/**
 * The Supabase session as an [AuthTokenProvider]: hands out the current JWT, refreshes it when it
 * is about to expire, and establishes an anonymous session for a guest who has none. A mutex keeps
 * concurrent AI calls from racing each other into duplicate refreshes or sign-ins.
 */
fun supabaseAuthTokenProvider(supabase: SupabaseClient): AuthTokenProvider {
    val refreshMutex = kotlinx.coroutines.sync.Mutex()
    return AuthTokenProvider {
        // Try current session first
        val session = supabase.auth.currentSessionOrNull()
        if (session != null) {
            // Check if the access token is expired or about to expire (within 30s)
            val now = kotlinx.datetime.Clock.System.now()
            val timeUntilExpiry = session.expiresAt - now
            if (timeUntilExpiry.inWholeSeconds <= 30) {
                // Serialize refresh attempts, if another call already refreshed, reuse it
                refreshMutex.lock()
                try {
                    // Re-check after acquiring lock, another call may have refreshed already
                    val currentSession = supabase.auth.currentSessionOrNull()
                    if (currentSession != null) {
                        val freshExpiry = currentSession.expiresAt - kotlinx.datetime.Clock.System.now()
                        if (freshExpiry.inWholeSeconds > 30) {
                            return@AuthTokenProvider currentSession.accessToken
                        }
                    }
                    // Still expired, refresh with retry
                    var lastException: Exception? = null
                    for (attempt in 1..3) {
                        try {
                            supabase.auth.refreshCurrentSession()
                            val refreshed = supabase.auth.currentSessionOrNull()?.accessToken
                            if (refreshed != null) return@AuthTokenProvider refreshed
                        } catch (e: Exception) {
                            lastException = e
                            co.touchlab.kermit.Logger.w("AuthTokenProvider") {
                                "Token refresh attempt $attempt failed: ${e.message}"
                            }
                            if (attempt < 3) kotlinx.coroutines.delay(500L * attempt)
                        }
                    }
                    co.touchlab.kermit.Logger.e("AuthTokenProvider") {
                        "Token refresh failed after 3 attempts: ${lastException?.message}"
                    }
                    throw IllegalStateException("Authentication expired. Please sign in again.")
                } finally {
                    refreshMutex.unlock()
                }
            } else {
                session.accessToken
            }
        } else {
            // No session at all. This is the guest case, and it is recoverable.
            //
            // signInAsGuest() is meant to create a real Supabase *anonymous* session, which
            // carries a JWT the ai-proxy accepts, so guests are entitled to AI. But that call
            // has a 10s timeout, and when it expires (flaky network on first launch) the app
            // silently falls back to a local-only guest with no session. Previously that state
            // was permanent for the whole install: every AI call threw here, before reaching
            // the network, so the coach produced nothing and onboarding seeded no goals.
            //
            // Heal it instead: establish the anonymous session on demand. Mutex-guarded so
            // concurrent AI calls do not each start their own sign-in.
            refreshMutex.lock()
            try {
                supabase.auth.currentSessionOrNull()?.let { existing ->
                    return@AuthTokenProvider existing.accessToken
                }
                co.touchlab.kermit.Logger.i("AuthTokenProvider") {
                    "No session; establishing an anonymous one so this guest can use AI"
                }
                supabase.auth.signInAnonymously()
                supabase.auth.currentSessionOrNull()?.accessToken
                    ?: throw IllegalStateException("Not authenticated. Please sign in.")
            } catch (e: IllegalStateException) {
                throw e
            } catch (e: Exception) {
                // Offline, or anonymous sign-ups disabled on the project. Genuinely cannot
                // reach AI, so surface it as an auth problem rather than a silent nothing.
                co.touchlab.kermit.Logger.w("AuthTokenProvider") {
                    "Anonymous sign-in for guest failed: ${e.message}"
                }
                throw IllegalStateException("Not authenticated. Please sign in.")
            } finally {
                refreshMutex.unlock()
            }
        }
    }
}
