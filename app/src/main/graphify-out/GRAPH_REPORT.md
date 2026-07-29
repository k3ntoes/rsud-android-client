# Graph Report - app/src/main  (2026-07-29)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 317 nodes · 416 edges · 27 communities (20 shown, 7 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 23 edges (avg confidence: 0.8)
- Token cost: 1,067 input · 208 output

## Graph Freshness
- Built from commit: `e5d5bcc0`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Token Storage and Security
- Authentication Data Layer
- Dashboard and Analytics
- Draft Database Access
- Background Sync Workers
- Network and Dependency Injection
- Local Database Management
- Inspection Form Logic
- Inspection UI Components
- Draft List Management
- Inspection Data Repository
- Remote Sync Service
- Navigation and App Entry
- Authentication UI Logic
- Master Data Management
- Token Authentication Interceptor
- Master Data Repository
- Generic UI States
- Dashboard View Model
- Auth Network Interceptor
- Image Compression Utility
- NetworkConnectivityObserver
- ApiResponse.kt
- StateFlow
- ViewModel
- Modifier
- Flow

## God Nodes (most connected - your core abstractions)
1. `DrafDao` - 16 edges
2. `InspectionFormViewModel` - 16 edges
3. `AuthRepository` - 12 edges
4. `AuthViewModel` - 11 edges
5. `DaftarDrafViewModel` - 10 edges
6. `TokenManager` - 9 edges
7. `DashboardScreen()` - 9 edges
8. `AppDatabase` - 8 edges
9. `DrafInspeksi` - 8 edges
10. `TokenData` - 8 edges

## Surprising Connections (you probably didn't know these)
- `NavGraph()` --calls--> `DashboardScreen()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → java/my/id/kentoes/rsudajibarangapp/dashboard/DashboardScreen.kt
- `NavGraph()` --calls--> `DaftarDrafScreen()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → java/my/id/kentoes/rsudajibarangapp/inspection/ui/DaftarDrafScreen.kt
- `AuthViewModel` --references--> `UserOut`  [EXTRACTED]
  java/my/id/kentoes/rsudajibarangapp/auth/AuthViewModel.kt → java/my/id/kentoes/rsudajibarangapp/auth/api/AuthApi.kt
- `LoginScreen()` --references--> `AuthViewModel`  [EXTRACTED]
  java/my/id/kentoes/rsudajibarangapp/auth/ui/LoginScreen.kt → java/my/id/kentoes/rsudajibarangapp/auth/AuthViewModel.kt
- `RoomCard()` --references--> `RuangEntity`  [EXTRACTED]
  java/my/id/kentoes/rsudajibarangapp/master/ui/MasterDataListScreen.kt → java/my/id/kentoes/rsudajibarangapp/core/database/entity/RuangEntity.kt

## Import Cycles
- None detected.

## Communities (27 total, 7 thin omitted)

### Community 0 - "Token Storage and Security"
Cohesion: 0.09
Nodes (10): Aead, DataStore, UserOut, DatabaseModule, Context, TokenData, TokenDataSerializer, TokenEncryption (+2 more)

### Community 1 - "Authentication Data Layer"
Cohesion: 0.12
Nodes (13): AuthApi, ChangePasswordRequest, LoginRequest, LogoutRequest, RefreshRequest, TokenResponse, Authenticated, AuthRepository (+5 more)

### Community 2 - "Dashboard and Analytics"
Cohesion: 0.13
Nodes (16): Color, DrafInspeksi, ImageVector, StatusDisplay, toStatusDisplay(), AnalyticsApi, IssueFrequencyOut, RoomScoreOut (+8 more)

### Community 3 - "Draft Database Access"
Cohesion: 0.15
Nodes (5): DrafDao, Flow, DrafFoto, DrafInspeksi, DrafItem

### Community 4 - "Background Sync Workers"
Cohesion: 0.11
Nodes (13): Application, Configuration, CoroutineWorker, App, Context, SyncAwareWorkerFactory, enqueue(), Context (+5 more)

### Community 5 - "Network and Dependency Injection"
Cohesion: 0.14
Nodes (9): AuthInterceptor, AppModule, ItemOut, MasterDataApi, RoomOut, Json, OkHttpClient, Retrofit (+1 more)

### Community 6 - "Local Database Management"
Cohesion: 0.13
Nodes (8): AppDatabase, create(), Context, Flow, MasterDataDao, MasterDataItem, RuangEntity, RoomDatabase

### Community 7 - "Inspection Form Logic"
Cohesion: 0.20
Nodes (4): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel

### Community 8 - "Inspection UI Components"
Cohesion: 0.14
Nodes (11): android, ItemCard(), Modifier, Modifier, PhotoThumbnail(), Modifier, ScoreIndicator(), ScoreOption (+3 more)

### Community 9 - "Draft List Management"
Cohesion: 0.15
Nodes (8): DaftarDrafUiState, DaftarDrafViewModel, DraftSummary, StateFlow, ViewModel, DaftarDrafScreen(), DraftCard(), DraftSummary

### Community 10 - "Inspection Data Repository"
Cohesion: 0.20
Nodes (7): DraftSummary, DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, ItemState

### Community 11 - "Remote Sync Service"
Cohesion: 0.22
Nodes (8): DetailSubmit, InspectionSubmit, PhotoSubmit, SyncApi, UploadPhotoResponse, SyncManager, SyncResult, MultipartBody

### Community 12 - "Navigation and App Entry"
Cohesion: 0.17
Nodes (8): AuthViewModel, Bundle, ComponentActivity, NavGraph(), Routes, MainActivity, RsuAppTheme(), NavHostController

### Community 13 - "Authentication UI Logic"
Cohesion: 0.18
Nodes (5): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen()

### Community 14 - "Master Data Management"
Cohesion: 0.24
Nodes (6): StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel, MasterDataListScreen(), RoomCard()

### Community 15 - "Token Authentication Interceptor"
Cohesion: 0.32
Nodes (5): Authenticator, Response, TokenAuthenticator, Request, Route

### Community 16 - "Master Data Repository"
Cohesion: 0.38
Nodes (4): Flow, MasterDataRepository, MasterDataItem, RuangEntity

### Community 17 - "Generic UI States"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 18 - "Dashboard View Model"
Cohesion: 0.40
Nodes (4): DashboardUiState, DashboardViewModel, StateFlow, ViewModel

### Community 19 - "Auth Network Interceptor"
Cohesion: 0.50
Nodes (3): Interceptor, AuthInterceptor, Response

## Knowledge Gaps
- **7 isolated node(s):** `ApiResponse`, `Loading`, `Error`, `ScoreOption`, `Loading` (+2 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserOut` connect `Token Storage and Security` to `Authentication Data Layer`, `Dashboard and Analytics`, `Authentication UI Logic`?**
  _High betweenness centrality (0.425) - this node is a cross-community bridge._
- **Why does `DatabaseModule` connect `Token Storage and Security` to `Local Database Management`?**
  _High betweenness centrality (0.326) - this node is a cross-community bridge._
- **What connects `ApiResponse`, `Loading`, `Error` to the rest of the system?**
  _7 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Token Storage and Security` be split into smaller, more focused modules?**
  _Cohesion score 0.09425287356321839 - nodes in this community are weakly interconnected._
- **Should `Authentication Data Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.11965811965811966 - nodes in this community are weakly interconnected._
- **Should `Dashboard and Analytics` be split into smaller, more focused modules?**
  _Cohesion score 0.1341991341991342 - nodes in this community are weakly interconnected._
- **Should `Draft Database Access` be split into smaller, more focused modules?**
  _Cohesion score 0.1471861471861472 - nodes in this community are weakly interconnected._