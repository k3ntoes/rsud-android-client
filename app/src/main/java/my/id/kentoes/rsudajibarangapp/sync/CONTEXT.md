# Sync

Menangani sinkronisasi offline-first antara perangkat dan server. Mengelola upload foto, pengiriman data inspeksi, master data refresh, dan riwayat inspeksi. Semua proses berjalan di latar belakang saat jaringan tersedia.

## Language

**Sinkronisasi Inspeksi**: 
Proses pengiriman data inspeksi (draf dengan status PENDING_SYNC) dari penyimpanan perangkat ke server. Alur: kompres foto → upload foto → dapatkan nama file → kirim payload JSON → hapus draf. Berjalan otomatis via WorkManager saat `Network.CONNECTED`.
_Avoid_: Sync, pengiriman data

**Sinkronisasi Master Data**: 
Proses pengunduhan data referensi dari server ke penyimpanan lokal. Urutan sync: (1) rooms, (2) inspection-items, (3) room-items, (4) user-rooms, (5) my-rooms. Data di-*upsert* ke Room lokal dengan replace all data lama untuk pivot tables.
_Avoid_: Master sync, data download, refresh data

**Sinkronisasi Inkremental**: 
Mekanisme sync yang hanya mengunduh data yang berubah sejak timestamp tertentu. Menggunakan parameter `?since=<ISO 8601>` di endpoint master data. Server return `SyncResponse { data, synced_at }`. Android menyimpan `synced_at` untuk digunakan di sync berikutnya. **First-time sync**: kirim `since=1970-01-01T00:00:00Z`.
_Avoid_: Incremental sync, delta sync, differential sync

**SyncResponse**: 
Wrapper response khusus untuk endpoint sync: `{ data: [...], synced_at: "..." }`. `data` berisi array entitas (rooms, items, room-items), `synced_at` adalah timestamp yang harus disimpan untuk request `?since=` berikutnya. Berbeda dengan `PaginatedResponse` yang digunakan oleh Web Admin.
_Avoid_: Sync wrapper, data response

**Keadaan Sinkronisasi**: 
`SyncState` — kumpulan timestamp `synced_at` untuk setiap endpoint master data: `roomsSyncedAt`, `itemsSyncedAt`, `roomItemsSyncedAt`, `userRoomsSyncedAt`, `myRoomsSyncedAt`. Disimpan di DataStore/SharedPreferences dan digunakan sebagai parameter `?since=` di sync berikutnya.
_Avoid_: Sync state, sync status, sync progress

**Penjadwal Latar**: 
Mekanisme yang menjalankan tugas sinkronisasi di latar belakang secara otomatis saat kondisi jaringan memungkinkan.
_Avoid_: WorkManager, background worker, scheduler

**Upload Dua Langkah**: 
Alur pengiriman: (1) upload foto terkompresi → dapatkan nama file dari server → (2) kirim payload JSON dengan nama file ke endpoint inspeksi.
_Avoid_: Two-step upload, upload bertahap

**Kompresi Gambar**: 
Proses resize dan penurunan kualitas foto hingga maksimal 300KB sebelum dikirim ke server. Dilakukan di sisi klien.
_Avoid_: Image compression, foto compress

**Timestamp Absolut**: 
Waktu dalam format UTC ISO 8601 yang digenerate saat data disimpan di perangkat. Menjadi acuan urutan data. Digunakan sebagai `local_timestamp` di payload inspeksi dan sebagai bagian dari idempotency key.
_Avoid_: Waktu lokal, timestamp

**Pemetaan Ruangan-Item**: 
Struktur data lokal `Map<Long, List<Long>>` (key: roomId, value: list of itemIds) yang dibangun dari hasil sync `room-items`. Digunakan untuk validasi offline: hanya items yang terasosiasi dengan room yang wajib di-score saat submit.
_Avoid_: Room-item map, pivot mapping

**Pemetaan Pengguna-Ruangan**: 
Struktur data lokal `Map<Int, List<Long>>` (key: userId, value: list of roomIds) yang dibangun dari hasil sync `user-rooms`. Digunakan oleh Supervisor untuk melihat assignment dan oleh dashboard untuk filter data per petugas.
_Avoid_: User-room map, assignment mapping

**Konflik Sinkronisasi**: 
Kondisi ketika data lokal dan server tidak sinkron. Ditangani dengan: (1) idempotency key `(room_id, local_timestamp, inspector_id)` untuk mencegah duplikat, (2) validasi server-side untuk room_items dan user_rooms, (3) standard error codes (`TOKEN_EXPIRED`, `DUPLICATE_INSPECTION`, dll).
_Avoid_: Sync conflict, bentrok data

**Riwayat Hibrida**: 
Mekanisme penyimpanan riwayat inspeksi: simpan hasil submit (`InspectionOut`) ke Room lokal sebagai cache, lalu refresh dari server (`GET /api/inspections`) untuk mendapatkan data terbaru. Kombinasi instant load dari cache + update dari server.
_Avoid_: Hybrid history, inspection cache
