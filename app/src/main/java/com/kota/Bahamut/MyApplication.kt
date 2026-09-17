package com.kota.Bahamut

import android.app.Application
import android.webkit.WebView
import com.google.android.gms.games.PlayGamesSdk

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 Google Play Games SDK
        PlayGamesSdk.initialize(this)

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
