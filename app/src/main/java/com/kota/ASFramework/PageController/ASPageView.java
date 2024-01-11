// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewController

public class ASPageView extends FrameLayout
{

    private static int _count = 0;
    private ASViewController _owner_controller;

    public ASPageView(Context context)
    {
        super(context);
        _owner_controller = null;
        init();
    }

    public ASPageView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        _owner_controller = null;
        init();
    }

    public ASPageView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
        _owner_controller = null;
        init();
    }

    private void init()
    {
    }

    public void draw(Canvas canvas)
    {
        int i = canvas.getSaveCount();
        dispatchDraw(canvas);
        canvas.restoreToCount(i);
    }

    protected void finalize()
        throws Throwable
    {
        super.finalize();
    }

    public ASViewController getOwnerController()
    {
        return _owner_controller;
    }

    protected void onDraw(Canvas canvas)
    {
    }

    public void onPageAnimationFinished()
    {
    }

    public void onPageAnimationStart()
    {
    }

    public void setOwnerController(ASViewController asviewcontroller)
    {
        _owner_controller = asviewcontroller;
    }

}
