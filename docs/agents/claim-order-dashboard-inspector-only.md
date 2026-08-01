# Claim Order — Dashboard Inspector-Only (ADR-0017)

> Buat agent lain: daftar kerja eksekusi ADR-0017 + keputusan sesi grill 2026-08-01.
> **Langkah #0 untuk SEMUA issue: baca `CODING-RULES.md` sebelum claim/implement.**

## Diagram Dependensi

```
E1 (5v8) ──▶ E2 (d7a)          E4 (y20) — boleh kapan saja
E3 (vmj) ── independen         H3 (v63) — DEFERRED, jangan claim
E6 (usf) ── independen         (terkait E1: kolom name sudah ada di UserEntity)
```

## Checklist (urutan claim)

> **Status 2026-08-01: E1–E4 SELESAI** — semua `done` via `bd`. Payung `8vt` ditutup. H3 tetap deferred. **E6 ditambahkan 2026-08-01 (audit logcat: `auth/users` admin-only → inspector 403).**

| Urutan | Issue | Isi singkat | Boleh paralel | Verifikasi wajib | Status |
|--------|-------|-------------|---------------|------------------|--------|
| 1 | **`rsud-android-client-5v8`** (E1) | Field `name` di `UserOut`/`UserEntity` (+cek migrasi Room) + hapus `showAll` dead param | — | `grep showAll` bersih, `:app:testDebugUnitTest` hijau | ✅ done |
| 2 | **`rsud-android-client-d7a`** (E2) | Verifikasi hapus Aksi Cepat (edit sudah diterapkan, blm di-build), TopAppBar `nama · username`, hapus analytics + card Total Inspeksi + branch admin | — (deps E1) | compile + `testDebugUnitTest` hijau, grep analytics bersih | ✅ done |
| 3 | **`rsud-android-client-vmj`** (E3) | Tolak login non-inspector; force-logout sesi lama di `init()`/`refreshCurrentUser()` | ✅ paralel dengan E2 (dan E4) | 4 skenario test (login ok, login ditolak, init logout, refresh logout) | ✅ done |
| 4 | **`rsud-android-client-y20`** (E4) | Update `docs/android-implementation-guide.md` §4.3 + baris `show_all`/admin; sinkronkan dgn CONTEXT.md & ADR-0017 | ✅ kapan saja | grep docs bersih dari pernyataan kontradiktif | ✅ done |
| 5 | **`rsud-android-client-usf`** (E6) | Hapus `syncUsers` + tabel `UserEntity`: `auth/users` admin-only (ADR-0008) → inspector selalu 403; lookup nama petugas pakai `currentUser` (dari `auth/me`), fallback "Petugas #ID" | ✅ independen (bisa dikerjakan sekarang; terkait E1 karena `name` sudah ada di `UserEntity` yang akan dihapus) | grep `auth/users`/`UserEntity` bersih di main source; `syncMasterData` tanpa langkah Users; detail riwayat tampil `name`/`username` user login, selain itu "Petugas #ID"; test hijau | ✅ done |
| — | **`rsud-android-client-v63`** (H3) | syncError basi | ❌ **DEFERRED +4w — jangan claim** | — | ❄ deferred |
| 6 | **`rsud-android-client-8vt`** (payung) | Payung ADR-0017 — E1–E4 selesai; E6 dilacak terpisah | — | `bd list` → semua done | ✅ done |

## Aturan main

1. **Claim satu per satu** (kecuali E2 ↔ E3 ↔ E4 yang boleh paralel): `bd update <id> --claim`.
2. **Jangan mulai E2 sebelum E1 selesai** (header butuh field `name`).
3. E3 & E4 **tidak butuh menunggu** — bebas di-claim oleh agent kedua/ketiga secara paralel.
4. Setelah selesai: `bd update <id> --status done`, dan **update graph** bila struktur file berubah besar (`graphify extract` — lihat AGENTS.md).
5. Semua test harus hijau sebelum `done` (`./gradlew :app:testDebugUnitTest`).
6. H3 (`v63`) dikerjakan **hanya** setelah konfirmasi user — jangan un-defer sendiri.

## Konteks keputusan (ringkas)

- Android = klien `inspector` saja (ADR-0017); supervisor/admin_ppi → web dashboard.
- Boundary client-side penuh; tanpa klaim enforcement server-side.
- Dashboard final: header `nama · username`, grid 3 card (Draf, Menunggu Kirim, Terkirim) + card Belum/Sudah Diinspeksi.
- "Aksi Cepat" (Riwayat) dihapus — duplikat card "Sudah Diinspeksi".

## Catatan eksekusi (2026-08-01)

- E1: `name` ditambahkan nullable di akhir `UserOut`/`UserEntity` (kompatibel konstruktor positional), `TokenData` menyimpan `name` (restore sesi tetap tampil), Room v5→v6 via `fallbackToDestructiveMigration` (pola project, cache di-sync ulang). `showAll` dihapus dari `SyncApi.getInspections` + `InspectionHistoryRepository.fetchInspections`.
- E2: `AnalyticsApi.kt`, `RoomScoreCard.kt`, `IssueCard.kt` dihapus; `provideAnalyticsApi` dari AppModule; `fetchAnalytics`/state analytics + branch role dihapus dari `DashboardViewModel`; `authRepository` tidak lagi dipakai `MasterDataViewModel` (filter `isMyRoom` selalu). Grid Ringkasan = Draf, Menunggu Kirim, Terkirim.
- E3: pesan tolak login di-hardcode mengikuti konvensi codebase (tidak ada `R.string` di main source; ADR-0017 juga hardcode). 4 skenario test baru di `AuthRepositoryTest`.
- E4: §4.3 ditulis ulang, klaim `show_all`/admin-only users dibetulkan, tabel endpoint dashboard ditandai web-only.
- E6 (2026-08-01, audit logcat): `GET /api/auth/users` ternyata **admin-only** (ADR-0008 + auth/CONTEXT.md:65) — langkah "Users" di `syncMasterData` selalu 403, cache `UserEntity` selalu kosong, dan satu-satunya konsumennya (lookup nama petugas di detail riwayat) bisa dipenuhi `auth/me` karena Android inspector-only. Dampak dokumen yang perlu di-update: `android-implementation-guide.md` (~59, 168, 534, 550), `android-to-be-api-contract.md` (~496), `adr/0013` (~85, 111), `auth/CONTEXT.md:59`, `core/CONTEXT.md:51`.
- E6 eksekusi (2026-08-01): `syncUsers()`/`usersPerPage` dihapus dari `MasterDataRepository`; langkah "Users" dihapus dari `syncMasterData`; `getUsers()` + import `PaginatedResponse` dihapus dari `AuthApi`; DAO `getAllUsers/getUserById/insertUsers/clearUsers` + registrasi `UserEntity` dihapus; `UserEntity.kt` dihapus; Room v6→v7 (`fallbackToDestructiveMigration`, pola project); `clearUsers()` dihapus dari `clearLocalCache`; `InspectionHistoryViewModel` inject `AuthRepository` — `loadDetail()` tampil `name · username` user login jika `inspectorId` cocok, selain itu "Petugas #ID". Komentar eksplanatori `auth/users` di main source dibiarkan (menjelaskan kenapa dihapus). Test disesuaikan (4 test syncUsers dihapus dari `MasterDataRepositoryTest`, hitungan langkah 6→5 di `SyncManagerTest`, stub `authRepository.currentUser` di `InspectionHistoryViewModelTest`, `clearUsers` dihapus dari `AuthRepositoryTest`); `./gradlew :app:testDebugUnitTest` hijau; graphify re-index (UserEntity.kt dihapus).
- Verifikasi: `./gradlew :app:testDebugUnitTest` hijau; grep `showAll`/analytics/main-source bersih; graphify di-re-index (3 file dihapus).
