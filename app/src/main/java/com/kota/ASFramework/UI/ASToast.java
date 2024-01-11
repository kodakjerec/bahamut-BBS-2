// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.UI;

import android.widget.Toast;
import com.kumi.ASFramework.PageController.ASNavigationController;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASToast
{

    public ASToast()
    {
    }

    public static void showLongToast(String s)
    {
        (new ASRunner(s) {

            final String val$aToastMessage;

            public void run()
            {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 1).show();
            }

            
            {
                aToastMessage = s;
                super();
            }
        }).runInMainThread();
    }

    public static void showShortToast(String s)
    {
        (new ASRunner(s) {

            final String val$aToastMessage;

            public void run()
            {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 0).show();
            }

            
            {
                aToastMessage = s;
                super();
            }
        }).runInMainThread();
    }
}
