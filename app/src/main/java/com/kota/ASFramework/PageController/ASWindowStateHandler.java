package com.kota.ASFramework.PageController;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class ASWindowStateHandler {
    public static AppCompatActivity activity = null;
    public static int contentViewHeight = -1;
    public static int contentViewWidth = -1;
    public static double screenHeightInch = 0.0d;
    public static int screenHeightPx = 0;
    public static double screenInch = 0.0d;
    public static double screenWidthInch = 0.0d;
    public static int screenWidthPx = 0;
    public static int statusBarHeight = 0;
    public static int titleBarHeight = 0;

    public static void construct(AppCompatActivity aActivity) {
        activity = aActivity;
        Rect rectgle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        statusBarHeight = rectgle.top;
        int contentViewTop = window.findViewById(16908290).getTop();
        if (contentViewTop > 0) {
            titleBarHeight = contentViewTop - statusBarHeight;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidthPx = metrics.widthPixels;
        screenHeightPx = metrics.heightPixels;
        screenWidthInch = (double) (((float) metrics.widthPixels) / metrics.xdpi);
        screenHeightInch = (double) (((float) metrics.heightPixels) / metrics.ydpi);
        screenInch = Math.sqrt(Math.pow(screenWidthInch, 2.0d) + Math.pow(screenHeightInch, 2.0d));
        contentViewWidth = screenWidthPx;
        contentViewHeight = (screenHeightPx - titleBarHeight) - statusBarHeight;
    }
}
