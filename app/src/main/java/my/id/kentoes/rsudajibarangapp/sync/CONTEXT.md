# Sync

Menangani sinkronisasi offline-first antara perangkat dan server. Mengelola upload foto, pengiriman data inspeksi, dan penghapusan draf setelah berhasil. Semua proses berjalan di latar belakang saat jaringan tersedia.

## Language

**Sinkronisasi**: 
Proses pengiriman data dari penyimpanan perangkat ke server saat jaringan tersedia. Berjalan otomatis tanpa intervensi Petugas.
_Avoid_: Sync, pengiriman data

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
Waktu dalam format UTC ISO 8601 yang digenerate saat data disimpan di perangkat. Menjadi acuan urutan data.
_Avoid_: Waktu lokal, timestamp

**Konflik Sinkronisasi**: 
Kondisi ketika data lokal dan server tidak sinkron (misal: data server lebih baru). Ditangani dengan mekanisme timestamp dan prioritas tertentu.
_Avoid_: Sync conflict, bentrok data
