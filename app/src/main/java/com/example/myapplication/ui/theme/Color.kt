package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// ====================================================================================
// ☀️ 薄荷浅色调色盘（M3 Tonal Palette · Seed: #00C9A7）
// ====================================================================================

// Primary（深薄荷）
val MintLightPrimary              = Color(0xFF006A5B)
val MintLightOnPrimary            = Color(0xFFFFFFFF)
val MintLightPrimaryContainer     = Color(0xFF78F8DC)
val MintLightOnPrimaryContainer   = Color(0xFF00201A)

// Secondary（沉静青）
val MintLightSecondary            = Color(0xFF4A635D)
val MintLightOnSecondary          = Color(0xFFFFFFFF)
val MintLightSecondaryContainer   = Color(0xFFCCE8E1)
val MintLightOnSecondaryContainer = Color(0xFF06201A)

// Tertiary（蓝调点缀）
val MintLightTertiary             = Color(0xFF426277)
val MintLightOnTertiary           = Color(0xFFFFFFFF)
val MintLightTertiaryContainer    = Color(0xFFC6E7FF)
val MintLightOnTertiaryContainer  = Color(0xFF001E2D)

// Error
val MintLightError                = Color(0xFFBA1A1A)
val MintLightOnError              = Color(0xFFFFFFFF)
val MintLightErrorContainer       = Color(0xFFFFDAD6)
val MintLightOnErrorContainer     = Color(0xFF410002)

// Background & Surface
val MintLightBackground           = Color(0xFFF4FBF8)
val MintLightOnBackground         = Color(0xFF161D1B)
val MintLightSurface              = Color(0xFFF4FBF8)
val MintLightOnSurface            = Color(0xFF161D1B)
val MintLightSurfaceVariant       = Color(0xFFDBE5E0)
val MintLightOnSurfaceVariant     = Color(0xFF3F4945)
val MintLightSurfaceContainer     = Color(0xFFE8F2EE)
val MintLightSurfaceContainerHigh = Color(0xFFE2ECE8)
val MintLightSurfaceContainerHighest = Color(0xFFDCE6E2)

// Outline
val MintLightOutline              = Color(0xFF6F7975)
val MintLightOutlineVariant       = Color(0xFFBFC9C4)

// ====================================================================================
// 🌙 薄荷深色调色盘（M3 Tonal Palette · Dark）
// ====================================================================================

// Primary（亮薄荷）
val MintDarkPrimary               = Color(0xFF59DBB9)
val MintDarkOnPrimary             = Color(0xFF00382E)
val MintDarkPrimaryContainer      = Color(0xFF005144)
val MintDarkOnPrimaryContainer    = Color(0xFF78F8DC)

// Secondary（柔青）
val MintDarkSecondary             = Color(0xFFB1CCC6)
val MintDarkOnSecondary           = Color(0xFF1C352F)
val MintDarkSecondaryContainer    = Color(0xFF334B45)
val MintDarkOnSecondaryContainer  = Color(0xFFCCE8E1)

// Tertiary（蓝调点缀）
val MintDarkTertiary              = Color(0xFFA9CBE3)
val MintDarkOnTertiary            = Color(0xFF0F3447)
val MintDarkTertiaryContainer     = Color(0xFF294B5F)
val MintDarkOnTertiaryContainer   = Color(0xFFC6E7FF)

// Error
val MintDarkError                 = Color(0xFFFFB4AB)
val MintDarkOnError               = Color(0xFF690005)
val MintDarkErrorContainer        = Color(0xFF93000A)
val MintDarkOnErrorContainer      = Color(0xFFFFDAD6)

// Background & Surface
val MintDarkBackground            = Color(0xFF0E1512)
val MintDarkOnBackground          = Color(0xFFDCE5E0)
val MintDarkSurface               = Color(0xFF0E1512)
val MintDarkOnSurface             = Color(0xFFDCE5E0)
val MintDarkSurfaceVariant        = Color(0xFF3F4945)
val MintDarkOnSurfaceVariant      = Color(0xFFBFC9C4)
val MintDarkSurfaceContainer      = Color(0xFF1A2420)
val MintDarkSurfaceContainerHigh  = Color(0xFF252F2C)
val MintDarkSurfaceContainerHighest = Color(0xFF2F3937)

// Outline
val MintDarkOutline               = Color(0xFF899390)
val MintDarkOutlineVariant        = Color(0xFF3F4945)

// ====================================================================================
// 🎨 语义调色板（终端日志 / 状态徽章 · 与主题色系协调）
// ====================================================================================

// 终端 INFO 色（薄荷高亮蓝绿）
val TerminalInfo    = Color(0xFF5EEAD4)
// 终端 SUCCESS 色（清新绿）
val TerminalSuccess = Color(0xFF4ADE80)
// 终端 ERROR 色（暖红）
val TerminalError   = Color(0xFFF87171)
// 终端 WARN 色（琥珀）
val TerminalWarn    = Color(0xFFEAB308)
// 终端 EXEC 色（薰衣草紫）
val TerminalExec    = Color(0xFFA78BFA)

// 运行中状态绿点
val StatusRunning   = Color(0xFF4ADE80)

// 代码编辑器专属深色（保留极客感，换为薄荷暗调）
val EditorBackground    = Color(0xFF0A120F)
val EditorSurface       = Color(0xFF050D0B)
val EditorTextColor     = TerminalInfo
val TerminalSheetBg     = Color(0xFF0A120F)

// 脚本类型徽章颜色（语义固定，不随主题翻转）
val TypeColorPython = Color(0xFF5EEAD4)   // 薄荷青（与主题呼应）
val TypeColorShell  = Color(0xFF4ADE80)   // 绿
val TypeColorNode   = Color(0xFFA78BFA)   // 紫
val TypeColorOther  = Color(0xFF94A3B8)   // 钢灰

// 日志卡片（深色，浅/深模式共用）
val LogCardBg           = Color(0xFF111A17)
val LogCardHeaderText   = Color(0xFF8DA8A0)
val LogCardMutedText    = Color(0xFF4A6059)
