package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Option1GoldDark =
  darkColorScheme(
    primary = Color(0xFFFFD700), // Royal Gold
    secondary = Color(0xFFDFB43F), // Antique Gold
    tertiary = Color(0xFFC5A039),
    background = Color(0xFF110F0A), // Rich Warm Charcoal Obsidian
    surface = Color(0xFF1C1A14), // Warm dark slate card
    onPrimary = Color(0xFF1C1300),
    onSecondary = Color(0xFF1C1300),
    onSurface = Color(0xFFF6F0E2), // Parchment text
    primaryContainer = Color(0xFF3D2E00),
    onPrimaryContainer = Color(0xFFFFE59E),
    secondaryContainer = Color(0xFF2B2414),
    onSecondaryContainer = Color(0xFFE8E0D1),
    surfaceVariant = Color(0xFF28251C),
    onSurfaceVariant = Color(0xFFDFDACF),
    outline = Color(0xFF9E9580)
  )

private val Option1GoldLight =
  lightColorScheme(
    primary = Color(0xFF785B00), // Rich imperial bronze
    secondary = Color(0xFF6B5A2F),
    tertiary = Color(0xFF556500),
    background = Color(0xFFFFFBF4), // Pure Warm Cream Parchment
    surface = Color(0xFFF8F1E4), // Ivory Card base
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1B15),
    primaryContainer = Color(0xFFFFE082),
    onPrimaryContainer = Color(0xFF241A00),
    secondaryContainer = Color(0xFFF5DEC2),
    onSecondaryContainer = Color(0xFF241A00),
    surfaceVariant = Color(0xFFECE1CF),
    onSurfaceVariant = Color(0xFF4D4639),
    outline = Color(0xFF7E7667)
  )

private val Option2CosmicDark =
  darkColorScheme(
    primary = Color(0xFF00FFCC), // Holographic Neon Turquoise
    secondary = Color(0xFFBD00FF), // Cyber Neon Violet / Orchid
    tertiary = Color(0xFFFF1493), // Cyber Pink
    background = Color(0xFF050312), // Deep Interstellar Space Abyss
    surface = Color(0xFF100A2C), // Translucent Space Nebula Purple Card
    onPrimary = Color(0xFF00382C),
    onSecondary = Color(0xFFFFFFFF),
    onSurface = Color(0xFFE2E0FF), // Soft Starlight blue-gray
    primaryContainer = Color(0xFF004D3F),
    onPrimaryContainer = Color(0xFF9EFFF0),
    secondaryContainer = Color(0xFF3E1256),
    onSecondaryContainer = Color(0xFFF5D6FF),
    surfaceVariant = Color(0xFF1B143D),
    onSurfaceVariant = Color(0xFFD4CCFB),
    outline = Color(0xFF867EAE)
  )

private val Option2CosmicLight =
  lightColorScheme(
    primary = Color(0xFF008272), // Rich Jade Teal
    secondary = Color(0xFF8100B8), // Royal Purple Orchid
    tertiary = Color(0xFFB01D70),
    background = Color(0xFFF6F3FB), // Clear Light Aurora Lavender Base
    surface = Color(0xFFECE4F6), // Clean sky lavender card base
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1523),
    primaryContainer = Color(0xFF99F5E6),
    onPrimaryContainer = Color(0xFF00201B),
    secondaryContainer = Color(0xFFF5D6FF),
    onSecondaryContainer = Color(0xFF2A0041),
    surfaceVariant = Color(0xFFE9DCF2),
    onSurfaceVariant = Color(0xFF4B4352),
    outline = Color(0xFF7C7483)
  )

@Composable
fun MyApplicationTheme(
  themeOption: Int = 1,
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      themeOption == 1 -> {
        if (darkTheme) Option1GoldDark else Option1GoldLight
      }
      themeOption == 2 -> {
        if (darkTheme) Option2CosmicDark else Option2CosmicLight
      }
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> Option1GoldDark
      else -> Option1GoldLight
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
