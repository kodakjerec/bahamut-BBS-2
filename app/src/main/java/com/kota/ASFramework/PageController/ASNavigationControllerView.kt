package com.kota.ASFramework.PageController

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import com.kota.ASFramework.Model.ASSize

/* loaded from: classes.dex */
class ASNavigationControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ASPageView(context, attrs, defStyleAttr), ASGestureViewDelegate {

    private var _background_view: ASPageView? = null
    private var _content_view: ASPageView? = null
    private var _gesture_view: ASGestureView? = null
    private var _content_size = ASSize(0, 0)
    private var _page_controller: ASNavigationController? = null

    init {
        initial(context)
    }

    private fun initial(context: Context) {
        _background_view = ASPageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        addView(_background_view)

        _content_view = ASPageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        addView(_content_view)

        _gesture_view = ASGestureView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setDelegate(this@ASNavigationControllerView)
        }
        addView(_gesture_view)
    }

    fun getBackgroundView(): ASPageView? = _background_view

    fun getContentView(): ASPageView? = _content_view

    fun getContentSize(): ASSize = _content_size

    fun setPageController(aController: ASNavigationController) {
        _page_controller = aController
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _page_controller?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onASGestureDispathTouchEvent(event: MotionEvent) {
        _content_view?.dispatchTouchEvent(event)
    }

    override fun onASGestureReceivedGestureUp(): Boolean =
        _page_controller?.onReceivedGestureUp() ?: false

    override fun onASGestureReceivedGestureDown(): Boolean =
        _page_controller?.onReceivedGestureDown() ?: false

    override fun onASGestureReceivedGestureLeft(): Boolean =
        _page_controller?.onReceivedGestureLeft() ?: false

    override fun onASGestureReceivedGestureRight(): Boolean =
        _page_controller?.onReceivedGestureRight() ?: false
}
