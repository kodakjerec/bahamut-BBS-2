package com.kumi.ASFramework.Thread;

import android.os.Handler;
import android.os.Message;

public abstract class ASRunner {
  private static Handler _main_handler;
  
  private static Thread _main_thread = null;
  
  private long _timeout = 0L;
  
  static {
    _main_handler = null;
  }
  
  public static void construct() {
    _main_thread = Thread.currentThread();
    _main_handler = new Handler() {
        public void handleMessage(Message param1Message) {
          ((ASRunner)param1Message.obj).run();
        }
      };
  }
  
  public static boolean isMainThread() {
    return (Thread.currentThread() == _main_thread);
  }
  
  public long getTimeout() {
    return this._timeout;
  }
  
  public abstract void run();
  
  public ASRunner runInMainThread() {
    if (Thread.currentThread() == _main_thread) {
      run();
      return this;
    } 
    Message message = new Message();
    message.obj = this;
    _main_handler.sendMessage(message);
    return this;
  }
  
  public ASRunner runInNewThread() {
    (new Thread() {
        final ASRunner this$0;
        
        final ASRunner val$runner;
        
        public void run() {
          runner.run();
        }
      }).start();
    return this;
  }
  
  public ASRunner setTimeout(long paramLong) {
    this._timeout = paramLong;
    return this;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Thread\ASRunner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */