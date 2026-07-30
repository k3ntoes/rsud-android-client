# Spec: Dashboard Inspection Status Cards (Inspector Role)

**Tanggal**: 2026-07-30
**Status**: Draft (menunggu implementasi)

## Ringkasan

Menambahkan dua card informatif di dashboard untuk role **inspector** yang menunjukkan status inspeksi hari ini, lengkap dengan aksi navigasi.

## Glossary (key terms)

| Term | Definisi |
|------|----------|
| **Sudah Diinspeksi** | Ruangan yang memiliki catatan di `DrafInspeksi` ATAU `InspectionEntity` dengan `businessDate` = hari ini |
| **Belum Diinspeksi** | Ruangan yang TIDAK memiliki catatan sama sekali untuk `businessDate` = hari ini |
| **Tanggal Hari Ini** | `businessDate` format `YYYY-MM-DD`, dihitung dari tanggal perangkat saat ini |

## Layout Dashboard (Inspector Role)

```
┌─ TopAppBar ──────────────────────────────┐
│ Dashboard                                 │
│ username · role                [Keluar]   │
├───────────────────────────────────────────┤
│ Ringkasan Inspeksi                        │
│ ┌─ Draf ──────clickable──┐ ┌─ Menunggu ─┐│
│ │       2                │ │      1      ││
│ └────────────────────────┘ └────────────┘│
│ ┌── Terkirim ──┐ ┌─ Total Inspeksi ───┐  │
│ │      5       │ │        8           │   │
│ └──────────────┘ └────────────────────┘  │
│                                           │
│ Status Inspeksi Hari Ini                  │
│ ┌─ Blm Diinspeksi ─────┐ ┌ Sdh Diinspek ┐│
│ │        12            │ │      6       ││
│ │ Ketuk utk mulai      │ │ Ketuk lht    ││
│ └──────────────────────┘ └─────────────┘│
│                                           │
│ Master Data                               │
│ ┌── Ruangan ──┐ ┌──── Item ────┐         │
│ │     18      │ │     45       │         │
│ └─────────────┘ └──────────────┘         │
│                                           │
│ Aksi Cepat                                │
│ [────── Riwayat Inspeksi ──────]          │
│                                           │
│ Ruangan dengan Skor Terendah              │
│ [...analytics...]                         │
│                                           │
│ Temuan Paling Sering                      │
│ [...analytics...]                         │
│                                           │
│ Aktivitas Terbaru                         │
│ [...recent drafts...]                     │
└───────────────────────────────────────────┘
```

## Perubahan pada Komponen yang Ada

### 1. DashboardScreen.kt
- **Hapus** tombol "Inspeksi Baru" dari section "Aksi Cepat"
- **Hapus** tombol "Lihat Draf" dari section "Aksi Cepat"
- **Pertahankan** tombol "Riwayat Inspeksi" di section "Aksi Cepat"
- **Tambah** section baru "Status Inspeksi Hari Ini" dengan 2 card
- **Tambah** callback `onNavigateToUninspectedRooms` untuk card "Belum Diinspeksi"
- **Tambah** callback `onNavigateToHistoryWithDate` untuk card "Sudah Diinspeksi"

### 2. StatCard.kt
- **Tambah** parameter opsional `onClick: (() -> Unit)? = null`
- Jika `onClick != null`, card menjadi clickable (efek elevasi/interaction)

### 3. DashboardViewModel.kt
- **Tambah** state: `inspectedRoomCount: Int` dan `uninspectedRoomCount: Int`
- **Tambah** logic: query dari `MasterDataDao` untuk mendapat roomIds yang sudah diinspeksi hari ini (dari `draf_inspeksi` dan `inspection` tabel)

### 4. MasterDataDao.kt
- **Tambah** query: `getDraftRoomIdsForDate(date)` → `SELECT DISTINCT roomId FROM draf_inspeksi WHERE businessDate = :date`
- **Tambah** query: `getInspectedRoomIdsForDate(date)` → `SELECT DISTINCT roomId FROM inspection WHERE businessDate = :date`

### 5. MasterDataRepository.kt
- **Tambah** method: `getInspectedRoomIdsForDate(date)` → union dua query DAO

### 6. MasterDataViewModel.kt
- **Tambah** state: `excludeRoomIds: Set<Long>` — untuk filter ruangan yang sudah diinspeksi
- **Tambah** method: `setUninspectedFilter(date)` — set `excludeRoomIds`
- **Update** combine block: filter rooms jika `excludeRoomIds` tidak kosong

### 7. NavGraph.kt
- **Tambah** route parameter `filterDate` untuk `INSPECTION_HISTORY` route
- **Tambah** route parameter `uninspectedOnly` + `date` untuk `INSPECTION_LIST` route
- **Tambah** navigasi baru dari Dashboard ke filtered room selection

### 8. InspectionHistoryViewModel.kt
- **Tambah** state: `filterDate: String?`
- **Tambah** method: `setFilterDate(date)` — update filter + refresh cache
- **Update** `collectCache()` dan `refreshFromServer()` untuk menggunakan filter date

### 9. InspectionListScreen.kt
- **Tambah** indikator filter tanggal aktif (misal: chip atau label "Hari Ini")

### 10. MasterDataListScreen.kt
- **Update** untuk menerima parameter uninspectedOnly + date
- **Filter** daftar ruangan berdasarkan excludeRoomIds

## Navigasi Flow

### Card "Belum Diinspeksi" → Click
```
Dashboard → Room Selection (filtered: only uninspected rooms for today)
         → Room clicked → InspectionFormScreen
```

### Card "Sudah Diinspeksi" → Click
```
Dashboard → InspectionHistory (filterDate = today, filter lokal via Room DB)
```

### Card "Draf" (di Ringkasan Inspeksi) → Click
```
Dashboard → DaftarDrafScreen (sama seperti tombol Lihat Draf yang dihapus)
```

### Tombol "Riwayat Inspeksi" (di Aksi Cepat) → Click
```
Dashboard → InspectionHistory (tanpa filter date — semua riwayat)
```

## Data Flow

### Perhitungan Inspected/Uninspected Counts

```kotlin
// Di DashboardViewModel
suspend fun computeInspectionStatus(date: String) {
    val allRooms = masterDataDao.getAllRooms().first()
    val draftIds = masterDataDao.getDraftRoomIdsForDate(date)
    val inspectionIds = masterDataDao.getInspectedRoomIdsForDate(date)
    val allInspectedIds = (draftIds + inspectionIds).distinct()
    
    _uiState.value = _uiState.value.copy(
        totalRooms = allRooms.size,
        inspectedRoomCount = allInspectedIds.size,
        uninspectedRoomCount = allRooms.size - allInspectedIds.size
    )
}
```

### Filter Uninspected Rooms

```kotlin
// Di MasterDataRepository
suspend fun getInspectedRoomIdsForDate(date: String): Set<Long> {
    val draftIds = masterDataDao.getDraftRoomIdsForDate(date)
    val inspectionIds = masterDataDao.getInspectedRoomIdsForDate(date)
    return (draftIds + inspectionIds).toSet()
}
```

## Daftar File yang Berubah

| File | Perubahan |
|------|-----------|
| `MasterDataDao.kt` | +2 query baru |
| `MasterDataRepository.kt` | +1 method baru |
| `DashboardViewModel.kt` | +4 state fields, +1 computation coroutine |
| `DashboardScreen.kt` | Layout baru: 2 card + hapus 2 tombol |
| `StatCard.kt` | +`onClick` parameter |
| `MasterDataViewModel.kt` | +excludeRoomIds filter |
| `MasterDataListScreen.kt` | +uninspectedOnly mode |
| `NavGraph.kt` | +route params untuk filter |
| `InspectionHistoryViewModel.kt` | +date filter |
| `InspectionListScreen.kt` | +date filter indicator |

## Referensi

- `docs/adr/0014-media-store-photo-storage.md` — Photo storage & retention (keputusan terkait)
- `docs/adr/0013-hybrid-inspection-history.md` — Hybrid cache strategy
- `app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md` — Glossary
