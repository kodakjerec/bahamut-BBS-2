// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.ViewGroup;
import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewRemover

class this._cls0 extends ASRunner
{

    final ASViewRemover this$0;

    public void run()
    {
        if (ASViewRemover.access$100(ASViewRemover.this) != null && ASViewRemover.access$200(ASViewRemover.this) != null)
        {
            ASViewRemover.access$100(ASViewRemover.this).removeView(ASViewRemover.access$200(ASViewRemover.this));
        }
    }

    _cls9()
    {
        this$0 = ASViewRemover.this;
        super();
    }
}
