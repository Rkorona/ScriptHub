package com.scripthub.app.utils.scheduler

import android.content.Context
import android.util.Log
import com.scripthub.app.data.ScheduledTaskEntity
import com.scripthub.app.utils.SchedulerPreference
import com.scripthub.app.utils.SchedulerPreference.SchedulerType

object TaskSchedulerManager {

    private const val TAG = "TaskSchedulerManager"

    fun scheduleTask(context: Context, task: ScheduledTaskEntity) {
        if (!task.isEnabled) return
        when (SchedulerPreference.getType(context)) {
            SchedulerType.WORK_MANAGER  -> WorkManagerScheduler.scheduleTask(context, task)
            SchedulerType.ALARM_MANAGER -> AlarmManagerScheduler.scheduleTask(context, task)
        }
        Log.i(TAG, "已调度任务[${task.name}] 使用 ${SchedulerPreference.getType(context).label}")
    }

    fun cancelTask(context: Context, taskId: String, allTasks: List<ScheduledTaskEntity> = emptyList()) {
        WorkManagerScheduler.cancelTask(context, taskId)
        AlarmManagerScheduler.cancelTask(context, taskId)
        Log.i(TAG, "已取消任务[$taskId]（双引擎均已清除）")
    }

    /**
     * 切换调度引擎：
     * 1. 取消两个引擎的所有任务
     * 2. 保存新的引擎偏好
     * 3. 用新引擎重新注册所有已启用任务
     */
    fun switchScheduler(
        context: Context,
        newType: SchedulerType,
        allTasks: List<ScheduledTaskEntity>
    ) {
        val oldType = SchedulerPreference.getType(context)
        if (oldType == newType) return

        Log.i(TAG, "切换调度引擎: ${oldType.label} → ${newType.label}")

        // 取消旧引擎的所有任务
        when (oldType) {
            SchedulerType.WORK_MANAGER  -> WorkManagerScheduler.cancelAllTasks(context)
            SchedulerType.ALARM_MANAGER -> AlarmManagerScheduler.cancelAllTasks(context, allTasks)
        }

        // 保存新偏好
        SchedulerPreference.setType(context, newType)

        // 用新引擎重新注册所有启用的任务
        allTasks.filter { it.isEnabled }.forEach { task ->
            when (newType) {
                SchedulerType.WORK_MANAGER  -> WorkManagerScheduler.scheduleTask(context, task)
                SchedulerType.ALARM_MANAGER -> AlarmManagerScheduler.scheduleTask(context, task)
            }
        }

        Log.i(TAG, "引擎切换完成，已重新注册 ${allTasks.count { it.isEnabled }} 个任务")
    }
}
