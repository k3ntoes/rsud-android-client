# Graph Report - rsud-android-client  (2026-07-31)

## Corpus Check
- 228 files · ~136,769 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1489 nodes · 1994 edges · 149 communities (91 shown, 58 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 323 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5479fa5e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Sync Syncmanagertest
- Master Masterdatarepositorytest
- Core Database
- Master Masterdataviewmodeltest
- Inspection Inspectionhistoryrepositorytest
- Inspection UI
- Core Database
- Inspection Inspectionhistoryviewmodeltest
- Core Network
- Inspection Inspectionformviewmodeltest
- Dashboard Dashboardviewmodeltest
- Inspection UI
- Sync Draftphotocleanupworker
- Auth Authrepositorytest
- Inspection Inspectionformviewmodel
- Auth Authrepository
- Inspection Inspectionhistoryviewmodel
- Auth API
- Inspection Draftphotocleanertest
- Inspections
- Auth API
- Core Network
- Docs Be
- Inspection Inspectionformviewmodeltest
- Claude Skills
- Agents Skills
- Dashboard Components
- Docs Be
- Auth API
- App
- Docs Adr
- Agents Skills
- Backend App
- Docs Be
- Agents Skills
- Backend App
- Aead
- Agents Skills
- Android
- App Ic
- App Ic
- Core Database
- Master Masterdatarepository
- Master UI
- App Src
- App Src
- Core Network
- Core Network
- Core Network
- Inspection Inspectionhistoryrepositorytest
- Master Masterdatarepositorytest
- Auth Admin
- Auth
- Auth
- Authinterceptor
- Authviewmodel
- Datastore
- Docs Adr
- Docs Adr
- Docs Agents
- Docs Agents
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Be
- Docs Implementation
- Draftsummary
- Flow
- Inspectionoutdto
- Interceptor
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Java My
- Masterdataitem
- Networkconnectivityobserver
- Okhttpclient
- Retrofit
- Ruangentity
- Serializer
- Syncresponse
- T
- Tokenauthenticator
- Uri
- Userout
- extraction-spec.md
- 00-core-prompt.md
- 01-media-auth.md
- 03-directory-structure.md
- knowledge.md
- context7.sh
- ADR-0003: Offline-First Submission
- Implementation Tracking
- Refactoring Tracker
- ADR-0005: Async ORM Strategy

## God Nodes (most connected - your core abstractions)
1. `MasterDataRepositoryTest` - 47 edges
2. `MasterDataDao` - 43 edges
3. `InspectionFormViewModelTest` - 37 edges
4. `SyncManagerTest` - 37 edges
5. `InspectionHistoryViewModelTest` - 35 edges
6. `ApiEndpointIntegrationTest` - 31 edges
7. `SyncResponse` - 28 edges
8. `InspectionHistoryRepositoryTest` - 28 edges
9. `MasterDataViewModel` - 27 edges
10. `AuthRepositoryTest` - 26 edges

## Surprising Connections (you probably didn't know these)
- `NavGraph()` --calls--> `LoginScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/auth/ui/LoginScreen.kt
- `NavGraph()` --calls--> `DashboardScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/dashboard/DashboardScreen.kt
- `NavGraph()` --calls--> `InspectionFormScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/InspectionFormScreen.kt
- `NavGraph()` --calls--> `DaftarDrafScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/ui/DaftarDrafScreen.kt
- `NavGraph()` --calls--> `InspectionDetailScreen()`  [INFERRED]
  app/src/main/java/my/id/kentoes/rsudajibarangapp/core/navigation/NavGraph.kt → app/src/main/java/my/id/kentoes/rsudajibarangapp/inspection/ui/InspectionDetailScreen.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Domain Contexts** — app_src_main_java_my_id_kentoes_rsudajibarangapp_auth_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_inspections_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_sync_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_core_context [EXTRACTED 1.00]
- **User Roles** — auth_petugas, auth_supervisor, auth_admin_ppi [EXTRACTED 1.00]
- **Android Data Synchronization Strategy** — docs_adr_0012_dual_mode_response, docs_adr_0013_hybrid_inspection_history, docs_adr_0015_draft_ownership_per_account [EXTRACTED 0.90]
- **Main Flow Skills** — agents_skills_ask_matt_skill_md, agents_skills_tdd_skill_md, agents_skills_code_review_skill_md [EXTRACTED 0.90]
- **RSUD Ajibarang Inspection System** — app_src_main_java_my_id_kentoes_rsudajibarangapp_inspections_context, docs_implementation_claim_order, docs_implementation_claim_order_phase4, docs_dashboard_inspection_status_cards_spec, docs_be_docs_00_core_prompt, docs_be_docs_01_database_schema, docs_be_docs_02_prd_server, docs_be_docs_03_project_structure, docs_be_docs_04_architecture [EXTRACTED 1.00]
- **GitNexus Skill Suite** — claude_skills_gitnexus_gitnexus_cli_skill, claude_skills_gitnexus_gitnexus_debugging_skill, claude_skills_gitnexus_gitnexus_exploring_skill, claude_skills_gitnexus_gitnexus_guide_skill, claude_skills_gitnexus_gitnexus_impact_analysis_skill, claude_skills_gitnexus_gitnexus_refactoring_skill [EXTRACTED 1.00]
- **Agent Skill Development Framework** — agents_skills_writing_great_skills_skill, agents_skills_writing_great_skills_glossary, agents_skills_triage_skill, agents_skills_wayfinder_skill [INFERRED 0.80]

## Communities (149 total, 58 thin omitted)

### Community 0 - "Sync Syncmanagertest"
Cohesion: 0.09
Nodes (8): DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, UploadPhotoResponse, SentPhotoStorage, SyncManagerTest

### Community 1 - "Master Masterdatarepositorytest"
Cohesion: 0.06
Nodes (9): ApiResponse, SyncResponse, ItemOut, MasterDataApi, RoomItemDto, RoomOut, SyncState, SyncStateStore (+1 more)

### Community 2 - "Core Database"
Cohesion: 0.05
Nodes (8): Flow, MasterDataDao, InspectionDetailEntity, InspectionEntity, InspectionPhotoEntity, RoomItemEntity, UserEntity, UserRoomEntity

### Community 3 - "Master Masterdataviewmodeltest"
Cohesion: 0.06
Nodes (11): UserOut, RuangEntity, Flow, MasterDataRepository, StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel (+3 more)

### Community 4 - "Inspection Inspectionhistoryrepositorytest"
Cohesion: 0.11
Nodes (3): PaginatedResponse, InspectionListItemDto, InspectionHistoryRepositoryTest

### Community 5 - "Inspection UI"
Cohesion: 0.06
Nodes (23): DashboardComposablesTest, StatusDisplay, toStatusDisplay(), StateFlow, NetworkConnectivityObserver, MainActivity, RsuAppTheme(), AnalyticsApi (+15 more)

### Community 6 - "Core Database"
Cohesion: 0.13
Nodes (3): DrafDao, DrafFoto, DrafItem

### Community 8 - "Core Network"
Cohesion: 0.18
Nodes (4): ApiEndpointIntegrationTest, Json, MockWebServer, RecordedRequest

### Community 10 - "Dashboard Dashboardviewmodeltest"
Cohesion: 0.14
Nodes (5): DashboardUiState, DashboardViewModel, StateFlow, ViewModel, DashboardViewModelTest

### Community 11 - "Inspection UI"
Cohesion: 0.11
Nodes (7): formatMillisToDate(), parseDateToMillis(), InspectionDatePickerDialog(), DateUtilsTest, TimeZone, DateUtilsTimezoneTest, TimeZone

### Community 12 - "Sync Draftphotocleanupworker"
Cohesion: 0.07
Nodes (19): App, DraftPhotoCleanupWorker, Context, CoroutineWorker, Result, schedule(), Context, SyncAwareWorkerFactory (+11 more)

### Community 13 - "Auth Authrepositorytest"
Cohesion: 0.08
Nodes (3): LogoutRequest, TokenResponse, AuthRepositoryTest

### Community 14 - "Inspection Inspectionformviewmodel"
Cohesion: 0.18
Nodes (4): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel

### Community 15 - "Auth Authrepository"
Cohesion: 0.18
Nodes (7): Authenticated, AuthRepository, AuthState, Error, StateFlow, Loading, Unauthenticated

### Community 16 - "Inspection Inspectionhistoryviewmodel"
Cohesion: 0.21
Nodes (5): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, Job

### Community 17 - "Auth API"
Cohesion: 0.16
Nodes (5): AuthApi, ChangePasswordRequest, LoginRequest, RefreshRequest, UserRoomDto

### Community 19 - "Inspections"
Cohesion: 0.24
Nodes (11): Auth Context, Core Context, Inspections Context, Sync Context, Force Logout, Context Map, Spec: Dashboard Inspection Status Cards, Implementation Claim Order — Phase 4 (+3 more)

### Community 20 - "Auth API"
Cohesion: 0.05
Nodes (44): 1. Ringkasan Perubahan, 2.1. Endpoint yang Terkena, 2.2. Format Response Paginated, 2.3. Request Parameters, 2.4. Catatan Khusus, 2.5. Sorting yang Didukung (Allowlist), 2.6. Pagination di Android — Riwayat Inspeksi (ADR-0013), 2. Pagination — Server-Driven (+36 more)

### Community 21 - "Core Network"
Cohesion: 0.11
Nodes (12): AppModule, Json, AuthInterceptor, Response, Response, TokenAuthenticator, Authenticator, Interceptor (+4 more)

### Community 24 - "Claude Skills"
Cohesion: 0.33
Nodes (6): GitNexus CLI Commands, Debugging with GitNexus, Exploring Codebases with GitNexus, GitNexus Guide, Impact Analysis with GitNexus, Refactoring with GitNexus

### Community 25 - "Agents Skills"
Cohesion: 0.40
Nodes (5): ask-matt SKILL.md, code-review SKILL.md, diagnosing-bugs SKILL.md, graphify SKILL.md, tdd SKILL.md

### Community 26 - "Dashboard Components"
Cohesion: 0.05
Nodes (40): ADR-0016: Dual-Path Photo Storage — Folder Draf & Folder Terkirim (retensi 30 hari), Alur Baru (ringkas), Consequences, Considered Options, Context, Decision, Lokasi Folder Terkirim, Mekanisme (+32 more)

### Community 28 - "Auth API"
Cohesion: 0.05
Nodes (37): ADR-0011: Single Module Architecture, Compared Options, Consequences, Context, Decision, Evaluasi, Negatif, Positif (+29 more)

### Community 29 - "App"
Cohesion: 0.50
Nodes (3): :app:testDebugUnitTest, DashboardViewModelTest > init loads all stats from dao flows, DashboardViewModelTest.kt

### Community 30 - "Docs Adr"
Cohesion: 0.50
Nodes (4): ADR-0012: Dual Mode Response — PaginatedResponse & SyncResponse, ADR-0013: Hybrid Inspection History — Local Cache + Server Fetch, ADR-0014: MediaStore Photo Storage & 30-Day Data Retention, ADR-0015: Draft Ownership per Akun & Siklus Hidup File Foto Draf

### Community 31 - "Agents Skills"
Cohesion: 0.67
Nodes (3): Writing Agent Briefs, Out-of-Scope Knowledge Base, Triage Skill

### Community 33 - "Docs Be"
Cohesion: 0.67
Nodes (3): DATABASE SCHEMA & LOGIC, Project Structure — RSUD Ajibarang Server Stack, Architecture — RSUD Ajibarang Server Stack

### Community 37 - "Aead"
Cohesion: 0.05
Nodes (15): Aead, AppDatabase, create(), Context, DatabaseModule, Context, DataStore, TokenData (+7 more)

### Community 39 - "Android"
Cohesion: 0.06
Nodes (30): Before exploring, read these, Domain Docs, File structure, Flag ADR conflicts, Use the glossary's vocabulary, Conventions, Issue tracker: GitHub, Pull requests as a triage surface (+22 more)

### Community 43 - "Core Database"
Cohesion: 0.06
Nodes (30): 1. Prinsip Desain, 2. Aturan File, 3. Research & Context Gathering, 4. Code Quality, 5. Proses Development, Always Do, Baca Domain Docs Terkait, Checklist Sebelum Commit (+22 more)

### Community 44 - "Master Masterdatarepository"
Cohesion: 0.07
Nodes (27): 1. State the question, 2. Pick the language, 3. Isolate the logic in a portable module, 4. Build the smallest TUI that exposes the state, 5. Make it runnable in one command, 6. Hand it over, 7. Capture the answer and the prototype, Anti-patterns (+19 more)

### Community 45 - "Master UI"
Cohesion: 0.07
Nodes (25): Learning Record Format, Numbering, Optional sections, Supersession, Template, What does _not_ qualify, When to write a learning record, MISSION.md Format (+17 more)

### Community 48 - "Core Network"
Cohesion: 0.09
Nodes (21): 1. In-process, 2. Local-substitutable, 3. Remote but owned (Ports & Adapters), 4. True external (Mock), Deepening, Dependency categories, Seam discipline, Testing strategy: replace, don't layer (+13 more)

### Community 49 - "Core Network"
Cohesion: 0.09
Nodes (19): ADR Format, Numbering, Optional sections, Template, What qualifies, When to offer an ADR, CONTEXT.md Format, Rules (+11 more)

### Community 50 - "Core Network"
Cohesion: 0.10
Nodes (18): Call-graph collapse, Candidate card, Cross-section (good for layered shallowness), Diagram patterns, Hand-built boxes-and-arrows (when Mermaid's layout fights you), Header, HTML Report Format, Mass diagram (good for "interface as wide as implementation") (+10 more)

### Community 51 - "Inspection Inspectionhistoryrepositorytest"
Cohesion: 0.17
Nodes (7): DetailSubmit, InspectionSubmit, PhotoSubmit, SyncApi, SyncManager, SyncResult, MultipartBody

### Community 56 - "Authinterceptor"
Cohesion: 0.18
Nodes (7): DaftarDrafUiState, DaftarDrafViewModel, StateFlow, ViewModel, DraftSummary, DaftarDrafScreen(), DraftCard()

### Community 57 - "Authviewmodel"
Cohesion: 0.15
Nodes (12): 1. Gather context, 2. Explore the codebase (optional), 3. Draft vertical slices, 4. Quiz the user, 5. Publish the tickets to the configured tracker, Acceptance criteria, Blocked by, <NN> — <Ticket title> (+4 more)

### Community 58 - "Datastore"
Cohesion: 0.19
Nodes (3): ImageCompressor, ImageCompressorTest, Context

### Community 78 - "Flow"
Cohesion: 0.15
Nodes (13): 2.2. Room-Item Relations Sync, 2.3. User-Room Relations Sync (Assigned Rooms), 2. Master Data API untuk Offline-First, 3. Upload & Media, 4.5. Standard Error Response Format, 5. Ringkasan Semua Endpoint Android, 6. Prioritas Implementasi, Android → Backend API Contract (+5 more)

### Community 79 - "Inspectionoutdto"
Cohesion: 0.18
Nodes (5): AuthViewModel, StateFlow, ViewModel, LoginUiState, LoginScreen()

### Community 80 - "Interceptor"
Cohesion: 0.17
Nodes (12): 🚦 Cara Claim Issue, 🗺️ Dependency Graph, File Baru, Fitur Baru, 📋 Implementation Claim Order — Phase 3: API Alignment & New Sync Endpoints, 📋 Issue List, 🎯 Latar Belakang, 🚦 Legend (+4 more)

### Community 81 - "Java My"
Cohesion: 0.22
Nodes (5): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, Flow, PhotoDetail

### Community 84 - "Java My"
Cohesion: 0.47
Nodes (3): InspectionDetailOutDto, InspectionOutDto, PhotoOutDto

### Community 86 - "Java My"
Cohesion: 0.20
Nodes (9): ADR-0004: Jetpack Compose + Modern Android Stack, Consequences, Considered Options, Context, Decision, Dependency Injection, Pelengkap, Serialization (+1 more)

### Community 87 - "Java My"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 88 - "Java My"
Cohesion: 0.22
Nodes (8): Boundaries, Intensity, Output, Persistence, Ponytail, Rules, The ladder, When NOT to be lazy

### Community 89 - "Java My"
Cohesion: 0.22
Nodes (8): Further Notes, Implementation Decisions, Out of Scope, Problem Statement, Process, Solution, Testing Decisions, User Stories

### Community 90 - "Java My"
Cohesion: 0.25
Nodes (7): Guidelines, How to Fetch Documentation, Step 1: Resolve the Library ID, Step 2: Select the Best Match, Step 3: Fetch the Documentation, Step 4: Use the Documentation, When to Use This Skill

### Community 91 - "Java My"
Cohesion: 0.25
Nodes (7): Configure Default Mode, Deactivate, Levels, More, Ponytail Help, Skills, Update

### Community 92 - "Java My"
Cohesion: 0.39
Nodes (3): NavGraph(), Routes, NavHostController

### Community 93 - "Java My"
Cohesion: 0.32
Nodes (5): ErrorSnackbarEffect(), InspectionDateFilterBar(), InspectionHistoryCard(), InspectionListScreen(), SnackbarHostState

### Community 94 - "Java My"
Cohesion: 0.38
Nodes (3): ApiErrorUtil, Response, ApiErrorDto

### Community 95 - "Java My"
Cohesion: 0.29
Nodes (6): 1. Objektif Repositori, 2. Peran Pengguna, 3. Spesifikasi Fitur Utama (UI/UX), 4. Spesifikasi Logika Sinkronisasi (Offline-First), 5. Keamanan Perangkat, PRODUCT REQUIREMENTS DOCUMENT (PRD) - ANDROID CLIENT

### Community 96 - "Java My"
Cohesion: 0.29
Nodes (6): Before exploring, read these, Contexts, Domain Docs, File structure, Flag ADR conflicts, Use the glossary's vocabulary

### Community 97 - "Java My"
Cohesion: 0.33
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 99 - "Java My"
Cohesion: 0.33
Nodes (6): 7. Lampiran: Perubahan ADR BE, ADR-0003 (JWT Layered Auth) — Perlu Update, ADR-0009 — Room-Item Many-to-Many (Phase 9A), ADR-0010 — User-Room Assignment (Phase 9B), ADR-0016 (Android) — Dependensi Endpoint Replace Photo, ADR Baru: Dual Delivery Auth

### Community 100 - "Java My"
Cohesion: 0.40
Nodes (4): Boundaries, Hunt, Output, Tags

### Community 101 - "Java My"
Cohesion: 0.40
Nodes (4): Boundaries, Honesty boundary, Ponytail Gain, Scoreboard

### Community 102 - "Java My"
Cohesion: 0.40
Nodes (4): Boundaries, Examples, Format, Scoring

### Community 104 - "Java My"
Cohesion: 0.40
Nodes (5): 1. Perubahan Auth: Dual Delivery Refresh Token, Latar Belakang, Perubahan 1.1: Login Response, Perubahan 1.2: Refresh Endpoint, Perubahan 1.3: Logout Response

### Community 105 - "Java My"
Cohesion: 0.40
Nodes (5): 4. Inspection Submission, Perubahan 4.1: Submit Inspection Request Body, Perubahan 4.2: Submit Inspection Response, Perubahan 4.3: List Inspections, Perubahan 4.4: Get Inspection Detail

### Community 106 - "Java My"
Cohesion: 0.83
Nodes (3): capture(), hitl-loop.template.sh script, step()

### Community 107 - "Java My"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 108 - "Java My"
Cohesion: 0.50
Nodes (3): For git commit hook, For native AGENTS.md integration, graphify reference: commit hook and native AGENTS.md integration

### Community 109 - "Java My"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 110 - "Java My"
Cohesion: 0.50
Nodes (3): Boundaries, Output, Scan

### Community 111 - "Java My"
Cohesion: 0.50
Nodes (3): Bad Tests, Good and Bad Tests, Good Tests

### Community 112 - "Java My"
Cohesion: 0.50
Nodes (3): GLOSSARY.md Format, Rules, Structure

### Community 113 - "Java My"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 121 - "Retrofit"
Cohesion: 0.67
Nodes (3): ✅ API-01: DB Schema — updated_at, business_date, new entities, File yang Diubah/Dibuat, Task List

### Community 122 - "Ruangentity"
Cohesion: 0.67
Nodes (3): ✅ API-02: API DTOs & Interfaces — SyncResponse, PaginatedResponse, new endpoints, File yang Diubah/Dibuat, Task List

### Community 123 - "Serializer"
Cohesion: 0.67
Nodes (3): ✅ API-03: Master Data Sync — RoomItems, MyRooms, UserRooms, File yang Diubah/Dibuat, Task List

### Community 124 - "Syncresponse"
Cohesion: 0.67
Nodes (3): ✅ API-04: Inspection History — List & Detail, Hybrid Storage, File yang Diubah/Dibuat, Task List

### Community 125 - "T"
Cohesion: 0.33
Nodes (5): Error, Loading, Success, UiState, T

### Community 127 - "Tokenauthenticator"
Cohesion: 0.67
Nodes (3): ✅ API-05: Dashboard & Analytics — `/api/analytics/dashboard`, File yang Diubah/Dibuat, Task List

### Community 128 - "Uri"
Cohesion: 0.12
Nodes (11): createTempPhotoUri(), Context, ItemCard(), Modifier, Modifier, PhotoThumbnail(), Modifier, ScoreIndicator() (+3 more)

### Community 129 - "Userout"
Cohesion: 0.67
Nodes (3): ✅ API-06: Submit Response & Standard Error Codes, File yang Diubah/Dibuat, Task List

## Knowledge Gaps
- **410 isolated node(s):** `Loading`, `Unauthenticated`, `Error`, `ApiResponse`, `Loading` (+405 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserOut` connect `Master Masterdataviewmodeltest` to `Inspection UI`, `Aead`, `Inspectionoutdto`, `Auth Authrepository`, `Auth API`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Why does `SyncManagerTest` connect `Sync Syncmanagertest` to `Master Masterdataviewmodeltest`, `Core Database`, `Java My`, `Inspection Inspectionhistoryrepositorytest`, `Datastore`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Why does `MasterDataDao` connect `Core Database` to `Master Masterdatarepositorytest`, `Java My`, `Master Masterdataviewmodeltest`, `Inspection Inspectionhistoryrepositorytest`, `Aead`, `Inspection Inspectionhistoryviewmodeltest`, `Dashboard Dashboardviewmodeltest`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **What connects `Loading`, `Unauthenticated`, `Error` to the rest of the system?**
  _410 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Sync Syncmanagertest` be split into smaller, more focused modules?**
  _Cohesion score 0.0851063829787234 - nodes in this community are weakly interconnected._
- **Should `Master Masterdatarepositorytest` be split into smaller, more focused modules?**
  _Cohesion score 0.061367621274108705 - nodes in this community are weakly interconnected._
- **Should `Core Database` be split into smaller, more focused modules?**
  _Cohesion score 0.054901960784313725 - nodes in this community are weakly interconnected._