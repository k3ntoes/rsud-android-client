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

    // ── BUG-FIX (2026-08): businessDate WIB vs UTC ──

    /**
     * Regresi: draf yang dibuat 00:00–07:00 WIB (UTC masih hari SEBELUMNYA) harus
     * tetap distempel hari-bisnis WIB yang BENAR — bukan take(10) dari timestamp UTC
     * yang menghasilkan "kemarin". Test ini sengaja dijalankan dengan timezone default
     * GMT-5 (dari @Before) untuk membuktikan wibToday() TIDAK terpengaruh timezone device.
     */
    @Test
    fun `wibToday during 00-07 WIB window returns current WIB day not UTC day`() {
        // 2026-08-08T00:30:00 WIB = 2026-08-07T17:30:00 UTC — masih "kemarin" di UTC.
        // parse via UTC-midnight helper: 2026-08-08 00:00 UTC
        val utcMidnightOfDay = parseDateToMillis("2026-08-08")
        val wib0330 = utcMidnightOfDay + 3 * 3600_000L + 30 * 60_000L // 03:30 UTC = 10:30 WIB

        // 03:30 UTC → WIB sudah 10:30 (hari sama).
        assertEquals("2026-08-08", wibToday(wib0330))

        // 17:30 UTC → WIB 00:30 keesokan hari. UTC masih 08-07, WIB sudah 08-08.
        // Ini persis jendela 00:00–07:00 WIB yang dulu salah distempel "kemarin".
        val utc1730 = utcMidnightOfDay + 17 * 3600_000L + 30 * 60_000L
        assertEquals("2026-08-09", wibToday(utc1730))

        // 18:30 UTC → WIB 01:30 (tetap keesokan hari).
        val utc1830 = utcMidnightOfDay + 18 * 3600_000L + 30 * 60_000L
        assertEquals("2026-08-09", wibToday(utc1830))

        // 00:30 UTC → WIB 07:30 (hari sama, tepat setelah jendela).
        val utc0030 = utcMidnightOfDay + 30 * 60_000L
        assertEquals("2026-08-08", wibToday(utc0030))
    }

    @Test
    fun `wibToday ignores device timezone default`() {
        // Timezone default sedang GMT-5 (dari @Before). 15:00 UTC = 22:00 WIB (hari sama),
        // tapi jika wibToday ikut GMT-5 akan menjadi 10:00 (hari sama juga) — beda kasus:
        // gunakan 20:00 UTC = 03:00 WIB keesokan; GMT-5 = 15:00 hari yang sama.
        val utcMidnight = parseDateToMillis("2026-08-08")
        val utc2000 = utcMidnight + 20 * 3600_000L // 20:00 UTC = 03:00 WIB (09-08)

        assertEquals("2026-08-09", wibToday(utc2000))
    }
}
