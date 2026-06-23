// app_template/app/src/main/java/com/example/myapplication/ui/screens/DashboardScreen.kt

package com.scripthub.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.scripthub.app.ui.theme.StatusRunning
import com.scripthub.app.ui.theme.TerminalSuccess
import com.scripthub.app.ui.theme.TerminalInfo
import com.scripthub.app.ui.theme.TerminalError
import com.scripthub.app.ui.theme.LogCardBg
import com.scripthub.app.ui.theme.LogCardHeaderText
import com.scripthub.app.ui.theme.LogCardMutedText

// =====================================================================================
// 数据模型 —— 归零初始状态，为接入真实数据库/执行环境做准备
// =====================================================================================

enum class LogStatus { SUCCESS, RUNNING, FAILED }

data class LogEntry(
    val time: String,
    val scriptName: String,
    val message: String,
    val status: LogStatus
)

data class NextRun(val time: String, val scriptName: String)

data class DashboardUiState(
    val serviceRunning: Boolean = false,
    val uptimeLabel: String = "服务尚未启动",
    val totalScripts: Int = 0,
    val runningNow: Int = 0,
    val triggeredToday: Int = 0,
    val failedCount: Int = 0,
    val nextRun: NextRun = NextRun("--:--", "暂无调度任务"),
    // 保留系统真实资源占用的模拟数值
    val ramUsedGb: Float = 2.4f,
    val ramTotalGb: Float = 4f,
    val storageUsedGb: Float = 1.2f,
    val storageTotalGb: Float = 10f,
    val recentLogs: List<LogEntry> = emptyList() // 初始日志为空
)

// =====================================================================================
// 主屏幕
// =====================================================================================

@Composable
fun DashboardScreen(
    state: DashboardUiState = DashboardUiState(),
    contentPadding: PaddingValues = PaddingValues(),
    onRestartService: () -> Unit = {},
    onViewServiceLogs: () -> Unit = {},
    onFailuresClick: () -> Unit = {},
    onStatClick: (String) -> Unit = {},
    onViewAllLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "service") {
            AnimatedSection(visible, 0) {
                ServiceHealthCard(state, onRestartService, onViewServiceLogs)
            }
        }

        if (state.failedCount > 0) {
            item(key = "failures") {
                AnimatedSection(visible, 60) {
                    FailureBanner(state.failedCount, onFailuresClick)
                }
            }
        }

        item(key = "stats") {
            AnimatedSection(visible, 120) {
                StatGrid(state, onStatClick)
            }
        }



        item(key = "logs") {
            AnimatedSection(visible, 240) {
                TerminalLogCard(state.recentLogs, onViewAllLogs)
            }
        }
    }
}

@Composable
private fun AnimatedSection(visible: Boolean, delayMillis: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350, delayMillis)) +
            slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(350, delayMillis))
    ) {
        content()
    }
}

// =====================================================================================
// 1. 守护服务状态卡
// =====================================================================================

@Composable
private fun ServiceHealthCard(
    state: DashboardUiState,
    onRestart: () -> Unit,
    onViewLogs: () -> Unit
) {
    val statusColor = if (state.serviceRunning) StatusRunning else MaterialTheme.colorScheme.error

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(statusColor.copy(alpha = if (state.serviceRunning) pulseAlpha else 0.2f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(10.dp).background(statusColor, RoundedCornerShape(50)))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("面板守护服务", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (state.serviceRunning) state.uptimeLabel else "服务已停止，点击重启", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row {
                IconButton(onClick = onViewLogs) { Icon(Icons.Default.History, contentDescription = "查看日志") }
                FilledIconButton(
                    onClick = onRestart,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "重启服务")
                }
            }
        }
    }
}

// =====================================================================================
// 2. 失败警示条
// =====================================================================================

@Composable
private fun FailureBanner(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().expressiveClickable(onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("$count 个脚本执行失败，点击查看详情", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

// =====================================================================================
// 3. KPI 网格
// =====================================================================================

@Composable
private fun StatGrid(state: DashboardUiState, onStatClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("总脚本数", state.totalScripts.toString(), null, Icons.Default.Code, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { onStatClick("total") }
            StatCard("当前运行", state.runningNow.toString(), null, Icons.Default.PlayArrow, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(1f)) { onStatClick("running") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("今日触发", state.triggeredToday.toString(), null, Icons.Default.Speed, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f)) { onStatClick("triggered") }
            StatCard("下次执行", state.nextRun.time, state.nextRun.scriptName, Icons.Default.Schedule, MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { onStatClick("nextRun") }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, caption: String?, icon: ImageVector, containerColor: Color, contentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.expressiveClickable(onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
            if (caption != null) {
                Spacer(Modifier.height(2.dp))
                Text(caption, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
            }
        }
    }
}



// =====================================================================================
// 5. 终端日志卡
// =====================================================================================

@Composable
private fun TerminalLogCard(logs: List<LogEntry>, onViewAll: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LogCardBg),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("最近执行动态", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LogCardHeaderText, modifier = Modifier.weight(1f))
                if (logs.isNotEmpty()) {
                    TextButton(onClick = onViewAll) {
                        Text("查看全部", color = TerminalInfo, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            
            // 空状态保护
            if (logs.isEmpty()) {
                Text(
                    "暂无任何脚本执行记录",
                    color = LogCardMutedText,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                logs.forEachIndexed { index, entry ->
                    LogLine(entry)
                    if (index != logs.lastIndex) Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LogLine(entry: LogEntry) {
    val (icon, color) = when (entry.status) {
        LogStatus.SUCCESS -> Icons.Default.CheckCircle to TerminalSuccess
        LogStatus.RUNNING -> Icons.Default.PlayArrow to TerminalInfo
        LogStatus.FAILED -> Icons.Default.Cancel to TerminalError
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text("[${entry.time}] ${entry.scriptName} -> ${entry.message}", color = color, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp))
    }
}

// =====================================================================================
// 触感反馈
// =====================================================================================

private fun Modifier.expressiveClickable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.96f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    this.graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}