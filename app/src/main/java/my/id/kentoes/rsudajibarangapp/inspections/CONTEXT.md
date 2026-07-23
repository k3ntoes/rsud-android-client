# Inspections

Mengelola kuesioner inspeksi kebersihan: menampilkan daftar item dinamis, mencatat skor, memvalidasi bukti foto, dan menyimpan hasil ke penyimpanan perangkat.

## Language

**Inspeksi**: 
Satu sesi pemeriksaan kebersihan yang terdiri dari kumpulan item yang dinilai. Status dikelola server-side (PENDING → APPROVED/REJECTED).
_Avoid_: Pemeriksaan, pengecekan

**Item Kebersihan**: 
Satu baris parameter dalam form inspeksi. Didapat dari Master Data yang disimpan lokal (offline-first).
_Avoid_: Pertanyaan, parameter, indikator

**Skor**: 
Nilai item: 0 (Berisiko — **wajib foto**), 1 (Minor Defect — foto opsional), 2 (Sesuai Standar — tidak perlu foto).
_Avoid_: Nilai, rating, grade

**Bukti Foto**: 
Foto yang diambil Petugas sebagai bukti temuan. Multi-foto per item (unlimited). **Minimal 1 foto wajib** jika Skor = 0.
_Avoid_: Evidence, lampiran, gambar bukti

**Draf**: 
Data inspeksi yang sudah disimpan di perangkat tapi belum dikirim ke server. Boleh incomplete (tidak semua item diskor).
_Avoid_: Draft, simpan lokal

**Master Data**: 
Daftar item kebersihan, ruangan, dan konfigurasi form yang diunduh dari server dan disimpan lokal. Direfresh saat sinkronisasi.
_Avoid_: Data master, template, referensi

**Kirim**: 
Aksi mengirim inspeksi ke server. Validasi: **semua item harus valid** (skor terisi + foto jika skor 0). Data masuk ke WorkManager untuk dikirim saat jaringan tersedia.
_Avoid_: Submit, upload

**Simpan Draf**: 
Aksi menyimpan progress inspeksi ke lokal. Tidak perlu semua item lengkap.
_Avoid_: Save, simpan sementara

**Re-validasi**: 
Proses pengecekan ulang validasi foto saat Petugas mengubah skor item. Foto lama tidak otomatis dihapus — Petugas dapat menghapus manual.
_Avoid_: Revalidasi, validasi ulang

**Waktu Buat**: 
`local_timestamp` dalam format UTC yang digenerate saat Petugas memulai inspeksi. Waktu inilah yang dikirim ke server (bukan waktu upload/sinkronisasi).
_Avoid_: Timestamp, waktu kirim, waktu dibuat
