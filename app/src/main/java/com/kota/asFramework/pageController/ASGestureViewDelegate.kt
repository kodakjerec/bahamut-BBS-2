package com.kota.asFramework.pageController

import android.view.MotionEvent

/**
 * [ASGestureView] 滑動與觸控手勢偵測的回呼代理介面
 */
interface ASGestureViewDelegate {
    /**
     * 當手勢被鎖定或分發觸控事件時呼叫
     *
     * @param paramMotionEvent 觸控事件物件
     */
    fun onASGestureDisPathTouchEvent(paramMotionEvent: MotionEvent?)

    /** 收到向下滑動手勢時呼叫 */
    fun onASGestureReceivedGestureDown(): Boolean

    /** 收到向左滑動手勢時呼叫 */
    fun onASGestureReceivedGestureLeft(): Boolean

    /** 收到向右滑動手勢時呼叫 */
    fun onASGestureReceivedGestureRight(): Boolean

    /** 收到向上滑動手勢時呼叫 */
    fun onASGestureReceivedGestureUp(): Boolean
}
