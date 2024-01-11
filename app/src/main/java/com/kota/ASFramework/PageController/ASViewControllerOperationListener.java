// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;


// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewController

public interface ASViewControllerOperationListener
{

    public abstract void onASViewControllerWillAddToNavigationController(ASViewController asviewcontroller);

    public abstract void onASViewControllerWillRemoveFromNavigationController(ASViewController asviewcontroller);
}
