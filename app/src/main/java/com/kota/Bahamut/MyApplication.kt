package com.kota.Bahamut

import android.app.Application
import android.webkit.WebView

/**
 * [MyApplication] - 全域 Application 類別。
 *
 * 職責：
 * 1. 初始化應用程式環境。
 * 2. 設定 WebView 除錯狀態，避免重複初始化問題。
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 WebView 除錯設定，避免多次初始化導致的錯誤
        try {
            WebView.setWebContentsDebuggingEnabled(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}
