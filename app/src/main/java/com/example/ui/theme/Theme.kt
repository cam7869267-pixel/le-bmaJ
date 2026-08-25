package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeStyle

private val ObsidianKimiScheme = darkColorScheme(
    primary = KimiPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFF66FFC2),
    secondary = KimiSecondary,
    onSecondary = Color(0xFF003544),
    tertiary = KimiAccent,
    background = KimiBackground,
    onBackground = KimiOnSurface,
    surface = KimiSurface,
    onSurface = KimiOnSurface,
    surfaceVariant = KimiSurfaceVariant,
    onSurfaceVariant = KimiOnSurfaceVariant,
    outline = KimiBorder
)

private val TitaniumCyberScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004F55),
    onPrimaryContainer = Color(0xFF80F8FF),
    secondary = CyberSecondary,
    onSecondary = Color(0xFF003062),
    tertiary = CyberAccent,
    background = CyberBackground,
    onBackground = CyberOnSurface,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceVariant,
    outline = CyberBorder
)

private val HoloVioletScheme = darkColorScheme(
    primary = HoloPrimary,
    onPrimary = Color(0xFF38006B),
    primaryContainer = Color(0xFF55009E),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = HoloSecondary,
    onSecondary = Color(0xFF490024),
    tertiary = HoloAccent,
    background = HoloBackground,
    onBackground = HoloOnSurface,
    surface = HoloSurface,
    onSurface = HoloOnSurface,
    surfaceVariant = HoloSurfaceVariant,
    onSurfaceVariant = HoloOnSurfaceVariant,
    outline = HoloBorder
)

private val PaperLightScheme = lightColorScheme(
    primary = PaperPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = PaperSecondary,
    onSecondary = Color.White,
    tertiary = PaperAccent,
    background = PaperBackground,
    onBackground = PaperOnSurface,
    surface = PaperSurface,
    onSurface = PaperOnSurface,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = PaperOnSurfaceVariant,
    outline = PaperBorder
)

private val SolarAmberScheme = darkColorScheme(
    primary = SolarPrimary,
    onPrimary = Color(0xFF452B00),
    primaryContainer = Color(0xFF633F00),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = SolarSecondary,
    onSecondary = Color(0xFF492300),
    tertiary = SolarAccent,
    background = SolarBackground,
    onBackground = SolarOnSurface,
    surface = SolarSurface,
    onSurface = SolarOnSurface,
    surfaceVariant = SolarSurfaceVariant,
    onSurfaceVariant = SolarOnSurfaceVariant,
    outline = SolarBorder
)

@Composable
fun JumblePTheme(
    themeStyle: ThemeStyle = ThemeStyle.OBSIDIAN_KIMI,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        ThemeStyle.OBSIDIAN_KIMI -> ObsidianKimiScheme
        ThemeStyle.TITANIUM_CYBER -> TitaniumCyberScheme
        ThemeStyle.HOLOGRAPHIC_VIOLET -> HoloVioletScheme
        ThemeStyle.PAPER_LIGHT -> PaperLightScheme
        ThemeStyle.SOLAR_AMBER -> SolarAmberScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JumblePTheme(ThemeStyle.OBSIDIAN_KIMI, content)
}
