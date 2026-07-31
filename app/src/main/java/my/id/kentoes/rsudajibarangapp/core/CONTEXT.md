# Core

Fondasi aplikasi yang digunakan bersama oleh semua konteks lain. Menyediakan pengaturan umum, tipe data bersama, response wrappers, error handling, dan kerangka navigasi.

## Language

**Alamat Server**: 
Lokasi server backend RSUD yang menjadi tujuan semua permintaan data. Termasuk path prefix `/api/` — semua endpoint Retrofit menggunakan path relatif tanpa `/api/`. Contoh: `BASE_URL = "https://be-ajib.kentoes.my.id/api/"`.
_Avoid_: Base URL, server address, endpoint

**Penyedia Token**: 
Mekanisme yang memungkinkan konteks lain (Inspections, Sync) mendapatkan Access Token untuk berkomunikasi dengan server. Implementasi langsung oleh `TokenManager` (tanpa interface indirection) dan di-inject via `AuthInterceptor`.
_Avoid_: TokenProvider, credential provider

**Tata Letak Layar**: 
Alur navigasi antar layar yang berubah tergantung status sesi dan role. Dashboard adalah start destination setelah login. Navigation graph mencakup: Dashboard, MasterDataList, InspectionForm, DraftList, Login, InspectionHistory, InspectionDetail.
_Avoid_: Navigation graph, routing, nav graph

**Response Umum**: 
`ApiResponse<T>` — wrapper response untuk endpoint non-sync seperti login, upload, dan change password. Format: `{ success: Boolean, message: String?, data: T? }`. Tidak digunakan oleh endpoint master data yang sudah beralih ke `SyncResponse`.
_Avoid_: Generic response, base response

**Response Sinkronisasi**: 
`SyncResponse<T>` — wrapper response khusus untuk endpoint master data yang dipanggil dengan parameter `?since=`. Format: `{ data: List<T>, synced_at: String }`. `synced_at` adalah timestamp yang harus disimpan dan dikirim sebagai `since` di sync berikutnya. Endpoint pivot (`/api/room-items`, `/api/auth/me/rooms`, `/api/auth/user-rooms`) **selalu** return `SyncResponse` — tidak memiliki mode lain.
_Avoid_: Sync response, data wrapper, response envelope

**Response Terindeks**: 
`PaginatedResponse<T>` — wrapper response untuk endpoint list yang dipanggil TANPA parameter `?since=`. Format: `{ items: List<T>, total, page, per_page, total_pages }`. Digunakan oleh Web Admin (browser pagination). Android tidak menggunakan mode ini — selalu gunakan `?since=` untuk SyncResponse. Android dan Web Admin memiliki Retrofit interface berbeda karena return type yang tidak sama.
_Avoid_: Paginated response, paged response

**Response Ganda**: 
Endpoint master data (`/api/rooms`, `/api/inspection-items`) memiliki dua mode response tergantung ada/tidaknya query `?since=`:
- **Dengan `?since=`** → return `SyncResponse<T>` (unpaginated) — digunakan Android
- **Tanpa `?since=`** → return `PaginatedResponse<T>` — digunakan Web Admin
Android SELALU menggunakan mode `?since=` (first-time sync kirim `since=1970-01-01T00:00:00Z`).

> ⚠️ **Pengecualian**: Endpoint pivot (`/api/room-items`, `/api/auth/me/rooms`, `/api/auth/user-rooms`) tidak memiliki dual mode — selalu return `SyncResponse`. Ini karena endpoint-endpoint ini khusus untuk sync Android dan tidak digunakan oleh Web Admin.



**Kode Error Standar**: 
Semua error response dari server menggunakan format `{ detail: String, code: String }`. Field `code` digunakan oleh `AuthInterceptor` dan `TokenAuthenticator` untuk logika yang lebih stabil dibanding parsing string `detail`. Error codes: `TOKEN_EXPIRED`, `TOKEN_INVALID`, `FILE_TOO_LARGE`, `DUPLICATE_INSPECTION`, `DUPLICATE_ASSIGNMENT`, `VALIDATION_ERROR`.
_Avoid_: Error code, status code, error format

**Entitas Database**: 
Room database `rsud_ajibarang.db` (version 4) berisi 11 entity:
- `MasterDataItem`, `RuangEntity` — master data. `RuangEntity.isMyRoom` menandai room yang di-assign ke user login (di-set oleh `syncMyRooms`, di-reset sebelum penandaan ulang — lihat Sync)
- `DrafInspeksi`, `DrafItem`, `DrafFoto` — draft inspeksi. `DrafInspeksi.inspectorId` distempel dari user yang login saat disimpan untuk pemilahan per akun
- `RoomItemEntity`, `UserRoomEntity` — pivot tables (replace-all full snapshot)
- `InspectionEntity`, `InspectionDetailEntity`, `InspectionPhotoEntity` — hybrid history cache
- `UserEntity` — cache user untuk lookup nama petugas

**Keadaan Sinkronisasi**: 
`SyncStateStore` — persistence SharedPreferences untuk `SyncState` (timestamp `synced_at` per endpoint master data: rooms, items, room-items, user-rooms, my-rooms). Dipakai sebagai parameter `?since=` di sync berikutnya (ADR-0012). `clear()` dipanggil saat logout agar akun berikutnya sync penuh dari epoch. Timestamp non-sensitif, cukup SharedPreferences (bukan token yang butuh Tink).
_Avoid_: Sync state, sync progress

**Paginasi Server-Driven**: 
Semua endpoint LIST menggunakan parameter `page`/`per_page` yang dikelola server. Android mengirim `page=1&per_page=10000` (atau menggunakan mode `?since=` untuk master data). Response menyertakan `total`, `total_pages`, dan `page` untuk tracking posisi halaman.
_Avoid_: Server pagination, page-based pagination
