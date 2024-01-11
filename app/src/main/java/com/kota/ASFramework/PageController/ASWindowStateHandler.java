// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ASWindowStateHandler
{

    public static Activity activity = null;
    public static int contentViewHeight = -1;
    public static int contentViewWidth = -1;
    public static double screenHeightInch = 0.0D;
    public static int screenHeightPx = 0;
    public static double screenInch = 0.0D;
    public static double screenWidthInch = 0.0D;
    public static int screenWidthPx = 0;
    public static int statusBarHeight = 0;
    public static int titleBarHeight = 0;

    public ASWindowStateHandler()
    {
    }

    public static void construct(Activity activity1)
    {
        activity = activity1;
        activity1 = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(activity1);
        statusBarHeight = ((Rect) (activity1)).top;
        int i = window.findViewById(0x1020002).getTop();
        if (i > 0)
        {
            titleBarHeight = i - statusBarHeight;
        }
        activity1 = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(activity1);
        screenWidthPx = ((DisplayMetrics) (activity1)).widthPixels;
        screenHeightPx = ((DisplayMetrics) (activity1)).heightPixels;
        screenWidthInch = (float)((DisplayMetrics) (activity1)).widthPixels / ((DisplayMetrics) (activity1)).xdpi;
        screenHeightInch = (float)((DisplayMetrics) (activity1)).heightPixels / ((DisplayMetrics) (activity1)).ydpi;
        screenInch = Math.sqrt(Math.pow(screenWidthInch, 2D) + Math.pow(screenHeightInch, 2D));
        contentViewWidth = screenWidthPx;
        contentViewHeight = screenHeightPx - titleBarHeight - statusBarHeight;
    }

}
