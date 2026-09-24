package com.parallax.wallpaper.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPurple

data class ExtractedPalette(
    val primary: ComposeColor,
    val secondary: ComposeColor,
    val surface: ComposeColor
)

object PaletteHelper {

    /**
     * Samples pixels from the bitmap to extract vibrant dominant colors for Material You dynamic theming.
     */
    fun extractDominantColors(bitmap: Bitmap): ExtractedPalette {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val step = (width * height / 300).coerceAtLeast(1)

            var bestVibrantColor: Int? = null
            var bestSaturation = 0f

            var bestSecondaryColor: Int? = null
            var secondBestSaturation = 0f

            val hsv = FloatArray(3)

            for (y in 0 until height step (height / 20).coerceAtLeast(1)) {
                for (x in 0 until width step (width / 20).coerceAtLeast(1)) {
                    val pixel = bitmap.getPixel(x, y)
                    Color.colorToHSV(pixel, hsv)
                    val saturation = hsv[1]
                    val value = hsv[2]

                    // We look for colors with vibrant saturation and decent brightness
                    if (saturation > 0.45f && value > 0.35f) {
                        if (saturation > bestSaturation) {
                            bestSecondaryColor = bestVibrantColor
                            secondBestSaturation = bestSaturation
                            bestVibrantColor = pixel
                            bestSaturation = saturation
                        } else if (saturation > secondBestSaturation) {
                            bestSecondaryColor = pixel
                            secondBestSaturation = saturation
                        }
                    }
                }
            }

            val primary = bestVibrantColor?.let { ComposeColor(it) } ?: NeonCyan
            val secondary = bestSecondaryColor?.let { ComposeColor(it) } ?: NeonPurple
            val surface = ComposeColor(0xFF080912)

            return ExtractedPalette(primary = primary, secondary = secondary, surface = surface)
        } catch (e: Exception) {
            return ExtractedPalette(primary = NeonCyan, secondary = NeonPurple, surface = ComposeColor(0xFF080912))
        }
    }
}
