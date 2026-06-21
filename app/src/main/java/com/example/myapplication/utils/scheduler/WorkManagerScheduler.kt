package com.example.myapplication.utils.scheduler

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.example.myapplication.data.ScheduledTaskEntity
import com.example.myapplication.utils.CronNextRunCalculator
import com.example.myapplication.utils.scheduler.CronScheduleWorker

object WorkManagerScheduler {

    private const val TAG = "WorkManagerScheduler"
    const val TAG_ALL_CRON = "all_cron_tasks"

    fun scheduleTask(context: Context, task: ScheduledTaskEntity) {
        if (!task.isEnabled) return
        val scriptType = inferScriptType(task.targetScript)
        CronScheduleWorker.scheduleNext(
            context        = context,
            taskId         = task.id,
            taskName       = task.name,
            scriptName     = task.targetScript,
            cronExpression = task.cronExpression,
            scriptType     = scriptType
        )
        Log.i(TAG, "已用 WorkManager 注册任务[${task.name}]")
    }

    fun cancelTask(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
        Log.i(TAG, "已取消 WorkManager 任务[$taskId]")
    }

    fun cancelAllTasks(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_ALL_CRON)
        Log.i(TAG, "已取消所有 WorkManager 定时任务")
    }

    private fun inferScriptType(scriptName: String): String = when {
        scriptName.endsWith(".py",  ignoreCase = true) -> "Python"
        scriptName.endsWith(".js",  ignoreCase = true) ||
        scriptName.endsWith(".ts",  ignoreCase = true) -> "Node.js"
        scriptName.endsWith(".sh",  ignoreCase = true) -> "Shell"
        else -> "Shell"
    }
}
