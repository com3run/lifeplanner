package az.tribe.lifeplanner.domain.service

/**
 * Which part of the day it is, by the clock alone.
 *
 * The Present tab is named after the moment it opens on, so the moment has to be visible: the tab
 * wears a sun by day and a moon at night, and the hero's gradient warms and cools with the same
 * bands. One definition, so the icon and the colour never disagree about what time it is.
 */
enum class DayPhase {
    /** First light, before the day has properly started. */
    DAWN,

    /** Full daylight. */
    DAY,

    /** The sun on its way down. */
    DUSK,

    /** Dark. */
    NIGHT;

    companion object {
        /** @param hourOfDay 0..23, local. Values outside that range wrap, so a raw hour is safe to pass. */
        fun of(hourOfDay: Int): DayPhase {
            val h = ((hourOfDay % 24) + 24) % 24
            return when {
                h < 6 || h >= 20 -> NIGHT
                h < 8 -> DAWN
                h < 17 -> DAY
                else -> DUSK
            }
        }
    }
}
