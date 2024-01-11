// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Thread;

import android.os.Handler;
import android.os.Message;

public abstract class ASRunner
{

    private static Handler _main_handler = null;
    private static Thread _main_thread = null;
    private long _timeout;

    public ASRunner()
    {
        _timeout = 0L;
    }

    public static void construct()
    {
        _main_thread = Thread.currentThread();
        _main_handler = new Handler() {

            public void handleMessage(Message message)
            {
                ((ASRunner)message.obj).run();
            }

        };
    }

    public static boolean isMainThread()
    {
        return Thread.currentThread() == _main_thread;
    }

    public long getTimeout()
    {
        return _timeout;
    }

    public abstract void run();

    public ASRunner runInMainThread()
    {
        if (Thread.currentThread() == _main_thread)
        {
            run();
            return this;
        } else
        {
            Message message = new Message();
            message.obj = this;
            _main_handler.sendMessage(message);
            return this;
        }
    }

    public ASRunner runInNewThread()
    {
        (new Thread() {

            final ASRunner this$0;
            final ASRunner val$runner;

            public void run()
            {
                runner.run();
            }

            
            {
                this$0 = ASRunner.this;
                runner = asrunner1;
                super();
            }
        }).start();
        return this;
    }

    public ASRunner setTimeout(long l)
    {
        _timeout = l;
        return this;
    }

}
