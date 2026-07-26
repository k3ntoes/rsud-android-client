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
│   │   │   │   ├── App.kt                  # HiltAndroidApp + WorkerFactory
│   │   │   │   ├── auth/                   # Login, AuthViewModel, AuthRepository
│   │   │   │   │   ├── api/
│   │   │   │   │   └── ui/
│   │   │   │   ├── core/                   # Foundation: DI, navigation, theme
│   │   │   │   │   ├── di/                 # AppModule (consolidated)
│   │   │   │   │   ├── navigation/
│   │   │   │   │   └── ui/theme/
│   │   │   │   ├── core/model/             # Shared data classes (ApiResponse, UiState)
│   │   │   │   ├── core/network/           # Retrofit, OkHttp, AuthInterceptor
│   │   │   │   ├── core/database/          # Room — entities, DAOs, AppDatabase
│   │   │   │   │   ├── dao/
│   │   │   │   │   └── entity/
│   │   │   │   ├── core/datastore/         # DataStore + Tink — TokenManager
│   │   │   │   │   └── di/                 # DatabaseModule (consolidated)
│   │   │   │   ├── dashboard/              # DashboardScreen, ViewModel
│   │   │   │   ├── inspection/             # Form inspeksi, scoring, draf
│   │   │   │   │   ├── components/
│   │   │   │   │   └── ui/
│   │   │   │   ├── master/                 # Master data download
│   │   │   │   │   ├── api/
│   │   │   │   │   └── ui/
│   │   │   │   └── sync/                   # WorkManager, kompresi, upload
│   │   │   │       └── api/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/my/id/kentoes/rsudajibarangapp/  # Unit tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                  # Version catalog
├── docs/
│   ├── adr/                                # Architecture Decision Records
│   └── ...
├── build.gradle.kts                        # Root build (plugin aliases only)
└── settings.gradle.kts                     # Hanya include(":app")
```