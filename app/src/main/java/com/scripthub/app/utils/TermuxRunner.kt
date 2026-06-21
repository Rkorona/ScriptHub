package com.scripthub.app.utils

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
     * 自动执行脚本的主引擎
     * @param envVars 从数据库读取的已启用环境变量，将在执行前 export 到 Shell 环境中
     */
    fun executeScript(
        context: Context,
        scriptName: String,
        isFolder: Boolean,
        entryPoint: String,
        scriptType: String,
        socketPort: Int = 9090,
        envVars: Map<String, String> = emptyMap()
    ) {
        if (!isTermuxInstalled(context)) {
            throw IllegalStateException("未检测到 Termux，请先安装 Termux 并完成基础环境初始化")
        }

        val executablePath = "/data/data/com.termux/files/usr/bin/bash"

        val runCmd = when (scriptType) {
            "Python" -> "python3"
            "Node.js" -> "node"
            "Shell" -> "bash"
            else -> "bash"
        }

        val targetFile = if (isFolder) "$scriptName/$entryPoint" else scriptName

        // 将 App 内设置的环境变量逐条 export，注入到执行环境
        val envExports = if (envVars.isEmpty()) {
            ""
        } else {
            envVars.entries.joinToString(" && \\\n") { (k, v) ->
                // 对值进行单引号包裹，防止特殊字符被 Shell 解释
                val escaped = v.replace("'", "'\\''")
                "export $k='$escaped'"
            } + " && \\\n"
        }

        val fullBashCommand = """
            export PATH=/data/data/com.termux/files/usr/bin:${"$"}PATH && \
            cd /sdcard/QLPanel/scripts && \
            $envExports( $runCmd $targetFile 2>&1 ; EXIT_VAL=${"$"}? ; echo "[SYSTEM_EXIT_CODE]:${"$"}{EXIT_VAL}" ) | nc -N 127.0.0.1 $socketPort
        """.trimIndent()

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
