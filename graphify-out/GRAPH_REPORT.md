# Graph Report - .  (2026-07-31)

## Corpus Check
- 80 files · ~130,657 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 889 nodes · 1153 edges · 189 communities (42 shown, 147 thin omitted)
- Extraction: 80% EXTRACTED · 20% INFERRED · 0% AMBIGUOUS · INFERRED: 227 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- master_masterdatarepositorytest
- core_database
- master_masterdataviewmodeltest
- core_database
- inspection_inspectionhistoryrepositorytest
- inspection_inspectionhistoryviewmodeltest
- sync_syncmanagertest
- core_navigation
- core_network
- inspection_inspectionformviewmodeltest
- dashboard_dashboardviewmodeltest
- inspection_ui
- sync_draftphotocleanupworker
- auth_authrepositorytest
- auth_authrepository
- inspection_inspectionformviewmodel
- auth_api
- inspection_inspectionhistoryviewmodel
- adr_0014
- inspection_draftphotocleanertest
- agents_skills
- auth_context
- core_network
- inspection_inspectionformviewmodeltest
- docs_be
- agents_skills
- claude_skills
- agents_skills
- agents_skills
- dashboard_components
- docs_be
- auth_api
- app_testdebugunittest
- docs_adr
- adr_0012
- agents_skills
- agents_skills
- backend_app
- docs_be
- agents_skills
- backend_app
- aead
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- agents_skills
- android
- app_ic
- app_ic
- core_database
- master_masterdatarepository
- master_ui
- app_src
- app_src
- core_network
- core_network
- core_network
- inspection_inspectionhistoryrepositorytest
- master_masterdatarepositorytest
- auth_admin
- auth_petugas
- auth_supervisor
- authinterceptor
- authviewmodel
- core_navigation
- datastore
- docs_03
- docs_adr
- docs_adr
- docs_agents
- docs_agents
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_be
- docs_implementation
- docs_implementation
- draftsummary
- flow
- inspectionoutdto
- interceptor
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- java_my
- masterdataitem
- networkconnectivityobserver
- okhttpclient
- paginatedresponse
- retrofit
- ruangentity
- serializer
- syncresponse
- t
- tokenauthenticator
- uri
- userout

## God Nodes (most connected - your core abstractions)
1. `MasterDataRepositoryTest` - 47 edges
2. `MasterDataDao` - 40 edges
3. `InspectionFormViewModelTest` - 37 edges
4. `SyncManagerTest` - 34 edges
5. `InspectionHistoryViewModelTest` - 32 edges
6. `ApiEndpointIntegrationTest` - 31 edges
7. `MasterDataViewModel` - 27 edges
8. `AuthRepositoryTest` - 26 edges
9. `DrafDao` - 23 edges
10. `InspectionHistoryRepositoryTest` - 23 edges

## Surprising Connections (you probably didn't know these)
- `ADR-0003: JWT Layered Auth` --conceptually_related_to--> `Auth API`  [INFERRED]
  docs/BE/docs/adr/0003-jwt-auth-architecture.md → backend/app/modules/auth/api.py
- `ADR-0003: Offline-First Submission` --conceptually_related_to--> `Inspection Services`  [INFERRED]
  docs/adr/0003-offline-first-inspection-submission.md → backend/app/modules/inspection/services.py
- `Implementation Tracking` --references--> `Auth API`  [EXTRACTED]
  docs/BE/docs/05-implementation-tracking.md → backend/app/modules/auth/api.py
- `Refactoring Tracker` --references--> `Inspection Services`  [EXTRACTED]
  docs/BE/docs/06-refactoring-tracker.md → backend/app/modules/inspection/services.py
- `Background Jobs Gaps` --references--> `Background Services`  [EXTRACTED]
  docs/BE/docs/07-background-jobs-gaps.md → backend/app/modules/background/services.py

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Codebase Design Concepts** — agents_skills_codebase_design_skill, agents_skills_codebase_design_deepening, agents_skills_codebase_design_design_it_twice [EXTRACTED 1.00]
- **Domain Modeling Concepts** — agents_skills_domain_modeling_skill, agents_skills_domain_modeling_adr_format, agents_skills_domain_modeling_context_format [EXTRACTED 1.00]
- **Teaching Workspace Structure** — agents_skills_teach_skill, agents_skills_teach_glossary_format, agents_skills_teach_learning_record_format, agents_skills_teach_mission_format, agents_skills_teach_resources_format [EXTRACTED 1.00]
- **Dashboard Inspection Status Flow** — dashboard_dashboardviewmodel, dashboard_dashboardscreen, master_masterdatarepository, core_database_dao_masterdatadao [EXTRACTED 0.90]
- **Sync and Master Data Infrastructure** — master_masterdatarepository, sync_syncstatestore, adr_0012 [EXTRACTED 0.85]
- **Domain Contexts** — app_src_main_java_my_id_kentoes_rsudajibarangapp_auth_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_inspections_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_sync_context, app_src_main_java_my_id_kentoes_rsudajibarangapp_core_context [EXTRACTED 1.00]
- **User Roles** — auth_petugas, auth_supervisor, auth_admin_ppi [EXTRACTED 1.00]
- **Android Data Synchronization Strategy** — docs_adr_0012_dual_mode_response, docs_adr_0013_hybrid_inspection_history, docs_adr_0015_draft_ownership_per_account [EXTRACTED 0.90]
- **Main Flow Skills** — agents_skills_ask_matt_skill_md, agents_skills_tdd_skill_md, agents_skills_code_review_skill_md [EXTRACTED 0.90]
- **GitNexus Skill Suite** — claude_skills_gitnexus_gitnexus_cli_skill, claude_skills_gitnexus_gitnexus_debugging_skill, claude_skills_gitnexus_gitnexus_exploring_skill, claude_skills_gitnexus_gitnexus_guide_skill, claude_skills_gitnexus_gitnexus_impact_analysis_skill, claude_skills_gitnexus_gitnexus_refactoring_skill [EXTRACTED 1.00]
- **Agent Skill Development Framework** — agents_skills_writing_great_skills_skill, agents_skills_writing_great_skills_glossary, agents_skills_triage_skill, agents_skills_wayfinder_skill [INFERRED 0.80]
- **Backend Authentication Flow** — docs_be_docs_adr_0003_jwt_auth_architecture, backend_app_modules_auth_api, docs_be_docs_adr_0007_frontend_auth_pattern [EXTRACTED]
- **Inspection Lifecycle Management** — docs_adr_0003_offline_first_inspection_submission, backend_app_modules_inspection_services, docs_be_docs_adr_0002_multi_photo_schema [EXTRACTED]
- **Background Job & Analytics System** — docs_be_docs_07_background_jobs_gaps, backend_app_modules_background_services, docs_be_docs_08_context_audit [INFERRED]

## Communities (189 total, 147 thin omitted)

### Community 0 - "master_masterdatarepositorytest"
Cohesion: 0.00
Nodes (8): ItemOut, SyncResponse, MasterDataApi, RoomItemDto, RoomOut, SyncStateStore, MasterDataRepositoryTest, SyncState

### Community 1 - "core_database"
Cohesion: 0.00
Nodes (11): AppDatabase, create(), Context, DrafDao, Flow, DrafDaoTest, InspectionRepositoryTest, DrafFoto (+3 more)

### Community 2 - "master_masterdataviewmodeltest"
Cohesion: 0.00
Nodes (10): UserOut, RuangEntity, Flow, MasterDataItem, MasterDataRepository, StateFlow, ViewModel, MasterDataUiState (+2 more)

### Community 3 - "core_database"
Cohesion: 0.00
Nodes (9): Flow, MasterDataItem, MasterDataDao, InspectionDetailEntity, InspectionEntity, InspectionPhotoEntity, RoomItemEntity, UserEntity (+1 more)

### Community 4 - "inspection_inspectionhistoryrepositorytest"
Cohesion: 0.00
Nodes (14): ApiErrorDto, DetailSubmit, InspectionDetailOutDto, InspectionListItemDto, InspectionOutDto, InspectionSubmit, PaginatedResponse, PhotoOutDto (+6 more)

### Community 5 - "inspection_inspectionhistoryviewmodeltest"
Cohesion: 0.00
Nodes (8): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, Flow, PaginatedResult, PhotoDetail, InspectionHistoryCard(), InspectionHistoryViewModelTest

### Community 6 - "sync_syncmanagertest"
Cohesion: 0.00
Nodes (8): DraftWithItems, InspectionPayload, InspectionRepository, Flow, PayloadItem, UploadPhotoResponse, SyncManagerTest, ImageCompressor

### Community 7 - "core_navigation"
Cohesion: 0.00
Nodes (24): AuthViewModel, LoginScreen(), AuthViewModel, NavGraph(), Routes, StateFlow, NetworkConnectivityObserver, MainActivity (+16 more)

### Community 8 - "core_network"
Cohesion: 0.00
Nodes (6): AnalyticsApi, LoginRequest, ApiEndpointIntegrationTest, Json, MockWebServer, RecordedRequest

### Community 10 - "dashboard_dashboardviewmodeltest"
Cohesion: 0.00
Nodes (7): DashboardUiState, DashboardViewModel, StateFlow, ViewModel, DashboardViewModelTest, AnalyticsApi, DrafDao

### Community 11 - "inspection_ui"
Cohesion: 0.00
Nodes (7): formatMillisToDate(), parseDateToMillis(), InspectionDatePickerDialog(), DateUtilsTest, TimeZone, DateUtilsTimezoneTest, TimeZone

### Community 12 - "sync_draftphotocleanupworker"
Cohesion: 0.00
Nodes (14): App, DraftPhotoCleanupWorker, Context, schedule(), Context, SyncAwareWorkerFactory, DraftPhotoCleanupWorkerTest, Application (+6 more)

### Community 14 - "auth_authrepository"
Cohesion: 0.00
Nodes (7): Authenticated, AuthRepository, AuthState, Error, StateFlow, Loading, Unauthenticated

### Community 15 - "inspection_inspectionformviewmodel"
Cohesion: 0.00
Nodes (4): InspectionFormUiState, InspectionFormViewModel, StateFlow, ViewModel

### Community 16 - "auth_api"
Cohesion: 0.00
Nodes (6): AuthApi, ChangePasswordRequest, PaginatedResponse, SyncResponse, RefreshRequest, UserRoomDto

### Community 17 - "inspection_inspectionhistoryviewmodel"
Cohesion: 0.00
Nodes (7): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, InspectionDetailScreen(), PhotoThumbnailCard(), Job

### Community 18 - "adr_0014"
Cohesion: 0.00
Nodes (15): ADR-0014: MediaStore Photo Storage, ADR-0015: Draft Ownership per Akun, MasterDataDao, StatCard, DashboardScreen, DashboardViewModel, Implementation Claim Order — Phase 4, EPIC-12: Dashboard Inspeksi Hari Ini (+7 more)

### Community 20 - "agents_skills"
Cohesion: 0.00
Nodes (10): Deepening, Design It Twice, Codebase Design Skill, ADR Format, CONTEXT.md Format, Domain Modeling Skill, Grill with Docs Skill, Grilling Skill (+2 more)

### Community 21 - "auth_context"
Cohesion: 0.00
Nodes (10): Auth Context, Core Context, Inspections Context, Sync Context, Force Logout, Context Map, Spec: Dashboard Inspection Status Cards, Draf Inspeksi (+2 more)

### Community 22 - "core_network"
Cohesion: 0.00
Nodes (5): TokenAuthenticator, Authenticator, Request, Response, Route

### Community 24 - "docs_be"
Cohesion: 0.00
Nodes (7): Inspection Services, Media API, ADR-0003: Offline-First Submission, Refactoring Tracker, Comprehensive CONTEXT Audit, CONTEXT Audit GAP Fixes, ADR-0005: Async ORM Strategy

### Community 25 - "agents_skills"
Cohesion: 0.00
Nodes (6): Domain Docs Setup, Issue Tracker: GitHub, Issue Tracker: GitLab, Issue Tracker: Local Markdown, Setup Matt Pocock Skills Skill, Triage Labels Setup

### Community 26 - "claude_skills"
Cohesion: 0.00
Nodes (6): GitNexus CLI Commands, Debugging with GitNexus, Exploring Codebases with GitNexus, GitNexus Guide, Impact Analysis with GitNexus, Refactoring with GitNexus

### Community 27 - "agents_skills"
Cohesion: 0.00
Nodes (5): ask-matt SKILL.md, code-review SKILL.md, diagnosing-bugs SKILL.md, graphify SKILL.md, tdd SKILL.md

### Community 28 - "agents_skills"
Cohesion: 0.00
Nodes (5): GLOSSARY.md Format, Learning Record Format, MISSION.md Format, RESOURCES.md Format, Teach Skill

### Community 29 - "dashboard_components"
Cohesion: 0.00
Nodes (4): StatCard(), Color, ImageVector, Modifier

### Community 30 - "docs_be"
Cohesion: 0.00
Nodes (5): Auth API, Implementation Tracking, ADR-0003: JWT Layered Auth, ADR-0007: Frontend Auth Pattern, Frontend API Client

### Community 32 - "app_testdebugunittest"
Cohesion: 0.00
Nodes (3): :app:testDebugUnitTest, DashboardViewModelTest > init loads all stats from dao flows, DashboardViewModelTest.kt

### Community 33 - "docs_adr"
Cohesion: 0.00
Nodes (4): ADR-0012: Dual Mode Response — PaginatedResponse & SyncResponse, ADR-0013: Hybrid Inspection History — Local Cache + Server Fetch, ADR-0014: MediaStore Photo Storage & 30-Day Data Retention, ADR-0015: Draft Ownership per Akun & Siklus Hidup File Foto Draf

### Community 34 - "adr_0012"
Cohesion: 0.00
Nodes (3): ADR-0012: Sync Inkremental, ADR-0013: Pagination Server-Driven, Android Implementation Guide

### Community 35 - "agents_skills"
Cohesion: 0.00
Nodes (3): Logic Prototype, Prototype Skill, UI Prototype

### Community 36 - "agents_skills"
Cohesion: 0.00
Nodes (3): Writing Agent Briefs, Out-of-Scope Knowledge Base, Triage Skill

### Community 37 - "backend_app"
Cohesion: 0.00
Nodes (3): Backend Configuration, Docker Compose, ADR-0004: SQLite + aiosqlite untuk Development, PostgreSQL untuk Production

### Community 38 - "docs_be"
Cohesion: 0.00
Nodes (3): DATABASE SCHEMA & LOGIC, Project Structure — RSUD Ajibarang Server Stack, Architecture — RSUD Ajibarang Server Stack

## Knowledge Gaps
- **127 isolated node(s):** `DashboardViewModelTest.kt`, `diagnosing-bugs SKILL.md`, `tdd SKILL.md`, `code-review SKILL.md`, `graphify SKILL.md` (+122 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **147 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SyncManagerTest` connect `sync_syncmanagertest` to `core_database`, `master_masterdataviewmodeltest`, `inspection_inspectionhistoryrepositorytest`, `inspection_inspectionhistoryviewmodeltest`?**
  _High betweenness centrality (0.135) - this node is a cross-community bridge._
- **Why does `MasterDataRepositoryTest` connect `master_masterdatarepositorytest` to `auth_api`, `master_masterdataviewmodeltest`, `core_database`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `MasterDataRepository` connect `master_masterdataviewmodeltest` to `master_masterdatarepositorytest`, `dashboard_dashboardviewmodeltest`, `core_database`, `sync_syncmanagertest`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **What connects `DashboardViewModelTest.kt`, `diagnosing-bugs SKILL.md`, `tdd SKILL.md` to the rest of the system?**
  _127 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `master_masterdatarepositorytest` be split into smaller, more focused modules?**
  _Cohesion score 0.0531986531986532 - nodes in this community are weakly interconnected._
- **Should `core_database` be split into smaller, more focused modules?**
  _Cohesion score 0.05660377358490566 - nodes in this community are weakly interconnected._
- **Should `master_masterdataviewmodeltest` be split into smaller, more focused modules?**
  _Cohesion score 0.06259426847662142 - nodes in this community are weakly interconnected._