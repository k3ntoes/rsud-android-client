# Graph Report - .  (2026-08-07)

## Corpus Check
- 44 files · ~126,886 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1364 nodes · 2006 edges · 100 communities (45 shown, 55 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 293 edges (avg confidence: 0.8)
- Token cost: 24,364 input · 1,688 output

## Community Hubs (Navigation)
- DashboardViewModelTest
- Inspection History & Pagination
- InspectionHistoryRepositoryTest
- AuthViewModel
- SyncManagerTest
- TokenManagerTest
- InspectionHistoryViewModelTest
- DrafDao
- MasterDataDao
- DashboardViewModel
- DraftPhotoCleanupWorker
- graphify_update.py
- AuthRepositoryTest
- MasterDataViewModel
- InspectionFormViewModelTest
- DateUtilsTest
- AppModule
- ApiEndpointIntegrationTest
- InspectionFormViewModel
- InspectionHistoryViewModel
- AuthRepository
- ItemCard
- ApiErrorUtilTest
- DraftPhotoCleanerTest
- AuthApi
- ImageCompressorTest
- ApiResponseSerializationTest
- AppDatabaseMigrationTest.kt
- RoomItemEntity
- CONTEXT-MAP.md
- ItemState
- Panduan Implementasi Android — RSUD Ajibarang Server Stack
- ApiErrorUtil
- Domain Docs
- Claim Order — Dashboard Inspector-Only (ADR-0017)
- MasterDataItem
- UiState
- SyncWorkerTest
- EPIC-4: Auth Login
- AGENTS.md
- LogoutRequest
- Project Structure
- ADR-0011: Single Module Architecture
- gradlew
- app/build.gradle.kts
- ExampleInstrumentedTest
- ExampleUnitTest
- context7.sh
- App Icon (MDPI)
- App Icon Round (XHDPI)
- App Icon (XHDPI)
- build.gradle.kts
- CORE PROMPT
- ADR-0003: Offline-First Inspection Submission Model
- ADR-0004: Jetpack Compose + Modern Android Stack
- ADR-0015: Draft Ownership per Akun
- ADR-0018: Inspection Submit Success and Notes
- Checklist Claim Order — Implementasi Review 2026-08
- Triage Labels
- Android → Backend API Contract
- Spec: Dashboard Inspection Status Cards (Inspector Role)
- Draf Screenshot
- Form Inspeksi Screenshot
- Pilih Ruangan Screenshot
- Riwayat Screenshot
- EPIC-8: Sinkronisasi
- Media Handling & Auth Logic
- PRD Android Client
- settings.gradle.kts
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98
- Community 99

## God Nodes (most connected - your core abstractions)
1. `MasterDataRepositoryTest` - 51 edges
2. `InspectionFormViewModelTest` - 43 edges
3. `InspectionFormViewModelTest` - 43 edges
4. `SyncManagerTest` - 42 edges
5. `MasterDataDao` - 37 edges
6. `InspectionHistoryViewModelTest` - 36 edges
7. `DashboardViewModelTest` - 34 edges
8. `DashboardViewModelTest` - 34 edges
9. `InspectionHistoryRepositoryTest` - 33 edges
10. `InspectionHistoryRepositoryTest` - 33 edges

## Surprising Connections (you probably didn't know these)
- `Phase 6: UI/UX Refresh Inspector` --references--> `DashboardScreen`  [EXTRACTED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE6.md → dashboard/DashboardScreen.kt
- `Phase 6: UI/UX Refresh Inspector` --references--> `DashboardViewModel`  [EXTRACTED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE6.md → dashboard/DashboardViewModel.kt
- `Phase 7-8: Riwayat/Detail Informatif` --references--> `StatusDisplay`  [EXTRACTED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE7-8.md → core/model/StatusDisplay.kt
- `Phase 7-8: Riwayat/Detail Informatif` --references--> `Theme`  [EXTRACTED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE7-8.md → core/ui/theme/Theme.kt
- `ADR-0020: UI Form Redesign Rules` --references--> `ItemState`  [EXTRACTED]
  docs/adr/0020-ui-form-inspection-redesign.md → inspection/ItemState.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **UI Redesign Implementation Flow** — docs_implementation_claim_order_phase6_md, docs_implementation_claim_order_phase7_8_md, docs_implementation_claim_order_phase9_md, docs_ui_form_redesign_checklist_md [EXTRACTED 1.00]
- **Inspection Domain Logic & Validation** — app_src_main_java_my_id_kentoes_rsudajibarangapp_inspection_context_md, docs_adr_0020_ui_form_inspection_redesign_md, inspection_itemstate [EXTRACTED 0.95]

## Communities (100 total, 55 thin omitted)

### Community 0 - "DashboardViewModelTest"
Cohesion: 0.06
Nodes (18): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, PaginatedResult, PhotoDetail, InspectionHistoryCard(), InspectionListScreen(), DetailSubmit (+10 more)

### Community 1 - "Inspection History & Pagination"
Cohesion: 0.05
Nodes (24): DrafDao, MasterDataRepository, DashboardHeader(), DashboardScreen(), UserOut, ProgressTodayCard(), Quadruple, RoomStatusRow() (+16 more)

### Community 2 - "InspectionHistoryRepositoryTest"
Cohesion: 0.07
Nodes (13): ImageCompressor, DraftSummary, DraftWithItems, InspectionPayload, InspectionRepository, Flow, ItemState, MasterDataItem (+5 more)

### Community 3 - "AuthViewModel"
Cohesion: 0.05
Nodes (16): Aead, Serializer, AppDatabase, create(), Context, DrafDao, RoomDatabase, DatabaseModule (+8 more)

### Community 4 - "SyncManagerTest"
Cohesion: 0.05
Nodes (9): Authenticated, AuthRepository, AuthState, Error, StateFlow, UserOut, Loading, Unauthenticated (+1 more)

### Community 5 - "TokenManagerTest"
Cohesion: 0.05
Nodes (9): Authenticated, AuthRepository, AuthState, Error, StateFlow, UserOut, Loading, Unauthenticated (+1 more)

### Community 6 - "InspectionHistoryViewModelTest"
Cohesion: 0.05
Nodes (32): Color, Modifier, StatCard(), DashboardHeader(), DashboardScreen(), UserOut, ProgressTodayCard(), Quadruple (+24 more)

### Community 7 - "DrafDao"
Cohesion: 0.05
Nodes (3): ItemState, InspectionFormViewModelTest, InspectionFormViewModel

### Community 8 - "MasterDataDao"
Cohesion: 0.05
Nodes (3): ItemState, InspectionFormViewModelTest, InspectionFormViewModel

### Community 9 - "DashboardViewModel"
Cohesion: 0.11
Nodes (7): ApiEndpointIntegrationTest, AuthApi, Json, MasterDataApi, MockWebServer, RecordedRequest, ApiEndpointIntegrationTest

### Community 10 - "DraftPhotoCleanupWorker"
Cohesion: 0.09
Nodes (5): DashboardViewModelTest, MasterDataDao, DrafInspeksi, RuangEntity, InspectionRepositoryTest

### Community 11 - "graphify_update.py"
Cohesion: 0.06
Nodes (28): draftStatusColor(), Color, StatusDisplay, toStatusDisplay(), RecentDraftCard(), DaftarDrafScreen(), DraftCard(), AuthViewModel (+20 more)

### Community 12 - "AuthRepositoryTest"
Cohesion: 0.09
Nodes (6): DrafFoto, DrafItem, DrafDao, DrafInspeksi, Flow, DrafDaoTest

### Community 13 - "MasterDataViewModel"
Cohesion: 0.08
Nodes (15): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, InspectionDetailScreen(), Color, PhotoThumbnailCard(), ScoreCountText() (+7 more)

### Community 14 - "InspectionFormViewModelTest"
Cohesion: 0.10
Nodes (6): RuangEntity, StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel, MasterDataViewModelTest

### Community 15 - "DateUtilsTest"
Cohesion: 0.07
Nodes (19): Application, Configuration, CoroutineWorker, ListenableWorker, Result, App, DraftPhotoCleanupWorker, Context (+11 more)

### Community 17 - "ApiEndpointIntegrationTest"
Cohesion: 0.08
Nodes (21): AuthViewModel, ProfileInfoRow(), ProfileScreen(), AuthViewModel, NavGraph(), Routes, ItemCard(), Modifier (+13 more)

### Community 18 - "InspectionFormViewModel"
Cohesion: 0.07
Nodes (17): BottomNavBar(), BottomTab, DASHBOARD, HISTORY, INSPECTION, PROFILE, StateFlow, NetworkConnectivityObserver (+9 more)

### Community 20 - "AuthRepository"
Cohesion: 0.13
Nodes (5): PaginatedResponse, SentPhotoStorage, InspectionListItemDto, InspectionHistoryRepositoryTest, MasterDataDao

### Community 21 - "ItemCard"
Cohesion: 0.17
Nodes (6): MultipartBody, InspectionDetailOutDto, InspectionOutDto, InspectionSubmit, PhotoOutDto, SyncApi

### Community 22 - "ApiErrorUtilTest"
Cohesion: 0.13
Nodes (9): AuthApi, ChangePasswordRequest, SyncResponse, LoginRequest, LogoutRequest, RefreshRequest, TokenResponse, UserOut (+1 more)

### Community 23 - "DraftPhotoCleanerTest"
Cohesion: 0.14
Nodes (10): Authenticator, AuthInterceptor, OkHttpClient, Request, Retrofit, Route, AppModule, Json (+2 more)

### Community 24 - "AuthApi"
Cohesion: 0.13
Nodes (3): InspectionPhotoEntity, RuangEntity, MasterDataDao

### Community 25 - "ImageCompressorTest"
Cohesion: 0.12
Nodes (6): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen(), AuthViewModelTest

### Community 26 - "ApiResponseSerializationTest"
Cohesion: 0.18
Nodes (5): InspectionFormUiState, InspectionFormViewModel, ItemState, StateFlow, ViewModel

### Community 29 - "CONTEXT-MAP.md"
Cohesion: 0.22
Nodes (4): Flow, MasterDataItem, RuangEntity, MasterDataRepository

### Community 30 - "ItemState"
Cohesion: 0.21
Nodes (5): ApiErrorUtil, ApiErrorDto, Response, ApiErrorUtil, ApiErrorDto

### Community 32 - "ApiErrorUtil"
Cohesion: 0.21
Nodes (5): Flow, InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, PhotoDetail

### Community 33 - "Domain Docs"
Cohesion: 0.19
Nodes (3): ImageCompressor, ImageCompressorTest, Context

### Community 37 - "SyncWorkerTest"
Cohesion: 0.29
Nodes (5): DetailSubmit, PhotoSubmit, MasterDataSyncResult, SyncManager, SyncResult

### Community 38 - "EPIC-4: Auth Login"
Cohesion: 0.29
Nodes (5): AppDatabaseMigrationTest, AppDatabaseV7, RoomDatabase, RoomItemEntityV7, RoomItemV7Dao

### Community 39 - "AGENTS.md"
Cohesion: 0.22
Nodes (10): Inspections Glossary, NavGraph, DashboardScreen, DashboardViewModel, ADR-0020: UI Form Redesign Rules, Phase 6: UI/UX Refresh Inspector, Form Inspeksi PRD, UI Form Redesign Checklist (+2 more)

### Community 40 - "LogoutRequest"
Cohesion: 0.27
Nodes (3): SyncResponse, MasterDataApi, RoomItemDto

### Community 42 - "ADR-0011: Single Module Architecture"
Cohesion: 0.25
Nodes (4): DaftarDrafUiState, DaftarDrafViewModel, StateFlow, ViewModel

### Community 46 - "ExampleUnitTest"
Cohesion: 0.29
Nodes (5): BottomTab, DASHBOARD, HISTORY, INSPECTION, PROFILE

### Community 48 - "App Icon (MDPI)"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 50 - "App Icon (XHDPI)"
Cohesion: 0.50
Nodes (3): Interceptor, AuthInterceptor, Response

### Community 51 - "build.gradle.kts"
Cohesion: 0.50
Nodes (3): formatMillisToDate(), parseDateToMillis(), InspectionDatePickerDialog()

### Community 55 - "ADR-0015: Draft Ownership per Akun"
Cohesion: 0.67
Nodes (3): AuthViewModel, ProfileInfoRow(), ProfileScreen()

### Community 58 - "Triage Labels"
Cohesion: 0.50
Nodes (3): ApiResponse, PaginatedResponse, SyncResponse

### Community 59 - "Android → Backend API Contract"
Cohesion: 0.50
Nodes (3): createTempPhotoUri(), Context, Uri

### Community 62 - "Draf Screenshot"
Cohesion: 0.67
Nodes (3): StatusDisplay, Theme, Phase 7-8: Riwayat/Detail Informatif

## Knowledge Gaps
- **53 isolated node(s):** `DrafFoto`, `DrafInspeksi`, `DrafItem`, `InspectionDetailEntity`, `InspectionEntity` (+48 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SyncManagerTest` connect `InspectionHistoryRepositoryTest` to `ApiErrorUtil`, `ItemCard`, `SyncWorkerTest`, `CONTEXT-MAP.md`?**
  _High betweenness centrality (0.178) - this node is a cross-community bridge._
- **Why does `InspectionHistoryViewModelTest` connect `AppModule` to `AuthApi`, `ApiErrorUtil`, `ItemCard`, `TokenManagerTest`?**
  _High betweenness centrality (0.148) - this node is a cross-community bridge._
- **Why does `MasterDataDao` connect `AuthApi` to `AuthViewModel`, `Project Structure`, `context7.sh`, `AppModule`, `InspectionHistoryViewModel`, `ADR-0018: Inspection Submit Success and Notes`, `Checklist Claim Order — Implementasi Review 2026-08`, `Form Inspeksi Screenshot`?**
  _High betweenness centrality (0.131) - this node is a cross-community bridge._
- **What connects `DrafFoto`, `DrafInspeksi`, `DrafItem` to the rest of the system?**
  _53 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `DashboardViewModelTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05683060109289618 - nodes in this community are weakly interconnected._
- **Should `Inspection History & Pagination` be split into smaller, more focused modules?**
  _Cohesion score 0.054354178842782 - nodes in this community are weakly interconnected._
- **Should `InspectionHistoryRepositoryTest` be split into smaller, more focused modules?**
  _Cohesion score 0.07003367003367003 - nodes in this community are weakly interconnected._