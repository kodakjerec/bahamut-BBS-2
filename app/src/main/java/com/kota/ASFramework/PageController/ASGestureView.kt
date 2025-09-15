package com.kota.ASFramework.PageController

import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout

class ASGestureView(context: Context) : FrameLayout(context), GestureDetector.OnGestureListener {
    companion object {
        var filter = 550
        var range = 2.4f
    }

    private var _delegate: ASGestureViewDelegate? = null
    private var _event_locked = false
    private val _gesture_detector: GestureDetector

    init {
        _gesture_detector = GestureDetector(context, this)
        filter = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            550.0f, 
            context.resources.displayMetrics
        ).toInt()
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onFling(
        e1: MotionEvent?, 
        e2: MotionEvent, 
        velocityX: Float, 
        velocityY: Float
    ): Boolean {
        var gestureHandled = false
        
        _delegate?.let { delegate ->
            val absVelocityX = Math.abs(velocityX)
            val absVelocityY = Math.abs(velocityY)
            
            if (absVelocityX > filter && absVelocityX > range * absVelocityY) {
                gestureHandled = if (velocityX > 0.0f) {
                    delegate.onASGestureReceivedGestureRight()
                } else {
                    delegate.onASGestureReceivedGestureLeft()
                }
            } else if (absVelocityY > filter && absVelocityY > range * absVelocityX) {
                gestureHandled = if (velocityY > 0.0f) {
                    delegate.onASGestureReceivedGestureDown()
                } else {
                    delegate.onASGestureReceivedGestureUp()
                }
            }
        }
        
        _event_locked = gestureHandled
        
        if (gestureHandled && _delegate != null && e1 != null) {
            val cancelEvent = MotionEvent.obtain(e2)
            cancelEvent.action = MotionEvent.ACTION_CANCEL
            _delegate?.onASGestureDispathTouchEvent(cancelEvent)
            cancelEvent.recycle()
        }
        
        return gestureHandled
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onScroll(
        e1: MotionEvent?, 
        e2: MotionEvent, 
        distanceX: Float, 
        distanceY: Float
    ): Boolean = false

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        _gesture_detector.onTouchEvent(event)
        
        if (!_event_locked) {
            _delegate?.onASGestureDispathTouchEvent(event)
        }
        
        if (event.action == MotionEvent.ACTION_UP) {
            _event_locked = false
        }
        
        return true
    }

    fun setDelegate(delegate: ASGestureViewDelegate) {
        _delegate = delegate
    }
}
