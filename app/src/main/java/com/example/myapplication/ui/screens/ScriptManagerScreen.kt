package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 模拟脚本数据模型
data class ScriptItem(
    val name: String,
    val type: String, // Shell, Python, Node
    val trigger: String,
    val lastRun: String,
    val isRunning: Boolean = false,
    val themeColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptManagerScreen(
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    // 假数据，刚好对应前面 Dashboard 的配色体系
    val scripts = remember {
        listOf(
            ScriptItem("telegram_bot.py", "Python", "⏰ crontab: */10 * * * *", "上次成功: 2分钟前", true, Color(0xFF38BDF8)),
            ScriptItem("daily_check.js", "Node.js", "⏰ crontab: 0 8 * * *", "上次成功: 今天 08:00", false, Color(0xFFA855F7)),
            ScriptItem("backup_db.sh", "Shell", "⚡ 手动触发", "上次成功: 昨天 23:00", false, Color(0xFF22C55E)),
            ScriptItem("clean_logs.sh", "Shell", "⏰ crontab: 0 0 * * 0", "上次失败: 1周前", false, Color(0xFFEF4444)),
            ScriptItem("test_api_status.py", "Python", "⚡ 手动触发", "从未运行", false, Color(0xFF38BDF8))
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "Python", "Shell", "Node.js")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Expressive 风格的硬核大圆角悬浮按钮
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 新建脚本 */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp) // 非标准的饱满大圆角
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新建脚本", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            // 🔍 1. 战术搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("搜索脚本...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // 🏷️ 2. 横向滚动过滤标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            enabled = true,          // 📌 针对旧版 M3 必须显式传入
                            selected = isSelected,   // 📌 针对旧版 M3 必须显式传入
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,          // 📌 针对旧版 M3 必须显式传入
                            selected = isSelected,   // 📌 针对旧版 M3 必须显式传入
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.2.dp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 📜 3. 武器库列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp), // 为 FAB 留空
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scripts.filter { selectedFilter == "全部" || it.type == selectedFilter }) { script ->
                    ScriptCard(script = script)
                }
            }
        }
    }
}

// 📌 核心单体：丰富脚本卡片
@Composable
fun ScriptCard(script: ScriptItem) {
    // 慢速呼吸灯动画（如果正在运行，外圈会泛起淡淡的微光）
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp), // 完美呼应 Dashboard
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 左侧：高识别度语言微标
            Box(
                modifier = Modifier
                    .size(46.dp)
                    // 如果在运行，外圈膨胀出呼吸光晕效果
                    .background(
                        color = if (script.isRunning) script.themeColor.copy(alpha = pulseAlpha) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(script.themeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // 用硬核的 Terminal 图标打底，或者你可以换成具体的文字 badge
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = script.themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 2. 中间：极客感十足的代码核心信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = script.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (script.isRunning) {
                        Spacer(modifier = Modifier.width(6.dp))
                        // 正在运行的小绿点
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF22C55E), RoundedCornerShape(50))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 等宽字体的 Cron / 触发器，看起来就很专业
                Text(
                    text = script.trigger,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                
                Text(
                    text = script.lastRun,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // 3. 右侧：快捷战术动作舱
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 一键快火执行按钮
                FilledIconButton(
                    onClick = { /* 立即执行脚本 */ },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = script.themeColor.copy(alpha = 0.1f),
                        contentColor = script.themeColor
                    ),
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // 更多菜单
                IconButton(onClick = { /* 弹出菜单 */ }) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = "More", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
