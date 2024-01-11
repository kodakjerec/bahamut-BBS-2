// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

public class ASScrollView extends LinearLayout
    implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener
{

    private int _content_size_height;
    private int _content_size_width;
    private View _content_view;
    private ScaleGestureDetector _scale_detector;
    private GestureDetector _scroll_detector;

    public ASScrollView(Context context)
    {
        super(context);
        _scroll_detector = null;
        _scale_detector = null;
        _content_view = null;
        _content_size_width = 0;
        _content_size_height = 0;
        initial(context);
    }

    public ASScrollView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        _scroll_detector = null;
        _scale_detector = null;
        _content_view = null;
        _content_size_width = 0;
        _content_size_height = 0;
        initial(context);
    }

    private void initial(Context context)
    {
        _scroll_detector = new GestureDetector(context, this);
        _scale_detector = new ScaleGestureDetector(context, this);
    }

    public boolean onDown(MotionEvent motionevent)
    {
        return true;
    }

    public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
    {
        return false;
    }

    public void onLongPress(MotionEvent motionevent)
    {
    }

    public boolean onScale(ScaleGestureDetector scalegesturedetector)
    {
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector scalegesturedetector)
    {
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector scalegesturedetector)
    {
    }

    public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
    {
        if (_content_view == null)
        {
            reload();
        }
        if (_content_view != null)
        {
            _content_size_width = _content_view.getWidth();
            _content_size_height = _content_view.getHeight();
            int i = getScrollX();
            int l = getScrollY();
            int j = (int)((float)i + f);
            int k = _content_size_width - getWidth();
            i = j;
            if (j > k)
            {
                i = k;
            }
            j = i;
            if (i < 0)
            {
                j = 0;
            }
            k = (int)((float)l + f1);
            l = _content_size_height - getHeight();
            i = k;
            if (k > l)
            {
                i = l;
            }
            k = i;
            if (i < 0)
            {
                k = 0;
            }
            scrollTo(j, k);
        }
        return false;
    }

    public void onShowPress(MotionEvent motionevent)
    {
    }

    public boolean onSingleTapUp(MotionEvent motionevent)
    {
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionevent)
    {
        if (motionevent.getPointerCount() == 2)
        {
            return _scale_detector.onTouchEvent(motionevent);
        } else
        {
            return _scroll_detector.onTouchEvent(motionevent);
        }
    }

    public void reload()
    {
        if (getChildCount() > 0)
        {
            _content_view = getChildAt(0);
        }
    }

    public void setContentSize(int i, int j)
    {
        _content_size_width = i;
        _content_size_height = j;
    }
}
