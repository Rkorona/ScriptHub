package com.example.myapplication.utils.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.utils.TermuxRunner

class AlarmScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId     = intent.getStringExtra(KEY_TASK_ID)     ?: return
        val taskName   = intent.getStringExtra(KEY_TASK_NAME)   ?: return
        val scriptName = intent.getStringExtra(KEY_SCRIPT_NAME) ?: return
        val cronExpr   = intent.getStringExtra(KEY_CRON_EXPR)   ?: return
        val scriptType = intent.getStringExtra(KEY_SCRIPT_TYPE) ?: "Shell"

        Log.i(TAG, "AlarmManager 触发任务[$taskName] 脚本:$scriptName")

        try {
            TermuxRunner.executeScript(
                context    = context,
                scriptName = scriptName,
                isFolder   = false,
                entryPoint = "",
                scriptType = scriptType
            )
        } catch (e: Exception) {
            Log.e(TAG, "执行失败: ${e.message}")
        }

        // 立即注册下次闹钟（续期）
        AlarmManagerScheduler.scheduleNextAlarm(
            context    = context,
            taskId     = taskId,
            taskName   = taskName,
            scriptName = scriptName,
            cronExpr   = cronExpr,
            scriptType = scriptType
        )
    }

    companion object {
        private const val TAG = "AlarmScheduleReceiver"

        const val KEY_TASK_ID     = "taskId"
        const val KEY_TASK_NAME   = "taskName"
        const val KEY_SCRIPT_NAME = "scriptName"
        const val KEY_CRON_EXPR   = "cronExpression"
        const val KEY_SCRIPT_TYPE = "scriptType"
    }
}
