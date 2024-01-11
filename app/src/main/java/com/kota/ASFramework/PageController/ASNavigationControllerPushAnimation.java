// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import com.kumi.ASFramework.Thread.ASRunner;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASViewController, ASPageAnimation, ASNavigationController, ASPageView

public class ASNavigationControllerPushAnimation
{

    private boolean _finished;
    private Animation _source_animation;
    private ASViewController _source_controller;
    private boolean _source_finished;
    private Animation _target_animation;
    private ASViewController _target_controller;
    private boolean _target_finished;
    private int animation_duration;

    public ASNavigationControllerPushAnimation(ASViewController asviewcontroller, ASViewController asviewcontroller1)
    {
        animation_duration = 250;
        _source_controller = null;
        _target_controller = null;
        _source_finished = false;
        _target_finished = false;
        _finished = false;
        _source_animation = null;
        _target_animation = null;
        _source_controller = asviewcontroller;
        _target_controller = asviewcontroller1;
    }

    private void animate()
    {
        if (_source_controller == null || _source_controller.getPageView() == null) goto _L2; else goto _L1
_L1:
        _source_animation = ASPageAnimation.getFadeOutToLeftAnimation();
        _source_animation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
        _source_animation.setDuration(animation_duration);
        _source_animation.setAnimationListener(new Animation.AnimationListener() {

            final ASNavigationControllerPushAnimation this$0;

            public void onAnimationEnd(Animation animation)
            {
                synchronized (ASNavigationControllerPushAnimation.this)
                {
                    _source_controller.getPageView().onPageAnimationFinished();
                    _source_finished = true;
                    checkFinished();
                }
                return;
                exception1;
                animation;
                JVM INSTR monitorexit ;
                throw exception1;
            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }

            
            {
                this$0 = ASNavigationControllerPushAnimation.this;
                super();
            }
        });
        _source_controller.getPageView().onPageAnimationStart();
        _source_controller.getPageView().startAnimation(_source_animation);
_L4:
        if (_target_controller != null && _target_controller.getPageView() != null)
        {
            _target_animation = ASPageAnimation.getFadeInFromRightAnimation();
            _target_animation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
            _target_animation.setDuration(animation_duration);
            _target_animation.setAnimationListener(new Animation.AnimationListener() {

                final ASNavigationControllerPushAnimation this$0;

                public void onAnimationEnd(Animation animation)
                {
                    synchronized (ASNavigationControllerPushAnimation.this)
                    {
                        _target_controller.getPageView().onPageAnimationFinished();
                        _target_finished = true;
                        checkFinished();
                    }
                    return;
                    exception1;
                    animation;
                    JVM INSTR monitorexit ;
                    throw exception1;
                }

                public void onAnimationRepeat(Animation animation)
                {
                }

                public void onAnimationStart(Animation animation)
                {
                }

            
            {
                this$0 = ASNavigationControllerPushAnimation.this;
                super();
            }
            });
            _target_controller.getPageView().onPageAnimationStart();
            _target_controller.getPageView().startAnimation(_target_animation);
            return;
        }
        break; /* Loop/switch isn't completed */
_L2:
        this;
        JVM INSTR monitorenter ;
        _source_finished = true;
        checkFinished();
        this;
        JVM INSTR monitorexit ;
        if (true) goto _L4; else goto _L3
        Exception exception;
        exception;
        this;
        JVM INSTR monitorexit ;
        throw exception;
_L3:
        this;
        JVM INSTR monitorenter ;
        _target_finished = true;
        checkFinished();
        this;
        JVM INSTR monitorexit ;
        return;
        exception;
        this;
        JVM INSTR monitorexit ;
        throw exception;
    }

    private void checkFinished()
    {
        if (_source_finished && _target_finished)
        {
            finish();
        }
    }

    private void finish()
    {
        if (!_finished)
        {
            (new ASRunner() {

                final ASNavigationControllerPushAnimation this$0;

                public void run()
                {
                    onAnimationFinished();
                }

            
            {
                this$0 = ASNavigationControllerPushAnimation.this;
                super();
            }
            }).runInMainThread();
            _finished = true;
        }
    }

    public void onAnimationFinished()
    {
    }

    public void start(boolean flag)
    {
        if (!flag)
        {
            finish();
            return;
        } else
        {
            animate();
            return;
        }
    }



/*
    static boolean access$102(ASNavigationControllerPushAnimation asnavigationcontrollerpushanimation, boolean flag)
    {
        asnavigationcontrollerpushanimation._source_finished = flag;
        return flag;
    }

*/




/*
    static boolean access$402(ASNavigationControllerPushAnimation asnavigationcontrollerpushanimation, boolean flag)
    {
        asnavigationcontrollerpushanimation._target_finished = flag;
        return flag;
    }

*/
}
