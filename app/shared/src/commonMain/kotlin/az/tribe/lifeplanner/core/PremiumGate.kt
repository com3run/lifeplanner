package az.tribe.lifeplanner.core

import co.touchlab.kermit.Logger
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo

/**
 * Pillar 4 / TRI-70 — premium entitlement gate, backed by RevenueCat. Checks whether the
 * `premium` entitlement is active for the current customer. Falls back to **open** when the
 * SDK isn't configured (no API key) or on a transient error, so a config gap never locks
 * users out. `isPremium()` is suspend because the RevenueCat check is async.
 */
interface PremiumGate {
    suspend fun isPremium(): Boolean
}

/** RevenueCat-backed gate (production). */
class RevenueCatPremiumGate(
    private val entitlementId: String = PREMIUM_ENTITLEMENT
) : PremiumGate {
    override suspend fun isPremium(): Boolean {
        if (!Purchases.isConfigured) return true // no key configured → stay open
        return runCatching {
            Purchases.sharedInstance.awaitCustomerInfo().entitlements.active[entitlementId] != null
        }.getOrElse {
            Logger.w("RevenueCatPremiumGate") { "entitlement check failed: ${it.message}" }
            true // don't lock users out on a transient error
        }
    }

    companion object {
        const val PREMIUM_ENTITLEMENT = "premium"
    }
}

/** Always-open gate — for tests or when billing is intentionally disabled. */
class DefaultPremiumGate : PremiumGate {
    override suspend fun isPremium(): Boolean = true
}
