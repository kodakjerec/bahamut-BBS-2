package com.kota.ASFramework.UI

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.LinearLayout

class ASScrollView : LinearLayout, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    
    private var _content_size_height = 0
    private var _content_size_width = 0
    private var _content_view: View? = null
    private var _scale_detector: ScaleGestureDetector? = null
    private var _scroll_detector: GestureDetector? = null
    
    constructor(context: Context) : super(context) {
        initial(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initial(context)
    }
    
    private fun initial(context: Context) {
        _scroll_detector = GestureDetector(context, this)
        _scale_detector = ScaleGestureDetector(context, this)
    }
    
    override fun onDown(e: MotionEvent): Boolean {
        return true
    }
    
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
    
    override fun onLongPress(e: MotionEvent) {
        // 空實現
    }
    
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        return true
    }
    
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }
    
    override fun onScaleEnd(detector: ScaleGestureDetector) {
        // 空實現
    }
    
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (_content_view == null) {
            reload()
        }
        
        _content_view?.let { contentView ->
            _content_size_width = contentView.width
            _content_size_height = contentView.height
            
            val currentScrollX = scrollX
            val currentScrollY = scrollY
            
            // 計算新的 X 座標
            var newScrollX = (currentScrollX + distanceX).toInt()
            val maxScrollX = _content_size_width - width
            newScrollX = when {
                newScrollX > maxScrollX -> maxScrollX
                newScrollX < 0 -> 0
                else -> newScrollX
            }
            
            // 計算新的 Y 座標
            var newScrollY = (currentScrollY + distanceY).toInt()
            val maxScrollY = _content_size_height - height
            newScrollY = when {
                newScrollY > maxScrollY -> maxScrollY
                newScrollY < 0 -> 0
                else -> newScrollY
            }
            
            scrollTo(newScrollX, newScrollY)
        }
        
        return false
    }
    
    override fun onShowPress(e: MotionEvent) {
        // 空實現
    }
    
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (event.pointerCount == 2) {
            _scale_detector?.onTouchEvent(event) ?: false
        } else {
            _scroll_detector?.onTouchEvent(event) ?: false
        }
    }
    
    fun reload() {
        if (childCount > 0) {
            _content_view = getChildAt(0)
        }
    }
    
    fun setContentSize(width: Int, height: Int) {
        _content_size_width = width
        _content_size_height = height
    }
}
