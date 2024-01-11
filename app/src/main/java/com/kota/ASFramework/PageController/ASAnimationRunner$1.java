// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASAnimationRunner

class this._cls0 extends ASRunner
{

    final ASAnimationRunner this$0;

    public void run()
    {
        int i = 0;
        boolean flag1 = false;
        do
        {
label0:
            {
                boolean flag = flag1;
                if (i < 10)
                {
                    if (getTargetView() == null)
                    {
                        break label0;
                    }
                    ASAnimationRunner.access$000(ASAnimationRunner.this);
                    flag = true;
                }
                if (!flag)
                {
                    ASAnimationRunner.access$100(ASAnimationRunner.this);
                }
                return;
            }
            i++;
            try
            {
                Thread.sleep(10L);
            }
            catch (InterruptedException interruptedexception)
            {
                interruptedexception.printStackTrace();
            }
        } while (true);
    }

    ()
    {
        this$0 = ASAnimationRunner.this;
        super();
    }
}
