package com.kumi.ASFramework.PageController;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

public class ASDeviceController {
  private static final String WAKELOCK_KEY = "ASFramework_WakeLock";
  
  private static final String WIFILOCK_KEY = "ASFramework_WifiLock";
  
  private Context _context = null;
  
  private boolean _wifi_locked = true;
  
  private PowerManager.WakeLock mWakeLock = null;
  
  private WifiManager.WifiLock mWifiLock = null;
  
  public ASDeviceController(Context paramContext) {
    this._context = paramContext;
    this.mWakeLock = ((PowerManager)this._context.getSystemService("power")).newWakeLock(1, "ASFramework_WakeLock");
    this.mWakeLock.setReferenceCounted(true);
    this.mWifiLock = ((WifiManager)paramContext.getSystemService("wifi")).createWifiLock("ASFramework_WifiLock");
    this.mWifiLock.setReferenceCounted(true);
  }
  
  public NetworkInfo.State getNetworkState(int paramInt) {
    NetworkInfo.State state = NetworkInfo.State.DISCONNECTED;
    NetworkInfo networkInfo = ((ConnectivityManager)this._context.getSystemService("connectivity")).getNetworkInfo(paramInt);
    if (networkInfo != null)
      state = networkInfo.getState(); 
    return state;
  }
  
  public boolean isNetworkAvailable() {
    boolean bool = false;
    NetworkInfo.State state4 = getNetworkState(0);
    NetworkInfo.State state1 = getNetworkState(1);
    NetworkInfo.State state5 = getNetworkState(6);
    NetworkInfo.State state2 = getNetworkState(7);
    NetworkInfo.State state3 = getNetworkState(9);
    if (state4 == NetworkInfo.State.CONNECTED || state1 == NetworkInfo.State.CONNECTED || state5 == NetworkInfo.State.CONNECTED || state2 == NetworkInfo.State.CONNECTED || state3 == NetworkInfo.State.CONNECTED)
      bool = true; 
    return bool;
  }
  
  public void lockWifi() {
    System.out.println("Lock Wifi");
    if (!this._wifi_locked) {
      this._wifi_locked = true;
      this.mWakeLock.acquire();
      this.mWifiLock.acquire();
    } 
  }
  
  public void unlockWifi() {
    if (this._wifi_locked) {
      System.out.println("Unlock Wifi");
      try {
        this.mWifiLock.release();
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
      try {
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