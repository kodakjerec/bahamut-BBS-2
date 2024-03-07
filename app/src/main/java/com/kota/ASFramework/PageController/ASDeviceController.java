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
  static final String wakeLockKey = "myapp:wakeLockKey";
  
  static final String wifiLockKey = "myapp:wifiLockKey";
  
  final Context _context;
  
  boolean _wifi_locked = false;
  boolean _wake_locked = true; // 讓CPU保持開啟, 目前不需要作用
  
  final PowerManager.WakeLock mWakeLock;
  
  final WifiManager.WifiLock mWifiLock;
  int _transportType = -1;
  
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

  // 檢查網路狀況
  // return _transportType 網路類型, -1=斷線
  public int isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
    Network activeNetwork = connectivityManager.getActiveNetwork();
    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
    _transportType = -1;
    if (capabilities != null) {
      // any type of internet
      for (int i=0;i<10;i++) {
        if (capabilities.hasTransport(i)) {
          _transportType = i;
          break;
        }
      }
    }
    return _transportType;
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