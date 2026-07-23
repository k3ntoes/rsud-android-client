# Auth

Mengelola autentikasi Petugas: login, penyimpanan token, refresh token otomatis, dan force logout. Auth adalah gerbang masuk ke seluruh aplikasi.

## Language

**Petugas**: 
Pengguna aplikasi yang melakukan inspeksi kebersihan di lapangan. Role tunggal (belum ada supervisor/admin).
_Avoid_: User, pengguna, akun

**Access Token**: 
JWT yang dikirim di header `Authorization: Bearer` untuk setiap request API. Berlaku singkat (konfigurasi server).
_Avoid_: Token, auth token

**Refresh Token**: 
Token kedua yang digunakan untuk mendapatkan Access Token baru saat yang lama expired. Disimpan terenkripsi di perangkat.
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
