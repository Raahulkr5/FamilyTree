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

private val DarkColorScheme =
  darkColorScheme(
    primary = HeritagePrimaryDark,
    secondary = HeritageSecondaryDark,
    tertiary = HeritageTertiaryDark,
    background = HeritageBackgroundDark,
    surface = HeritageSurfaceDark,
    onPrimary = HeritageOnPrimaryDark,
    onSecondary = HeritageOnSecondaryDark,
    onSurface = HeritageOnSurfaceDark,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF443F4A)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HeritagePrimaryLight,
    secondary = HeritageSecondaryLight,
    tertiary = HeritageTertiaryLight,
    background = HeritageBackgroundLight,
    surface = HeritageSurfaceLight,
    onPrimary = HeritageOnPrimaryLight,
    onSecondary = HeritageOnSecondaryLight,
    onSurface = HeritageOnSurfaceLight,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),
    outlineVariant = Color(0xFFE7E0EC)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Branded focus: Set dynamic color to false by default for accurate heritage palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
