# Claim Order — Dashboard Inspector-Only (ADR-0017)

> Buat agent lain: daftar kerja eksekusi ADR-0017 + keputusan sesi grill 2026-08-01.
> **Langkah #0 untuk SEMUA issue: baca `CODING-RULES.md` sebelum claim/implement.**

## Diagram Dependensi

```
E1 (5v8) ──▶ E2 (d7a)          E4 (y20) — boleh kapan saja
E3 (vmj) ── independen         H3 (v63) — DEFERRED, jangan claim
```

## Checklist (urutan claim)

> **Status 2026-08-01: E1–E4 SELESAI** — semua `done` via `bd`. Payung `8vt` ditutup. H3 tetap deferred.

| Urutan | Issue | Isi singkat | Boleh paralel | Verifikasi wajib | Status |
|--------|-------|-------------|---------------|------------------|--------|
| 1 | **`rsud-android-client-5v8`** (E1) | Field `name` di `UserOut`/`UserEntity` (+cek migrasi Room) + hapus `showAll` dead param | — | `grep showAll` bersih, `:app:testDebugUnitTest` hijau | ✅ done |
| 2 | **`rsud-android-client-d7a`** (E2) | Verifikasi hapus Aksi Cepat (edit sudah diterapkan, blm di-build), TopAppBar `nama · username`, hapus analytics + card Total Inspeksi + branch admin | — (deps E1) | compile + `testDebugUnitTest` hijau, grep analytics bersih | ✅ done |
| 3 | **`rsud-android-client-vmj`** (E3) | Tolak login non-inspector; force-logout sesi lama di `init()`/`refreshCurrentUser()` | ✅ paralel dengan E2 (dan E4) | 4 skenario test (login ok, login ditolak, init logout, refresh logout) | ✅ done |
| 4 | **`rsud-android-client-y20`** (E4) | Update `docs/android-implementation-guide.md` §4.3 + baris `show_all`/admin; sinkronkan dgn CONTEXT.md & ADR-0017 | ✅ kapan saja | grep docs bersih dari pernyataan kontradiktif | ✅ done |
| — | **`rsud-android-client-v63`** (H3) | syncError basi | ❌ **DEFERRED +4w — jangan claim** | — | ❄ deferred |
| 5 | **`rsud-android-client-8vt`** (payung) | Tutup setelah E1-E4 selesai | — | `bd list` → semua done | ✅ done |

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
- Verifikasi: `./gradlew :app:testDebugUnitTest` hijau; grep `showAll`/analytics/main-source bersih; graphify di-re-index (3 file dihapus).
