package com.kota.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.kota.ASFramework.PageController.ASGestureView;

public class ASListView extends ListView implements GestureDetector.OnGestureListener {
    private GestureDetector _gesture_detector = null;
    private boolean _scroll_on_bottom = false;
    private boolean _scroll_on_top = false;
    public ASListViewExtentOptionalDelegate extendOptionalDelegate = null;
    public ASListViewOverscrollDelegate overscrollDelegate = null;

    public ASListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ASListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ASListView(Context context) {
        super(context);
        init();
    }

    private void init() {
        this._gesture_detector = new GestureDetector(getContext(), this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        this._gesture_detector.onTouchEvent(event);
        if (event.getAction() == 0) {
            detectScrollPosition(event);
        }
        return super.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent arg0) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distance_x = Math.abs(velocityX);
        float distance_y = Math.abs(velocityY);
        if (this.extendOptionalDelegate != null && distance_x > ((float) ASGestureView.filter) && distance_x > ASGestureView.range * distance_y && getChildCount() > 0) {
            synchronized (this) {
                float position_y = e1.getY();
                int i = 0;
                while (true) {
                    if (i >= getChildCount()) {
                        break;
                    }
                    View child_view = getChildAt(i);
                    if (child_view.getHeight() <= 0 || ((float) child_view.getTop()) > position_y || ((float) child_view.getBottom()) < position_y) {
                        i++;
                    } else {
                        if (this.extendOptionalDelegate.onASListViewHandleExtentOptional(this, getFirstVisiblePosition() + i)) {
                            MotionEvent cancel_event = MotionEvent.obtain(e2);
                            cancel_event.setAction(3);
                            super.onTouchEvent(cancel_event);
                        }
                    }
                }
            }
        }
        if (this.overscrollDelegate == null || distance_y <= ((float) ASGestureView.filter) || distance_y <= ASGestureView.range * distance_x || getChildCount() <= 0) {
            return true;
        }
        synchronized (this) {
            if (velocityY < 0.0f) {
                if (this._scroll_on_bottom) {
                    this.overscrollDelegate.onASListViewDelectedOverscrollTop(this);
                    this._scroll_on_top = false;
                    this._scroll_on_bottom = false;
                }
            }
            if (velocityY > 0.0f) {
                if (this._scroll_on_top) {
                    this.overscrollDelegate.onASListViewDelectedOverscrollBottom(this);
                }
            }
            this._scroll_on_top = false;
            this._scroll_on_bottom = false;
        }
        return true;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private void detectScrollPosition(MotionEvent event) {
        synchronized (this) {
            this._scroll_on_top = false;
            this._scroll_on_bottom = false;
            if (getChildCount() > 0) {
                if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0) {
                    this._scroll_on_top = true;
                }
                if (getLastVisiblePosition() == getCount() - 1 && getChildAt(getChildCount() - 1).getBottom() <= getHeight()) {
                    this._scroll_on_bottom = true;
                }
            } else {
                this._scroll_on_top = true;
                this._scroll_on_bottom = true;
            }
        }
    }
}
