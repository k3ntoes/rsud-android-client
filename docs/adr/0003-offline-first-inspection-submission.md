# ADR-0003: Offline-First Inspection Submission Model

**Status**: Accepted

Mengadopsi model state inspeksi UDF (Unidirectional Data Flow) dengan pemisahan data murni (`ItemKebersihan`) dan state UI (`ItemState`), serta alur submit dua langkah yang selaras dengan backend API RSUD.

## Context

Aplikasi harus mendukung inspeksi kebersihan secara offline-first. Petugas di blind spot harus tetap bisa mengisi form, mengambil foto, dan menyimpan draf. Saat jaringan tersedia, data dikirim ke backend melalui two-step upload (foto dulu, JSON kemudian).

Backend sudah memiliki skema database yang mature: tabel `inspections` (header), `inspection_details` (line items dengan score), dan `inspection_photos` (multi-foto per item). Backend juga menerapkan idempotency key `(room_id, local_timestamp, inspector_id)` dan validasi "all items required" di sisi server.

Keputusan ini mendefinisikan bagaimana Android mengelola state inspeksi lokal, kapan validasi berlaku, dan bagaimana data dikirim ke backend.

## Considered Options

### Model State

- **Data class tunggal (Item dengan skor + foto inline)**: Simpel, kurang dari 5 file. Tapi logika validasi tersebar di ViewModel dan mudah inkonsisten. Tidak dipilih.

- **UDF pattern (ItemKebersihan + ItemState pisah) — dipilih**: Data murni (dari Master Data) tidak pernah berubah. Semua state interaktif (skor, foto, catatan) di `ItemState` dengan computed property `isValid`. ViewModel jadi single source of truth, UI hanya render.

### Aturan Validasi

- **Validasi backend-only**: Tombol Kirim selalu aktif. Backend tolak jika ada item kurang. UX jelek — Petugas harus isi ulang.

- **Validasi ganda (Android + Backend) — dipilih**: Android menghitung `canSubmit = items.all { it.isValid }` sebelum mengaktifkan tombol Kirim. Backend tetap memvalidasi sebagai jaring pengaman. Petugas tidak akan mengalami error server akibat data tidak lengkap.

### Penanganan Foto Saat Re-validasi Skor

- **Hapus semua foto saat skor ganti ke 0**: Lebih aman, foto lama mungkin tidak relevan. Tapi Petugas kehilangan foto yang masih berguna.

- **Foto tetap ada, Petugas hapus manual — dipilih**: Foto dari skor sebelumnya tetap disimpan. Jika Petugas ganti skor 1→0, foto lama masih ada dan validasi lolos selama ≥ 1 foto. Petugas bisa hapus foto yang tidak relevan.

### Alur Submit

- **Kirim langsung saat online**: Tombol Kirim hanya aktif jika online. Tidak cocok untuk offline-first.

- **Kirim via WorkManager — dipilih**: Data langsung masuk ke WorkManager dan dijadwalkan untuk dikirim saat `Network.CONNECTED`. Petugas tidak perlu menunggu sinyal.

## Consequences

- Model UDF memudahkan testing — validasi bisa diuji tanpa UI.
- Draf yang incomplete tetap bisa disimpan, mengurangi frustrasi Petugas di blind spot.
- Idempotency key `(room_id, local_timestamp, inspector_id)` mencegah duplikasi jika Petugas tidak sengaja menekan Kirim dua kali.
- `local_timestamp` dikirim sebagai waktu dibuat (bukan dikirim), selaras dengan backend `business_date`.
- WorkManager menambah kompleksitas — perlu handle dua step upload secara berurutan (foto dulu, JSON kemudian).
