package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

public class ASScrollView extends LinearLayout implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    private int _content_size_height = 0;
    private int _content_size_width = 0;
    private View _content_view = null;
    private ScaleGestureDetector _scale_detector = null;
    private GestureDetector _scroll_detector = null;

    public ASScrollView(Context context) {
        super(context);
        initial(context);
    }

    public ASScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial(context);
    }

    private void initial(Context context) {
        this._scroll_detector = new GestureDetector(context, this);
        this._scale_detector = new ScaleGestureDetector(context, this);
    }

    public void setContentSize(int width, int height) {
        this._content_size_width = width;
        this._content_size_height = height;
    }

    public void reload() {
        if (getChildCount() > 0) {
            this._content_view = getChildAt(0);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            return this._scale_detector.onTouchEvent(event);
        }
        return this._scroll_detector.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (this._content_view == null) {
            reload();
        }
        if (this._content_view == null) {
            return false;
        }
        this._content_size_width = this._content_view.getWidth();
        this._content_size_height = this._content_view.getHeight();
        int scroll_x = getScrollX();
        int scroll_y = getScrollY();
        int scroll_x2 = (int) (((float) scroll_x) + distanceX);
        int scroll_limit_x = this._content_size_width - getWidth();
        if (scroll_x2 > scroll_limit_x) {
            scroll_x2 = scroll_limit_x;
        }
        if (scroll_x2 < 0) {
            scroll_x2 = 0;
        }
        int scroll_y2 = (int) (((float) scroll_y) + distanceY);
        int scroll_limit_y = this._content_size_height - getHeight();
        if (scroll_y2 > scroll_limit_y) {
            scroll_y2 = scroll_limit_y;
        }
        if (scroll_y2 < 0) {
            scroll_y2 = 0;
        }
        scrollTo(scroll_x2, scroll_y2);
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
    }
}
