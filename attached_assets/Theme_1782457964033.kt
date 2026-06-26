package com.scripthub.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// 🌙 Fluent 深色方案
private val DarkColorScheme = darkColorScheme(
    primary                = FluentDarkPrimary,
    onPrimary              = FluentDarkOnPrimary,
    primaryContainer       = FluentDarkPrimaryContainer,
    onPrimaryContainer     = FluentDarkOnPrimaryContainer,
    secondary              = FluentDarkSecondary,
    onSecondary            = FluentDarkOnSecondary,
    secondaryContainer     = FluentDarkSecondaryContainer,
    onSecondaryContainer   = FluentDarkOnSecondaryContainer,
    tertiary               = FluentDarkTertiary,
    onTertiary             = FluentDarkOnTertiary,
    tertiaryContainer      = FluentDarkTertiaryContainer,
    onTertiaryContainer    = FluentDarkOnTertiaryContainer,
    error                  = FluentDarkError,
    onError                = FluentDarkOnError,
    errorContainer         = FluentDarkErrorContainer,
    onErrorContainer       = FluentDarkOnErrorContainer,
    background             = FluentDarkBackground,
    onBackground           = FluentDarkOnBackground,
    surface                = FluentDarkSurface,
    onSurface              = FluentDarkOnSurface,
    surfaceVariant         = FluentDarkSurfaceVariant,
    onSurfaceVariant       = FluentDarkOnSurfaceVariant,
    surfaceContainer       = FluentDarkSurfaceContainer,
    surfaceContainerHigh   = FluentDarkSurfaceContainerHigh,
    surfaceContainerHighest= FluentDarkSurfaceContainerHighest,
    outline                = FluentDarkOutline,
    outlineVariant         = FluentDarkOutlineVariant
)

// ☀️ Fluent 浅色方案
private val LightColorScheme = lightColorScheme(
    primary                = FluentLightPrimary,
    onPrimary              = FluentLightOnPrimary,
    primaryContainer       = FluentLightPrimaryContainer,
    onPrimaryContainer     = FluentLightOnPrimaryContainer,
    secondary              = FluentLightSecondary,
    onSecondary            = FluentLightOnSecondary,
    secondaryContainer     = FluentLightSecondaryContainer,
    onSecondaryContainer   = FluentLightOnSecondaryContainer,
    tertiary               = FluentLightTertiary,
    onTertiary             = FluentLightOnTertiary,
    tertiaryContainer      = FluentLightTertiaryContainer,
    onTertiaryContainer    = FluentLightOnTertiaryContainer,
    error                  = FluentLightError,
    onError                = FluentLightOnError,
    errorContainer         = FluentLightErrorContainer,
    onErrorContainer       = FluentLightOnErrorContainer,
    background             = FluentLightBackground,
    onBackground           = FluentLightOnBackground,
    surface                = FluentLightSurface,
    onSurface              = FluentLightOnSurface,
    surfaceVariant         = FluentLightSurfaceVariant,
    onSurfaceVariant       = FluentLightOnSurfaceVariant,
    surfaceContainer       = FluentLightSurfaceContainer,
    surfaceContainerHigh   = FluentLightSurfaceContainerHigh,
    surfaceContainerHighest= FluentLightSurfaceContainerHighest,
    outline                = FluentLightOutline,
    outlineVariant         = FluentLightOutlineVariant
)

// 📐 Fluent 形状语言：圆角更克制（4 / 8 / 12dp），区别于 M3 Expressive 的大圆角与不对称切角
private val FluentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(6.dp),
    medium     = RoundedCornerShape(8.dp),
    large      = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun ScriptHubTheme(
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
        shapes = FluentShapes,
        content = content
    )
}
