package com.kota.Bahamut.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.Bahamut.BahamutController;
import com.kota.Bahamut.R;
import com.kota.Telnet.TelnetClient;

public class BahaBBSBackgroundService extends Service {
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
        
        // 啟動前景服務
        startForeground(NOTIFICATION_ID, createNotification());
        
        Log.i("BahaBBS", "BackgroundService start as foreground.");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Bahamut BBS 服務",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("維持 BBS 連線");
            serviceChannel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
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
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        // 創建斷線按鈕的 PendingIntent
        Intent disconnectIntent = new Intent(this, BahaBBSBackgroundService.class);
        disconnectIntent.setAction(ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bahamut BBS")
            .setContentText("BBS 連線維持中...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "關閉", disconnectPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setNumber(0)
            .build();
    }
    
    private void disconnectAndStop() {
        Log.i("BahaBBS", "User requested disconnect from notification");
        
        // 斷開 Telnet 連線
        if (_client != null) {
            _client.close();
        }
        
        // 停止服務
        stopForeground(true);
        stopSelf();
    }
}