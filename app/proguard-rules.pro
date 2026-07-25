# RSUD Ajibarang App ProGuard / R8 Rules
# Add ProGuard rules here for serialization/reflection classes

# Kotlin Serialization
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

# Room
-keep class * extends androidx.room3.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room3.paging.**

# Hilt / Dagger
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
