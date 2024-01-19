package com.kota.Bahamut.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.Telnet.TelnetClient;

/* loaded from: classes.dex */
public class BahaBBSBackgroundService extends Service {
    TelnetClient _client;
    ASNavigationController _controller;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        this._client = TelnetClient.getClient();
        this._controller = ASNavigationController.getCurrentController();
        Log.i("BahaBBS", "BackgroundService start.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this._client = null;
        this._controller = null;
        Log.i("BahaBBS", "BackgroundService finish.");
    }
}
