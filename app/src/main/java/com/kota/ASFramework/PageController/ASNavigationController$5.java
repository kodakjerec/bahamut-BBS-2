// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.View;
import java.util.Iterator;
import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationControllerPushAnimation, ASNavigationController, ASViewController, ASNavigationControllerView, 
//            ASPageView

class hAnimation extends ASNavigationControllerPushAnimation
{

    final ASNavigationController this$0;
    final ASViewController val$sourceController;
    final ASViewController val$targetController;

    public void onAnimationFinished()
    {
        if (val$sourceController != null)
        {
            val$sourceController.onPageDidDisappear();
        }
        if (val$targetController != null)
        {
            Object obj = new Vector();
            for (int i = 0; i < ASNavigationController.access$000(ASNavigationController.this).getContentView().getChildCount(); i++)
            {
                View view = ASNavigationController.access$000(ASNavigationController.this).getContentView().getChildAt(i);
                if (view != val$targetController.getPageView())
                {
                    ((Vector) (obj)).add(view);
                }
            }

            View view1;
            for (obj = ((Vector) (obj)).iterator(); ((Iterator) (obj)).hasNext(); ASNavigationController.access$000(ASNavigationController.this).getContentView().removeView(view1))
            {
                view1 = (View)((Iterator) (obj)).next();
            }

        }
        Iterator iterator = ASNavigationController.access$200(ASNavigationController.this).iterator();
        do
        {
            if (!iterator.hasNext())
            {
                break;
            }
            ASViewController asviewcontroller = (ASViewController)iterator.next();
            if (asviewcontroller != val$targetController && asviewcontroller.getPageView() != null)
            {
                ASNavigationController.access$100(ASNavigationController.this, asviewcontroller);
            }
        } while (true);
        if (val$targetController != null)
        {
            val$targetController.notifyPageDidAppear();
        }
        ASNavigationController.access$300(ASNavigationController.this);
    }

    w(ASViewController asviewcontroller2, ASViewController asviewcontroller3)
    {
        this$0 = final_asnavigationcontroller;
        val$sourceController = asviewcontroller2;
        val$targetController = asviewcontroller3;
        super(final_asviewcontroller, ASViewController.this);
    }
}
