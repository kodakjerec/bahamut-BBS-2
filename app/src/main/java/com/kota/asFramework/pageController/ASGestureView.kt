package com.kota.asFramework.pageController

import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

class ASGestureView(paramContext: Context) : FrameLayout(paramContext),
    GestureDetector.OnGestureListener {
    private var _delegate: ASGestureViewDelegate? = null

    private var _event_locked = false

    private val _gesture_detector: GestureDetector

    init {
        this._gesture_detector = GestureDetector(paramContext, this)
        filter = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            550.0f,
            paramContext.getResources().getDisplayMetrics()
        ).toInt()
    }

    override fun onDown(paramMotionEvent: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(
        paramMotionEvent1: MotionEvent,
        paramMotionEvent2: MotionEvent?,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        var paramMotionEvent1 = paramMotionEvent1
        var bool1 = false
        if (this._delegate != null) {
            val f2 = abs(paramFloat1)
            val f1 = abs(paramFloat2)
            if (f2 > filter && f2 > range * f1) {
                if (paramFloat1 > 0.0f) {
                    bool1 = this._delegate!!.onASGestureReceivedGestureRight()
                } else {
                    bool1 = this._delegate!!.onASGestureReceivedGestureLeft()
                }
            } else {
                if (f1 > filter) {
                    if (f1 > range * f2) if (paramFloat2 > 0.0f) {
                        bool1 = this._delegate!!.onASGestureReceivedGestureDown()
                    } else {
                        bool1 = this._delegate!!.onASGestureReceivedGestureUp()
                    }
                }
            }
        }
        this._event_locked = bool1
        if (bool1 && this._delegate != null) {
            paramMotionEvent1 = MotionEvent.obtain(paramMotionEvent2)
            paramMotionEvent1.setAction(3)
            this._delegate!!.onASGestureDispathTouchEvent(paramMotionEvent1)
            paramMotionEvent1.recycle()
        }
        return bool1
    }

    override fun onLongPress(paramMotionEvent: MotionEvent?) {}

    override fun onScroll(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent?,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(paramMotionEvent: MotionEvent?) {}

    override fun onSingleTapUp(paramMotionEvent: MotionEvent?): Boolean {
        return true
    }

    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        this._gesture_detector.onTouchEvent(paramMotionEvent)
        if (!this._event_locked && this._delegate != null) this._delegate!!.onASGestureDispathTouchEvent(
            paramMotionEvent
        )
        if (paramMotionEvent.getAction() == 1) this._event_locked = false
        return true
    }

    fun setDelegate(paramASGestureViewDelegate: ASGestureViewDelegate?) {
        this._delegate = paramASGestureViewDelegate
    }

    companion object {
        var filter: Int = 550

        var range: Float = 2.4f
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASGestureView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


