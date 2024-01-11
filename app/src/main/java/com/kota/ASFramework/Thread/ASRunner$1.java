// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Thread;

import android.os.Handler;
import android.os.Message;

// Referenced classes of package com.kumi.ASFramework.Thread:
//            ASRunner

static final class  extends Handler
{

    public void handleMessage(Message message)
    {
        ((ASRunner)message.obj).run();
    }

    ()
    {
    }
}
