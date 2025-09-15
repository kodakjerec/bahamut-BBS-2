package com.kota.ASFramework.PageController

import android.view.MotionEvent

interface ASGestureViewDelegate {
    fun onASGestureDispathTouchEvent(event: MotionEvent)
    fun onASGestureReceivedGestureDown(): Boolean
    fun onASGestureReceivedGestureLeft(): Boolean
    fun onASGestureReceivedGestureRight(): Boolean
    fun onASGestureReceivedGestureUp(): Boolean
}
