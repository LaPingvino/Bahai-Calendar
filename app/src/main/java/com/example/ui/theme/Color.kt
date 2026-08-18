package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val displayName: String,
    val description: String,
    val isDark: Boolean,
    val previewColor: Color,
    val previewBg: Color
) {
    ELEGANT_DARK(
        displayName = "Elegant Dark",
        description = "Obsidian canvas with radiant lavender highlights",
        isDark = true,
        previewColor = Color(0xFFD0BCFF),
        previewBg = Color(0xFF1C1B1F)
    ),
    LAPIS_GOLD(
        displayName = "Lapis & Radiant Gold",
        description = "Royal navy with radiant gold & amber accents",
        isDark = true,
        previewColor = Color(0xFFFFD54F),
        previewBg = Color(0xFF0D1B2A)
    ),
    PURE_IVORY_GOLD(
        displayName = "Warm Ivory & Gold",
        description = "Warm daylight parchment with regal gold & navy",
        isDark = false,
        previewColor = Color(0xFFC8963E),
        previewBg = Color(0xFFF9F7F2)
    ),
    EMERALD_NIGHT(
        displayName = "Spiritual Emerald",
        description = "Deep night teal with jade & amber glow",
        isDark = true,
        previewColor = Color(0xFF4DD0E1),
        previewBg = Color(0xFF0F2027)
    ),
    SYSTEM_DYNAMIC(
        displayName = "Material You Dynamic",
        description = "Colors dynamically adapted to your device wallpaper",
        isDark = true,
        previewColor = Color(0xFF81D4FA),
        previewBg = Color(0xFF212121)
    )
}

// 1. Elegant Dark Palette Tokens
val DarkBackground = Color(0xFF1C1B1F)
val DarkSurface = Color(0xFF25232A)
val DarkCardSurface = Color(0xFF49454F)
val DarkSurfaceVariant = Color(0xFF35333B)

val LavenderPrimary = Color(0xFFD0BCFF)
val LavenderOnPrimary = Color(0xFF381E72)
val LavenderContainer = Color(0xFF4F378B)
val OnLavenderContainer = Color(0xFFEADDFF)

val TextPrimary = Color(0xFFE6E1E5)
val TextSecondary = Color(0xFFCAC4D0)
val TextTertiary = Color(0xFF938F99)

val ActiveNavPill = Color(0xFF4A4458)
val BorderColor = Color(0xFF49454F)

// Accent badges
val HolyDayGold = Color(0xFFFFD54F)
val FeastTeal = Color(0xFF4DD0E1)
val FastRose = Color(0xFFFF8A80)
val AyyamIHaPurple = Color(0xFFD0BCFF)

// 2. Lapis & Radiant Gold Tokens
val LapisGoldBackground = Color(0xFF0A1118)
val LapisGoldSurface = Color(0xFF101C2A)
val LapisGoldCardSurface = Color(0xFF1A2B3D)
val GoldPrimary = Color(0xFFFFD54F)
val GoldOnPrimary = Color(0xFF3F2E00)
val GoldContainer = Color(0xFF533F00)
val OnGoldContainer = Color(0xFFFFE082)

// 3. Warm Ivory & Gold Tokens (Light Theme)
val IvoryBackground = Color(0xFFF9F7F2)
val IvorySurface = Color(0xFFFFFFFF)
val IvoryCardSurface = Color(0xFFEDE8DD)
val IvoryPrimary = Color(0xFFC8963E)
val IvoryOnPrimary = Color(0xFFFFFFFF)
val IvoryContainer = Color(0xFFFDE8BA)
val OnIvoryContainer = Color(0xFF3B2700)
val IvoryTextPrimary = Color(0xFF1C1B1A)
val IvoryTextSecondary = Color(0xFF49454E)
val IvoryBorder = Color(0xFFDDD7CD)

// 4. Spiritual Emerald Tokens
val EmeraldBackground = Color(0xFF0B171B)
val EmeraldSurface = Color(0xFF13252C)
val EmeraldCardSurface = Color(0xFF1D3740)
val EmeraldPrimary = Color(0xFF4DD0E1)
val EmeraldOnPrimary = Color(0xFF00363F)
val EmeraldContainer = Color(0xFF004D59)
val OnEmeraldContainer = Color(0xFFBAF0F8)
