package com.kota.ASFramework.PageController;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;

public class ASDeviceController {
  private static final String wakeLockKey = "myapp:wakeLockKey";
  
  private static final String wifiLockKey = "myapp:wifiLockKey";
  
  private final Context _context;
  
  private boolean _wifi_locked = true;
  
  private final PowerManager.WakeLock mWakeLock;
  
  private final WifiManager.WifiLock mWifiLock;
  
  public ASDeviceController(Context paramContext) {
    this._context = paramContext;
    this.mWakeLock = ((PowerManager)paramContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockKey);
    this.mWakeLock.setReferenceCounted(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      this.mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, wifiLockKey);
    } else {
      this.mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, wifiLockKey);
    }
    this.mWifiLock.setReferenceCounted(true);
  }
  
  public boolean isNetworkAvailable() {
    boolean bool = false;
    ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
    Network activeNetwork = connectivityManager.getActiveNetwork();
    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
    if (capabilities != null) {
      // any type of internet
      bool = true;
    }
    return bool;
  }
  
  public void lockWifi() {
    System.out.println("Lock Wifi");
    if (!this._wifi_locked) {
      this._wifi_locked = true;
      this.mWakeLock.acquire(10*60*1000L /*10 minutes*/);
      this.mWifiLock.acquire();
    } 
  }
  
  public void unlockWifi() {
    if (this._wifi_locked) {
      System.out.println("Unlock Wifi");
      try {
        if (this.mWifiLock.isHeld())
          this.mWifiLock.release();
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
      try {
        if (this.mWakeLock.isHeld())
          this.mWakeLock.release();
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
      this._wifi_locked = false;
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASDeviceController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */