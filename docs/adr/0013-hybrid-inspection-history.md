# ADR-0013: Hybrid Inspection History — Local Cache + Server Fetch

**Status**: Accepted

**Tanggal**: 2026-07-29

## Context

Aplikasi perlu menampilkan riwayat inspeksi yang sudah dikirim ke server. Ada dua sumber data potensial:

1. **Lokal** — hasil submit inspeksi yang disimpan sebelum draf dihapus. Instant, offline, tapi bisa basi (status berubah jika Supervisor APPROVED/REJECTED).
2. **Server** — data terbaru dari endpoint `GET /api/inspections`. Selalu akurat, tapi butuh koneksi internet dan ada latency.

Keputusan ini menentukan bagaimana Android menggabungkan kedua sumber untuk memberikan UX yang cepat (instant load) sekaligus akurat (data terbaru).

## Considered Options

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Online-only (fetch dari server tiap lihat history)** | Data selalu akurat. Tidak perlu storage tambahan. | Loading state setiap kali — tidak ada data saat offline. UX lambat karena harus nunggu network. |
| **Local-only (simpan hasil submit, jangan dihapus)** | Instant load, offline-ready. | Data bisa basi (status tidak terupdate). Storage bertambah terus. Perlu cache invalidation logic. |
| **Hybrid (cache lokal + fetch dari server) — dipilih** | Instant load dari cache + update dari server. Bekerja offline (pakai cache). | Kompleksitas sedang — perlu sync logic antara cache dan server. Dua tabel Room tambahan. |

### Detail Hybrid

```text
Layer 1 (Cache): InspectionEntity, InspectionDetailEntity, InspectionPhotoEntity
  - Diisi saat submit sukses (syncSingleDraft → simpan InspectionOut)
  - Instant load untuk layar history

Layer 2 (Fetch): GET /api/inspections + GET /api/inspections/{id}
  - Panggil API saat masuk layar history
  - Update cache dengan data terbaru dari server
  - Jaringan offline → fallback ke cache

Strategy: "Tampilkan cache dulu, refresh dari server"
  1. Load dari Room → tampilkan (50ms)
  2. Panggil API → update Room → update UI (200-500ms)
```

### Sinkronisasi Cache

```kotlin
// Hybrid fetch strategy
fun getInspections(): Flow<List<InspectionListItem>> {
    // 1. Emit cache dulu
    val local = inspectionDao.getAll()  // Flow dari Room
    // 2. Trigger fetch dari server
    viewModelScope.launch {
        try {
            val remote = api.getInspections(page = 1, perPage = 20)
            inspectionDao.upsertAll(remote.items)
            // Room Flow otomatis emit ulang dengan data baru
        } catch (e: IOException) {
            // Offline — tetap pakai cache
        }
    }
    return local
}
```

## Decision

**Gunakan hybrid**: cache lokal + fetch dari server.

### Mekanisme

1. **Saat submit inspeksi sukses** (`POST /api/inspections` return 200): simpan `InspectionOutDto` ke tabel lokal (`InspectionEntity` + `InspectionDetailEntity` + `InspectionPhotoEntity`)
2. **Saat membuka layar History**: load dari cache lokal dulu (instant), lalu refresh dari server
3. **Saat refresh**: panggil `GET /api/inspections?page=1&per_page=20`, update cache, Room Flow otomatis emit ulang
4. **Saat offline**: fallback penuh ke cache lokal — user tetap bisa lihat history yang pernah di-sync
5. **Pagination**: server-driven via `page`/`per_page`, infinite scroll

### Yang Disimpan

| Entity | Field Kunci | Sumber |
|--------|-------------|--------|
| `InspectionEntity` | `id`, `roomId`, `inspectorId`, `status`, `businessDate` | Response submit & list |
| `InspectionDetailEntity` | `id`, `inspectionId`, `itemId`, `itemNameSnapshot`, `score` | Response detail |
| `InspectionPhotoEntity` | `id`, `detailId`, `photoFileName`, `sortOrder` | Response detail |

### Yang TIDAK Disimpan

- `room_name` — lookup dari `RoomEntity` lokal (data master yang sudah di-sync)
- `inspector_name` — lookup dari `UserEntity` lokal (sync via `GET /api/auth/users`)

## Consequences

### Positif

- Instant load dari cache (tanpa spinner menunggu API)
- Data tetap akurat karena di-refresh dari server
- Bekerja offline penuh (data yang pernah di-cache bisa dilihat tanpa koneksi)
- Tidak perlu storage besar — hanya inspeksi yang sudah dikirim yang di-cache

### Negatif

- Kompleksitas: perlu 3 tabel Room tambahan + DAO methods
- Race condition: cache bisa tampil dulu, lalu tiba-tiba berubah saat refresh selesai
- Storage bertambah seiring jumlah inspeksi (mitigasi: bisa di-prune nanti)
- `InspectionOut` dari response submit (POST) mungkin berbeda format dengan `InspectionOut` dari response detail (GET) — perlu diverifikasi

## Referensi

- `docs/android-to-be-api-contract.md` — Inspection response schemas
- `docs/android-implementation-guide.md` — Hybrid strategy
