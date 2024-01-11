// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASProcessingDialog

static final class  extends ASRunner
{

    public void run()
    {
        if (ASProcessingDialog.access$000() != null)
        {
            ASProcessingDialog.access$000().dismiss();
            ASProcessingDialog.access$200();
        }
    }

    ()
    {
    }
}
