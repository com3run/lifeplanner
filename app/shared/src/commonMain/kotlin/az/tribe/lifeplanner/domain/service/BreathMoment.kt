package az.tribe.lifeplanner.domain.service

/**
 * Whether now is a moment worth suggesting a breath, and why.
 *
 * The breathing card used to sit near the top of the feed every day until you had done enough
 * breaths to dismiss it. A prompt that is always there is not a reminder, it is furniture: the user
 * learns to scroll past it, and by the time there is a day they genuinely need it, it has already
 * become invisible.
 *
 * So it now needs a reason, and it says the reason. Most days it says nothing at all.
 */
object BreathMoment {

    /** Enough due today that the day reads as heavy from the outside. */
    private const val BUSY_DAY_ITEMS = 5

    /** Below this on the wheel, the area is bad enough to be worth acting on. */
    private const val LOW_AREA = 5.0

    data class Moment(val reason: String)

    /**
     * @param hourOfDay 0..23, local.
     * @param planItems how many things are on today's plan.
     * @param mentalScore the wheel's Mental score, or null when the user has not set one.
     * @param breathsToday how many the user has already done today.
     * @param overdueItems steps on today's plan that are already late.
     */
    fun of(
        hourOfDay: Int,
        planItems: Int,
        mentalScore: Double?,
        breathsToday: Int,
        overdueItems: Int = 0,
    ): Moment? {
        // Already done it today. Asking again is the furniture problem in miniature.
        if (breathsToday > 0) return null

        return when {
            // The strongest signal we have that someone is under it, and the one case where the
            // prompt is doing the job it exists for.
            mentalScore != null && mentalScore < LOW_AREA ->
                Moment("Mental is the area you rated lowest. Ninety seconds is a fair start.")

            // A visibly heavy day, said before it starts rather than after it has gone badly.
            planItems >= BUSY_DAY_ITEMS && hourOfDay < 12 ->
                Moment("$planItems things on today. Worth starting from a settled place.")

            overdueItems >= 3 ->
                Moment("A few things have slipped. Panic makes that list longer, not shorter.")

            // Winding down. The one time of day almost nobody regrets stopping for a minute.
            hourOfDay >= 21 ->
                Moment("Late. A minute of this makes the next hour easier to put down.")

            // Otherwise: silence. Most days are ordinary and do not need to be intervened in.
            else -> null
        }
    }
}
