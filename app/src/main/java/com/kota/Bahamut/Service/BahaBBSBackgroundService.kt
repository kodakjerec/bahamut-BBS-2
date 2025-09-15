package com.kota.Bahamut.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.Bahamut.R
import com.kota.Telnet.TelnetClient

class BahaBBSBackgroundService : Service() {
    
    private var client: TelnetClient? = null
    private var controller: ASNavigationController? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null // 不支援綁定
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        // 處理斷線動作
        if (ACTION_DISCONNECT == action) {
            disconnectAndStop()
            return START_NOT_STICKY
        }
        
        client = TelnetClient.getClient()
        controller = ASNavigationController.getCurrentController()
        
        // 啟動前景服務
        startForeground(NOTIFICATION_ID, createNotification())
        
        Log.i("BahaBBS", "BackgroundService start as foreground.")
        return START_STICKY // 確保服務重啟
    }

    override fun onDestroy() {
        super.onDestroy()
        client = null
        controller = null
        Log.i("BahaBBS", "BackgroundService finish.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Bahamut BBS 服務",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "維持 BBS 連線"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        // 創建主要內容的 PendingIntent (點擊通知) - 使用不同方式
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP or
                   Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        val contentIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // 創建斷線按鈕的 PendingIntent
        val disconnectIntent = Intent(this, BahaBBSBackgroundService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bahamut BBS")
            .setContentText("BBS 連線維持中...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉", disconnectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .build()
    }
    
    private fun disconnectAndStop() {
        Log.i("BahaBBS", "User requested disconnect from notification")
        
        // 斷開 Telnet 連線
        client?.close()
        
        // 停止服務
        stopForeground(true)
        stopSelf()
    }

    companion object {
        private const val CHANNEL_ID = "BahaBBSServiceChannel"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_DISCONNECT = "ACTION_DISCONNECT"
    }
}
