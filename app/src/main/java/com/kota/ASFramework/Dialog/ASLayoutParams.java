// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.Dialog;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ASLayoutParams
{

    private static ASLayoutParams _instance = null;
    private float _default_touch_block_height;
    private float _default_touch_block_width;
    private float _dialog_width_large;
    private float _dialog_width_normal;
    private float _padding_large;
    private float _padding_normal;
    private float _padding_small;
    private float _text_size_large;
    private float _text_size_normal;
    private float _text_size_small;
    private float _text_size_ultra_large;

    private ASLayoutParams()
    {
        _text_size_small = 16F;
        _text_size_normal = 20F;
        _text_size_large = 24F;
        _text_size_ultra_large = 28F;
        _dialog_width_normal = 270F;
        _dialog_width_large = 320F;
        _default_touch_block_width = 60F;
        _default_touch_block_height = 60F;
        _padding_small = 5F;
        _padding_normal = 10F;
        _padding_large = 20F;
        initial();
    }

    public static ASLayoutParams getInstance()
    {
        if (_instance == null)
        {
            _instance = new ASLayoutParams();
        }
        return _instance;
    }

    private void initial()
    {
    }

    public Drawable getAlertItemBackgroundDrawable()
    {
        StateListDrawable statelistdrawable = new StateListDrawable();
        ColorDrawable colordrawable = new ColorDrawable(-14066);
        statelistdrawable.addState(new int[] {
            0x10100a7, 0x101009e
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff800000);
        statelistdrawable.addState(new int[] {
            0x101009e, 0x101009c
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff400000);
        statelistdrawable.addState(new int[] {
            0x101009e
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff200000);
        statelistdrawable.addState(new int[0], colordrawable);
        return statelistdrawable;
    }

    public ColorStateList getAlertItemTextColor()
    {
        return new ColorStateList(new int[][] {
            new int[] {
                0x10100a7, 0x101009e
            }, new int[] {
                0x101009e, 0x101009c
            }, new int[] {
                0x101009e
            }, new int[0]
        }, new int[] {
            0xff000000, 0xff000000, -1, 0xff808080
        });
    }

    public float getDefaultTouchBlockHeight()
    {
        return _default_touch_block_height;
    }

    public float getDefaultTouchBlockWidth()
    {
        return _default_touch_block_width;
    }

    public float getDialogWidthLarge()
    {
        return _dialog_width_large;
    }

    public float getDialogWidthNormal()
    {
        return _dialog_width_normal;
    }

    public Drawable getListItemBackgroundDrawable()
    {
        StateListDrawable statelistdrawable = new StateListDrawable();
        ColorDrawable colordrawable = new ColorDrawable(-14066);
        statelistdrawable.addState(new int[] {
            0x10100a7, 0x101009e
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff000000);
        statelistdrawable.addState(new int[] {
            0x101009e, 0x101009c
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff000000);
        statelistdrawable.addState(new int[] {
            0x101009e
        }, colordrawable);
        colordrawable = new ColorDrawable(0xff000000);
        statelistdrawable.addState(new int[0], colordrawable);
        return statelistdrawable;
    }

    public ColorStateList getListItemTextColor()
    {
        return new ColorStateList(new int[][] {
            new int[] {
                0x10100a7, 0x101009e
            }, new int[] {
                0x101009e, 0x101009c
            }, new int[] {
                0x101009e
            }, new int[0]
        }, new int[] {
            0xff000000, 0xff000000, -1, 0xff808080
        });
    }

    public float getPaddingLarge()
    {
        return _padding_large;
    }

    public float getPaddingNormal()
    {
        return _padding_normal;
    }

    public float getPaddingSmall()
    {
        return _padding_small;
    }

    public float getTextSizeLarge()
    {
        return _text_size_large;
    }

    public float getTextSizeNormal()
    {
        return _text_size_normal;
    }

    public float getTextSizeSmall()
    {
        return _text_size_small;
    }

    public float getTextSizeUltraLarge()
    {
        return _text_size_ultra_large;
    }

}
