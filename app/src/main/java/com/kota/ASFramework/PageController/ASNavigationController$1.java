// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.View;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationController, ASViewController, ASNavigationControllerView

class val.page_view
    implements Runnable
{

    final ASNavigationController this$0;
    final ASViewController val$aPage;
    final View val$page_view;

    public void run()
    {
        val$aPage.onPageDidDisappear();
        ASNavigationController.access$000(ASNavigationController.this).removeView(val$page_view);
    }

    w()
    {
        this$0 = final_asnavigationcontroller;
        val$aPage = asviewcontroller;
        val$page_view = View.this;
        super();
    }
}
