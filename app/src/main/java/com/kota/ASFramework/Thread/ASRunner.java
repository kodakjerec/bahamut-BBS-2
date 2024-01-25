package com.kota.ASFramework.Thread;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

/* loaded from: classes.dex */
public abstract class ASRunner {
  private long _timeout = 0;
  private static Thread _main_thread = null;
  private static Handler _main_handler = null;

  public abstract void run();

  @SuppressLint("HandlerLeak")
  public static void construct() {
    _main_thread = Thread.currentThread();
    _main_handler = new Handler() { // from class: com.kumi.ASFramework.Thread.ASRunner.1
      @Override // android.os.Handler
      public void handleMessage(Message message) {
        ASRunner runner = (ASRunner) message.obj;
        runner.run();
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

  public static void runInNewThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
}

  public long getTimeout() {
    return this._timeout;
  }

  public ASRunner setTimeout(long timeout) {
    this._timeout = timeout;
    return this;
  }
}