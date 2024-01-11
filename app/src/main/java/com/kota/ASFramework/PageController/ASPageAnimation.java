// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ASPageAnimation
{

    private static Animation _fade_in_from_left = null;
    private static Animation _fade_in_from_right = null;
    private static Animation _fade_out_to_left = null;
    private static Animation _fade_out_to_right = null;

    public ASPageAnimation()
    {
    }

    public static Animation getFadeInFromLeftAnimation()
    {
        if (_fade_in_from_left == null)
        {
            _fade_in_from_left = new TranslateAnimation(2, -1F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
            init(_fade_in_from_left);
        }
        return _fade_in_from_left;
    }

    public static Animation getFadeInFromRightAnimation()
    {
        if (_fade_in_from_right == null)
        {
            _fade_in_from_right = new TranslateAnimation(2, 1.0F, 2, 0.0F, 2, 0.0F, 2, 0.0F);
            init(_fade_in_from_right);
        }
        return _fade_in_from_right;
    }

    public static Animation getFadeOutToLeftAnimation()
    {
        if (_fade_out_to_left == null)
        {
            _fade_out_to_left = new TranslateAnimation(2, 0.0F, 2, -1F, 2, 0.0F, 2, 0.0F);
            init(_fade_out_to_left);
        }
        return _fade_out_to_left;
    }

    public static Animation getFadeOutTtRightAnimation()
    {
        if (_fade_out_to_right == null)
        {
            _fade_out_to_right = new TranslateAnimation(2, 0.0F, 2, 1.0F, 2, 0.0F, 2, 0.0F);
            init(_fade_out_to_right);
        }
        return _fade_out_to_right;
    }

    private static void init(Animation animation)
    {
        animation.setFillBefore(true);
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
    }

}
