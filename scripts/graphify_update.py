#!/usr/bin/env python3
"""
graphify_update.py — Reusable incremental update untuk knowledge graph.

Membungkus seluruh pipeline `/graphify . --update` menjadi SATU perintah yang
bisa dijalankan ulang kapan saja, tanpa perlu menulis inline python -c atau
file temp (masalah escaping bash yang bikin file temp dihapus lagi).

Pipeline:
  detect_incremental -> AST (code) -> semantic (docs, Gemini bila ada key) ->
  build_merge + manifest -> cluster + report + labels + HTML -> cost + cleanup

Usage:
  python3 scripts/graphify_update.py                 # update inkremental penuh
  python3 scripts/graphify_update.py --code-only     # hanya code (skip semantic docs)
  python3 scripts/graphify_update.py --labels-only   # regenerate report/html dari graph.json (tanpa re-extract)
  python3 scripts/graphify_update.py --prune-ignored # + hapus node yang file-nya kini di-ignore (docs/BE, .agents, dll)

Catatan:
  - Wajib dijalankan dari project root (atau via `scripts/`).
  - Interpreter: otomatis memakai python yang terdaftar di graphify-out/.graphify_python
    (fallback: python3 yang sedang berjalan) — lihat `_resolve_python()`.
  - Semantic docs butuh GEMINI_API_KEY / GOOGLE_API_KEY; tanpa key otomatis
    berperilaku seperti --code-only untuk bagian docs.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "graphify-out"
SPEC = ROOT / ".agents/skills/graphify/references/extraction-spec.md"

# --------------------------------------------------------------------------- helpers


def _resolve_python() -> str:
    """Gunakan interpreter graphify yang terdaftar, kalau ada."""
    marker = OUT / ".graphify_python"
    if marker.exists():
        py = marker.read_text(encoding="utf-8").strip()
        if py and Path(py).exists():
            return py
    return sys.executable


def _run_script(script: str) -> None:
    """Jalankan snippet python dengan interpreter graphify. Raises on failure."""
    result = subprocess.run(
        [_resolve_python(), "-c", script],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    sys.stdout.write(result.stdout)
    sys.stderr.write(result.stderr)
    if result.returncode != 0:
        raise RuntimeError(f"python snippet gagal (exit {result.returncode})")


def _run_step(fn, label: str) -> None:
    print(f"\n== {label} ==")
    fn()


# --------------------------------------------------------------------------- pipeline


def detect_incremental() -> None:
    _run_script(
        """
import json
from pathlib import Path
from graphify.detect import detect_incremental

result = detect_incremental(Path('.'))
new_total = result.get('new_total', 0)
Path('graphify-out/.graphify_incremental.json').write_text(
    json.dumps(result, ensure_ascii=False), encoding='utf-8')
deleted = list(result.get('deleted_files', []))
print(f'new/changed: {new_total}, deleted: {len(deleted)}')
"""
    )


def populate_detect() -> None:
    _run_script(
        """
import json
from pathlib import Path
r = json.loads(Path('graphify-out/.graphify_incremental.json').read_text(encoding='utf-8'))
Path('graphify-out/.graphify_detect.json').write_text(json.dumps({
    'files': r.get('new_files', {}),
    'all_files': r.get('files', {}),
    'total_files': r.get('new_total', 0),
    'total_words': r.get('total_words', 0),
    'skipped_sensitive': r.get('skipped_sensitive', []),
    'needs_graph': True,
}, ensure_ascii=False), encoding='utf-8')
print('detect.json populated')
"""
    )


def ast_extract() -> None:
    _run_script(
        """
import json
from pathlib import Path
from graphify.extract import collect_files, extract

code_files = []
detect = json.loads(Path('graphify-out/.graphify_detect.json').read_text(encoding='utf-8'))
for f in detect.get('files', {}).get('code', []):
    code_files.extend(collect_files(Path(f)) if Path(f).is_dir() else [Path(f)])

if code_files:
    result = extract(code_files, cache_root=Path('.'))
    Path('graphify-out/.graphify_ast.json').write_text(
        json.dumps(result, indent=2, ensure_ascii=False), encoding='utf-8')
    print(f'AST: {len(result["nodes"])} nodes, {len(result["edges"])} edges')
else:
    Path('graphify-out/.graphify_ast.json').write_text(
        json.dumps({'nodes': [], 'edges': [], 'input_tokens': 0, 'output_tokens': 0}),
        encoding='utf-8')
    print('No code files changed - skipped AST')
"""
    )


def semantic_extract(code_only: bool) -> None:
    if code_only:
        print("--code-only: semantic docs di-skip.")
        _run_script(
            """
import json
from pathlib import Path
Path('graphify-out/.graphify_semantic.json').write_text(
    json.dumps({'nodes': [], 'edges': [], 'hyperedges': [], 'input_tokens': 0, 'output_tokens': 0}),
    encoding='utf-8')
print('semantic.json: empty (code-only)')
"""
        )
        return

    has_key = bool(os.environ.get("GEMINI_API_KEY") or os.environ.get("GOOGLE_API_KEY"))
    if not has_key:
        print("Tanpa GEMINI_API_KEY/GOOGLE_API_KEY — semantic docs dilewati (code-only).")
        _run_script(
            """
import json
from pathlib import Path
Path('graphify-out/.graphify_semantic.json').write_text(
    json.dumps({'nodes': [], 'edges': [], 'hyperedges': [], 'input_tokens': 0, 'output_tokens': 0}),
    encoding='utf-8')
print('semantic.json: empty (no API key)')
"""
        )
        return

    # Re-extract hanya docs yang BELUM ada di cache semantic
    _run_script(
        f"""
import json
from pathlib import Path
from graphify.cache import check_semantic_cache

detect = json.loads(Path('graphify-out/.graphify_detect.json').read_text(encoding='utf-8'))
all_files = [f for cat in ('document', 'paper', 'image')
             for f in detect['files'].get(cat, [])]
cached_nodes, cached_edges, cached_hyperedges, uncached = check_semantic_cache(
    all_files, root='.', prompt_file={str(SPEC)!r})

if cached_nodes or cached_edges or cached_hyperedges:
    Path('graphify-out/.graphify_cached.json').write_text(
        json.dumps({{'nodes': cached_nodes, 'edges': cached_edges,
                    'hyperedges': cached_hyperedges}}, ensure_ascii=False), encoding='utf-8')
else:
    Path('graphify-out/.graphify_cached.json').unlink(missing_ok=True)
Path('graphify-out/.graphify_uncached.txt').write_text('\\n'.join(uncached), encoding='utf-8')
print(f'Cache: {{len(all_files) - len(uncached)}} hit, {{len(uncached)}} perlu extract')
"""
    )

    uncached = (OUT / ".graphify_uncached.txt").read_text(encoding="utf-8").splitlines()
    uncached = [l for l in uncached if l.strip()]
    if not uncached:
        # semua docs sudah di cache → merge dari cache
        _run_script(
            """
import json
from pathlib import Path
cached = json.loads(Path('graphify-out/.graphify_cached.json').read_text(encoding='utf-8')) \\
    if Path('graphify-out/.graphify_cached.json').exists() else {'nodes': [], 'edges': [], 'hyperedges': []}
Path('graphify-out/.graphify_semantic.json').write_text(json.dumps({
    'nodes': cached['nodes'], 'edges': cached['edges'], 'hyperedges': cached['hyperedges'],
    'input_tokens': 0, 'output_tokens': 0}, ensure_ascii=False), encoding='utf-8')
print('semantic.json: all from cache')
"""
        )
        return

    # Ada docs baru → ekstraksi via Gemini (extract_corpus_parallel)
    _run_script(
        f"""
import json
from pathlib import Path
from graphify.llm import extract_corpus_parallel

files = [Path(l) for l in Path('graphify-out/.graphify_uncached.txt')
         .read_text(encoding='utf-8').splitlines() if l]
print(f'Extracting {{len(files)}} docs dengan backend=gemini...')
result = extract_corpus_parallel(files, backend='gemini', root=Path('.'), chunk_size=20)
print('nodes:', len(result.get('nodes', [])), 'edges:', len(result.get('edges', [])),
      'hyperedges:', len(result.get('hyperedges', [])))
Path('graphify-out/.graphify_semantic_new.json').write_text(
    json.dumps(result, ensure_ascii=False), encoding='utf-8')
"""
    )

    # Merge cache + hasil baru
    _run_script(
        f"""
import json
from pathlib import Path
from graphify.cache import save_semantic_cache

new = json.loads(Path('graphify-out/.graphify_semantic_new.json').read_text(encoding='utf-8')) \\
    if Path('graphify-out/.graphify_semantic_new.json').exists() else {{'nodes': [], 'edges': [], 'hyperedges': []}}
uncached = [line for line in Path('graphify-out/.graphify_uncached.txt')
            .read_text(encoding='utf-8').splitlines() if line]
saved = save_semantic_cache(new.get('nodes', []), new.get('edges', []),
                            new.get('hyperedges', []), root='.',
                            allowed_source_files=uncached, prompt_file={str(SPEC)!r})
print(f'Cached {{saved}} files')

cached = json.loads(Path('graphify-out/.graphify_cached.json').read_text(encoding='utf-8')) \\
    if Path('graphify-out/.graphify_cached.json').exists() else {{'nodes': [], 'edges': [], 'hyperedges': []}}
all_nodes = cached['nodes'] + new.get('nodes', [])
all_edges = cached['edges'] + new.get('edges', [])
seen = set()
deduped = []
for n in all_nodes:
    if n['id'] not in seen:
        seen.add(n['id'])
        deduped.append(n)
merged = {{
    'nodes': deduped,
    'edges': all_edges,
    'hyperedges': cached.get('hyperedges', []) + new.get('hyperedges', []),
    'input_tokens': new.get('input_tokens', 0),
    'output_tokens': new.get('output_tokens', 0),
}}
Path('graphify-out/.graphify_semantic.json').write_text(
    json.dumps(merged, indent=2, ensure_ascii=False), encoding='utf-8')
print('semantic.json:', len(deduped), 'nodes,', len(all_edges), 'edges')
"""
    )


def merge_extraction(prune_ignored: bool) -> None:
    # Gabungkan AST + semantic
    _run_script(
        """
import json
from pathlib import Path
ast = json.loads(Path('graphify-out/.graphify_ast.json').read_text(encoding='utf-8'))
sem = json.loads(Path('graphify-out/.graphify_semantic.json').read_text(encoding='utf-8'))
seen = {n['id'] for n in ast['nodes']}
merged_nodes = list(ast['nodes'])
for n in sem['nodes']:
    if n['id'] not in seen:
        merged_nodes.append(n)
        seen.add(n['id'])
merged = {
    'nodes': merged_nodes,
    'edges': ast['edges'] + sem['edges'],
    'hyperedges': sem.get('hyperedges', []),
    'input_tokens': sem.get('input_tokens', 0),
    'output_tokens': sem.get('output_tokens', 0),
}
Path('graphify-out/.graphify_extract.json').write_text(
    json.dumps(merged, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Merged: {len(merged_nodes)} nodes, {len(merged["edges"])} edges '
      f'({len(ast["nodes"])} AST + {len(sem["nodes"])} semantic)')
"""
    )

    if prune_ignored:
        print("--prune-ignored: cek file yang kini di-ignore .gitignore/.graphifyignore")
        _run_script(
            """
import json
from pathlib import Path
from graphify.detect import _load_graphifyignore, _is_ignored

patterns = _load_graphifyignore(Path('.'))
root = Path('.').resolve()

# Kandidat = file di manifest + source_file di graph.json (robust walau manifest
# sudah cleared di run sebelumnya — node lama tetap harus ikut di-prune).
candidates = set()
manifest = json.loads(Path('graphify-out/manifest.json').read_text(encoding='utf-8'))
candidates.update(manifest.keys())
graph = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
for n in graph.get('nodes', []):
    sf = (n.get('source_file') or '').strip()
    if sf:
        candidates.add(sf)

ignored_now = sorted(p for p in candidates if _is_ignored(root / p, root, patterns))
if ignored_now:
    inc = json.loads(Path('graphify-out/.graphify_incremental.json').read_text(encoding='utf-8'))
    inc.setdefault('deleted_files', [])
    inc['deleted_files'] = list(dict.fromkeys(inc['deleted_files'] + ignored_now))
    Path('graphify-out/.graphify_incremental.json').write_text(
        json.dumps(inc, ensure_ascii=False), encoding='utf-8')
    print(f'{len(ignored_now)} file kini di-ignore -> ditandai deleted utk prune')
else:
    print('Tidak ada file yang kini di-ignore')
"""
        )

    # Backup + build_merge + manifest
    shutil.copy(OUT / "graph.json", OUT / ".graphify_old.json")
    _run_script(
        """
import json
from pathlib import Path
from graphify.build import build_merge
from graphify.detect import save_manifest
from graphify.cli import _stamped_manifest_files

new_extraction = json.loads(Path('graphify-out/.graphify_extract.json').read_text(encoding='utf-8'))
incremental = json.loads(Path('graphify-out/.graphify_incremental.json').read_text(encoding='utf-8'))
deleted = list(incremental.get('deleted_files', []))
prune = list(deleted) or None

G = build_merge(
    [new_extraction],
    graph_path='graphify-out/graph.json',
    prune_sources=prune,
    root='.',
    directed=False,
)
print(f'Merged: {G.number_of_nodes()} nodes, {G.number_of_edges()} edges')

merged_out = {
    'nodes': [{'id': n, **d} for n, d in G.nodes(data=True)],
    'edges': [
        {**{k: val for k, val in d.items() if k not in ('_src', '_tgt', 'source', 'target')},
         'source': d.get('_src', u), 'target': d.get('_tgt', v)}
        for u, v, d in G.edges(data=True)
    ],
    'hyperedges': list(G.graph.get('hyperedges', [])),
    'input_tokens': new_extraction.get('input_tokens', 0),
    'output_tokens': new_extraction.get('output_tokens', 0),
}
Path('graphify-out/.graphify_extract.json').write_text(
    json.dumps(merged_out, ensure_ascii=False), encoding='utf-8')
print(f'Extraction merged ({len(merged_out["nodes"])} nodes, {len(merged_out["edges"])} edges)')

_manifest_files = _stamped_manifest_files(incremental['files'], new_extraction, Path('.'))
_sem_types = ('document', 'paper', 'image')
_dispatched = {f for t, fl in incremental.get('new_files', {}).items() if t in _sem_types for f in fl}
_stamped = {f for fl in _manifest_files.values() for f in fl}
_cleared = _dispatched - _stamped
_scan = {f for fl in incremental['files'].values() for f in fl}
save_manifest(_manifest_files, root='.', scan_corpus=_scan, clear_semantic=_cleared or None)
print('Manifest saved.')
"""
    )


def build_graph(force: bool = False) -> None:
    _run_script(
        f"""
import json
from graphify.build import build_from_json
from graphify.cluster import cluster, score_all
from graphify.analyze import god_nodes, surprising_connections, suggest_questions
from graphify.report import generate
from graphify.export import to_json
from pathlib import Path

extraction = json.loads(Path('graphify-out/.graphify_extract.json').read_text(encoding='utf-8'))
detection = json.loads(Path('graphify-out/.graphify_detect.json').read_text(encoding='utf-8'))
detection['files'] = detection.get('all_files') or detection['files']
detection['total_files'] = detection.get('total_files') or len(
    [f for fl in detection['files'].values() for f in fl])

G = build_from_json(extraction, root='.', directed=False)
if G.number_of_nodes() == 0:
    print('ERROR: Graph is empty')
    raise SystemExit(1)
communities = cluster(G)
cohesion = score_all(G, communities)
tokens = {{'input': extraction.get('input_tokens', 0), 'output': extraction.get('output_tokens', 0)}}
gods = god_nodes(G)
surprises = surprising_connections(G, communities)
labels = {{cid: 'Community ' + str(cid) for cid in communities}}
questions = suggest_questions(G, communities, labels)

wrote = to_json(G, communities, 'graphify-out/graph.json', force={force})
if not wrote:
    print('ERROR: refused to shrink graph.json (existing has more nodes). '
          'Jalankan ulang dengan --force kalau penyusutan memang disengaja.')
    raise SystemExit(1)
report = generate(G, communities, cohesion, labels, gods, surprises, detection, tokens,
                  '.', suggested_questions=questions)
Path('graphify-out/GRAPH_REPORT.md').write_text(report, encoding='utf-8')
analysis = {{
    'communities': {{str(k): v for k, v in communities.items()}},
    'cohesion': {{str(k): v for k, v in cohesion.items()}},
    'gods': gods, 'surprises': surprises, 'questions': questions,
}}
Path('graphify-out/.graphify_analysis.json').write_text(
    json.dumps(analysis, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Graph: {{G.number_of_nodes()}} nodes, {{G.number_of_edges()}} edges, '
      f'{{len(communities)}} communities')
"""
    )


def label_and_html() -> None:
    _run_script(
        """
import json
from graphify.build import build_from_json
from graphify.cluster import score_all
from graphify.analyze import god_nodes, surprising_connections, suggest_questions
from graphify.report import generate
from graphify.export import to_html
from pathlib import Path

extraction = json.loads(Path('graphify-out/.graphify_extract.json').read_text(encoding='utf-8'))
detection = json.loads(Path('graphify-out/.graphify_detect.json').read_text(encoding='utf-8'))
analysis = json.loads(Path('graphify-out/.graphify_analysis.json').read_text(encoding='utf-8'))

G = build_from_json(extraction, root='.', directed=False)
communities = {int(k): v for k, v in analysis['communities'].items()}
cohesion = {int(k): v for k, v in analysis['cohesion'].items()}
tokens = {'input': extraction.get('input_tokens', 0), 'output': extraction.get('output_tokens', 0)}
labels = {int(k): v for k, v in json.loads(
    Path('graphify-out/.graphify_labels.json').read_text(encoding='utf-8')).items()} \\
    if Path('graphify-out/.graphify_labels.json').exists() else {}
questions = suggest_questions(G, communities, labels)
report = generate(G, communities, cohesion, labels, analysis['gods'],
                  analysis['surprises'], detection, tokens, '.', suggested_questions=questions)
Path('graphify-out/GRAPH_REPORT.md').write_text(report, encoding='utf-8')
to_html(G, communities, 'graphify-out/graph.html', community_labels=labels)
print('Report + HTML regenerated')
"""
    )


def finish() -> None:
    _run_script(
        """
import json
from pathlib import Path
from datetime import datetime, timezone
from graphify.detect import save_manifest

# Manifest final (untuk kasus labels-only yang tidak melewati merge)
if Path('graphify-out/.graphify_detect.json').exists():
    detect = json.loads(Path('graphify-out/.graphify_detect.json').read_text(encoding='utf-8'))
    extract = json.loads(Path('graphify-out/.graphify_extract.json').read_text(encoding='utf-8'))
    from graphify.cli import _stamped_manifest_files
    _corpus = detect.get('all_files') or detect['files']
    _manifest_files = _stamped_manifest_files(_corpus, extract, Path('.'))
    _sem_types = ('document', 'paper', 'image')
    _dispatched = {f for t, fl in detect['files'].items() if t in _sem_types for f in fl}
    _stamped = {f for fl in _manifest_files.values() for f in fl}
    _cleared = _dispatched - _stamped
    _scan = {f for fl in _corpus.values() for f in fl}
    save_manifest(_manifest_files, root='.', scan_corpus=_scan, clear_semantic=_cleared or None)
    print(f'Manifest final saved (cleared {len(_cleared)} semantic stamps)')

    cost_path = Path('graphify-out/cost.json')
    if cost_path.exists():
        cost = json.loads(cost_path.read_text(encoding='utf-8'))
    else:
        cost = {'runs': [], 'total_input_tokens': 0, 'total_output_tokens': 0}
    cost['runs'].append({
        'date': datetime.now(timezone.utc).isoformat(),
        'input_tokens': extract.get('input_tokens', 0),
        'output_tokens': extract.get('output_tokens', 0),
        'files': detect.get('total_files', 0),
    })
    cost['total_input_tokens'] += extract.get('input_tokens', 0)
    cost['total_output_tokens'] += extract.get('output_tokens', 0)
    cost_path.write_text(json.dumps(cost, indent=2, ensure_ascii=False), encoding='utf-8')
    print(f'Cost: this run {extract.get("input_tokens", 0):,}/{extract.get("output_tokens", 0):,} '
          f'| total {cost["total_input_tokens"]:,}/{cost["total_output_tokens"]:,}')
"""
    )


def cleanup() -> None:
    for name in [
        ".graphify_detect.json", ".graphify_extract.json", ".graphify_ast.json",
        ".graphify_semantic.json", ".graphify_analysis.json", ".graphify_incremental.json",
        ".graphify_old.json", ".graphify_cached.json", ".graphify_uncached.txt",
        ".graphify_semantic_new.json",
    ]:
        (OUT / name).unlink(missing_ok=True)
    for p in OUT.glob(".graphify_chunk_*.json"):
        p.unlink(missing_ok=True)
    (OUT / ".needs_update").unlink(missing_ok=True)
    print("Cleanup selesai.")


# --------------------------------------------------------------------------- main


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser(description="Reusable graphify incremental update")
    parser.add_argument("--code-only", action="store_true",
                        help="skip semantic extraction (docs)")
    parser.add_argument("--labels-only", action="store_true",
                        help="regenerate report+HTML dari graph.json (tanpa re-extract)")
    parser.add_argument("--prune-ignored", action="store_true",
                        help="hapus node yang file-nya kini di-ignore (.gitignore/.graphifyignore)")
    parser.add_argument("--force", action="store_true",
                        help="izinkan graph.json menyusut (mis. setelah prune besar yang disengaja)")
    parser.add_argument("--no-cleanup", action="store_true",
                        help="pertahankan file intermediate untuk debugging")
    args = parser.parse_args()


    if not (OUT / "graph.json").exists():
        print("ERROR: graphify-out/graph.json tidak ada. Jalankan full build dulu (/graphify .).")
        sys.exit(1)

    if args.labels_only:
        _run_step(build_graph_from_existing, "labels-only: rebuild graph + labels + HTML")
        _run_step(label_and_html, "labels & HTML")
        if not args.no_cleanup:
            cleanup()
        print("\nSelesai (labels-only).")
        return

    _run_step(detect_incremental, "1/7 detect_incremental")
    _run_step(populate_detect, "2/7 populate detect.json")
    _run_step(ast_extract, "3/7 AST extraction (code)")
    _run_step(lambda: semantic_extract(args.code_only), "4/7 semantic extraction (docs)")
    _run_step(lambda: merge_extraction(args.prune_ignored), "5/7 merge + prune + manifest")
    _run_step(lambda: build_graph(args.force), "6/7 build + cluster + report")
    _run_step(label_and_html, "7/7 labels + HTML")
    _run_step(finish, "manifest final + cost")
    if not args.no_cleanup:
        cleanup()
    print("\nGraph update selesai. Buka graphify-out/graph.html untuk visualisasi.")


def build_graph_from_existing() -> None:
    """labels-only: pakai .graphify_extract.json bila ada, kalau tidak baca graph.json."""
    _run_script(
        """
import json
from pathlib import Path
from graphify.build import build_from_json
from graphify.cluster import cluster, score_all
from graphify.analyze import god_nodes, surprising_connections, suggest_questions
from graphify.report import generate
from graphify.export import to_json

# graph.json adalah node-link; extract.json adalah format extraction. Untuk
# labels-only tanpa re-extract, baca langsung graph.json via node-link.
from networkx.readwrite import json_graph
import networkx as nx
data = json.loads(Path('graphify-out/graph.json').read_text(encoding='utf-8'))
G = json_graph.node_link_graph(data, edges='links', multigraph=False)
communities = {}
for n, d in G.nodes(data=True):
    cid = d.get('community')
    if cid is not None:
        communities.setdefault(int(cid), []).append(n)
communities = {cid: nodes for cid, nodes in communities.items() if nodes}
if not communities:
    communities = cluster(G)
cohesion = score_all(G, communities)
tokens = {'input': 0, 'output': 0}
gods = god_nodes(G)
surprises = surprising_connections(G, communities)
labels = {int(k): v for k, v in json.loads(
    Path('graphify-out/.graphify_labels.json').read_text(encoding='utf-8')).items()} \\
    if Path('graphify-out/.graphify_labels.json').exists() else {}
questions = suggest_questions(G, communities, labels)
detection = {'files': {}, 'total_files': G.number_of_nodes(), 'total_words': 0, 'skipped_sensitive': []}
report = generate(G, communities, cohesion, labels, gods, surprises, detection, tokens,
                  '.', suggested_questions=questions)
Path('graphify-out/GRAPH_REPORT.md').write_text(report, encoding='utf-8')
analysis = {'communities': {str(k): v for k, v in communities.items()},
            'cohesion': {str(k): v for k, v in cohesion.items()},
            'gods': gods, 'surprises': surprises, 'questions': questions}
Path('graphify-out/.graphify_analysis.json').write_text(
    json.dumps(analysis, indent=2, ensure_ascii=False), encoding='utf-8')
print(f'Rebuilt dari graph.json: {G.number_of_nodes()} nodes, {len(communities)} communities')
"""
    )


if __name__ == "__main__":
    main()
