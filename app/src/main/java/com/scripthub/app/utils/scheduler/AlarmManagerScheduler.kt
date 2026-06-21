package com.scripthub.app.utils.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.scripthub.app.data.ScheduledTaskEntity
import com.scripthub.app.utils.CronNextRunCalculator

object AlarmManagerScheduler {

    private const val TAG = "AlarmManagerScheduler"

    fun scheduleTask(context: Context, task: ScheduledTaskEntity) {
        if (!task.isEnabled) return
        val scriptType = inferScriptType(task.targetScript)
        scheduleNextAlarm(
            context    = context,
            taskId     = task.id,
            taskName   = task.name,
            scriptName = task.targetScript,
            cronExpr   = task.cronExpression,
            scriptType = scriptType
        )
        Log.i(TAG, "已用 AlarmManager 注册任务[${task.name}]")
    }

    fun scheduleNextAlarm(
        context: Context,
        taskId: String,
        taskName: String,
        scriptName: String,
        cronExpr: String,
        scriptType: String
    ) {
        val nextMillis = CronNextRunCalculator.nextRunMillis(cronExpr)
        if (nextMillis == Long.MAX_VALUE) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, taskId, taskName, scriptName, cronExpr, scriptType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // 无精准闹钟权限时退回普通闹钟
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextMillis, pendingIntent)
                Log.w(TAG, "无 SCHEDULE_EXACT_ALARM 权限，已使用非精准模式")
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextMillis, pendingIntent)
        }

        Log.i(TAG, "下次触发[$taskName] at $nextMillis")
    }

    fun cancelTask(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 构造一个同 requestCode 的 PendingIntent 以取消
        val intent = Intent(context, AlarmScheduleReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
        Log.i(TAG, "已取消 AlarmManager 任务[$taskId]")
    }

    fun cancelAllTasks(context: Context, tasks: List<ScheduledTaskEntity>) {
        tasks.forEach { cancelTask(context, it.id) }
        Log.i(TAG, "已取消所有 AlarmManager 定时任务(${tasks.size}个)")
    }

    private fun buildPendingIntent(
        context: Context,
        taskId: String,
        taskName: String,
        scriptName: String,
        cronExpr: String,
        scriptType: String
    ): PendingIntent {
        val intent = Intent(context, AlarmScheduleReceiver::class.java).apply {
            putExtra(AlarmScheduleReceiver.KEY_TASK_ID,     taskId)
            putExtra(AlarmScheduleReceiver.KEY_TASK_NAME,   taskName)
            putExtra(AlarmScheduleReceiver.KEY_SCRIPT_NAME, scriptName)
            putExtra(AlarmScheduleReceiver.KEY_CRON_EXPR,   cronExpr)
            putExtra(AlarmScheduleReceiver.KEY_SCRIPT_TYPE, scriptType)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun inferScriptType(scriptName: String): String = when {
        scriptName.endsWith(".py", ignoreCase = true)  -> "Python"
        scriptName.endsWith(".js", ignoreCase = true) ||
        scriptName.endsWith(".ts", ignoreCase = true)  -> "Node.js"
        scriptName.endsWith(".sh", ignoreCase = true)  -> "Shell"
        else -> "Shell"
    }
}
