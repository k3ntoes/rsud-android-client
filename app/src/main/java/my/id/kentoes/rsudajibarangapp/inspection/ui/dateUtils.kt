package my.id.kentoes.rsudajibarangapp.inspection.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Material3 DatePicker mengeluarkan/menerima millis sebagai UTC-midnight.
// Wajib format/parse di UTC — dengan timezone default JVM, tanggal bergeser
// satu hari di device dengan offset negatif (mis. GMT-5).
private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

// BUSINESS-DAY (WIB): stempel tanggal bisnis konsisten untuk RSUD Ajibarang.
// JANGAN pakai UTC/device timezone di sini — draf yang dibuat 00:00–07:00 WIB
// (UTC masih hari sebelumnya) akan distempel tanggal salah dan tampil sebagai
// "kemarin" di dashboard. BUG-FIX (diagnosa 2026-08).
private val wibDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("Asia/Jakarta")
}

/**
 * Tanggal bisnis "hari ini" dalam format yyyy-MM-dd, dikunci ke Asia/Jakarta (WIB).
 * @param epochMillis waktu referensi (default: sekarang) — parameter untuk test.
 */
internal fun wibToday(epochMillis: Long = System.currentTimeMillis()): String =
    wibDateFormat.format(Date(epochMillis))

/** Parse tanggal "yyyy-MM-dd" ke epoch millis (UTC-midnight). Melempar ParseException untuk input yang tidak valid. */
internal fun parseDateToMillis(dateStr: String): Long {
    return dateFormat.parse(dateStr).time
}

/** Format epoch millis ke string "yyyy-MM-dd" (dipetakan di UTC). */
internal fun formatMillisToDate(millis: Long): String {
    return dateFormat.format(Date(millis))
}
