package com.kota.ASFramework.PageController

import android.view.MotionEvent

interface ASGestureViewDelegate {
    fun onASGestureDispathTouchEvent(paramMotionEvent: MotionEvent?)

    fun onASGestureReceivedGestureDown(): Boolean

    fun onASGestureReceivedGestureLeft(): Boolean

    fun onASGestureReceivedGestureRight(): Boolean

    fun onASGestureReceivedGestureUp(): Boolean
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASGestureViewDelegate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


