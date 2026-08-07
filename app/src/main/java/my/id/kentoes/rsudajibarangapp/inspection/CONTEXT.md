# Inspections

Mengelola kuesioner inspeksi kebersihan: menampilkan daftar item dinamis, mencatat skor, memvalidasi bukti foto, menyimpan hasil ke penyimpanan perangkat, dan menampilkan riwayat inspeksi.

## Language

**Inspeksi**:
Satu sesi pemeriksaan kebersihan yang terdiri dari kumpulan item yang dinilai di suatu room. Status dikelola server-side: `PENDING` (menunggu review) → `APPROVED` (disetujui Supervisor) / `REJECTED` (ditolak, ada catatan).
_Avoid_: Pemeriksaan, pengecekan

**Item Kebersihan**:
Satu baris parameter dalam form inspeksi. Didapat dari Master Data yang disimpan lokal (offline-first). Item terasosiasi dengan room via pivot `room_items` — tidak semua item berlaku untuk semua room. **Deskripsi item** (`deskripsi` dari entity, bukan null) ditampilkan di kartu item form inspeksi (keputusan review 2026-08).
_Avoid_: Pertanyaan, parameter, indikator

**Skor**:
Nilai item: 0 (Berisiko — **wajib foto**), 1 (Minor Defect — foto opsional), 2 (Sesuai Standar — tidak perlu foto).
_Avoid_: Nilai, rating, grade

**Bukti Foto**:
Foto yang diambil Petugas sebagai bukti temuan. Multi-foto per item (unlimited). **Minimal 1 foto wajib** jika Skor = 0. Format: diupload ke server → dapat `photo_file_name` → dikirim sebagai referensi di payload inspeksi.
_Avoid_: Evidence, lampiran, gambar bukti

**Draf**:
Data inspeksi yang sudah disimpan di perangkat tapi belum dikirim ke server. Boleh incomplete (tidak semua item diskor). Status: `DRAFT` (disimpan) → `PENDING_SYNC` (siap kirim). Menyimpan `inspector_id` pemilik (dari user yang login saat disimpan) untuk pemilahan per akun.
_Avoid_: Draft, simpan lokal

**Master Data**:
Koleksi data referensi yang di-sync dari server: room, inspection items, room-item pivot, user-room pivot. Semua data ini disimpan di Room lokal dan direfresh secara periodik via incremental sync.
- **Sync saat dashboard dibuka** (keputusan review 2026-08): jika cache lokal kosong, dashboard memicu sync otomatis sendiri — user baru tidak harus membuka layar Pilih Ruangan dulu.
- **Transparansi status**: dashboard menampilkan indikator "Terakhir sync" / "Menyinkronkan..." / "Sync gagal", plus pull-to-refresh untuk sync ulang manual. Gagal sync = pesan + aksi retry yang jelas, bukan diam-diam 0.
_Avoid_: Data master, template, referensi

**Room-Item Pivot**:
Relasi many-to-many antara room dan inspection items via tabel `room_items`. Satu room memiliki banyak items, satu item bisa dipakai di banyak room. Setiap relasi membawa `sort_order` (urutan checklist, kontrak BE ADR-0013). Mapping ini digunakan untuk:
- Validasi offline: hanya items yang terasosiasi dengan room yang wajib di-score
- Tampilan UI: badge/nama items di setiap room (item count di kartu ruangan dihitung dari pivot ini — keputusan review 2026-08: bukan heuristik nama)
- Form inspeksi TIDAK punya fallback "tampilkan semua item" — pivot kosong = form kosong (state "Tidak ada item untuk ruangan ini")
- Urutan checklist form — lihat **Urutan Checklist** (ADR-0019)
_Avoid_: Room items, item assignment, pivot table

**Urutan Checklist**:
Urutan tampilan item dalam form inspeksi suatu room. Diatur Admin PPI per ruangan via web-admin (tombol ▲/▼) dan dikirim BE lewat `sort_order` di pivot `room_items` (ADR-0013 BE, ADR-0019 Android). Aturan: `sort_order ASC, item_id ASC` (tie-breaker `item_id`). Pivot adalah SATU-SATUNYA sumber urutan — grouping kategori dihapus dari form (vestigial: BE tidak mengirim kategori, `kategori` di Android selalu kosong). Saat resume draft, item di-sort ulang pakai pivot terbaru; item yang sudah tidak terasosiasi (dilepas admin / pivot belum ter-sync) TETAP dipertahankan di akhir urutan — tidak di-drop (keputusan ADR-0019).
_Avoid_: Sort order, sequence, urutan abjad

**Kirim**:
Aksi mengirim inspeksi ke server. Validasi: **semua item yang terasosiasi dengan room harus valid** (skor terisi + foto jika skor 0). Data masuk ke WorkManager untuk dikirim saat jaringan tersedia.
_Avoid_: Submit, upload

**Catatan Item**:
Teks opsional per item yang diisi Petugas (mis. detail temuan). Tersimpan di draf (`DrafItem.catatan`) DAN kini ikut dikirim ke server sebagai `catatan` di tiap `details` payload submit (ADR-0018 Q2, 2026-08) — sebelumnya hilang saat submit karena `DetailSubmit` tidak memilikinya. BE perlu kolom `inspection_details.catatan`; Pydantic mengabaikan field tak dikenal sampai kolom ada.
_Avoid_: Notes, keterangan, komentar

**Simpan Draf**:
Aksi menyimpan progress inspeksi ke lokal. Tidak perlu semua item lengkap.
_Avoid_: Save, simpan sementara

**Re-validasi**:
Proses pengecekan ulang validasi foto saat Petugas mengubah skor item. Foto lama tidak otomatis dihapus — Petugas dapat menghapus manual.
_Avoid_: Revalidasi, validasi ulang

**Waktu Buat**:
`local_timestamp` yang digenerate saat Petugas memulai inspeksi dan dikirim ke server sebagai bagian dari idempotency key. **Catatan keputusan**: saat ini diformat device-local (jam WIB) dengan sufiks `Z` literal — bukan UTC sejati. Dibiarkan sengaja (keputusan review 2026-08) karena `business_date` dikirim eksplisit sebagai tanggal lokal device; hanya waktu absolut di server (analytics) yang terdampak.
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

**Terkirim**:
Metrik dashboard: jumlah inspeksi yang sudah dikonfirmasi server, dihitung dari `InspectionEntity` (cache riwayat) — **bukan** dari draf berstatus SYNCED (draf dihapus dari DB setelah sync sukses). Card "Total Inspeksi" di Ringkasan Inspeksi dihapus (ADR-0017) karena nilainya identik dengan "Terkirim" — keduanya bersumber dari `InspectionEntity`. Nilai bersifat device-wide (ADR-0016); scope per akun (`inspector_id`) dicatat sebagai follow-up.
**Definisi sukses (ADR-0018 Q1, 2026-08)**: "terkirim" = server mengakui inspeksi (200 dengan id) DAN cache riwayat lokal tertulis. Path `409 DUPLICATE_INSPECTION` juga menulis cache riwayat (`cacheDuplicateInspection` — cari via `roomId + businessDate` karena list endpoint BE tidak memuat `local_timestamp`; ambil kandidat terbaru) sehingga riwayat & dashboard konsisten tanpa menunggu fetch ulang. Kegagalan cache saat 409 bersifat best-effort — tidak menggagalkan penghapusan draf.
_Avoid_: Synced, sent count

**Status Inspeksi Hari Ini**:
**Daftar per-room** di dashboard untuk Petugas (inspector): satu baris per room yang di-assign, menampilkan status hari ini + jumlah item + target navigasi (UX-02, grill 2026-08). Status per baris:
- `BELUM` — belum ada catatan hari ini; klik → **form inspeksi baru** untuk room tsb
- `DRAF` — ada `DrafInspeksi` DRAFT hari ini; klik → **resume draf**
- `MENUNGGU_KIRIM` — ada draf PENDING_SYNC hari ini (siap kirim); klik → resume draf
- `MENUNGGU_REVIEW` / `DISETUJUI` / `DITOLAK` — ada `InspectionEntity` (PENDING/APPROVED/REJECTED) hari ini; klik → **detail inspeksi**
- Precedence: inspeksi (kebenaran server) menang atas draf (lokal), baru BELUM.
- Sorting: BELUM paling atas, lalu nama room.
- Scope: 1 hari (businessDate = today) · **hanya room yang di-assign** (`isMyRoom`) — inspector-only (ADR-0017).
- Definisi "selesai": memiliki catatan di `DrafInspeksi` (DRAFT/PENDING_SYNC) ATAU `InspectionEntity` dengan `businessDate` hari ini — dihitung dari `RoomStatusItem.status != BELUM`.
- **Analytics dashboard (Skor Terendah, Temuan Paling Sering) tidak ada di Android** — web dashboard adalah tempatnya (supervisor/admin_ppi, ADR-0017).
- **Inspeksi Ulang**: aksi di halaman detail inspeksi (terkirim) → membuka form inspeksi kosong untuk room yang sama. Draf yang belum terkirim dilanjutkan via "Draf" → Resume. Keputusan review 2026-08: tidak ada tombol "Inspeksi Baru" di dashboard; jalur masuk inspeksi ulang = daftar per-room (status terkirim) → detail → "Inspeksi Ulang".
_Avoid_: Daily stats, hari ini, status cards

**Retensi Data**:
Kebijakan penyimpanan data lokal (lihat ADR-0016 untuk keputusan detail):
- Metadata inspeksi (`InspectionEntity`, `InspectionDetailEntity`): disimpan permanen (ukuran kecil).
- Foto bukti disimpan di **dua path** sesuai siklus hidup (ADR-0016):
  - **Draf aktif**: file asli di `files/photos/`, tidak dihapus sampai draf berhasil dikirim. Menghapus draf (manual/resume, sync sukses, ganti akun) ikut menghapus file **asli**-nya.
  - **Terkirim**: saat sync sukses, file **terkompresi** (byte-identik server, ~300KB) dipindahkan ke `files/photos_sent/` dan `InspectionPhotoEntity.localPath` diisi. Disimpan **30 hari** (dihitung dari tanggal sync), lalu dihapus otomatis oleh `DraftPhotoCleanupWorker`. Tidak dihapus saat ganti akun — riwayat bersifat device-wide.
- **Riwayat inspeksi menampilkan foto lokal-first**: jika `localPath` ada & file ada → tampilkan file lokal (instan, offline-ready); jika tidak → URL server.
- **Draf bertag `inspector_id`** (diisi dari user yang sedang login saat draf disimpan). Draf TIDAK dihapus saat logout — user yang sama login ulang tidak kehilangan progress. Hanya draf milik akun LAIN (`inspector_id` berbeda) yang dihapus saat akun berbeda login. Draf legacy tanpa `inspector_id` dipertahankan (tidak dapat diatribusikan).
- **Foto draf yatim** (file di `files/photos/` tanpa referensi `draf_foto` — sisa draf terhapus/capture kamera dibatalkan — atau baris `draf_foto` tanpa header draf valid) dibersihkan otomatis oleh `DraftPhotoCleanupWorker` (periodik harian via WorkManager). File berumur < 24 jam tidak disentuh agar foto yang baru diambil namun belum disimpan ke draf tetap aman.
- **Re-upload manual** (opsional, menunggu endpoint replace backend): foto terkirim yang rusak/hilang di server bisa di-re-upload dari `files/photos_sent`.
_Avoid_: Cache, storage policy
