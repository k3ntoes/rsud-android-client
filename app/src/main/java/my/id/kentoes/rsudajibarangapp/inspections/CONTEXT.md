# Inspections

Mengelola kuesioner inspeksi kebersihan: menampilkan daftar item dinamis, mencatat skor, memvalidasi bukti foto, menyimpan hasil ke penyimpanan perangkat, dan menampilkan riwayat inspeksi.

## Language

**Inspeksi**: 
Satu sesi pemeriksaan kebersihan yang terdiri dari kumpulan item yang dinilai di suatu room. Status dikelola server-side: `PENDING` (menunggu review) → `APPROVED` (disetujui Supervisor) / `REJECTED` (ditolak, ada catatan).
_Avoid_: Pemeriksaan, pengecekan

**Item Kebersihan**: 
Satu baris parameter dalam form inspeksi. Didapat dari Master Data yang disimpan lokal (offline-first). Item terasosiasi dengan room via pivot `room_items` — tidak semua item berlaku untuk semua room.
_Avoid_: Pertanyaan, parameter, indikator

**Skor**: 
Nilai item: 0 (Berisiko — **wajib foto**), 1 (Minor Defect — foto opsional), 2 (Sesuai Standar — tidak perlu foto).
_Avoid_: Nilai, rating, grade

**Bukti Foto**: 
Foto yang diambil Petugas sebagai bukti temuan. Multi-foto per item (unlimited). **Minimal 1 foto wajib** jika Skor = 0. Format: diupload ke server → dapat `photo_file_name` → dikirim sebagai referensi di payload inspeksi.
_Avoid_: Evidence, lampiran, gambar bukti

**Draf**: 
Data inspeksi yang sudah disimpan di perangkat tapi belum dikirim ke server. Boleh incomplete (tidak semua item diskor). Status: `DRAFT` (disimpan) → `PENDING_SYNC` (siap kirim).
_Avoid_: Draft, simpan lokal

**Master Data**: 
Koleksi data referensi yang di-sync dari server: room, inspection items, room-item pivot, user-room pivot. Semua data ini disimpan di Room lokal dan direfresh secara periodik via incremental sync.
_Avoid_: Data master, template, referensi

**Room-Item Pivot**: 
Relasi many-to-many antara room dan inspection items via tabel `room_items`. Satu room memiliki banyak items, satu item bisa dipakai di banyak room. Mapping ini digunakan untuk:
- Validasi offline: hanya items yang terasosiasi dengan room yang wajib di-score
- Tampilan UI: badge/nama items di setiap room
_Avoid_: Room items, item assignment, pivot table

**Kirim**: 
Aksi mengirim inspeksi ke server. Validasi: **semua item yang terasosiasi dengan room harus valid** (skor terisi + foto jika skor 0). Data masuk ke WorkManager untuk dikirim saat jaringan tersedia.
_Avoid_: Submit, upload

**Simpan Draf**: 
Aksi menyimpan progress inspeksi ke lokal. Tidak perlu semua item lengkap.
_Avoid_: Save, simpan sementara

**Re-validasi**: 
Proses pengecekan ulang validasi foto saat Petugas mengubah skor item. Foto lama tidak otomatis dihapus — Petugas dapat menghapus manual.
_Avoid_: Revalidasi, validasi ulang

**Waktu Buat**: 
`local_timestamp` dalam format UTC ISO 8601 yang digenerate saat Petugas memulai inspeksi. Waktu inilah yang dikirim ke server (bukan waktu upload/sinkronisasi). Menjadi bagian dari idempotency key.
_Avoid_: Timestamp, waktu kirim, waktu dibuat

**Tanggal Bisnis**: 
`business_date` dalam format `YYYY-MM-DD` yang menandakan tanggal inspeksi secara bisnis. Dapat berbeda dengan tanggal submit (misal: inspeksi dilakukan jam 23:59, submit jam 00:15). Opsional — jika tidak dikirim, server mengisi dengan tanggal hari ini.
_Avoid_: Business date, tanggal inspeksi

**Inspection Status**: 
Status inspeksi yang dikelola server: `PENDING` (menunggu review Supervisor), `APPROVED` (disetujui), `REJECTED` (ditolak dengan alasan). Android menampilkan status ini di riwayat inspeksi. `detail_count` hanya ada di response list, bukan di response detail.
_Avoid_: State, status

**Riwayat Inspeksi**: 
Daftar inspeksi yang sudah dikirim. Disimpan secara hybrid: cache lokal dari hasil submit + fetch dari server via endpoint `GET /api/inspections` untuk data terbaru. Mendukung pagination server-driven, filter status, dan filter tanggal (businessDate).
_Avoid_: History, inspection list, log

**Status Inspeksi Hari Ini**: 
Dua metrik yang ditampilkan di dashboard untuk inspector: jumlah ruangan yang sudah vs belum diinspeksi pada hari ini (businessDate = today). 
- Scope: 1 hari (hari ini). Akan diperpanjang jika ada perubahan requirement.
- Definisi "sudah": memiliki catatan di `DrafInspeksi` (DRAFT/PENDING_SYNC) ATAU `InspectionEntity` (PENDING/APPROVED/REJECTED) dengan `businessDate` hari ini.
- Card "Belum Diinspeksi": click → navigasi ke room selection, hanya tampilkan ruangan yang belum dicatat hari ini.
- Card "Sudah Diinspeksi": click → navigasi ke riwayat inspeksi dengan filter businessDate = hari ini (filter lokal via Room DB).
_Avoid_: Daily stats, hari ini

**Retensi Data**: 
Kebijakan penyimpanan data lokal:
- Metadata inspeksi (`InspectionEntity`, `InspectionDetailEntity`): disimpan permanen (ukuran kecil).
- Foto bukti: disimpan di `Pictures/rsud_ajibarang/` via MediaStore; dihapus otomatis setelah 30 hari via WorkManager; user bisa hapus manual dari galeri.
- Draf aktif: foto tidak dihapus sampai draf berhasil dikirim.
_Avoid_: Cache, storage policy
