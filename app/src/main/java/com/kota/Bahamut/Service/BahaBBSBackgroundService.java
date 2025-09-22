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
import com.kota.Bahamut.BahamutController;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;

public class BahaBBSBackgroundService extends Service {
    private static final String TAG = "BahaBBSService";
    private static final String CHANNEL_ID = "BahaBBSServiceChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";

    TelnetClient _client;
    ASNavigationController _controller;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null; // 不支援綁定
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 檢查通知權限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted");
                // 要求通知權限
                BahamutController.checkAndRequestNotificationPermission();
            }
        }

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

        this._client = TelnetClient.getClient();
        this._controller = ASNavigationController.getCurrentController();

        // 嘗試啟動前景服務
        try {
            startForeground(NOTIFICATION_ID, createNotification());
            Log.i(TAG, "BackgroundService started as foreground service.");
        } catch (Exception e) {
            // Android 12+ 可能會拋出 ForegroundServiceStartNotAllowedException
            Log.w(TAG, "Failed to start as foreground service: " + e.getClass().getSimpleName(), e);
            Log.i(TAG, "BackgroundService running as regular service.");
        }

        return START_STICKY; // 確保服務重啟
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this._client = null;
        this._controller = null;
        Log.i("BahaBBS", "BackgroundService finish.");
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
                .setOngoing(true)
                .setAutoCancel(false)
                .setSilent(true)  // Android 15 要求前台服務通知靜音
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void disconnectAndStop() {
        Log.i(TAG, "User requested disconnect from notification");

        // 斷開 Telnet 連線
        if (_client != null) {
            _client.close();
        }

        // 停止服務
        try {
            stopForeground(true);
        } catch (Exception e) {
            // 如果不是前景服務，stopForeground 可能會失敗，這是正常的
            Log.d(TAG, "Not running as foreground service, continuing with stopSelf()");
        }
        stopSelf();
    }
}