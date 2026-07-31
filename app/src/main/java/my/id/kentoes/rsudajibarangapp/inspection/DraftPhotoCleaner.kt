package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.sync.SentPhotoStorage
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pembersih foto untuk kebersihan storage jangka panjang.
 *
 * Tiga kategori yang dibersihkan:
 * 1. Baris `draf_foto` tanpa header valid — parent `draf_item`-nya sudah tidak ada
 *    (draft dihapus namun cascade tidak berjalan / data legacy). Baris + file-nya dihapus.
 * 2. File di folder `photos` yang tidak direferensikan `draf_foto` manapun — sisa draf
 *    yang dihapus (deleteDraft/deleteDraftCascade hanya menghapus baris DB, bukan file)
 *    atau capture kamera yang dibatalkan (file `IMG_*`). File yang masih muda
 *    (< grace period) dipertahankan — melindungi foto yang baru diambil tapi belum
 *    disimpan ke draf.
 * 3. File di folder `photos_sent` yang sudah melewati retensi 30 hari (ADR-0016) — backup
 *    foto terkirim yang kadaluarsa. Dipakai terus untuk tampilan lokal-first riwayat
 *    sampai lewat masa retensi, lalu dihapus otomatis.
 */
@Singleton
class DraftPhotoCleaner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val drafDao: DrafDao,
    private val sentPhotoStorage: SentPhotoStorage
) {

    /**
     * Hapus foto yatim/kadaluarsa. Mengembalikan jumlah item yang dibersihkan
     * (baris orfan + file yang terhapus) — untuk log/observability.
     */
    suspend fun cleanup(graceMillis: Long = DEFAULT_GRACE_MS): Int {
        var cleaned = 0

        // 1. Baris draf_foto tanpa header valid (parent draf_item hilang) → hapus baris + file
        val orphanedRows = drafDao.getOrphanedDraftPhotos()
        if (orphanedRows.isNotEmpty()) {
            cleaned += drafDao.deleteOrphanedDraftPhotos()
        }
        orphanedRows.forEach { row ->
            runCatching { File(row.pathLokal).delete() } // best-effort: jangan tinggalkan file yatim
        }

        // 2. File di disk yang tidak direferensikan draf_foto valid & sudah tua → hapus
        val referenced = drafDao.getAllReferencedPhotoPaths().toHashSet()
        val cutoff = System.currentTimeMillis() - graceMillis
        val photosDir = File(context.getExternalFilesDir(null), "photos")
        if (photosDir.isDirectory) {
            photosDir.listFiles()?.forEach { file ->
                if (file.isFile &&
                    file.absolutePath !in referenced &&
                    file.lastModified() < cutoff
                ) {
                    if (runCatching { file.delete() }.getOrDefault(false)) cleaned++
                }
            }
        }

        // 3. Foto terkirim yang lewat retensi 30 hari → hapus (ADR-0016)
        cleaned += sentPhotoStorage.deleteOlderThan(SENT_PHOTO_RETENTION_MS)
        return cleaned
    }

    companion object {
        /** File lebih muda dari ini dianggap masih dipakai (baru difoto, belum disimpan ke draf). */
        const val DEFAULT_GRACE_MS = 24L * 60 * 60 * 1000 // 24 jam

        /** Masa retensi backup foto terkirim di photos_sent (ADR-0016). */
        const val SENT_PHOTO_RETENTION_MS = 30L * 24 * 60 * 60 * 1000 // 30 hari
    }
}
