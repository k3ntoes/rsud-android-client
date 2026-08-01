# ADR-0017: Android Sebagai Klien Inspector-Only — Supervisor & Admin PPI Melalui Web

**Status**: Accepted

**Tanggal**: 2026-08-01

## Context

Aplikasi Android memiliki fitur untuk role `supervisor` dan `admin_ppi` (analytics dashboard, master data stats global, branch role di berbagai ViewModel) yang ternyata tidak mencerminkan tanggung jawab masing-masing role. Dashboard inspector menampilkan angka global (total ruangan 6 padahal inspector hanya bertanggung jawab atas 4 ruangan yang di-assign) dan statistik mati tanpa drill-down (total item). Backend sendiri sudah menetapkan arah sebaliknya: endpoint analytics hanya bisa diakses `supervisor`/`admin_ppi` (403 untuk inspector) dan web dashboard sudah menjadi rumah utama mereka.

Keputusan: **Android adalah klien untuk role `inspector` saja.** `supervisor` dan `admin_ppi` menggunakan web dashboard. Boundary ditegakkan **sepenuhnya client-side** di Android: (1) login menolak role non-inspector dengan pesan jelas, dan (2) sesi yang sudah ada juga dicek saat `init()`/`refreshCurrentUser()` — jika role bukan `inspector`, sesi di-force-logout. Tidak ada perubahan backend: server tidak membedakan client Android vs web (cookie vs body), sehingga penolakan login server-side per-client tidak realistis; web dashboard menjaga role-nya sendiri.

## Konsekuensi

- Fitur analytics Android dihapus: `AnalyticsApi`, `RoomScoreCard`, `IssueCard`, `fetchAnalytics`.
- Branch role admin dihapus: `computeInspectionStatus` (DashboardViewModel) dan `MasterDataViewModel` selalu memakai scope `isMyRoom`.
- Dashboard inspector: section "Master Data Stats" (Ruangan + Item) dihapus; card "Total Inspeksi" dihapus karena nilainya identik dengan "Terkirim" (keduanya = jumlah `InspectionEntity`). Grid "Ringkasan Inspeksi" menjadi 3 card: Draf, Menunggu Kirim, Terkirim.
- Per-role section dashboard tidak ada lagi — dashboard = dashboard inspector.
- Penolakan login: `AuthRepository.login()` cek `response.user.role` sebelum menyimpan token → `AuthState.Error("Akun ini hanya untuk web dashboard")`.
- Sesi lama: `AuthRepository.init()` dan `refreshCurrentUser()` cek role user tersimpan/ter-refresh → role non-inspector memicu `forceLogout()`.
- ADR-0016 tetap berlaku: riwayat & cache inspeksi bersifat device-wide. Scope "Terkirim" per akun (`inspector_id`) dicatat sebagai follow-up, tidak dieksekusi.

## Considered Options

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Android inspector-only — dipilih** | Satu audience, satu mental model. Menghapus branch role di seluruh kode. Selaras dengan backend (analytics 403 untuk inspector, web dashboard untuk supervisor/admin). | Supervisor yang ingin cek analytics dari HP harus buka web. |
| Android multi-role (status quo) | Supervisor bisa akses analytics mobile. | Dashboard inspector menampilkan angka di luar tanggung jawabnya (6 vs 4). Branch role menyebar, duplikasi fitur web+Android, biaya perawatan ganda. |
| Supervisor tetap di Android, admin web-only | Menjaga analytics mobile. | Setengah-setengah: boundary per-role tidak konsisten, kode branch role tetap ada untuk dua role. |
