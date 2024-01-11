// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import android.view.View;
import android.widget.Button;

// Referenced classes of package com.kumi.ASFramework.Dialog:
//            ASListDialog

class this._cls0
    implements android.view.kListener
{

    final ASListDialog this$0;

    public boolean onLongClick(View view)
    {
        return ASListDialog.access$100(ASListDialog.this, (Button)view);
    }

    ()
    {
        this$0 = ASListDialog.this;
        super();
    }
}
