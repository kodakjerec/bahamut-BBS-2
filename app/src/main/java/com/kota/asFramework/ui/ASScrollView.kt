package com.kota.asFramework.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isNotEmpty

class ASScrollView : LinearLayout, GestureDetector.OnGestureListener, OnScaleGestureListener {
    private var contentSizeHeight = 0

    private var contentSizeWidth = 0

    private var contentView: View? = null

    private var scaleDetector: ScaleGestureDetector? = null

    private var scrollDetector: GestureDetector? = null

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
        this.scrollDetector = GestureDetector(paramContext, this)
        this.scaleDetector = ScaleGestureDetector(paramContext, this)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        paramMotionEvent2: MotionEvent?,
        p1: MotionEvent,
        paramFloat2: Float,
        p3: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent) {}

    override fun onScale(p0: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector) {}

    override fun onScroll(
        paramMotionEvent1: MotionEvent?,
        paramMotionEvent2: MotionEvent,
        paramFloat1: Float,
        paramFloat2: Float
    ): Boolean {
        if (this.contentView == null) reload()
        if (this.contentView != null) {
            this.contentSizeWidth = this.contentView!!.width
            this.contentSizeHeight = this.contentView!!.height
            var i = scrollX
            var m = scrollY
            var j = (i + paramFloat1).toInt()
            var k = this.contentSizeWidth - width
            i = j
            if (j > k) i = k
            j = i
            if (i < 0) j = 0
            m = (m + paramFloat2).toInt()
            k = this.contentSizeHeight - height
            i = m
            if (m > k) i = k
            k = i
            if (i < 0) k = 0
            scrollTo(j, k)
        }
        return false
    }

    override fun onShowPress(p0: MotionEvent) {}

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(paramMotionEvent: MotionEvent): Boolean {
        return if (paramMotionEvent.pointerCount == 2) this.scaleDetector!!.onTouchEvent(
            paramMotionEvent
        ) else this.scrollDetector!!.onTouchEvent(paramMotionEvent)
    }

    fun reload() {
        if (isNotEmpty()) this.contentView = getChildAt(0)
    }

    fun setContentSize(paramInt1: Int, paramInt2: Int) {
        this.contentSizeWidth = paramInt1
        this.contentSizeHeight = paramInt2
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\UI\ASScrollView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


