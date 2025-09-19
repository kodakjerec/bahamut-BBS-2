package com.kota.asFramework.pageController

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.kota.asFramework.model.ASSize

class ASNavigationControllerView : ASPageView, ASGestureViewDelegate {
    var backgroundView: ASPageView?
        private set
    val contentSize: ASSize?
    var contentView: ASPageView?
        private set
    private var gestureView: ASGestureView?
    private var pageController: ASNavigationController?

    constructor(context: Context) : super(context) {
        this.backgroundView = null
        this.contentView = null
        this.gestureView = null
        this.contentSize = ASSize(0, 0)
        this.pageController = null
        initial(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.backgroundView = null
        this.contentView = null
        this.gestureView = null
        this.contentSize = ASSize(0, 0)
        this.pageController = null
        initial(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.backgroundView = null
        this.contentView = null
        this.gestureView = null
        this.contentSize = ASSize(0, 0)
        this.pageController = null
        initial(context)
    }

    private fun initial(context: Context) {
        this.backgroundView = ASPageView(context)
        this.backgroundView?.layoutParams = LayoutParams(-1, -1)
        addView(this.backgroundView)
        this.contentView = ASPageView(context)
        this.contentView?.layoutParams = LayoutParams(-1, -1)
        addView(this.contentView)
        this.gestureView = ASGestureView(context)
        this.gestureView?.layoutParams = LayoutParams(-1, -1)
        this.gestureView?.setDelegate(this)
        addView(this.gestureView)
    }

    fun setPageController(aController: ASNavigationController?) {
        this.pageController = aController
    }

    // android.view.View
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (this.pageController != null) {
            this.pageController?.onSizeChanged(w, h, oldw, oldh)
        }
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureDisPathTouchEvent(paramMotionEvent: MotionEvent?) {
        this.contentView?.dispatchTouchEvent(paramMotionEvent)
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureUp(): Boolean {
        if (this.pageController != null) {
            return this.pageController?.onReceivedGestureUp()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureDown(): Boolean {
        if (this.pageController != null) {
            return this.pageController?.onReceivedGestureDown()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureLeft(): Boolean {
        if (this.pageController != null) {
            return this.pageController?.onReceivedGestureLeft()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureRight(): Boolean {
        if (this.pageController != null) {
            return this.pageController?.onReceivedGestureRight()
        }
        return false
    }
}
