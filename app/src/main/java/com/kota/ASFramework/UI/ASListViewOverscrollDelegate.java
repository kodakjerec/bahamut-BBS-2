// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.UI;


// Referenced classes of package com.kumi.ASFramework.UI:
//            ASListView

public interface ASListViewOverscrollDelegate
{

    public abstract void onASListViewDelectedOverscrollBottom(ASListView aslistview);

    public abstract void onASListViewDelectedOverscrollTop(ASListView aslistview);
}
