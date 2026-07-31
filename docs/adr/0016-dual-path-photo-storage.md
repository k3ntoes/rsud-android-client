# ADR-0016: Dual-Path Photo Storage — Folder Draf & Folder Terkirim (retensi 30 hari)

**Status**: Accepted

**Tanggal**: 2026-07-31

**Supersedes**: ADR-0014 bagian lokasi penyimpanan (MediaStore). Retensi 30 hari ADR-0014 tetap berlaku.

## Context

Foto inspeksi saat ini disimpan di app-specific storage `files/photos/` dan **dihapus permanen** begitu draf berhasil dikirim (`deleteSyncedDraft` menghapus baris + file). Akibatnya:

1. **Foto tidak muncul di riwayat inspeksi** — detail riwayat hanya menampilkan URL server (`${BASE_URL}/uploads/{fileName}`). Jika foto server rusak atau gagal dimuat, yang tampil hanyalah ikon placeholder, tanpa fallback ke file lokal (file lokal sudah tidak ada).
2. **Tidak ada backup** — jika file di server korup/hilang, tidak ada salinan lokal yang bisa ditampilkan maupun di-re-upload.

Keputusan: simpan foto pada **dua path** sesuai siklus hidupnya — (1) folder draf selama belum terkirim, (2) folder terkirim setelah sync sukses, dengan retensi 30 hari lalu dihapus otomatis. Ini menanggulangi masalah foto terkirim rusak: user tetap punya backup foto asli (byte-identik dengan server) dan foto bisa ditampilkan di riwayat inspeksi.

## Considered Options

### Lokasi Folder Terkirim

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Folder privat app-specific `photos_sent` — dipilih** | Privat & aman (tidak bisa terhapus dari galeri — tujuan backup justru bertentangan dengan hapus manual galeri). Tanpa permission baru. Konsisten pola `files/photos`. | Tidak muncul di galeri HP (by design — foto inspeksi bersifat internal). |
| MediaStore `Pictures/rsud_ajibarang/` (ADR-0014) | Muncul di galeri, user bisa lihat/hapus manual. | Risiko: user hapus dari galeri = backup hilang. Butuh `READ_MEDIA_IMAGES` untuk read-back. |
| Internal `filesDir` | Paling privat. | Kapasitas internal HP kelas bawah lebih terbatas. |

### Salinan yang Disimpan

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Terkompresi byte-identik server (~300KB) — dipilih** | Persis file yang di-upload ke server → re-upload memulihkan kondisi yang seharusnya. Cukup untuk tampilan riwayat (1920px). ~90 MB/bulan (10 foto/hari × 30 hari × 0,3 MB). Selaras tujuan ADR-0014 mencegah bloat. | Bukan resolusi penuh kamera. |
| Foto asli full-res (3–5MB) | Resolusi penuh. | ~1,2 GB/bulan — storage bengkak, persis masalah yang dicegah ADR-0014. |
| Keduanya | Arsip + tampilan. | ~1,3 GB/bulan — overkill. |

### Strategi Tampil di Riwayat

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Lokal-first, fallback URL server — dipilih** | Foto tampil instan & offline-ready, kebal korupsi server. Jika file lokal tidak ada → fallback URL server (placeholder "foto dihapus" bila gagal). | Perlu kolom `localPath` untuk lookup. |
| Server-first, fallback lokal | Data paling fresh dari server. | Tiap foto butuh network round-trip dulu; offline/rusak → placeholder sesaat. |
| Lokal-only | Instan, offline penuh. | Tidak ada fallback jika file lokal hilang. |

### Mekanisme Retensi 30 Hari

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Perluas `DraftPhotoCleaner` yang ada — dipilih** | Worker harian yang sudah dijadwalkan juga scan `photos_sent`: hapus file > 30 hari. Satu tempat logika cleanup, tanpa jadwal baru. | Perlu penanganan dua folder dalam satu cleaner. |
| Worker terpisah | Isolasi concern. | 2 periodic worker untuk hal mirip — overhead scheduling & maintenance ganda. |
| Cleanup on-launch | Tanpa worker. | Foto menumpuk selama user tidak buka app. |

## Decision

**Foto terkirim dipindahkan (bukan dihapus) ke folder privat `getExternalFilesDir/photos_sent` saat sync sukses, disimpan 30 hari, lalu dihapus otomatis. Riwayat inspeksi menampilkan foto lokal-first dengan fallback URL server.**

### Mekanisme

1. **Sync sukses** (`SyncManager.syncSingleDraft`): alih-alih `deleteSyncedDraft` menghapus file, file **terkompresi** (byte-identik yang di-upload) dipindahkan dari `cacheDir/compressed_photos/` ke `photos_sent/` — diberi nama mengikuti nama file server (UUID) agar lookup trivial dan re-upload byte-identik. **Catatan teknis**: `cacheDir` (internal) dan `photos_sent` (external) berada di volume berbeda — pemindahan dilakukan copy+delete, bukan rename atomik. File asli 3–5MB di `files/photos/` tetap dihapus.
2. **Pemetaan**: kolom baru `localPath` (nullable) di `InspectionPhotoEntity`, diisi saat sync sukses dari mapping `fotoFileNames` yang sudah dimiliki `SyncManager`. Migrasi Room v4 → v5.
3. **Tampilan riwayat** (`InspectionDetailScreen`): jika `localPath` ada & file ada → tampilkan file lokal; jika tidak → URL server seperti sekarang.
4. **Retensi**: `DraftPhotoCleaner` diperluas — selain cleanup foto draf yatim, scan `photos_sent/` dan hapus file dengan `lastModified()` > 30 hari (timestamp move ≈ waktu sync, tanpa kolom tanggal baru).
5. **Ganti akun**: photos_sent TIDAK dihapus saat akun berbeda login — foto terikat inspeksi yang sudah di server (riwayat bersifat device-wide), self-expire 30 hari yang mengontrol storage. Konsisten dengan ADR-0015 yang hanya membersihkan DRAF akun lama.
6. **Re-upload manual**: tombol di detail riwayat → upload file `localPath` → panggil endpoint replace → update `InspectionPhotoEntity`. **Dependensi backend**: kontrak saat ini tidak punya endpoint replace (hanya `POST /api/upload` yang selalu menghasilkan UUID baru) — butuh endpoint baru `PUT /api/inspections/{id}/photos/{photoId}` (Multipart) + update `docs/android-to-be-api-contract.md`.

### Alur Baru (ringkas)

```text
Capture → files/photos (asli) → sync: kompres → upload → submit sukses
  → file terkompresi DIPINDAH ke photos_sent (copy+delete, nama = nama file server)
  → InspectionPhotoEntity.localPath diisi
  → file asli dihapus
  → Detail riwayat: tampil file lokal (fallback URL server)
  → setelah 30 hari: DraftPhotoCleaner hapus dari photos_sent
  → (opsional) re-upload manual via endpoint replace baru
```

## Consequences

### Positif

- ✅ Foto tampil di riwayat inspeksi walau foto server rusak/hilang (lokal-first)
- ✅ Backup byte-identik server → re-upload manual memulihkan kondisi yang seharusnya
- ✅ Tetap tampil offline (tanpa koneksi, foto riwayat bisa dilihat)
- ✅ Storage terkendali: ~90 MB/bulan terkompresi + self-expire 30 hari
- ✅ Privat (tidak muncul di galeri — tidak bisa terhapus user, tidak bocor ke app lain)
- ✅ Worker & pola yang ada dipakai ulang (DraftPhotoCleaner) — tanpa penjadwalan baru

### Negatif

- ⚠️ Migrasi Room v4 → v5 (kolom `localPath`) — pola destructive yang sudah dipakai project
- ⚠️ Dependensi backend untuk re-upload: endpoint replace baru belum ada — fitur re-upload ter-blokir sampai BE selesai (bisa dikerjakan paralel; fitur backup/tampilan tidak ter-blokir)
- ⚠️ Photos_sent bertahan saat ganti akun — trade-off privasi (konsisten: riwayat pun device-wide)
- ⚠️ `DraftPhotoCleaner` kini mengelola dua folder — perlu memastikan cutoff draf (grace 24 jam) dan cutoff photos_sent (30 hari) tidak saling mengganggu
- ⚠️ `InspectionPhotoEntity.localPath` diisi hanya untuk inspeksi yang di-sync SETELAH fitur ini — inspeksi lama (foto sudah terhapus) tetap tampil via URL server saja

## Referensi

- `docs/adr/0014-media-store-photo-storage.md` — keputusan awal (retensi 30 hari dipertahankan, lokasi MediaStore diganti)
- `docs/adr/0013-hybrid-inspection-history.md` — riwayat hybrid, `InspectionPhotoEntity`
- `docs/adr/0015-draft-ownership-per-account.md` — siklus hidup file foto & pemilahan per akun
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/SyncManager.kt` — `syncSingleDraft`, mapping lokal→server
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/InspectionRepository.kt` — `deleteSyncedDraft`
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/DraftPhotoCleaner.kt` — cleaner yang akan diperluas
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/DraftPhotoCleanupWorker.kt` — worker periodik harian
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/ui/InspectionDetailScreen.kt` — render foto riwayat
- `docs/android-to-be-api-contract.md` — kontrak API (perlu tambah endpoint replace)
