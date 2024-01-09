package com.kota.ASFramework.PageController;

import android.view.MotionEvent;

public interface ASGestureViewDelegate {
    void onASGestureDispathTouchEvent(MotionEvent motionEvent);

    boolean onASGestureReceivedGestureDown();

    boolean onASGestureReceivedGestureLeft();

    boolean onASGestureReceivedGestureRight();

    boolean onASGestureReceivedGestureUp();
}
