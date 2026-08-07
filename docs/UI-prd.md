# Product Requirements Document (PRD)

## Redesign UI/UX Aplikasi Android PPI RSUD Ajibarang

**Versi:** 1.0
**Tanggal:** Agustus 2026
**Platform:** Android
**Framework:** Flutter (Material Design 3)
**Pemilik Produk:** Tim PPI RSUD Ajibarang

---

# 1. Latar Belakang

Aplikasi Android PPI digunakan oleh petugas IPCN/IPCLN untuk melakukan inspeksi kepatuhan Pencegahan dan Pengendalian Infeksi (PPI) di seluruh unit pelayanan RSUD Ajibarang.

Tampilan aplikasi saat ini sudah berfungsi dengan baik, namun masih memiliki beberapa kekurangan:

* UI terlihat seperti aplikasi internal lama.
* Visual kurang mencerminkan identitas rumah sakit.
* Informasi prioritas belum terlihat jelas.
* Belum menggunakan Material Design 3.
* Dominasi warna putih membuat dashboard terasa datar.

Tujuan redesign adalah meningkatkan pengalaman pengguna (UX), mempercepat proses inspeksi, dan memberikan tampilan modern dengan identitas visual yang sesuai dengan lingkungan kesehatan.

---

# 2. Tujuan Produk

Meningkatkan efisiensi petugas inspeksi melalui dashboard yang:

* lebih modern
* mudah dibaca
* fokus terhadap progress inspeksi
* nyaman digunakan selama bekerja di lapangan

---

# 3. Sasaran Pengguna

### Primary User

* IPCN
* IPCLN
* Surveyor PPI

### Secondary User

* Ketua Tim PPI
* Kepala Ruangan
* Manajemen Rumah Sakit

---

# 4. Design Principles

Menggunakan konsep

> **Modern Medical Dashboard**

Karakteristik:

* Clean
* Minimalis
* Soft Color
* Material Design 3
* Banyak whitespace
* Rounded Corner
* Friendly
* Cepat dipahami

---

# 5. Color System

## Primary

Medical Green

```
#16A34A
```

---

Primary Dark

```
#15803D
```

---

Secondary

```
#22C55E
```

---

Background

```
#F5FAF7
```

---

Card

```
#FFFFFF
```

---

Text Primary

```
#1F2937
```

---

Subtitle

```
#6B7280
```

---

Success

```
#10B981
```

---

Warning

```
#F59E0B
```

---

Danger

```
#EF4444
```

---

# 6. Typography

Google Font

**Inter**

atau

**Google Sans**

Hierarchy

| Elemen      | Size |
| ----------- | ---- |
| Dashboard   | 30   |
| Section     | 22   |
| Card Number | 34   |
| Card Title  | 16   |
| Body        | 14   |
| Caption     | 12   |

---

# 7. Dashboard Layout

```
HEADER

↓

Inspection Summary

↓

Today's Progress

↓

Room Status

↓

Floating Action Button

↓

Bottom Navigation
```

---

# 8. Header

## Existing

```
Dashboard

Inspector

Keluar
```

## New

```
Selamat Datang 👋

Dashboard PPI

Inspector

Sinkron terakhir

13:09 WIB

Keluar
```

Background menggunakan gradient.

```
#16A34A

↓

#22C55E
```

Header memiliki ilustrasi transparan bertema kesehatan (shield/medical cross) untuk memperkuat branding.

---

# 9. Inspection Summary

Menampilkan tiga metrik utama:

* Draft
* Menunggu Kirim
* Terkirim

Masing-masing menggunakan:

* Rounded Card
* Soft Shadow
* Circular Icon
* Large Number

Contoh:

```
🟢

15

Draft
```

---

# 10. Progress Hari Ini

Widget baru.

Berisi:

```
Progress Hari Ini

1 dari 4 Ruangan

████████░░░░

25%
```

Ditambah Circular Progress di sisi kanan.

---

# 11. Status Ruangan

Setiap ruangan dibuat sebagai card.

Contoh

```
ICU

6 Item

██████░░░

0 / 6

Belum diperiksa

Mulai
```

---

Komponen

* Nama Ruangan
* Icon
* Jumlah item
* Progress Bar
* Badge Status
* Tombol aksi

---

Status menggunakan warna.

Belum

Abu

Sedang

Kuning

Selesai

Hijau

---

# 12. Floating Action Button

Posisi

Bottom Right

Label

```
+ Mulai Inspeksi
```

Aksi

Membuat inspeksi baru.

---

# 13. Bottom Navigation

Empat menu.

```
Dashboard

Inspeksi

Riwayat

Profil
```

Material Symbols Rounded.

---

# 14. Icon System

Menggunakan

Material Symbols Rounded

Contoh

Dashboard

Health

Hospital

Checklist

Cloud Sync

Task

History

Person

Logout

---

# 15. Micro Interaction

Saat card ditekan

* Elevation naik
* Ripple hijau

Saat tombol ditekan

* Scale 98%
* Ripple

Saat sinkronisasi

Animasi icon cloud.

---

# 16. Empty State

Draft kosong

```
Belum ada Draft

Mulai inspeksi pertama Anda.
```

---

Tidak ada ruangan

```
Tidak ada inspeksi hari ini.
```

---

# 17. Loading

Menggunakan

Skeleton Loading

Bukan Circular Progress saja.

---

# 18. Offline Mode

Jika internet mati.

Header berubah.

```
Offline

Perubahan akan dikirim saat koneksi tersedia.
```

Warna

Orange.

---

# 19. Sync Status

Status cloud.

Hijau

```
Semua data tersinkron
```

Orange

```
Menunggu sinkronisasi
```

Merah

```
Sinkronisasi gagal
```

---

# 20. Accessibility

* Kontras memenuhi WCAG AA.
* Target sentuh minimal 48 × 48 dp.
* Mendukung Dynamic Font Size.
* Ikon disertai label teks.

---

# 21. Performance

Target:

* Splash < 2 detik.
* Dashboard < 1 detik.
* Scroll 60 FPS.
* Ukuran APK tetap ringan.

---

# 22. Acceptance Criteria

### Dashboard

* Header menggunakan gradient hijau.
* Menampilkan waktu sinkronisasi.
* Menampilkan greeting pengguna.

### Ringkasan

* Card memiliki shadow dan sudut membulat.
* Menampilkan jumlah Draft, Menunggu Kirim, dan Terkirim.

### Progress

* Progress linear dan circular tampil sesuai data.
* Persentase diperbarui secara real-time.

### Status Ruangan

* Menampilkan nama ruangan, jumlah item, progres, status, dan tombol aksi.
* Warna badge berubah sesuai status inspeksi.

### Navigasi

* FAB "Mulai Inspeksi" selalu terlihat.
* Bottom Navigation memiliki empat menu utama.

---

# 23. Visual Reference

Desain yang menjadi acuan:

* Google Material Design 3
* Google Health Connect
* Google Fit
* Microsoft Fluent Mobile
* Apple Health
* NHS Mobile App
* Halodoc (untuk pola kartu dan ruang putih)

---

## Hasil yang Diharapkan

Redesain ini diharapkan menghasilkan dashboard yang lebih modern, intuitif, dan profesional, dengan identitas visual hijau yang kuat sebagai representasi lingkungan kesehatan. Fokus utama adalah mempercepat akses ke informasi penting (status inspeksi, progres harian, dan sinkronisasi), mengurangi beban kognitif pengguna, serta meningkatkan efisiensi petugas PPI saat melakukan inspeksi di lapangan.
