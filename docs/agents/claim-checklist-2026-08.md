# Checklist Claim Order — Implementasi Review 2026-08

Hasil sesi grill dashboard (keputusan review 2026-08) + perubahan kontrak BE (`docs/android-to-be-api-contract.md`). Glosarium sudah di-update di `CONTEXT.md` domain — implementasi tinggal mengikuti keputusan ini.

> ⚠️ **Langkah #0 setiap claim**: baca `CODING-RULES.md` SEBELUM `bd update --claim` atau menulis kode.
> Sebelum edit simbol: jalankan impact analysis (GitNexus). Sebelum commit: `detect_changes()`.

## Issues

| # | ID | Issue | Effort | Blokir |
|---|----|-------|--------|--------|
| 1 | `rsud-android-client-01n` | Fix: tombstone `is_active` di sync pivot (room-items & user-rooms) | 90m | blokir #6 (`86i`) |
| 2 | `rsud-android-client-35h` | Dashboard: auto-sync cache kosong + indikator sync + pull-to-refresh + retry | 150m | blokir #3 (`hyz`) |
| 3 | `rsud-android-client-hyz` | Dashboard: metrik Status Inspeksi Hari Ini + Terkirim/Total dari `InspectionEntity` | 120m | — |
| 4 | `rsud-android-client-9na` | Dashboard: analytics hanya supervisor/admin + hapus dead code | 90m | — |
| 5 | `rsud-android-client-hl7` | Inspeksi Ulang: tombol di detail → form kosong room sama | 90m | — |
| 6 | `rsud-android-client-86i` | Form & RoomCard: item count dari pivot + hapus fallback item kosong | 60m | butuh #1 (`01n`) |
| 7 | `rsud-android-client-ksc` | Form: tampilkan deskripsi item dari entity (bukan null) | 15m | — |

**Tanpa issue (sudah didokumentasikan, bukan perubahan kode):**
- `local_timestamp` non-UTC dibiarkan — lihat inspections/CONTEXT.md "Waktu Buat".

## Urutan Claim (wajib)

Claim SATU per satu, selesaikan + verifikasi, baru claim berikutnya:

```
1. bd update rsud-android-client-01n --claim   # pivot tombstone — fondasi data
2. bd update rsud-android-client-35h --claim   # dashboard sync UX (seri DashboardViewModel #1)
3. bd update rsud-android-client-hyz --claim   # metrik dashboard (seri #2 — file sama dengan #2, jangan paralel)
4. bd update rsud-android-client-9na --claim   # analytics gate + dead code (seri #3 — file sama, jangan paralel)
5. bd update rsud-android-client-hl7 --claim   # inspeksi ulang (file terpisah, boleh interleave dengan 6-7)
6. bd update rsud-android-client-86i --claim   # item count dari pivot (butuh #1 selesai)
7. bd update rsud-android-client-ksc --claim   # deskripsi item — quick win, boleh claim bareng #6
```

### Aturan paralelisme

| Bisa paralel | Alasan |
|--------------|--------|
| #1 + #5 + #7 | File disjoint (api/repo vs detail/nav vs form UI) |
| #6 + #7 | Form UI berdekatan, sebaiknya dikerjakan berurutan oleh agent yang sama |

| JANGAN paralel | Alasan |
|----------------|--------|
| #2, #3, #4 | Ketiganya menyentuh `DashboardViewModel.kt` + `DashboardScreen.kt` — konflik besar |

## Per Issue

### 1. `rsud-android-client-01n` — Tombstone pivot (Kontrak §2.2/§2.3)
- [ ] `RoomItemDto` + `UserRoomDto`: tambah `is_active` + `updated_at`
- [ ] `syncRoomItems()`/`syncUserRooms()`: filter hanya `is_active == true`
- [ ] Komentar `replacePhoto` di SyncApi.kt: hapus "Endpoint belum ada di backend" (BE sudah implement, ADR-0012)
- [ ] Test: tombstone tidak masuk mapping lokal
- [ ] `./gradlew :app:testDebugUnitTest`

### 2. `rsud-android-client-35h` — Sync UX dashboard (Q7)
- [ ] Auto-sync saat cache kosong (guard anti-loop)
- [ ] Indikator: "Terakhir sync" / "Menyinkronkan..." / "Sync gagal — ketuk retry" (dari `SyncStateStore`)
- [ ] Pull-to-refresh
- [ ] Test DashboardViewModel

### 3. `rsud-android-client-hyz` — Metrik dashboard (Q1+Q2)
- [ ] Scope Sudah/Belum: `isMyRoom` (inspector/supervisor), semua room (`admin_ppi`)
- [ ] Terkirim & Total dari `InspectionEntity` (bukan draf SYNCED)
- [ ] Test per role

### 4. `rsud-android-client-9na` — Analytics gate + dead code (Q6+Q6b)
- [ ] Inspector: tidak render & tidak fetch analytics
- [ ] Hapus `serverPendingCount`, `serverMonthlyCount`, `serverAvgScorePct`, `isForbidden`, `fetchDashboard()`, `AnalyticsApi.getDashboard()`, `DashboardDto`
- [ ] `fetchAnalytics()` tetap untuk supervisor/admin

### 5. `rsud-android-client-hl7` — Inspeksi Ulang (Q4)
- [ ] Tombol "Inspeksi Ulang" di detail inspeksi terkirim
- [ ] Buka form KOSONG room yang sama (bukan resume draf)
- [ ] Jalur "Draf" → Resume tetap jalan

### 6. `rsud-android-client-86i` — Item count + fallback (Q5+Q6)
- [ ] RoomCard count dari pivot `room_items` (pakai `getRoomItemMap()`), hapus heuristik string L165-168
- [ ] Pivot kosong → form tampil "Tidak ada item untuk ruangan ini" (hapus fallback L68-74)

### 7. `rsud-android-client-ksc` — Deskripsi item (Q8)
- [ ] `deskripsi = item.deskripsi` (bukan null hardcoded)
- [ ] Item tanpa deskripsi → tampilan tidak berubah

## Verifikasi Akhir (setelah #7 selesai)

- [ ] `./gradlew :app:testDebugUnitTest` hijau
- [ ] Lint/typecheck yang berlaku (lihat CODING-RULES.md)
- [ ] `bd list` — semua issue `done` (`bd update <id> --status done`)
- [ ] `gitnexus detect_changes()` — pastikan hanya simbol yang diharapkan berubah
- [ ] Update graphify jika ada restrukturisasi besar (`graphify extract ./app/src/main/ --code-only --no-viz && cp ...`)
- [ ] `bd dolt push` — sinkronkan dengan remote
