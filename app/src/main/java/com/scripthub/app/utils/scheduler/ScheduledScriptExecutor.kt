package com.scripthub.app.utils.scheduler

import android.content.Context
import android.util.Log
import com.scripthub.app.data.AppDatabase
import com.scripthub.app.data.RunLogEntity
import com.scripthub.app.utils.FileHelper
import com.scripthub.app.utils.ProotRunner
import com.scripthub.app.utils.ScriptForegroundService
import com.scripthub.app.utils.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScheduledScriptExecutor {
    private const val TAG = "ScheduledScriptExecutor"

    suspend fun executeAndLog(
        context: Context,
        scriptName: String,
        scriptType: String,
        taskName: String
    ) = withContext(Dispatchers.IO) {
        FileHelper.init(context)

        val db        = AppDatabase.getDatabase(context)
        val runLogDao = db.runLogDao()

        val envVars = try {
            db.envVarDao().getAll().first()
                .filter { it.isEnabled }
                .associate { it.name to it.value }
        } catch (e: Exception) {
            Log.w(TAG, "[$taskName] 读取环境变量失败: ${e.message}")
            emptyMap()
        }
        if (envVars.isNotEmpty()) {
            Log.i(TAG, "[$taskName] 注入 ${envVars.size} 个环境变量: ${envVars.keys.joinToString(", ")}")
        }

        val startTime = System.currentTimeMillis()
        val rawLines  = mutableListOf<String>()
        var exitCode  = -1

        val useShizuku = ShizukuHelper.state.value == ShizukuHelper.State.READY

        if (useShizuku && scriptType == "Shell") {
            // ── Shizuku 路径：以 shell 用户权限执行 sh 脚本 ────────────────────────
            try {
                val scriptEntity = db.scriptDao().getByName(scriptName)
                val scriptFile = if (scriptEntity != null && scriptEntity.isFolder)
                    java.io.File(FileHelper.scriptsDir, "${scriptEntity.name}/${scriptEntity.entryPoint}")
                else
                    java.io.File(FileHelper.scriptsDir, scriptName)

                val scriptPath = scriptFile.absolutePath
                Log.i(TAG, "[$taskName] 通过 Shizuku 以 shell 权限执行: $scriptPath")

                ScriptForegroundService.start(context, "正在定时执行: $taskName")

                val envExports = if (envVars.isEmpty()) "" else
                    envVars.entries.joinToString(" && ") { (k, v) ->
                        val escaped = v.replace("'", "'\\''")
                        "export $k='$escaped'"
                    } + " && "

                val result = ShizukuHelper.exec("${envExports}sh \"$scriptPath\"")
                exitCode = result.exitCode

                val output = (result.stdout + result.stderr).trimEnd()
                output.lines().filter { it.isNotBlank() }.forEach { rawLines.add(it) }

                Log.i(TAG, "[$taskName] Shizuku 执行完成，exitCode=$exitCode")

                if (result.stderr.isNotBlank()) {
                    Log.w(TAG, "[$taskName] stderr: ${result.stderr.take(500)}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[$taskName] Shizuku 执行异常: ${e.message}")
                rawLines.add("[ERROR] Shizuku 执行异常: ${e.message}")
            } finally {
                ScriptForegroundService.stop(context)
            }
        } else {
            // ── proot 路径：Python / Node.js 脚本，或 Shizuku 未就绪时的 Shell ────
            if (scriptType == "Shell" && !useShizuku) {
                Log.w(TAG, "[$taskName] Shizuku 未就绪，降级使用 proot（无 Android/data 访问权限）")
                rawLines.add("[WARN] Shizuku 未就绪，以 proot 模式执行，无法访问 Android/data 目录")
            }

            var process: Process?       = null
            var reader:  BufferedReader? = null

            try {
                Log.i(TAG, "[$taskName] 启动 proot 进程执行脚本: $scriptName")
                ScriptForegroundService.start(context, "正在定时执行: $taskName")

                val scriptEntity = db.scriptDao().getByName(scriptName)

                process = ProotRunner.executeScript(
                    context    = context,
                    scriptName = scriptName,
                    isFolder   = scriptEntity?.isFolder ?: false,
                    entryPoint = scriptEntity?.entryPoint ?: "",
                    scriptType = scriptType,
                    envVars    = envVars
                )

                reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val finalLine = line!!
                    rawLines.add(finalLine)
                    if (finalLine.startsWith("[SYSTEM_EXIT_CODE]:")) {
                        exitCode = finalLine.removePrefix("[SYSTEM_EXIT_CODE]:").trim().toIntOrNull() ?: -1
                    }
                }
                process.waitFor()

            } catch (e: IllegalStateException) {
                Log.e(TAG, "[$taskName] proot 环境未就绪: ${e.message}")
                rawLines.add("[ERROR] ${e.message}")
                rawLines.add("[INFO] 请在「配置中心 → Linux 运行环境」完成安装")
            } catch (e: Exception) {
                Log.e(TAG, "[$taskName] 执行异常: ${e.message}")
                rawLines.add("[ERROR] 定时任务执行异常: ${e.message}")
            } finally {
                ScriptForegroundService.stop(context)
                try { reader?.close() } catch (_: Exception) {}
                try { process?.destroy() } catch (_: Exception) {}
            }
        }

        val durationMs = System.currentTimeMillis() - startTime
        val logText    = rawLines
            .filter { !it.startsWith("[SYSTEM_EXIT_CODE]:") }
            .joinToString("\n")

        try {
            runLogDao.insert(
                RunLogEntity(
                    scriptName = scriptName,
                    startTime  = startTime,
                    durationMs = durationMs,
                    exitCode   = exitCode,
                    logText    = logText
                )
            )
            runLogDao.pruneOldLogs(scriptName)
            Log.i(TAG, "[$taskName] 日志写入完成，exitCode=$exitCode，耗时=${durationMs}ms")
        } catch (e: Exception) {
            Log.e(TAG, "[$taskName] 日志入库失败: ${e.message}")
        }

        try {
            val fmt   = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val label = if (exitCode == 0) "✅ ${fmt.format(Date(startTime))}"
                        else "❌ ${fmt.format(Date(startTime))}"
            db.scriptDao().updateLastRun(scriptName, label)
        } catch (e: Exception) {
            Log.e(TAG, "[$taskName] 更新最后运行时间失败: ${e.message}")
        }
    }
}
