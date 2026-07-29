## 📖 Cara Membaca Dokumen Ini

| File | Fungsi | Dibaca Otomatis? |
|------|--------|------------------|
| **`CLAUDE.md`** | Quick-reference ringkas — prioritas eksplorasi, stack Android, ✅❌ | ⚠️ Beberapa AI tools (Claude Code, dll) |
| **`AGENTS.md`** (ini) | Dokumentasi detail — semua workflow, exception, tabel, Android patterns | ❌ Tidak, harus dibaca manual |
| **`CODING-RULES.md`** | Coding standards — YAGNI, KISS, 300 baris/file, keamanan Android | ❌ Tidak, harus dibaca manual |
| **`knowledge.md`** | Auto-inject — prioritas tools | ⚠️ Dibaca tools tertentu (Codebuff) |

> **Mulailah dengan `CLAUDE.md`** jika Anda AI agent yang mendukung auto-read. Jika tidak, baca `AGENTS.md` ini untuk panduan lengkap.

> ⚠️ **`CODING-RULES.md` tidak auto-read!** Agent WAJIB membaca manual sebagai langkah #0 sebelum claim issue / implement kode.

---

## 🧭 Graphify-First Exploration

Sebelum membaca file APAPUN, AI agent **WAJIB** menggunakan `graphify` untuk memahami codebase terlebih dahulu. Ini menghemat ribuan token dengan menghindari pembacaan file secara buta.

### Always Do

- **WAJIB: Gunakan `graphify query` sebelum membaca file.** Untuk memahami bagaimana suatu fitur bekerja, jalankan:
  ```bash
  graphify query "Bagaimana alur <fitur>?"
  ```
  Ini akan mengembalikan node-node relevan dari knowledge graph tanpa perlu membaca file secara langsung.

- **WAJIB: Gunakan `graphify path` untuk mencari jalur antar 2 konsep.**
  ```bash
  graphify path "Auth" "Sync"
  ```

- **WAJIB: Gunakan `graphify explain` untuk penjelasan detail suatu simbol/modul.**
  ```bash
  graphify explain "InspectionViewModel"
  ```

- **WAJIB: Cek god nodes dan surprising connections** untuk mendapatkan gambaran besar codebase:
  ```bash
  graphify query "Apa god nodes di codebase ini?"
  graphify query "Apa surprising connections?"
  ```

- **Setelah graphify memberi gambaran, baru baca file spesifik** yang diperlukan — bukan seluruh folder.

### Never Do

- **JANGAN langsung membaca file** (via `read_files`, `grep`, atau `code-searcher`) tanpa `graphify query` terlebih dahulu.
- **JANGAN membaca seluruh folder** (`read_subtree` dengan maxTokens besar) tanpa tahu persis apa yang dicari.
- **JANGAN menggunakan `grep`/`ripgrep` untuk mencari konsep tingkat tinggi** — gunakan `graphify query` yang sudah memahami relasi antar kode.

### Exception

- Jika `graphify-out/graph.json` tidak ada atau rusak, rebuild dulu (lihat bagian bawah) atau fallback ke GitNexus.
- Untuk perubahan kecil/typo yang sudah jelas lokasinya, graphify query bisa dilewati.
- Query spesifik yang butuh implementasi detail (bukan arsitektur) boleh langsung ke file setelah konfirmasi user.

---

## Stack Android

| Layer | Pilihan | Alasan |
|-------|---------|--------|
| **UI Toolkit** | Jetpack Compose | Deklaratif, state-driven, form inspeksi dinamis via LazyColumn |
| **Navigation** | Compose Navigation | NavHost + NavController, integrasi Hilt via `hiltViewModel()` |
| **DI** | Hilt | Compile-time safe, `@HiltWorker` untuk WorkManager, `hiltViewModel()` |
| **Serialization** | Kotlin Serialization | Compile-time, multiplatform, Retrofit converter official |
| **Network** | Retrofit + OkHttp | Standar de facto Android |
| **Image Loading** | Coil | AsyncImage, Coroutine-native, ringan |
| **Background Jobs** | WorkManager | `Network.CONNECTED` constraint, two-step upload berurutan |
| **Camera** | Intent-based | MediaStore.ACTION_IMAGE_CAPTURE — minimal dependency |
| **Token Storage** | Proto DataStore + Google Tink | AEAD + Android Keystore, async (tidak blocking main thread) |
| **Local DB** | Room 3.0+ | KSP only, `@ConstructedBy`, master data + draf inspeksi |

> Lihat ADR-0004 (Jetpack Compose + Modern Stack) untuk detail dan pertimbangan.

---

## Arsitektur Domain

### 4 Domain Contexts

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│   Auth   │────▶│ Inspections  │────▶│   Sync   │
└──────────┘     └──────────────┘     └──────────┘
       │                 │                  │
       └─────────────────┴──────────────────┘
                         ▼
                   ┌──────────┐
                   │   Core   │
                   └──────────┘
```

| Context | Path | Deskripsi |
|---------|------|-----------|
| **Auth** | `app/.../auth/CONTEXT.md` | Login, JWT token, refresh, force logout, AuthState |
| **Inspections** | `app/.../inspections/CONTEXT.md` | Form dinamis, skoring 0/1/2, bukti foto, draf |
| **Sync** | `app/.../sync/CONTEXT.md` | Offline-first, WorkManager, two-step upload, kompresi gambar |
| **Core** | `app/.../core/CONTEXT.md` | Fondasi app, DI, shared models, navigation |

Lihat `CONTEXT-MAP.md` untuk relasi lengkap.

### Pola Kunci

#### Offline-First
- Semua inspeksi → Room lokal sebagai **Draf**.
- WorkManager mengirim data saat `Network.CONNECTED`.
- `local_timestamp` di-generate **saat draf dibuat** (tidak berubah saat retry).

#### Two-Step Upload
1. Kompresi gambar (max 300KB) → upload Multipart → dapatkan nama file.
2. Payload JSON + nama file → endpoint inspeksi.
3. Jika 200 OK → hapus draf dari lokal.

#### UDF (Unidirectional Data Flow)
- `ItemKebersihan` = data murni dari master data (immutable).
- `ItemState` = state UI interaktif (skor, foto, catatan) + computed `isValid`.
- ViewModel = single source of truth.

#### Skoring
| Skor | Label | Wajib Foto? |
|------|-------|-------------|
| 0 | Berisiko | ✅ Minimal 1 foto |
| 1 | Minor Defect | ⚠️ Opsional |
| 2 | Sesuai Standar | ❌ Tidak perlu |

#### Idempotency
- Composite unique key `(room_id, local_timestamp, inspector_id)`.
- `local_timestamp` sebagai business_date di backend.

---

## Agent skills

### Issue tracker
Issues tracked via Beads (CLI: `bd`), stored in `.beads/` as a Dolt-backed AI-native issue tracker. See `docs/agents/issue-tracker.md`.

### Triage labels
Default triage labels: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs
Multi-context layout: `CONTEXT-MAP.md` at root points to per-context `CONTEXT.md` files. See `docs/agents/domain.md`.

---

## Workflow: Issue-Driven Changes

### Always Do

- **WAJIB: Baca `CODING-RULES.md` SEBELUM claim issue atau implementasi kode.**
  - `CODING-RULES.md` berisi YAGNI/KISS/DRY, max 300 baris/file, aturan keamanan Android (token storage, camera, validasi), research workflow (Graphify/GitNexus/Context7), dan checklist pre-commit.
  - File ini **tidak auto-read** oleh AI tools — agent harus membaca manual.
  - Ini adalah **langkah #0** — dilakukan SEBELUM `bd update --claim` atau menulis kode apapun.

- **MUST create a Beads issue before starting work on a new change.**
  - Before modifying any file, run `bd create "<title>" --body "<description>"` to create a tracking issue.
  - The issue title should clearly describe what will be changed and why.
  - The issue body should include:
    - Context: what needs to change and why
    - Files affected (estimated)
    - Dependencies: ADRs, CONTEXT.md terms, or existing issues this relates to
  - After creation, claim the issue: `bd update <issue-id> --claim`

- **Exception: already working on an issue.**
  - If an issue already exists for the work (claimed or assigned), skip creation.
  - If the user explicitly asks to continue without creating an issue, skip creation.

### Never Do

- NEVER claim an issue (`bd update --claim`) without first reading `CODING-RULES.md`.
- NEVER make changes without a corresponding Beads issue, unless the change is trivial (typo, rename) or explicitly requested by the user without one.

---

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **rsud-android-client** (1754 symbols, 3009 relationships, 43 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> Index stale? Run `node .gitnexus/run.cjs analyze` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? `npx gitnexus analyze` (npm 11 crash → `npm i -g gitnexus`; #1939).

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows. For regression review, compare against the default branch: `detect_changes({scope: "compare", base_ref: "main"})`.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `query({search_query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `context({name: "symbolName"})`.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).

## Never Do

- NEVER edit a function, class, or method without first running `impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit changes without running `detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/rsud-android-client/context` | Codebase overview, check index freshness |
| `gitnexus://repo/rsud-android-client/clusters` | All functional areas |
| `gitnexus://repo/rsud-android-client/processes` | All execution flows |
| `gitnexus://repo/rsud-android-client/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->

---

<!-- context7:start -->
# Context7 — Dokumentasi Library/Framework Eksternal

> **Pembagian tugas:** Graphify → memahami **codebase internal** (kode Android). Context7 → dokumentasi **library/framework eksternal** (Jetpack Compose, Room, Hilt, Coil, DataStore, dll).

Context7 memberikan akses ke dokumentasi library/framework terkini melalui **CLI** (`ctx7`). **Selalu gunakan ini dulu sebelum `researcher_web` atau `read_url` untuk dokumentasi teknis.**

## Setup

Context7 sudah dikonfigurasi:
- CLI: `npx ctx7 library` / `npx ctx7 docs` — **primary method**
- API Key: otomatis via environment variable `CONTEXT7_API_KEY`
- Helper script: `./scripts/context7.sh`

> ⚠️ **Context7 MCP (`resolve-library-id` / `query-docs`) tidak digunakan.** Freebuff tidak mendukung auto-connect MCP server. Gunakan ctx7 CLI sebagai gantinya.

## Always Do

- **WAJIB: Gunakan ctx7 CLI** sebelum menulis kode yang melibatkan library/framework Android:
  ```bash
  # Cari library ID
  npx ctx7 library <nama-library> [query]
  
  # Query dokumentasi
  npx ctx7 docs <libraryId> <query>
  
  # Helper script (alternatif)
  ./scripts/context7.sh resolve <nama> [query]
  ./scripts/context7.sh query <libraryId> <query>
  ```
- **WAJIB: Mulai dengan resolve library ID**, lalu query docs per konsep.
  - Contoh:
    ```
    npx ctx7 library Coil "image loading compose Android"
    → /coil-kt/coil (score 78.22)
    
    npx ctx7 docs /coil-kt/coil "AsyncImage API"
    → AsyncImage(model, contentDescription, ...)
    ```
- **WAJIB: Pisahkan query per konsep** — jangan gabung routing + DI + DB dalam satu query.
- **WAJIB: Verifikasi API signatures** — jangan andalkan training data yang mungkin usang.

## Never Do

- **JANGAN gunakan `researcher_web` + `read_url` untuk dokumentasi library** — biaya token 2,000-5,000 vs hanya ~200-500 via Context7 CLI.
- **JANGAN gunakan `skill(\"find-docs\")` sebagai pengganti** — Context7 CLI lebih akurat karena langsung ke dokumentasi resmi.
- **JANGAN coba setup MCP Context7 ulang** — ctx7 CLI adalah method yang sudah diverifikasi berfungsi.

## Perbandingan Biaya Token

| Metode | Token | Akurasi |
|--------|-------|---------|
| ✅ **Context7 CLI** `ctx7 docs` | ~200-500 | ✅ Tinggi |
| ❌ `researcher_web` + `read_url` | ~2,000-5,000+ | ❌ Rendah |
| ⚠️ `skill(\"find-docs\")` | ~1,000-2,000 | ⚠️ Sedang |

## Workflow (Hemat Token)

```bash
npx ctx7 library <nama> [query]          # step 1: cari library ID
  → /org/library (score, snippets)
  
npx ctx7 docs /org/library "konsep"     # step 2: fetch docs per konsep
  → dokumentasi + contoh kode
```

Atau via helper script:
```bash
./scripts/context7.sh resolve Coil "image loading"
./scripts/context7.sh query /coil-kt/coil "AsyncImage API"
```

## Exception

- Jika `CONTEXT7_API_KEY` tidak diset atau ctx7 CLI error, fallback ke `researcher_docs` agent, lalu `read_url` ke developer.android.com.
- Untuk pertanyaan tentang ekosistem (bukan dokumentasi teknis), `researcher_web` lebih cocok.

<!-- context7:end -->

---

<!-- graphify:start -->
# Graphify — Knowledge Graph

This project uses **graphify** to build a navigable knowledge graph from source code. The graph helps agents understand code relationships, detect communities, and trace execution paths.

## Always Do

- **WAJIB: Gunakan `graphify query` sebagai langkah PERTAMA** sebelum membaca file apapun (lihat bagian "Graphify-First Exploration" di atas).
- **WAJIB: Update graph setelah perubahan kode besar** (refactor, module baru, restrukturisasi).
- **WAJIB: Cek index freshness** — pastikan `graphify-out/graph.json` masih relevan dengan kode terbaru.

## Never Do

- **JANGAN membaca file secara buta** tanpa graphify query terlebih dahulu (boros token).
- **JANGAN gunakan grep/ripgrep untuk eksplorasi arsitektur tingkat tinggi** — itu tugas graphify.

## Update Graph

Karena project ini single-module (Gradle), update lebih sederhana dari server-stack:

```bash
# Extract dari source utama (Gradle module)
graphify extract ./app/ --code-only --no-viz

# Copy hasil ke root
cp ./app/graphify-out/graph.json graphify-out/graph.json
```

Jika nanti modular (multi-module ADR-0001), gunakan merge:

```bash
graphify extract ./feature:auth/ --code-only --no-viz
graphify extract ./feature:inspections/ --code-only --no-viz
graphify extract ./core:network/ --code-only --no-viz
graphify extract ./core:datastore/ --code-only --no-viz
graphify merge-graphs .../graph.json .../graph.json --out graphify-out/graph.json
```

## Query Workflow (Hemat Token)

Urutan yang benar untuk memahami kode:

```bash
1. graphify query "Bagaimana arsitektur <fitur>?"
2. graphify path "<KonsepA>" "<KonsepB>"  (jika perlu hubungan)
3. graphify explain "<simbol>"              (jika perlu detail simbol)
4. Baca file spesifik yang disebut graphify  (hanya file yang diperlukan)
```

## Resources

| Task | Command |
|------|---------|
| Full re-index | `graphify extract ./app/src/main/ --code-only --no-viz && cp ./app/src/main/graphify-out/graph.json graphify-out/graph.json` |
| Query merged graph | `graphify query "<question>"` (dari root) |
| Shortest path | `graphify path "<NodeA>" "<NodeB>"` |
| Show god nodes | `graphify gods` |
| Show surprises | `graphify surprises` |
| Merge graphs | `graphify merge-graphs <g1.json> <g2.json> --out <output.json>` |

<!-- graphify:end -->
