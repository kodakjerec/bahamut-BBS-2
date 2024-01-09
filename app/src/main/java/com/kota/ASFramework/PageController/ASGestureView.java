package com.kota.ASFramework.PageController;

import android.content.Context;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ASGestureView extends FrameLayout implements GestureDetector.OnGestureListener {
    public static int filter = 550;
    public static float range = 2.4f;
    private ASGestureViewDelegate _delegate = null;
    private boolean _event_locked = false;
    private GestureDetector _gesture_detector = null;

    public ASGestureView(Context context) {
        super(context);
        this._gesture_detector = new GestureDetector(context, this);
        filter = (int) TypedValue.applyDimension(1, 550.0f, context.getResources().getDisplayMetrics());
    }

    public void setDelegate(ASGestureViewDelegate aDelegate) {
        this._delegate = aDelegate;
    }

    public boolean onTouchEvent(MotionEvent event) {
        this._gesture_detector.onTouchEvent(event);
        if (!this._event_locked && this._delegate != null) {
            this._delegate.onASGestureDispathTouchEvent(event);
        }
        if (event.getAction() == 1) {
            this._event_locked = false;
        }
        return true;
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        if (this._delegate != null) {
            float distance_x = Math.abs(velocityX);
            float distance_y = Math.abs(velocityY);
            if (distance_x > ((float) filter) && distance_x > range * distance_y) {
                result = velocityX > 0.0f ? this._delegate.onASGestureReceivedGestureRight() : this._delegate.onASGestureReceivedGestureLeft();
            } else if (distance_y > ((float) filter) && distance_y > range * distance_x) {
                result = velocityY > 0.0f ? this._delegate.onASGestureReceivedGestureDown() : this._delegate.onASGestureReceivedGestureUp();
            }
        }
        this._event_locked = result;
        if (result && this._delegate != null) {
            MotionEvent cancel_event = MotionEvent.obtain(e2);
            cancel_event.setAction(3);
            this._delegate.onASGestureDispathTouchEvent(cancel_event);
            cancel_event.recycle();
        }
        return result;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}
