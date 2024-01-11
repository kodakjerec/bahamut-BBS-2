package com.kumi.Bahamut.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.Telnet.TelnetClient;

public class BahaBBSBackgroundService extends Service {
  TelnetClient _client;
  
  ASNavigationController _controller;
  
  public IBinder onBind(Intent paramIntent) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  public void onDestroy() {
    super.onDestroy();
    this._client = null;
    this._controller = null;
    Log.i("BahaBBS", "BackgroundService finish.");
  }
  
  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
    this._client = TelnetClient.getClient();
    this._controller = ASNavigationController.getCurrentController();
    Log.i("BahaBBS", "BackgroundService start.");
    return super.onStartCommand(paramIntent, paramInt1, paramInt2);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Service\BahaBBSBackgroundService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */