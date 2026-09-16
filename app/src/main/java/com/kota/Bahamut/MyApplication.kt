package com.kota.Bahamut

import android.app.Application
import android.webkit.WebView

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化WebView，避免多次初始化導致的錯誤
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
