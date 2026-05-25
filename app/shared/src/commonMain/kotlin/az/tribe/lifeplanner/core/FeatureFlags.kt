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
}
