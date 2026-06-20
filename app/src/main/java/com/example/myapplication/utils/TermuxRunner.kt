// app_template/app/src/main/java/com/example/myapplication/utils/TermuxRunner.kt
package com.example.myapplication.utils

import android.content.Context
import android.content.Intent

object TermuxRunner {
    private const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
    private const val TERMUX_PACKAGE = "com.termux"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    /**
     * 调用 Termux 引擎执行脚本，并将标准输出与错误重定向到本地 socket 端口
     */
    fun executeScript(
        context: Context,
        scriptName: String,
        isFolder: Boolean,
        entryPoint: String,
        scriptType: String,
        socketPort: Int = 9090
    ) {
        // Termux 默认标准 Shell 路径
        val executablePath = "/data/data/com.termux/files/usr/bin/bash"
        
        // 判定解析器命令
        val runCmd = when (scriptType) {
            "Python" -> "python3"
            "Node.js" -> "node"
            "Shell" -> "bash"
            else -> "bash"
        }

        // 拼接执行目标：单文件 vs 文件夹项目入口
        val targetFile = if (isFolder) "$scriptName/$entryPoint" else scriptName
        
        // 核心黑科技管道指令：
        // 2>&1 将标准错误和标准输出整合，再通过 | nc (netcat) 发送到本地 App 开放的端口上
        val fullBashCommand = "cd /sdcard/QLPanel/scripts && $runCmd $targetFile 2>&1 | nc 127.0.0.1 $socketPort"

        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setClassName(TERMUX_PACKAGE, "$TERMUX_PACKAGE.$TERMUX_SERVICE")
            putExtra("com.termux.RUN_COMMAND_PATH", executablePath)
            // -lc 确保加载 Termux 完整的环境变量（包含已装好的 npm/pip 环境）
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-lc", fullBashCommand))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true) // 静默后台运行，不弹黑框
        }

        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}