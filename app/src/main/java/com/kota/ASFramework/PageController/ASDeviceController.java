package com.kota.ASFramework.PageController;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.WindowManager;

public class ASDeviceController {
  private static final String wakeLockKey = "myapp:wakeLockKey";
  
  private static final String wifiLockKey = "myapp:wifiLockKey";
  
  private final Context _context;
  
  private boolean _wifi_locked = false;
  private boolean _wake_locked = true; // 讓CPU保持開啟, 目前不需要作用
  
  private final PowerManager.WakeLock mWakeLock;
  
  private final WifiManager.WifiLock mWifiLock;
  
  public ASDeviceController(Context paramContext) {
    _context = paramContext;

    // create wake-lock
    mWakeLock = ((PowerManager)paramContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockKey);
    mWakeLock.setReferenceCounted(true);

    // create wifi-lock
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, wifiLockKey);
    } else {
      mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, wifiLockKey);
    }
    mWifiLock.setReferenceCounted(true);
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

  public void lockWake() {
    System.out.println("Lock Wake");
    ((Activity)_context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }
  public void unlockWake() {
    ((Activity)_context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  public void lockWifi() {
    System.out.println("Lock Wifi");
    if (!_wifi_locked) {
      _wifi_locked = true;
      mWifiLock.acquire();
    }
    if (!_wake_locked) {
      _wake_locked = true;
      mWakeLock.acquire(60*1000L /*10 minutes*/);
    }
  }
  
  public void unlockWifi() {
    if (_wifi_locked) {
      System.out.println("Unlock Wifi");
      try {
        if (mWifiLock.isHeld())
          mWifiLock.release();
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      _wifi_locked = false;
    }
    if (_wake_locked) {
      System.out.println("Unlock Wake");
      try {
        if (mWakeLock.isHeld())
          mWakeLock.release();
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      _wake_locked = false;
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASDeviceController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */