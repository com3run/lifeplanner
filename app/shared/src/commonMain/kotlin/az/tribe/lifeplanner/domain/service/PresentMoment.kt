package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.FeedItem

/**
 * The one thing that is true of this hour, for the top of the Present tab.
 *
 * The screen used to open on the day: weather, then everything due before midnight, then the life
 * as a whole. All of it accurate, none of it answering the question you actually opened the app
 * with, which is "what am I meant to be doing right now". So one line goes above the day and
 * answers it, and it only ever holds something happening now or something you can act on now.
 *
 * When there is nothing, it says nothing. An empty hour is a real answer and does not need a card
 * to announce itself (the same rule [BreathMoment] follows).
 */
object PresentMoment {

    /** How far ahead still counts as "now-ish". Beyond this it is the day's plan, not this moment. */
    const val SOON_WINDOW_MINUTES = 90

    private const val MINUTE_MILLIS = 60_000L

    enum class Kind {
        /** An event running right now. */
        EVENT_NOW,

        /** An event starting inside the window. */
        EVENT_SOON,

        /** A step whose date has already passed. */
        LATE_STEP,

        /** A step due today. */
        STEP,

        /** A habit the feed is already recommending, pulled up to where it can be done. */
        HABIT,
    }

    /** A milestone on today's plan, flattened so this stays free of the UI's row model. */
    data class Step(
        val goalId: String,
        val milestoneId: String,
        val title: String,
        val goalTitle: String,
        val overdue: Boolean,
    )

    data class Moment(
        val kind: Kind,
        val title: String,
        /** The quiet second line: where it is, or which goal it serves. Null when there is nothing to add. */
        val detail: String?,
        /** Minutes until it starts. Null when it is already under way or has no clock time. */
        val minutesUntil: Int? = null,
        /** When a running event ends, epoch millis, so the UI can say how long is left. */
        val endsAtEpochMillis: Long? = null,
        val goalId: String? = null,
        val milestoneId: String? = null,
        val habitId: String? = null,
    )

    /**
     * @param nowEpochMillis current time, UTC millis.
     * @param events today's calendar events. All-day entries are ignored: they are true of the
     *   whole day, so they say nothing about this hour.
     * @param steps today's plan, soonest first.
     * @param nudge the feed's top card, used only when it carries a habit that can be checked in
     *   from here. An insight or a lesson would just be the card below, printed twice.
     */
    fun of(
        nowEpochMillis: Long,
        events: List<CalendarEvent>,
        steps: List<Step>,
        nudge: FeedItem? = null,
    ): Moment? {
        val timed = events.filterNot { it.allDay }

        timed.filter { nowEpochMillis in it.startEpochMillis until it.endEpochMillis }
            .minByOrNull { it.endEpochMillis }
            ?.let {
                return Moment(
                    kind = Kind.EVENT_NOW,
                    title = it.title,
                    detail = it.location,
                    endsAtEpochMillis = it.endEpochMillis,
                )
            }

        val windowEnd = nowEpochMillis + SOON_WINDOW_MINUTES * MINUTE_MILLIS
        timed.filter { it.startEpochMillis in (nowEpochMillis + 1)..windowEnd }
            .minByOrNull { it.startEpochMillis }
            ?.let {
                return Moment(
                    kind = Kind.EVENT_SOON,
                    title = it.title,
                    detail = it.location,
                    // Round up, so a card that says "in 1 min" is never already over.
                    minutesUntil = ((it.startEpochMillis - nowEpochMillis + MINUTE_MILLIS - 1) / MINUTE_MILLIS).toInt(),
                )
            }

        // Late first: it is the thing most likely to be quietly rotting, and the plan below is
        // sorted by date, so the first overdue step is also the oldest.
        (steps.firstOrNull { it.overdue } ?: steps.firstOrNull())?.let {
            return Moment(
                kind = if (it.overdue) Kind.LATE_STEP else Kind.STEP,
                title = it.title,
                detail = it.goalTitle,
                goalId = it.goalId,
                milestoneId = it.milestoneId,
            )
        }

        val habitId = nudge?.actionHabitId ?: return null
        return Moment(
            kind = Kind.HABIT,
            title = nudge.title,
            detail = nudge.body.takeIf { it.isNotBlank() },
            habitId = habitId,
        )
    }
}
