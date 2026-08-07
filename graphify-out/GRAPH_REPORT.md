# Graph Report - .  (2026-08-07)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1006 nodes · 1644 edges · 70 communities (38 shown, 32 thin omitted)
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 366 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a1c420a2`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

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
- LogoutRequest
- Project Structure
- ADR-0011: Single Module Architecture
- gradlew
- ExampleInstrumentedTest
- ExampleUnitTest
- context7.sh
- App Icon (MDPI)
- App Icon Round (XHDPI)
- App Icon (XHDPI)
- CORE PROMPT
- ADR-0003: Offline-First Inspection Submission Model
- ADR-0004: Jetpack Compose + Modern Android Stack
- ADR-0015: Draft Ownership per Akun
- ADR-0018: Inspection Submit Success and Notes
- Checklist Claim Order — Implementasi Review 2026-08
- Triage Labels
- Android → Backend API Contract
- Spec: Dashboard Inspection Status Cards (Inspector Role)
- Detail Screenshot
- Draf Screenshot
- Form Inspeksi Screenshot
- Pilih Ruangan Screenshot
- Riwayat Screenshot
- EPIC-8: Sinkronisasi
- Media Handling & Auth Logic
- PRD Android Client

## God Nodes (most connected - your core abstractions)
1. `MasterDataRepositoryTest` - 51 edges
2. `InspectionFormViewModelTest` - 43 edges
3. `SyncManagerTest` - 42 edges
4. `MasterDataDao` - 39 edges
5. `SyncResponse` - 36 edges
6. `InspectionHistoryViewModelTest` - 36 edges
7. `InspectionHistoryRepositoryTest` - 33 edges
8. `AuthRepositoryTest` - 30 edges
9. `DashboardViewModelTest` - 30 edges
10. `MasterDataViewModel` - 29 edges

## Surprising Connections (you probably didn't know these)
- `App Icon Round (HDPI)` --participate_in--> `Project Structure`  [INFERRED]
  app/src/main/res/mipmap-hdpi/ic_launcher_round.webp → docs/03-directory-structure.md
- `App Icon (HDPI)` --participate_in--> `Project Structure`  [INFERRED]
  app/src/main/res/mipmap-hdpi/ic_launcher.webp → docs/03-directory-structure.md
- `Implementation Phase 3` --references--> `Core Context`  [INFERRED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE3.md → app/src/main/java/my/id/kentoes/rsudajibarangapp/core/CONTEXT.md
- `Implementation Phase 4` --references--> `Inspections Context`  [INFERRED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE4.md → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/CONTEXT.md
- `Implementation Phase 5` --references--> `Sync Context`  [INFERRED]
  docs/IMPLEMENTATION-CLAIM-ORDER-PHASE5.md → app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/CONTEXT.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Documentation Hierarchy** — claude_md, agents_md, coding_rules_md, knowledge_md [EXTRACTED 1.00]
- **Domain Contexts** — app_src_main_java_my_id_kentoes_rsudajibarangapp_auth_context_md, inspections_context_md, app_src_main_java_my_id_kentoes_rsudajibarangapp_sync_context_md, app_src_main_java_my_id_kentoes_rsudajibarangapp_core_context_md [EXTRACTED 1.00]
- **Implementation Phases** — implementation_phase2_md, implementation_phase3_md, implementation_phase4_md, implementation_phase5_md [EXTRACTED 1.00]
- **Core Infrastructure Epics** — epic_0, epic_1, epic_2, epic_3 [EXTRACTED 1.00]
- **Photo Storage Strategy Evolution** — docs_adr_0014, docs_adr_0016, docs_adr_0018 [INFERRED 0.85]
- **Synchronization Strategy ADRs** — docs_adr_0012, docs_adr_0013, docs_adr_0019 [INFERRED 0.80]
- **System Domain Contexts** — auth_context, inspections_context, sync_context, core_context [EXTRACTED 1.00]
- **ADR-0017 Implementation Tasks** — docs_agents_claim_order_dashboard_inspector_only, adr_0017, docs_tutorial_preview_dashboard [INFERRED 0.80]

## Communities (70 total, 32 thin omitted)

### Community 0 - "DashboardViewModelTest"
Cohesion: 0.05
Nodes (9): DashboardComposablesTest, DrafInspeksi, RuangEntity, RecentDraftCard(), Flow, MasterDataRepository, MasterDataSyncResult, DashboardViewModelTest (+1 more)

### Community 1 - "Inspection History & Pagination"
Cohesion: 0.06
Nodes (9): UserRoomDto, SyncResponse, ItemOut, MasterDataApi, RoomItemDto, RoomOut, SyncState, SyncStateStore (+1 more)

### Community 2 - "InspectionHistoryRepositoryTest"
Cohesion: 0.06
Nodes (15): ApiResponse, PaginatedResponse, InspectionHistoryRepository, DetailSubmit, InspectionDetailOutDto, InspectionListItemDto, InspectionOutDto, InspectionSubmit (+7 more)

### Community 3 - "AuthViewModel"
Cohesion: 0.05
Nodes (23): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen(), NavGraph(), Routes, DaftarDrafUiState (+15 more)

### Community 4 - "SyncManagerTest"
Cohesion: 0.08
Nodes (8): DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, UploadPhotoResponse, SentPhotoStorage, SyncManagerTest

### Community 5 - "TokenManagerTest"
Cohesion: 0.05
Nodes (15): Aead, AppDatabase, create(), Context, RoomDatabase, DatabaseModule, Context, DataStore (+7 more)

### Community 6 - "InspectionHistoryViewModelTest"
Cohesion: 0.11
Nodes (6): InspectionDetailItem, InspectionHistoryItem, Flow, PaginatedResult, PhotoDetail, InspectionHistoryViewModelTest

### Community 7 - "DrafDao"
Cohesion: 0.08
Nodes (5): DrafDao, Flow, DrafFoto, DrafItem, DrafDaoTest

### Community 8 - "MasterDataDao"
Cohesion: 0.08
Nodes (6): Flow, MasterDataDao, InspectionDetailEntity, InspectionEntity, InspectionPhotoEntity, UserRoomEntity

### Community 9 - "DashboardViewModel"
Cohesion: 0.07
Nodes (20): StatusDisplay, toStatusDisplay(), StateFlow, NetworkConnectivityObserver, MainActivity, RsuAppTheme(), Modifier, StatCard() (+12 more)

### Community 10 - "DraftPhotoCleanupWorker"
Cohesion: 0.07
Nodes (19): App, DraftPhotoCleanupWorker, Context, CoroutineWorker, Result, schedule(), Context, SyncAwareWorkerFactory (+11 more)

### Community 11 - "graphify_update.py"
Cohesion: 0.11
Nodes (29): Path, add_heading_ids(), build(), Render full markdown (with mermaid placeholders) to HTML via marked., GitHub-style anchor slug: lowercase, strip punctuation, spaces -> '-'., Inject GitHub-style id attributes into <h1>-<h4> rendered by marked., Hapus <h1> pertama (judul md) — template hero sudah punya h1 sendiri., Wrap the '## Daftar Isi' heading + its <ol> in <nav class='toc'>. (+21 more)

### Community 12 - "AuthRepositoryTest"
Cohesion: 0.08
Nodes (3): TokenResponse, UserOut, AuthRepositoryTest

### Community 13 - "MasterDataViewModel"
Cohesion: 0.11
Nodes (5): StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel, MasterDataViewModelTest

### Community 15 - "DateUtilsTest"
Cohesion: 0.11
Nodes (7): formatMillisToDate(), parseDateToMillis(), InspectionDatePickerDialog(), DateUtilsTest, TimeZone, DateUtilsTimezoneTest, TimeZone

### Community 16 - "AppModule"
Cohesion: 0.11
Nodes (12): AppModule, Json, AuthInterceptor, Response, Response, TokenAuthenticator, Authenticator, Interceptor (+4 more)

### Community 17 - "ApiEndpointIntegrationTest"
Cohesion: 0.20
Nodes (5): LoginRequest, ApiEndpointIntegrationTest, Json, MockWebServer, RecordedRequest

### Community 18 - "InspectionFormViewModel"
Cohesion: 0.18
Nodes (4): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel

### Community 19 - "InspectionHistoryViewModel"
Cohesion: 0.17
Nodes (7): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, InspectionDetailScreen(), PhotoThumbnailCard(), Job

### Community 20 - "AuthRepository"
Cohesion: 0.20
Nodes (7): Authenticated, AuthRepository, AuthState, Error, StateFlow, Loading, Unauthenticated

### Community 21 - "ItemCard"
Cohesion: 0.12
Nodes (11): createTempPhotoUri(), Context, ItemCard(), Modifier, Modifier, PhotoThumbnail(), Modifier, ScoreIndicator() (+3 more)

### Community 24 - "AuthApi"
Cohesion: 0.18
Nodes (3): AuthApi, ChangePasswordRequest, RefreshRequest

### Community 25 - "ImageCompressorTest"
Cohesion: 0.19
Nodes (3): ImageCompressor, ImageCompressorTest, Context

### Community 27 - "AppDatabaseMigrationTest.kt"
Cohesion: 0.29
Nodes (5): AppDatabaseMigrationTest, AppDatabaseV7, RoomDatabase, RoomItemEntityV7, RoomItemV7Dao

### Community 29 - "CONTEXT-MAP.md"
Cohesion: 0.25
Nodes (7): Auth Context, Core Context, Sync Context, Implementation Phase 3, Implementation Phase 4, Implementation Phase 5, Inspections Context

### Community 31 - "Panduan Implementasi Android — RSUD Ajibarang Server Stack"
Cohesion: 0.25
Nodes (8): ADR-0012: Dual Mode Response, ADR-0013: Hybrid Inspection History, ADR-0014: MediaStore Photo Storage, ADR-0016: Dual-Path Photo Storage, ADR-0017: Android Sebagai Klien Inspector-Only, ADR-0019: Urutan Checklist Inspeksi, Panduan Implementasi Android — RSUD Ajibarang Server Stack, Alur Sistem & Diagram — RSUD Ajibarang Android Client

### Community 32 - "ApiErrorUtil"
Cohesion: 0.38
Nodes (3): ApiErrorUtil, Response, ApiErrorDto

### Community 33 - "Domain Docs"
Cohesion: 0.29
Nodes (7): Auth Context, Core Context, Domain Docs, Login Screenshot, Sync Flow Diagram, Inspections Context, Sync Context

### Community 34 - "Claim Order — Dashboard Inspector-Only (ADR-0017)"
Cohesion: 0.33
Nodes (6): ADR-0008: Admin-only Auth Users, ADR-0017: Dashboard Inspector-Only, Beads CLI (bd), Claim Order — Dashboard Inspector-Only (ADR-0017), Issue tracker: Beads, Dashboard Screenshot

### Community 36 - "UiState"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 38 - "EPIC-4: Auth Login"
Cohesion: 0.40
Nodes (5): EPIC-0: Build System, EPIC-1: Core DI & Nav, EPIC-2: Network Layer, EPIC-3: Database & Token Store, EPIC-4: Auth Login

### Community 41 - "Project Structure"
Cohesion: 0.50
Nodes (4): App Icon Round (HDPI), App Icon (HDPI), Project Structure, Implementation Phase 2

### Community 42 - "ADR-0011: Single Module Architecture"
Cohesion: 0.50
Nodes (4): ADR-0001: Multi-module Architecture, ADR-0002: Proto DataStore + Tink for Token Storage, ADR-0011: Single Module Architecture, Implementation Claim Order — RSUD Ajibarang Android Client

### Community 43 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **52 isolated node(s):** `Loading`, `Unauthenticated`, `Error`, `ApiResponse`, `Loading` (+47 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **32 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SyncManagerTest` connect `SyncManagerTest` to `DashboardViewModelTest`, `ImageCompressorTest`, `InspectionHistoryRepositoryTest`, `DrafDao`?**
  _High betweenness centrality (0.145) - this node is a cross-community bridge._
- **Why does `MasterDataDao` connect `MasterDataDao` to `DashboardViewModelTest`, `Inspection History & Pagination`, `InspectionHistoryRepositoryTest`, `MasterDataItem`, `TokenManagerTest`, `InspectionHistoryViewModelTest`, `RoomItemEntity`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `UserOut` connect `AuthRepositoryTest` to `AuthViewModel`, `TokenManagerTest`, `InspectionHistoryViewModelTest`, `DashboardViewModel`, `AuthRepository`, `AuthApi`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **Are the 30 inferred relationships involving `SyncResponse` (e.g. with `.`SyncResponse serializes back correctly`()` and `.`syncFromApi does not insert items when items list is empty`()`) actually correct?**
  _`SyncResponse` has 30 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Loading`, `Unauthenticated`, `Error` to the rest of the system?**
  _52 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `DashboardViewModelTest` be split into smaller, more focused modules?**
  _Cohesion score 0.05201266395296246 - nodes in this community are weakly interconnected._
- **Should `Inspection History & Pagination` be split into smaller, more focused modules?**
  _Cohesion score 0.05920745920745921 - nodes in this community are weakly interconnected._