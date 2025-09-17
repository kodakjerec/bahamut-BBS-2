package com.kota.asFramework.pageController

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.WindowManager

class ASDeviceController(val context: Context) {
    // 檢查是否正在使用鎖定
    var isWifiLocked: Boolean = false
    var isCpuWakeLocked: Boolean = false

    // create wake-lock
    val mWakeLock: WakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        WAKE_LOCK_KEY
    )
    val mWifiLock: WifiLock
    var transportType: Int = -1

    init {
        mWakeLock.setReferenceCounted(false)

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

    fun lockWake() {
        println("Lock Wake")
        (context as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun unlockWake() {
        (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 單獨鎖定 CPU WakeLock
    fun lockCpuWake() {
        println("Lock CPU Wake")
        if (!this.isCpuWakeLocked) {
            this.isCpuWakeLocked = true
            if (!mWakeLock.isHeld) {
                // 移除時間限制，讓 telnet 連線保持
                mWakeLock.acquire(10*60*1000L /*10 minutes*/)
            }
        }
    }

    fun lockWifi() {
        println("Lock Wifi")
        if (!this.isWifiLocked) {
            this.isWifiLocked = true
            if (!mWifiLock.isHeld) {
                mWifiLock.acquire()
            }
        }
        // 同時鎖定 CPU，確保 telnet 連線不中斷
        lockCpuWake()
    }


    fun unlockCpuWake() {
        if (this.isCpuWakeLocked) {
            println("Unlock CPU Wake")
            try {
                if (mWakeLock.isHeld) {
                    mWakeLock.release()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            this.isCpuWakeLocked = false
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
        // 釋放 CPU WakeLock
        unlockCpuWake()
    }

    // 添加清理方法，避免 memory leak
    fun cleanup() {
        unlockWifi()
    }

    // 添加強制保持連線方法（用於重要的網路操作）
    fun forceKeepConnection() {
        lockWifi()
        lockCpuWake()
    }

    companion object {
        const val WAKE_LOCK_KEY: String = "myapp:wakeLockKey"

        const val WIFI_LOCK_KEY: String = "myapp:wifiLockKey"
    }
}