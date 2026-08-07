package my.id.kentoes.rsudajibarangapp.inspection.ui

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.TimeZone

/**
 * DatePicker mengeluarkan `selectedDateMillis` sebagai UTC-midnight.
 * dateUtils harus memformat/parse dengan timezone UTC agar tanggal tidak
 * bergeser di device dengan offset negatif (mis. GMT-5).
 *
 * Bug: dengan timezone default JVM, UTC-midnight 2026-07-28 dirender
 * sebagai 2026-07-27 di GMT-5 → filter riwayat bergeser satu hari.
 */
class DateUtilsTimezoneTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun pinNegativeOffsetTimeZone() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `formatMillisToDate shows the selected date in negative-offset timezone`() {
        // UTC-midnight millis persis seperti yang dikeluarkan DatePicker.
        val utcMidnight = LocalDate.of(2026, 7, 28).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals("2026-07-28", formatMillisToDate(utcMidnight))
    }

    @Test
    fun `parseDateToMillis yields UTC midnight so DatePicker initial selection is exact`() {
        val expected = LocalDate.of(2026, 7, 28).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals(expected, parseDateToMillis("2026-07-28"))
    }

    @Test
    fun `round-trip parse then format returns original date in negative-offset timezone`() {
        val dates = listOf("2026-07-28", "2026-01-01", "2025-12-31", "2024-02-29", "1970-01-01")

        dates.forEach { date ->
            assertEquals(date, formatMillisToDate(parseDateToMillis(date)))
        }
    }
}
