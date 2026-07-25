# RSUD Ajibarang — Quick Reference for AI Agents

> **CLAUDE.md ini dibaca otomatis oleh AI tools tertentu (Claude Code, Cursor via CLAUDE.md).** Ringkasan cepat di bawah. Detail lengkap: `AGENTS.md` dan `CODING-RULES.md`.

---

## 🎯 Prioritas Eksplorasi Codebase (Hemat Token)

Gunakan urutan ini — jangan langsung baca file:

```
➊ BACA CODING-RULES.md              → prinsip desain, aturan file, keamanan (Android)
➋ graphify query "<pertanyaan>"    → pahami arsitektur codebase Android
➌ skill("context7-mcp") → query-docs  → dokumentasi library eksternal (Jetpack, Compose, Room)
➍ impact({target: "symbolName"})   → analisis dampak sebelum edit (GitNexus)
➎ Baca file spesifik               → hanya file yang disebut di atas
```

> ⚠️ **WAJIB BACA `CODING-RULES.md` sebelum claim issue / implement kode!**
> File itu **tidak auto-read** oleh AI tools — agent harus membaca manual.

---

## Stack Android

| Layer | Pilihan | Catatan |
|-------|---------|---------|
| **UI** | Jetpack Compose | Deklaratif, state-driven, LazyColumn untuk form dinamis |
| **Navigation** | Compose Navigation | NavHost + NavController, integrasi Hilt |
| **DI** | Hilt | Compile-time, @HiltWorker untuk WorkManager |
| **Serialization** | Kotlin Serialization | `kotlinx.serialization`, compile-time, multiplatform |
| **Network** | Retrofit + OkHttp | Converter kotlinx.serialization (official) |
| **Image Loading** | Coil | AsyncImage untuk Compose, Coroutine-native |
| **Background Jobs** | WorkManager | Network.CONNECTED, two-step upload |
| **Camera** | Intent-based | MediaStore.ACTION_IMAGE_CAPTURE |
| **Token Storage** | Proto DataStore + Tink | Encrypted tokens (AEAD + Android Keystore) |
| **Local DB** | Room 3.0+ | Master Data + Draf Inspeksi (KSP only) |

---

## 📊 Arsitektur Domain (4 Contexts)

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
| **Auth** | `app/.../auth/` | Login, JWT tokens, refresh, force logout |
| **Inspections** | `app/.../inspections/` | Form dinamis, skoring 0/1/2, bukti foto |
| **Sync** | `app/.../sync/` | Offline-first, WorkManager, two-step upload |
| **Core** | `app/.../core/` | Fondasi, DI, shared models, navigation |

---

## ➊ Graphify — Pahami Codebase Internal

Graphify — knowledge graph dari kode Android. Gunakan sebelum membaca file:

```bash
graphify query "Bagaimana alur inspeksi?"   # ~100-500 token
graphify path "Auth" "Sync"                  # jalur terpendek antar konsep
graphify explain "InspectionViewModel"       # detail simbol/modul
```

- ✅ **WAJIB:** `graphify query` sebelum `read_files`/`grep`/`code-searcher`
- ❌ **JANGAN:** `grep`/`read_subtree` untuk arsitektur tingkat tinggi
- ❌ **JANGAN:** baca file secara buta tanpa tahu yang dicari

> Update graph: `graphify extract ./app/ --code-only --no-viz` (output di `app/graphify-out/graph.json`), lalu copy/symlink ke root `graphify-out/graph.json`.

---

## ➋ Context7 — Dokumentasi Library Eksternal

Untuk **Jetpack Compose, Room, Hilt, Coil, dll**. Jangan pakai `researcher_web`:

```python
skill("context7-mcp")                                       # load skill dulu
resolve-library-id(libraryName: "jetpack-compose", query: "LazyColumn")
query-docs(libraryId: "/android/compose", query: "LazyColumn state hoisting")
```

| ✅ Context7 ~100-500 token | ❌ `researcher_web` + `read_url` ~2,000-5,000+ token |
|------------------------------|--------------------------------------------------------|

- ❌ **JANGAN** pakai `researcher_web`/`read_url` untuk dokumentasi library
- ❌ **JANGAN** gabung multiple konsep dalam satu query
- ⚠️ Jika server down → fallback `researcher_docs`, lalu `read_url` resmi

---

## ➌ GitNexus — Impact Analysis Sebelum Edit

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **rsud-android-client** (530 symbols, 510 relationships, 0 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

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

## ➍ Key Android Patterns

### Offline-First
- Semua inspeksi wajib disimpan ke Room lokal **sebagai Draf** sebelum dikirim.
- WorkManager berjalan saat `Network.CONNECTED` — tidak ada retry manual.

### Two-Step Upload
1. Kompresi gambar → upload foto (Multipart) → dapatkan nama file
2. Kirim JSON inspeksi + nama file → endpoint inspeksi

### Idempotency
- `local_timestamp` (UTC ISO 8601) digenerate **saat draf dibuat** — tidak berubah meskipun retry.
- Composite key `(room_id, local_timestamp, inspector_id)`.

### Skoring
| Skor | Label | Foto Wajib? |
|------|-------|-------------|
| 0 | Berisiko | ✅ Wajib minimal 1 foto |
| 1 | Minor Defect | ⚠️ Opsional |
| 2 | Sesuai Standar | ❌ Tidak perlu |

### UDF (Unidirectional Data Flow)
- `ItemKebersihan` (data murni, tidak berubah) ↔ `ItemState` (skor, foto, catatan, computed `isValid`)
- ViewModel = single source of truth, UI hanya render.

---

## ➎ CODING-RULES.md: WAJIB Dibaca Sebelum Claim / Implement!

`CODING-RULES.md` **tidak auto-read** oleh AI tools. Agent WAJIB membaca file ini **sebelum claim issue atau mengimplementasi kode** karena berisi:

- **YAGNI/KISS/DRY** — prinsip desain utama
- **Max 300 baris per file** — aturan batas ukuran file
- **Aturan keamanan** — validasi input, JWT, token storage
- **Checklist sebelum commit** — testing, blast radius, compliance
- **Research & Context Gathering** — Graphify, GitNexus, Context7 workflow

> **Aturan:** Setiap agent WAJIB membaca `CODING-RULES.md` sebagai langkah pertama sebelum claim issue (`bd update --claim`) atau sebelum memulai implementasi kode.
> Baca: [CODING-RULES.md](./CODING-RULES.md) — jangan skip!

---

## ➏ Issue-Driven Changes

Sebelum modifikasi (kecuali trivial/typo):

```bash
# 1. Baca CODING-RULES.md dulu (langkah ➎)
# 2. Lalu:
bd create "Judul" --body "Deskripsi perubahan"
bd update <issue-id> --claim
```

- ✅ Buat issue untuk perubahan baru (refactor, module, fitur)
- 🔄 Lanjutkan issue yang sudah ada jika relevan
- ⏭️ Skip untuk typo, rename kecil, atau jika user minta langsung

---

## Ringkasan Tools

| Tool | Fungsi | Urutan |
|------|--------|--------|
| **CODING-RULES.md** | **WAJIB baca sebelum claim/implement** | **#0 Sebelum apapun** |
| **Graphify** | Pahami arsitektur codebase | #1 Eksplorasi |
| **Context7** | Dokumentasi library eksternal | #2 Dokumentasi |
| **GitNexus** | Impact analysis sebelum edit | #3 Sebelum edit |
| **Beads** | Issue tracking | Setelah baca rules |

> **Detail lengkap:** `AGENTS.md` (workflow, exception, tabel, Android-specific patterns).

> **WAJIB baca** `CODING-RULES.md` sebagai LANGKAH PERTAMA sebelum claim/implement!

## Agent skills

### Domain docs

Empat konteks dengan `CONTEXT.md` masing-masing:
- Auth → `app/.../auth/CONTEXT.md`
- Inspections → `app/.../inspections/CONTEXT.md`
- Sync → `app/.../sync/CONTEXT.md`
- Core → `app/.../core/CONTEXT.md`

Lihat `CONTEXT-MAP.md` untuk gambaran relasi antar konteks.

### Triage labels

Lima label triase kanonik: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. Lihat `docs/agents/triage-labels.md`.

### Issue tracker

Issues via Beads CLI (`bd`), data di `.beads/`. Lihat `docs/agents/issue-tracker.md`.
