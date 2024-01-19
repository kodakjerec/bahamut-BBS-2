package com.kota.ASFramework.PageController;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Window;

public class ASWindowStateHandler {
  public static Activity activity;
  
  public static int contentViewHeight;
  
  public static int contentViewWidth;
  
  public static double screenHeightInch;
  
  public static int screenHeightPx;
  
  public static double screenInch;
  
  public static double screenWidthInch;
  
  public static int screenWidthPx;
  
  public static int statusBarHeight = 0;
  
  public static int titleBarHeight = 0;
  
  static {
    contentViewWidth = -1;
    contentViewHeight = -1;
    screenWidthPx = 0;
    screenHeightPx = 0;
    screenWidthInch = 0.0D;
    screenHeightInch = 0.0D;
    screenInch = 0.0D;
    activity = null;
  }
  
  public static void construct(Activity paramActivity) {
    activity = paramActivity;
    Rect rect = new Rect();
    Window window = activity.getWindow();
    window.getDecorView().getWindowVisibleDisplayFrame(rect);
    statusBarHeight = rect.top;
    int i = window.findViewById(android.R.id.content).getTop();
    if (i > 0)
      titleBarHeight = i - statusBarHeight; 
    DisplayMetrics displayMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenWidthPx = displayMetrics.widthPixels;
    screenHeightPx = displayMetrics.heightPixels;
    screenWidthInch = (displayMetrics.widthPixels / displayMetrics.xdpi);
    screenHeightInch = (displayMetrics.heightPixels / displayMetrics.ydpi);
    screenInch = Math.sqrt(Math.pow(screenWidthInch, 2.0D) + Math.pow(screenHeightInch, 2.0D));
    contentViewWidth = screenWidthPx;
    contentViewHeight = screenHeightPx - titleBarHeight - statusBarHeight;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASWindowStateHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */