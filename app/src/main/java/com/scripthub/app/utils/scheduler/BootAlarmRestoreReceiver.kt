package com.scripthub.app.utils.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.scripthub.app.data.AppDatabase
import com.scripthub.app.utils.SchedulerPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 设备重启后，自动恢复 AlarmManager 定时任务。
 * WorkManager 任务由系统自动恢复，无需额外处理。
 */
class BootAlarmRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.i(TAG, "系统重启，开始恢复 AlarmManager 定时任务...")

        val schedulerType = SchedulerPreference.getType(context)
        if (schedulerType != SchedulerPreference.SchedulerType.ALARM_MANAGER) {
            Log.i(TAG, "当前使用 WorkManager，无需手动恢复，跳过")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val tasks = db.scheduledTaskDao().getAllOnce()
                val enabledTasks = tasks.filter { it.isEnabled }
                enabledTasks.forEach { task ->
                    AlarmManagerScheduler.scheduleTask(context, task)
                }
                Log.i(TAG, "已恢复 ${enabledTasks.size} 个 AlarmManager 定时任务")
            } catch (e: Exception) {
                Log.e(TAG, "恢复定时任务失败: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "BootAlarmRestoreReceiver"
    }
}
