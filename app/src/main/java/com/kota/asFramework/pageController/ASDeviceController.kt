package com.kota.asFramework.pageController

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build

/**
 * [ASDeviceController] - 裝置狀態與網路鎖定控制器。
 *
 * 職責：
 * 1. 於背景掛網或保持 Telnet 保持連線時鎖定 Wi-Fi ([lockWifi])，防止休眠斷線。
 * 2. 檢測並回傳當前網路連線狀態與連線類型。
 *
 * @property context 應用程式上下文 Context
 */
class ASDeviceController(val context: Context) {

    /** 檢查是否正在使用 Wi-Fi 鎖定 */
    var isWifiLocked: Boolean = false

    /** Wi-Fi 鎖定物件 */
    val mWifiLock: WifiLock

    /** 當前網路傳輸類型 */
    var transportType: Int = -1

    init {
        mWifiLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ 使用 WIFI_MODE_FULL_LOW_LATENCY
            (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
                WIFI_LOCK_KEY
            )
        } else {
            // API 28 及以下使用 WIFI_MODE_FULL_HIGH_PERF
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                WIFI_LOCK_KEY
            )
        }
        mWifiLock.setReferenceCounted(false)
    }

    /**
     * 檢查當前網路連線狀況
     *
     * @return 傳播介面類型代碼 (-1 代表無網路)
     */
    val isNetworkAvailable: Int
        get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork)
            transportType = -1
            if (capabilities != null) {
                for (i in 0..9) {
                    if (capabilities.hasTransport(i)) {
                        transportType = i
                        break
                    }
                }
            }
            return transportType
        }

    /**
     * 獲取 Wi-Fi 鎖定，防止網路休眠斷線
     */
    fun lockWifi() {
        if (!this.isWifiLocked) {
            this.isWifiLocked = true
            if (!mWifiLock.isHeld) {
                mWifiLock.acquire()
            }
        }
    }

    /**
     * 釋放 Wi-Fi 鎖定
     */
    fun unlockWifi() {
        if (this.isWifiLocked) {
            try {
                if (mWifiLock.isHeld) mWifiLock.release()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            this.isWifiLocked = false
        }
    }

    /**
     * 清理資源，釋放鎖定以避免 Memory Leak
     */
    fun cleanup() {
        unlockWifi()
    }

    companion object {
        const val WIFI_LOCK_KEY: String = "myapp:wifiLockKey"
    }
}
