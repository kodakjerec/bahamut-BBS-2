package com.kota.asFramework.pageController

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

class ASGestureView(paramContext: Context?) : FrameLayout(paramContext),
    GestureDetector.OnGestureListener {
    private var gestureViewDelegate: ASGestureViewDelegate? = null

    private var eventLocked = false

    private val gestureDetector: GestureDetector = GestureDetector(paramContext, this)

    init {
        filter = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            550.0f,
            paramContext.resources.displayMetrics
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
        var paramMotionEvent1: MotionEvent
        var bool1 = false
        if (this.gestureViewDelegate != null) {
            val f2 = abs(paramFloat1)
            val f1 = abs(paramFloat2)
            if (f2 > filter && f2 > range * f1) {
                bool1 = if (paramFloat1 > 0.0f) {
                    this.gestureViewDelegate?.onASGestureReceivedGestureRight()
                } else {
                    this.gestureViewDelegate?.onASGestureReceivedGestureLeft()
                }
            } else {
                if (f1 > filter) {
                    if (f1 > range * f2) bool1 = if (paramFloat2 > 0.0f) {
                        this.gestureViewDelegate?.onASGestureReceivedGestureDown()
                    } else {
                        this.gestureViewDelegate?.onASGestureReceivedGestureUp()
                    }
                }
            }
        }
        this.eventLocked = bool1
        if (bool1 && this.gestureViewDelegate != null) {
            paramMotionEvent1 = MotionEvent.obtain(paramMotionEvent2)
            paramMotionEvent1.action = 3
            this.gestureViewDelegate?.onASGestureDisPathTouchEvent(paramMotionEvent1)
            paramMotionEvent1.recycle()
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
        if (!this.eventLocked && this.gestureViewDelegate != null) this.gestureViewDelegate?.onASGestureDisPathTouchEvent(
            paramMotionEvent
        )
        if (paramMotionEvent.action == 1) this.eventLocked = false
        return true
    }

    fun setDelegate(paramASGestureViewDelegate: ASGestureViewDelegate?) {
        this.gestureViewDelegate = paramASGestureViewDelegate
    }

    companion object {
        var filter: Int = 550

        var range: Float = 2.4f
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASGestureView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


