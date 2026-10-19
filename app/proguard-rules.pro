# Google Play Console / R8 ProGuard Configuration for Parallax Wallpaper App

# Jetpack Compose Rules
-keepattributes *Annotation*
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Data Models & API Models (Essential for Gson deserialization)
-keepclassmembers class com.parallax.wallpaper.model.** {
    <fields>;
    <methods>;
}
-keep class com.parallax.wallpaper.model.** { *; }
-keep class com.parallax.wallpaper.data.api.** { *; }
-keep class com.parallax.wallpaper.ads.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson & Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }

# Google Play Services & AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**
-keep public class com.google.ads.** {
   public *;
}
-dontwarn com.google.ads.**
-keep public class com.google.android.ump.** {
   public *;
}
-dontwarn com.google.android.ump.**

# Google Play Core (In-App Review & In-App Update)
-keep class com.google.android.play.core.** { *; }

# Parallax Wallpaper Service & Sensor Engine
-keep class com.parallax.wallpaper.service.ParallaxWallpaperService { *; }
-keep class com.parallax.wallpaper.service.ParallaxWallpaperService$* { *; }
-keep class com.parallax.wallpaper.sensor.ParallaxSensorManager { *; }
-keep class com.parallax.wallpaper.MainActivity { *; }
