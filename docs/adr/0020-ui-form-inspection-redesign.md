# ADR-0020: UI Form Inspeksi Redesign — Aturan Foto & Batas Upload

**Status:** Accepted  
**Date:** 2026-08-07  
**Deciders:** kentoes (grill session dengan Antigravity)

---

## Context

PRD "Redesign Halaman Form Inspeksi PPI Android" (docs/UI-Form-PRD.md) mendefinisikan ulang
aturan validasi foto dan batas upload per item inspeksi. Dua keputusan di sini bersifat
hard-to-reverse karena mempengaruhi `ItemState.isValid` (kontrak submit) dan ekspektasi
petugas di lapangan.

---

## Decisions

### 1. Skor 1 (Minor Defect) kini wajib foto

**Sebelumnya:** hanya skor 0 (Berisiko) yang wajib foto. Skor 1 opsional.

**Sesudahnya:** skor 0 **dan** skor 1 wajib minimal 1 foto. Skor 2 tidak perlu foto.

**Alasan:** Temuan "Minor" tetap merupakan temuan yang perlu terdokumentasi untuk keperluan
audit akreditasi RSUD Ajibarang. Tanpa foto, temuan Minor tidak dapat diverifikasi Supervisor.
Ini konsisten dengan praktik audit lapangan — petugas sudah terbiasa memfoto setiap temuan.

**Trade-off yang dipertimbangkan:**
- Lebih banyak langkah bagi petugas (harus ambil foto di skor 1)
- Kualitas data audit meningkat signifikan
- Konsisten: skor yang menunjukkan masalah (0 dan 1) selalu punya bukti visual
- Mencegah "klik 1 lalu lanjut" tanpa dokumentasi

**Implementasi:** `ItemState.isValid`:
```kotlin
// Before
skor == 0 -> fotoPaths.isNotEmpty()
// After
skor == 0 || skor == 1 -> fotoPaths.isNotEmpty()
```

### 2. Batas maksimal 5 foto per item (UI-only)

**Keputusan:** Tombol "Tambah Foto" disembunyikan jika `fotoPaths.size >= 5`. Tidak ada
validasi di ViewModel atau backend — ini adalah batas UX semata.

**Alasan:**
- Mencegah upload berlebihan yang membebani jaringan dan storage
- PRD dan gambar referensi secara eksplisit menyebutkan "Maks. 5 foto"
- Batas di UI (bukan hard limit DB/API) cukup — tidak perlu migrasi schema

**Trade-off yang dipertimbangkan:**
- Petugas tidak bisa tambah foto ke-6 meski perlu
- Cukup untuk dokumentasi audit (5 foto per item sangat memadai)
- Implementasi sederhana: satu kondisi `if (fotoPaths.size < 5)` di `ItemCard`

---

## Consequences

- `ItemState.isValid` berubah -> test `InspectionFormViewModelTest` perlu diupdate
- `ItemCard` perlu conditional show/hide tombol Tambah Foto
- CONTEXT.md terms **Skor**, **Bukti Foto**, dan **Kirim** diupdate (done)
- Tidak ada perubahan di backend, WorkManager, Room schema, atau API payload
