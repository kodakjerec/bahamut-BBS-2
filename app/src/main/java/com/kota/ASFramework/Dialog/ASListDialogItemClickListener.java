// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;


// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASListDialog

public interface ASListDialogItemClickListener
{

    public abstract void onListDialogItemClicked(ASListDialog aslistdialog, int i, String s);

    public abstract boolean onListDialogItemLongClicked(ASListDialog aslistdialog, int i, String s);
}
