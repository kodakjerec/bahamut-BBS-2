// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import java.util.Vector;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationController

class geCommand extends geCommand
{

    final ASNavigationController this$0;
    final Vector val$aControllerList;

    public void run()
    {
        if (val$aControllerList != null)
        {
            ASNavigationController.access$500(ASNavigationController.this).removeAllElements();
            ASNavigationController.access$500(ASNavigationController.this).addAll(val$aControllerList);
        }
    }

    geCommand()
    {
        this$0 = final_asnavigationcontroller;
        val$aControllerList = Vector.this;
        super(final_asnavigationcontroller, null);
    }
}
