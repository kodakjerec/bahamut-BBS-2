package com.kota.ASFramework.UI

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import com.kota.ASFramework.PageController.ASGestureView

class ASListView : ListView, GestureDetector.OnGestureListener {
    
    private var _gesture_detector: GestureDetector? = null
    private var _scroll_on_bottom = false
    private var _scroll_on_top = false
    var extendOptionalDelegate: ASListViewExtentOptionalDelegate? = null
    var overscrollDelegate: ASListViewOverscrollDelegate? = null
    
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    
    constructor(context: Context) : super(context) {
        init()
    }
    
    private fun init() {
        _gesture_detector = GestureDetector(context, this)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        _gesture_detector?.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            detectScrollPosition(event)
        }
        return super.onTouchEvent(event)
    }
    
    // 用户轻触触摸屏
    override fun onDown(e: MotionEvent): Boolean {
        return true
    }
    
    // 用户按下触摸屏、快速移动后松开
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val distanceX = kotlin.math.abs(velocityX)
        val distanceY = kotlin.math.abs(velocityY)
        
        // 處理水平滑動的擴展選項
        if (extendOptionalDelegate != null && 
            distanceX > ASGestureView.filter && 
            distanceX > ASGestureView.range * distanceY && 
            childCount > 0) {
            
            synchronized(this) {
                e1?.let { startEvent ->
                    val positionY = startEvent.y
                    for (i in 0 until childCount) {
                        val childView = getChildAt(i)
                        if (childView.height > 0 && 
                            childView.top <= positionY && 
                            childView.bottom >= positionY) {
                            
                            val index = firstVisiblePosition + i
                            if (extendOptionalDelegate?.onASListViewHandleExtentOptional(this, index) == true) {
                                val cancelEvent = MotionEvent.obtain(e2)
                                cancelEvent.action = MotionEvent.ACTION_CANCEL
                                super.onTouchEvent(cancelEvent)
                            }
                            return true
                        }
                    }
                }
            }
        }
        
        // 處理垂直滑動的過度滾動
        if (overscrollDelegate != null && 
            distanceY > ASGestureView.filter && 
            distanceY > ASGestureView.range * distanceX && 
            childCount > 0) {
            
            synchronized(this) {
                when {
                    velocityY < 0.0f && _scroll_on_bottom -> {
                        overscrollDelegate?.onASListViewDelectedOverscrollTop(this)
                        _scroll_on_top = false
                        _scroll_on_bottom = false
                    }
                    velocityY > 0.0f && _scroll_on_top -> {
                        overscrollDelegate?.onASListViewDelectedOverscrollBottom(this)
                    }
                }
                _scroll_on_top = false
                _scroll_on_bottom = false
            }
            return true
        }
        
        return true
    }
    
    // 用户长按触摸屏
    override fun onLongPress(e: MotionEvent) {
        // 空實現
    }
    
    // 用户按下触摸屏 & 拖动
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }
    
    // 用户轻触触摸屏，尚未松开或拖动
    override fun onShowPress(e: MotionEvent) {
        // 空實現
    }
    
    // 用户轻击屏幕后抬起
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }
    
    private fun detectScrollPosition(event: MotionEvent) {
        synchronized(this) {
            _scroll_on_top = false
            _scroll_on_bottom = false
            
            if (childCount > 0) {
                // 檢查是否滾動到頂部
                if (firstVisiblePosition == 0) {
                    val firstChild = getChildAt(0)
                    if (firstChild.top >= 0) {
                        _scroll_on_top = true
                    }
                }
                
                // 檢查是否滾動到底部
                if (lastVisiblePosition == count - 1) {
                    val lastChild = getChildAt(childCount - 1)
                    if (lastChild.bottom <= height) {
                        _scroll_on_bottom = true
                    }
                }
            } else {
                _scroll_on_top = true
                _scroll_on_bottom = true
            }
        }
    }
}
