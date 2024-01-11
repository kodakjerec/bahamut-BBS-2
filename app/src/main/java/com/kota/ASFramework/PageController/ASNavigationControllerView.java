// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.kumi.ASFramework.Model.ASSize;

// Referenced classes of package com.kumi.ASFramework.PageController:
//            ASPageView, ASGestureViewDelegate, ASGestureView, ASNavigationController

public class ASNavigationControllerView extends ASPageView
    implements ASGestureViewDelegate
{

    private ASPageView _background_view;
    private ASSize _content_size;
    private ASPageView _content_view;
    private ASGestureView _gesture_view;
    private ASNavigationController _page_controller;

    public ASNavigationControllerView(Context context)
    {
        super(context);
        _background_view = null;
        _content_view = null;
        _gesture_view = null;
        _content_size = new ASSize(0, 0);
        _page_controller = null;
        initial(context);
    }

    public ASNavigationControllerView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        _background_view = null;
        _content_view = null;
        _gesture_view = null;
        _content_size = new ASSize(0, 0);
        _page_controller = null;
        initial(context);
    }

    public ASNavigationControllerView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
        _background_view = null;
        _content_view = null;
        _gesture_view = null;
        _content_size = new ASSize(0, 0);
        _page_controller = null;
        initial(context);
    }

    private void initial(Context context)
    {
        _background_view = new ASPageView(context);
        _background_view.setLayoutParams(new LayoutParams(-1, -1));
        addView(_background_view);
        _content_view = new ASPageView(context);
        _content_view.setLayoutParams(new LayoutParams(-1, -1));
        addView(_content_view);
        _gesture_view = new ASGestureView(context);
        _gesture_view.setLayoutParams(new LayoutParams(-1, -1));
        _gesture_view.setDelegate(this);
        addView(_gesture_view);
    }

    public ASPageView getBackgroundView()
    {
        return _background_view;
    }

    public ASSize getContentSize()
    {
        return _content_size;
    }

    public ASPageView getContentView()
    {
        return _content_view;
    }

    public void onASGestureDispathTouchEvent(MotionEvent motionevent)
    {
        _content_view.dispatchTouchEvent(motionevent);
    }

    public boolean onASGestureReceivedGestureDown()
    {
        if (_page_controller != null)
        {
            return _page_controller.onReceivedGestureDown();
        } else
        {
            return false;
        }
    }

    public boolean onASGestureReceivedGestureLeft()
    {
        if (_page_controller != null)
        {
            return _page_controller.onReceivedGestureLeft();
        } else
        {
            return false;
        }
    }

    public boolean onASGestureReceivedGestureRight()
    {
        if (_page_controller != null)
        {
            return _page_controller.onReceivedGestureRight();
        } else
        {
            return false;
        }
    }

    public boolean onASGestureReceivedGestureUp()
    {
        if (_page_controller != null)
        {
            return _page_controller.onReceivedGestureUp();
        } else
        {
            return false;
        }
    }

    protected void onSizeChanged(int i, int j, int k, int l)
    {
        super.onSizeChanged(i, j, k, l);
        if (_page_controller != null)
        {
            _page_controller.onSizeChanged(i, j, k, l);
        }
    }

    public void setPageController(ASNavigationController asnavigationcontroller)
    {
        _page_controller = asnavigationcontroller;
    }
}
