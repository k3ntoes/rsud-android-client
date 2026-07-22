# PRODUCT REQUIREMENTS DOCUMENT (PRD) - ANDROID CLIENT
**Proyek:** Sistem Inspeksi Kebersihan Harian - Mobile App
**Klien:** RSUD Ajibarang

## 1. Objektif Repositori
Menyediakan aplikasi Android *offline-first* yang digunakan oleh Petugas Kebersihan di lapangan. Aplikasi harus sangat ringan, cepat, dan tangguh menghadapi kondisi jaringan rumah sakit yang tidak stabil (*blank spot*).

## 2. Peran Pengguna
*   **Petugas:** Melakukan login, mengunduh Master Data terbaru, mengisi kuesioner inspeksi, memotret bukti, dan menyinkronkan data.

## 3. Spesifikasi Fitur Utama (UI/UX)
*   **Form Inspeksi Dinamis:** Menampilkan daftar item kebersihan berdasarkan data yang ditarik dari API (Master Data).
*   **Sistem Skoring:** Menyediakan pilihan radio button: 0 (Berisiko), 1 (Minor Defect), 2 (Sesuai Standar).
*   **Validasi Bukti:** Jika petugas memilih skor 0, aplikasi WAJIB memblokir tombol "Simpan" sebelum petugas mengambil/mengunggah foto bukti.
*   **Kompresi Sisi Klien:** Aplikasi WAJIB melakukan kompresi gambar secara lokal (resize & kualitas dikurangi) hingga ukuran file maksimal 300KB sebelum diunggah ke server.

## 4. Spesifikasi Logika Sinkronisasi (Offline-First)
*   **Penyimpanan Lokal:** Saat petugas menekan tombol "Simpan", data dan direktori foto disimpan ke dalam database lokal (SQLite/Room).
*   **Timestamp Absolut:** Aplikasi WAJIB men-generate `local_timestamp` dalam format UTC (ISO 8601) pada detik yang sama saat data disimpan ke database lokal.
*   **WorkManager (Lazy Sync):** Proses sinkronisasi hanya berjalan di latar belakang saat `Network.CONNECTED`.
*   **Alur Pengiriman:** 
    1. Upload file foto terkompresi terlebih dahulu.
    2. Tangkap respons nama file dari API.
    3. Masukkan nama file ke dalam payload JSON dan kirim ke endpoint inspeksi utama.
    4. Jika respons API 200 OK, hapus draf dari database lokal.

## 5. Keamanan Perangkat
*   Menggunakan Interceptor untuk menangkap status 401. 
*   Aplikasi harus bisa menukar Refresh Token secara mandiri di latar belakang. Jika gagal menukar token, hapus seluruh sesi lokal (Force Logout) dan kembalikan user ke layar Login.
