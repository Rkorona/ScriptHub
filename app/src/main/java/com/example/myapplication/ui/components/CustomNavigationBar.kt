package com.example.myapplication.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExpressiveNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer, 
    ) {
        // 1. 仪表盘 (Q弹放大动画)
        NavigationBarItem(
            selected = currentRoute == "Dashboard",
            onClick = { onNavigate("Dashboard") },
            alwaysShowLabel = false, // 只有选中时才显示标签
            label = { Text("仪表盘") },
            // 📌 修正：动画判断必须跟路由字符串 "Dashboard" 完全一致
            icon = { AnimatedHomeIcon(selected = currentRoute == "Dashboard") }
        )

        // 2. 脚本管理 (向上顶一下的果冻动画)
        NavigationBarItem(
            selected = currentRoute == "ScriptManager",
            onClick = { onNavigate("ScriptManager") },
            alwaysShowLabel = false,
            label = { Text("脚本管理") },
            // 📌 修正：动画判断必须跟路由字符串 "ScriptManager" 完全一致
            icon = { AnimatedProfileIcon(selected = currentRoute == "ScriptManager") }
        )

        // 3. 设置 (炫酷齿轮旋转一圈)
        // 📌 修正：全部改成小写 "settings"，与你的 MainScreen 严格对齐！
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            alwaysShowLabel = false,
            label = { Text("设置") },
            icon = { AnimatedSettingsIcon(selected = currentRoute == "settings") }
        )
    }
}

// ==================== 🛠️ 图标微动效组件库 ====================

/**
 * 设置图标：被选中时旋转一圈，并带有弹性回弹
 */
@Composable
fun AnimatedSettingsIcon(selected: Boolean) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(selected) {
        if (selected) {
            rotation.snapTo(0f) // 先重置角度
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Icon(
        imageVector = Icons.Filled.Settings,
        contentDescription = "Settings",
        modifier = Modifier.graphicsLayer(rotationZ = rotation.value)
    )
}

/**
 * 主页图标：被选中时先放大再缩回
 */
@Composable
fun AnimatedHomeIcon(selected: Boolean) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(1f)
            scale.animateTo(1.25f, animationSpec = tween(durationMillis = 100))
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Icon(
        imageVector = Icons.Filled.Home,
        contentDescription = "Dashboard",
        modifier = Modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value)
    )
}

/**
 * 个人图标：被选中时向上弹跳一下
 */
@Composable
fun AnimatedProfileIcon(selected: Boolean) {
    val translationY = remember { Animatable(0f) }

    LaunchedEffect(selected) {
        if (selected) {
            translationY.snapTo(0f)
            translationY.animateTo(-8f, animationSpec = tween(durationMillis = 100))
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = "ScriptManager",
        modifier = Modifier.graphicsLayer(translationY = translationY.value)
    )
}

// ==================== 🔍 预览 ====================

@Preview
@Composable
fun ExpressiveNavigationBarPreview() {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    ExpressiveNavigationBar(
        currentRoute = selectedTab,
        onNavigate = { selectedTab = it }
    )
}
