package com.kota.ASFramework.Thread;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ASRunner {
  static Looper mainLooper = Looper.getMainLooper();
  static Thread _main_thread = null;
  static Handler mainHandler = null;
  static Runnable runnable;
  private int token = 0;
  private static final AtomicInteger tokenGenerator = new AtomicInteger();

  public abstract void run();

  @SuppressLint("HandlerLeak")
  public static void construct() {
    _main_thread = Thread.currentThread();
    mainHandler = new Handler(mainLooper) {
      @Override // android.os.Handler
      public void handleMessage(@NonNull Message message) {
        ASRunner runner = (ASRunner) message.obj;
        runner.run();
      }
    };
  }

  public static boolean isMainThread() {
    return Thread.currentThread() == _main_thread;
  }

  /** 在主執行序內執行 */
  public ASRunner runInMainThread() {
    if (Thread.currentThread() == _main_thread) {
      run();
    } else {
      Message message = new Message();
      message.obj = this;
      mainHandler.sendMessage(message);
    }
    return this;
  }

  /** 在新執行序內執行 */
  public static void runInNewThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
  }

  /** 延遲執行 */
  public void postDelayed(int delayMillis) {
    // 先取消之前的任務
    cancel();

    token = tokenGenerator.incrementAndGet();

    runnable = this::run;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      mainHandler.postDelayed(runnable, token, delayMillis);
    } else {
      mainHandler.postDelayed(runnable, delayMillis);
    }
  }
  /** 取消執行 */
  public void cancel() {
    if (runnable!=null) {
      mainHandler.removeCallbacks(runnable, token);
    }
    runnable = null;
  }

  /** 釋放資源 */
  public void release() {
    cancel();
  }
}