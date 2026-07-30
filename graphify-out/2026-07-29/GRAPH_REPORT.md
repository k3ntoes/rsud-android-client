# Graph Report - .  (2026-07-29)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 785 nodes · 1171 edges · 93 communities (33 shown, 60 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 216 edges (avg confidence: 0.8)
- Token cost: 3,534 input · 996 output

## Graph Freshness
- Built from commit: `cd538abe`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Inspection History Repository
- Dashboard UI Components
- Authentication API
- Database Configuration
- Login View Model
- Sync and Draft Management
- Master Data API
- API Integration Tests
- Inspection Form Logic
- Dependency Injection Modules
- Master Data View Model
- Background Sync Worker
- Draft Data Access
- Inspection Form View Model
- Master Data Access
- API Response Serialization
- Camera and UI Helpers
- API Error Handling Tests
- Draft List Screen
- Master Data Repository
- Image Compression Utility
- Draft State Management
- Architecture and Tech Stack
- Inspection Detail Entities
- Project Documentation
- Network Error Utilities
- Room Item Entities
- User Data Entities
- Generic UI State
- Master Data Items
- User Room Entities
- Sync Worker Tests
- Context Map Documentation
- Automation Scripts
- Gradle Wrapper
- Android Instrumented Tests
- Network Connectivity Observer
- Unit Test Examples
- API Response ADRs
- Inspection History ADRs
- API Alignment Documentation
- Product Requirements
- Master Data Sync State
- Core Sync Context
- Context Utility Script
- Android Platform
- Database Module
- Auth Context
- Core Context
- App Launcher Icon
- Media and Auth Logic
- Project Structure
- Offline-First Strategy
- State Management
- State Management
- View Model Pattern
- Android Context
- Reactive Streams
- Reactive Streams
- Android Context
- Data Storage
- JSON Serialization
- Network Response
- Network Response
- Reactive Streams
- Network Response
- Compose UI Modifiers
- State Management
- View Model Pattern
- Compose UI Modifiers
- Compose UI Modifiers
- Compose UI Modifiers
- State Management
- View Model Pattern
- State Management
- View Model Pattern
- State Management
- View Model Pattern
- Reactive Streams
- Compose UI Modifiers
- Reactive Streams
- State Management
- View Model Pattern
- Android Context
- Android Context
- Data Storage
- JSON Serialization
- Android Context
- Upload Strategy
- Unidirectional Data Flow

## God Nodes (most connected - your core abstractions)
1. `InspectionFormViewModelTest` - 36 edges
2. `MasterDataDao` - 33 edges
3. `SyncManagerTest` - 33 edges
4. `ApiEndpointIntegrationTest` - 31 edges
5. `MasterDataRepositoryTest` - 31 edges
6. `InspectionHistoryViewModelTest` - 24 edges
7. `InspectionHistoryRepositoryTest` - 23 edges
8. `DrafInspeksi` - 22 edges
9. `SyncResponse` - 20 edges
10. `MasterDataViewModel` - 20 edges

## Surprising Connections (you probably didn't know these)
- `PRD Android Client` --conceptually_related_to--> `Inspections Context`  [INFERRED]
  docs/02-prd-android.md → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspections/CONTEXT.md
- `Core Prompt` --conceptually_related_to--> `Sync Context`  [INFERRED]
  docs/00-core-prompt.md → app/src/main/java/my/id/kentoes/rsudajibarangapp/sync/CONTEXT.md
- `NavGraph()` --calls--> `DashboardScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/dashboard/DashboardScreen.kt
- `NavGraph()` --calls--> `InspectionFormScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/InspectionFormScreen.kt
- `NavGraph()` --calls--> `DaftarDrafScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/ui/DaftarDrafScreen.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Agent Workflow Tools** — graphify_tool, gitnexus_tool, context7_tool, beads_tool [EXTRACTED 1.00]
- **Android Architectural Patterns** — offline_first_pattern, two_step_upload_pattern, udf_pattern [EXTRACTED 1.00]
- **Domain Contexts** — auth_context, inspections_context, sync_context, core_context [EXTRACTED 1.00]
- **RSUD Ajibarang Domain Documentation** — app_src_main_java_my_id_kentoes_rsudajibarangapp_auth_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_core_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_inspections_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_sync_context [EXTRACTED]
- **Offline-First Sync Flow** — docs_00_core_prompt, docs_02_prd_android, app_src_main_java_my_id_kentoes_rsudajibarangapp_sync_context [INFERRED]
- **Modern Android Stack** — jetpack_compose, hilt, kotlin_serialization, room_3, workmanager [EXTRACTED 1.00]
- **Domain Contexts** — auth_context, inspections_context, sync_context, core_context [EXTRACTED 1.00]

## Communities (93 total, 60 thin omitted)

### Community 0 - "Inspection History Repository"
Cohesion: 0.05
Nodes (18): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, Flow, PaginatedResult, PhotoDetail, DetailSubmit, InspectionDetailOutDto (+10 more)

### Community 1 - "Dashboard UI Components"
Cohesion: 0.06
Nodes (20): DashboardComposablesTest, StatusDisplay, toStatusDisplay(), AnalyticsApi, DashboardDto, IssueFrequencyOut, RoomScoreOut, IssueCard() (+12 more)

### Community 2 - "Authentication API"
Cohesion: 0.06
Nodes (14): AuthApi, ChangePasswordRequest, LogoutRequest, RefreshRequest, TokenResponse, UserOut, Authenticated, AuthRepository (+6 more)

### Community 3 - "Database Configuration"
Cohesion: 0.06
Nodes (15): Aead, AppDatabase, create(), Context, DatabaseModule, Context, DataStore, TokenData (+7 more)

### Community 4 - "Login View Model"
Cohesion: 0.05
Nodes (23): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen(), NavGraph(), Routes, MainActivity (+15 more)

### Community 5 - "Sync and Draft Management"
Cohesion: 0.10
Nodes (9): DraftSummary, DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, UploadPhotoResponse, SyncManagerTest (+1 more)

### Community 6 - "Master Data API"
Cohesion: 0.10
Nodes (7): UserRoomDto, SyncResponse, ItemOut, MasterDataApi, RoomItemDto, RoomOut, MasterDataRepositoryTest

### Community 7 - "API Integration Tests"
Cohesion: 0.17
Nodes (5): LoginRequest, ApiEndpointIntegrationTest, Json, MockWebServer, RecordedRequest

### Community 9 - "Dependency Injection Modules"
Cohesion: 0.11
Nodes (12): AppModule, Json, AuthInterceptor, Response, Response, TokenAuthenticator, Authenticator, Interceptor (+4 more)

### Community 10 - "Master Data View Model"
Cohesion: 0.13
Nodes (5): StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel, MasterDataViewModelTest

### Community 11 - "Background Sync Worker"
Cohesion: 0.11
Nodes (13): App, Context, SyncAwareWorkerFactory, enqueue(), Context, SyncWorker, Application, Configuration (+5 more)

### Community 12 - "Draft Data Access"
Cohesion: 0.17
Nodes (3): DrafDao, DrafFoto, DrafItem

### Community 13 - "Inspection Form View Model"
Cohesion: 0.18
Nodes (4): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel

### Community 14 - "Master Data Access"
Cohesion: 0.18
Nodes (3): Flow, MasterDataDao, InspectionEntity

### Community 15 - "API Response Serialization"
Cohesion: 0.14
Nodes (4): ApiResponse, PaginatedResponse, ApiResponseSerializationTest, TestItem

### Community 16 - "Camera and UI Helpers"
Cohesion: 0.12
Nodes (11): createTempPhotoUri(), Context, ItemCard(), Modifier, Modifier, PhotoThumbnail(), Modifier, ScoreIndicator() (+3 more)

### Community 18 - "Draft List Screen"
Cohesion: 0.15
Nodes (8): DaftarDrafUiState, DaftarDrafViewModel, StateFlow, ViewModel, Modifier, OfflineBanner(), DaftarDrafScreen(), DraftCard()

### Community 19 - "Master Data Repository"
Cohesion: 0.19
Nodes (3): RuangEntity, Flow, MasterDataRepository

### Community 20 - "Image Compression Utility"
Cohesion: 0.19
Nodes (3): ImageCompressor, ImageCompressorTest, Context

### Community 21 - "Draft State Management"
Cohesion: 0.23
Nodes (3): Flow, DrafInspeksi, ItemState

### Community 22 - "Architecture and Tech Stack"
Cohesion: 0.22
Nodes (9): AppModule, DatabaseModule, ADR-0004: Jetpack Compose + Modern Android Stack, ADR-0011: Single Module Architecture, EPIC-RM: Refactor Multi-Module ke Single Module, Hilt, Jetpack Compose, Kotlin Serialization (+1 more)

### Community 24 - "Project Documentation"
Cohesion: 0.39
Nodes (4): Beads Issue Tracker, Context7, GitNexus, Graphify

### Community 25 - "Network Error Utilities"
Cohesion: 0.38
Nodes (3): ApiErrorUtil, Response, ApiErrorDto

### Community 28 - "Generic UI State"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 32 - "Context Map Documentation"
Cohesion: 1.00
Nodes (4): Auth Context, Core Context, Inspections Context, Sync Context

### Community 33 - "Automation Scripts"
Cohesion: 0.83
Nodes (3): capture(), hitl-loop.template.sh script, step()

### Community 34 - "Gradle Wrapper"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 38 - "API Response ADRs"
Cohesion: 0.67
Nodes (3): ADR-0012: Dual Mode Response, PaginatedResponse, SyncResponse

### Community 39 - "Inspection History ADRs"
Cohesion: 0.67
Nodes (3): ADR-0013: Hybrid Inspection History, InspectionEntity, Room 3.0

### Community 40 - "API Alignment Documentation"
Cohesion: 0.67
Nodes (3): Android Implementation Guide, Android to BE API Contract, EPIC-11: API Alignment & New Sync Endpoints

## Knowledge Gaps
- **33 isolated node(s):** `Loading`, `Unauthenticated`, `Error`, `ApiResponse`, `Loading` (+28 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **60 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SyncManagerTest` connect `Sync and Draft Management` to `Inspection History Repository`, `Master Data Repository`, `Draft Data Access`, `Image Compression Utility`?**
  _High betweenness centrality (0.145) - this node is a cross-community bridge._
- **Why does `MasterDataRepositoryTest` connect `Master Data API` to `Room Item Entities`, `Authentication API`, `Master Data Repository`, `Master Data Access`?**
  _High betweenness centrality (0.135) - this node is a cross-community bridge._
- **Why does `MasterDataDao` connect `Master Data Access` to `Inspection History Repository`, `Dashboard UI Components`, `Database Configuration`, `Master Data API`, `Master Data Repository`, `Inspection Detail Entities`, `Room Item Entities`, `User Data Entities`, `Master Data Items`, `User Room Entities`?**
  _High betweenness centrality (0.133) - this node is a cross-community bridge._
- **What connects `Loading`, `Unauthenticated`, `Error` to the rest of the system?**
  _33 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Inspection History Repository` be split into smaller, more focused modules?**
  _Cohesion score 0.050351721584598295 - nodes in this community are weakly interconnected._
- **Should `Dashboard UI Components` be split into smaller, more focused modules?**
  _Cohesion score 0.0602322206095791 - nodes in this community are weakly interconnected._
- **Should `Authentication API` be split into smaller, more focused modules?**
  _Cohesion score 0.05725490196078432 - nodes in this community are weakly interconnected._