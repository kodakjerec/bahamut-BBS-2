package com.kota.asFramework.pageController

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.kota.asFramework.model.ASSize

/* loaded from: classes.dex */
class ASNavigationControllerView : ASPageView, ASGestureViewDelegate {
    var backgroundView: ASPageView?
        private set
    val contentSize: ASSize?
    var contentView: ASPageView?
        private set
    private var _gesture_view: ASGestureView?
    private var _page_controller: ASNavigationController?

    constructor(context: Context?) : super(context) {
        this.backgroundView = null
        this.contentView = null
        this._gesture_view = null
        this.contentSize = ASSize(0, 0)
        this._page_controller = null
        initial(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.backgroundView = null
        this.contentView = null
        this._gesture_view = null
        this.contentSize = ASSize(0, 0)
        this._page_controller = null
        initial(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.backgroundView = null
        this.contentView = null
        this._gesture_view = null
        this.contentSize = ASSize(0, 0)
        this._page_controller = null
        initial(context)
    }

    private fun initial(context: Context?) {
        this.backgroundView = ASPageView(context)
        this.backgroundView!!.setLayoutParams(LayoutParams(-1, -1))
        addView(this.backgroundView)
        this.contentView = ASPageView(context)
        this.contentView!!.setLayoutParams(LayoutParams(-1, -1))
        addView(this.contentView)
        this._gesture_view = ASGestureView(context)
        this._gesture_view!!.setLayoutParams(LayoutParams(-1, -1))
        this._gesture_view!!.setDelegate(this)
        addView(this._gesture_view)
    }

    fun setPageController(aController: ASNavigationController?) {
        this._page_controller = aController
    }

    // android.view.View
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (this._page_controller != null) {
            this._page_controller!!.onSizeChanged(w, h, oldw, oldh)
        }
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureDispathTouchEvent(event: MotionEvent?) {
        this.contentView!!.dispatchTouchEvent(event)
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureUp(): Boolean {
        if (this._page_controller != null) {
            return this._page_controller!!.onReceivedGestureUp()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureDown(): Boolean {
        if (this._page_controller != null) {
            return this._page_controller!!.onReceivedGestureDown()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureLeft(): Boolean {
        if (this._page_controller != null) {
            return this._page_controller!!.onReceivedGestureLeft()
        }
        return false
    }

    // com.kota.ASFramework.PageController.ASGestureViewDelegate
    override fun onASGestureReceivedGestureRight(): Boolean {
        if (this._page_controller != null) {
            return this._page_controller!!.onReceivedGestureRight()
        }
        return false
    }
}
