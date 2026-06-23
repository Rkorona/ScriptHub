package com.scripthub.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ScriptForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "script_execution_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, taskDesc: String) {
            val intent = Intent(context, ScriptForegroundService::class.java).apply {
                putExtra("task_desc", taskDesc)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScriptForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskDesc = intent?.getStringExtra("task_desc") ?: "正在执行脚本..."
        createNotificationChannel()

        // 默认使用内置图标或者使用 app_icon 如果有的话 (我们暂时使用 ic_menu_info_details)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScriptHub 任务运行中")
            .setContentText(taskDesc)
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // 或者 R.mipmap.ic_launcher (不直接引以防止包名报错)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "脚本执行保活",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于在后台执行耗时脚本时保持应用存活"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
