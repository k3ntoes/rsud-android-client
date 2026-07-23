# ADR-0004: Jetpack Compose + Modern Android Stack

**Status**: Accepted

Mengadopsi Jetpack Compose sebagai UI toolkit, Compose Navigation, Hilt untuk DI, dan Kotlin Serialization — membentuk fondasi teknis Android modern.

## Context

Proyek masih baru (belum ada kode UI). Keputusan UI toolkit, DI framework, serialization, dan navigation library adalah fondasi yang mempengaruhi seluruh struktur kode ke depan.

Empat pilar yang perlu diputuskan:
1. **UI Toolkit**: Jetpack Compose (deklaratif) vs XML Views (imperatif)
2. **Navigation**: Compose Navigation vs XML NavGraph (Fragment-based)
3. **DI**: Hilt (compile-time) vs Koin (runtime)
4. **Serialization**: kotlinx.serialization vs Moshi

Seluruh pilihan ini saling terkait — Compose Navigation membutuhkan Compose, Hilt terintegrasi native dengan Compose + WorkManager, dan serialization mempengaruhi setup Retrofit.

## Considered Options

### UI Toolkit

| Kriteria | Jetpack Compose | XML Views |
|----------|----------------|-----------|
| Boilerplate | Minimal (state-driven) | Tinggi (ViewBinding/FindViewById) |
| Form dinamis | LazyColumn + State alami | RecyclerView + Adapter |
| Conditional UI | `if (score == 0) CameraButton()` | visibility GONE/VISIBLE |
| Animasi | Built-in, mudah | Animator/Transition XML |
| Maturity (2026) | Stable, produksi | Legacy, deprecated untuk new projects |

### Dependency Injection

| Kriteria | Hilt | Koin |
|----------|------|------|
| Safety | Compile-time | Runtime |
| WorkManager | `@HiltWorker` native | KoinWorker (ekstra setup) |
| ViewModel | `hiltViewModel()` native | `koinViewModel()` (ekstra) |
| Multi-module | Dagger component tree | Unify modules manual |
| Build speed | Lebih lambat (annotation processing) | Lebih cepat |

### Serialization

| Kriteria | kotlinx.serialization | Moshi |
|----------|----------------------|-------|
| Compiler plugin | Ya (no reflection) | Codegen/Reflection |
| Multiplatform | Ya (KMP) | Terbatas |
| Retrofit integration | **`com.squareup.retrofit2:converter-kotlinx-serialization`** (official) | `converter-moshi` |
| Error messages | Standar | Lebih detail |

> ⚠️ **Jangan gunakan** `com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter` — library komunitas ini **deprecated**. Official converter sekarang di `com.squareup.retrofit2:converter-kotlinx-serialization` (dikelola langsung oleh tim Retrofit).

## Decision

**Dipilih: Modern Android Stack** — seluruh pilar berikut bersama:

1. **Jetpack Compose** — UI deklaratif, state-driven, form inspeksi dinamis
2. **Compose Navigation** — NavHost + NavController, diintegrasikan dengan Hilt
3. **Hilt** — compile-time DI, terintegrasi native dengan ViewModel + WorkManager
4. **Kotlin Serialization** — kotlinx.serialization, compile-time, multiplatform-ready
5. **Coil** — image loading library untuk Compose (AsyncImage), Coroutine-native, ringan

### Pelengkap

| Kategori | Pilihan |
|----------|---------|
| Image Loading | **Coil** — AsyncImage untuk Compose, Coroutine-native |
| Network | **Retrofit** + OkHttp |
| Background Jobs | **WorkManager** — Network.CONNECTED constraint, two-step upload |
| Camera | **Intent-based** — MediaStore.ACTION_IMAGE_CAPTURE |
| Token Storage | **Proto DataStore + Google Tink** (ADR-0002) |
| Local DB | **Room 3.0+** — Master Data + Draf Inspeksi (`androidx.room3.*`) |

## Consequences

- Tidak ada XML layouts/Layout Editor — semua UI di Compose
- Build time lebih lambat karena Hilt annotation processing + Compose compiler
- Form inspeksi dinamis (item list + scoring + conditional photo) lebih mudah diimplementasi dengan state Compose
- Hilt memudahkan @HiltWorker untuk Sync (WorkManager) dan hiltViewModel() untuk InspectionFormScreen
- Kotlin Serialization memudahkan parsing API responses dan serialization Proto DataStore
- **Room 3.0** (2026): paket `androidx.room3.*`, **KSP only** (KAPT dihapus), perlu `@ConstructedBy` annotation, dan `@DaoReturnTypeConverter` untuk custom return types
- **HiltWorker setup**: butuh `Configuration.Provider` di Application class + `HiltWorkerFactory` + remove default WorkManager initializer dari AndroidManifest
- Learning curve: tim perlu familiar dengan Compose (state hoisting, recomposition, side effects)
