# RSUD Ajibarang App ProGuard / R8 Rules

# ── Kotlin Serialization ──
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class my.id.kentoes.rsudajibarangapp.**$$serializer { *; }
-keepclassmembers class my.id.kentoes.rsudajibarangapp.** {
    *** Companion;
}
-keepclasseswithmembers class my.id.kentoes.rsudajibarangapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Room 3.0 ──
-keep class * extends androidx.room3.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room3.paging.**

# ── Hilt / Dagger ──
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Retrofit + OkHttp ──
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*Response
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*Request
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*Dto
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*Out
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*In
-keep,allowobfuscation class my.id.kentoes.rsudajibarangapp.**.api.*Submit

# ── Coil ──
-keep class coil.** { *; }
-dontwarn coil.**

# ── WorkManager ──
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# ── Kotlin Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
