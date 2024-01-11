package com.kota.Bahamut.SelfFramework.Thread;

import android.os.Looper;

public class ASRunner {
    public static boolean isMainThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            // Running on the main thread
            return true;
        } else {
            // Not running on the main thread
            return false;
        }
    }
}
