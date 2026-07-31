# Project Structure — Single Module (ADR-0011)

> **Status:** ✅ Single module (`:app`) — ADR-0001 superseded by ADR-0011

```plaintext
rsud-android-client/
├── .gitignore
├── README.md
├── app/                        # Single module — semua source code
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/my/id/kentoes/rsudajibarangapp/
│   │   │   │   ├── App.kt                  # HiltAndroidApp + SyncAwareWorkerFactory
│   │   │   │   ├── auth/                   # Login, AuthViewModel, AuthRepository
│   │   │   │   │   ├── api/                # AuthApi (login/refresh/logout, users, user-rooms)
│   │   │   │   │   └── ui/                 # LoginScreen
│   │   │   │   ├── core/                   # Foundation: DI, database, network, navigation
│   │   │   │   │   ├── di/                 # AppModule (consolidated)
│   │   │   │   │   ├── model/              # UiState, ApiResponse (SyncResponse/PaginatedResponse), StatusDisplay
│   │   │   │   │   ├── network/            # Retrofit, OkHttp, AuthInterceptor, TokenAuthenticator, ApiErrorUtil, NetworkConnectivityObserver
│   │   │   │   │   ├── database/           # Room — AppDatabase (v4), DAOs, entities
│   │   │   │   │   │   ├── dao/            # MasterDataDao, DrafDao
│   │   │   │   │   │   └── entity/         # RuangEntity (isMyRoom), DrafInspeksi (inspectorId), dll.
│   │   │   │   │   ├── datastore/          # DataStore + Tink — TokenManager
│   │   │   │   │   │   └── di/             # DatabaseModule (consolidated)
│   │   │   │   │   ├── navigation/         # NavGraph
│   │   │   │   │   └── ui/                 # MainActivity, theme/
│   │   │   │   ├── dashboard/              # Dashboard & statistik
│   │   │   │   │   ├── api/                # AnalyticsApi
│   │   │   │   │   ├── components/         # StatCard, IssueCard, RecentDraftCard, RoomScoreCard
│   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   └── DashboardViewModel.kt
│   │   │   │   ├── inspection/             # Form inspeksi, skoring, draf, riwayat
│   │   │   │   │   ├── components/         # CameraHelper, ItemCard, PhotoThumbnail, ScoreIndicator
│   │   │   │   │   ├── ui/                 # DaftarDrafScreen, InspectionListScreen, InspectionDetailScreen
│   │   │   │   │   │   ├── components/     # OfflineBanner
│   │   │   │   │   │   ├── dateUtils.kt                # Helper format/parse tanggal
│   │   │   │   │   │   ├── ErrorSnackbarEffect.kt      # Snackbar error terpusat
│   │   │   │   │   │   ├── InspectionDateFilterBar.kt  # Filter tanggal riwayat
│   │   │   │   │   │   └── InspectionDatePickerDialog.kt
│   │   │   │   │   ├── DraftPhotoCleaner.kt   # Cleanup foto draf yatim
│   │   │   │   │   ├── InspectionFormScreen.kt / ViewModel
│   │   │   │   │   ├── InspectionHistoryRepository.kt / ViewModel
│   │   │   │   │   └── InspectionRepository.kt
│   │   │   │   ├── master/                 # Master data download & sync state
│   │   │   │   │   ├── api/                # MasterDataApi
│   │   │   │   │   ├── ui/                 # MasterDataListScreen
│   │   │   │   │   ├── MasterDataRepository.kt / ViewModel
│   │   │   │   │   ├── SyncState.kt        # synced_at per endpoint
│   │   │   │   │   └── SyncStateStore.kt   # Persist state sync (SharedPreferences, ADR-0012)
│   │   │   │   └── sync/                   # WorkManager, kompresi, upload
│   │   │   │       ├── api/                # SyncApi
│   │   │   │       ├── SyncManager.kt
│   │   │   │       ├── SyncWorker.kt
│   │   │   │       ├── SyncAwareWorkerFactory.kt   # Factory worker (bukan @HiltWorker)
│   │   │   │       ├── DraftPhotoCleanupWorker.kt  # Worker periodik cleanup foto draf
│   │   │   │       └── ImageCompressor.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/my/id/kentoes/rsudajibarangapp/  # Unit tests (21 files, 289 @Test)
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                  # Version catalog
├── docs/
│   ├── adr/                                # ADR-0001 s.d. ADR-0015
│   └── ...
├── build.gradle.kts                        # Root build (plugin aliases only)
└── settings.gradle.kts                     # Hanya include(":app")
```
