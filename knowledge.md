# RSUD Ajibarang — Knowledge (auto-inject)

Prioritas absolut: **Graphify** → **Context7 (CLI)** → **GitNexus** → **baca file**.

```
graphify query "<pertanyaan>"       → pahami codebase (~100 token)
npx ctx7 library/docs "<library>"  → dokumentasi library via CLI (~200 token)
impact({target: "..."})              → analisis dampak
read_files(...)                      → hanya file spesifik
```

❌ `grep`/`researcher_web`/`read_subtree` tanpa graphify/context7 dulu.
❌ Baca file secara buta tanpa tahu yang dicari.

> **Context7 merujuk ke CLI `ctx7`** — bukan MCP. Lihat `AGENTS.md` bagian Context7 untuk usage.
> 
> Detail lengkap: `AGENTS.md` (workflow) → `CLAUDE.md` (quick-ref) → `CODING-RULES.md` (standards). Rebuild graph: lihat `AGENTS.md` bagian Graphify.

## Graphify Update (reusable script)

Jangan tulis snippet `python -c` / file temp untuk update graph — pakai script permanen:

```
./scripts/graphify_update.py --prune-ignored --force   # update inkremental penuh
./scripts/graphify_update.py --code-only                # hanya code, skip semantic docs
./scripts/graphify_update.py --labels-only              # regenerate report+HTML tanpa re-extract
```

- Pipeline: detect → AST → semantic (Gemini, otomatis skip tanpa key) → merge+manifest → cluster+report → labels+HTML → cost → cleanup.
- Jalankan via `"$(cat graphify-out/.graphify_python)" scripts/graphify_update.py` (interpreter graphify).
- `--force` dibutuhkan saat graph menyusut (prune besar yang disengaja) — shrink-guard to_json.
- `.graphifyignore` (root): exclude noise dari graph — `docs/BE/`, `.claude/`, `.agents/`, `.beads/`, `apk/`, test log, HTML generated. Kalau ada rule baru: `--prune-ignored --force` untuk membersihkan node lama.

## Graphify Full Rebuild

Update inkremental tidak menghapus node residu (mis. referensi lintas-repo `backend/`/`web-admin/` yang tersisa dari docs lama). Kalau graph perlu dibangun ulang dari nol:

```
# 1. Full re-scan headless (AST + semantic gemini + dedup + build) — wajib dari repo root
$(cat graphify-out/.graphify_python) -m graphify extract . --backend gemini --force

# 2. Regenerate report + HTML + nama komunitas (langkah yang disarankan extract)
$(cat graphify-out/.graphify_python) -m graphify cluster-only .
```

- Jangan pakai `--out graphify-out` — itu menulis hasil ke `graphify-out/graphify-out/` (nested); default sudah menulis ke `graphify-out/`.
- `cluster-only` menampilkan warning kalau label lama (`.graphify_labels.json`) tidak cocok dengan komunitas baru — label otomatis di-rename oleh hub; jalankan `graphify label .` untuk nama bermakna via LLM.
- Update `graphify-out/cost.json` secara manual setelah `extract` (run extract tidak mencatat cost otomatis).
- Reference: `AGENTS.md` bagian Graphify + `scripts/graphify_update.py` (update inkremental).
