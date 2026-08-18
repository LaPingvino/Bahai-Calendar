package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Elegant Dark Color Scheme
private val ElegantDarkColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = OnLavenderContainer,
    secondary = LavenderPrimary,
    onSecondary = LavenderOnPrimary,
    secondaryContainer = DarkCardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = HolyDayGold,
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = DarkCardSurface,
    onTertiaryContainer = HolyDayGold,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

// 2. Lapis & Radiant Gold Color Scheme
private val LapisGoldColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003258),
    secondaryContainer = LapisGoldCardSurface,
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = FeastTeal,
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = LapisGoldCardSurface,
    onTertiaryContainer = FeastTeal,
    background = LapisGoldBackground,
    onBackground = TextPrimary,
    surface = LapisGoldSurface,
    onSurface = TextPrimary,
    surfaceVariant = LapisGoldCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF27394E)
)

// 3. Warm Ivory & Gold Color Scheme (Light Theme)
private val WarmIvoryColorScheme = lightColorScheme(
    primary = IvoryPrimary,
    onPrimary = IvoryOnPrimary,
    primaryContainer = IvoryContainer,
    onPrimaryContainer = OnIvoryContainer,
    secondary = Color(0xFF1B3B6F),
    onSecondary = Color.White,
    secondaryContainer = IvoryCardSurface,
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = Color(0xFF007788),
    onTertiary = Color.White,
    tertiaryContainer = IvoryCardSurface,
    onTertiaryContainer = Color(0xFF002026),
    background = IvoryBackground,
    onBackground = IvoryTextPrimary,
    surface = IvorySurface,
    onSurface = IvoryTextPrimary,
    surfaceVariant = IvoryCardSurface,
    onSurfaceVariant = IvoryTextSecondary,
    outline = IvoryBorder
)

// 4. Spiritual Emerald Color Scheme
private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = HolyDayGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = EmeraldCardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = EmeraldCardSurface,
    onTertiaryContainer = Color(0xFF80CBC4),
    background = EmeraldBackground,
    onBackground = TextPrimary,
    surface = EmeraldSurface,
    onSurface = TextPrimary,
    surfaceVariant = EmeraldCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF25444E)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.ELEGANT_DARK,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.ELEGANT_DARK -> ElegantDarkColorScheme
        AppThemeMode.LAPIS_GOLD -> LapisGoldColorScheme
        AppThemeMode.PURE_IVORY_GOLD -> WarmIvoryColorScheme
        AppThemeMode.EMERALD_NIGHT -> EmeraldColorScheme
        AppThemeMode.SYSTEM_DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemInDark) ElegantDarkColorScheme else WarmIvoryColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
