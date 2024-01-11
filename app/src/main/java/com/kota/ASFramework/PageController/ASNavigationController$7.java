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

    public void run()
    {
        if (ASNavigationController.access$500(ASNavigationController.this).size() > 0)
        {
            ASNavigationController.access$500(ASNavigationController.this).remove(ASNavigationController.access$500(ASNavigationController.this).size() - 1);
        }
    }

    geCommand()
    {
        this$0 = ASNavigationController.this;
        super(ASNavigationController.this, null);
    }
}
