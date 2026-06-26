package com.scripthub.app.ui.theme

import androidx.compose.ui.graphics.Color

// ====================================================================================
// ☀️ Fluent 浅色调色盘（致敬 Microsoft 系应用 · Accent: #0067C0 通信蓝）
// ====================================================================================

// Primary（Microsoft 蓝）
val FluentLightPrimary              = Color(0xFF0067C0)
val FluentLightOnPrimary            = Color(0xFFFFFFFF)
val FluentLightPrimaryContainer     = Color(0xFFD3E4FA)
val FluentLightOnPrimaryContainer   = Color(0xFF001C38)

// Secondary（中性蓝灰，用于次级控件 / 描边按钮）
val FluentLightSecondary            = Color(0xFF5B6471)
val FluentLightOnSecondary          = Color(0xFFFFFFFF)
val FluentLightSecondaryContainer   = Color(0xFFE4E6EA)
val FluentLightOnSecondaryContainer = Color(0xFF1A1F27)

// Tertiary（Microsoft 紫，呼应 Teams / OneNote 强调色）
val FluentLightTertiary             = Color(0xFF744DA9)
val FluentLightOnTertiary           = Color(0xFFFFFFFF)
val FluentLightTertiaryContainer    = Color(0xFFEADDFB)
val FluentLightOnTertiaryContainer  = Color(0xFF2A0A4D)

// Error（Fluent 语义红）
val FluentLightError                = Color(0xFFC42B1C)
val FluentLightOnError              = Color(0xFFFFFFFF)
val FluentLightErrorContainer       = Color(0xFFFFDAD4)
val FluentLightOnErrorContainer     = Color(0xFF410E0B)

// Background & Surface（接近纯白的 Fluent Layer 背景）
val FluentLightBackground           = Color(0xFFFAFAFA)
val FluentLightOnBackground         = Color(0xFF1B1B1B)
val FluentLightSurface              = Color(0xFFFFFFFF)
val FluentLightOnSurface            = Color(0xFF1B1B1B)
val FluentLightSurfaceVariant       = Color(0xFFF0F0F0)
val FluentLightOnSurfaceVariant     = Color(0xFF5C5C5C)
val FluentLightSurfaceContainer     = Color(0xFFF5F5F5)
val FluentLightSurfaceContainerHigh = Color(0xFFEFEFEF)
val FluentLightSurfaceContainerHighest = Color(0xFFE9E9E9)

// Outline（Fluent 细分割线，比 M3 Expressive 更轻）
val FluentLightOutline              = Color(0xFFC4C4C4)
val FluentLightOutlineVariant       = Color(0xFFE1E1E1)

// ====================================================================================
// 🌙 Fluent 深色调色盘（Windows 11 / Office 深色模式同源 Token）
// ====================================================================================

// Primary（深色模式下提亮的通信蓝）
val FluentDarkPrimary               = Color(0xFF60CDFF)
val FluentDarkOnPrimary             = Color(0xFF00304D)
val FluentDarkPrimaryContainer      = Color(0xFF00497A)
val FluentDarkOnPrimaryContainer    = Color(0xFFD3E4FA)

// Secondary
val FluentDarkSecondary             = Color(0xFFC5CAD3)
val FluentDarkOnSecondary           = Color(0xFF2B313A)
val FluentDarkSecondaryContainer    = Color(0xFF424954)
val FluentDarkOnSecondaryContainer  = Color(0xFFE4E6EA)

// Tertiary
val FluentDarkTertiary              = Color(0xFFCDB3EE)
val FluentDarkOnTertiary            = Color(0xFF3D1A63)
val FluentDarkTertiaryContainer     = Color(0xFF54357C)
val FluentDarkOnTertiaryContainer   = Color(0xFFEADDFB)

// Error
val FluentDarkError                 = Color(0xFFFFB4A9)
val FluentDarkOnError               = Color(0xFF690500)
val FluentDarkErrorContainer        = Color(0xFF8C1D12)
val FluentDarkOnErrorContainer      = Color(0xFFFFDAD4)

// Background & Surface（Win11 "Smoke" 深灰层级，非纯黑）
val FluentDarkBackground            = Color(0xFF1F1F1F)
val FluentDarkOnBackground          = Color(0xFFF5F5F5)
val FluentDarkSurface               = Color(0xFF1F1F1F)
val FluentDarkOnSurface             = Color(0xFFF5F5F5)
val FluentDarkSurfaceVariant        = Color(0xFF2B2B2B)
val FluentDarkOnSurfaceVariant      = Color(0xFFC5C5C5)
val FluentDarkSurfaceContainer      = Color(0xFF262626)
val FluentDarkSurfaceContainerHigh  = Color(0xFF2E2E2E)
val FluentDarkSurfaceContainerHighest = Color(0xFF363636)

// Outline
val FluentDarkOutline               = Color(0xFF6E6E6E)
val FluentDarkOutlineVariant        = Color(0xFF3B3B3B)

// ====================================================================================
// 🟦🟩🟪 图标色块色板（圆角方形 icon avatar · 仿 GitHub / Microsoft To Do 分类色）
// 用于 ScriptManagerScreen 列表项左侧的实心图标底色
// ====================================================================================

val IconSwatchBlue    = Color(0xFF0078D4)  // 默认 / 脚本
val IconSwatchGreen   = Color(0xFF107C41)  // 成功 / Shell
val IconSwatchPurple  = Color(0xFF5C2D91)  // 讨论 / Node
val IconSwatchOrange  = Color(0xFFCA5010)  // 组织 / 提醒
val IconSwatchTeal    = Color(0xFF038387)  // 备用强调
val IconSwatchYellow  = Color(0xFFFFB900)  // 收藏 / 星标
val IconSwatchSlate   = Color(0xFF69797E)  // 中性 / 其他分类
val IconSwatchRed     = Color(0xFFD13438)  // 错误 / 危险操作

// ====================================================================================
// 🎨 语义调色板（终端日志 / 状态徽章 · 与 Fluent 主题协调）
// ====================================================================================

// 终端 INFO 色
val TerminalInfo    = Color(0xFF0078D4)
// 终端 SUCCESS 色
val TerminalSuccess = Color(0xFF107C41)
// 终端 ERROR 色
val TerminalError   = Color(0xFFD13438)
// 终端 WARN 色
val TerminalWarn    = Color(0xFFFFB900)
// 终端 EXEC 色
val TerminalExec    = Color(0xFF5C2D91)

// 运行中状态绿点
val StatusRunning   = Color(0xFF107C41)

// 脚本类型徽章颜色（语义固定，不随主题翻转）
val TypeColorPython = Color(0xFF0078D4)   // 通信蓝
val TypeColorShell  = Color(0xFF107C41)   // 绿
val TypeColorNode   = Color(0xFF5C2D91)   // 紫
val TypeColorOther  = Color(0xFF69797E)   // 中性灰

// 日志卡片（深色，浅/深模式共用，贴近 Windows Terminal 配色）
val LogCardBg           = Color(0xFF1A1A1A)
val LogCardHeaderText   = Color(0xFF9A9A9A)
val LogCardMutedText    = Color(0xFF6E6E6E)
