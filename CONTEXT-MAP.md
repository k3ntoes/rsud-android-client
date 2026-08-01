# Context Map — RSUD Ajibarang App

Aplikasi Android *offline-first* untuk inspeksi kebersihan rumah sakit oleh Petugas Kebersihan. Lima konteks domain yang saling berhubungan.

## Contexts

| Context | Location | Description |
|---------|----------|-------------|
| [Auth](./app/src/main/java/my/id/kentoes/rsudajibarangapp/auth/CONTEXT.md) | `auth/` | Token management, login/logout, session handling, pemilahan draf per akun |
| [Inspections](./app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md) | `inspections/` | Form inspeksi dinamis, skoring, validasi bukti foto, siklus hidup draf & file foto |
| [Master](./app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/CONTEXT.md) | `master/` | Master data download & incremental sync (`?since=`), `SyncStateStore` (synced_at per endpoint), penanda `isMyRoom` per user |
| [Sync](./app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/CONTEXT.md) | `sync/` | WorkManager, offline-first sync, upload dua langkah, hybrid inspection history, cleanup foto draf yatim |
| [Core](./app/src/main/java/my/id/kentoes/rsudajibarangapp/core/CONTEXT.md) | `core/` | App foundation, DI, shared models, base types, database & SharedPreferences |

> Master tidak memiliki `CONTEXT.md` sendiri — semantiknya didokumentasikan di `sync/CONTEXT.md` (Sinkronisasi Master Data, Sinkronisasi Inkremental, Keadaan Sinkronisasi) dan `core/CONTEXT.md` (Entitas Database).

## Relationships

- **Auth → Core**: Auth menyediakan `AuthState` yang dikonsumsi oleh Core (navigation, DI scope)
- **Auth → Inspections**: `login()` memanggil `InspectionRepository.clearForeignDrafts(user.id)` — hapus draf akun LAMA saat akun berbeda login (ADR-0015). Logout/force logout TIDAK menghapus draf.
- **Auth → Master**: `forceLogout()` memanggil `clearLocalCache()` + `SyncStateStore.clear()` agar akun berikutnya sync penuh dari epoch — mencegah room/assignment akun lama bocor ke akun baru
- **Inspections → Auth**: Setiap request API inspeksi membutuhkan Access Token dari Auth
- **Inspections → Core**: Menggunakan shared models, base types, dan database Core (`DrafDao`, `draf_inspeksi.inspectorId`)
- **Inspections → Master**: Form inspeksi & dropdown pemilihan room memakai master data lokal; hanya room bertanda `isMyRoom` yang tampil (kecuali `admin_ppi`)
- **Inspections → Sync**: Data inspeksi yang disimpan lokal akan diproses oleh Sync untuk dikirim ke server; `syncSingleDraft` memanggil `InspectionRepository.deleteSyncedDraft` (hapus baris + pindahkan file foto terkompresi ke `photos_sent` — lihat ADR-0016)
- **Master → Core**: `SyncStateStore` (SharedPreferences) menyimpan `synced_at` per endpoint; `RuangEntity.isMyRoom` dan `MasterDataDao` tinggal di database Core
- **Sync → Auth**: WorkManager menggunakan Access Token milik sesi terakhir yang tersimpan. `GET /api/auth/users` admin-only (ADR-0008) → tidak di-sync Android (E6, 2026-08-01); nama petugas riwayat diambil dari user login (`auth/me`)
- **Sync → Master**: `syncMasterData()` memanggil `MasterDataRepository` (items, rooms, pivots, my-rooms); urutan `syncRooms` → `syncMyRooms` load-bearing agar penanda `isMyRoom` benar (REPLACE syncRooms me-reset flag)
- **Sync → Inspections**: `DraftPhotoCleanupWorker` memanggil `DraftPhotoCleaner` (package `inspection/`) untuk cleanup foto draf yatim periodik
- **Sync → Core**: Menggunakan base networking dan dependency injection dari Core
