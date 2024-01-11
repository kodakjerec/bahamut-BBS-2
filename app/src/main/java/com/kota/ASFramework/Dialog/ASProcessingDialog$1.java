// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASProcessingDialog, ASProcessingDialogOnBackDelegate

static final class val.onBackDelegate extends ASRunner
{

    final String val$aMessage;
    final ASProcessingDialogOnBackDelegate val$onBackDelegate;

    public void run()
    {
        if (ASProcessingDialog.access$000() == null)
        {
            ASProcessingDialog.access$100();
        }
        ASProcessingDialog.access$000().setMessage(val$aMessage);
        ASProcessingDialog.access$000().setOnBackDelegate(val$onBackDelegate);
        if (!ASProcessingDialog.access$000().isShowing())
        {
            ASProcessingDialog.access$000().show();
        }
    }

    ackDelegate(String s, ASProcessingDialogOnBackDelegate asprocessingdialogonbackdelegate)
    {
        val$aMessage = s;
        val$onBackDelegate = asprocessingdialogonbackdelegate;
        super();
    }
}
