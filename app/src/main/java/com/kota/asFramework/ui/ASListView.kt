package com.kota.asFramework.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ListView
import com.kota.asFramework.pageController.ASGestureView
import kotlin.math.abs
import androidx.core.view.isNotEmpty
import androidx.core.view.size

class ASListView : ListView, GestureDetector.OnGestureListener {
    private var gestureDetector: GestureDetector?
    private var isScrolledToBottom: Boolean
    private var isScrolledToTop: Boolean
    @JvmField
    var extendOptionalDelegate: ASListViewExtentOptionalDelegate?
    var overscrollDelegate: ASListViewOverscrollDelegate?

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.gestureDetector = null
        this.isScrolledToTop = false
        this.isScrolledToBottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.gestureDetector = null
        this.isScrolledToTop = false
        this.isScrolledToBottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    constructor(context: Context?) : super(context) {
        this.gestureDetector = null
        this.isScrolledToTop = false
        this.isScrolledToBottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    private fun init() {
        this.gestureDetector = GestureDetector(context, this)
    }

    // android.widget.AbsListView, android.view.View
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.gestureDetector?.onTouchEvent(event)
        if (event.action == 0) {
            detectScrollPosition(event)
        }
        return super.onTouchEvent(event)
    }

    // 用户轻触触摸屏
    // android.view.GestureDetector.OnGestureListener
    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    // 用户按下触摸屏、快速移动后松开
    // android.view.GestureDetector.OnGestureListener
    override fun onFling(e2: MotionEvent?, e1: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val distanceX = abs(velocityX)
        val distanceY = abs(velocityY)
        if (this.extendOptionalDelegate != null && distanceX > ASGestureView.filter && distanceX > ASGestureView.range * distanceY && isNotEmpty()) {
            synchronized(this) {
                val positionY = e1.y
                var i = 0
                while (true) {
                    if (i >= size) {
                        break
                    }
                    val childView = getChildAt(i)
                    if (childView.height <= 0 || childView.top > positionY || childView.bottom < positionY) {
                        i++
                    } else {
                        val index = firstVisiblePosition + i
                        if (this.extendOptionalDelegate!!.onASListViewHandleExtentOptional(
                                this,
                                index
                            )
                        ) {
                            val cancelEvent = MotionEvent.obtain(e2)
                            cancelEvent.action = 3
                            super.onTouchEvent(cancelEvent)
                        }
                        return true
                    }
                }
            }
        }
        if (this.overscrollDelegate != null && distanceY > ASGestureView.filter && distanceY > ASGestureView.range * distanceX && isNotEmpty()) {
            synchronized(this) {
                if (velocityY < 0.0f) {
                    if (this.isScrolledToBottom) {
                        this.overscrollDelegate?.onASListViewDelectedOverscrollTop(this)
                        this.isScrolledToTop = false
                        this.isScrolledToBottom = false
                    }
                }
                if (velocityY > 0.0f && this.isScrolledToTop) {
                    this.overscrollDelegate?.onASListViewDelectedOverscrollBottom(this)
                }
                this.isScrolledToTop = false
                this.isScrolledToBottom = false
            }
            return true
        }
        return true
    }

    // 用户长按触摸屏
    // android.view.GestureDetector.OnGestureListener
    override fun onLongPress(p0: MotionEvent) {
    }

    // 用户按下触摸屏 & 拖动
    // android.view.GestureDetector.OnGestureListener
    override fun onScroll(e2: MotionEvent?, p1: MotionEvent, distanceY: Float, p3: Float): Boolean {
        return false
    }

    // 用户轻触触摸屏，尚未松开或拖动
    // android.view.GestureDetector.OnGestureListener
    override fun onShowPress(p0: MotionEvent) {
    }

    // 用户轻击屏幕后抬起
    // android.view.GestureDetector.OnGestureListener
    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    private fun detectScrollPosition(event: MotionEvent?) {
        synchronized(this) {
            this.isScrolledToTop = false
            this.isScrolledToBottom = false
            if (isNotEmpty()) {
                if (firstVisiblePosition == 0) {
                    val firstChild = getChildAt(0)
                    if (firstChild.top >= 0) {
                        this.isScrolledToTop = true
                    }
                }
                if (lastVisiblePosition == count - 1) {
                    val lastChild = getChildAt(childCount - 1)
                    if (lastChild.bottom <= height) {
                        this.isScrolledToBottom = true
                    }
                }
            } else {
                this.isScrolledToTop = true
                this.isScrolledToBottom = true
            }
        }
    }
}
