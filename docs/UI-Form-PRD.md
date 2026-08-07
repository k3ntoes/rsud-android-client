# Product Requirements Document (PRD)

# Redesign Halaman Form Inspeksi PPI Android

**RSUD Ajibarang**

**Versi:** 1.0
**Platform:** Android (Flutter + Material Design 3)

---

# 1. Latar Belakang

Halaman **Form Inspeksi** merupakan halaman utama yang paling sering digunakan oleh petugas PPI selama melakukan audit di ruangan rumah sakit.

Berdasarkan evaluasi UI saat ini, ditemukan beberapa masalah:

* Tampilan masih terlihat seperti form biasa.
* Informasi penting belum memiliki hirarki visual.
* Tombol skor kurang menarik dan sulit dibedakan ketika aktif.
* Area upload foto terlalu kecil.
* Progress inspeksi kurang terlihat.
* Jarak antar komponen terlalu rapat.
* User harus banyak melakukan scroll.

Redesain difokuskan agar proses inspeksi lebih cepat, lebih jelas, dan nyaman digunakan saat mobile.

---

# 2. Tujuan

Menciptakan halaman inspeksi yang:

* Modern
* Cepat digunakan
* Mudah dipahami
* Konsisten dengan Dashboard
* Mengurangi kesalahan input
* Mendukung inspeksi lapangan

---

# 3. Target User

Primary User

* IPCN
* IPCLN
* Surveyor Akreditasi
* Tim PPI

---

# 4. Design Language

Konsep

> **Modern Medical Inspection**

Karakteristik

* Dominan hijau
* Clean
* Soft shadow
* Rounded card
* Material 3
* White space banyak
* Mobile first

---

# 5. Color Palette

## Primary

```text
#16A34A
```

---

Primary Container

```text
#DCFCE7
```

---

Background

```text
#F5FAF7
```

---

Card

```text
#FFFFFF
```

---

Success

```text
#22C55E
```

---

Warning

```text
#F59E0B
```

---

Danger

```text
#EF4444
```

---

Text

```text
#1F2937
```

---

Subtitle

```text
#6B7280
```

---

# 6. Screen Structure

```
Header

↓

Score Information

↓

Inspection Card

↓

Inspection Card

↓

...

↓

Progress Footer

↓

Action Button
```

---

# 7. Header

## Existing

```
←

Form Inspeksi

ICU
```

---

## New

```
←

Form Inspeksi

ICU

Progress 2 / 10
```

Background menggunakan gradient hijau.

```
#16A34A

↓

#22C55E
```

Tambahkan ilustrasi transparan icon shield.

Header tinggi sekitar 150dp.

---

# 8. Score Legend

Saat ini

```
Skor:
0
1
2
```

Kurang menonjol.

Diubah menjadi Information Card.

```
ⓘ

0 = Berisiko (Foto wajib)

1 = Minor

2 = Sesuai
```

Background

Hijau muda.

Icon info.

Rounded 16dp.

---

# 9. Inspection Card

Setiap item inspeksi menjadi card.

Contoh

```
①

Penggunaan APD

Wajib Foto

----------------

Skor

[0] [1] [2]

----------------

Upload Foto

----------------

Catatan
```

---

Card memiliki

* Elevation 2dp
* Radius 24dp
* Shadow halus

---

# 10. Nomor Item

Tambahkan nomor.

Misalnya

```
1
```

Berada di lingkaran hijau.

Membantu orientasi saat inspeksi panjang.

---

# 11. Badge

Jika skor 0

Badge

```
Wajib Foto
```

Merah.

Jika skor 2

Badge

```
Selesai
```

Hijau.

---

# 12. Pilihan Skor

Saat ini

```
Berisiko

Minor

Sesuai
```

Kurang terlihat.

Diubah menjadi Selection Card.

```
⚠

0

Berisiko
```

```
—

1

Minor
```

```
✔

2

Sesuai
```

Saat dipilih

* Background berubah
* Border menjadi hijau
* Shadow naik

Animasi 150 ms.

---

# 13. Upload Foto

Saat ini

```
📷

Tambah
```

Kurang menarik.

Diubah menjadi Drop Area.

```
──────────────────

📷

Tambah Foto

Maks 5 Foto

──────────────────
```

Tampilan

Border dashed.

Icon besar.

Jika ada foto

```
□ □ □

Tambah
```

Menampilkan thumbnail.

---

# 14. Catatan

Diubah menjadi TextField Material 3.

Placeholder

```
Tambahkan catatan...
```

Tambahkan

Counter

```
0 / 300
```

---

# 15. Progress

Progress selalu terlihat.

```
Progress

█████████░

4 / 10 Item
```

Jika semua selesai

```
100%

Siap dikirim
```

Hijau.

---

# 16. Sticky Bottom Bar

Bottom Action selalu terlihat.

```
──────────────────────────

Progress

███████░░░

6/10

[Draft]

[Kirim]

──────────────────────────
```

Tidak ikut scroll.

---

# 17. Tombol

## Simpan Draft

Outlined Button

Hijau.

Icon

bookmark

---

## Kirim

Filled Button.

Hijau.

Icon

send

---

Jika belum lengkap

Button

Disabled.

---

# 18. Validation

Jika memilih

0

Muncul

```
⚠

Foto wajib diunggah.
```

Jika foto belum ada

Border merah.

---

Jika item belum diisi

```
Belum diisi
```

Abu.

---

# 19. Interaction

Saat memilih skor

Animasi Scale.

Saat upload

Ripple.

Saat berhasil upload

Checklist muncul.

---

# 20. Empty State

Belum ada foto

```
📷

Belum ada foto

Tambah Foto
```

---

Belum ada catatan

Placeholder abu.

---

# 21. Offline Mode

Header

```
Offline

Semua perubahan disimpan lokal.
```

Orange.

---

# 22. Accessibility

* Target sentuh minimal 48dp.
* Font mengikuti Dynamic Type.
* Kontras WCAG AA.
* Semua ikon memiliki label aksesibilitas.

---

# 23. Performance

Target:

* Pergantian item < 100 ms
* Scroll 60 FPS
* Upload thumbnail < 1 detik
* Auto-save draft tanpa jeda yang terasa

---

# 24. Acceptance Criteria

### Header

* Menampilkan nama form, nama ruangan, dan progres inspeksi.
* Menggunakan gradient hijau dengan ilustrasi medis.

### Legend Skor

* Ditampilkan dalam bentuk information card yang mudah dibaca.

### Item Inspeksi

* Setiap item berupa card dengan nomor, judul, badge status, pilihan skor, area foto, dan catatan.

### Pilihan Skor

* Menggunakan tiga selection card (0 Berisiko, 1 Minor, 2 Sesuai).
* Status terpilih memiliki perubahan warna, border, dan animasi.

### Upload Foto

* Menggunakan area upload bergaya dashed dengan thumbnail setelah foto berhasil ditambahkan.
* Menampilkan validasi "Wajib Foto" jika skor 0 dipilih.

### Progress & Navigasi

* Progress inspeksi tampil sebagai sticky footer.
* Tombol **Simpan Draft** dan **Kirim** selalu terlihat, dengan tombol **Kirim** aktif hanya jika seluruh item telah memenuhi validasi.

---

# 25. Rekomendasi Teknis (Flutter)

### Widget yang Disarankan

* `SliverAppBar` untuk header yang dapat menyusut saat scroll.
* `Card` dengan Material 3 dan `RoundedRectangleBorder(radius: 24)`.
* `SegmentedButton<int>` untuk pemilihan skor 0–2 agar konsisten dengan Material 3.
* `LinearProgressIndicator` untuk progres keseluruhan.
* `FilledButton` dan `OutlinedButton` untuk aksi utama.
* `TextField` Material 3 dengan penghitung karakter.
* `AnimatedContainer` dan `AnimatedSwitcher` untuk transisi status yang halus.

Dokumen ini menjadi acuan bagi tim UI/UX, developer Flutter, dan QA agar implementasi halaman **Form Inspeksi PPI** konsisten, modern, mudah digunakan, serta mendukung efisiensi kerja petugas PPI di RSUD Ajibarang.
