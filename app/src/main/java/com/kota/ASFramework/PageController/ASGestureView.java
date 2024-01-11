// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASGestureViewDelegate

public class ASGestureView extends FrameLayout
    implements GestureDetector.OnGestureListener
{

    public static int filter = 550;
    public static float range = 2.4F;
    private ASGestureViewDelegate _delegate;
    private boolean _event_locked;
    private GestureDetector _gesture_detector;

    public ASGestureView(Context context)
    {
        super(context);
        _gesture_detector = null;
        _delegate = null;
        _event_locked = false;
        _gesture_detector = new GestureDetector(context, this);
        filter = (int)TypedValue.applyDimension(1, 550F, context.getResources().getDisplayMetrics());
    }

    public boolean onDown(MotionEvent motionevent)
    {
        return true;
    }

    public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
    {
        boolean flag;
        boolean flag1;
        flag1 = false;
        flag = flag1;
        if (_delegate == null) goto _L2; else goto _L1
_L1:
        float f2;
        float f3;
        f2 = Math.abs(f);
        f3 = Math.abs(f1);
        if (f2 <= (float)filter || f2 <= range * f3) goto _L4; else goto _L3
_L3:
        if (f > 0.0F)
        {
            flag = _delegate.onASGestureReceivedGestureRight();
        } else
        {
            flag = _delegate.onASGestureReceivedGestureLeft();
        }
_L2:
        _event_locked = flag;
        if (flag && _delegate != null)
        {
            motionevent = MotionEvent.obtain(motionevent1);
            motionevent.setAction(3);
            _delegate.onASGestureDispathTouchEvent(motionevent);
            motionevent.recycle();
        }
        return flag;
_L4:
        flag = flag1;
        if (f3 > (float)filter)
        {
            flag = flag1;
            if (f3 > range * f2)
            {
                if (f1 > 0.0F)
                {
                    flag = _delegate.onASGestureReceivedGestureDown();
                } else
                {
                    flag = _delegate.onASGestureReceivedGestureUp();
                }
            }
        }
        if (true) goto _L2; else goto _L5
_L5:
    }

    public void onLongPress(MotionEvent motionevent)
    {
    }

    public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
    {
        return false;
    }

    public void onShowPress(MotionEvent motionevent)
    {
    }

    public boolean onSingleTapUp(MotionEvent motionevent)
    {
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionevent)
    {
        _gesture_detector.onTouchEvent(motionevent);
        if (!_event_locked && _delegate != null)
        {
            _delegate.onASGestureDispathTouchEvent(motionevent);
        }
        if (motionevent.getAction() == 1)
        {
            _event_locked = false;
        }
        return true;
    }

    public void setDelegate(ASGestureViewDelegate asgestureviewdelegate)
    {
        _delegate = asgestureviewdelegate;
    }

}
