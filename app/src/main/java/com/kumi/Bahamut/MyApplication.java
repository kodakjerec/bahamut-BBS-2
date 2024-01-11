package com.kumi.Bahamut;

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


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\MyApplication.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */