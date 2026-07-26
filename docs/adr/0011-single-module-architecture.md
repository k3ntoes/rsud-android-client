# ADR-0011: Single Module Architecture

**Status**: Accepted (supersedes ADR-0001)

**Tanggal**: 2026-07-27

## Context

Aplikasi RSUD Ajibarang Android Client memiliki ~40 source files, 1 domain bisnis (inspeksi kebersihan rumah sakit), dan dikerjakan oleh 1 developer. Sejak ADR-0001, project menggunakan multi-module Gradle architecture:

- `:app` — entry point, DI setup, NavHost
- `:feature:auth` — Login, AuthViewModel
- `:feature:inspection` — Form inspeksi, Dashboard, Sync
- `:core:model` — Shared data classes, interfaces
- `:core:network` — Retrofit, OkHttp interceptor
- `:core:datastore` — Room, DataStore + Tink

Setelah MVP selesai (EPIC-0 s.d. EPIC-10), dilakukan evaluasi menyeluruh terhadap biaya dan manfaat multi-module.

## Evaluasi

| Aspek | Kenyataan |
|-------|-----------|
| **Boilerplate** | ~200 lines build config (5 `build.gradle.kts`) untuk ~40 source files — rasio 5:1 |
| **Interface Indirection** | `TokenProvider` + `TokenRefreshHandler` di `:core:model` semata-mata untuk circular dependency antar module. Di single module, tidak perlu. |
| **DI Tersebar** | 6 Hilt modules di 6 lokasi berbeda — bisa dikonsolidasi jadi 2. |
| **Build Time** | Incremental build di project kecil tidak terasa bedanya. |
| **Isolasi** | Tidak ada tim paralel — 1 developer. Isolasi build antar module tidak memberi manfaat. |

## Decision

Kembali ke **single module** (`:app`).

### Yang Berubah

- Semua source files dipindahkan ke `app/src/main/java/`
- Semua `build.gradle.kts` module lain dihapus
- `TokenProvider` dan `TokenRefreshHandler` interface dihapus — inject class konkrit langsung
- 6 Hilt modules dikonsolidasi menjadi 2: `AppModule` dan `DatabaseModule`
- `settings.gradle.kts` hanya include `:app`

### Yang Tetap

- ✅ Package structure per domain (`auth/`, `inspection/`, `sync/`, `core/`)
- ✅ Hilt DI (hanya dikonsolidasi, tidak dihapus)
- ✅ Version catalog `gradle/libs.versions.toml`
- ✅ Semua logika bisnis — **tidak ada perubahan kode**, hanya perpindahan file

## Consequences

### Positif

- Build config lebih sederhana (1 file vs 5 file)
- Setup development lebih cepat (tidak perlu build module dependencies)
- Tidak ada circular dependency — interface indirection bisa dihapus
- DI lebih mudah dipahami — 2 modules vs 6 modules

### Negatif

- Hilangnya isolasi build per fitur — tidak relevan untuk tim 1 dev
- Semua file dalam satu module — potensi file count tinggi di masa depan
- Jika tim bertumbuh, modularisasi perlu dilakukan lagi

## Compared Options

| Opsi | Pro | Kontra |
|------|-----|--------|
| **Single module (dulu ADR-0001 → sekarang dipilih)** | Build config minimal, setup cepat | Tidak ada isolasi |
| **Multi-module (dipilih ADR-0001)** | Isolasi build, ownership jelas | ~200 lines boilerplate untuk 40 files, interface indirection |
| **Hybrid (app + 1 core module)** | Sedikit lebih baik dari multi-module | Masih butuh interface indirection |

## Referensi

- ADR-0001: Multi-module Architecture (superseded)
- `docs/IMPLEMENTATION-CLAIM-ORDER-PHASE2.md`
