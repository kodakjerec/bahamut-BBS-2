package com.kota.ASFramework.PageController

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/* loaded from: classes.dex */
class ASLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    fun notifyDataSetChanged() {
        // Empty implementation
    }

    override fun requestLayout() {
        super.requestLayout()
    }
}
