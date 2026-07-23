# ADR-0001: Multi-module Architecture

**Status**: Accepted

Mengadopsi multi-module Gradle architecture (`app`, `:feature:auth`, `:core:network`, `:core:datastore`) untuk memisahkan concern sejak awal dan memungkinkan isolasi build serta pengembangan paralel.

## Context

Proyek dimulai sebagai single-module (`:app`). Setelah riset best practice Android 2025-2026, modularisasi per fitur adalah standar yang direkomendasikan Google untuk proyek yang diharapkan bertumbuh. Dengan target module yang jelas sejak awal, kita menghindari refaktor besar di tengah jalan.

## Concrete Module Structure

Setelah sesi domain-modeling, struktur konkretnya:

```
app/                          ← Application entry point, DI setup, NavHost
:feature:auth                 ← LoginScreen, AuthViewModel, TokenStorage
:feature:inspections          ← DashboardScreen, InspectionFormScreen, HistoryScreen
:core:network                 ← Retrofit, OkHttp interceptor, API interfaces, TokenProvider
:core:datastore               ← Proto DataStore + Tink (token), Room database (Master Data + Draf)
:core:model                   ← Shared data classes (mirip backend schemas)
```

### Dependency Graph

```
:app → :feature:auth, :feature:inspections
:feature:auth → :core:network, :core:datastore, :core:model
:feature:inspections → :core:network, :core:datastore, :core:model
:core:network → :core:model
:core:datastore → :core:model
```

## Considered Options

- **Single-module (apps start monolithic)**: Lebih sederhana untuk MVP, modularisasi bisa dilakukan nanti. Tapi research menunjukkan modularisasi dari awal mengurangi utang teknis dan biaya refaktor.
- **Opsi 2 (app + feature:auth + core:network)**: Memisahkan network layer ke module sendiri. Cukup untuk auth, tapi kurang fleksibel untuk fitur Sync dan Inspections yang membutuhkan DataStore sendiri.
- **Opsi 3 (app + feature:auth + feature:inspections + core:network + core:datastore + core:model) — dipilih**: Pemisahan paling bersih. `core:datastore` untuk Room + DataStore-Tink, `core:model` untuk shared DTOs/entities.

## Consequences

- Setup Gradle lebih kompleks di awal (version catalog, module dependencies, Hilt multi-module config).
- Build time lebih cepat untuk perubahan yang hanya menyentuh satu module.
- Memudahkan ownership dan testing per module.
- Module `:core:datastore` berisi Room database + Proto DataStore untuk semua kebutuhan persistence.
- Module `:core:model` menjadi single source of truth untuk DTOs dan entities.
- Hilt multi-module membutuhkan `@InstallIn` dan component registration yang teliti.
- **KSP (Kotlin Symbol Processing)** — Room 3.0+ WAJIB menggunakan KSP (KAPT sudah dihapus). Hilt 2.x+ juga direkomendasikan via KSP. Setup: `id("com.google.devtools.ksp")` di Gradle.
