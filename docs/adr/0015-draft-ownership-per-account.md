# ADR-0015: Draft Ownership per Akun & Siklus Hidup File Foto Draf

**Status**: Accepted

**Tanggal**: 2026-07-31

## Context

Perangkat bisa dipakai oleh lebih dari satu Petugas (login bergantian). Draf inspeksi disimpan lokal (offline-first) dan bisa dalam kondisi belum terkirim. Ada dua masalah:

1. **Kebocoran draf antar akun** — jika logout tidak menghapus draf, user berikutnya yang login bisa melihat/mengirim draf milik user sebelumnya. Jika logout menghapus semua draf, user yang sama login ulang (misal token expired, atau force logout sementara) kehilangan progress kerja.
2. **File foto yatim** — menghapus draf hanya menghapus baris DB (`draf_inspeksi` + `draf_item` + `draf_foto` via CASCADE), bukan file foto di disk (`files/photos/`). Setiap draf yang dihapus atau inspeksi yang berhasil dikirim meninggalkan file yatim yang menumpuk sampai worker cleanup periodik berjalan — bahkan bisa lebih lama lagi jika cleanup gagal.

## Considered Options

### Opsi 1: Hapus semua draf saat logout

| Pro | Kontra |
|-----|--------|
| Tidak ada kebocoran antar akun | ❌ User yang sama login ulang kehilangan progress |
| Sederhana | ❌ Draft tidak bisa dibedakan per user — palu godam |

### Opsi 2: Draf bertag `inspector_id`, hapus hanya draf akun lain saat login — dipilih

| Pro | Kontra |
|-----|--------|
| ✅ Draf dibedakan per user — hanya draf akun LAIN yang dihapus | Perlu kolom baru + stamping saat simpan |
| ✅ User yang sama login ulang tidak kehilangan progress | Draf legacy (tanpa tag) tidak bisa diatribusikan — dipertahankan (konservatif) |
| ✅ Logout TIDAK menghapus draf — hanya ganti akun yang membersihkan | |

### Opsi 3: Tanpa pemilahan (biarkan semua draf tampil semua akun)

| Pro | Kontra |
|-----|--------|
| Paling sederhana | ❌ Bocor privacy antar akun — reviewer mencatat ini |

## Decision

**Draf bertag `inspector_id` (dari user yang login saat disimpan). Logout TIDAK menghapus draf. Saat akun BERBEDA login, hapus draf akun lama (`clearForeignDrafts`).**

### Mekanisme

1. **Stamping**: `DrafInspeksi.inspectorId` diisi dari `currentUser.id` saat draf disimpan (setiap simpan).
2. **Login**: `AuthRepository.login()` memanggil `InspectionRepository.clearForeignDrafts(user.id)` — `DELETE FROM draf_inspeksi WHERE inspectorId IS NOT NULL AND inspectorId != :current` (+ file foto ikut dihapus best-effort). Kegagalan cleanup tidak menggagalkan login.
3. **Logout / force logout**: TIDAK menghapus draf. Cache master data + `SyncState` tetap di-clear agar akun berikutnya sync penuh dari epoch — tapi draf milik user yang sama tetap ada saat login ulang.
4. **Draf legacy** (`inspector_id` null — dibuat sebelum fitur ini): dipertahankan (tidak bisa diatribusikan, konservatif).

### Siklus Hidup File Foto Draf

Semua jalur yang menghapus draf kini ikut menghapus file foto lokal:

| Jalur | Metode | File foto |
|-------|--------|-----------|
| Hapus manual (Daftar Draf) / resume | `deleteDraft` | Hapus bersama baris |
| Sync sukses / `DUPLICATE_INSPECTION` | `deleteSyncedDraft` | Hapus bersama baris (foto sudah di server) |
| Ganti akun | `clearForeignDrafts` | Hapus bersama baris |
| Cleanup periodik (yatim) | `DraftPhotoCleaner` via `DraftPhotoCleanupWorker` | Baris orfan + file tak tereferensi (> 24 jam) |

Helper `deleteFilesBestEffort` (best-effort `File.delete()`, tidak pernah throw) dipakai semua jalur.

## Consequences

### Positif

- Draf dibedakan per akun — user baru tidak melihat/mengirim draf user lama
- User yang sama login ulang tetap punya draf (tidak kehilangan progress)
- Semua jalur hapus draf ikut menghapus file — storage bersih tanpa menunggu worker periodik
- Kegagalan jaringan saat refresh TIDAK memicu logout paksa (AuthRepository.refreshToken hanya force-logout pada 401/403/`TOKEN_INVALID`) — user offline tidak kehilangan sesi/draf

### Negatif

- Kolom `inspector_id` sudah ada sejak DB version 3; migration v3 → v4 (destructive — pola yang sudah dipakai project) menambahkan kolom `isMyRoom` di `RuangEntity`
- Draf legacy tanpa tag dipertahankan — berpotensi tampil di akun baru (trade-off konservatif, jumlahnya minimal karena fitur baru)
- `clearForeignDrafts` dijalankan saat login — ada biaya satu query DELETE (kecil)

## Referensi

- `app/src/main/java/my/id/kentoes/rsudajibarangapp/auth/AuthRepository.kt` — login/forceLogout/refreshToken
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/InspectionRepository.kt` — deleteDraft/deleteSyncedDraft/clearForeignDrafts
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/DraftPhotoCleaner.kt` — cleanup periodik
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/DraftPhotoCleanupWorker.kt` — worker periodik
- `docs/adr/0014-media-store-photo-storage.md` — konteks penyimpanan foto
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md` — kebijakan retensi
