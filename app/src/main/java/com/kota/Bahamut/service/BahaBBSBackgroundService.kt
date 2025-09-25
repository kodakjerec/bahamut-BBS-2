package com.kota.Bahamut.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Telnet.TelnetClient

class BahaBBSBackgroundService : Service() {
    var myClient: TelnetClient? = null
    var myController: ASNavigationController? = null
    private var isRunningForeground = false
    private var timeoutRunner: ASRunner? = null

    // android.app.Service
    override fun onBind(intent: Intent?): IBinder? {
        return null // 不支援綁定
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "BackgroundService onCreate() called")

        // 預先創建通知頻道，確保 startForeground 能快速執行
        createNotificationChannel()
    }

    // android.app.Service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        // 處理斷線動作
        if (ACTION_DISCONNECT == action) {
            disconnectAndStop()
            return START_NOT_STICKY
        }

        // 立即嘗試啟動前景服務（在處理任何邏輯前）
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING)
            else
                startForeground(NOTIFICATION_ID, createNotification())
            isRunningForeground = true
            Log.i(TAG, "BackgroundService started as foreground service.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: No notification permission", e)
            isRunningForeground = false
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            // Android 12+ 可能會拋出 ForegroundServiceStartNotAllowedException
            isRunningForeground = false
            Log.w(TAG, "Failed to start as foreground service: " + e.javaClass.simpleName, e)
            Log.i(TAG, "BackgroundService running as regular service.")
            // 如果無法啟動前景服務，立即停止避免異常
            stopSelf()
            return START_NOT_STICKY
        }

        this.myClient = TelnetClient.getClient()
        this.myController = ASNavigationController.getCurrentController()

        return START_STICKY // 確保服務重啟
    }

    // android.app.Service
    override fun onDestroy() {
        Log.i(TAG, "BackgroundService onDestroy() called")

        // 立即取消所有等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner!!.cancel()
            timeoutRunner = null
        }

        // 確保前景服務狀態正確停止（必須在 onDestroy 開始時立即執行）
        if (isRunningForeground) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
                isRunningForeground = false
                Log.d(TAG, "Foreground service stopped in onDestroy")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping foreground service in onDestroy", e)
                // 即使失敗也要繼續
            }
        }

        // 同步快速清理，不使用異步操作
        try {
            if (myClient != null) {
                // 在 onDestroy 中不應該使用異步操作，直接清理引用
                myClient = null
                Log.d(TAG, "Client reference cleared")
            }
            myController = null
        } catch (e: Exception) {
            Log.w(TAG, "Error during onDestroy cleanup", e)
        }

        super.onDestroy()
        Log.i(TAG, "BackgroundService onDestroy() completed quickly")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i(TAG, "onTaskRemoved() called - app was swiped away")

        // 當應用被清除時，立即停止前景服務
        if (isRunningForeground) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
                isRunningForeground = false
                Log.d(TAG, "Foreground service stopped due to task removal")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping foreground service in onTaskRemoved", e)
            }
        }

        // 快速清理引用並停止服務
        myClient = null
        myController = null

        // 取消任何等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner!!.cancel()
            timeoutRunner = null
        }

        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            getContextString(R.string.background_service_name),
            NotificationManager.IMPORTANCE_LOW
        )
        serviceChannel.description = getContextString(R.string.background_service_description)
        serviceChannel.setShowBadge(false)
        serviceChannel.enableLights(false)
        serviceChannel.enableVibration(false)
        serviceChannel.setSound(null, null)

        val manager = getSystemService(NotificationManager::class.java)
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createNotification(): Notification {
        // 創建主要內容的 PendingIntent (點擊通知) - 使用不同方式
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (notificationIntent != null) {
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // 創建斷線按鈕的 PendingIntent
        val disconnectIntent = Intent(this, BahaBBSBackgroundService::class.java)
        disconnectIntent.action = ACTION_DISCONNECT
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getContextString(R.string.background_service_notification_title))
            .setContentText(getContextString(R.string.background_service_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getContextString(R.string.close),
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setSilent(true) // Android 15 要求前台服務通知靜音
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun disconnectAndStop() {
        Log.i(TAG, "User requested disconnect from notification")
        stopServiceGracefully()
    }

    /**
     * 立即停止服務，確保在時限內完成
     */
    private fun stopServiceGracefully() {
        Log.i(TAG, "Gracefully stopping service")

        // 立即停止前景服務狀態以避免超時
        if (isRunningForeground) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    stopForeground(true)
                }
                isRunningForeground = false
                Log.d(TAG, "Foreground service stopped immediately")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping foreground service", e)
            }
        }

        // 取消任何等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner!!.cancel()
            timeoutRunner = null
        }

        // 快速斷線並立即停止服務
        try {
            if (myClient != null) {
                // 在背景執行緒快速關閉連線，但不等待完成
                ASRunner.runInNewThread {
                    try {
                        myClient!!.close()
                        Log.d(TAG, "Telnet client closed in background")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error closing telnet client", e)
                    }
                }
                // 不等待連線關閉，立即置null
                myClient = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error during client disconnect", e)
        }
    }

    companion object {
        private const val TAG = "BahaBBSService"
        private const val CHANNEL_ID = "BahaBBSServiceChannel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_DISCONNECT = "ACTION_DISCONNECT"
    }
}