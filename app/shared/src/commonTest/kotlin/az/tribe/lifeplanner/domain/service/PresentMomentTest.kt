package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.FeedKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PresentMomentTest {

    private val now = 1_700_000_000_000L
    private val minute = 60_000L

    private fun event(
        id: String = "e1",
        title: String = "Standup",
        startsInMinutes: Long = 0,
        lastsMinutes: Long = 30,
        allDay: Boolean = false,
        location: String? = null,
    ) = CalendarEvent(
        id = id,
        title = title,
        startEpochMillis = now + startsInMinutes * minute,
        endEpochMillis = now + (startsInMinutes + lastsMinutes) * minute,
        allDay = allDay,
        location = location,
    )

    private fun step(
        title: String = "Draft the outline",
        overdue: Boolean = false,
        milestoneId: String = "m1",
    ) = PresentMoment.Step(
        goalId = "g1",
        milestoneId = milestoneId,
        title = title,
        goalTitle = "Write the book",
        overdue = overdue,
    )

    private fun habitNudge(habitId: String? = "h1") = FeedItem(
        id = "f1",
        kind = FeedKind.DO_NEXT,
        eyebrow = "Do next",
        title = "Ten minutes of reading",
        body = "You have kept this for six days.",
        actionHabitId = habitId,
    )

    @Test
    fun `an event under way beats everything else`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = listOf(event(startsInMinutes = -10, lastsMinutes = 40, location = "Room 2")),
            steps = listOf(step(overdue = true)),
            nudge = habitNudge(),
        )
        assertEquals(PresentMoment.Kind.EVENT_NOW, m?.kind)
        assertEquals("Standup", m?.title)
        assertEquals("Room 2", m?.detail)
        assertEquals(now + 30 * minute, m?.endsAtEpochMillis)
    }

    @Test
    fun `the shortest running event wins so the card clears soonest`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = listOf(
                event(id = "long", title = "Offsite", startsInMinutes = -60, lastsMinutes = 300),
                event(id = "short", title = "Call", startsInMinutes = -5, lastsMinutes = 20),
            ),
            steps = emptyList(),
        )
        assertEquals("Call", m?.title)
    }

    @Test
    fun `an event inside the window reads as minutes away`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = listOf(event(startsInMinutes = 25)),
            steps = listOf(step()),
        )
        assertEquals(PresentMoment.Kind.EVENT_SOON, m?.kind)
        assertEquals(25, m?.minutesUntil)
    }

    @Test
    fun `an event past the window is the day's plan and not this moment`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = listOf(event(startsInMinutes = PresentMoment.SOON_WINDOW_MINUTES + 1L)),
            steps = emptyList(),
        )
        assertNull(m)
    }

    @Test
    fun `all-day entries say nothing about this hour`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = listOf(event(startsInMinutes = -60, lastsMinutes = 24 * 60, allDay = true)),
            steps = emptyList(),
        )
        assertNull(m)
    }

    @Test
    fun `a late step is picked ahead of one merely due today`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = emptyList(),
            steps = listOf(
                step(title = "Due today", milestoneId = "m1"),
                step(title = "Slipped last week", overdue = true, milestoneId = "m2"),
            ),
        )
        assertEquals(PresentMoment.Kind.LATE_STEP, m?.kind)
        assertEquals("Slipped last week", m?.title)
        assertEquals("m2", m?.milestoneId)
    }

    @Test
    fun `with nothing late the first step of the day stands in`() {
        val m = PresentMoment.of(
            nowEpochMillis = now,
            events = emptyList(),
            steps = listOf(step(title = "Draft the outline")),
        )
        assertEquals(PresentMoment.Kind.STEP, m?.kind)
        assertEquals("Write the book", m?.detail)
    }

    @Test
    fun `a habit is the last thing offered and only when it can be done from here`() {
        val withHabit = PresentMoment.of(now, emptyList(), emptyList(), habitNudge())
        assertEquals(PresentMoment.Kind.HABIT, withHabit?.kind)
        assertEquals("h1", withHabit?.habitId)

        val readOnlyCard = PresentMoment.of(now, emptyList(), emptyList(), habitNudge(habitId = null))
        assertNull(readOnlyCard)
    }

    @Test
    fun `an empty hour gets no card at all`() {
        assertNull(PresentMoment.of(now, emptyList(), emptyList(), null))
    }
}
