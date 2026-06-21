package com.example.myapplication.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

object TermuxRunner {
    private const val TAG = "TermuxRunner"
    private const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
    private const val TERMUX_PACKAGE = "com.termux"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    /**
     * 自动执行脚本的主引擎（已加固退出码回传机制）
     */
    fun executeScript(
        context: Context,
        scriptName: String,
        isFolder: Boolean,
        entryPoint: String,
        scriptType: String,
        socketPort: Int = 9090
    ) {
        // 前置检查：Termux 是否已安装
        if (!isTermuxInstalled(context)) {
            throw IllegalStateException("未检测到 Termux，请先安装 Termux 并完成基础环境初始化")
        }

        // Termux 默认标准 Shell 路径
        val executablePath = "/data/data/com.termux/files/usr/bin/bash"

        // 判定编译器
        val runCmd = when (scriptType) {
            "Python" -> "python3"
            "Node.js" -> "node"
            "Shell" -> "bash"
            else -> "bash"
        }

        // 拼接目标脚本
        val targetFile = if (isFolder) "$scriptName/$entryPoint" else scriptName

        /**
         * 【极客级重构指令】
         * 1. 使用 () 将目标脚本的执行打包成子 Shell，方便整体捕获其标准输出及标准错误。
         * 2. 利用一个 TCP 管道（通过 nc 保持长连接）。
         * 3. 子 Shell 运行完毕后，通过 $? 瞬间拿到进程的退出状态码。
         * 4. 显式通过 nc 管道向 App 注入一条特殊的终端状态协议：`[SYSTEM_EXIT_CODE]:X`。
         * 5. 这样无论脚本内部是否有输出（如 mkdir 静默执行），App 都能百分之百收到运行结果。
         */
        val fullBashCommand = """
            export PATH=/data/data/com.termux/files/usr/bin:${"$"}PATH && \
            cd /sdcard/QLPanel/scripts && \
            ( $runCmd $targetFile 2>&1 ; EXIT_VAL=${"$"}? ; echo "[SYSTEM_EXIT_CODE]:${"$"}{EXIT_VAL}" ) | nc -N 127.0.0.1 $socketPort
        """.trimIndent()

        // 备注：上面 nc 的 -N 参数（如果有支持）表示在输入遇到 EOF 时主动半关闭 TCP 连接，确保 App 能够顺畅读到流末尾并退出。

        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setClassName(TERMUX_PACKAGE, TERMUX_SERVICE)
            putExtra("com.termux.RUN_COMMAND_PATH", executablePath)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", fullBashCommand))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun isTermuxInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(TAG, "Termux 未安装: $TERMUX_PACKAGE")
        false
    }
}