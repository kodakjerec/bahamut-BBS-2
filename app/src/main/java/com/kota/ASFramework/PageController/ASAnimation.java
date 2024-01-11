// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASNavigationController

public class ASAnimation
{

    private static int animation_duration = 250;

    public ASAnimation()
    {
    }

    public static Animation getFadeInFromLeftAnimation()
    {
        TranslateAnimation translateanimation = new TranslateAnimation(1, -1F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
        translateanimation.setDuration(animation_duration);
        translateanimation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
        return translateanimation;
    }

    public static Animation getFadeInFromRightAnimation()
    {
        TranslateAnimation translateanimation = new TranslateAnimation(1, 1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
        translateanimation.setDuration(animation_duration);
        translateanimation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
        return translateanimation;
    }

    public static Animation getFadeOutToLeftAnimation()
    {
        TranslateAnimation translateanimation = new TranslateAnimation(1, 0.0F, 2, -1F, 2, 0.0F, 2, 0.0F);
        translateanimation.setDuration(animation_duration);
        translateanimation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
        return translateanimation;
    }

    public static Animation getFadeOutToRightAnimation()
    {
        TranslateAnimation translateanimation = new TranslateAnimation(1, 0.0F, 2, 1.0F, 2, 0.0F, 2, 0.0F);
        translateanimation.setDuration(animation_duration);
        translateanimation.setInterpolator(ASNavigationController.getCurrentController(), 0x10a0004);
        return translateanimation;
    }

}
