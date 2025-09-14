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
    
    DisplayMetrics displayMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenWidthPx = displayMetrics.widthPixels;
    screenHeightPx = displayMetrics.heightPixels;
    screenWidthInch = (displayMetrics.widthPixels / displayMetrics.xdpi);
    screenHeightInch = (displayMetrics.heightPixels / displayMetrics.ydpi);
    screenInch = Math.sqrt(Math.pow(screenWidthInch, 2.0D) + Math.pow(screenHeightInch, 2.0D));
    contentViewWidth = screenWidthPx;
    
    // 適應 edge-to-edge 的處理
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      // 在 edge-to-edge 模式下，初始設定為全螢幕
      // 實際的 insets 會由 WindowInsets 處理
      statusBarHeight = 0;
      titleBarHeight = 0;
      contentViewHeight = screenHeightPx;
    } else {
      // 舊版本的處理方式
      Rect rect = new Rect();
      Window window = activity.getWindow();
      window.getDecorView().getWindowVisibleDisplayFrame(rect);
      statusBarHeight = rect.top;
      int i = window.findViewById(android.R.id.content).getTop();
      if (i > 0)
        titleBarHeight = i - statusBarHeight; 
      contentViewHeight = screenHeightPx - titleBarHeight - statusBarHeight;
    }
  }
  
  /**
   * 更新 WindowInsets 資訊（用於 edge-to-edge 模式）
   */
  public static void updateWindowInsets(int top, int bottom, int left, int right) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      statusBarHeight = top;
      // 更新內容視圖高度，扣除上下的系統欄
      contentViewHeight = screenHeightPx - top - bottom;
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASWindowStateHandler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */