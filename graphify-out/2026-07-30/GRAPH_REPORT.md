# Graph Report - .  (2026-07-30)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 425 nodes · 544 edges · 103 communities (18 shown, 85 thin omitted)
- Extraction: 82% EXTRACTED · 18% INFERRED · 0% AMBIGUOUS · INFERRED: 100 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e6d2a397`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MasterDataDao
- MasterDataRepositoryTest
- NavGraph
- InspectionHistoryRepositoryTest
- ApiEndpointIntegrationTest
- InspectionHistoryViewModelTest
- DashboardViewModel
- MasterDataViewModel
- InspectionHistoryViewModel
- MasterDataRepository
- Inspection Services
- GitNexus Guide
- ask-matt SKILL.md
- Auth API
- :app:testDebugUnitTest
- Triage Skill
- Inspections Context
- Architecture — RSUD Ajibarang Server Stack
- Glossary — Building Great Skills
- Background Services
- Aead
- Wayfinder Skill
- android
- Application
- Authenticator
- AuthInterceptor
- Configuration
- CoroutineWorker
- DataStore
- ADR-0001: Multi-module Architecture (Android)
- ADR-0002: Proto DataStore + Tink
- ADR-0014: MediaStore & Retention
- Context: Analytics
- Context: Authentication & Authorization
- Context: Background Jobs
- Context: Inspection
- Context: Master Data
- Context: Media & Upload
- CORE PROMPT: RSUD Ajibarang Server Stack
- PRODUCT REQUIREMENTS DOCUMENT (PRD) - SERVER STACK
- ADR-0001: React + Vite + TanStack
- ADR-0002: Multi-Photo Schema
- ADR-0004: Dual-Database Strategy
- ADR-0006: Test Strategy
- ADR-0008: User Management & Monitoring
- Implementation Claim Order
- DrafInspeksi
- Flow
- Interceptor
- ItemState
- StateFlow
- StateFlow
- ViewModel
- StateFlow
- ViewModel
- Context
- Flow
- Flow
- Context
- Response
- Response
- Flow
- Response
- Modifier
- StateFlow
- ViewModel
- Context
- Modifier
- Modifier
- Modifier
- DraftSummary
- StateFlow
- ViewModel
- StateFlow
- ViewModel
- Flow
- StateFlow
- ViewModel
- Flow
- Modifier
- DraftSummary
- Flow
- StateFlow
- ViewModel
- Context
- Context
- ListenableWorker
- MasterDataItem
- MultipartBody
- OkHttpClient
- Request
- Result
- Retrofit
- RoomDatabase
- Route
- RuangEntity
- Serializer
- T
- TokenAuthenticator
- Uri
- WorkerFactory
- WorkerParameters

## God Nodes (most connected - your core abstractions)
1. `MasterDataRepositoryTest` - 37 edges
2. `MasterDataDao` - 36 edges
3. `ApiEndpointIntegrationTest` - 31 edges
4. `InspectionHistoryViewModelTest` - 30 edges
5. `MasterDataViewModel` - 24 edges
6. `DashboardViewModel` - 23 edges
7. `InspectionHistoryRepositoryTest` - 23 edges
8. `DashboardViewModelTest` - 22 edges
9. `MasterDataViewModelTest` - 19 edges
10. `MasterDataRepository` - 18 edges

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
- **Main Flow Skills** — agents_skills_ask_matt_skill_md, agents_skills_tdd_skill_md, agents_skills_code_review_skill_md [EXTRACTED 0.90]
- **RSUD Ajibarang Inspection System** — app_src_main_java_my_id_kentoes_rsudajibarangapp_inspections_context, docs_implementation_claim_order, docs_implementation_claim_order_phase4, docs_dashboard_inspection_status_cards_spec, docs_be_docs_00_core_prompt, docs_be_docs_01_database_schema, docs_be_docs_02_prd_server, docs_be_docs_03_project_structure, docs_be_docs_04_architecture [EXTRACTED 1.00]
- **GitNexus Skill Suite** — claude_skills_gitnexus_gitnexus_cli_skill, claude_skills_gitnexus_gitnexus_debugging_skill, claude_skills_gitnexus_gitnexus_exploring_skill, claude_skills_gitnexus_gitnexus_guide_skill, claude_skills_gitnexus_gitnexus_impact_analysis_skill, claude_skills_gitnexus_gitnexus_refactoring_skill [EXTRACTED 1.00]
- **Agent Skill Development Framework** — agents_skills_writing_great_skills_skill, agents_skills_writing_great_skills_glossary, agents_skills_triage_skill, agents_skills_wayfinder_skill [INFERRED 0.80]
- **Backend Authentication Flow** — docs_be_docs_adr_0003_jwt_auth_architecture, backend_app_modules_auth_api, docs_be_docs_adr_0007_frontend_auth_pattern [EXTRACTED]
- **Inspection Lifecycle Management** — docs_adr_0003_offline_first_inspection_submission, backend_app_modules_inspection_services, docs_be_docs_adr_0002_multi_photo_schema [EXTRACTED]
- **Background Job & Analytics System** — docs_be_docs_07_background_jobs_gaps, backend_app_modules_background_services, docs_be_docs_08_context_audit [INFERRED]

## Communities (103 total, 85 thin omitted)

### Community 0 - "MasterDataDao"
Cohesion: 0.06
Nodes (10): Flow, MasterDataItem, RuangEntity, MasterDataDao, InspectionDetailEntity, InspectionEntity, InspectionPhotoEntity, RoomItemEntity (+2 more)

### Community 1 - "MasterDataRepositoryTest"
Cohesion: 0.08
Nodes (8): ItemOut, MasterDataApi, RoomItemDto, RoomOut, AuthApi, MasterDataRepositoryTest, PaginatedResponse, SyncResponse

### Community 2 - "NavGraph"
Cohesion: 0.07
Nodes (23): NavGraph(), Routes, MainActivity, StatCard(), DashboardScreen(), DaftarDrafScreen(), DraftCard(), InspectionHistoryCard() (+15 more)

### Community 3 - "InspectionHistoryRepositoryTest"
Cohesion: 0.08
Nodes (8): InspectionDetailItem, InspectionHistoryItem, InspectionHistoryRepository, Flow, PhotoDetail, InspectionHistoryRepositoryTest, SyncApi, InspectionOutDto

### Community 4 - "ApiEndpointIntegrationTest"
Cohesion: 0.15
Nodes (7): ApiEndpointIntegrationTest, AnalyticsApi, AuthApi, SyncApi, Json, MockWebServer, RecordedRequest

### Community 6 - "DashboardViewModel"
Cohesion: 0.11
Nodes (7): DashboardUiState, DashboardViewModel, StateFlow, ViewModel, DashboardViewModelTest, AnalyticsApi, DrafDao

### Community 7 - "MasterDataViewModel"
Cohesion: 0.11
Nodes (5): StateFlow, ViewModel, MasterDataUiState, MasterDataViewModel, MasterDataViewModelTest

### Community 8 - "InspectionHistoryViewModel"
Cohesion: 0.22
Nodes (5): InspectionHistoryUiState, InspectionHistoryViewModel, StateFlow, ViewModel, Job

### Community 9 - "MasterDataRepository"
Cohesion: 0.22
Nodes (4): Flow, MasterDataItem, RuangEntity, MasterDataRepository

### Community 10 - "Inspection Services"
Cohesion: 0.29
Nodes (7): Inspection Services, Media API, ADR-0003: Offline-First Submission, Refactoring Tracker, Comprehensive CONTEXT Audit, CONTEXT Audit GAP Fixes, ADR-0005: Async ORM Strategy

### Community 11 - "GitNexus Guide"
Cohesion: 0.33
Nodes (6): GitNexus CLI Commands, Debugging with GitNexus, Exploring Codebases with GitNexus, GitNexus Guide, Impact Analysis with GitNexus, Refactoring with GitNexus

### Community 12 - "ask-matt SKILL.md"
Cohesion: 0.40
Nodes (5): ask-matt SKILL.md, code-review SKILL.md, diagnosing-bugs SKILL.md, graphify SKILL.md, tdd SKILL.md

### Community 13 - "Auth API"
Cohesion: 0.40
Nodes (5): Auth API, Implementation Tracking, ADR-0003: JWT Layered Auth, ADR-0007: Frontend Auth Pattern, Frontend API Client

### Community 14 - ":app:testDebugUnitTest"
Cohesion: 0.50
Nodes (3): :app:testDebugUnitTest, DashboardViewModelTest > init loads all stats from dao flows, DashboardViewModelTest.kt

### Community 15 - "Triage Skill"
Cohesion: 0.67
Nodes (3): Writing Agent Briefs, Out-of-Scope Knowledge Base, Triage Skill

### Community 16 - "Inspections Context"
Cohesion: 1.00
Nodes (3): Inspections Context, Spec: Dashboard Inspection Status Cards, Implementation Claim Order — Phase 4

### Community 17 - "Architecture — RSUD Ajibarang Server Stack"
Cohesion: 0.67
Nodes (3): DATABASE SCHEMA & LOGIC, Project Structure — RSUD Ajibarang Server Stack, Architecture — RSUD Ajibarang Server Stack

## Knowledge Gaps
- **35 isolated node(s):** `InspectionDetailItem`, `PhotoDetail`, `DashboardViewModelTest.kt`, `diagnosing-bugs SKILL.md`, `tdd SKILL.md` (+30 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **85 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MasterDataDao` connect `MasterDataDao` to `MasterDataRepositoryTest`, `InspectionHistoryRepositoryTest`, `InspectionHistoryViewModelTest`, `DashboardViewModel`?**
  _High betweenness centrality (0.252) - this node is a cross-community bridge._
- **Why does `MasterDataRepositoryTest` connect `MasterDataRepositoryTest` to `MasterDataDao`, `MasterDataRepository`, `NavGraph`?**
  _High betweenness centrality (0.235) - this node is a cross-community bridge._
- **Why does `MasterDataRepository` connect `MasterDataRepository` to `MasterDataDao`, `MasterDataRepositoryTest`, `DashboardViewModel`, `MasterDataViewModel`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Are the 15 inferred relationships involving `MasterDataViewModel` (e.g. with `.`clearSyncMessage clears syncMessage to null`()` and `.`clearSyncMessage does not affect other state fields`()`) actually correct?**
  _`MasterDataViewModel` has 15 INFERRED edges - model-reasoned connections that need verification._
- **What connects `InspectionDetailItem`, `PhotoDetail`, `DashboardViewModelTest.kt` to the rest of the system?**
  _35 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MasterDataDao` be split into smaller, more focused modules?**
  _Cohesion score 0.06475485661424607 - nodes in this community are weakly interconnected._
- **Should `MasterDataRepositoryTest` be split into smaller, more focused modules?**
  _Cohesion score 0.07781649245063879 - nodes in this community are weakly interconnected._