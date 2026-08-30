# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable,InnerClasses,Signature,Exceptions,*Annotation*

# FFmpegKit (matches AAR consumer rules + full package keep)
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.smartexception.** { *; }
-dontwarn com.arthenica.**

-keep class com.arthenica.ffmpegkit.FFmpegKitConfig {
    native <methods>;
    void log(long, int, byte[]);
    void statistics(long, int, float, float, long, double, double, double);
    int safOpen(int);
    int safClose(int);
}

-keep class com.arthenica.ffmpegkit.AbiDetect {
    native <methods>;
}

-keep class com.arthenica.ffmpegkit.*Callback { *; }
-keep class com.arthenica.ffmpegkit.*Session { *; }

# Hilt / Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# Media3
-keep class androidx.media3.** { *; }

# Serialization / models used by reflection
-keep class com.videocompress.core.domain.model.** { *; }
-keep class com.videocompress.core.common.** { *; }
-keep class com.videocompress.core.data.local.** { *; }

# Native
-keepclasseswithmembernames class * {
    native <methods>;
}
