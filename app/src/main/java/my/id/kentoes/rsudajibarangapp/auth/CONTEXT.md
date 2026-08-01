# Auth

Mengelola autentikasi Petugas: login, penyimpanan token, refresh token otomatis, force logout, dan assignment ruangan. Auth adalah gerbang masuk ke seluruh aplikasi dan menentukan akses pengguna. Android hanya melayani role `inspector`; `supervisor` dan `admin_ppi` melayani via web dashboard (ADR-0017).

## Language

**Petugas**: 
Pengguna aplikasi Android yang melakukan inspeksi kebersihan di lapangan. Memiliki role `inspector` — hanya bisa melihat dan menginspeksi room yang di-assign. **Satu-satunya role yang login ke aplikasi Android** (keputusan ADR-0017): supervisor dan admin_ppi ditolak saat login dengan pesan "gunakan web", dan sesi non-inspector yang sudah ada di-force-logout saat `init()`.
_Avoid_: User, pengguna, akun

**Supervisor**: 
Pengguna dengan role `supervisor` yang memantau hasil inspeksi dan memberikan persetujuan (APPROVED/REJECTED). Dapat melihat dashboard analitik dan semua room (dengan parameter `?show_all=true`). Melayani via **web dashboard** — tidak login ke aplikasi Android (ADR-0017).
_Avoid_: Atasan, manager, penyelia

**Admin PPI**: 
Pengguna dengan role `admin_ppi` yang mengelola master data (rooms, items, user assignments) melalui web dashboard. Melayani via **web dashboard** — tidak login ke aplikasi Android (ADR-0017).
_Avoid_: Admin, superadmin

**Dual Delivery Refresh Token**: 
Mekanisme pengiriman Refresh Token dengan dua jalur: Web menerima via httpOnly cookie, Android menerima via response body (`refresh_token` field). Android mengirim Refresh Token via request body `{ "refresh_token": "..." }` ke endpoint `/api/auth/refresh`, bukan cookie.
_Avoid_: Dual delivery, dual channel token

**Access Token**: 
JWT yang dikirim di header `Authorization: Bearer` untuk setiap request API. Berlaku singkat (±15 menit, konfigurasi server).
_Avoid_: Token, auth token

**Refresh Token**: 
Token kedua yang digunakan untuk mendapatkan Access Token baru saat yang lama expired. Disimpan terenkripsi di perangkat via DataStore + Tink.
_Avoid_: Refresh, secret

**Sesi**: 
Periode antara login berhasil hingga logout (manual atau force). Sesi tetap aktif meskipun Access Token expired selama Refresh Token masih valid.
_Avoid_: Session, login state

**Force Logout**: 
Penghapusan paksa semua token dan data sesi karena Refresh Token ditolak server (expired/direvoke admin). Mengembalikan Petugas ke layar Login. **Hanya dipicu saat server benar-benar menolak token** — `TOKEN_INVALID` (dari error code) atau `HttpException` 401/403 di `refreshToken()`. Kegagalan jaringan (`IOException`) TIDAK memicu logout paksa — sesi tetap valid, user offline tidak kehilangan draf saat refresh gagal sementara.
_Avoid_: Logout paksa, session kill

**Kepemilikan Draf**: 
Draf inspeksi lokal bertag `inspector_id` dari user yang login saat disimpan. Saat login akun BERBEDA, draf akun lama (inspector_id != user baru) dihapus termasuk file fotonya (`clearForeignDrafts`). Draf TIDAK dihapus saat logout — user yang sama login ulang tidak kehilangan progress. Draf legacy tanpa `inspector_id` dipertahankan (tidak dapat diatribusikan).
_Avoid_: Draft ownership, draft per akun

**AuthState**: 
Representasi reaktif status sesi yang menentukan apakah Petugas dapat mengakses layanan aplikasi atau harus login ulang.
_Avoid_: Status login, session state

**User-Room Assignment**: 
Relasi many-to-many antara user dan ruangan via tabel pivot `user_rooms`. Setiap user (Petugas) di-assign ke satu atau lebih room. Assignment ini menentukan:
- Room mana yang tampil di dropdown/list pemilihan room
- Room mana yang valid untuk submit inspeksi (validasi server-side)
- Room mana yang tampil di dashboard Supervisor secara default
_Avoid_: User room mapping, room access, assigned rooms

**Idempotency Key**: 
Kombinasi `(room_id, local_timestamp, inspector_id)` yang mencegah duplikasi inspeksi. `inspector_id` berasal dari `user.id` yang didapat saat login. Key ini dikirim ke server agar retry dari WorkManager tidak membuat inspeksi duplikat.
_Avoid_: Idempotensi, duplicate prevention key

**Nama Petugas (Riwayat)**: 
Lookup nama petugas di InspectionDetailScreen memakai **user login** dari `auth/me` (field `name`/`username` pada `UserOut`). Jika `inspector_id` inspeksi == id user login → tampil `name`/`username`; selain itu fallback "Petugas #ID". Tabel `UserEntity` dan sync `GET /api/auth/users` **dihapus** (2026-08-01, E6): endpoint admin-only (ADR-0008) → inspector selalu 403.
_Avoid_: User cache, user table, user lookup
