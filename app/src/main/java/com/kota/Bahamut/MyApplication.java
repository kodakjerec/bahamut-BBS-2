package com.kota.Bahamut;

import android.app.Application;

public class MyApplication extends Application {
    static MyApplication _instance;

    public MyApplication() {
        _instance = this;
    }

    public static MyApplication getInstance() {
        return _instance;
    }
}
