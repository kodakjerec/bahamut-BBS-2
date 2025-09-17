package com.kota.asFramework.pageController

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class ASLinearLayout : LinearLayout {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    fun notifyDataSetChanged() {
    }

    // android.view.View, android.view.ViewParent
    override fun requestLayout() {
        super.requestLayout()
    }
}
