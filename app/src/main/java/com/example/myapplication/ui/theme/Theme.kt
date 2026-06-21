package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 🌙 薄荷深色方案（M3 Expressive）
private val DarkColorScheme = darkColorScheme(
    primary                = MintDarkPrimary,
    onPrimary              = MintDarkOnPrimary,
    primaryContainer       = MintDarkPrimaryContainer,
    onPrimaryContainer     = MintDarkOnPrimaryContainer,
    secondary              = MintDarkSecondary,
    onSecondary            = MintDarkOnSecondary,
    secondaryContainer     = MintDarkSecondaryContainer,
    onSecondaryContainer   = MintDarkOnSecondaryContainer,
    tertiary               = MintDarkTertiary,
    onTertiary             = MintDarkOnTertiary,
    tertiaryContainer      = MintDarkTertiaryContainer,
    onTertiaryContainer    = MintDarkOnTertiaryContainer,
    error                  = MintDarkError,
    onError                = MintDarkOnError,
    errorContainer         = MintDarkErrorContainer,
    onErrorContainer       = MintDarkOnErrorContainer,
    background             = MintDarkBackground,
    onBackground           = MintDarkOnBackground,
    surface                = MintDarkSurface,
    onSurface              = MintDarkOnSurface,
    surfaceVariant         = MintDarkSurfaceVariant,
    onSurfaceVariant       = MintDarkOnSurfaceVariant,
    surfaceContainer       = MintDarkSurfaceContainer,
    surfaceContainerHigh   = MintDarkSurfaceContainerHigh,
    surfaceContainerHighest= MintDarkSurfaceContainerHighest,
    outline                = MintDarkOutline,
    outlineVariant         = MintDarkOutlineVariant
)

// ☀️ 薄荷浅色方案（M3 Expressive）
private val LightColorScheme = lightColorScheme(
    primary                = MintLightPrimary,
    onPrimary              = MintLightOnPrimary,
    primaryContainer       = MintLightPrimaryContainer,
    onPrimaryContainer     = MintLightOnPrimaryContainer,
    secondary              = MintLightSecondary,
    onSecondary            = MintLightOnSecondary,
    secondaryContainer     = MintLightSecondaryContainer,
    onSecondaryContainer   = MintLightOnSecondaryContainer,
    tertiary               = MintLightTertiary,
    onTertiary             = MintLightOnTertiary,
    tertiaryContainer      = MintLightTertiaryContainer,
    onTertiaryContainer    = MintLightOnTertiaryContainer,
    error                  = MintLightError,
    onError                = MintLightOnError,
    errorContainer         = MintLightErrorContainer,
    onErrorContainer       = MintLightOnErrorContainer,
    background             = MintLightBackground,
    onBackground           = MintLightOnBackground,
    surface                = MintLightSurface,
    onSurface              = MintLightOnSurface,
    surfaceVariant         = MintLightSurfaceVariant,
    onSurfaceVariant       = MintLightOnSurfaceVariant,
    surfaceContainer       = MintLightSurfaceContainer,
    surfaceContainerHigh   = MintLightSurfaceContainerHigh,
    surfaceContainerHighest= MintLightSurfaceContainerHighest,
    outline                = MintLightOutline,
    outlineVariant         = MintLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
