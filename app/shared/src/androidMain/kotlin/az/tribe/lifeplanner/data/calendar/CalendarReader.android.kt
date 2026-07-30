@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.DeviceCalendar
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform

actual class CalendarReader {

    private val context: Context by lazy { KoinPlatform.getKoin().get() }

    actual suspend fun isAvailable(): Boolean = true

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    actual suspend fun listCalendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        )

        val calendars = mutableListOf<DeviceCalendar>()
        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                "${CalendarContract.Calendars.ACCOUNT_NAME} ASC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC",
            )?.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val typeIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
                val colorIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)
                val primaryIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
                val accessIdx = c.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
                while (c.moveToNext()) {
                    val access = if (c.isNull(accessIdx)) 0 else c.getInt(accessIdx)
                    calendars += DeviceCalendar(
                        id = c.getLong(idIdx).toString(),
                        displayName = c.getString(nameIdx)?.takeIf { it.isNotBlank() } ?: "(unnamed)",
                        accountName = c.getString(accountIdx)?.takeIf { it.isNotBlank() },
                        accountType = c.getString(typeIdx)?.takeIf { it.isNotBlank() },
                        colorArgb = if (c.isNull(colorIdx)) null else c.getInt(colorIdx).toLong() and 0xFFFFFFFFL,
                        isPrimary = !c.isNull(primaryIdx) && c.getInt(primaryIdx) == 1,
                        isReadOnly = access < CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR,
                    )
                }
            }
        } catch (e: Exception) {
            Logger.w("CalendarReader") { "Failed to list calendars: ${e.message}" }
        }
        calendars
    }

    actual suspend fun readUpcomingEvents(days: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        return readEvents(now, now + days.toLong() * 24L * 60L * 60L * 1000L)
    }

    actual suspend fun readEvents(startEpochMillis: Long, endEpochMillis: Long): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext emptyList()

        // Instances (rather than Events) expands recurring events into concrete occurrences within
        // the window, which is what "events for this day" means to the user.
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().let { b ->
            ContentUris.appendId(b, startEpochMillis)
            ContentUris.appendId(b, endEpochMillis)
            b.build()
        }
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.CALENDAR_ID,
        )

        val events = mutableListOf<CalendarEvent>()
        try {
            context.contentResolver.query(
                uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                val locIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
                val calIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
                val calIdIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
                while (c.moveToNext()) {
                    val begin = c.getLong(beginIdx)
                    events += CalendarEvent(
                        // EVENT_ID repeats across recurring instances, so key by id + start.
                        id = "${c.getLong(idIdx)}@$begin",
                        title = c.getString(titleIdx)?.takeIf { it.isNotBlank() } ?: "(no title)",
                        startEpochMillis = begin,
                        endEpochMillis = c.getLong(endIdx),
                        allDay = c.getInt(allDayIdx) == 1,
                        location = c.getString(locIdx)?.takeIf { it.isNotBlank() },
                        calendarName = c.getString(calIdx)?.takeIf { it.isNotBlank() },
                        calendarId = if (c.isNull(calIdIdx)) null else c.getLong(calIdIdx).toString(),
                    )
                }
            }
        } catch (e: Exception) {
            Logger.w("CalendarReader") { "Failed to read calendar events: ${e.message}" }
        }
        events
    }
}
