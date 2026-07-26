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
