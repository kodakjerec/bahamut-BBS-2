// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.kumi.ASFramework.PageController.ASGestureView;

// Referenced classes of package com.kumi.ASFramework.UI:
//            ASListViewExtentOptionalDelegate, ASListViewOverscrollDelegate

public class ASListView extends ListView
    implements GestureDetector.OnGestureListener
{

    private GestureDetector _gesture_detector;
    private boolean _scroll_on_bottom;
    private boolean _scroll_on_top;
    public ASListViewExtentOptionalDelegate extendOptionalDelegate;
    public ASListViewOverscrollDelegate overscrollDelegate;

    public ASListView(Context context)
    {
        super(context);
        _gesture_detector = null;
        _scroll_on_top = false;
        _scroll_on_bottom = false;
        extendOptionalDelegate = null;
        overscrollDelegate = null;
        init();
    }

    public ASListView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        _gesture_detector = null;
        _scroll_on_top = false;
        _scroll_on_bottom = false;
        extendOptionalDelegate = null;
        overscrollDelegate = null;
        init();
    }

    public ASListView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
        _gesture_detector = null;
        _scroll_on_top = false;
        _scroll_on_bottom = false;
        extendOptionalDelegate = null;
        overscrollDelegate = null;
        init();
    }

    private void detectScrollPosition(MotionEvent motionevent)
    {
        this;
        JVM INSTR monitorenter ;
        _scroll_on_top = false;
        _scroll_on_bottom = false;
        if (getChildCount() <= 0)
        {
            break MISSING_BLOCK_LABEL_83;
        }
        if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0)
        {
            _scroll_on_top = true;
        }
        if (getLastVisiblePosition() == getCount() - 1 && getChildAt(getChildCount() - 1).getBottom() <= getHeight())
        {
            _scroll_on_bottom = true;
        }
_L2:
        this;
        JVM INSTR monitorexit ;
        return;
        _scroll_on_top = true;
        _scroll_on_bottom = true;
        if (true) goto _L2; else goto _L1
_L1:
        motionevent;
        this;
        JVM INSTR monitorexit ;
        throw motionevent;
    }

    private void init()
    {
        _gesture_detector = new GestureDetector(getContext(), this);
    }

    public boolean onDown(MotionEvent motionevent)
    {
        return true;
    }

    public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
    {
        float f2;
        f = Math.abs(f);
        f2 = Math.abs(f1);
        if (extendOptionalDelegate == null || f <= (float)ASGestureView.filter || f <= ASGestureView.range * f2 || getChildCount() <= 0) goto _L2; else goto _L1
_L1:
        this;
        JVM INSTR monitorenter ;
        float f3 = motionevent.getY();
        int i = 0;
_L16:
        if (i >= getChildCount()) goto _L4; else goto _L3
_L3:
        motionevent = getChildAt(i);
        if (motionevent.getHeight() <= 0 || (float)motionevent.getTop() > f3 || (float)motionevent.getBottom() < f3) goto _L6; else goto _L5
_L5:
        int j = getFirstVisiblePosition();
        if (extendOptionalDelegate.onASListViewHandleExtentOptional(this, j + i))
        {
            motionevent = MotionEvent.obtain(motionevent1);
            motionevent.setAction(3);
            super.onTouchEvent(motionevent);
        }
_L4:
        this;
        JVM INSTR monitorexit ;
_L2:
        if (overscrollDelegate == null || f2 <= (float)ASGestureView.filter || f2 <= ASGestureView.range * f || getChildCount() <= 0) goto _L8; else goto _L7
_L7:
        this;
        JVM INSTR monitorenter ;
        if (f1 >= 0.0F) goto _L10; else goto _L9
_L9:
        if (!_scroll_on_bottom) goto _L10; else goto _L11
_L11:
        overscrollDelegate.onASListViewDelectedOverscrollTop(this);
_L13:
        _scroll_on_top = false;
        _scroll_on_bottom = false;
        this;
        JVM INSTR monitorexit ;
_L8:
        return true;
_L6:
        i++;
        continue; /* Loop/switch isn't completed */
        motionevent;
        this;
        JVM INSTR monitorexit ;
        throw motionevent;
_L10:
        if (f1 <= 0.0F) goto _L13; else goto _L12
_L12:
        if (!_scroll_on_top) goto _L13; else goto _L14
_L14:
        overscrollDelegate.onASListViewDelectedOverscrollBottom(this);
          goto _L13
        motionevent;
        this;
        JVM INSTR monitorexit ;
        throw motionevent;
        if (true) goto _L16; else goto _L15
_L15:
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
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionevent)
    {
        _gesture_detector.onTouchEvent(motionevent);
        if (motionevent.getAction() == 0)
        {
            detectScrollPosition(motionevent);
        }
        return super.onTouchEvent(motionevent);
    }
}
