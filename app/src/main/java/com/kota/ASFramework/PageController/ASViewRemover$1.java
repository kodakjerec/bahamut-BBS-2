// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewRemover

class this._cls0 extends ASRunner
{

    final ASViewRemover this$0;

    public void run()
    {
        try
        {
            Thread.sleep(1000L);
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
        ASViewRemover.access$000(ASViewRemover.this);
    }

    _cls9()
    {
        this$0 = ASViewRemover.this;
        super();
    }
}
