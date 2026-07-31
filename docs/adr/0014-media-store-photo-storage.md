# ADR-0014: MediaStore Photo Storage & 30-Day Data Retention

**Status**: Accepted — bagian **lokasi penyimpanan** disupersede oleh [ADR-0016](./0016-dual-path-photo-storage.md) (folder privat `photos_sent` menggantikan MediaStore). **Retensi 30 hari tetap berlaku.**

**Tanggal**: 2026-07-30

## Context

Aplikasi menyimpan foto bukti inspeksi yang diambil oleh Petugas. Foto-foto ini perlu:

1. **Diakses oleh pengguna** — user ingin bisa melihat dan menghapus foto secara manual dari galeri HP
2. **Dikelola penyimpanannya** — foto adalah penyebab utama *bloat* storage (3-5 MB per foto). Dengan pemakaian harian, storage bisa cepat penuh
3. **Tetap tersedia untuk draf yang belum dikirim** — foto draf tidak boleh dihapus sampai inspeksi berhasil dikirim

Saat ini foto disimpan di `context.getExternalFilesDir(null) + "/photos"` (app-specific storage, path: `Android/data/{package}/files/photos/`). Lokasi ini **tidak bisa diakses dari galeri** — user tidak bisa melihat atau menghapus foto secara manual.

Faktor pembatas:
- `targetSdk = 36` (Android 14) — Scoped Storage berlaku, akses ke `/sdcard/Pictures/` harus via `MediaStore`
- `minSdk = 24` (Android 7) — perlu fallback untuk API < 29
- WorkManager tersedia sebagai dependency — bisa untuk periodic cleanup job

## Considered Options

### Storage Location

| Opsi | Pro | Kontra |
|------|-----|--------|
| **App-specific (`getExternalFilesDir`) — existing** | Tidak perlu permission khusus. Sederhana. | ❌ Tidak bisa diakses galeri. User tidak bisa hapus manual. |
| **MediaStore `Pictures/rsud_ajibarang/` — dipilih** | ✅ Muncul di galeri. ✅ Bisa hapus manual. ✅ Tidak perlu `MANAGE_EXTERNAL_STORAGE`. | Butuh `READ_MEDIA_IMAGES` (Android 13+). Adaptasi kode untuk URI-based I/O. |
| **`MANAGE_EXTERNAL_STORAGE`** | Akses penuh ke filesystem. | ❌ Hampir pasti ditolak Google Play — khusus file manager/antivirus. |
| **Room DB (BLOB)** | Satu tempat, gampang backup. | ❌ Bencana performa — database membengkak. Tidak bisa diakses galeri. |

### Data Retention

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Simpan semua selamanya** | User bisa lihat foto lama kapan saja. | ❌ Storage pasti penuh — dengan 10 foto/hari × 3MB × 30 hari = ~900 MB. |
| **Hapus foto > 30 hari — dipilih** | ✅ Metadata tetap ada untuk riwayat. ✅ Foto lama bisa di-fetch ulang dari server. ✅ Galeri tetap bersih. | ⚠️ User perlu koneksi untuk lihat foto lama. |
| **Hapus semua foto + metadata > 30 hari** | Paling hemat storage. | ❌ Riwayat inspeksi tidak bisa dilihat offline. |
| **Hapus manual saja (tanpa auto cleanup)** | User punya kendali penuh. | ❌ Praktik membuktikan user hampir tidak pernah membersihkan cache secara sukarela. |

## Decision

**Gunakan MediaStore, path `Pictures/rsud_ajibarang/`, dengan auto-cleanup WorkManager setiap 30 hari.**

### Detail Storage

```kotlin
// API 29+ — MediaStore
val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${timestamp}.jpg")
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/rsud_ajibarang")
}
val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
// Tulis file via contentResolver.openOutputStream(uri)
```

```kotlin
// API 24-28 — fallback
val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    .resolve("rsud_ajibarang")
dir.mkdirs()
val file = File(dir, "IMG_${timestamp}.jpg")
```

### Detail Retention

1. **Metadata inspeksi** (`InspectionEntity`, `InspectionDetailEntity`) — **disimpan permanen**. Ukurannya sangat kecil (byte per baris), ribuan inspeksi hanya beberapa MB.
2. **Foto** — disimpan di `Pictures/rsud_ajibarang/`, muncul di galeri:
   - **Draf aktif**: foto **tidak dihapus** sampai draf berhasil dikirim atau dihapus user
   - **Inspeksi tersubmit**: foto **dihapus otomatis setelah 30 hari** via WorkManager periodic task
   - **Manual delete**: user bisa hapus foto kapan saja dari galeri — resiko user
3. **Auto-cleanup**: WorkManager `PeriodicWorkRequest` (min interval 12 jam, idealnya daily) → query foto > 30 hari dari `MediaStore` → hapus via `contentResolver.delete()`

### Permission Strategy

| API Level | Permission | Notes |
|-----------|------------|-------|
| API 24-28 | `WRITE_EXTERNAL_STORAGE` di AndroidManifest | Runtime request opsional, bisa di-declare saja |
| API 29-32 | Tidak perlu permission khusus untuk `MediaStore.Images` | Scoped Storage — write via ContentResolver |
| API 33+ | `READ_MEDIA_IMAGES` (opsional, untuk read-back) | Tidak perlu `WRITE_EXTERNAL_STORAGE` — sudah di-deprecate |

### FileProvider

Camera capture tetap butuh `FileProvider` untuk `TakePicture` contract. Foto sementara dari kamera akan di-copy ke MediaStore, lalu file sementara dihapus.

```xml
<!-- AndroidManifest.xml — tetap perlu untuk camera temp file -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## Pelengkap: Cleanup Foto Draf Yatim (update 2026-07-31)

ADR ini mengatur foto inspeksi tersubmit (MediaStore, retensi 30 hari). Untuk **foto draf** (disimpan di app-specific `files/photos/`, belum terkirim), ditambahkan:

1. **Hapus-on-hapus**: `InspectionRepository.deleteDraft` (hapus manual / resume) dan `deleteSyncedDraft` (sukses sync & `DUPLICATE_INSPECTION`) menghapus file foto lokal bersama baris DB — foto sudah terupload ke server, file lokal tidak diperlukan lagi. Mencegah file yatim menumpuk di sela cleanup periodik.
2. **`DraftPhotoCleanupWorker`** (periodik harian via WorkManager, dibuat manual oleh `SyncAwareWorkerFactory`) — memanggil `DraftPhotoCleaner.cleanup()` yang membersihkan dua kategori "yatim": baris `draf_foto` tanpa header valid (parent draf_item hilang) dan file di `files/photos/` yang tidak direferensikan `draf_foto` manapun. File berumur < 24 jam dipertahankan (grace period) agar foto yang baru diambil tapi belum disimpan ke draf tetap aman.
3. **`clearForeignDrafts`** — saat akun berbeda login, draf akun lama + file fotonya dihapus (lihat ADR-0015).

## Consequences

### Positif

- ✅ Foto muncul di galeri HP — user bisa lihat dan hapus manual
- ✅ Storage terkendali — auto-cleanup 30 hari cegah bloat
- ✅ Metadata riwayat tetap bisa dilihat offline (tanpa foto)
- ✅ Tidak perlu `MANAGE_EXTERNAL_STORAGE` — tidak masalah review Play Store
- ✅ Draf aktif aman — fotonya tidak ikut dihapus
- ✅ Semua jalur hapus draf (manual, resume, sync sukses, ganti akun) ikut menghapus file foto — storage bersih tanpa menunggu worker periodik

### Negatif

- ⚠️ Kode penyimpanan foto berubah: dari `File(path)` → `ContentResolver(uri)` — butuh refactor di `CameraHelper`, `InspectionFormScreen`, `ImageCompressor`, `SyncManager`
- ⚠️ Untuk API 24-28, perlu deklarasi `WRITE_EXTERNAL_STORAGE` di manifest (tapi target SDK 36 berarti sebagian besar pengguna di API 29+)
- ⚠️ WorkManager periodic task minimal interval 12 jam (Android 12+) — tidak bisa real-time
- ⚠️ User salah hapus foto dari galeri → foto tidak bisa ditampilkan di detail inspeksi offline — perlu fallback ke placeholder "Foto telah dihapus"
- ⚠️ `DraftPhotoCleaner` memakai `context.getExternalFilesDir(null)/photos` — foto draf TIDAK muncul di galeri (by design: file internal aplikasi)

## Referensi

- `docs/adr/0013-hybrid-inspection-history.md` — Hybrid cache strategy (metadata disimpan permanen)
- `docs/IMPLEMENTATION-CLAIM-ORDER.md` — Implementation priority
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/components/CameraHelper.kt` — Existing photo storage
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/ImageCompressor.kt` — Image compression (perlu adaptasi)
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/SyncManager.kt` — Sync flow yang mengelola foto
