// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.ViewGroup;
import com.kumi.ASFramework.Thread.ASRunner;

public class ASViewRemover
{

    private ViewGroup _parent_view;
    private View _target_view;

    public ASViewRemover(ViewGroup viewgroup, View view)
    {
        _parent_view = null;
        _target_view = null;
        _parent_view = viewgroup;
        _target_view = view;
    }

    private void remove()
    {
        (new ASRunner() {

            final ASViewRemover this$0;

            public void run()
            {
                if (_parent_view != null && _target_view != null)
                {
                    _parent_view.removeView(_target_view);
                }
            }

            
            {
                this$0 = ASViewRemover.this;
                super();
            }
        }).runInMainThread();
    }

    public static void remove(ViewGroup viewgroup, View view)
    {
        (new ASViewRemover(viewgroup, view)).start();
    }

    public void start()
    {
        (new ASRunner() {

            final ASViewRemover this$0;

            public void run()
            {
                try
                {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedexception)
                {
                    interruptedexception.printStackTrace();
                }
                remove();
            }

            
            {
                this$0 = ASViewRemover.this;
                super();
            }
        }).runInNewThread();
    }



}
