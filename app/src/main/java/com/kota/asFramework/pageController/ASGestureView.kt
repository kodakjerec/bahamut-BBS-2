package com.kota.asFramework.pageController

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * [ASGestureView] - 手勢偵測 FrameLayout 容器。
 *
 * 職責：
 * 1. 攔截並分析上、下、左、右滑動手勢 ([onFling])。
 * 2. 透過 [ASGestureViewDelegate] 代理介面將滑動事件發送至頁面控制器（如向右滑動返回上頁）。
 */
class ASGestureView(paramContext: Context?) : FrameLayout(paramContext!!),
    GestureDetector.OnGestureListener {

    private var gestureViewDelegate: ASGestureViewDelegate? = null
    private var eventLocked = false
    private val gestureDetector: GestureDetector = GestureDetector(paramContext, this)

    init {
        filter = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            550.0f,
            paramContext!!.resources.displayMetrics
        ).toInt()
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        var bool1 = false
        if (this.gestureViewDelegate != null) {
            val f2 = abs(paramFloat1)
            val f1 = abs(paramFloat2)
            if (f2 > filter && f2 > range * f1) {
                bool1 = if (paramFloat1 > 0.0f) {
                    this.gestureViewDelegate!!.onASGestureReceivedGestureRight()
                } else {
                    this.gestureViewDelegate!!.onASGestureReceivedGestureLeft()
                }
            } else {
                if (f1 > filter) {
                    if (f1 > range * f2) {
                        bool1 = if (paramFloat2 > 0.0f) {
                            this.gestureViewDelegate!!.onASGestureReceivedGestureDown()
                        } else {
                            this.gestureViewDelegate!!.onASGestureReceivedGestureUp()
                        }
                    }
                }
            }
        }
        this.eventLocked = bool1
        if (bool1 && this.gestureViewDelegate != null) {
            val cancelEvent = MotionEvent.obtain(paramMotionEvent2)
            cancelEvent.action = MotionEvent.ACTION_CANCEL
            this.gestureViewDelegate?.onASGestureDisPathTouchEvent(cancelEvent)
            cancelEvent.recycle()
        }
        return bool1
    }

    override fun onLongPress(p0: MotionEvent) {}

    override fun onScroll(
        paramMotionEvent2: MotionEvent?,
        p1: MotionEvent,
        paramFloat2: Float,
        p3: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {}

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        this.gestureDetector.onTouchEvent(paramMotionEvent)
        if (!this.eventLocked && this.gestureViewDelegate != null) {
            this.gestureViewDelegate?.onASGestureDisPathTouchEvent(paramMotionEvent)
        }
        if (paramMotionEvent.action == MotionEvent.ACTION_UP) {
            this.eventLocked = false
        }
        return true
    }

    fun setDelegate(paramASGestureViewDelegate: ASGestureViewDelegate?) {
        this.gestureViewDelegate = paramASGestureViewDelegate
    }

    companion object {
        var filter: Int = 550
        var range: Float = 2.4f
    }
}
