package az.tribe.lifeplanner.core

/**
 * Pillar 4, minimal premium entitlement gate. Causal Insights is a paywall candidate, but
 * there is no billing/subscription mechanism in the app yet, so this is structurally in place
 * and **default-open** for MVP. When real billing lands (Play Billing / RevenueCat), back
 * [isPremium] with the entitlement check, the UI already branches on it.
 */
interface PremiumGate {
    val isPremium: Boolean
}

class DefaultPremiumGate : PremiumGate {
    // TODO(billing): wire to real entitlement. Open to everyone until then.
    override val isPremium: Boolean = true
}
