package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pengelola folder `photos_sent` — backup foto terkirim (ADR-0016).
 *
 * Saat sync sukses, file foto terkompresi (byte-identik server) dipindahkan ke sini
 * dengan nama = nama file server. Bertahan 30 hari (dihitung dari waktu sync via
 * file lastModified), lalu dihapus otomatis oleh [my.id.kentoes.rsudajibarangapp.inspection.DraftPhotoCleaner].
 *
 * Folder privat app-specific (`getExternalFilesDir/photos_sent`) — TIDAK dihapus saat
 * ganti akun (riwayat bersifat device-wide, lihat ADR-0015 & ADR-0016).
 */
@Singleton
class SentPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Folder privat app-specific untuk foto terkirim. */
    fun sentDir(): File = File(context.getExternalFilesDir(null), "photos_sent")

    /**
     * Pindahkan file sumber ke `photos_sent` dengan nama = nama file server.
     *
     * Copy + delete (bukan rename) karena sumber bisa lintas volume: file terkompresi
     * hidup di `cacheDir/compressed_photos` (internal), folder tujuan di external
     * app-specific storage. Sumber bisa juga berupa original path (file ≤300KB yang
     * dikembalikan compress() tanpa perubahan).
     *
     * @return map serverFileName → path absolut file tujuan (yang berhasil dipindah)
     */
    fun moveToSent(sourcePathsByServerName: Map<String, String>): Map<String, String> {
        val dir = sentDir()
        if (!dir.exists()) dir.mkdirs()
        val moved = mutableMapOf<String, String>()
        for ((serverName, sourcePath) in sourcePathsByServerName) {
            val source = File(sourcePath)
            if (!source.exists() || serverName.isBlank()) continue
            val target = File(dir, serverName)
            runCatching {
                source.copyTo(target, overwrite = true)
                source.delete()
                moved[serverName] = target.absolutePath
            }
        }
        return moved
    }

    /**
     * Hapus file `photos_sent` yang lebih tua dari `retentionMillis`.
     * Return jumlah file yang dihapus — untuk log/observability.
     */
    fun deleteOlderThan(retentionMillis: Long): Int {
        val dir = sentDir()
        if (!dir.isDirectory) return 0
        val cutoff = System.currentTimeMillis() - retentionMillis
        var deleted = 0
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                if (runCatching { file.delete() }.getOrDefault(false)) deleted++
            }
        }
        return deleted
    }
}
