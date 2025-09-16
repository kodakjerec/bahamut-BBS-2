package com.kota.ASFramework.UI

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.widget.LinearLayout

class ASScrollView : LinearLayout, GestureDetector.OnGestureListener, OnScaleGestureListener {
    private var _content_size_height = 0

    private var _content_size_width = 0

    private var _content_view: View? = null

    private var _scale_detector: ScaleGestureDetector? = null

    private var _scroll_detector: GestureDetector? = null

    constructor(paramContext: Context) : super(paramContext) {
        initial(paramContext)
    }

    constructor(paramContext: Context, paramAttributeSet: AttributeSet?) : super(
        paramContext,
        paramAttributeSet
    ) {
        initial(paramContext)
    }

    private fun initial(paramContext: Context) {
        this._scroll_detector = GestureDetector(paramContext, this)
        this._scale_detector = ScaleGestureDetector(paramContext, this)
    }

    override fun onDown(paramMotionEvent: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent?,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(paramMotionEvent: MotionEvent?) {}

    override fun onScale(paramScaleGestureDetector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleBegin(paramScaleGestureDetector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(paramScaleGestureDetector: ScaleGestureDetector?) {}

    override fun onScroll(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent?,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        if (this._content_view == null) reload()
        if (this._content_view != null) {
            this._content_size_width = this._content_view!!.getWidth()
            this._content_size_height = this._content_view!!.getHeight()
            var i = getScrollX()
            var m = getScrollY()
            var j = (i + paramFloat1).toInt()
            var k = this._content_size_width - getWidth()
            i = j
            if (j > k) i = k
            j = i
            if (i < 0) j = 0
            m = (m + paramFloat2).toInt()
            k = this._content_size_height - getHeight()
            i = m
            if (m > k) i = k
            k = i
            if (i < 0) k = 0
            scrollTo(j, k)
        }
        return false
    }

    override fun onShowPress(paramMotionEvent: MotionEvent?) {}

    override fun onSingleTapUp(paramMotionEvent: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        return if (paramMotionEvent.getPointerCount() == 2) this._scale_detector!!.onTouchEvent(
            paramMotionEvent
        ) else this._scroll_detector!!.onTouchEvent(paramMotionEvent)
    }

    fun reload() {
        if (getChildCount() > 0) this._content_view = getChildAt(0)
    }

    fun setContentSize(paramInt1: Int, paramInt2: Int) {
        this._content_size_width = paramInt1
        this._content_size_height = paramInt2
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\UI\ASScrollView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


