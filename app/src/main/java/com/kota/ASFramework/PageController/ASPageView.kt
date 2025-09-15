package com.kota.ASFramework.PageController

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

/* loaded from: classes.dex */
class ASPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private var _count = 0
    }

    private var _owner_controller: ASViewController? = null

    init {
        init()
    }

    private fun init() {
        // Empty initialization
    }

    fun setOwnerController(aController: ASViewController?) {
        _owner_controller = aController
    }

    fun getOwnerController(): ASViewController? = _owner_controller

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val saveCount = canvas.saveCount
        dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onDraw(aCanvas: Canvas) {
        // Empty implementation
    }

    open fun onPageAnimationStart() {
        // Empty implementation
    }

    open fun onPageAnimationFinished() {
        // Empty implementation
    }
}
