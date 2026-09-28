package com.kota.asFramework.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ListView
import androidx.core.view.isNotEmpty
import androidx.core.view.size
import com.kota.asFramework.pageController.ASGestureView
import kotlin.math.abs

/**
 * [ASListView] - 擴充型 ListView 元件。
 *
 * 職責：
 * 1. 結合 [GestureDetector] 支援橫向快速手勢偵測。
 * 2. 透過 [extendOptionalDelegate] 將橫向手勢轉發予 [ASListViewExtentOptionalDelegate]（例如左滑/右滑快捷操作）。
 */
class ASListView : ListView, GestureDetector.OnGestureListener {

    private var gestureDetector: GestureDetector? = null

    var isScrolledToBottom: Boolean = false
        private set

    var isScrolledToTop: Boolean = false
        private set

    @JvmField
    var extendOptionalDelegate: ASListViewExtentOptionalDelegate? = null

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    private fun init() {
        this.gestureDetector = GestureDetector(context, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.gestureDetector?.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            detectScrollPosition(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onFling(e2: MotionEvent?, e1: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val distanceX = abs(velocityX)
        val distanceY = abs(velocityY)
        if (this.extendOptionalDelegate != null && distanceX > ASGestureView.filter && distanceX > ASGestureView.range * distanceY && isNotEmpty()) {
            synchronized(this) {
                val positionY = e1.y
                var i = 0
                while (i < size) {
                    val childView = getChildAt(i)
                    if (childView.height <= 0 || childView.top > positionY || childView.bottom < positionY) {
                        i++
                    } else {
                        val index = firstVisiblePosition + i
                        if (this.extendOptionalDelegate!!.onASListViewHandleExtentOptional(this, index)) {
                            val cancelEvent = MotionEvent.obtain(e2)
                            cancelEvent.action = MotionEvent.ACTION_CANCEL
                            super.onTouchEvent(cancelEvent)
                        }
                        return true
                    }
                }
            }
        }
        return true
    }

    override fun onLongPress(p0: MotionEvent) {}

    override fun onScroll(e2: MotionEvent?, p1: MotionEvent, distanceY: Float, p3: Float): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {}

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    /**
     * 偵測清單當前捲動位置是否位於最頂端或最底端
     */
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
