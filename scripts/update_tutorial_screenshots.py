#!/usr/bin/env python3
"""
Ganti ilustrasi SVG mockup di docs/TUTORIAL-PENGGUNAAN.html dengan screenshot
asli dari emulator Android (docs/screenshots/*.png), lalu sesuaikan teks yang
bertentangan dengan kondisi nyata aplikasi.

Usage: python3 scripts/update_tutorial_screenshots.py
"""
import re
from pathlib import Path

HTML = Path(__file__).resolve().parent.parent / "docs" / "TUTORIAL-PENGGUNAAN.html"
text = HTML.read_text(encoding="utf-8")

# ── 1. Ganti setiap <svg class="phone">…</svg> dengan <img> ──
SVG_MAP = [
    ("Ilustrasi layar Login", "01-login.png", "Layar Login — masukkan username dan password"),
    ("Ilustrasi layar Dashboard", "02-dashboard.png", "Layar Dashboard"),
    ("Ilustrasi layar Profil", "09-profil.png", "Layar Profil"),
    ("Ilustrasi layar Pilih Ruangan", "04-inspeksi.png", "Layar Pilih Ruangan"),
    ("Ilustrasi layar Form Inspeksi", "05-form.png", "Layar Form Inspeksi"),
    ("Ilustrasi layar Draf Tersimpan", "06-draf.png", "Layar Draf Tersimpan"),
    ("Ilustrasi layar Riwayat Inspeksi", "07-riwayat.png", "Layar Riwayat Inspeksi"),
    ("Ilustrasi layar Detail Inspeksi", "08-detail.png", "Layar Detail Inspeksi"),
]

for label, img, alt in SVG_MAP:
    pattern = re.compile(
        r'<svg class="phone"[^>]*aria-label="' + re.escape(label) + r'"[^>]*>.*?</svg>',
        re.DOTALL,
    )
    text, n = pattern.subn(
        f'<img class="phone" src="screenshots/{img}" alt="{alt}">', text
    )
    if n != 1:
        raise SystemExit(f"FAIL: ekspektasi 1 blok SVG '{label}', dapat {n}")

# ── 2. Teks yang disesuaikan dengan kondisi nyata (screenshot) ──
TEXT_FIXES = [
    # Login: akun demo di screenshot adalah "inspector"
    ('(contoh: <span class="kbd">budi.ins</span>)',
     '(contoh: <span class="kbd">inspector</span>)'),
    # Dashboard: angka asli di screenshot (4 ruangan, draf ICU 2/6)
    ('(contoh <span class="kbd">9 dari 12 ruangan · 75%</span>)',
     '(contoh <span class="kbd">1 dari 4 ruangan · 25%</span>)'),
    ('(contoh <span class="kbd">5/8</span>)',
     '(contoh <span class="kbd">2/6</span>)'),
    # Profil: username & role asli
    ('(contoh <span class="kbd">@budi.ins</span>)',
     '(contoh <span class="kbd">@inspector</span>)'),
    ('<b>Role</b> (INSPECTOR)',
     '<b>Role</b> (inspector)'),
    # Pilih Ruangan: layar asli tanpa info lantai; jumlah item 6–10
    ('Setiap kartu menampilkan nama ruangan, lantai, dan jumlah item kebersihan '
     'yang harus diperiksa (misal <span class="kbd">12 item</span>).',
     'Setiap kartu menampilkan nama ruangan dan jumlah item kebersihan yang '
     'harus diperiksa (misal <span class="kbd">10 item</span>).'),
    # Form: progres asli draf ICU
    ('(contoh <span class="kbd">5/6 item valid</span>)',
     '(contoh <span class="kbd">2/6 item valid</span>)'),
]

for old, new in TEXT_FIXES:
    if old not in text:
        raise SystemExit(f"FAIL: teks lama tidak ditemukan: {old[:60]}...")
    text = text.replace(old, new, 1)

# ── 3. Dashboard: tambah showcase kedua (tampilan bawah: status + Aktivitas) ──
SECOND_SHOWCASE = '''            <div class="phone-cap">Layar Dashboard</div>
          </div>
        </div>
      </div>
      <div class="showcase" style="margin-top:26px">
        <div>
          <ol class="steps">
            <li><div class="n">↓</div><div><b>Gulir ke bawah</b><span>Bagian bawah Dashboard menampilkan status tiap ruangan dengan progres pengisiannya, serta bagian <b>Aktivitas Terbaru</b> berisi draf yang baru saja disimpan.</span></div></li>
          </ol>
          <div class="note tip"><b>📌 Ketuk baris ruangan</b>Kartu status langsung bisa diketuk: <b>Mulai</b> untuk ruangan yang belum diperiksa, <b>Lanjutkan</b> untuk draf, dan <b>Lihat Hasil</b> untuk inspeksi yang sudah dikirim.</div>
        </div>
        <div class="phone-wrap">
          <div class="phone-shadow"></div>
          <img class="phone" src="screenshots/03-dashboard-bawah.png" alt="Layar Dashboard bagian bawah — status per ruangan dan Aktivitas Terbaru">
          <div class="phone-cap">Dashboard — status per ruangan &amp; Aktivitas Terbaru</div>
        </div>
      </div>'''
ANCHOR = '''            <div class="phone-cap">Layar Dashboard</div>
          </div>
        </div>
      </div>'''
if ANCHOR not in text:
    raise SystemExit("FAIL: anchor showcase Dashboard tidak ditemukan")
text = text.replace(ANCHOR, SECOND_SHOWCASE, 1)

# ── 4. Footer: sebutkan screenshot emulator ──
text = text.replace(
    "Dokumen dibuat dari kode sumber aplikasi (Kotlin · Jetpack Compose · Room · WorkManager) · Agustus 2026",
    "Dokumen dibuat dari kode sumber aplikasi (Kotlin · Jetpack Compose · Room · WorkManager) dengan tangkapan layar asli dari emulator Android · Agustus 2026",
    1,
)

HTML.write_text(text, encoding="utf-8")
print("OK — TUTORIAL-PENGGUNAAN.html diperbarui")
print(f"  <img> phone: {text.count('class=\"phone\" src=')}")
print(f"  SVG phone tersisa: {text.count('<svg class=\"phone\"')}")
