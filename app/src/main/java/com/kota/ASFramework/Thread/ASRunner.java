package com.kota.ASFramework.Thread;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public abstract class ASRunner {
  static Looper mainLooper = Looper.getMainLooper();
  static Thread _main_thread = null;
  static Handler _main_handler = null;

  static Runnable runnable;

  public abstract void run();

  @SuppressLint("HandlerLeak")
  public static void construct() {
    _main_thread = Thread.currentThread();
    _main_handler = new Handler(mainLooper) {
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
      _main_handler.sendMessage(message);
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
    if (runnable == null)
      runnable = ASRunner.this::run;
    // Call the actual run method after delay
    _main_handler.postDelayed(runnable, delayMillis);
  }
  /** 取消執行 */
  public void cancel() {
    if (runnable!=null)
      _main_handler.removeCallbacks(runnable);
  }
}