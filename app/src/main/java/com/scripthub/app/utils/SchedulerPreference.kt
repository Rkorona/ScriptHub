package com.scripthub.app.utils

import android.content.Context

object SchedulerPreference {

    enum class SchedulerType(
        val label: String,
        val shortLabel: String,
        val description: String,
        val pros: String,
        val cons: String
    ) {
        ALARM_MANAGER(
            label        = "AlarmManager",
            shortLabel   = "Alarm",
            description  = "系统级精准触发，适合对时间敏感的任务",
            pros         = "支持任意 Cron 间隔，分钟级精准",
            cons         = "设备重启后需重新注册"
        ),
        WORK_MANAGER(
            label        = "WorkManager",
            shortLabel   = "Worker",
            description  = "遵循系统省电策略，适合低频后台任务",
            pros         = "重启自动恢复，系统深度集成",
            cons         = "最小间隔受系统策略限制"
        )
    }

    private const val PREFS_NAME = "ql_scheduler_prefs"
    private const val KEY_TYPE   = "scheduler_type"

    fun getType(context: Context): SchedulerType {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TYPE, SchedulerType.ALARM_MANAGER.name)
        return SchedulerType.entries.firstOrNull { it.name == raw } ?: SchedulerType.ALARM_MANAGER
    }

    fun setType(context: Context, type: SchedulerType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TYPE, type.name)
            .apply()
    }
}
