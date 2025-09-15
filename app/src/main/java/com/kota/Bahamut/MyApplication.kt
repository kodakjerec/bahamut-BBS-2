package com.kota.Bahamut

import android.app.Application
import android.webkit.WebView

class MyApplication : Application() {
    
    init {
        _instance = this
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
        private var _instance: MyApplication? = null

        fun getInstance(): MyApplication? {
            return _instance
        }
    }
}
