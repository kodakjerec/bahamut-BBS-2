package com.kota.asFramework.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ListView
import com.kota.asFramework.pageController.ASGestureView
import kotlin.math.abs

/* loaded from: classes.dex */
class ASListView : ListView, GestureDetector.OnGestureListener {
    private var _gesture_detector: GestureDetector?
    private var _scroll_on_bottom: Boolean
    private var _scroll_on_top: Boolean
    @JvmField
    var extendOptionalDelegate: ASListViewExtentOptionalDelegate?
    var overscrollDelegate: ASListViewOverscrollDelegate?

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this._gesture_detector = null
        this._scroll_on_top = false
        this._scroll_on_bottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this._gesture_detector = null
        this._scroll_on_top = false
        this._scroll_on_bottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    constructor(context: Context?) : super(context) {
        this._gesture_detector = null
        this._scroll_on_top = false
        this._scroll_on_bottom = false
        this.extendOptionalDelegate = null
        this.overscrollDelegate = null
        init()
    }

    private fun init() {
        this._gesture_detector = GestureDetector(getContext(), this)
    }

    // android.widget.AbsListView, android.view.View
    override fun onTouchEvent(event: MotionEvent): Boolean {
        this._gesture_detector!!.onTouchEvent(event)
        if (event.getAction() == 0) {
            detectScrollPosition(event)
        }
        return super.onTouchEvent(event)
    }

    // 用户轻触触摸屏
    // android.view.GestureDetector.OnGestureListener
    override fun onDown(arg0: MotionEvent?): Boolean {
        return true
    }

    // 用户按下触摸屏、快速移动后松开
    // android.view.GestureDetector.OnGestureListener
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val distance_x = abs(velocityX)
        val distance_y = abs(velocityY)
        if (this.extendOptionalDelegate != null && distance_x > ASGestureView.filter && distance_x > ASGestureView.range * distance_y && getChildCount() > 0) {
            synchronized(this) {
                val position_y = e1.getY()
                var i = 0
                while (true) {
                    if (i >= getChildCount()) {
                        break
                    }
                    val child_view = getChildAt(i)
                    if (child_view.getHeight() <= 0 || child_view.getTop() > position_y || child_view.getBottom() < position_y) {
                        i++
                    } else {
                        val index = getFirstVisiblePosition() + i
                        if (this.extendOptionalDelegate!!.onASListViewHandleExtentOptional(
                                this,
                                index
                            )
                        ) {
                            val cancel_event = MotionEvent.obtain(e2)
                            cancel_event.setAction(3)
                            super.onTouchEvent(cancel_event)
                        }
                        return true
                    }
                }
            }
        }
        if (this.overscrollDelegate != null && distance_y > ASGestureView.filter && distance_y > ASGestureView.range * distance_x && getChildCount() > 0) {
            synchronized(this) {
                if (velocityY < 0.0f) {
                    if (this._scroll_on_bottom) {
                        this.overscrollDelegate!!.onASListViewDelectedOverscrollTop(this)
                        this._scroll_on_top = false
                        this._scroll_on_bottom = false
                    }
                }
                if (velocityY > 0.0f && this._scroll_on_top) {
                    this.overscrollDelegate!!.onASListViewDelectedOverscrollBottom(this)
                }
                this._scroll_on_top = false
                this._scroll_on_bottom = false
            }
            return true
        }
        return true
    }

    // 用户长按触摸屏
    // android.view.GestureDetector.OnGestureListener
    override fun onLongPress(e: MotionEvent?) {
    }

    // 用户按下触摸屏 & 拖动
    // android.view.GestureDetector.OnGestureListener
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    // 用户轻触触摸屏，尚未松开或拖动
    // android.view.GestureDetector.OnGestureListener
    override fun onShowPress(e: MotionEvent?) {
    }

    // 用户轻击屏幕后抬起
    // android.view.GestureDetector.OnGestureListener
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    private fun detectScrollPosition(event: MotionEvent?) {
        synchronized(this) {
            this._scroll_on_top = false
            this._scroll_on_bottom = false
            if (getChildCount() > 0) {
                if (getFirstVisiblePosition() == 0) {
                    val first_child = getChildAt(0)
                    if (first_child.getTop() >= 0) {
                        this._scroll_on_top = true
                    }
                }
                if (getLastVisiblePosition() == getCount() - 1) {
                    val last_child = getChildAt(getChildCount() - 1)
                    if (last_child.getBottom() <= getHeight()) {
                        this._scroll_on_bottom = true
                    }
                }
            } else {
                this._scroll_on_top = true
                this._scroll_on_bottom = true
            }
        }
    }
}
