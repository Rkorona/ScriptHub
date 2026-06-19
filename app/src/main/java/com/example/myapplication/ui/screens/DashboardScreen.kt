package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp, // 稍微缩减一点间距，紧凑更精致
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==== 1. 后台守护状态（全面精修版） ====
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(24.dp) // Expressive 夸张大圆角
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 带有外圈光晕的呼吸灯
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF22C55E), RoundedCornerShape(50))
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("面板守护服务", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("已连续运行 24 小时", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                    
                    // 用轻量按钮代替大图标
                    FilledIconButton(
                        onClick = { /* 重启服务 */ },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // ==== 2. 数据战报网格 (注入 Icon 灵魂) ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatusCard(
                        title = "总脚本数", 
                        value = "12", 
                        icon = Icons.Default.Code, 
                        iconColor = Color(0xFF38BDF8), // 亮天蓝
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        title = "当前运行", 
                        value = "2", 
                        icon = Icons.Default.PlayArrow, 
                        iconColor = Color(0xFFA855F7), // 科技紫
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatusCard(
                        title = "今日触发", 
                        value = "148", 
                        icon = Icons.Default.Speed, 
                        iconColor = Color(0xFF22C55E), // 荧光绿
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        title = "执行失败", 
                        value = "1", 
                        icon = Icons.Default.Warning, 
                        iconColor = Color(0xFFEF4444), // 警告红
                        modifier = Modifier.weight(1f),
                        isError = true
                    )
                }
            }
        }

        // ==== 3. 系统资源监控（微调间距） ====
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("系统资源", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("RAM 内存占用", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text("65% (2.4G / 4G)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.65f }, 
                            modifier = Modifier.fillMaxWidth().height(6.dp), 
                            color = Color(0xFF38BDF8),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                    }

                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("沙盒存储空间", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            Text("12% (1.2G / 10G)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.12f }, 
                            modifier = Modifier.fillMaxWidth().height(6.dp), 
                            color = Color(0xFF22C55E),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                    }
                }
            }
        }

        // ==== 4. 最近终端动态流（保持硬核终端感） ====
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), 
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("最近执行动态", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val logStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Text("[21:40:08] telegram_bot.py -> 通知发送成功 ✅", color = Color(0xFF4ADE80), style = logStyle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("[21:55:12] daily_check.js -> 正在跑 npm run...", color = Color(0xFF38BDF8), style = logStyle)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("[22:00:00] sign_in.py -> 登录失败: 凭证过期 ❌", color = Color(0xFFF87171), style = logStyle)
                }
            }
        }
    }
}

// 📌 彻底整容的网格小卡片组件
@Composable
fun StatusCard(
    title: String, 
    value: String, 
    icon: ImageVector, 
    iconColor: Color, 
    modifier: Modifier = Modifier, 
    isError: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp) // 大圆角显得非常现代
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 顶层：标题 + 彩色 Icon 微标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                // 核心视觉改良：带 15% 透明底色的 Icon 容器
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = iconColor, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 底层：夸张放大的粗体数字
            Text(
                text = value,
                fontSize = 32.sp, 
                fontWeight = FontWeight.Bold,
                color = if (isError && value != "0") iconColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
