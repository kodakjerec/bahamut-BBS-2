package com.kota.asFramework.pageController

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

open class ASPageView : FrameLayout {
    var ownerController: ASViewController?

    constructor(context: Context) : super(context) {
        this.ownerController = null
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.ownerController = null
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.ownerController = null
        init()
    }

    private fun init() {
    }

    // android.view.View
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val saveCount = canvas.saveCount
        dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    // android.view.View
    override fun onDraw(canvas: Canvas) {
    }

    fun onPageAnimationStart() {
    }

    fun onPageAnimationFinished() {
    }

    companion object {
        private const val COUNT = 0
    }
}
