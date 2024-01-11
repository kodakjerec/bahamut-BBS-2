// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import java.io.PrintStream;

public class ASDeviceController
{

    private static final String WAKELOCK_KEY = "ASFramework_WakeLock";
    private static final String WIFILOCK_KEY = "ASFramework_WifiLock";
    private Context _context;
    private boolean _wifi_locked;
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    public ASDeviceController(Context context)
    {
        _context = null;
        mWakeLock = null;
        mWifiLock = null;
        _wifi_locked = true;
        _context = context;
        mWakeLock = ((PowerManager)_context.getSystemService("power")).newWakeLock(1, "ASFramework_WakeLock");
        mWakeLock.setReferenceCounted(true);
        mWifiLock = ((WifiManager)context.getSystemService("wifi")).createWifiLock("ASFramework_WifiLock");
        mWifiLock.setReferenceCounted(true);
    }

    public NetworkInfo.State getNetworkState(int i)
    {
        NetworkInfo.State state = NetworkInfo.State.DISCONNECTED;
        NetworkInfo networkinfo = ((ConnectivityManager)_context.getSystemService("connectivity")).getNetworkInfo(i);
        if (networkinfo != null)
        {
            state = networkinfo.getState();
        }
        return state;
    }

    public boolean isNetworkAvailable()
    {
        boolean flag = false;
        NetworkInfo.State state = getNetworkState(0);
        NetworkInfo.State state1 = getNetworkState(1);
        NetworkInfo.State state2 = getNetworkState(6);
        NetworkInfo.State state3 = getNetworkState(7);
        NetworkInfo.State state4 = getNetworkState(9);
        if (state == NetworkInfo.State.CONNECTED || state1 == NetworkInfo.State.CONNECTED || state2 == NetworkInfo.State.CONNECTED || state3 == NetworkInfo.State.CONNECTED || state4 == NetworkInfo.State.CONNECTED)
        {
            flag = true;
        }
        return flag;
    }

    public void lockWifi()
    {
        System.out.println("Lock Wifi");
        if (!_wifi_locked)
        {
            _wifi_locked = true;
            mWakeLock.acquire();
            mWifiLock.acquire();
        }
    }

    public void unlockWifi()
    {
        if (_wifi_locked)
        {
            System.out.println("Unlock Wifi");
            try
            {
                mWifiLock.release();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
            try
            {
                mWakeLock.release();
            }
            catch (Exception exception1)
            {
                exception1.printStackTrace();
            }
            _wifi_locked = false;
        }
    }
}
