# ADR-0018: Inspection Submit — Definisi Sukses, Catatan ke Server, dan Limitation Foto Yatim

**Status**: Accepted (grill-with-docs session 2026-08)

## Context

Sesi *grill-with-docs* menelusuri alur kirim inspeksi dari draf hingga benar-benar
terkirim ke server (`graphify query` + trace kode). Empat keputusan muncul, dua di
antaranya mengubah perilaku kode, dua lagi menetapkan dokumentasi kontrak/limitation.

## Decisions

### Q1 — Definisi "terkirim": server 200 + cache riwayat lokal wajib

Sebelumnya: sukses = `POST /inspections` membalas 200 dengan id → draf dihapus. Namun
path `409 DUPLICATE_INSPECTION` menganggap sukses TANPA menulis cache riwayat lokal —
inspeksi itu hilang dari dashboard "Terkirim" dan riwayat sampai fetch ulang berikutnya.

**Diputuskan**: "terkirim" = server mengakui inspeksi (200 dengan id) **dan** cache
riwayat lokal (`InspectionEntity`) tertulis. Path 409 wajib menulis cache juga:
`SyncManager` memanggil `InspectionHistoryRepository.cacheDuplicateInspection(roomId, businessDate)`
— cari id via `GET /inspections`, fetch detail, lalu cache. **Kunci pencocokan**: `roomId + businessDate`
(kontrak BE `InspectionListItem` tidak memuat `local_timestamp` — REVIEW-FIX 2026-08);
ambil kandidat terbaru (id maksimal) karena satu room bisa diinspeksi lebih dari sekali
per hari. Best-effort: kegagalan cache tidak menggagalkan penghapusan draf.

- Kenapa bukan "cukup 200": konsistensi riwayat/dashboard tanpa menunggu fetch ulang,
  dan pengguna tidak melihat inspeksi yang sebenarnya sudah terkirim.
- Trade-off: satu panggilan `GET /inspections` ekstra di path 409 (jarang).

### Q2 — Catatan per-item ikut dikirim ke server

Sebelumnya: form menyimpan `catatan` di draf, tapi `DetailSubmit` tidak punya field
catatan — catatan **hilang saat submit** (draf dihapus setelah sukses).

**Diputuskan**: `DetailSubmit` (Android) + kontrak API §4.1 mendapat field `catatan`
(string/null, opsional). BE perlu menambahkan kolom `inspection_details.catatan` +
field di `DetailSubmit`/`DetailOut` — Pydantic v2 default mengabaikan field tak dikenal,
jadi Android sudah mengirim tanpa memblokir.

### Q3 — Foto yatim server: known limitation

Alur upload dua langkah: (1) upload semua foto, (2) submit JSON. Jika submit gagal
setelah sebagian foto ter-upload, foto tersebut jadi file yatim di server, dan retry
meng-upload ulang semua foto dari awal.

**Diputuskan**: terima sebagai *known limitation* untuk MVP. Retry idempotent di level
inspeksi (idempotency key) sudah mencegah duplikat inspeksi; file yatim hanya makan
storage server, bukan data korup. Opsi lanjutan (endpoint `DELETE /upload/{fileName}`
atau cleanup berkala di BE) dicatat sebagai follow-up, tidak dikerjakan sekarang.

### Q4 — Satu panggilan sync master data per run worker

Sebelumnya: `SyncWorker.doWork()` memanggil `syncMasterData()`, lalu `syncAllPending()`
memanggilnya **lagi** — dua kali per run.

**Diputuskan**: hapus panggilan di `doWork()`; `syncAllPending()` tetap menjalankan
master data sync lebih dulu sebelum memproses draf. Hemat satu round-trip per run,
antrean draf terkuras lebih cepat.

## Consequences

- Dashboard "Terkirim" & riwayat inspeksi konsisten bahkan saat submit terkena 409.
- Catatan inspektur tersimpan di server (data tidak hilang saat draf dihapus).
- Sync worker lebih ringan (tanpa double master-data sync).
- Foto yatim server tetap mungkin terjadi pada kegagalan parsial — didokumentasikan,
  penanganan otomatis ditunda.
- BE perlu: kolom `inspection_details.catatan` (Q2) — tidak blocking, Pydantic
  mengabaikan field ekstra sampai kolom ada.
