package com.kota.ASFramework.PageController

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.WindowManager

class ASDeviceController(val _context: Context) {
    // 檢查是否正在使用鎖定
    var isWifiLocked: Boolean = false
    var isCpuWakeLocked: Boolean = false

    val mWakeLock: WakeLock
    val mWifiLock: WifiLock
    var _transportType: Int = -1

    init {
        // create wake-lock
        mWakeLock = (_context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            wakeLockKey
        )
        mWakeLock.setReferenceCounted(false)

        // create wifi-lock
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mWifiLock =
                (_context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                    WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
                    wifiLockKey
                )
        } else {
            mWifiLock =
                (_context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    wifiLockKey
                )
        }
        mWifiLock.setReferenceCounted(false)
    }

    val isNetworkAvailable: Int
        // 檢查網路狀況
        get() {
            val connectivityManager =
                _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.getActiveNetwork()
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork)
            _transportType = -1
            if (capabilities != null) {
                // any type of internet
                for (i in 0..9) {
                    if (capabilities.hasTransport(i)) {
                        _transportType = i
                        break
                    }
                }
            }
            return _transportType
        }

    fun lockWake() {
        println("Lock Wake")
        (_context as Activity).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun unlockWake() {
        (_context as Activity).getWindow()
            .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 單獨鎖定 CPU WakeLock
    fun lockCpuWake() {
        println("Lock CPU Wake")
        if (!this.isCpuWakeLocked) {
            this.isCpuWakeLocked = true
            if (!mWakeLock.isHeld()) {
                // 移除時間限制，讓 telnet 連線保持
                mWakeLock.acquire()
            }
        }
    }

    fun lockWifi() {
        println("Lock Wifi")
        if (!this.isWifiLocked) {
            this.isWifiLocked = true
            if (!mWifiLock.isHeld()) {
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
                if (mWakeLock.isHeld()) {
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
                if (mWifiLock.isHeld()) mWifiLock.release()
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
        const val wakeLockKey: String = "myapp:wakeLockKey"

        const val wifiLockKey: String = "myapp:wifiLockKey"
    }
}