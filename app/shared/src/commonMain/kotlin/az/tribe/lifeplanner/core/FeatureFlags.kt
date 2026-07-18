package az.tribe.lifeplanner.core

/**
 * Compile-time feature flags.
 * Set a flag to `true` to enable the feature, `false` to hide it completely.
 *
 * Dead code elimination will strip all guarded branches when a flag is `false`,
 * so disabled features add zero runtime overhead.
 */
object FeatureFlags {
    /** Abilities, habit-XP → skill leveling system. Hidden until ready for release. */
    const val ABILITIES_ENABLED = false

    // ── v3 rollout ── flip on per phase ─────────────────────────────
    /**
     * Phase 1 interim: use the lightweight legacy Home as the Home tab instead of
     * the data-coupled For You feed. Default false = current behavior (For You home).
     * Flip to true for the Phase 1 release; back to false once Phase 2 lands For You.
     */
    const val USE_LEGACY_HOME_TAB = false

    /** Phase 2: For You feed as Home (needs PossibilityEngine + feed tables). */
    const val REDESIGN_HOME_FORYOU = false

    // Pillar kill switches. Each one guards every entry point AND the nav registration
    // for its feature, so flipping it to `false` both hides the entry point and makes the
    // route unreachable by a stale deep link. They ship `true`: the features are live.
    // Flip one to `false` to pull that pillar without a hotfix.

    /** Your Wiring (DecisionProfile / TuningInferenceEngine). */
    const val PILLAR_WIRING = true

    /** Causal Insights (CausalInsightEngine). */
    const val PILLAR_CAUSAL = true

    /** Becoming (IdentityStatement). */
    const val PILLAR_BECOMING = true

    /** Possibility Mode. */
    const val PILLAR_POSSIBILITY = true
}
