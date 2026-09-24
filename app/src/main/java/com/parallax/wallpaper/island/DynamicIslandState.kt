package com.parallax.wallpaper.island

/**
 * Camera punch hole alignment on Android devices.
 */
enum class CameraHolePosition {
    CENTER,
    LEFT,
    RIGHT
}

/**
 * Live smart event displayed inside the Dynamic Island capsule.
 */
sealed class IslandEvent {
    /**
     * Compact pill state around the camera hole
     */
    data object Compact : IslandEvent()

    /**
     * Charging connected event (percentage, fast charge wattage)
     */
    data class Charging(
        val batteryPercent: Int = 88,
        val wattage: String = "66W SuperDart",
        val isFastCharge: Boolean = true
    ) : IslandEvent()

    /**
     * Music playback event (Spotify, YT Music, local player)
     */
    data class Music(
        val trackTitle: String = "Starboy",
        val artistName: String = "The Weeknd",
        val isPlaying: Boolean = true
    ) : IslandEvent()

    /**
     * Bluetooth audio earbuds connection alert
     */
    data class Earbuds(
        val deviceName: String = "AirPods Pro 2",
        val leftBattery: Int = 95,
        val rightBattery: Int = 92,
        val caseBattery: Int = 80
    ) : IslandEvent()

    /**
     * Incoming Phone call or WhatsApp call
     */
    data class IncomingCall(
        val callerName: String = "Elon Musk",
        val callerLabel: String = "Incoming Mobile Call"
    ) : IslandEvent()

    /**
     * WhatsApp / Telegram / Message quick alert
     */
    data class NotificationAlert(
        val sender: String = "WhatsApp",
        val message: String = "Hey, check out these new 4K 3D wallpapers! 🔥"
    ) : IslandEvent()
}
