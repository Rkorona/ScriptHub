package com.scripthub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scripthub.app.ui.theme.TerminalError
import com.scripthub.app.ui.theme.TerminalExec
import com.scripthub.app.ui.theme.TerminalInfo
import com.scripthub.app.ui.theme.TerminalSuccess
import com.scripthub.app.ui.theme.TerminalWarn
import com.scripthub.app.utils.DistroPreference
import com.scripthub.app.utils.ProotManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

private data class TermLine(val text: String, val color: Color, val isPrompt: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellTerminalScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val distro  = remember { DistroPreference.getDistro(context) }

    val lines      = remember { mutableStateListOf<TermLine>() }
    val listState  = rememberLazyListState()
    val focus      = remember { FocusRequester() }

    var input      by remember { mutableStateOf("") }
    var isRunning  by remember { mutableStateOf(false) }
    var shellReady by remember { mutableStateOf(false) }
    var shellError by remember { mutableStateOf<String?>(null) }

    var process: Process? by remember { mutableStateOf(null) }
    var stdinWriter by remember { mutableStateOf<java.io.OutputStreamWriter?>(null) }

    fun scrollToBottom() {
        scope.launch {
            if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
        }
    }

    fun addLine(text: String, color: Color = TerminalInfo, isPrompt: Boolean = false) {
        lines.add(TermLine(text, color, isPrompt))
        scrollToBottom()
    }

    fun sendCommand(cmd: String) {
        if (cmd.isBlank() || !shellReady) return
        val trimmed = cmd.trim()
        addLine("$ $trimmed", TerminalExec, isPrompt = true)
        input = ""
        isRunning = true
        scope.launch(Dispatchers.IO) {
            try {
                stdinWriter?.write("$trimmed\necho '---CMD_DONE---'\n")
                stdinWriter?.flush()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLine("[ERROR] 写入失败: ${e.message}", TerminalError)
                    isRunning = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!ProotManager.isProotReady(context)) {
            shellError = "proot 引擎未就绪，请在「配置中心 → Linux 运行环境」完成安装"
            return@LaunchedEffect
        }
        if (!ProotManager.isDistroInstalled(context, distro)) {
            shellError = "${distro.displayName} 尚未安装，请在「配置中心 → Linux 运行环境」完成安装"
            return@LaunchedEffect
        }

        addLine("连接到 ${distro.displayName} 环境...", TerminalInfo)
        addLine("提示：输入命令后按回车执行，Ctrl+C 效果用 kill 替代", TerminalWarn)

        withContext(Dispatchers.IO) {
            try {
                val cmd = ProotManager.buildProotCommand(context, distro, "bash --norc --noprofile")
                val pb = ProcessBuilder(cmd).apply {
                    environment()["HOME"]  = "/root"
                    environment()["TERM"]  = "xterm-256color"
                    environment()["LANG"]  = "C.UTF-8"
                    environment()["PS1"]   = ""
                    environment()["PATH"]  =
                        "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
                    redirectErrorStream(true)
                }
                val proc = pb.start()
                process = proc
                val writer = java.io.OutputStreamWriter(proc.outputStream)
                stdinWriter = writer

                withContext(Dispatchers.Main) {
                    shellReady = true
                    addLine("─── 已连接到 ${distro.displayName} shell ───", TerminalSuccess)
                }

                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line == "---CMD_DONE---") {
                        withContext(Dispatchers.Main) { isRunning = false }
                        continue
                    }
                    if (line.isBlank() && !isRunning) continue
                    val color = when {
                        line.contains("error",   ignoreCase = true) ||
                        line.contains("failed",  ignoreCase = true) ||
                        line.contains("No such", ignoreCase = true) -> TerminalError
                        line.contains("warning", ignoreCase = true) ||
                        line.contains("warn",    ignoreCase = true)  -> TerminalWarn
                        else -> TerminalInfo
                    }
                    withContext(Dispatchers.Main) { addLine(line, color) }
                }
                withContext(Dispatchers.Main) {
                    shellReady = false
                    isRunning  = false
                    addLine("─── Shell 已退出 ───", TerminalWarn)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    shellError = "启动 shell 失败: ${e.message}"
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { stdinWriter?.write("exit\n"); stdinWriter?.flush() } catch (_: Exception) {}
            try { process?.destroy() } catch (_: Exception) {}
        }
    }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top    = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            )
    ) {
        // ─── 状态栏 ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val statusColor = when {
                shellError != null -> colors.error
                shellReady && !isRunning -> TerminalSuccess
                shellReady && isRunning  -> TerminalWarn
                else -> colors.onSurfaceVariant
            }
            val statusText = when {
                shellError != null       -> "错误"
                shellReady && !isRunning -> "就绪"
                shellReady && isRunning  -> "执行中"
                else                     -> "连接中..."
            }

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint     = statusColor,
                        modifier = Modifier.size(7.dp)
                    )
                    Text(
                        text       = statusText,
                        color      = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 12.sp
                    )
                }
            }

            Text(
                text  = distro.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))

            if (isRunning) {
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            stdinWriter?.write("\u0003")
                            stdinWriter?.flush()
                        }
                        isRunning = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.StopCircle,
                        contentDescription = "中断",
                        tint     = colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(
                onClick = { lines.clear() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "清空",
                    tint     = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ─── 错误提示 ─────────────────────────────────────────────────────────
        if (shellError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = colors.errorContainer),
                shape  = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text     = shellError!!,
                    color    = colors.onErrorContainer,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // ─── 终端输出区 ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainer)
        ) {
            LazyColumn(
                state   = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(lines) { _, line ->
                    if (line.isPrompt) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = TerminalSuccess, fontWeight = FontWeight.Bold)) {
                                    append("$ ")
                                }
                                withStyle(SpanStyle(color = TerminalExec)) {
                                    append(line.text.removePrefix("$ "))
                                }
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 13.sp,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text       = line.text,
                            color      = line.color,
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }

        // ─── 输入区 ───────────────────────────────────────────────────────────
        Surface(
            modifier      = Modifier.fillMaxWidth(),
            color         = colors.surfaceContainerLow,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text  = "$",
                    color = TerminalSuccess,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                OutlinedTextField(
                    value         = input,
                    onValueChange = { input = it },
                    modifier      = Modifier
                        .weight(1f)
                        .focusRequester(focus),
                    placeholder   = {
                        Text(
                            "输入命令...",
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 13.sp,
                            color      = colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    singleLine    = true,
                    enabled       = shellReady && shellError == null,
                    textStyle     = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 13.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendCommand(input) }),
                    shape   = RoundedCornerShape(12.dp),
                    colors  = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = colors.outline.copy(alpha = 0.3f),
                        focusedContainerColor   = colors.surfaceContainer,
                        unfocusedContainerColor = colors.surfaceContainer
                    )
                )
                FilledIconButton(
                    onClick  = { sendCommand(input) },
                    enabled  = input.isNotBlank() && shellReady && !isRunning && shellError == null,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "发送",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
