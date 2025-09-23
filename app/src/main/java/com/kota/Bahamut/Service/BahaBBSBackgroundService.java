package com.kota.Bahamut.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;

public class BahaBBSBackgroundService extends Service {
    private static final String TAG = "BahaBBSService";
    private static final String CHANNEL_ID = "BahaBBSServiceChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";

    TelnetClient myClient;
    ASNavigationController _controller;
    private boolean _isRunningForeground = false;
    private ASRunner timeoutRunner = null;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null; // 不支援綁定
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "BackgroundService onCreate() called");

        // 預先創建通知頻道，確保 startForeground 能快速執行
        createNotificationChannel();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        // 處理斷線動作
        if (ACTION_DISCONNECT.equals(action)) {
            disconnectAndStop();
            return START_NOT_STICKY;
        }

        // 檢查並處理通知權限
        if (!hasNotificationPermission()) {
            Log.w(TAG, "No notification permission - cannot start foreground service");

            // 沒有權限時無法啟動前台服務，直接停止
            stopSelf();
            return START_NOT_STICKY;
        }

        // 立即嘗試啟動前景服務（在處理任何邏輯前）
        try {
            startForeground(NOTIFICATION_ID, createNotification());
            _isRunningForeground = true;
            Log.i(TAG, "BackgroundService started as foreground service.");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: No notification permission", e);
            _isRunningForeground = false;
            stopSelf();
            return START_NOT_STICKY;
        } catch (Exception e) {
            // Android 12+ 可能會拋出 ForegroundServiceStartNotAllowedException
            _isRunningForeground = false;
            Log.w(TAG, "Failed to start as foreground service: " + e.getClass().getSimpleName(), e);
            Log.i(TAG, "BackgroundService running as regular service.");
            // 如果無法啟動前景服務，立即停止避免異常
            stopSelf();
            return START_NOT_STICKY;
        }

        this.myClient = TelnetClient.getClient();
        this._controller = ASNavigationController.getCurrentController();

        return START_STICKY; // 確保服務重啟
    }

    /**
     * 檢查是否有通知權限
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要 POST_NOTIFICATIONS 權限
            boolean hasPermission = ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

            if (!hasPermission) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
                return false;
            }
        }

        // 檢查通知是否被用戶禁用
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();
            if (!areNotificationsEnabled) {
                Log.w(TAG, "Notifications are disabled by user");
                return false;
            }

            // Android 8.0+ 檢查通知頻道是否被禁用
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Log.w(TAG, "Notification channel is disabled");
                return false;
            }
        }

        return true;
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "BackgroundService onDestroy() called");

        // 立即取消所有等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner.cancel();
            timeoutRunner = null;
        }

        // 確保前景服務狀態正確停止（必須在 onDestroy 開始時立即執行）
        if (_isRunningForeground) {
            try {
                stopForeground(true);
                _isRunningForeground = false;
                Log.d(TAG, "Foreground service stopped in onDestroy");
            } catch (Exception e) {
                Log.w(TAG, "Error stopping foreground service in onDestroy", e);
                // 即使失敗也要繼續
            }
        }

        // 同步快速清理，不使用異步操作
        try {
            if (myClient != null) {
                // 在 onDestroy 中不應該使用異步操作，直接清理引用
                myClient = null;
                Log.d(TAG, "Client reference cleared");
            }
            _controller = null;
        } catch (Exception e) {
            Log.w(TAG, "Error during onDestroy cleanup", e);
        }

        super.onDestroy();
        Log.i(TAG, "BackgroundService onDestroy() completed quickly");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved() called - app was swiped away");

        // 當應用被清除時，立即停止前景服務
        if (_isRunningForeground) {
            try {
                stopForeground(true);
                _isRunningForeground = false;
                Log.d(TAG, "Foreground service stopped due to task removal");
            } catch (Exception e) {
                Log.w(TAG, "Error stopping foreground service in onTaskRemoved", e);
            }
        }

        // 快速清理引用並停止服務
        myClient = null;
        _controller = null;

        // 取消任何等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner.cancel();
            timeoutRunner = null;
        }

        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Bahamut BBS 服務",
                NotificationManager.IMPORTANCE_LOW);
        serviceChannel.setDescription("維持 BBS 連線");
        serviceChannel.setShowBadge(false);
        serviceChannel.enableLights(false);
        serviceChannel.enableVibration(false);
        serviceChannel.setSound(null, null);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
            Log.d(TAG, "Notification channel created");
        }
    }

    private Notification createNotification() {
        // 創建主要內容的 PendingIntent (點擊通知) - 使用不同方式
        Intent notificationIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (notificationIntent != null) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 創建斷線按鈕的 PendingIntent
        Intent disconnectIntent = new Intent(this, BahaBBSBackgroundService.class);
        disconnectIntent.setAction(ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent = PendingIntent.getService(
                this, 1, disconnectIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bahamut BBS")
                .setContentText("BBS 連線維持中...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉", disconnectPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false)
                .setSilent(true) // Android 15 要求前台服務通知靜音
                .setOnlyAlertOnce(true)
                .build();
    }

    private void disconnectAndStop() {
        Log.i(TAG, "User requested disconnect from notification");
        stopServiceGracefully();
    }

    /**
     * 立即停止服務，確保在時限內完成
     */
    private void stopServiceGracefully() {
        Log.i(TAG, "Gracefully stopping service");

        // 立即停止前景服務狀態以避免超時
        if (_isRunningForeground) {
            try {
                stopForeground(true);
                _isRunningForeground = false;
                Log.d(TAG, "Foreground service stopped immediately");
            } catch (Exception e) {
                Log.w(TAG, "Error stopping foreground service", e);
            }
        }

        // 取消任何等待中的任務
        if (timeoutRunner != null) {
            timeoutRunner.cancel();
            timeoutRunner = null;
        }

        // 快速斷線並立即停止服務
        try {
            if (myClient != null) {
                // 在背景執行緒快速關閉連線，但不等待完成
                ASRunner.runInNewThread(() -> {
                    try {
                        myClient.close();
                        Log.d(TAG, "Telnet client closed in background");
                    } catch (Exception e) {
                        Log.w(TAG, "Error closing telnet client", e);
                    }
                });
                // 不等待連線關閉，立即置null
                myClient = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error during client disconnect", e);
        }
    }
}