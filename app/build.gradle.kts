import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// ===== BASE_URL dari file .env (root project) =====
// Ubah URL backend cukup edit file `.env` — tanpa perlu buka project / file ini.
// Format: BASE_URL=https://be-ajib.kentoes.my.id/api/
// Jika `.env` tidak ada atau key BASE_URL kosong → fallback ke BASE_URL_DEFAULT.
private val BASE_URL_DEFAULT = "https://be-ajib.kentoes.my.id/api/"

private fun resolveBaseUrl(): String {
    val envFile = rootProject.layout.projectDirectory.file(".env")
    // providers.fileContents di-track configuration cache → build otomatis
    // memakai nilai terbaru saat .env berubah (tanpa perlu clean / no-config-cache).
    val envText = if (envFile.asFile.exists()) {
        providers.fileContents(envFile).asText.get().removePrefix("\uFEFF") // anti-BOM (Notepad Windows)
    } else {
        ""
    }
    if (envText.isBlank()) return BASE_URL_DEFAULT
    val props = Properties()
    props.load(StringReader(envText))
    // Escape untuk buildConfigField (URL normal tidak mengandung karakter ini)
    return props.getProperty("BASE_URL")?.trim()?.takeIf { it.isNotBlank() }
        ?.replace("\\", "\\\\")?.replace("\"", "\\\"") ?: BASE_URL_DEFAULT
}

android {
    namespace = "my.id.kentoes.rsudajibarangapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "my.id.kentoes.rsudajibarangapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Base URL — dibaca dari file .env di root project (lihat resolveBaseUrl)
        buildConfigField("String", "BASE_URL", "\"${resolveBaseUrl()}\"")
    }

    buildTypes {
        // BASE_URL diwarisi dari defaultConfig (sumber: .env) untuk semua build type.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true // dibutuhkan Robolectric
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileSdkMinor = 0
}

// ===== Task buildApk: build APK debug + release, salin ke folder `apk/` =====
// Usage: ./gradlew buildApk
// Output:
//   apk/rsud-ajibarang-v<versionName>-debug.apk
//   apk/rsud-ajibarang-v<versionName>-release-unsigned.apk  (belum ada signing config)
tasks.register("buildApk") {
    group = "build"
    description = "Build APK debug + release dan salin hasilnya ke folder apk/ di root project"

    // Catatan: nama task assemble ini berlaku selama project single-flavor;
    // jika nanti menambah product flavors, task berubah jadi assemble<Flavor><Type>.
    dependsOn("assembleDebug", "assembleRelease")

    // Nilai di-resolve ke File/String polos di level task (bukan script level),
    // agar doLast tidak menangkap referensi objek script — kompatibel configuration cache.
    val outputDir = rootProject.layout.projectDirectory.dir("apk").asFile
    val apkOutputParentDir = layout.buildDirectory.dir("outputs/apk").get().asFile
    val projectRootDir = rootProject.layout.projectDirectory.asFile
    val appVersionName = android.defaultConfig.versionName ?: "1.0"

    doLast {
        // Bersihkan APK lama hasil task ini (bukan APK lain yang mungkin ditaruh manual).
        outputDir.listFiles { file -> file.extension == "apk" && file.name.startsWith("rsud-ajibarang-") }
            ?.forEach { it.delete() }
        outputDir.mkdirs()

        val baseName = "rsud-ajibarang-v$appVersionName"

        fun copyApk(buildType: String) {
            val apkDir = File(apkOutputParentDir, buildType)
            // Prioritaskan APK signed (nama tanpa "unsigned"); fallback ke APK apa pun.
            val apk = apkDir.listFiles { file -> file.extension == "apk" }
                ?.sortedBy { it.name.contains("unsigned") }
                ?.firstOrNull()
                ?: error("APK $buildType tidak ditemukan di $apkDir — build gagal?")
            // Nama target mencerminkan kondisi asli: release tanpa signing -> suffix "-unsigned".
            val suffix = if (apk.name.contains("unsigned")) "-unsigned" else ""
            val target = File(outputDir, "$baseName-$buildType$suffix.apk")
            apk.copyTo(target, overwrite = true)
            println("  \u2714 $buildType: ${apk.name} -> ${target.relativeTo(projectRootDir)}")
        }

        println("Menyalin APK (debug + release) ke folder apk/...")
        copyApk("debug")
        copyApk("release")
        println("Selesai. APK tersedia di folder: ${outputDir.relativeTo(projectRootDir)}")
    }
}

dependencies {
    // Network — Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Room 3.0 — KSP only
    implementation(libs.room3.runtime)
    ksp(libs.room3.compiler)

    // DataStore + Tink
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.tink)
    implementation(libs.tink.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // App-level: Core UI & theme
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    // Coil
    implementation(libs.coil.compose)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.robolectric) // test DAO Room in-memory di JVM
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
}
