#!/usr/bin/env python3
"""
Generator: docs/flow-diagrams.md -> docs/flow-diagrams.html

- Splits markdown into text segments and ```mermaid blocks.
- Renders the whole document in ONE marked call (placeholders for mermaid
  blocks), then adds GitHub-style slug `id`s to headings so TOC anchor links
  work (marked v5+ no longer emits heading ids).
- Inlines the mmdc-rendered SVG per mermaid block (render first:
  `mmdc -i diagram_NN.mmd -o svg/diagram_NN.svg`).
- Wraps in a self-contained HTML document (Google Fonts degrade offline).

Usage:
    1.  Render mermaid blocks to SVG dengan mmdc:
        mmdc -p <puppeteer-config.json> -i diagram_NN.mmd -o svg/diagram_NN.svg
        (konfigurasi --no-sandbox diperlukan di Linux dengan AppArmor userns).
    2.  python3 scripts/generate-flow-diagrams-html.py [SVG_DIR]
"""
import argparse
import itertools
import re
import subprocess
import tempfile
import unicodedata
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
SRC = PROJECT_ROOT / "docs/flow-diagrams.md"
OUT = PROJECT_ROOT / "docs/flow-diagrams.html"

MERMAID_RE = re.compile(r"```mermaid\n(.*?)```", re.DOTALL)
# Placeholder berupa komentar HTML agar marked TIDAK membungkusnya dalam <p>
# (plain text @@DIAGRAM_N@@ akan dirender <p>... </p> → nesting <p><div> invalid).
PLACEHOLDER_RE = re.compile(r"<!--@@DIAGRAM_(\d+)@@-->")


def slugify(text: str) -> str:
    """GitHub-style anchor slug: lowercase, strip punctuation, spaces -> '-'."""
    text = text.replace("&amp;", "&")  # marked escapes '&' in heading content
    text = unicodedata.normalize("NFKC", text).lower()
    text = re.sub(r"[^\w\s-]", "", text, flags=re.UNICODE)
    return text.replace(" ", "-")


def add_heading_ids(html: str) -> str:
    """Inject GitHub-style id attributes into <h1>-<h4> rendered by marked."""
    def repl(m: re.Match) -> str:
        tag = "h" + m.group(1)  # group is just the digit, e.g. "2" -> "h2"
        content = m.group(2)
        plain = re.sub(r"<[^>]+>", "", content)  # strip inline tags for slug
        return f'<{tag} id="{slugify(plain)}">{content}</{tag}>'

    return re.sub(r"<h([1-4])>(.*?)</h\1>", repl, html, flags=re.DOTALL)


def strip_title_h1(html: str) -> str:
    """Hapus <h1> pertama (judul md) — template hero sudah punya h1 sendiri."""
    return re.sub(r"<h1 id=\"[^\"]*\">.*?</h1>", "", html, count=1, flags=re.DOTALL)


def wrap_toc(html: str) -> str:
    """Wrap the '## Daftar Isi' heading + its <ol> in <nav class='toc'>."""
    def repl(m: re.Match) -> str:
        return f'<nav class="toc">{m.group(1)}</nav>'

    return re.sub(
        r'(<h2 id="daftar-isi">.*?</h2>\s*<ol>.*?</ol>)',
        repl, html, flags=re.DOTALL,
    )


# ── CSS (dark theme, self-contained) ─────────────────────────────────────
CSS = r"""
:root {
  --bg: #0f1420;
  --bg-soft: #151c2c;
  --card: #1b2438;
  --border: #2a3550;
  --text: #e6ebf5;
  --text-dim: #9aa7c4;
  --accent: #5b8def;
  --accent-soft: rgba(91, 141, 239, 0.12);
  --code-bg: #101a2e;
  --green: #4caf7d;
  --yellow: #e6b84c;
  --red: #e06666;
}
* { box-sizing: border-box; }
html { scroll-behavior: smooth; }
body {
  margin: 0;
  background: var(--bg);
  color: var(--text);
  font-family: "Inter", "Segoe UI", system-ui, -apple-system, sans-serif;
  line-height: 1.6;
}
.container { max-width: 1080px; margin: 0 auto; padding: 32px 24px 96px; }
header.hero {
  padding: 40px 0 24px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 32px;
}
header.hero h1 { margin: 0 0 8px; font-size: 2rem; letter-spacing: -0.02em; }
header.hero p { margin: 0; color: var(--text-dim); font-size: 1.02rem; }
.badge {
  display: inline-block; margin-top: 12px; padding: 4px 12px;
  border-radius: 999px; background: var(--accent-soft); color: var(--accent);
  border: 1px solid rgba(91,141,239,0.35); font-size: 0.82rem; font-weight: 600;
}
h2 {
  margin: 48px 0 16px; padding-bottom: 10px; border-bottom: 1px solid var(--border);
  font-size: 1.45rem; letter-spacing: -0.01em; scroll-margin-top: 24px;
}
h3 { margin: 32px 0 12px; font-size: 1.15rem; color: var(--text); }
h4 { margin: 24px 0 8px; font-size: 1rem; }
p { margin: 12px 0; }
a { color: var(--accent); text-decoration: none; }
a:hover { text-decoration: underline; }
ul, ol { padding-left: 24px; }
li { margin: 4px 0; }
hr { border: none; border-top: 1px solid var(--border); margin: 40px 0; }
code {
  font-family: "JetBrains Mono", "Fira Code", Consolas, monospace;
  background: var(--code-bg); padding: 2px 6px; border-radius: 4px;
  font-size: 0.88em; color: #c8dcff;
}
pre {
  background: var(--code-bg); padding: 14px 16px; border-radius: 8px;
  overflow-x: auto; border: 1px solid var(--border); font-size: 0.88em;
}
pre code { background: none; padding: 0; color: var(--text); }
blockquote {
  margin: 16px 0; padding: 10px 18px; border-left: 3px solid var(--accent);
  background: var(--bg-soft); border-radius: 0 8px 8px 0; color: var(--text-dim);
}
blockquote p { margin: 4px 0; }
table {
  width: 100%; border-collapse: collapse; margin: 16px 0;
  font-size: 0.92rem; overflow: hidden; border-radius: 8px;
}
th, td {
  border: 1px solid var(--border); padding: 8px 12px; text-align: left;
}
th { background: var(--bg-soft); color: var(--text); font-weight: 600; }
tr:nth-child(even) td { background: rgba(255,255,255,0.015); }

/* ── Diagram cards ────────────────────────────────────────────────────── */
.diagram {
  margin: 20px 0; background: var(--card); border: 1px solid var(--border);
  border-radius: 12px; padding: 20px; overflow-x: auto;
  box-shadow: 0 4px 24px rgba(0,0,0,0.25);
}
.diagram svg { display: block; margin: 0 auto; max-width: 100%; height: auto; }
.diagram-caption {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid var(--border);
  font-size: 0.8rem; color: var(--text-dim); font-weight: 600;
  text-transform: uppercase; letter-spacing: 0.06em;
}
/* Source code toggle */
.diagram-toggle {
  background: var(--bg-soft); border: 1px solid var(--border); color: var(--text-dim);
  border-radius: 6px; padding: 3px 10px; font-size: 0.72rem; cursor: pointer;
  font-family: inherit; letter-spacing: 0.03em;
}
.diagram-toggle:hover { color: var(--accent); border-color: var(--accent); }
.diagram-source {
  display: none; margin-top: 14px; padding-top: 14px; border-top: 1px dashed var(--border);
  font-size: 0.8rem;
}
.diagram-source pre { margin: 0; }
/* TOC */
nav.toc {
  background: var(--bg-soft); border: 1px solid var(--border);
  border-radius: 12px; padding: 18px 22px; margin: 28px 0;
}
nav.toc h2 { margin: 0 0 10px; font-size: 1.05rem; border: none; padding: 0; }
nav.toc ol { margin: 0; padding-left: 20px; columns: 2; column-gap: 40px; }
nav.toc li { break-inside: avoid; font-size: 0.92rem; }
nav.toc a { color: var(--text-dim); }
nav.toc a:hover { color: var(--accent); }
footer { margin-top: 64px; padding-top: 20px; border-top: 1px solid var(--border);
  color: var(--text-dim); font-size: 0.85rem; }
@media (max-width: 720px) { nav.toc ol { columns: 1; } .container { padding: 20px 14px 80px; } }
"""

PAGE_TEMPLATE = """<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Alur Sistem &amp; Diagram — RSUD Ajibarang Android Client</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=JetBrains+Mono&display=swap" rel="stylesheet">
<style>
@@CSS@@
</style>
</head>
<body>
<div class="container">
<header class="hero">
  <h1>Alur Sistem &amp; Diagram</h1>
  <p>RSUD Ajibarang Android Client — dokumentasi alur dengan diagram Mermaid ter-render inline.</p>
  <span class="badge">@@BADGE@@</span>
</header>
@@BODY@@
<footer>
  <p>Dihasilkan dari <code>docs/flow-diagrams.md</code> — klik tombol <em>Source</em> pada setiap
  diagram untuk melihat sintaks Mermaid. Perbarui dengan menjalankan ulang generator setelah
  mengubah dokumen sumber.</p>
</footer>
</div>
<script>
document.addEventListener('click', function (e) {
  var btn = e.target.closest('.diagram-toggle');
  if (!btn) return;
  var src = btn.parentElement.nextElementSibling;
  if (src && src.classList.contains('diagram-source')) {
    var hidden = src.style.display === 'none' || src.style.display === '';
    src.style.display = hidden ? 'block' : 'none';
    btn.textContent = hidden ? 'Sembunyikan' : 'Source';
  }
});
</script>
</body>
</html>
"""


def render_markdown_to_html(text: str) -> str:
    """Render full markdown (with mermaid placeholders) to HTML via marked."""
    with tempfile.TemporaryDirectory(prefix="flow-diagrams-") as tmp:
        work = Path(tmp) / "doc.md"
        work.write_text(text, encoding="utf-8")
        out = Path(tmp) / "doc.html"
        try:
            subprocess.run(
                ["npx", "-y", "marked", "-i", str(work), "-o", str(out)],
                check=True, capture_output=True, timeout=90,
            )
        except subprocess.CalledProcessError as e:
            print(f"ERROR: marked gagal ({e}). Pastikan npx tersedia.")
            raise
        # urutan PENTING: add_heading_ids dulu (wrap_toc butuh id="daftar-isi")
        return strip_title_h1(wrap_toc(add_heading_ids(out.read_text(encoding="utf-8"))))


def build(svg_dir: Path) -> None:
    src_text = SRC.read_text(encoding="utf-8")

    matches = list(MERMAID_RE.finditer(src_text))
    n_diagrams = len(matches)
    print(f"INFO: {n_diagrams} mermaid blocks ditemukan di {SRC.name}")

    # Replace mermaid blocks with placeholders (penomoran sekuensial), simpan source.
    counter = itertools.count(1)
    placeholder_text = MERMAID_RE.sub(
        lambda m: f"<!--@@DIAGRAM_{next(counter)}@@-->", src_text
    )
    html = render_markdown_to_html(placeholder_text)

    # Inline each diagram SVG where its placeholder sits.
    def swap(m: re.Match) -> str:
        idx = int(m.group(1))  # nomor placeholder == urutan diagram (1-based)
        svg_path = svg_dir / f"diagram_{idx:02d}.svg"
        if not svg_path.exists():
            return f'<p style="color:var(--red)">⚠️ Diagram {idx} SVG tidak ditemukan.</p>'
        svg = svg_path.read_text(encoding="utf-8")
        source = matches[idx - 1].group(1).rstrip() if idx <= len(matches) else ""
        return (
            f'<div class="diagram">'
            f'<div class="diagram-caption"><span>Diagram {idx}</span>'
            f'<button class="diagram-toggle" type="button">Source</button></div>'
            f'{svg}'
            f'<div class="diagram-source"><pre><code>{_escape_html("```mermaid")}\n'
            f'{_escape_html(source)}\n'
            f'{_escape_html("```")}</code></pre></div>'
            f'</div>'
        )

    body = PLACEHOLDER_RE.sub(swap, html)

    badge = f"{n_diagrams} diagram • ter-render dengan mermaid-cli (mmdc)"
    page = (
        PAGE_TEMPLATE
        .replace("@@CSS@@", CSS)
        .replace("@@BADGE@@", badge)
        .replace("@@BODY@@", body)
    )
    OUT.write_text(page, encoding="utf-8")
    print(f"OK: {OUT} ({len(page):,} bytes, {n_diagrams} diagrams inline)")


def _escape_html(s: str) -> str:
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate docs/flow-diagrams.html")
    parser.add_argument(
        "svg_dir", nargs="?", type=Path, default=None,
        help="Directory berisi diagram_NN.svg hasil render mmdc (default: "
             "docs/.flow-diagrams-svg atau /tmp/mermaid-html/svg)",
    )
    args = parser.parse_args()

    svg_dir = args.svg_dir
    if svg_dir is None:
        repo_svg = PROJECT_ROOT / "docs" / ".flow-diagrams-svg"
        svg_dir = repo_svg if repo_svg.exists() else Path("/tmp/mermaid-html/svg")
    if not svg_dir.is_dir():
        print(f"ERROR: SVG dir tidak ada: {svg_dir}")
        raise SystemExit(1)

    build(svg_dir)
