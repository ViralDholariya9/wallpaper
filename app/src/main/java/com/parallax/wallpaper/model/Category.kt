package com.parallax.wallpaper.model

import androidx.compose.ui.graphics.Color

/**
 * High-level visual grouping for organizing categories cleanly
 */
enum class CategoryGroup(
    val title: String,
    val gujaratiTitle: String,
    val icon: String
) {
    FEATURED(
        title = "Trending & Featured",
        gujaratiTitle = "ટ્રેન્ડિંગ & ખાસ",
        icon = "🔥"
    ),
    AESTHETICS(
        title = "Art & Styles",
        gujaratiTitle = "આર્ટ & સ્ટાઈલ્સ",
        icon = "🎨"
    ),
    NATURE_COSMOS(
        title = "Universe & Nature",
        gujaratiTitle = "અંતરિક્ષ & પ્રકૃતિ",
        icon = "🌌"
    ),
    PASSIONS(
        title = "Passions & Themes",
        gujaratiTitle = "થીમ્સ & પેકેજ",
        icon = "⚡"
    )
}

/**
 * Rich wallpaper category model with modern metadata for a world-class UI experience.
 * Fully backwards compatible with existing Category enum usage.
 */
enum class Category(
    val displayName: String,
    val gujaratiName: String,
    val emoji: String,
    val subtitle: String,
    val group: CategoryGroup,
    val primaryColor: Long,
    val secondaryColor: Long,
    val badge: String? = null
) {
    ALL(
        displayName = "All",
        gujaratiName = "બધા વૉલપેપર્સ",
        emoji = "🌟",
        subtitle = "Every 4K, 3D & Live design",
        group = CategoryGroup.FEATURED,
        primaryColor = 0xFF7928CA,
        secondaryColor = 0xFF00DFD8,
        badge = "ALL"
    ),
    PARALLAX_3D(
        displayName = "3D Parallax",
        gujaratiName = "3D પેરાલેક્સ",
        emoji = "🌀",
        subtitle = "Multi-layer gyro motion depth",
        group = CategoryGroup.FEATURED,
        primaryColor = 0xFF00E5FF,
        secondaryColor = 0xFF7928CA,
        badge = "HOT"
    ),
    AMOLED(
        displayName = "AMOLED",
        gujaratiName = "એમોલેડ ડાર્ક",
        emoji = "🖤",
        subtitle = "Deep #000000 true pitch black",
        group = CategoryGroup.FEATURED,
        primaryColor = 0xFF1F1C2C,
        secondaryColor = 0xFF928DAB,
        badge = "BATTERY"
    ),
    CYBERPUNK(
        displayName = "Cyberpunk",
        gujaratiName = "સાયબરપંક",
        emoji = "⚡",
        subtitle = "Futuristic neon cities & glitch",
        group = CategoryGroup.FEATURED,
        primaryColor = 0xFFFF007F,
        secondaryColor = 0xFF00F0FF,
        badge = "TREND"
    ),
    SPACE(
        displayName = "Space & Cosmos",
        gujaratiName = "અંતરિક્ષ & બ્રહ્માંડ",
        emoji = "🌌",
        subtitle = "Galaxies, nebulae & cosmic stars",
        group = CategoryGroup.NATURE_COSMOS,
        primaryColor = 0xFF2E0854,
        secondaryColor = 0xFF4A00E0,
        badge = "4K"
    ),
    NATURE(
        displayName = "Nature",
        gujaratiName = "પ્રકૃતિ & ધોધ",
        emoji = "🌿",
        subtitle = "Scenic waterfalls, trees & calm",
        group = CategoryGroup.NATURE_COSMOS,
        primaryColor = 0xFF0F9B0F,
        secondaryColor = 0xFF004D40,
        badge = null
    ),
    ANIME(
        displayName = "Anime",
        gujaratiName = "એનિમે & કાર્ટૂન",
        emoji = "⛩️",
        subtitle = "Epic shonen & artistic anime",
        group = CategoryGroup.AESTHETICS,
        primaryColor = 0xFFFF416C,
        secondaryColor = 0xFFFF4B2B,
        badge = "POPULAR"
    ),
    MINIMAL(
        displayName = "Minimal",
        gujaratiName = "મિનિમલ & ક્લીન",
        emoji = "📐",
        subtitle = "Clean vector lines & pastel tones",
        group = CategoryGroup.AESTHETICS,
        primaryColor = 0xFF3A6073,
        secondaryColor = 0xFF16222F,
        badge = null
    ),
    ABSTRACT(
        displayName = "Abstract & 4D",
        gujaratiName = "એબ્સ્ટ્રેક્ટ & 4D",
        emoji = "🔮",
        subtitle = "Fluid gradients & geometric shapes",
        group = CategoryGroup.AESTHETICS,
        primaryColor = 0xFF8A2387,
        secondaryColor = 0xFFE94057,
        badge = "4D"
    ),
    CARS(
        displayName = "Cars & Speed",
        gujaratiName = "સુપરકાર્સ & બાઇક્સ",
        emoji = "🏎️",
        subtitle = "Hypercars, drift & sports tracks",
        group = CategoryGroup.PASSIONS,
        primaryColor = 0xFFFF512F,
        secondaryColor = 0xFFDD2476,
        badge = "PRO"
    ),
    NEON(
        displayName = "Neon Glow",
        gujaratiName = "નિયોન ગ્લો",
        emoji = "✨",
        subtitle = "Vibrant synthwave & laser lights",
        group = CategoryGroup.AESTHETICS,
        primaryColor = 0xFF00F2FE,
        secondaryColor = 0xFF4FACFE,
        badge = "GLOW"
    ),
    SPIRITUAL(
        displayName = "Devotional",
        gujaratiName = "ભક્તિ & આસ્થા",
        emoji = "🕉️",
        subtitle = "Sacred deities, temples & blessings",
        group = CategoryGroup.PASSIONS,
        primaryColor = 0xFFFF8008,
        secondaryColor = 0xFFFFC837,
        badge = "DIVINE"
    ),
    GAMING(
        displayName = "Gaming",
        gujaratiName = "ગેમિંગ & સાય-ફાઇ",
        emoji = "🎮",
        subtitle = "Esports arenas & legendary games",
        group = CategoryGroup.PASSIONS,
        primaryColor = 0xFF11998E,
        secondaryColor = 0xFF38EF7D,
        badge = "NEW"
    );

    val primaryComposeColor: Color
        get() = Color(primaryColor)

    val secondaryComposeColor: Color
        get() = Color(secondaryColor)
}
