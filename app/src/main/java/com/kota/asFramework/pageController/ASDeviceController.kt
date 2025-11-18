package com.kota.asFramework.pageController

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build

class ASDeviceController(val context: Context) {
    // 檢查是否正在使用鎖定
    var isWifiLocked: Boolean = false

    val mWifiLock: WifiLock
    var transportType: Int = -1

    init {
        // create wifi-lock
        mWifiLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ 使用 WIFI_MODE_FULL_LOW_LATENCY
            (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
                WIFI_LOCK_KEY
            )
        } else {
            // API 28 及以下使用 WIFI_MODE_FULL_HIGH_PERF (已棄用但仍可用於舊版本)
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                WIFI_LOCK_KEY
            )
        }
        mWifiLock.setReferenceCounted(false)
    }

    val isNetworkAvailable: Int
        // 檢查網路狀況
        get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork)
            transportType = -1
            if (capabilities != null) {
                // any type of internet
                for (i in 0..9) {
                    if (capabilities.hasTransport(i)) {
                        transportType = i
                        break
                    }
                }
            }
            return transportType
        }

    fun lockWifi() {
        println("Lock Wifi")
        if (!this.isWifiLocked) {
            this.isWifiLocked = true
            if (!mWifiLock.isHeld) {
                mWifiLock.acquire()
            }
        }
    }

    fun unlockWifi() {
        if (this.isWifiLocked) {
            println("Unlock Wifi")
            try {
                if (mWifiLock.isHeld) mWifiLock.release()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            this.isWifiLocked = false
        }
    }

    // 添加清理方法，避免 memory leak
    fun cleanup() {
        unlockWifi()
    }

    companion object {

        const val WIFI_LOCK_KEY: String = "myapp:wifiLockKey"
    }
}