// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationController, ASViewController

class val.aPage
    implements android.view.animation.er
{

    final ASNavigationController this$0;
    final ASViewController val$aPage;

    public void onAnimationEnd(Animation animation)
    {
        removePageView(val$aPage);
    }

    public void onAnimationRepeat(Animation animation)
    {
    }

    public void onAnimationStart(Animation animation)
    {
    }

    ()
    {
        this$0 = final_asnavigationcontroller;
        val$aPage = ASViewController.this;
        super();
    }
}
