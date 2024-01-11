// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.View;
import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASAnimationRunner

class this._cls0 extends ASRunner
{

    final ASAnimationRunner this$0;

    public void run()
    {
        View view = getTargetView();
        if (view != null)
        {
            view.startAnimation(_animation);
        }
    }

    ()
    {
        this$0 = ASAnimationRunner.this;
        super();
    }
}
