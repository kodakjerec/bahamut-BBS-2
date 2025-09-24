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
  boolean _wake_locked = false;
  
  final PowerManager.WakeLock mWakeLock;
  final WifiManager.WifiLock mWifiLock;
  int _transportType = -1;
  
  public ASDeviceController(Context paramContext) {
    _context = paramContext;

    // create wake-lock
    mWakeLock = ((PowerManager)paramContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockKey);
    mWakeLock.setReferenceCounted(false);

    // create wifi-lock
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, wifiLockKey);
    } else {
      mWifiLock = ((WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, wifiLockKey);
    }
    mWifiLock.setReferenceCounted(false);
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

  // 單獨鎖定 CPU WakeLock
  public void lockCpuWake() {
    System.out.println("Lock CPU Wake");
    if (!_wake_locked) {
      _wake_locked = true;
      if (!mWakeLock.isHeld()) {
        // 移除時間限制，讓 telnet 連線保持
        mWakeLock.acquire();
      }
    }
  }
  
  public void lockWifi() {
    System.out.println("Lock Wifi");
    if (!_wifi_locked) {
      _wifi_locked = true;
      if (!mWifiLock.isHeld()) {
        mWifiLock.acquire();
      }
    }
    // 同時鎖定 CPU，確保 telnet 連線不中斷
    lockCpuWake();
  }
  
  
  public void unlockCpuWake() {
    if (_wake_locked) {
      System.out.println("Unlock CPU Wake");
      try {
        if (mWakeLock.isHeld()) {
          mWakeLock.release();
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      _wake_locked = false;
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
    // 釋放 CPU WakeLock
    unlockCpuWake();
  }
  
  // 添加清理方法，避免 memory leak
  public void cleanup() {
    unlockWifi();
  }
  
  // 添加強制保持連線方法（用於重要的網路操作）
  public void forceKeepConnection() {
    lockWifi();
    lockCpuWake();
  }
  
  // 檢查是否正在使用鎖定
  public boolean isWifiLocked() {
    return _wifi_locked;
  }
  
  public boolean isCpuWakeLocked() {
    return _wake_locked;
  }
}