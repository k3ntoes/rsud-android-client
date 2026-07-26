# 📋 Implementation Claim Order — RSUD Ajibarang Android Client

> **Status Project:** ✅ EPIC-0 s.d. EPIC-6 selesai — foundation + auth + master data + form inspeksi: build system, DI, navigation, network, database, auth, master data, dynamic form, scoring, camera
> **Stack:** Jetpack Compose · Hilt · Room 3.0+ · Retrofit · Proto DataStore + Tink · WorkManager · Coil

---

## 🗺️ Dependency Graph

```mermaid
flowchart LR
    EPIC0[EPIC-0: Build System] --> EPIC1[EPIC-1: Core DI & Nav]
    EPIC0 --> EPIC2[EPIC-2: Network Layer]
    EPIC0 --> EPIC3[EPIC-3: Database & Token Store]
    
    EPIC1 --> EPIC4[EPIC-4: Auth Login]
    EPIC2 --> EPIC4
    EPIC3 --> EPIC4
    
    EPIC1 --> EPIC5[EPIC-5: Master Data]
    EPIC2 --> EPIC5
    EPIC3 --> EPIC5
    
    EPIC4 --> EPIC6[EPIC-6: Form & Skoring]
    EPIC5 --> EPIC6
    
    EPIC4 --> EPIC7[EPIC-7: Draf & Kirim]
    EPIC6 --> EPIC7
    
    EPIC7 --> EPIC8[EPIC-8: Sinkronisasi]
    EPIC2 --> EPIC8
    EPIC3 --> EPIC8
    
    EPIC8 --> EPIC9[EPIC-9: Refinement]
```

> **Aturan:** Setiap EPIC hanya bisa di-*claim* setelah semua dependensinya selesai.

---

## 📊 Phase Overview

| Phase | Epic | ID | Priority | Depend On | Status | Estimasi |
|-------|------|----|----------|-----------|--------|----------|
| **0: Foundation** | Modern Android Stack | `EPIC-0` | 🔴 KRITIS | — | ✅ **Selesai** | 1-2 session |
| **1: Core** | Core DI & Nav | `EPIC-1` | 🔴 KRITIS | EPIC-0 | ✅ **Selesai** | 1 session |
| **1: Core** | Network Layer | `EPIC-2` | 🔴 KRITIS | EPIC-0 | ✅ **Selesai** | 1 session |
| **1: Core** | Database & Token | `EPIC-3` | 🔴 KRITIS | EPIC-0 | ✅ **Selesai** | 1-2 session |
| **2: Auth** | Login & Token Mgmt | `EPIC-4` | 🔴 KRITIS | EPIC-1,2,3 | ✅ **Selesai** | 1-2 session |
| **3: Inspeksi** | Master Data | `EPIC-5` | 🟠 TINGGI | EPIC-1,2,3 | ✅ **Selesai** | 1 session |
| **3: Inspeksi** | Form & Skoring | `EPIC-6` | 🟠 TINGGI | EPIC-5 | ✅ **Selesai** | 2 session |
| **3: Inspeksi** | Draf & Kirim | `EPIC-7` | 🟠 TINGGI | EPIC-6 | ⬜ Buka | 1-2 session |
| **4: Sinkronisasi** | Upload Worker | `EPIC-8` | 🟠 TINGGI | EPIC-7,2,3 | ⬜ Buka | 1-2 session |
| **5: Poles** | Refinement | `EPIC-9` | 🟢 NORMAL | EPIC-8 | ⬜ Buka | 1-2 session |

---

## ✅ Phase 0: Foundation — Build System & Dependencies

### 🔗 Issue: `EPIC-0` ( `rsud-android-client-dpw` ) — ✅ Selesai

**Objective:** Setup build system dengan modern Android stack (Jetpack Compose, Hilt, Room 3.0+, KSP).

- [x] ✅ Upgrade `gradle/libs.versions.toml` dengan semua dependencies:
  - Jetpack Compose BOM (`2026.06.01`), Compose UI, Material3, Compose Navigation
  - Hilt (`2.60.1`: `hilt-work`, `hilt-navigation-compose`)
  - Room 3.0 (`3.0.0` via KSP, pkg `androidx.room3.*`)
  - Proto DataStore (`1.2.1`) + Tink (`datastore-tink:1.3.0-alpha09`)
  - Retrofit (`3.0.0`) + OkHttp (`5.4.0`) + kotlinx.serialization converter
  - Coil (`2.7.0`, Compose integration)
  - Kotlin Serialization plugin (`1.11.0`)
  - Kotlin Compose compiler plugin (`2.4.10`)
  - KSP plugin (`2.3.10`)
- [x] ✅ Update `build.gradle.kts` root: plugins + classpath
- [x] ✅ Update `app/build.gradle.kts`: apply semua plugin, `buildFeatures { compose = true }`
- [x] ✅ Setup minSdk 24, targetSdk 36, compileSdk 37
- [x] ✅ Konfigurasi Kotlin Compiler Extension (via `kotlin-compose` plugin)
- [x] ✅ Setup `gradle.properties` (parallel build, caching, disallowKotlinSourceSets)
- [x] ✅ Verifikasi compatibility AGP 9 built-in Kotlin + KSP 2.3.10 (disallowKotlinSourceSets=true, Kotlin 2.4.10)
- [x] ✅ Verifikasi project build sukses (`./gradlew :app:assembleDebug`)

> **Setelah selesai:** 🟢 Claim `EPIC-2`, `EPIC-3` (parallel — tidak ada blocking)

---

## ✅ Phase 1: Core Infrastructure

### 🔗 Issue: `EPIC-1` — Core: DI & Navigation ( `rsud-android-client-9m3` ) — ✅ Selesai

**Objective:** App class, Hilt modules, UiState models, NavHost, theme.

- [x] ✅ Buat `App.kt`: `@HiltAndroidApp` + `Configuration.Provider` (HiltWorkerFactory)
- [x] ✅ Buat `core/model/UiState.kt`: `sealed class UiState<T> { Loading, Success, Error }`
- [x] ✅ Buat `core/navigation/NavGraph.kt`: NavHost routing (4 placeholder screens)
- [x] ✅ Buat `core/ui/theme/Theme.kt`: Compose theme (Material3 + dynamic colors)
- [x] ✅ Buat `core/ui/MainActivity.kt`: `@AndroidEntryPoint`, edge-to-edge, setContent
- [x] ✅ Buat `core/ui/components/` base composables: AppButton, AppTextField, AppCard, LoadingIndicator
- [x] ✅ Update AndroidManifest: App class, INTERNET permission, remove default WorkManager initializer, MainActivity

> **Catatan:** DI Modules (`NetworkModule`, `DatabaseModule`, `DataStoreModule`) akan dibuat di EPIC-2 dan EPIC-3 bersama instance yang mereka provide.

**Depends on:** `EPIC-0`

### 🔗 Issue: `EPIC-2` — Core: Network Layer ( `rsud-android-client-5xd` ) — ✅ Selesai

**Objective:** Retrofit + OkHttp interceptor chain dengan auto-refresh token.

- [x] ✅ Setup `OkHttpClient` dengan logging interceptor (debug only)
- [x] ✅ Setup `Retrofit` instance dengan kotlinx.serialization converter
- [x] ✅ Buat `di/NetworkModule.kt`: provide OkHttpClient + Retrofit + Json
- [x] ✅ Buat `network/TokenProvider.kt`: kontrak interface untuk token management
- [x] ✅ Buat `network/AuthInterceptor.kt`: tambah `Authorization: Bearer` header (skip login/refresh)
- [x] ✅ Buat `network/TokenAuthenticator.kt`: auto-refresh 401 → retry (anti race condition)
- [x] ✅ Buat `network/ApiResponse.kt`: base response wrapper `{ success, message, data, errors }`
- [x] ✅ Setup timeouts (15s connect/read/write) + retry policy
- [x] ✅ Setup `BuildConfig` untuk BASE_URL (debug vs release via buildConfigField)

**Depends on:** `EPIC-0`

> **Catatan:** `ApiServices` (interface Retrofit untuk endpoint spesifik) akan dibuat per-module di EPIC masing-masing (AuthApi di EPIC-4, MasterDataApi di EPIC-5, dll). Hanya infrastructure network yang disediakan di EPIC-2.

### 🔗 Issue: `EPIC-3` — Core: Database, Token Storage ( `rsud-android-client-b7p` ) — ✅ Selesai

**Objective:** Room database (master data + draf) + Proto DataStore (token).

- [x] ✅ Setup `AppDatabase.kt`: Room database class (KSP) — 5 entities
- [x] ✅ Buat `di/DatabaseModule.kt`: provide AppDatabase + DAOs
- [x] ✅ Buat `di/DataStoreModule.kt`: provide DataStore + TokenManager
- [x] ✅ Buat entity: `MasterDataItem`, `RuangEntity`
- [x] ✅ Buat entity: `DrafInspeksi` (header), `DrafItem` (line items), `DrafFoto` (path foto lokal)
- [x] ✅ Buat DAOs: `MasterDataDao`, `DrafDao`
- [x] ✅ Buat `TypeConverters.kt` (placeholder — tidak ada entity yang perlu complex type)
- [x] ✅ Buat `TokenManager.kt`: save, read, clear, isLoggedIn (implementasi `TokenProvider`)
- [x] ✅ Setup DataStore Preferences untuk token storage (Tink encryption siap wire nanti)

**Depends on:** `EPIC-0`

---

## ✅ Phase 2: Authentication

### 🔗 Issue: `EPIC-4` — Auth: Login & Token Management ( `rsud-android-client-o0v` ) — ✅ Selesai

**Objective:** Login screen, JWT token management, auto-refresh, force logout.

- [x] ✅ Buat `api/AuthApi.kt`: `POST /login`, `POST /refresh`, `POST /logout` — Retrofit interface
- [x] ✅ Buat `AuthRepository.kt`: login → save token → `AuthState` (StateFlow)
- [x] ✅ Buat `AuthViewModel.kt`: login logic, loading, error, `LoginUiState`
- [x] ✅ Buat `LoginScreen.kt` (Compose): form username + password + tombol login + error snackbar
- [x] ✅ Implementasi `AuthState` (StateFlow): Authenticated / Unauthenticated / Loading
- [x] ✅ Integrasi AuthState ke NavGraph (redirect login ↔ home via LaunchedEffect)
- [x] ✅ Integrasi TokenAuthenticator dengan AuthRepository (auto-refresh 401 → retry)
- [x] ✅ Implementasi Force Logout: clear token + redirect ke login
- [x] ✅ Handle error login: InvalidCredentials, NetworkError, ServerError (via UiState)

**Depends on:** `EPIC-1`, `EPIC-2`, `EPIC-3`

---

## ✅ Phase 3: Inspeksi

### 🔗 Issue: `EPIC-5` — Master Data Download ( `rsud-android-client-v2h` ) — ✅ Selesai

**Objective:** Download & cache daftar item kebersihan dan ruangan ke lokal.

- [x] ✅ Buat `api/MasterDataApi.kt`: `GET /master/items`, `GET /master/rooms` — Retrofit interface + `@Serializable` DTOs
- [x] ✅ Buat `MasterDataRepository.kt`: fetch → cache ke Room → Flow reaktif + `MasterDataSyncState`
- [x] ✅ Buat `MasterDataViewModel.kt`: `MasterDataUiState` (items, rooms, groupedItems), auto-sync saat cache kosong, pull-to-refresh
- [x] ✅ Buat `master/ui/MasterDataListScreen.kt` (Compose): daftar ruangan + `PullToRefreshBox` + loading/empty states + snackbar
- [x] ✅ Integrasi ke `NavGraph.kt`: `INSPECTION_LIST` → `MasterDataListScreen`, navigasi ke form inspeksi dengan roomId & roomName
- [x] ✅ Register `MasterDataApi` di `NetworkModule.kt`
- [x] ✅ Implementasi loading screen saat pertama kali download (isi cache kosong → sync otomatis dari API)

**Depends on:** `EPIC-1`, `EPIC-2`, `EPIC-3`

> **Catatan:** Cache freshness + periodic refresh belum diimplementasi — untuk MVP pull-to-refresh manual sudah cukup. Entity Room (`MasterDataItem`, `RuangEntity`) sudah dibuat di EPIC-3 dan digunakan langsung oleh Repository.

### 🔗 Issue: `EPIC-6` — Dynamic Form, Scoring & Photo ( `rsud-android-client-bcx` ) — ✅ Selesai

**Objective:** Form inspeksi dinamis dengan scoring 0/1/2 dan bukti foto.

- [x] ✅ Buat `ItemState.kt`: skor (-1/0/1/2), fotoPaths (List), catatan, computed `isValid`
- [x] ✅ Buat `InspectionFormViewModel.kt` (AndroidViewModel + Hilt): UDF itemStates map, skor/photo/catatan updates, saveDraft (DRAFT), submit (PENDING_SYNC)
- [x] ✅ Buat `InspectionFormScreen.kt` (Compose + LazyColumn): grouped by kategori, camera permission, TakePicture, progress bar, actions
- [x] ✅ Buat `ItemCard.kt` composable: nama item + deskripsi + ScoreIndicator + foto FlowRow + catatan
- [x] ✅ Buat `ScoreIndicator.kt` composable: 3 FilterChip — Berisiko (merah), Minor (kuning), Sesuai (hijau) + toggle
- [x] ✅ Buat `CameraHelper.kt`: FileProvider URI + photo file management
- [x] ✅ Buat `PhotoThumbnail.kt` composable: Coil AsyncImage + tombol hapus overlay
- [x] ✅ Implementasi validasi: skor 0 → wajib minimal 1 foto (`isValid`)
- [x] ✅ Implementasi re-validasi saat skor berubah (foto tetap, `copy(skor = skor)`)
- [x] ✅ Implementasi multi-foto per item (unlimited via FlowRow)
- [x] ✅ Tombol Simpan Draf (incomplete allowed — skor -1 tetap bisa simpan)
- [x] ✅ Tombol Kirim (semua item harus valid — `submitEnabled = valid == total`)
- [x] ✅ Setup CAMERA permission di AndroidManifest.xml (`required=false`)
- [x] ✅ Setup FileProvider + `res/xml/file_paths.xml`
- [x] ✅ Integrasi ke `NavGraph.kt`: `INSPECTION_FORM` → `InspectionFormScreen`

**Depends on:** `EPIC-5`

> **Catatan:** Camera state bisa loss saat config change (rotation) — untuk MVP acceptable, akan diperbaiki di EPIC-9 Refinement. `saveDraft()` dan `submit()` memiliki duplikasi kode yang bisa diekstrak nanti.

### 🔗 Issue: `EPIC-7` — Draf & Pengiriman ( `rsud-android-client-rrj` )

**Objective:** Simpan draf ke Room, siapkan payload untuk dikirim Sync.

- [ ] **⬜ Belum di-claim** — Buat `InspectionRepository.kt`: simpan draf, load draf, prepare kirim
- [ ] **⬜ Belum di-claim** — Simpan draf: DrafInspeksi + DrafItem + DrafFoto ke Room
- [ ] **⬜ Belum di-claim** — Generate `local_timestamp` (UTC ISO 8601) saat draf dibuat
- [ ] **⬜ Belum di-claim** — Implementasi idempotency key `(room_id, local_timestamp, inspector_id)`
- [ ] **⬜ Belum di-claim** — Load + resume draf yang belum terkirim
- [ ] **⬜ Belum di-claim** — Implementasi preparePengiriman: mapping ItemState → JSON payload
- [ ] **⬜ Belum di-claim** — Hapus draf lokal setelah kirim sukses
- [ ] **⬜ Belum di-claim** — Buat `DaftarDrafScreen.kt`: daftar draf tersimpan
- [ ] **⬜ Belum di-claim** — Implementasi hapus draf manual

**Depends on:** `EPIC-6`

---

## ✅ Phase 4: Synchronization

### 🔗 Issue: `EPIC-8` — Kompresi Gambar & Pengiriman ( `rsud-android-client-etl` )

**Objective:** Kompresi gambar (max 300KB) + WorkManager two-step upload.

- [ ] **⬜ Belum di-claim** — Implementasi `ImageCompressor.kt`: resize + turunkan kualitas hingga ~300KB
- [ ] **⬜ Belum di-claim** — Buat `SyncWorker.kt` (`@HiltWorker`): entry point WorkManager
- [ ] **⬜ Belum di-claim** — Konfigurasi WorkManager: hapus default initializer dari AndroidManifest
- [ ] **⬜ Belum di-claim** — Setup `Configuration.Provider` di App.kt (`HiltWorkerFactory`)
- [ ] **⬜ Belum di-claim** — Set WorkManager constraint: `Network.CONNECTED`
- [ ] **⬜ Belum di-claim** — Step 1: upload foto terkompresi via Multipart → dapatkan nama file
- [ ] **⬜ Belum di-claim** — Step 2: kirim JSON inspeksi + nama file ke endpoint
- [ ] **⬜ Belum di-claim** — Implementasi `SyncManager.kt`: orchestrasi sync flow
- [ ] **⬜ Belum di-claim** — Implementasi retry policy + exponential backoff
- [ ] **⬜ Belum di-claim** — Hapus draf dari Room setelah 200 OK
- [ ] **⬜ Belum di-claim** — Notifikasi hasil sync (success / failure via NotificationManager)
- [ ] **⬜ Belum di-claim** — Handle partial failure (sebagian foto gagal upload)

**Depends on:** `EPIC-7` (data draf), `EPIC-2` (network), `EPIC-3` (token + database)

---

## ✅ Phase 5: Refinement

### 🔗 Issue: `EPIC-9` — Error Handling & Polish ( `rsud-android-client-b72` )

**Objective:** UX polish, error handling, testing, release readiness.

- [ ] **⬜ Belum di-claim** — Global error handler (Snackbar / Dialog)
- [ ] **⬜ Belum di-claim** — Loading state di semua screen (Shimmer / CircularProgressIndicator)
- [ ] **⬜ Belum di-claim** — Empty state di DaftarDrafScreen
- [ ] **⬜ Belum di-claim** — Pull-to-refresh di master data & daftar draf
- [ ] **⬜ Belum di-claim** — Network connectivity observer (ConnectivityManager)
- [ ] **⬜ Belum di-claim** — Offline banner saat tidak ada koneksi
- [ ] **⬜ Belum di-claim** — ConfirmationDialog untuk hapus draf
- [ ] **⬜ Belum di-claim** — Optimasi recomposition (derivedStateOf, keys)
- [ ] **⬜ Belum di-claim** — ProGuard / R8 rules (`rules.keep`)
- [ ] **⬜ Belum di-claim** — Unit test: AuthViewModel, InspectionFormViewModel
- [ ] **⬜ Belum di-claim** — Unit test: InspectionRepository, MasterDataRepository
- [ ] **⬜ Belum di-claim** — Instrumented test: Room DAOs
- [ ] **⬜ Belum di-claim** — WorkManager test: SyncWorker
- [ ] **⬜ Belum di-claim** — Validasi idempotency test
- [ ] **⬜ Belum di-claim** — Final code review: blast radius via GitNexus

**Depends on:** `EPIC-8`

---

## 📝 Cara Claim Issue

Setiap agent yang ingin mengerjakan task WAJIB mengikuti protokol ini:

```bash
# 1. BACA CODING-RULES.md dulu (langkah #0 WAJIB)
# 2. LALU:
bd update <issue-id> --claim          # Claim issue

# 3. Setelah claim, ikuti workflow:
#    a. graphify query untuk pahami arsitektur
#    b. skill("context7-mcp") untuk dokumentasi library
#    c. impact() untuk analisis dampak
#    d. Implementasi kode
#    e. bd update <issue-id> --status closed  # Tandai selesai
```

### Prasyarat Claim

- ✅ Sudah baca `CODING-RULES.md` (wajib! file tidak auto-read)
- ✅ Semua EPIC dependencies sudah selesai (✅ di checklist)
- ✅ Paham vocabulary CONTEXT.md terkait

### Contoh Claim Flow

```bash
# Claim EPIC-0:
bd update rsud-android-client-dpw --claim

# Setelah selesai implementasi:
bd update rsud-android-client-dpw --status closed

# Lanjut claim EPIC-1, EPIC-2, EPIC-3 (parallel):
bd update rsud-android-client-9m3 --claim
bd update rsud-android-client-5xd --claim
bd update rsud-android-client-b7p --claim
```

---

## 🚦 Legend

| Simbol | Arti |
|--------|------|
| 🔴 KRITIS | Blocker untuk semua epic lain |
| 🟠 TINGGI | Core feature, harus selesai untuk MVP |
| 🟢 NORMAL | Bisa ditunda, untuk polish |
| ⬜ | Belum dikerjakan |
| 🔄 | Sedang dikerjakan (claimed) |
| ✅ | Selesai |
