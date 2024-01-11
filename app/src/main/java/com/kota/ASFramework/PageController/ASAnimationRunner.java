// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.View;
import android.view.animation.Animation;
import com.kumi.ASFramework.Thread.ASRunner;

public abstract class ASAnimationRunner
{

    Animation _animation;

    public ASAnimationRunner(Animation animation)
    {
        _animation = null;
        _animation = animation;
    }

    private void animate()
    {
        (new ASRunner() {

            final ASAnimationRunner this$0;

            public void run()
            {
                View view = getTargetView();
                if (view != null)
                {
                    view.startAnimation(_animation);
                }
            }

            
            {
                this$0 = ASAnimationRunner.this;
                super();
            }
        }).runInMainThread();
    }

    private void fail()
    {
        (new ASRunner() {

            final ASAnimationRunner this$0;

            public void run()
            {
                onAnimationStartFail();
            }

            
            {
                this$0 = ASAnimationRunner.this;
                super();
            }
        }).runInMainThread();
    }

    abstract View getTargetView();

    abstract void onAnimationStartFail();

    public void start()
    {
        (new ASRunner() {

            final ASAnimationRunner this$0;

            public void run()
            {
                int i = 0;
                boolean flag1 = false;
                do
                {
label0:
                    {
                        boolean flag = flag1;
                        if (i < 10)
                        {
                            if (getTargetView() == null)
                            {
                                break label0;
                            }
                            animate();
                            flag = true;
                        }
                        if (!flag)
                        {
                            fail();
                        }
                        return;
                    }
                    i++;
                    try
                    {
                        Thread.sleep(10L);
                    }
                    catch (InterruptedException interruptedexception)
                    {
                        interruptedexception.printStackTrace();
                    }
                } while (true);
            }

            
            {
                this$0 = ASAnimationRunner.this;
                super();
            }
        }).runInNewThread();
    }


}
