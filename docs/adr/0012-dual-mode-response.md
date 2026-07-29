# ADR-0012: Dual Mode Response — PaginatedResponse & SyncResponse

**Status**: Accepted

**Tanggal**: 2026-07-29

## Context

Backend aplikasi RSUD Ajibarang memiliki endpoint master data (`GET /api/rooms`, `GET /api/inspection-items`) yang digunakan oleh dua klien dengan kebutuhan berbeda:

1. **Web Admin Dashboard** — menampilkan data dalam tabel dengan pagination (halaman 1, 2, 3...), membutuhkan `total`, `page`, `per_page`
2. **Android Client** — menyimpan seluruh data ke storage lokal untuk akses offline, membutuhkan data lengkap (tidak terpotong halaman) dan timestamp sync

Endpoint yang sama harus melayani dua kebutuhan ini. Solusi umum seperti "buat endpoint terpisah" atau "pagination + mirror endpoint" memiliki biaya maintenance yang tidak proporsional untuk project kecil ini.

## Considered Options

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Dual mode via query param — dipilih** | 1 endpoint, 2 mode via `?since=`. Backend return `PaginatedResponse` (tanpa since) atau `SyncResponse` (dengan since). Android selalu kirim `?since=` (first-time: `1970-01-01T00:00:00Z`). | Backend perlu logic branching berdasarkan ada/tidaknya `?since=`. |
| **Dua endpoint terpisah** | Masing-masing endpoint punya return type yang jelas. Retrofit mudah dipetakan. | Duplikasi endpoint, kompleksitas maintenance backend naik 2x. Tidak proporsional untuk ~40 source files Android. |
| **Pagination saja (Android paksa pake pagination)** | 1 endpoint, 1 response format. | Android harus fetch semua halaman → banyak request serial (lambat, tidak efisien untuk sync). |
| **Sync saja (Web paksa pake sync response)** | 1 endpoint, 1 format. | Web Admin kehilangan informasi pagination (`total`, `total_pages`). UX dashboard jadi jelek. |

## Decision

**Gunakan dual mode via query parameter `?since=`.**

### Mekanisme

```
GET /api/rooms?since=2026-07-28T00:00:00Z
→ SyncResponse: { data: [...], synced_at: "..." }

GET /api/rooms?page=1&per_page=20
→ PaginatedResponse: { items: [...], total: 142, page: 1, per_page: 20, total_pages: 8 }
```

### Aturan Android

1. **Android SELALU** menggunakan mode `?since=`
2. **First-time sync**: kirim `since=1970-01-01T00:00:00Z` agar backend return `SyncResponse`
3. **Sync berikutnya**: kirim `since=<synced_at_dari_response_sebelumnya>`
4. Android **tidak perlu** handle dua tipe response — satu Retrofit interface cukup dengan `SyncResponse<T>`

### Aturan Backend

1. Jika request memiliki `?since=`, return `SyncResponse<T>` → `{ data, synced_at }`
2. Jika request **tidak** memiliki `?since=`, return `PaginatedResponse<T>` → `{ items, total, page, per_page, total_pages }`
3. `synced_at` adalah timestamp server saat query dijalankan (ISO 8601)
4. Saat `since` dikirim, **abaikan parameter pagination** (`page`, `per_page`)

## Consequences

### Positif

- Satu endpoint master data melayani dua klien
- Android mendapat data lengkap untuk offline storage dalam 1 request
- Web Admin mendapat pagination untuk UX dashboard
- Backend tidak perlu menambahkan endpoint baru

### Negatif

- Branching logic di backend berdasarkan ada/tidaknya `?since=`
- `SyncResponse` bisa sangat besar jika data banyak (mitigasi: `updated_at` filter mengurangi volume)
- Android perlu mengirim `since=1970-01-01` untuk first-time sync — sedikit hacky tapi efektif
- Retrofit di Android perlu parameter opsional `@Query("since") since: String? = null`

## Referensi

- `docs/android-to-be-api-contract.md` — Spesifikasi endpoint
- `docs/android-implementation-guide.md` — Sync mode vs pagination mode
