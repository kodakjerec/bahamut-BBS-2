package com.kota.Bahamut;

import android.app.Application;
import android.webkit.WebView;

public class MyApplication extends Application {
    static MyApplication _instance;

    public MyApplication() {
        _instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化WebView，避免多次初始化導致的錯誤
        try {
            WebView.setWebContentsDebuggingEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MyApplication getInstance() {
        return _instance;
    }
}
