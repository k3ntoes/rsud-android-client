# MEDIA HANDLING & AUTHENTICATION LOGIC

**1. Kompresi Gambar Klien:**
- Kamera menghasilkan foto besar (3-5MB).
- Sebelum dikirim oleh WorkManager, aplikasi Android WAJIB melakukan kompresi gambar (resize & turunkan kualitas) hingga ukuran maksimal ~300KB agar sinkronisasi cepat dan tidak membebani server.

**2. Manajemen Token JWT:**
- Gunakan Authenticator/Interceptor di OkHttp (Retrofit).
- Jika API mengembalikan 401 Unauthorized (Access Token mati), Interceptor secara otomatis memanggil endpoint `/refresh` menggunakan Refresh Token.
- Jika Refresh Token juga ditolak (karena di-Revoke Admin atau Expired), aplikasi otomatis memaksa Force Logout dan menghapus semua data sesi.
