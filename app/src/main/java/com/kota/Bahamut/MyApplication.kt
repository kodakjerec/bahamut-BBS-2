package com.kota.Bahamut

import android.app.Application
import android.webkit.WebView

class MyApplication : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()


        // 初始化WebView，避免多次初始化導致的錯誤
        try {
            WebView.setWebContentsDebuggingEnabled(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var instance: MyApplication?
            get() {
                TODO()
            }
            set(value) {}
    }
}
