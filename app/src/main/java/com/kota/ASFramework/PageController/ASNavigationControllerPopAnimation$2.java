// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationControllerPopAnimation, ASViewController, ASPageView

class this._cls0
    implements android.view.animation.pAnimation._cls2
{

    final ASNavigationControllerPopAnimation this$0;

    public void onAnimationEnd(Animation animation)
    {
        synchronized (ASNavigationControllerPopAnimation.this)
        {
            ASNavigationControllerPopAnimation.access$300(ASNavigationControllerPopAnimation.this).getPageView().onPageAnimationFinished();
            ASNavigationControllerPopAnimation.access$402(ASNavigationControllerPopAnimation.this, true);
            ASNavigationControllerPopAnimation.access$200(ASNavigationControllerPopAnimation.this);
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
        this$0 = ASNavigationControllerPopAnimation.this;
        super();
    }
}
