# Coding Rules — RSUD Ajibarang Android Client

Aturan ini wajib diikuti oleh **semua AI agent** yang menulis, mengubah, atau men-review kode di repository ini.

---

## 1. Prinsip Desain

### YAGNI (You Ain't Gonna Need It)
- Jangan tulis kode untuk fitur yang belum diminta.
- Jangan tambahkan abstraksi, parameter, atau fleksibilitas "untuk jaga-jaga".
- Jika fitur belum ada di PRD, jangan dibuat. Jika dibutuhkan nanti, akan ditambahkan nanti.
- Pertanyaan sebelum nambah kode: *"Apakah fitur ini dibutuhkan sekarang?"* Jika tidak, skip.
- **Khusus Android:** Jangan buat ViewModel/UseCase/Repository layer sebelum ada kebutuhan nyata. Function biasa cukup untuk logika sederhana.

### KISS (Keep It Simple, Stupid)
- Solusi paling sederhana yang bekerja adalah yang terbaik.
- Jangan bikin class/pattern complex kalau function biasa sudah cukup.
- Lebih suka standard library/Kotlin stdlib daripada custom utility.
- Lebih suka inline logic daripada abstraction layer yang tidak perlu.
- **Khusus Compose:** Jangan extract composable function yang hanya dipakai sekali. Extract saat复用 minimal ke-3.

### DRY (Don't Repeat Yourself)
- Jika pola yang sama muncul 2+ kali, extract ke function/component.
- Tapi jangan extract terlalu dini — **tunggu hingga pola ke-3**. (YAGNI > DRY)
- Duplikasi yang tidak disengaja lebih baik daripada abstraksi yang premature.

---

## 2. Aturan File

### Maksimal 300 Baris per File
- Setiap file **tidak boleh melebihi 300 baris** (termasuk imports dan comments).
- Jika sebuah file mencapai batas, refactor dengan memisahkan tanggung jawab ke file baru.
- Pengecualian: file konfigurasi Gradle, migration Room, atau data class besar (max 400 baris).

### Satu Tanggung Jawab per File
- Setiap file harus punya **satu alasan untuk berubah** (Single Responsibility).
- Contoh yang benar:
  - `AuthViewModel.kt` — hanya ViewModel auth
  - `LoginScreen.kt` — hanya komposisi UI login
  - `AuthRepository.kt` — hanya logika autentikasi
- Contoh yang salah:
  - `Utils.kt` — tempat sampah berbagai fungsi tidak terkait
  - `Screens.kt` — semua screen dalam satu file

### Naming Convention
- **Kotlin/Android**: 
  - `PascalCase` untuk class, interface, enum, composable function
  - `camelCase` untuk function, variabel, properties
  - `UPPER_SNAKE_CASE` untuk konstanta (`const val`, `enum` entries)
  - `snake_case` untuk XML resource files (seperti `my_view.xml`)
- **File names**:
  - File class → `PascalCase.kt` (misal `AuthViewModel.kt`)
  - File non-class (extensions, helpers) → `camelCase.kt` (misal `dateExtensions.kt`)
- **Composable functions**: `PascalCase`, dinamai sesuai screen/component (misal `LoginScreen`, `ScoreIndicator`)
- **ViewModel**: akhiri dengan `ViewModel` (misal `AuthViewModel`, `InspectionFormViewModel`)
- **Repository**: akhiri dengan `Repository` (misal `AuthRepository`, `InspectionRepository`)

### Package Structure
- Package names: lowercase, tanpa underscores.
- Satu package per domain context:
  ```
  my.id.kentoes.rsudajibarangapp
  ├── auth/
  │   ├── AuthViewModel.kt
  │   ├── LoginScreen.kt
  │   └── CONTEXT.md
  ├── inspections/
  │   ├── InspectionViewModel.kt
  │   ├── InspectionFormScreen.kt
  │   └── CONTEXT.md
  ├── sync/
  │   ├── SyncWorker.kt
  │   └── CONTEXT.md
  └── core/
      ├── model/
      ├── network/
      ├── di/
      └── CONTEXT.md
  ```

---

## 3. Research & Context Gathering

### 🔷 WAJIB: Gunakan Graphify Sebelum Membaca File Apapun
Sebelum membaca file atau membuat perubahan, AI agent **WAJIB** menggunakan Graphify knowledge graph sebagai langkah pertama:

1. `graphify query "Bagaimana arsitektur <konsep>?"` — pahami alur dan relasi kode
2. `graphify path "<A>" "<B>"` — cari jalur antara 2 konsep (jika perlu)
3. `graphify explain "<simbol>"` — detail suatu simbol/modul (jika perlu)
4. **Setelah graphify memberi jawaban**, baru baca file spesifik yang disebutkan

> **Mengapa?** Query ke graphify hanya memakan ~100-500 token, sedangkan membaca file langsung bisa ribuan token.

> **Fallback**: Jika `graphify-out/graph.json` tidak ada atau rusak, rebuild dengan `graphify extract ./app/src/main/ --code-only --no-viz` lalu copy ke root.

### Wajib: Gunakan GitNexus untuk Impact Analysis
Setelah memahami arsitektur via Graphify, gunakan GitNexus untuk analisis dampak sebelum mengedit kode:

1. `gitnexus://repo/rsud-android-client/context` — cek index freshness
2. Jika index stale, jalankan `node .gitnexus/run.cjs analyze`
3. Sebelum mengedit symbol: **`impact({target: "symbolName", direction: "upstream"})`** dan report blast radius
4. Gunakan `query()` untuk execution flow detail
5. Gunakan `context()` untuk 360-degree view
6. Gunakan `detect_changes()` sebelum commit

### Difference: Graphify vs GitNexus

| Graphify | GitNexus |
|----------|----------|
| 🎯 Arsitektur tingkat tinggi & relasi konsep | 🎯 Impact analysis per symbol |
| 💬 Query bahasa alami ("Bagaimana alur login?") | 💬 Query symbol spesifik |
| 🗺️ Shortest path antar konsep | 🛡️ Blast radius / call graph |
| 🔍 Penjelasan node/simbol | 🔄 Rename refactoring |
| ✅ **Langkah #1** — untuk memahami | ✅ **Langkah #2** — untuk mengubah |

### 🔷 WAJIB: Gunakan Context7 untuk Dokumentasi Library (Bukan Web Fetch)

**Context7 MCP** sudah terkonfigurasi dan harus menjadi pilihan PERTAMA untuk dokumentasi library/framework. Jangan pakai `researcher_web` atau `read_url` untuk ini.

#### Kenapa Context7 Lebih Baik?

| Metode | Biaya Token | Akurasi | Kecepatan |
|--------|-------------|---------|-----------|
| ✅ **Context7** `query-docs` | ~100-500 token | Tinggi (dokumentasi resmi) | Cepat (API langsung) |
| ❌ `researcher_web` + `read_url` | ~2,000-5,000+ token | Rendah (scrape HTML, iklan, noise) | Lambat (buka web, baca HTML) |

#### Always Do

Sebelum menulis kode yang melibatkan library/framework Android, AI agent **WAJIB**:

1. **Load skill dulu**: `skill("context7-mcp")`
2. **Resolve library ID**: panggil `resolve-library-id` dengan nama library
3. **Query docs**: panggil `query-docs` dengan `libraryId` + pertanyaan spesifik
4. **Satu konsep per query** — jangan gabung multiple konsep dalam satu `query-docs`

#### Never Do

- **JANGAN** gunakan `researcher_web` + `read_url` untuk dokumentasi library — ini boros token 5-10x lipat.
- **JANGAN** andalkan training data untuk API signatures — dokumentasi Android berubah cepat (Compose, Room 3.0+, Hilt 2.x), selalu pakai Context7.
- **JANGAN** gabung multiple konsep dalam satu `query-docs` — hasilnya dangkal untuk setiap topik.

#### Exception

- Jika Context7 MCP tidak tersedia (server down atau API key expired), fallback ke `researcher_docs` agent, lalu `read_url` langsung ke developer.android.com.
- Untuk pertanyaan tentang ekosistem (bukan dokumentasi teknis), `researcher_web` lebih cocok.

### Baca Domain Docs Terkait
- Baca `CONTEXT-MAP.md` untuk menemukan context yang relevan
- Baca `app/.../<context>/CONTEXT.md` untuk glossary dan key decisions
- Baca `docs/adr/` untuk keputusan arsitektural yang sudah dibuat

---

## 4. Code Quality

### Error Handling
- **View tidak handle error langsung** — delegasikan ke ViewModel. ViewModel expose `UiState` dengan field error.
- Gunakan `Result<T>` atau `sealed class` untuk merepresentasikan status loading/success/error.
- Jangan swallow exceptions tanpa log — minimal `Log.e()`.
- Gunakan `try/catch` di repository/data layer, konversi ke `Result<T>` sebelum dikirim ke ViewModel.

### State Management (Compose)
- **State hoisting**: State diangkat ke ViewModel, UI hanya render.
- Gunakan `StateFlow` di ViewModel, collect sebagai `State` di Compose.
- Hindari `mutableStateOf` di dalam composable untuk data yang perlu survive configuration change.
- Gunakan `derivedStateOf` untuk computed properties yang reaktif.

### Composition Local
- Hindari `CompositionLocalProvider` untuk state yang sering berubah — gunakan parameter biasa.
- `CompositionLocal` hanya untuk dependency silang (theme, nav controller, scope).

### Testing
- Unit test untuk semua ViewModel logic (validasi skor, idempotency, state transition).
- Unit test untuk Repository (mock API + Room).
- Integration test (instrumented) untuk Room DAO dan WorkManager flow.
- Nama test harus deskriptif: `testSubmitInspection_duplicateIdempotency`.

### Keamanan
- **Token storage**: Proto DataStore + Google Tink (AEAD encryption + Android Keystore). Jangan simpan token di SharedPreferences.
- **Camera**: Intent-based (MediaStore.ACTION_IMAGE_CAPTURE) — request permission runtime.
- **Network**: Jangan log sensitive data (token, password). Gunakan OkHttp logging interceptor hanya di debug.
- **Input**: Validasi input dari server (tidak pernah 100% trusted) sebelum dipakai di UI.
- **Environment variables / BuildConfig**: JWT secret, base URL hanya di BuildConfig atau encrypted storage.
- **ProGuard / R8**: Pastikan rules.keep untuk model class yang di-serialize.

### Performance
- **Image loading**: Coil + cache disk. Kompresi gambar sebelum upload (max 300KB).
- **Room queries**: Gunakan `@Transaction` untuk operasi multi-tabel.
- **Recomposition**: Hindari `remember`/`derivedStateOf` untuk kalkulasi mahal. Gunakan `LaunchedEffect` untuk side effects.
- **Background work**: Semua network call via Retrofit suspending functions (`suspend`) atau WorkManager. Retrofit + Kotlin Coroutines sudah berjalan di background thread via OkHttp dispatcher — tidak perlu `Dispatchers.IO` manual. Jangan blocking main thread.

---

## 5. Proses Development

### Urutan Implementasi (sesuai PRD dan ADR)
1. **Auth** — login, token management, refresh, force logout
2. **Core** — network layer, DI setup, base types, Room database
3. **Master Data** — download & cache inspection items, rooms
4. **Inspections** — form dinamis, skoring, bukti foto, draf lokal
5. **Sync** — WorkManager, two-step upload, kompresi gambar
6. **Refinement** — error handling, loading states, offline UX

### Checklist Sebelum Commit
- [ ] Semua test passing (unit + instrumented jika ada)
- [ ] Tidak ada debug code / `Log.d()` / `println()` di release code
- [ ] Tidak ada commented-out code
- [ ] Tidak ada file > 300 baris (kecuali pengecualian)
- [ ] Tidak ada duplikasi yang tidak perlu
- [ ] Blast radius sudah di-check via GitNexus `detect_changes()`
- [ ] ADR/PRD compliance — apakah perubahan sesuai dengan dokumen yang sudah disepakati?
- [ ] `CONTEXT.md` vocabulary digunakan dengan benar (tidak ada synonym drift)
- [ ] ProGuard/R8 rules sudah diupdate jika ada class baru yang di-reflect/serialize
