# Auth

Mengelola autentikasi Petugas: login, penyimpanan token, refresh token otomatis, force logout, dan assignment ruangan. Auth adalah gerbang masuk ke seluruh aplikasi dan menentukan akses pengguna.

## Language

**Petugas**: 
Pengguna aplikasi yang melakukan inspeksi kebersihan di lapangan. Memiliki role `inspector` — hanya bisa melihat dan menginspeksi room yang di-assign.
_Avoid_: User, pengguna, akun

**Supervisor**: 
Pengguna dengan role `supervisor` yang memantau hasil inspeksi dan memberikan persetujuan (APPROVED/REJECTED). Dapat melihat dashboard analitik dan semua room (dengan parameter `?show_all=true`).
_Avoid_: Atasan, manager, penyelia

**Admin PPI**: 
Pengguna dengan role `admin_ppi` yang mengelola master data (rooms, items, user assignments) melalui web dashboard. Di aplikasi Android, memiliki akses yang sama dengan Supervisor.
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
Penghapusan paksa semua token dan data sesi karena Refresh Token ditolak server (expired/direvoke admin). Mengembalikan Petugas ke layar Login.
_Avoid_: Logout paksa, session kill

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

**User Cache**: 
`UserEntity` — tabel Room lokal untuk cache data user. Di-sync dari endpoint `GET /api/auth/users`. Digunakan oleh InspectionDetailScreen untuk menampilkan nama petugas (`username`) dan role yang melakukan inspeksi, bukan placeholder "Petugas #ID".
_Avoid_: Pengguna, user table, user lookup
