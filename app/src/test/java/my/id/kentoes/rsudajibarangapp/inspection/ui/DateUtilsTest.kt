package my.id.kentoes.rsudajibarangapp.inspection.ui

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.TimeZone

class DateUtilsTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun pinUtcTimeZone() {
        // dateUtils.kt memformat/parse dengan timezone default JVM.
        // Kunci ke UTC agar asersi epoch millis deterministik di environment mana pun.
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    // ── parseDateToMillis ──

    @Test
    fun `parseDateToMillis converts valid date to UTC midnight millis`() {
        val expected = LocalDate.of(2026, 7, 28).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(expected, parseDateToMillis("2026-07-28"))
    }

    @Test
    fun `parseDateToMillis handles epoch date`() {
        assertEquals(0L, parseDateToMillis("1970-01-01"))
    }

    @Test
    fun `parseDateToMillis handles leap day`() {
        val expected = LocalDate.of(2024, 2, 29).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(expected, parseDateToMillis("2024-02-29"))
    }

    @Test
    fun `parseDateToMillis preserves chronological order`() {
        assertTrue(parseDateToMillis("2026-07-29") > parseDateToMillis("2026-07-28"))
        assertTrue(parseDateToMillis("2026-01-02") > parseDateToMillis("2025-12-31"))
    }

    // ── formatMillisToDate ──

    @Test
    fun `formatMillisToDate converts millis to date string`() {
        val millis = LocalDate.of(2026, 7, 28).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("2026-07-28", formatMillisToDate(millis))
    }

    @Test
    fun `formatMillisToDate handles epoch`() {
        assertEquals("1970-01-01", formatMillisToDate(0L))
    }

    @Test
    fun `formatMillisToDate pads single digit month and day`() {
        val millis = LocalDate.of(2026, 1, 5).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("2026-01-05", formatMillisToDate(millis))
    }

    // ── Round-trip (invariant yang dipakai di DatePicker flow) ──

    @Test
    fun `round-trip parse then format returns original date`() {
        val dates = listOf(
            "2026-07-28",
            "2026-01-01",
            "2025-12-31",
            "2024-02-29",
            "1970-01-01"
        )
        dates.forEach { date ->
            assertEquals(date, formatMillisToDate(parseDateToMillis(date)))
        }
    }
}
