package com.kota.ASFramework.PageController;

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

    public ASDeviceController(Context context) {
        this._context = context;
        this.mWakeLock = ((PowerManager) this._context.getSystemService("power")).newWakeLock(1, WAKELOCK_KEY);
        this.mWakeLock.setReferenceCounted(true);
        this.mWifiLock = ((WifiManager) context.getSystemService("wifi")).createWifiLock(WIFILOCK_KEY);
        this.mWifiLock.setReferenceCounted(true);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.mWakeLock.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            this._wifi_locked = false;
        }
    }

    public NetworkInfo.State getNetworkState(int networkType) {
        NetworkInfo.State state = NetworkInfo.State.DISCONNECTED;
        NetworkInfo info = ((ConnectivityManager) this._context.getSystemService("connectivity")).getNetworkInfo(networkType);
        if (info != null) {
            return info.getState();
        }
        return state;
    }

    public boolean isNetworkAvailable() {
        NetworkInfo.State mobile = getNetworkState(0);
        NetworkInfo.State wifi = getNetworkState(1);
        NetworkInfo.State wimax = getNetworkState(6);
        NetworkInfo.State bluetooth = getNetworkState(7);
        NetworkInfo.State default_ethernet = getNetworkState(9);
        if (mobile == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTED || wimax == NetworkInfo.State.CONNECTED || bluetooth == NetworkInfo.State.CONNECTED || default_ethernet == NetworkInfo.State.CONNECTED) {
            return true;
        }
        return false;
    }
}
