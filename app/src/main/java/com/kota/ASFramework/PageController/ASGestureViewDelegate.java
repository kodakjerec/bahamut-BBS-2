// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.view.MotionEvent;

public interface ASGestureViewDelegate
{

    public abstract void onASGestureDispathTouchEvent(MotionEvent motionevent);

    public abstract boolean onASGestureReceivedGestureDown();

    public abstract boolean onASGestureReceivedGestureLeft();

    public abstract boolean onASGestureReceivedGestureRight();

    public abstract boolean onASGestureReceivedGestureUp();
}
