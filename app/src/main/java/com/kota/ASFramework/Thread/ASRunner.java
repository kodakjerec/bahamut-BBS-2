package com.kota.ASFramework.Thread;

import android.os.Handler;
import android.os.Message;

public abstract class ASRunner {
    private static Handler _main_handler = null;
    private static Thread _main_thread = null;
    private long _timeout = 0;

    public abstract void run();

    public static void construct() {
        _main_thread = Thread.currentThread();
        _main_handler = new Handler() {
            public void handleMessage(Message message) {
                ((ASRunner) message.obj).run();
            }
        };
    }

    public static boolean isMainThread() {
        return Thread.currentThread() == _main_thread;
    }

    public ASRunner runInMainThread() {
        if (Thread.currentThread() == _main_thread) {
            run();
        } else {
            Message message = new Message();
            message.obj = this;
            _main_handler.sendMessage(message);
        }
        return this;
    }

    public ASRunner runInNewThread() {
        new Thread() {
            public void run() {
                this.run();
            }
        }.start();
        return this;
    }

    public long getTimeout() {
        return this._timeout;
    }

    public ASRunner setTimeout(long timeout) {
        this._timeout = timeout;
        return this;
    }
}
