# Checklist Implementasi: UI Form Inspeksi Redesign

> Dibuat dari sesi grill 2026-08-07. Lihat [UI-Form-PRD.md](docs/UI-Form-PRD.md) dan [ADR-0020](docs/adr/0020-ui-form-inspection-redesign.md).

---

## Keputusan Final dari Sesi Grill

| # | Topik | Keputusan |
|---|-------|-----------|
| 1 | Platform | Jetpack Compose — PRD tetap valid sebagai UI/UX spec |
| 2 | Header | **Fixed** (tidak collapse saat scroll), ~150dp, gradient hijau |
| 3 | Score toggle | **Dipertahankan** — tap terpilih → deselect ke `-1` |
| 4 | Batas foto | **5 foto max** per item, enforced di UI (hide tombol jika >= 5) |
| 5 | Counter catatan | **Soft indicator** `0/300` saja, tidak ada hard limit |
| 6 | Aturan wajib foto | **Skor 0 & 1 wajib foto**, skor 2 tidak perlu |
| 7 | Badge logic | **Dinamis**: 0/1 + belum foto → "Wajib Foto" merah; 0/1 + ada foto → "Selesai" hijau; 2 → "Selesai" hijau; -1 → tidak ada badge |
| 8 | Update test | **Ya**, update sekaligus saat implementasi |
| 9 | Scope | **InspectionFormScreen dan komponennya saja** |

---

## Claim Order & Checklist

> ⚠️ **Langkah #0 WAJIB** sebelum claim issue apapun: baca `CODING-RULES.md`

### Langkah 0 — Sebelum Mulai (WAJIB)

- [ ] Baca `CODING-RULES.md` (tidak auto-read)
- [ ] Jalankan `graphify query "InspectionFormScreen ItemCard ScoreIndicator"` untuk orientasi
- [ ] Jalankan `bd update <issue-id> --claim` setelah baca CODING-RULES

---

### Issue 1 — `rsud-android-client-2va`
**UI Form Inspeksi Redesign: Header gradient + legend card**

*Kerjakan PERTAMA — tidak ada dependensi ke issue lain*

- [ ] Claim: `bd update rsud-android-client-2va --claim`
- [ ] Ganti `TopAppBar` → `Box` dengan `Brush.verticalGradient(#16A34A → #22C55E)`
- [ ] Tinggi header ~150dp, back button tetap ada (icon putih)
- [ ] Judul "Form Inspeksi" + subtitle `roomName` (teks putih)
- [ ] Tambah teks "Progress X/Y" (dari `uiState.validItems` / `uiState.totalItems`)
- [ ] Tambah shield icon transparan (alpha ~0.15f) di pojok kanan
- [ ] Ganti score legend item dari plain Text → info card (background `surfaceContainerLow`, icon `Info`, rounded 16dp)
- [ ] Build + visual check
- [ ] Close: `bd update rsud-android-client-2va --status done`

---

### Issue 2 — `rsud-android-client-1rk`
**UI Form Inspeksi Redesign: ScoreIndicator → Selection Card**

*Kerjakan KEDUA (bisa paralel dengan issue 1)*

- [ ] Claim: `bd update rsud-android-client-1rk --claim`
- [ ] Hapus semua kode `FilterChip` di `ScoreIndicator.kt`
- [ ] Buat 3 `OutlinedCard` clickable dalam `Row` (weight = 1f each)
- [ ] Tiap card: icon (Warning/Remove/CheckCircle), angka bold, label
- [ ] State terpilih: `animateColorAsState(tween(150))` → background warna 15% opacity, border solid
- [ ] State belum dipilih (-1): semua abu/outline tipis
- [ ] Toggle: tap card terpilih → `onScoreSelected(-1)`
- [ ] Min touch target 48dp per card
- [ ] Build + visual check semua 4 state (-1, 0, 1, 2)
- [ ] Close: `bd update rsud-android-client-1rk --status done`

---

### Issue 3 — `rsud-android-client-xw1`
**UI Form Inspeksi Redesign: ItemCard redesign**

*Kerjakan KETIGA — setelah issue 1 & 2 selesai*

- [ ] Claim: `bd update rsud-android-client-xw1 --claim`
- [ ] Tambah parameter `itemNumber: Int` ke `ItemCard()`
- [ ] Di `InspectionFormScreen.kt`: ganti `items(...)` → `itemsIndexed(...)`, teruskan `index + 1`
- [ ] Tampilkan nomor: `Box` circle primary, diameter 32dp, teks putih bold
- [ ] Hapus left-accent bar (diganti circle number)
- [ ] Card: `shape = MaterialTheme.shapes.extraLarge` (24dp), elevation 2dp
- [ ] Buat `PhotoDropArea.kt` baru: dashed border, icon CameraAlt 48dp, teks, hanya tampil jika `fotoPaths.size < 5`
- [ ] Badge header: logika dinamis per keputusan grill (lihat tabel atas)
- [ ] Label "Foto Bukti (N)" + teks merah "Wajib diisi jika skor 0 atau 1" (conditional)
- [ ] Catatan: `supportingText = { Text("${catatanText.length}/300") }` (soft)
- [ ] Build + visual check semua kombinasi state
- [ ] Close: `bd update rsud-android-client-xw1 --status done`

---

### Issue 4 — `rsud-android-client-8jy`
**Update ItemState.isValid + test (skor 1 wajib foto)**

*Kerjakan TERAKHIR — setelah semua UI selesai*

- [ ] Claim: `bd update rsud-android-client-8jy --claim`
- [ ] Edit `ItemState.kt` isValid: `skor == 0 || skor == 1 -> fotoPaths.isNotEmpty()`
- [ ] Update komentar KDoc `isValid`
- [ ] Update test skor 1 tanpa foto: expectation berubah dari valid → invalid
- [ ] Tambah test: "skor 1 dengan foto → valid"
- [ ] Jalankan `./gradlew test` — semua hijau
- [ ] Close: `bd update rsud-android-client-8jy --status done`

---

## Post-Implementation Checklist

- [ ] `./gradlew assembleDebug` — tidak ada error/warning baru
- [ ] Visual review semua state item: belum isi / skor 0 tanpa foto / skor 0 ada foto / skor 1 / skor 2 / semua selesai
- [ ] Test offline banner masih muncul
- [ ] Test tombol Kirim disabled sampai semua item valid
- [ ] Test Simpan Draft masih berjalan
- [ ] Update graphify graph: `graphify extract ./app/ --code-only --no-viz && cp ./app/graphify-out/graph.json graphify-out/graph.json`
