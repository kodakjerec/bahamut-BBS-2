// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationControllerPushAnimation, ASViewController, ASPageView

class this._cls0
    implements android.view.animation.hAnimation._cls1
{

    final ASNavigationControllerPushAnimation this$0;

    public void onAnimationEnd(Animation animation)
    {
        synchronized (ASNavigationControllerPushAnimation.this)
        {
            ASNavigationControllerPushAnimation.access$000(ASNavigationControllerPushAnimation.this).getPageView().onPageAnimationFinished();
            ASNavigationControllerPushAnimation.access$102(ASNavigationControllerPushAnimation.this, true);
            ASNavigationControllerPushAnimation.access$200(ASNavigationControllerPushAnimation.this);
        }
        return;
        exception;
        animation;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public void onAnimationRepeat(Animation animation)
    {
    }

    public void onAnimationStart(Animation animation)
    {
    }

    ()
    {
        this$0 = ASNavigationControllerPushAnimation.this;
        super();
    }
}
