# ADR-0019: Urutan Checklist Inspeksi — Konsumsi `sort_order` (Room-Item Pivot)

**Status**: Accepted

**Tanggal**: 2026-08-07

**Konteks BE**: ADR-0013 BE menambahkan kolom `sort_order` pada pivot `room_items` — Admin PPI
mengatur urutan item per ruangan via web-admin (tombol ▲/▼). Kontrak `docs/BE/docs/android-to-be-api-contract.md`
§2.2 mengirim `sort_order` di payload `GET /api/room-items` dan menetapkan aturan urut
`sort_order ASC, item_id ASC`. Task Android (`odx`) ditandai "Done (docs)" di sisi BE —
**kode Android di repo ini belum mengonsumsi `sort_order` sama sekali** (nilai dibuang saat parse
karena `RoomItemDto` tidak punya field-nya; `RoomItemEntity` tidak menyimpannya).

## Context

Sebelum keputusan ini, form inspeksi menampilkan item **urut abjad**
(`ORDER BY kategori, nama` di query master items), dengan grouping per `kategori` — padahal
`kategori` selalu kosong (`kategori = ""` hardcoded di `syncItems`; BE tidak punya konsep kategori:
`inspection_items` hanya `id/name/is_active/updated_at`). Artinya:
- Urutan tampilan tidak pernah diatur Admin — acak-abjad, tidak bermakna bisnis.
- Grouping kategori adalah **vestigial** — menampilkan header kosong.

Dengan kontrak `sort_order`, BE kini menyediakan sumber kebenaran urutan yang dikelola bisnis.
Keputusan ini menentukan bagaimana Android mengonsumsinya — termasuk perilaku draft/resume.

## Considered Options

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Pertahankan urutan abjad** (ignore sort_order) | Tidak ada perubahan tampilan | Melanggar kontrak; reorder Admin tidak pernah sampai ke inspector |
| **Urut (sort_order, item_id) tapi biarkan grouping kategori** | Urutan benar | Header kategori kosong tetap muncul — UI aneh (vestigial) |
| **Urut (sort_order, item_id), hapus grouping kategori — dipilih** | Urutan sesuai kontrak, UI bersih, satu-satunya sumber urutan jelas | Perubahan perilaku tampilan (abjad → urutan Admin) |

Untuk resume draft, ada dua sub-keputusan:

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Pertahankan urutan draft apa adanya** | Inspector melihat apa yang ia tinggalkan | Tidak konsisten dengan urutan form baru; reorder yang terjadi saat draft disimpan tidak terlihat |
| **Sort ulang pakai pivot terbaru — dipilih** | Satu aturan urutan di semua jalur (form baru & resume) | Urutan bisa berubah dari yang inspector ingat |
| **Drop item non-pivot saat resume** | Draf selalu selaras kebenaran server | Menghilangkan skor/foto inspector; jika pivot belum ter-sync, resume jadi form kosong — regresi keputusan review 2026-08 (resume tidak boleh bergantung pivot) |
| **Pertahankan item non-pivot di akhir — dipilih** | Tidak kehilangan pekerjaan; resume tetap jalan walau pivot kosong; urutan utama tetap mengikuti kontrak | Item non-pivot ikut ter-submit (payload berisi item yang sudah tidak terasosiasi — BE saat ini tidak menolak) |

## Decision

1. **Sumber tunggal urutan checklist = pivot `room_items`** dengan aturan `sort_order ASC, item_id ASC`
   (tie-breaker `item_id`).
2. **Grouping kategori DIHAPUS dari form inspeksi** — `InspectionFormUiState.groupedItems` dan
   header kategori di `InspectionFormScreen` dihilangkan; item dirender sebagai satu daftar rata
   dalam urutan checklist.
3. **Resume draft di-sort ulang** memakai pivot terbaru; item yang tidak lagi di pivot
   (dilepas Admin setelah draft disimpan, atau pivot belum ter-sync) **tetap dipertahankan di
   akhir urutan** (urut `item_id`) — tidak di-drop.
4. **Sync tetap full-snapshot (replace-all dari epoch)** — tidak berubah. Reorder Admin sampai
   ke Android karena snapshot selalu penuh; nilai `sortOrder` kini disimpan saat mapping DTO→Entity
   (sebelumnya dibuang).
5. **Migrasi Room 7→8 non-destruktif** (`ALTER TABLE room_item ADD COLUMN sortOrder INTEGER
   NOT NULL DEFAULT 0`) — pertama kali project memakai `addMigrations`; draf inspeksi & riwayat
   user selamat saat upgrade. `fallbackToDestructiveMigration()` dipertahankan sebagai safety net.

### Implementasi

- `RoomItemDto.sortOrder` + `RoomItemEntity.sortOrder` — parse & simpan (bukan dibuang).
- `MasterDataRepository.syncRoomItems()` — map `sortOrder = dto.sortOrder`.
- `InspectionFormViewModel` — `orderByChecklist(states, sortMap)`:
  comparator `(inPivot ? 0 : 1, sortOrder, itemId)` — item pivot di depan, non-pivot di akhir.
- `InspectionFormScreen` — render `uiState.items` flat (tanpa header kategori).

## Consequences

### Positif

- Urutan checklist inspeksi dikendalikan Admin PPI (bisnis) dan konsisten di semua perangkat.
- Reorder Admin langsung terlihat setelah sync berikutnya (snapshot penuh).
- Resume tidak pernah menghilangkan pekerjaan inspector.
- Migrasi DB tidak menghapus draf/riwayat user.

### Negatif

- Perilaku tampilan berubah: dari abjad → urutan Admin (perubahan UX yang disengaja).
- Item non-pivot pada resume tetap ter-submit. **Terverifikasi (2026-08-07, kode BE
  `rsud-server-stack/backend/app/modules/inspection/services.py`)**: BE **TIDAK menolak**
  item ekstra — `submit_inspection` hanya memeriksa item yang KURANG
  (`room_item_ids - submitted_ids` → 422 `VALIDATION_ERROR`), tidak ada cek kelebihan
  (`submitted_ids - room_item_ids`). Item ekstra diterima dan tersimpan; `item_name_snapshot`
  diisi nama asli jika item masih aktif di master, selain itu `"Unknown"`. Implikasi minor:
  riwayat inspeksi bisa memuat item yang sudah tidak terasosiasi dengan room (wajar —
  snapshot apa yang dinilai inspector saat itu). Satu-satunya kegagalan 409 terjadi saat
  idempotency key duplikat atau item_id tidak ada di master sama sekali (IntegrityError
  tertangkap `except Exception` → 409 — skenario ini tidak terjadi untuk item draft nyata).
- `kategori` di `ItemState`/`MasterDataItem` masih ada (dipakai layar Master Data) — hanya grouping
  di form yang dihapus.

## Referensi

- Kontrak: `docs/BE/docs/android-to-be-api-contract.md` §2.2 (payload `sort_order`)
- ADR BE: `docs/BE/docs/adr/0013-room-item-ordering.md`
- Glossary: `inspections/CONTEXT.md` (term *Room-Item Pivot*, *Urutan Checklist*)
- Claim-order BE: `docs/BE/docs/room-item-ordering-claim-order.md` (issue `odx`)
