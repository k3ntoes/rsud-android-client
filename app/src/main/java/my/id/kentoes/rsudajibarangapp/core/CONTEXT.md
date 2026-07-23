# Core

Fondasi aplikasi yang digunakan bersama oleh semua konteks lain. Menyediakan pengaturan umum, tipe data bersama, dan kerangka navigasi.

## Language

**Alamat Server**: 
Lokasi server backend RSUD yang menjadi tujuan semua permintaan data. Digunakan bersama oleh Auth, Inspections, dan Sync.
_Avoid_: Base URL, server address, endpoint

**Penyedia Token**: 
Mekanisme yang memungkinkan konteks lain (Inspections, Sync) mendapatkan Access Token untuk berkomunikasi dengan server. Implementasinya dikelola oleh Auth.
_Avoid_: TokenProvider, credential provider

**Tata Letak Layar**: 
Alur navigasi antar layar yang berubah tergantung status sesi. Saat Petugas logout, layar Login muncul. Saat login, layar inspeksi muncul.
_Avoid_: Navigation graph, routing, nav graph
