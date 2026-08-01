# Graph Report - app/src/main  (2026-08-01)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 488 nodes · 639 edges · 52 communities (24 shown, 28 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 43 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `09dc6e52`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MasterDataDao
- TokenManager
- NavGraph
- SyncApi.kt
- AuthRepository
- .createWorker
- DrafDao
- AppModule
- InspectionFormViewModel
- InspectionRepository
- InspectionHistoryViewModel
- StatCard
- DashboardViewModel
- DaftarDrafViewModel
- NetworkConnectivityObserver
- ItemCard
- MasterDataViewModel
- MasterDataApi
- SyncStateStore
- ApiErrorUtil
- UiState
- AuthInterceptor
- ImageCompressor
- SentPhotoStorage
- DrafInspeksi
- ApiResponse.kt
- createTempPhotoUri
- DraftPhotoCleaner
- DrafFoto.kt
- DrafItem.kt
- InspectionDetailEntity.kt
- InspectionEntity.kt
- MasterDataItem.kt
- RoomItemEntity.kt
- UserRoomEntity.kt
- android
- AuthViewModel
- CoroutineWorker
- DrafDao
- Flow
- ItemState
- StateFlow
- ViewModel
- Flow
- Response
- Modifier
- DraftSummary
- MasterDataItem
- Result
- RuangEntity
- TokenAuthenticator

## God Nodes (most connected - your core abstractions)
1. `MasterDataDao` - 35 edges
2. `DrafDao` - 22 edges
3. `InspectionFormViewModel` - 16 edges
4. `NavGraph()` - 15 edges
5. `InspectionHistoryViewModel` - 15 edges
6. `MasterDataRepository` - 14 edges
7. `AuthRepository` - 13 edges
8. `AuthViewModel` - 12 edges
9. `InspectionRepository` - 11 edges
10. `MasterDataViewModel` - 10 edges

## Surprising Connections (you probably didn't know these)
- `NavGraph()` --calls--> `DashboardScreen()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → java/my/id/kentoes/rsudajibarangapp/dashboard/DashboardScreen.kt
- `NavGraph()` --calls--> `DaftarDrafScreen()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → java/my/id/kentoes/rsudajibarangapp/inspection/ui/DaftarDrafScreen.kt
- `NavGraph()` --calls--> `InspectionDetailScreen()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → java/my/id/kentoes/rsudajibarangapp/inspection/ui/InspectionDetailScreen.kt
- `InspectionListScreen()` --calls--> `InspectionDateFilterBar()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/inspection/ui/InspectionListScreen.kt → java/my/id/kentoes/rsudajibarangapp/inspection/ui/InspectionDateFilterBar.kt
- `ItemCard()` --calls--> `PhotoThumbnail()`  [INFERRED]
  java/my/id/kentoes/rsudajibarangapp/inspection/components/ItemCard.kt → java/my/id/kentoes/rsudajibarangapp/inspection/components/PhotoThumbnail.kt

## Import Cycles
- None detected.

## Communities (52 total, 28 thin omitted)

### Community 0 - "MasterDataDao"
Cohesion: 0.05
Nodes (12): InspectionDetailEntity, InspectionEntity, Flow, MasterDataItem, MasterDataDao, InspectionPhotoEntity, RuangEntity, Flow (+4 more)

### Community 1 - "TokenManager"
Cohesion: 0.07
Nodes (14): Aead, DataStore, UserOut, AppDatabase, create(), Context, DatabaseModule, Context (+6 more)

### Community 2 - "NavGraph"
Cohesion: 0.07
Nodes (19): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen(), NavGraph(), Routes, InspectionFormScreen() (+11 more)

### Community 3 - "SyncApi.kt"
Cohesion: 0.09
Nodes (20): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, Flow, PaginatedResult, PhotoDetail, DetailSubmit, InspectionDetailOutDto (+12 more)

### Community 4 - "AuthRepository"
Cohesion: 0.10
Nodes (15): AuthApi, ChangePasswordRequest, SyncResponse, LoginRequest, LogoutRequest, RefreshRequest, TokenResponse, UserRoomDto (+7 more)

### Community 5 - ".createWorker"
Cohesion: 0.08
Nodes (18): Application, Configuration, App, DraftPhotoCleanupWorker, Context, CoroutineWorker, Result, schedule() (+10 more)

### Community 6 - "DrafDao"
Cohesion: 0.13
Nodes (5): DrafFoto, DrafInspeksi, DrafItem, DrafDao, Flow

### Community 7 - "AppModule"
Cohesion: 0.14
Nodes (10): Authenticator, AuthInterceptor, AppModule, TokenAuthenticator, Json, OkHttpClient, Request, Response (+2 more)

### Community 8 - "InspectionFormViewModel"
Cohesion: 0.15
Nodes (6): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel, MasterDataItem, ItemState

### Community 9 - "InspectionRepository"
Cohesion: 0.17
Nodes (9): DaftarDrafViewModel, DraftSummary, DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, DaftarDrafScreen() (+1 more)

### Community 10 - "InspectionHistoryViewModel"
Cohesion: 0.17
Nodes (7): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, InspectionDetailScreen(), PhotoThumbnailCard(), Job

### Community 11 - "StatCard"
Cohesion: 0.20
Nodes (8): Color, ImageVector, StatusDisplay, toStatusDisplay(), StatCard(), Modifier, OfflineBanner(), Modifier

### Community 12 - "DashboardViewModel"
Cohesion: 0.24
Nodes (6): DashboardScreen(), SyncStatusBar(), DashboardUiState, DashboardViewModel, StateFlow, ViewModel

### Community 13 - "DaftarDrafViewModel"
Cohesion: 0.20
Nodes (5): DaftarDrafUiState, DaftarDrafViewModel, DraftSummary, StateFlow, ViewModel

### Community 14 - "NetworkConnectivityObserver"
Cohesion: 0.20
Nodes (5): Bundle, ComponentActivity, StateFlow, NetworkConnectivityObserver, MainActivity

### Community 15 - "ItemCard"
Cohesion: 0.20
Nodes (7): ItemCard(), Modifier, Modifier, PhotoThumbnail(), Modifier, ScoreIndicator(), ScoreOption

### Community 16 - "MasterDataViewModel"
Cohesion: 0.27
Nodes (4): StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel

### Community 17 - "MasterDataApi"
Cohesion: 0.36
Nodes (5): ItemOut, SyncResponse, MasterDataApi, RoomItemDto, RoomOut

### Community 19 - "ApiErrorUtil"
Cohesion: 0.38
Nodes (3): ApiErrorUtil, Response, ApiErrorDto

### Community 20 - "UiState"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 21 - "AuthInterceptor"
Cohesion: 0.50
Nodes (3): Interceptor, AuthInterceptor, Response

### Community 25 - "ApiResponse.kt"
Cohesion: 0.50
Nodes (3): ApiResponse, PaginatedResponse, SyncResponse

### Community 26 - "createTempPhotoUri"
Cohesion: 0.50
Nodes (3): createTempPhotoUri(), Context, Uri

## Knowledge Gaps
- **19 isolated node(s):** `DrafFoto`, `DrafItem`, `Loading`, `Error`, `ScoreOption` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **28 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `NavGraph()` connect `NavGraph` to `InspectionRepository`, `InspectionHistoryViewModel`, `DashboardViewModel`, `NetworkConnectivityObserver`?**
  _High betweenness centrality (0.225) - this node is a cross-community bridge._
- **Why does `UserOut` connect `TokenManager` to `NavGraph`, `AuthRepository`, `DashboardViewModel`?**
  _High betweenness centrality (0.151) - this node is a cross-community bridge._
- **Why does `MasterDataDao` connect `MasterDataDao` to `TokenManager`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `NavGraph()` (e.g. with `LoginScreen()` and `DashboardScreen()`) actually correct?**
  _`NavGraph()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DrafFoto`, `DrafItem`, `Loading` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MasterDataDao` be split into smaller, more focused modules?**
  _Cohesion score 0.052600818234950324 - nodes in this community are weakly interconnected._
- **Should `TokenManager` be split into smaller, more focused modules?**
  _Cohesion score 0.07152496626180836 - nodes in this community are weakly interconnected._