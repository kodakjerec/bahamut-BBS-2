package com.kota.ASFramework.PageController

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager

class ASDeviceController(private val _context: Context) {
    companion object {
        private const val WAKE_LOCK_KEY = "myapp:wakeLockKey"
        private const val WIFI_LOCK_KEY = "myapp:wifiLockKey"
    }

    private var _wifi_locked = false
    private var _wake_locked = false
    private var _transportType = -1

    private val mWakeLock: PowerManager.WakeLock
    private val mWifiLock: WifiManager.WifiLock

    init {
        // create wake-lock
        val powerManager = _context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_KEY)
        mWakeLock.setReferenceCounted(false)

        // create wifi-lock
        val wifiManager = _context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, WIFI_LOCK_KEY)
        } else {
            wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, WIFI_LOCK_KEY)
        }
        mWifiLock.setReferenceCounted(false)
    }

    // 檢查網路狀況
    // return _transportType 網路類型, -1=斷線
    fun isNetworkAvailable(): Int {
        val connectivityManager = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: Network? = connectivityManager.activeNetwork
        val capabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(activeNetwork)
        _transportType = -1
        
        capabilities?.let { caps ->
            // any type of internet
            for (i in 0 until 10) {
                if (caps.hasTransport(i)) {
                    _transportType = i
                    break
                }
            }
        }
        return _transportType
    }

    fun lockWake() {
        println("Lock Wake")
        (_context as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun unlockWake() {
        (_context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 單獨鎖定 CPU WakeLock
    fun lockCpuWake() {
        println("Lock CPU Wake")
        if (!_wake_locked) {
            _wake_locked = true
            if (!mWakeLock.isHeld) {
                // 移除時間限制，讓 telnet 連線保持
                mWakeLock.acquire()
            }
        }
    }

    fun lockWifi() {
        println("Lock Wifi")
        if (!_wifi_locked) {
            _wifi_locked = true
            if (!mWifiLock.isHeld) {
                mWifiLock.acquire()
            }
        }
        // 同時鎖定 CPU，確保 telnet 連線不中斷
        lockCpuWake()
    }

    fun unlockCpuWake() {
        if (_wake_locked) {
            println("Unlock CPU Wake")
            try {
                if (mWakeLock.isHeld) {
                    mWakeLock.release()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            _wake_locked = false
        }
    }

    fun unlockWifi() {
        if (_wifi_locked) {
            println("Unlock Wifi")
            try {
                if (mWifiLock.isHeld) {
                    mWifiLock.release()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            _wifi_locked = false
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

    // 檢查是否正在使用鎖定
    fun isWifiLocked(): Boolean = _wifi_locked

    fun isCpuWakeLocked(): Boolean = _wake_locked
}
