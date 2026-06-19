package com.example.myapplication.ui.theme

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

// 🌙 深色配置映射（方案二：元气潮酷 - 完美重制版）
private val DarkColorScheme = darkColorScheme(
    primary = NatureDarkPrimary,
    secondary = NatureDarkSecondary,
    tertiary = NatureDarkTertiary,
    background = NatureDarkBackground,
    surfaceContainer = NatureDarkSurfaceContainer,
    onSurface = NatureDarkOnSurface,
    onPrimary = NatureDarkBackground,

    // 📌 重点修复：接管深色模式下的胶囊底色和选中的图标色
    secondaryContainer = Color(0xFF1E3A8A),       // 深邃的高级暗蓝胶囊底
    onSecondaryContainer = Color(0xFF60A5FA),     // 胶囊内部选中的图标变回天空蓝
)

// ☀️ 浅色配置映射（方案二：元气潮酷 - 完美重制版）
private val LightColorScheme = lightColorScheme(
    primary = NatureLightPrimary,
    secondary = NatureLightSecondary,
    tertiary = NatureLightTertiary,
    background = NatureLightBackground,
    surfaceContainer = NatureLightSurfaceContainer,
    onSurface = NatureLightOnSurface,
    onPrimary = Color.White,

    // 📌 重点修复：接管浅色模式下的胶囊底色和选中的图标色
    secondaryContainer = Color(0xFFDBEAFE),       // 极其清爽的淡蓝色胶囊底
    onSecondaryContainer = NatureLightPrimary,      // 胶囊内部选中的图标采用克莱因蓝
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
