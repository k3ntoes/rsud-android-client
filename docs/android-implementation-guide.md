# Panduan Implementasi Android — RSUD Ajibarang Server Stack

> **Versi:** 1.1
> **Tanggal:** 31 Juli 2026
> **Tujuan:** Panduan untuk tim Android dalam mengimplementasikan API terbaru — Pagination (server-driven), Relasi Room↔Item, Dashboard Endpoint, dan Sync Inkremental (`since`/`SyncStateStore`)

---

## ✅ Hasil Verifikasi Endpoint

**Status per 29 Juli 2026:** Seluruh endpoint yang dirujuk di panduan ini telah terverifikasi siap di Backend.

| Item | Status |
|------|--------|
| Total endpoint di Backend | **46 endpoint** — semua sudah berjalan di production |
| Endpoint yang dirujuk panduan | ✅ **Semua terimplementasi** — tidak ada endpoint yang perlu ditambahkan |
| Mismatch contract vs backend | ✅ **Sudah diperbaiki** — response `submit inspection` dan `get detail inspection` |
| Auth dual delivery (refresh token) | ✅ `POST /api/auth/refresh` menerima body `{ "refresh_token" }` — tidak perlu cookie |
| Sync master data (`?since=`) | ✅ `GET /api/rooms`, `GET /api/inspection-items`, `GET /api/room-items` |
| Assigned rooms (`/me/rooms`) | ✅ `GET /api/auth/me/rooms?since=...` — siap |
| User-Rooms bulk sync | ✅ `GET /api/auth/user-rooms?since=...` — endpoint baru ditambahkan |
| Dashboard | ⚠️ Analytics dashboard (`/api/analytics/*`) dipakai **web dashboard** saja — tidak diimplementasikan di Android (ADR-0017) |

> ⚠️ **Catatan penting:** Response `GET /api/inspections/{id}` (detail inspeksi) hanya mengembalikan `room_id` dan `inspector_id`, **bukan** `room_name` atau `inspector_name`. Android perlu melakukan lookup dari data yang sudah di-sync secara lokal. Lihat [Catatan Detail Inspeksi](#catatan-detail-inspeksi) untuk detailnya.

---

## 📋 Daftar Isi

1. [Ringkasan Perubahan](#1-ringkasan-perubahan)
2. [Pagination — Server-Driven](#2-pagination--server-driven)
3. [Relasi Room vs Inspection Items](#3-relasi-room-vs-inspection-items)
4. [Dashboard Endpoint](#4-dashboard-endpoint)
5. [Strategi Sync untuk Offline-First](#5-strategi-sync-untuk-offline-first)
6. [Contoh Kode Kotlin/Retrofit](#6-contoh-kode-kotlinretrofit)
7. [Prioritas Implementasi](#7-prioritas-implementasi)
8. [Lampiran: Detail Fields Inspeksi](#8-lampiran-detail-fields-inspeksi)

---

## 1. Ringkasan Perubahan

| Area | Perubahan | Endpoint Baru? |
|------|-----------|----------------|
| **Pagination** | Semua endpoint LIST sekarang pakai `page`/`per_page` + sorting | ❌ (perubahan response) |
| **Room Items** | Relasi Many-to-Maintenance via pivot `room_items` — badge items di setiap room | ✅ `/api/room-items` |
| **User Rooms** | Relasi user↔room via pivot `user_rooms` — bulk sync semua asosiasi | ✅ `/api/auth/user-rooms` |
| **My Rooms** | Room yang di-assign ke user login (filter UI) | ✅ `/api/auth/me/rooms` |
| **Dashboard** | Satu endpoint khusus dashboard — 4 card stat dalam 1 panggilan | ✅ `/api/analytics/dashboard` |

---

## 2. Pagination — Server-Driven

### 2.1. Endpoint yang Terkena

| No | Endpoint | Sebelum | Sesudah |
|----|----------|---------|---------|
| 1 | `GET /api/rooms` | `list[Room]` | `PaginatedResponse<Room>` (kecuali `?since=`) |
| 2 | `GET /api/inspection-items` | `list[Item]` | `PaginatedResponse<Item>` (kecuali `?since=`) |
| 3 | `GET /api/inspections` | `list[Inspection]` | `PaginatedResponse<Inspection>` |

> `GET /api/auth/users` **admin-only** (ADR-0008) — klien Android inspector (ADR-0017) selalu dapat 403, jadi tidak dipakai di Android. Nama petugas di lookup dari `auth/me` (lihat [Catatan Detail Inspeksi](#catatan-detail-inspeksi)).

### 2.2. Format Response Paginated

```json
{
  "items": [ ... ],           // Array data — selalu ada
  "total": 142,               // Total seluruh data (bukan halaman ini)
  "page": 1,                  // Halaman saat ini (1-indexed)
  "per_page": 20,             // Jumlah per halaman
  "total_pages": 8            // Total halaman = ceil(total / per_page)
}
```

### 2.3. Request Parameters

| Parameter | Tipe | Default | Validasi | Deskripsi |
|-----------|------|---------|----------|-----------|
| `page` | int | 1 | `ge=1` | Halaman (1-indexed) |
| `per_page` | int | 20 | `ge=1, le=10000` (master data) / `le=100` (auth/inspection) | Jumlah per halaman |
| `search` | string | null | - | Pencarian global |
| `sort_by` | string | null | Allowlist per model | Nama kolom untuk sorting |
| `sort_order` | string | "asc" | `"asc"` atau `"desc"` | Arah sorting |

### 2.4. Catatan Khusus

**⚠️ Dual mode untuk master data:**
Endpoint `/api/rooms` dan `/api/inspection-items` punya 2 mode:

1. **Web mode (tanpa `?since=`)** → return `PaginatedResponse`
   ```
   GET /api/rooms?page=1&per_page=20
   → { "items": [...], "total": ..., "page": 1, "per_page": 20, "total_pages": ... }
   ```

2. **Sync mode (dengan `?since=`)** → return `SyncResponse` (unpaginated)
   ```
   GET /api/rooms?since=2026-07-28T00:00:00Z
   → { "data": [...], "synced_at": "2026-07-29T..." }
   ```

> **Untuk Android:** ANDA HANYA PERLU SYNC MODE (`?since=`). Pagination digunakan oleh Web Admin Dashboard. Android fetch semua data via `?since=`.
>
> **Catatan ADR-0012:** First-time case (`since` kosong) sudah ditangani otomatis di `MasterDataRepository` via `resolveSince()` — param eksplisit > `synced_at` tersimpan di `SyncStateStore` > fallback epoch `1970-01-01T00:00:00Z`. Lihat [Strategi Sync](#5-strategi-sync-untuk-offline-first).

> ⚠️ **PENTING: First-time Sync (`since=null`)** — Saat `since` tidak dikirim (sync pertama kali), backend akan return `PaginatedResponse`, bukan `SyncResponse`! Untuk menghindari masalah parsing, **selalu kirim `since` dengan nilai epoch** untuk first-time sync:
> ```
> GET /api/rooms?since=1970-01-01T00:00:00Z
> ```
> Dengan cara ini, retrofit selalu pakai `SyncResponse<T>` tanpa perlu handle 2 tipe response berbeda.

### 2.5. Sorting yang Didukung (Allowlist)

| Model | Kolom yang Bisa di-Sort |
|-------|------------------------|
| `Room` | `name`, `is_active`, `updated_at` |
| `InspectionItem` | `name`, `is_active`, `updated_at` |
| `User` | `username`, `role`, `is_active`, `created_at` |
| `Inspection` | `business_date`, `status`, `created_at`, `room_id` |

---

### 2.6. Pagination di Android — Riwayat Inspeksi (ADR-0013)

`GET /api/inspections` memakai pagination **server-driven**: Android tidak menebak batas halaman, tapi mengikuti `total_pages` dari server.

**Request:**
```
GET /api/inspections?page=1&per_page=20&status=PENDING
```

| Parameter | Tipe | Default | Deskripsi |
|-----------|------|---------|-----------|
| `page` | int | 1 | Halaman (1-indexed) |
| `per_page` | int | 20 | Jumlah per halaman (max 100) |
| `status` | string | null | Filter status: `PENDING` / `APPROVED` / `REJECTED` |

> `show_all` TIDAK dipakai Android (param dihapus, ADR-0017): web dashboard supervisor yang memfilter semua room, bukan klien inspector. Klien Android selalu memakai scope room yang di-assign.

**Alur di Android** (`InspectionHistoryRepository` + `InspectionHistoryViewModel`):

```kotlin
data class PaginatedResult(
    val items: List<InspectionHistoryItem>,
    val totalPages: Int,
    val currentPage: Int
)

// Repository — terjemahkan response server apa adanya (tanpa tebak-tebakan)
suspend fun fetchInspections(page: Int, perPage: Int, status: String?): PaginatedResult {
    val response = syncApi.getInspections(page, perPage, status)
    return PaginatedResult(
        items = mapItems(response.items),
        totalPages = response.totalPages,
        currentPage = response.page
    )
}

// ViewModel — hasMorePages dari totalPages server (TANPA ceiling hardcoded)
hasMorePages = result.currentPage < result.totalPages
```

- **Infinite scroll**: `loadNextPage()` memuat `currentPage + 1` selama `hasMorePages == true` dan tidak sedang `isLoadingMore`.
- **Race protection `loadEpoch`**: setiap refresh / ganti filter menaikkan counter `loadEpoch`; hasil fetch dari epoch lama dibuang agar tidak menimpa state baru (mis. refresh yang tiba saat `loadNextPage` berjalan).
- **Scope room**: klien Android selalu menampilkan room yang di-assign (inspector-only, ADR-0017).

> ⚠️ `GET /api/auth/users` **admin-only** (ADR-0008) — inspector selalu 403. Android TIDAK sync daftar user; nama petugas detail riwayat diambil dari user login (`auth/me`), fallback `"Petugas #ID"` (E6, 2026-08-01).

---

## 3. Relasi Room vs Inspection Items

### 3.1. Model Relasi

```
┌──────────┐       ┌──────────────┐       ┌──────────────────┐
│   Room   │       │  room_items  │       │ InspectionItem   │
├──────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)  │──1:N──│ room_id (FK) │──N:1──│ id (PK)          │
│ name     │       │ item_id (FK) │       │ name             │
│ is_active│       │ created_at   │       │ is_active        │
│ updated_at     │              │       │ updated_at       │
└──────────┘       └──────────────┘       └──────────────────┘
```

**Ini Many-to-Many** — Satu room punya banyak items, satu item bisa dipakai di banyak room.

### 3.2. Endpoint untuk Relasi

#### A. GET /api/room-items — Semua relasi room↔item

**Request:**
```
GET /api/room-items?since=2026-07-28T00:00:00Z
```

**Response:**
```json
{
  "data": [
    { "id": 1, "room_id": 1, "item_id": 1, "created_at": "2026-07-28T10:00:00Z" },
    { "id": 2, "room_id": 1, "item_id": 2, "created_at": "2026-07-28T10:00:00Z" },
    { "id": 3, "room_id": 2, "item_id": 1, "created_at": "2026-07-28T10:00:00Z" }
  ],
  "synced_at": "2026-07-29T12:00:00Z"
}
```

**Schema:**
| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `id` | int | Primary key relasi |
| `room_id` | int | ID Room |
| `item_id` | int | ID Inspection Item |
| `created_at` | string (ISO 8601) | Waktu assignment |

#### B. GET /api/rooms/{roomId}/items — Items per room

```
GET /api/rooms/1/items
→ [ { "id": 1, "room_id": 1, "item_id": 1, "created_at": "..." }, ... ]
```

#### C. GET /api/inspection-items/{itemId}/rooms — Rooms per item

```
GET /api/inspection-items/1/rooms
→ [ { "id": 1, "room_id": 1, "item_id": 1, "created_at": "..." }, ... ]
```

### 3.3. Alur Implementasi Android

**Step 1: Sync dari server**
```text
1. GET /api/rooms?since=<ts>             → simpan daftar room
2. GET /api/inspection-items?since=<ts>  → simpan daftar items
3. GET /api/room-items?since=<ts>        → simpan mapping room↔item
4. GET /api/auth/user-rooms?since=<ts>   → simpan mapping user↔room
5. GET /api/auth/me/rooms?since=<ts>     → simpan room yg di-assign ke user
```

> **Catatan Step 4 (`GET /api/auth/user-rooms`)**: Endpoint ini mengembalikan **semua** asosiasi user↔room. Android bisa menggunakannya untuk:
> - Membangun `Map<Int, List<Int>>` (user → room IDs) — mengetahui room mana saja yang di-assign ke setiap inspector
> - Membangun `Map<Int, List<Int>>` (room → user IDs) — mengetahui inspector mana saja yang bertugas di suatu room
> - Data ini diperlukan untuk validasi dan analytics (tidak digunakan langsung di UI inspector)
> - **Tidak perlu di-sync oleh inspector biasa** jika hanya menampilkan `my-rooms`— tapi dibutuhkan oleh supervisor untuk dashboard

**Step 2: Bangun struktur data lokal**
```kotlin
// Data class untuk RoomItem
data class RoomItemDto(
    val id: Int,
    val roomId: Int,
    val itemId: Int,
    val createdAt: String
)

// Build mapping roomId → list of itemIds
val roomItemMap: Map<Int, List<Int>> = roomItems
    .groupBy({ it.roomId }, { it.itemId })

// Build lookup item name
val itemMap: Map<Int, String> = items.associate { it.id to it.name }

// Untuk menampilkan badge items di setiap room:
fun getItemNamesForRoom(roomId: Int): List<String> {
    return roomItemMap[roomId]
        ?.mapNotNull { itemMap[it] }
        ?: emptyList()
}
```

**Step 3: Validasi inspeksi offline**
```kotlin
// Saat user memilih room tertentu, cek items apa saja yang WAJIB di-score
fun getRequiredItemsForRoom(roomId: Int): List<Int> {
    return roomItemMap[roomId] ?: emptyList()
}
```

### 3.4. Data Class Room Out

```json
{
  "id": 1,
  "name": "UGD",
  "is_active": true,
  "updated_at": "2026-07-22T10:00:00Z"
}
```

```kotlin
data class RoomDto(
    val id: Int,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("updated_at") val updatedAt: String?
)
```

### 3.5. Data Class Item Out

```json
{
  "id": 1,
  "name": "Kebersihan Tangan",
  "is_active": true,
  "updated_at": "2026-07-22T10:00:00Z"
}
```

```kotlin
data class ItemDto(
    val id: Int,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("updated_at") val updatedAt: String?
)
```

### 3.6. Mapping Room → Item Names di UI

**Android Room Database (Room Persistence Library):**
```kotlin
// Tabel Room
@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)

// Tabel InspectionItem
@Entity(tableName = "inspection_items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: String?
)

// Tabel pivot RoomItem
@Entity(
    tableName = "room_items",
    foreignKeys = [
        ForeignKey(entity = RoomEntity::class, parentColumns = ["id"], childColumns = ["room_id"]),
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["item_id"])
    ],
    indices = [Index("room_id"), Index("item_id")]
)
data class RoomItemEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "room_id") val roomId: Int,
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
)
```

**Query Room dengan item names:**
```kotlin
@Dao
interface RoomDao {
    @Query("""
        SELECT r.*, GROUP_CONCAT(i.name, ', ') AS itemNames
        FROM rooms r
        LEFT JOIN room_items ri ON r.id = ri.room_id
        LEFT JOIN inspection_items i ON ri.item_id = i.id
        WHERE r.is_active = 1
        GROUP BY r.id
        ORDER BY r.name
    """)
    suspend fun getRoomsWithItems(): List<RoomWithItems>
}

data class RoomWithItems(
    val id: Int,
    val name: String,
    val isActive: Boolean,
    val itemNames: String?  // comma-separated item names
)
```

---

## 4. Dashboard Endpoint

### 4.1. GET /api/analytics/dashboard

Endpoint khusus untuk halaman dashboard — mengembalikan semua stat dalam 1 panggilan.

**Request:**
```
GET /api/analytics/dashboard?year_month=2026-07
```

**Response:**
```json
{
  "pending_count": 5,
  "total_rooms": 12,
  "monthly_inspection_count": 34,
  "avg_score_pct": 82.5
}
```

| Field | Tipe | Contoh | Deskripsi |
|-------|------|--------|-----------|
| `pending_count` | int | `5` | Jumlah inspeksi yang menunggu persetujuan |
| `total_rooms` | int | `12` | Jumlah ruangan aktif |
| `monthly_inspection_count` | int | `34` | Jumlah inspeksi yang sudah diapprove bulan ini |
| `avg_score_pct` | float | `82.5` | Rata-rata skor bulan ini (0–100) |

**Query params:**
| Parameter | Tipe | Default | Deskripsi |
|-----------|------|---------|-----------|
| `year_month` | string | `bulan saat ini` | Format `YYYY-MM`, filter bulan tertentu |

**Contoh response untuk Android:**
```json
{
  "pending_count": 3,
  "total_rooms": 15,
  "monthly_inspection_count": 47,
  "avg_score_pct": 78.3
}
```

### 4.2. Data Class

```kotlin
data class DashboardDto(
    @SerializedName("pending_count") val pendingCount: Int,
    @SerializedName("total_rooms") val totalRooms: Int,
    @SerializedName("monthly_inspection_count") val monthlyInspectionCount: Int,
    @SerializedName("avg_score_pct") val avgScorePct: Double
)
```

### 4.3. Auth & Role

**Android = klien `inspector` saja (ADR-0017).** `supervisor` dan `admin_ppi` menggunakan web dashboard.
Enforcement **client-side penuh**: login menolak role non-inspector dengan pesan jelas, dan sesi lama dicek ulang saat `init()`/`refreshCurrentUser()` — role non-inspector di-force-logout. Tidak ada klaim enforcement server-side (backend tidak membedakan client Android vs web).

**Penanganan di Android:**
```kotlin
// AuthRepository.login(): tolak SEBELUM menyimpan token
if (response.user.role != "inspector") {
    _authState.value = AuthState.Error("Akun ini hanya dapat digunakan via web dashboard")
    return false
}

// AuthRepository.init()/refreshCurrentUser(): force-logout sesi non-inspector
if (user.role != "inspector") forceLogout()
```

### 4.4. Endpoint Analytics Lainnya (web dashboard)

Endpoint analytics (`/analytics/lowest-rooms`, `/analytics/top-issues`, `/analytics/inspector-performance`, `/analytics/dashboard`) **hanya dipakai web dashboard** untuk `supervisor`/`admin_ppi` (ADR-0017). Tidak diimplementasikan di klien Android (analytics dihapus dari dashboard Android).

| Endpoint | Method | Deskripsi |
|----------|--------|-----------|
| `/api/analytics/lowest-rooms?year_month=2026-07&limit=3` | GET | 3 ruangan dengan skor terendah |
| `/api/analytics/top-issues?year_month=2026-07&limit=10` | GET | 10 item paling sering bermasalah |
| `/api/analytics/inspector-performance?year_month=2026-07` | GET | Kinerja inspector bulan ini |

Semua endpoint di atas membutuhkan role **Supervisor** atau **Admin PPI** — diakses via web dashboard.

---

## 4.5. Catatan Detail Inspeksi

**Endpoint**: `GET /api/inspections/{id}`

**Response aktual dari backend (`InspectionOut`):**
```json
{
  "id": 42,
  "room_id": 1,
  "inspector_id": 5,
  "status": "PENDING",
  "business_date": "2026-07-23",
  "local_timestamp": "2026-07-23T08:30:00Z",
  "rejection_reason": null,
  "created_at": "2026-07-23T08:30:00Z",
  "details": [
    {
      "id": 1,
      "item_id": 1,
      "item_name_snapshot": "Kebersihan Tangan",
      "score": 2,
      "photos": [
        {
          "id": 1,
          "photo_file_name": "uuid-photo-1.jpg",
          "thumbnail_file_name": null,
          "sort_order": 0
        }
      ]
    }
  ]
}
```

### ⚠️ Yang TIDAK Ada di Response

| Field | Keterangan |
|-------|-----------|
| `room_name` | ❌ **Tidak ada**. Backend hanya return `room_id`. |
| `inspector_name` | ❌ **Tidak ada**. Backend hanya return `inspector_id`. |
| `detail_count` | ❌ **Tidak ada**. Hitung dari `details.length` di Android. |

### Solusi: Lookup Manual dari Data Sync

Karena semua data master sudah di-sync secara lokal, Android bisa melakukan join sendiri:

```kotlin
// Data sudah ada di lokal setelah sync:
val rooms: Map<Int, RoomDto> = ...

// Saat render detail inspeksi:
fun getRoomName(inspection: InspectionOutDto): String {
    return rooms[inspection.roomId]?.name ?: "Room #${inspection.roomId}"
}

fun getDetailCount(inspection: InspectionOutDto): Int {
    return inspection.details.size
}
```

**⚠️ Inspector name:** `GET /api/auth/users` **admin-only** (ADR-0008) → inspector selalu 403, jadi Android TIDAK sync daftar user. Karena Android inspector-only (ADR-0017), detail riwayat menampilkan nama user LOGIN (`auth/me`): jika `inspector_id` == id user login → `name`/`username`; selain itu fallback `"Petugas #${inspectorId}"`.

---

## 5. Strategi Sync untuk Offline-First

### 5.1. Urutan Sync

```
Step 1: Auth → Login, dapatkan access_token + refresh_token
Step 2: Sync Rooms       → GET /api/rooms?since=<last_sync>                (inkremental)
Step 3: Sync Items       → GET /api/inspection-items?since=<last_sync>     (inkremental)
Step 4: Sync RoomItems   → GET /api/room-items?since=1970-01-01T00:00:00Z   (replace-all)
Step 5: Sync UserRooms   → GET /api/auth/user-rooms?since=1970-01-01T00:00:00Z (replace-all)
Step 6: Sync MyRooms     → GET /api/auth/me/rooms?since=1970-01-01T00:00:00Z   (replace-all)
Step 7: Simpan synced_at → via SyncStateStore, untuk sync berikutnya
```

> **Dua mode sync:**
>
> 1. **Inkremental** (`rooms`, `inspection-items`) — kirim `since=<synced_at tersimpan>`; server hanya mengembalikan data yang berubah sejak timestamp tersebut.
> 2. **Replace-all snapshot** (pivot: `room-items`, `user-rooms`, `my-rooms`) — endpoint mengembalikan **snapshot penuh** semua asosiasi, jadi **selalu minta `since=epoch`**. Setelah fetch **sukses**: clear tabel + insert ulang. Konsekuensinya relasi yang dihapus server ikut terhapus lokal (tidak butuh tombstone/deleted-flag). Jika fetch gagal, tabel lama tetap utuh — clear hanya dieksekusi setelah sukses.

### 5.2. SyncStateStore — Persistensi synced_at (ADR-0012)

`SyncStateStore` (SharedPreferences — timestamp non-sensitif, cukup prefs biasa, tidak butuh enkripsi Tink seperti token) menyimpan `synced_at` per endpoint.

**API `SyncStateStore`:**
- `load(): SyncState` — baca semua timestamp
- `save(state)` — tulis semua timestamp
- `update { }` — **atomic** read-modify-write; hindari lost-update kalau dua sync berjalan bersamaan
- `clear()` — dipanggil saat **logout / ganti akun** agar akun berikutnya sync penuh dari epoch

```kotlin
@Singleton
class SyncStateStore @Inject constructor(@ApplicationContext context: Context) {
    fun load(): SyncState
    fun save(state: SyncState)
    fun update(transform: (SyncState) -> SyncState)
    fun clear()
}

data class SyncState(
    val roomsSyncedAt: String? = null,
    val itemsSyncedAt: String? = null,
    val roomItemsSyncedAt: String? = null,
    val userRoomsSyncedAt: String? = null,
    val myRoomsSyncedAt: String? = null
)
```

**Resolusi `since`** di `MasterDataRepository` — param eksplisit > timestamp tersimpan > fallback epoch (first-time):

```kotlin
private val firstSyncSince = "1970-01-01T00:00:00Z"

private fun resolveSince(explicit: String?, stored: String?): String =
    explicit ?: stored ?: firstSyncSince

suspend fun syncItems(since: String? = null) {
    val effectiveSince = resolveSince(since, syncStateStore.load().itemsSyncedAt)
    val response = masterDataApi.getItems(since = effectiveSince)
    if (response.data.isNotEmpty()) masterDataDao.insertItems(response.data)
    response.syncedAt?.let { syncedAt ->
        syncStateStore.update { it.copy(itemsSyncedAt = syncedAt) }
    }
}
```

**Pivot tables** (`room-items`, `user-rooms`, `my-rooms`) **tidak memakai** `since` tersimpan — selalu `since=epoch` (snapshot penuh, lihat 5.1). `synced_at` tetap disimpan untuk **forward-compat** jika backend kelak menambah delta `updated_at`:

```kotlin
suspend fun syncRoomItems() {
    val response = masterDataApi.getRoomItems(since = firstSyncSince)
    // Clear SETELAH fetch sukses — relasi yang dihapus server ikut terhapus lokal
    masterDataDao.clearRoomItems()
    if (response.data.isNotEmpty()) masterDataDao.insertRoomItems(response.data)
    response.syncedAt?.let { syncedAt ->
        syncStateStore.update { it.copy(roomItemsSyncedAt = syncedAt) }
    }
}
```

### 5.3. SyncPeriodik

Rekomendasi interval sync:
- **Saat pertama kali install app**: Full sync semua data
- **Setiap buka app**: Sync incremental (dengan `since=`)
- **Background periodic**: Setiap 30 menit (jika ada koneksi)
- **Setelah submit inspeksi**: Refresh RoomItems (mungkin berubah)

---

## 6. Contoh Kode Kotlin/Retrofit

### 6.1. Retrofit Interface

```kotlin
interface ApiService {
    // ── Auth ──
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): TokenResponse

    // ── Master Data (Sync) ──
    @GET("api/rooms")
    suspend fun getRooms(
        @Query("since") since: String? = null
    ): SyncResponse<RoomDto>

    @GET("api/inspection-items")
    suspend fun getItems(
        @Query("since") since: String? = null
    ): SyncResponse<ItemDto>

    @GET("api/room-items")
    suspend fun getRoomItems(
        @Query("since") since: String? = null
    ): SyncResponse<RoomItemDto>

    @GET("api/auth/user-rooms")
    suspend fun getUserRooms(
        @Query("since") since: String? = null
    ): SyncResponse<UserRoomDto>

    @GET("api/auth/me/rooms")
    suspend fun getMyRooms(
        @Query("since") since: String? = null
    ): SyncResponse<RoomDto>

    // Catatan: getUsers (GET api/auth/users) TIDAK dipakai Android — admin-only (ADR-0008),
    // inspector selalu 403. Nama petugas dari auth/me (E6, 2026-08-01).

    // ── Inspections ──
    @GET("api/inspections")
    suspend fun getInspections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): PaginatedResponse<InspectionListItemDto>

    @GET("api/inspections/{id}")
    suspend fun getInspectionDetail(
        @Path("id") id: Int
    ): InspectionOutDto

    @POST("api/inspections")
    suspend fun submitInspection(
        @Body request: InspectionSubmitRequest
    ): InspectionOutDto

    // ── Analytics / Dashboard ──
    @GET("api/analytics/dashboard")
    suspend fun getDashboard(
        @Query("year_month") yearMonth: String? = null
    ): DashboardDto

    @GET("api/analytics/lowest-rooms")
    suspend fun getLowestRooms(
        @Query("year_month") yearMonth: String? = null,
        @Query("limit") limit: Int = 3
    ): List<RoomScoreDto>

    @GET("api/analytics/top-issues")
    suspend fun getTopIssues(
        @Query("year_month") yearMonth: String? = null,
        @Query("limit") limit: Int = 10
    ): List<IssueFrequencyDto>

    @GET("api/analytics/inspector-performance")
    suspend fun getInspectorPerformance(
        @Query("year_month") yearMonth: String? = null
    ): List<InspectorPerformanceDto>
}
```

### 6.2. Response Wrappers

```kotlin
data class SyncResponse<T>(
    val data: List<T>,
    @SerializedName("synced_at") val syncedAt: String
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total_pages") val totalPages: Int
)
```

### 6.3. Data Transfer Objects

```kotlin
// ── Auth ──
data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    val user: UserDto
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

// ── User ──
data class UserDto(
    val id: Int,
    val username: String,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean
)

// ── Master Data ──
data class RoomDto(
    val id: Int,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ItemDto(
    val id: Int,
    val name: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("updated_at") val updatedAt: String?
)

data class RoomItemDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("created_at") val createdAt: String
)

data class UserRoomDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("created_at") val createdAt: String
)

// ── Inspections ──
data class InspectionListItemDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("inspector_id") val inspectorId: Int,
    val status: String,
    @SerializedName("business_date") val businessDate: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("detail_count") val detailCount: Int
)

data class InspectionOutDto(
    val id: Int,
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("inspector_id") val inspectorId: Int,
    val status: String,
    @SerializedName("business_date") val businessDate: String,
    @SerializedName("local_timestamp") val localTimestamp: String,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    @SerializedName("created_at") val createdAt: String?,
    val details: List<InspectionDetailItemDto> = emptyList()
)

data class InspectionDetailItemDto(
    val id: Int,
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("item_name_snapshot") val itemNameSnapshot: String,
    val score: Int,
    val photos: List<PhotoDto> = emptyList()
)

data class PhotoDto(
    val id: Int,
    @SerializedName("photo_file_name") val photoFileName: String,
    @SerializedName("thumbnail_file_name") val thumbnailFileName: String?,
    @SerializedName("sort_order") val sortOrder: Int
)

data class InspectionSubmitRequest(
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("local_timestamp") val localTimestamp: String,
    @SerializedName("business_date") val businessDate: String,
    val details: List<InspectionSubmitDetailDto>
)

data class InspectionSubmitDetailDto(
    @SerializedName("item_id") val itemId: Int,
    val score: Int,
    val photos: List<PhotoSubmitDto> = emptyList()
)

data class PhotoSubmitDto(
    @SerializedName("file_name") val fileName: String,
    @SerializedName("sort_order") val sortOrder: Int = 0
)

// ── Analytics ──
data class DashboardDto(
    @SerializedName("pending_count") val pendingCount: Int,
    @SerializedName("total_rooms") val totalRooms: Int,
    @SerializedName("monthly_inspection_count") val monthlyInspectionCount: Int,
    @SerializedName("avg_score_pct") val avgScorePct: Double
)

data class RoomScoreDto(
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("year_month") val yearMonth: String,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("max_score") val maxScore: Int,
    @SerializedName("score_pct") val scorePct: Double,
    @SerializedName("inspection_count") val inspectionCount: Int
)

data class IssueFrequencyDto(
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("item_name_snapshot") val itemNameSnapshot: String,
    @SerializedName("year_month") val yearMonth: String,
    @SerializedName("score_zero_count") val scoreZeroCount: Int
)

data class InspectorPerformanceDto(
    @SerializedName("inspector_id") val inspectorId: Int,
    val username: String,
    @SerializedName("total_inspections") val totalInspections: Int
)
```

---

## 7. Prioritas Implementasi

| Prioritas | Item | Endpoint | Effort | Alasan |
|-----------|------|----------|--------|--------|
| **🔴 P1** | Auth (Login + Refresh + Logout) | `POST /api/auth/*` | Sedang | **Blocking** — app tidak bisa digunakan |
| **🔴 P1** | Room-Items Sync | `GET /api/room-items` | Kecil | **Blocking** — diperlukan untuk validasi inspeksi |
| **🔴 P1** | My Rooms Sync | `GET /api/auth/me/rooms` | Kecil | **Blocking** — filter room per petugas |
| **🟡 P2** | User-Rooms Bulk Sync | `GET /api/auth/user-rooms` | Kecil | Mapping user↔room untuk validasi & analytics |
| **🟡 P2** | Master Data Sync (Rooms + Items) | `GET /api/rooms?since=`, `GET /api/inspection-items?since=` | Kecil | Cache lokal untuk offline |
| **🟡 P2** | Submit Inspection | `POST /api/inspections` | Sedang | **Core feature** — kirim inspeksi |
| **🟡 P2** | Riwayat Inspeksi (dengan pagination) | `GET /api/inspections?page=&per_page=` | Sedang | History screen |
| **🟢 P3** | Dashboard (web) | `GET /api/analytics/dashboard` | Kecil | Hanya web dashboard supervisor (ADR-0017) |
| **🟢 P3** | Analytics (web) | `GET /api/analytics/*` | Kecil | Hanya web dashboard (tidak ada di Android) |

### Catatan Tambahan

1. **Gunakan `?since=` untuk sync** — bukan pagination. Pagination hanya untuk Web Admin.
2. **Simpan `synced_at`** dari setiap response SyncResponse via `SyncStateStore` — dipakai sebagai `since` di sync berikutnya (ADR-0012).
3. **Pivot tables** (`room-items`, `user-rooms`, `my-rooms`) selalu minta `since=epoch` dan clear+insert (replace-all snapshot) — bukan inkremental.
4. **Pagination riwayat server-driven**: `hasMorePages = currentPage < totalPages` — tanpa ceiling hardcoded (ADR-0013).
5. **Token refresh**: Kirim `refresh_token` di body `{ "refresh_token": "..." }`, bukan di cookie.
6. **Error codes**: Semua error punya field `code` — gunakan untuk logika interceptor.

---

## Lampiran A: Room Database Entity

```kotlin
// Room Database (Android Room)
@Database(
    entities = [
        RoomEntity::class,
        ItemEntity::class,
        RoomItemEntity::class,
        InspectionEntity::class,
        InspectionDetailEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun inspectionDao(): InspectionDao
}

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomItems(roomItems: List<RoomItemEntity>)

    @Query("SELECT * FROM rooms WHERE is_active = 1 ORDER BY name")
    suspend fun getActiveRooms(): List<RoomEntity>

    @Query("SELECT i.name FROM room_items ri JOIN inspection_items i ON ri.item_id = i.id WHERE ri.room_id = :roomId")
    suspend fun getItemNamesForRoom(roomId: Int): List<String>

    @Query("DELETE FROM rooms")
    suspend fun deleteAllRooms()

    @Query("DELETE FROM inspection_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM room_items")
    suspend fun deleteAllRoomItems()
}
```

---

## Lampiran B: Error Handling (Interceptor)

```kotlin
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val tokenRefresher: TokenRefresher
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Tambahkan access token
        val request = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
            .build()

        val response = chain.proceed(request)

        // Jika 401, coba refresh
        if (response.code == 401) {
            response.close()

            val refreshSuccess = runBlocking {
                tokenRefresher.refresh()
            }

            if (refreshSuccess) {
                // Retry dengan token baru
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${tokenProvider.getAccessToken()}")
                    .build()
                return chain.proceed(retryRequest)
            } else {
                // Refresh gagal → logout
                throw IOException("Session expired")
            }
        }

        return response
    }
}

// Cara baca error code dari response
fun extractErrorCode(response: Response): String? {
    return try {
        val body = response.peekBody(2048)
        val json = JSONObject(body.string())
        json.optString("code", null)
    } catch (e: Exception) {
        null
    }
}
```

---

## 8. Lampiran: Detail Fields Inspeksi

Berikut adalah field-field yang ada di setiap response inspection — cocokkan dengan data class Kotlin.

### 8.1. Submit / Get Detail Inspection (`InspectionOut`)

**Backend schema:** `app/modules/inspection/schemas.py` → `InspectionOut`

```json
{
  "id": 42,
  "room_id": 1,
  "inspector_id": 5,
  "status": "PENDING",
  "business_date": "2026-07-23",
  "local_timestamp": "2026-07-23T08:30:00Z",
  "rejection_reason": null,
  "created_at": "2026-07-23T08:30:00Z",
  "details": [
    {
      "id": 1,
      "item_id": 1,
      "item_name_snapshot": "Kebersihan Tangan",
      "score": 2,
      "photos": [
        {
          "id": 1,
          "photo_file_name": "uuid-photo-1.jpg",
          "thumbnail_file_name": null,
          "sort_order": 0
        }
      ]
    }
  ]
}
```

| Field | Tipe | Ada di Response? |
|-------|------|------------------|
| `id` | int | ✅ Submit & Detail |
| `room_id` | int | ✅ Submit & Detail |
| `inspector_id` | int | ✅ Submit & Detail |
| `status` | string | ✅ Submit & Detail |
| `business_date` | string | ✅ Submit & Detail |
| `local_timestamp` | string | ✅ Submit & Detail |
| `rejection_reason` | string/null | ✅ Submit & Detail |
| `created_at` | string | ✅ Submit & Detail |
| `details[].id` | int | ✅ Submit & Detail |
| `details[].item_id` | int | ✅ Submit & Detail |
| `details[].item_name_snapshot` | string | ✅ Submit & Detail |
| `details[].score` | int (0-2) | ✅ Submit & Detail |
| `details[].photos[].id` | int | ✅ Submit & Detail |
| `details[].photos[].photo_file_name` | string | ✅ Submit & Detail |
| `details[].photos[].thumbnail_file_name` | string/null | ✅ Submit & Detail |
| `details[].photos[].sort_order` | int | ✅ Submit & Detail |
| `room_name` | string | ❌ **Tidak ada** — lookup manual |
| `inspector_name` | string | ❌ **Tidak ada** — lookup manual |
| `detail_count` | int | ❌ **Tidak ada** — pakai `details.length` |
| `message` | string | ❌ **Tidak ada** — jangan diharapkan |

### 8.2. List Inspeksi (`InspectionListItem`)

**Backend schema:** `app/modules/inspection/schemas.py` → `InspectionListItem`

```json
{
  "id": 42,
  "room_id": 1,
  "inspector_id": 5,
  "status": "PENDING",
  "business_date": "2026-07-23",
  "created_at": "2026-07-23T08:30:00Z",
  "detail_count": 2
}
```

| Field | Tipe | Ada di Response? |
|-------|------|------------------|
| `id` | int | ✅ |
| `room_id` | int | ✅ |
| `inspector_id` | int | ✅ |
| `status` | string | ✅ |
| `business_date` | string | ✅ |
| `created_at` | string | ✅ |
| `detail_count` | int | ✅ **(hanya di list)** |

> ⚠️ `detail_count` **hanya ada** di response list (`GET /api/inspections`), **tidak ada** di response submit atau detail!

---

> **Dokumen ini akan diperbarui secara berkala.**
> Untuk pertanyaan atau klarifikasi, hubungi tim Backend.
