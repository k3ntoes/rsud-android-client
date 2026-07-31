# 📋 Implementation Claim Order — Phase 5: Dual-Path Photo Storage (ADR-0016)

> **Status Project:** ✅ **MVP SELESAI** — EPIC-0 s.d. EPIC-13 ✅ | RM Phase 2 ✅ | Phase 3 ✅ | Phase 4 ✅
> **Status Phase 5:** ✅ **SELESAI** — EPIC-13: Dual-Path Photo Storage (ADR-0016) — Android selesai; PS-05 re-upload menunggu endpoint backend (kontrak §4.6)
> **ADR Baru:** `ADR-0016` — Dual-Path Photo Storage: Folder Draf & Folder Terkirim (retensi 30 hari) · [lihat ADR](./adr/0016-dual-path-photo-storage.md)
> **Kontrak API:** `docs/android-to-be-api-contract.md` §4.6 (endpoint replace photo — dependensi backend)
> **Glossary:** [`inspections/CONTEXT.md`](../app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md) — term "Retensi Data" sudah diperbarui

---

## 🎯 Latar Belakang

Foto inspeksi saat ini disimpan di app-specific storage `files/photos/` dan **dihapus permanen** begitu draf berhasil dikirim (`deleteSyncedDraft` menghapus baris + file). Akibatnya:

1. **Foto tidak muncul di riwayat inspeksi** — detail riwayat hanya menampilkan URL server; jika foto server rusak/hilang, hanya ikon placeholder yang tampil
2. **Tidak ada backup** — file di server korup → tidak ada salinan lokal untuk ditampilkan maupun di-re-upload

### Ringkasan 8 Keputusan Grill Session

| # | Pertanyaan | Keputusan |
|---|-----------|-----------|
| 1 | Lokasi "folder terkirim" | **Opsi A**: Folder privat app-specific `photos_sent` (merevisi ADR-0014 bagian lokasi) |
| 2 | Salinan yang disimpan | **Opsi A**: File terkompresi byte-identik server (~300KB, ~90 MB/bulan) |
| 3 | Pemetaan foto server ↔ lokal | **Opsi A**: Kolom `localPath` nullable di `InspectionPhotoEntity` (migrasi v4→v5) |
| 4 | Strategi tampil di riwayat | **Opsi A**: Lokal-first, fallback URL server |
| 5 | Mekanisme retensi 30 hari | **Opsi A**: Perluas `DraftPhotoCleaner` (worker harian yang ada) |
| 6 | Ganti akun | **Opsi A**: photos_sent TIDAK dihapus saat ganti akun (self-expire 30 hari) |
| 7 | Scope re-upload | **Opsi B**: Termasuk re-upload manual |
| 8 | Mekanisme re-upload | **Opsi A**: Endpoint baru `PUT /api/inspections/{id}/photos/{photoId}` (dependensi BE) |

---

## 🗺️ Dependency Graph

```mermaid
flowchart LR
    EPIC13[EPIC-13: Dual-Path Photo Storage] --> PS01[PS-01: DB Migration v4→v5 localPath]
    PS01 --> PS02[PS-02: SyncManager pindah ke photos_sent]
    PS02 --> PS03[PS-03: DetailScreen lokal-first]
    PS02 --> PS04[PS-04: Cleaner 30 hari]
    PS03 --> PS05[PS-05: Re-upload manual ✅ Android]
    PS05 -. BLOCKED by BE endpoint .-> BE[PUT /api/inspections/{id}/photos/{photoId} ⛔]

    style EPIC13 fill:#ff6b6b,color:#fff
    style PS01 fill:#ffd93d,color:#333
    style PS02 fill:#ffd93d,color:#333
    style PS03 fill:#6bcb77,color:#fff
    style PS04 fill:#6bcb77,color:#fff
    style PS05 fill:#a78bfa,color:#fff
```

> **Aturan:** Setiap issue hanya bisa di-*claim* setelah semua dependensinya selesai. PS-05 juga ter-blokir endpoint backend (bisa dikerjakan setelah PS-03 + BE selesai).

---

## 📋 Issue List

| ID | Judul | Beads ID | Dependensi | Status | Estimasi |
|----|-------|----------|------------|--------|----------|
| **EPIC-13** | Dual-Path Photo Storage (ADR-0016) | `rsud-android-client-bse` | — | ✅ | 8 jam |
| **PS-01** | DB Migration v4→v5 — kolom `localPath` | `rsud-android-client-bse.1` | EPIC-13 | ✅ | 60 menit |
| **PS-02** | SyncManager — pindah file ke `photos_sent` | `rsud-android-client-bse.2` | PS-01 | ✅ | 2 jam |
| **PS-03** | DetailScreen — foto lokal-first | `rsud-android-client-bse.3` | PS-02 | ✅ | 90 menit |
| **PS-04** | DraftPhotoCleaner — scan `photos_sent` 30 hari | `rsud-android-client-bse.4` | PS-02 | ✅ | 60 menit |
| **PS-05** | Re-upload manual foto rusak | `rsud-android-client-bse.5` | PS-03 + BE endpoint | ✅ Android / ⛔ BE | 2 jam |

---

## 📋 Task Detail

### 🆕 PS-01: DB Migration v4→v5 — kolom `localPath` di InspectionPhotoEntity

**Beads ID:** `rsud-android-client-bse.1`
**Objective:** Tambah kolom `localPath` (nullable) di `InspectionPhotoEntity` untuk menyimpan path file foto lokal di folder `photos_sent`.

#### Task List

- [x] Tambah field di `InspectionPhotoEntity`:
  ```kotlin
  data class InspectionPhotoEntity(
      @PrimaryKey val id: Long,
      val detailId: Long,
      val photoFileName: String,
      val thumbnailFileName: String? = null,
      val sortOrder: Int = 0,
      val localPath: String? = null   // NEW — path file di photos_sent
  )
  ```
- [x] Update `AppDatabase.kt`: `version = 4` → `version = 5` (fallback destructive — pola yang sudah dipakai project, lihat ADR-0015 v3→v4)
- [x] Update `InspectionHistoryRepository.cacheInspection()` — terima + simpan `localPath` dari SyncManager
- [x] **Linking key (reviewer-fix):** Pilih **opsi (a)** — SyncManager pass `map<serverPhotoId, localPath>` ke `cacheInspection`. Implementasi: `buildPhotoLocalPaths()` di `SyncManager` mencocokkan `photoFileName` pada response (nama file server = nama file yang dipindah) terhadap hasil `moveToSent` → map serverPhotoId → localPath dikirim ke `cacheInspection(response, photoLocalPaths)`
- [x] Tambah DAO method di `MasterDataDao` — dua method baru:
  ```kotlin
  // Update referensi + path lokal setelah re-upload (nama file server baru)
  @Query("UPDATE inspection_photo SET photoFileName = :fileName, thumbnailFileName = :thumbnailName, localPath = :localPath WHERE id = :photoId")
  suspend fun updatePhotoAfterReplace(photoId: Long, fileName: String, thumbnailName: String?, localPath: String?)

  // Semua foto milik satu inspeksi — untuk tampilan lokal-first
  @Query("SELECT p.* FROM inspection_photo p INNER JOIN inspection_detail d ON p.detailId = d.id WHERE d.inspectionId = :inspectionId")
  suspend fun getPhotosForInspection(inspectionId: Long): List<InspectionPhotoEntity>
  ```
  > Catatan: `updatePhotoLocalPath` (opsi b) TIDAK dipakai — linking ditangani lewat map di `cacheInspection` (opsi a)
- [x] **Verifikasi:** `./gradlew :app:assembleDebug` ✅

#### Files Changed

| File | Tindakan |
|------|----------|
| `core/database/entity/InspectionPhotoEntity.kt` | ✏️ +`localPath` field |
| `core/database/AppDatabase.kt` | ✏️ version 4 → 5 |
| `inspection/InspectionHistoryRepository.kt` | ✏️ +localPath di cacheInspection |
| `core/database/dao/MasterDataDao.kt` | ✏️ +updatePhotoAfterReplace, +getPhotosForInspection |

---

### 🆕 PS-02: SyncManager — pindah file terkompresi ke `photos_sent` + isi localPath

**Beads ID:** `rsud-android-client-bse.2`
**Objective:** Saat sync sukses, file foto terkompresi (byte-identik server) DIPINDAHKAN ke `photos_sent` (bukan dihapus), `localPath` diisi, file asli 3–5MB tetap dihapus.

#### Task List

- [x] Update alur sync — `deleteSyncedDraft` TETAP menghapus file asli 3–5MB; pindah file terkompresi ditangani `SentPhotoStorage` baru (bukan ubah deleteSyncedDraft)
- [x] Tambah helper pindah file — **class baru `sync/SentPhotoStorage.kt`**:
  ```kotlin
  // cacheDir/compressed_photos (internal) → photos_sent (external) = copy + delete, BUKAN rename atomik
  fun moveToSent(sourcePathsByServerName: Map<String, String>): Map<String, String>
  fun deleteOlderThan(retentionMillis: Long): Int
  ```
  - Tujuan: `File(context.getExternalFilesDir(null), "photos_sent")` — `mkdirs()` jika belum ada
  - Nama file = nama file server (UUID) agar lookup trivial & re-upload byte-identik
  - Skip server name blank (upload gagal/empty fileName) agar tidak membuat file `""`
- [x] **Compressed-path mapping (reviewer-fix):** `uploadedNames` (key = original fotoPath) untuk DetailSubmit; **`compressedByServer`** (key = serverFileName → compressResultPath) untuk pindah file. `compress()` dipanggil SEKALI per foto (double-compress = file temp baru per panggilan — bug yang dihindari). `compressResultPath` bisa = original path jika file ≤ 300KB — mapping menangani keduanya
- [x] Buat mapping localPath: `buildPhotoLocalPaths()` — `moveToSent(compressedByServer)` lalu cocokkan `response.details[].photos[].photoFileName` → `map<serverPhotoId, localPath>` dikirim ke `cacheInspection(response, map)` SEBELUM `deleteSyncedDraft`
- [x] Update `SyncManager.syncSingleDraft`: panggil move setelah submit SUKSES. `DUPLICATE_INSPECTION` (409) → file TIDAK dipindah (server sudah punya)
- [x] Pastikan file asli di `files/photos/` tetap dihapus via `deleteSyncedDraft` (hemat storage — keputusan Q2)
- [x] **Verifikasi:** `./gradlew :app:assembleDebug` ✅ + `./gradlew :app:testDebugUnitTest` ✅ (300 test pass)

#### Files Changed

| File | Tindakan |
|------|----------|
| `sync/SentPhotoStorage.kt` | ➕ class baru — folder photos_sent |
| `sync/SyncManager.kt` | ✏️ move + isi localPath (buildPhotoLocalPaths) |
| `sync/SyncManagerTest.kt` | ✏️ +test pindah file & linking |

---

### 🆕 PS-03: InspectionDetailScreen — foto lokal-first dengan fallback URL server

**Beads ID:** `rsud-android-client-bse.3`
**Objective:** Detail riwayat menampilkan foto lokal-first: jika `localPath` ada & file ada → tampilkan file lokal; jika tidak → URL server.

#### Task List

- [x] Update `InspectionHistoryViewModel` — `detailPhotoLocalPaths: Map<Long, String>` di UI state; `loadDetail` mengisi via `masterDataDao.getPhotosForInspection(id)` — HANYA path yang file-nya masih ada (`File.exists()` filter)
- [x] Update `PhotoThumbnailCard` di `InspectionDetailScreen`:
  ```kotlin
  // Jika localPath != null && File(localPath).exists() → load File(localPath)
  // else → load URL server (${BuildConfig.BASE_URL}uploads/${photo.photoFileName})
  ```
- [x] Coil: pilih model `File` vs `String URL` (`imageModel: Any = localFile ?: url`) — Coil mendukung File
- [x] Pertahankan placeholder (ikon warning) saat kedua sumber gagal — icon tetap digambar di belakang AsyncImage
- [x] **Verifikasi:** `./gradlew :app:assembleDebug` ✅

#### Files Changed

| File | Tindakan |
|------|----------|
| `inspection/ui/InspectionDetailScreen.kt` | ✏️ +lokal-first di PhotoThumbnailCard |
| `inspection/InspectionHistoryViewModel.kt` | ✏️ +enrich localPath (detailPhotoLocalPaths) |
| `core/database/dao/MasterDataDao.kt` | ✏️ +getPhotosForInspection |

---

### 🆕 PS-04: DraftPhotoCleaner — perluas scan `photos_sent` (cutoff 30 hari)

**Beads ID:** `rsud-android-client-bse.4`
**Objective:** Worker harian yang sudah dijadwalkan juga scan `photos_sent/`: hapus file `lastModified() > 30 hari`.

#### Task List

- [x] Update `DraftPhotoCleaner.cleanup()` — scan folder `photos_sent/` via `sentPhotoStorage.deleteOlderThan(SENT_PHOTO_RETENTION_MS)`
- [x] Tambah konstanta:
  ```kotlin
  companion object {
      const val DEFAULT_GRACE_MS = 24L * 60 * 60 * 1000          // draf yatim (existing)
      const val SENT_PHOTO_RETENTION_MS = 30L * 24 * 60 * 60 * 1000  // NEW — 30 hari
  }
  ```
- [x] Pastikan cutoff draf (grace 24 jam) dan cutoff photos_sent (30 hari) tidak saling mengganggu — dua pass terpisah, `cleaned` diakumulasi
- [x] Pastikan `clearForeignDrafts` TIDAK menyentuh folder `photos_sent` (ADR-0016 Q6 — foto terkirim tetap ada saat ganti akun) — cleanup hanya `deleteOlderThan`, tidak ada pembersihan lintas akun
- [x] **Verifikasi:** `./gradlew :app:assembleDebug` ✅ + `./gradlew :app:testDebugUnitTest` ✅

#### Files Changed

| File | Tindakan |
|------|----------|
| `inspection/DraftPhotoCleaner.kt` | ✏️ +scan photos_sent (inject SentPhotoStorage) |
| `inspection/DraftPhotoCleanerTest.kt` | ✏️ +test 30 hari (real SentPhotoStorage) |

---

### 🆕 PS-05: Re-upload manual foto rusak (BLOCKED by BE endpoint)

**Beads ID:** `rsud-android-client-bse.5`
**Objective:** Tombol re-upload di detail riwayat untuk foto dengan `localPath`: upload file lokal → endpoint replace → update `InspectionPhotoEntity`.

> ⚠️ **BLOCKED:** backend belum punya endpoint `PUT /api/inspections/{id}/photos/{photoId}` — lihat kontrak API §4.6. Dapat dikerjakan setelah PS-03 + backend selesai.

#### Task List

- [x] **BLOCKED (backend):** `PUT /api/inspections/{id}/photos/{photoId}` (Multipart) — kontrak API §4.6. **Menunggu implementasi backend** — klien sudah siap
- [x] Tambah `SyncApi.replacePhoto` (Retrofit): `@Multipart PUT inspections/{id}/photos/{photoId}`
- [x] DTO response: pakai `PhotoOutDto` yang sudah ada (`id`, `photo_file_name`, `thumbnail_file_name`, `sort_order`)
- [x] Tambah tombol/aksi re-upload di `InspectionDetailScreen` — icon Refresh overlay di `PhotoThumbnailCard`, hanya tampil jika file backup lokal ada
- [x] Flow: `viewModel.reuploadPhoto` → `repository.replacePhoto(id, photoId, localPath)` → upload Multipart → `moveToSent` (rename ke nama server baru) → `updatePhotoAfterReplace` → `loadDetail` refresh
- [x] Handle error: `IllegalStateException` (backup hilang), exception umum → `error` di UI state (endpoint belum ada di BE akan menghasilkan error — sesuai status BE-pending)
- [x] **Verifikasi:** `./gradlew :app:assembleDebug` ✅ + `./gradlew :app:testDebugUnitTest` ✅

#### Files Changed

| File | Tindakan |
|------|----------|
| `sync/api/SyncApi.kt` | ✏️ +replacePhoto (PUT) |
| `inspection/ui/InspectionDetailScreen.kt` | ✏️ +tombol re-upload |
| `inspection/InspectionHistoryRepository.kt` | ✏️ +replacePhoto flow |
| `inspection/InspectionHistoryViewModel.kt` | ✏️ +reuploadPhoto |

---

## 📊 Ringkasan Semua Perubahan

| File | PS | Tindakan |
|------|----|----------|
| `core/database/entity/InspectionPhotoEntity.kt` | PS-01 | ✏️ +`localPath` field |
| `core/database/AppDatabase.kt` | PS-01 | ✏️ version 4 → 5 |
| `core/database/dao/MasterDataDao.kt` | PS-01 | ✏️ +updatePhotoAfterReplace, +getPhotosForInspection |
| `sync/SentPhotoStorage.kt` | PS-02 | ➕ class baru — folder photos_sent |
| `inspection/InspectionHistoryRepository.kt` | PS-01, PS-03, PS-05 | ✏️ +localPath di cacheInspection, +replacePhoto |
| `sync/SyncManager.kt` | PS-02 | ✏️ move + isi localPath (buildPhotoLocalPaths) |
| `sync/SyncManagerTest.kt` | PS-02 | ✏️ +test pindah file & linking |
| `inspection/ui/InspectionDetailScreen.kt` | PS-03, PS-05 | ✏️ lokal-first + tombol re-upload |
| `inspection/InspectionHistoryViewModel.kt` | PS-03, PS-05 | ✏️ +detailPhotoLocalPaths, +reuploadPhoto |
| `inspection/DraftPhotoCleaner.kt` | PS-04 | ✏️ +scan photos_sent 30 hari |
| `inspection/DraftPhotoCleanerTest.kt` | PS-04 | ✏️ +test 30 hari |
| `sync/api/SyncApi.kt` | PS-05 | ✏️ +replacePhoto (PUT) |
| `docs/android-to-be-api-contract.md` | PS-05 | ✏️ §4.6 endpoint replace (dari sesi grill) |
| `docs/adr/0016-dual-path-photo-storage.md` | — | ➕ ADR-0016 (dari sesi grill) |

### 📝 Catatan Implementasi (deviasi dari rencana awal)

1. **PS-01 Linking:** dipilih opsi (a) — `SyncManager.buildPhotoLocalPaths()` mengirim `map<serverPhotoId, localPath>` ke `cacheInspection`; `updatePhotoLocalPath` per-photo (opsi b) tidak dipakai.
2. **PS-02:** tidak mengubah `InspectionRepository.deleteSyncedDraft`; pindah file ditangani class baru `SentPhotoStorage` + `SyncManager` (move SEBELUM deleteSyncedDraft). `DUPLICATE_INSPECTION` → file tidak dipindah.
3. **PS-05:** DTO pakai `PhotoOutDto` existing (bukan `ReplacePhotoResponse` baru). Android side selesai & ter-test; tetap ⛔ menunggu endpoint backend.
4. **Verifikasi:** `:app:assembleDebug` ✅ · `:app:testDebugUnitTest` ✅ — **300 test pass** (termasuk 10 test baru: photos_sent cleaner, cacheInspection localPath, replacePhoto, reuploadPhoto, SyncManager linking).

---

## 🚦 Cara Claim Issue

```bash
# 1. Baca konteks
graphify query "bagaimana alur penyimpanan foto dari draft sampai terkirim?"
cat docs/adr/0016-dual-path-photo-storage.md

# 2. Claim issue (claim EPIC-13 dulu, lalu PS-01, dst.)
bd update rsud-android-client-bse --claim
bd update rsud-android-client-bse.1 --claim

# 3. Implementasi — ikuti task list

# 4. Verifikasi
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest

# 5. Close
bd update rsud-android-client-bse.1 --status closed
```

### Prasyarat Claim

- ✅ Sudah baca `CODING-RULES.md` (wajib! file tidak auto-read)
- ✅ Semua dependencies selesai (PS-02 butuh PS-01, dst.)
- ✅ Paham vocabulary `inspections/CONTEXT.md` (term **Retensi Data** sudah diperbarui)

---

## 🚦 Legend

| Simbol | Arti |
|--------|------|
| 🆕 | Belum dikerjakan |
| 🔄 | Sedang dikerjakan |
| ✅ | Selesai |
| 🔴 P0 | Kritis — core feature |
| 🟡 P1 | Tinggi — enhancement penting |
| 🟢 P2 | Normal — bisa ditunda |
| ⛔ | Blocked (dependensi backend) |
