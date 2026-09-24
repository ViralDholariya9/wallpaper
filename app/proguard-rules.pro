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

# Keep Data Models
-keepclassmembers class com.parallax.wallpaper.model.** {
    <fields>;
    <methods>;
}

# Parallax Wallpaper Service & Sensor Engine
-keep class com.parallax.wallpaper.service.ParallaxWallpaperService { *; }
-keep class com.parallax.wallpaper.service.ParallaxWallpaperService$* { *; }
-keep class com.parallax.wallpaper.sensor.ParallaxSensorManager { *; }
